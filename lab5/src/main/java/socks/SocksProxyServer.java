package socks;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SocksProxyServer implements Runnable {

    private final Integer port;

    private static final Logger logger = Logger.getLogger(SocksProxyServer.class.getName());

    @SneakyThrows
    @Override
    public void run() {
        Selector selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.currentThread().isInterrupted()) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isValid()) {
                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isConnectable()) {
                            handleConnect(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        close(key);
                    }
                }
            }

        }
    }

    public static SocketChannel getChannel(@NotNull SelectionKey key) {
        return (SocketChannel) key.channel();
    }

    public static ByteChannel getByteChannel(@NotNull SelectionKey key) {
        return (ByteChannel) key.channel();
    }

    public static void clearIn(@NotNull SelectionKey key) {
        ((Attachment) key.attachment()).getIn().clear();
    }

    public static void clearOut(@NotNull SelectionKey key) {
        ((Attachment) key.attachment()).getOut().clear();
    }

    public static void clearBuffers(@NotNull SelectionKey key) {
        clearIn(key);
        clearOut(key);
    }

    public static boolean isDecoupled(@NotNull SelectionKey key) {
        return !((Attachment) key.attachment()).isCoupled();
    }


    public static void couple(InetAddress address, int port, @NotNull SelectionKey key) throws IOException {
        ((Attachment) key.attachment()).couple(address, port, key);

    }

    public static void close(@NotNull SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        ((Attachment) key.attachment()).decouple();
    }

    public static void partiallyClose(@NotNull SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }

    public static boolean tryWriteToBuffer(@NotNull SelectionKey key) throws IOException {
        ByteBuffer buf = ((Attachment) key.attachment()).getOut();
        buf.flip();
        int writeSymbols = getByteChannel(key).write(buf);
        logger.log(Level.INFO, "write symbols - " + writeSymbols);
        return (writeSymbols > 0);
    }

    public static boolean outIsEmpty(@NotNull SelectionKey key) {
        return (((Attachment) key.attachment()).getOut().remaining() == 0);
    }



    private void handleAccept(@NotNull SelectionKey key) throws IOException {
        // accept channel from user
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        // set socket non-blocking
        channel.configureBlocking(false);
        // set attachment
        key.attach(new Attachment(Type.AUTH_READ));
        // add channel to selector
        channel.register(key.selector(), SelectionKey.OP_READ);
    }

    private void handleConnect(SelectionKey key) throws IOException {
        getChannel(key).finishConnect();
        ((Attachment) key.attachment()).finishCouple();
        key.interestOps(0);
    }

    private void handleRead(@NotNull SelectionKey key) throws Exception {
        Attachment attach = ((Attachment) key.attachment());
        if (attach == null) {
            key.attach(new Attachment(Type.AUTH_READ));
        }
        attach = ((Attachment) key.attachment());
        int countReadSymbol = getByteChannel(key).read(((Attachment) key.attachment()).getIn());
        logger.log(Level.INFO, countReadSymbol  + " - count read symbols");
        if (countReadSymbol <= 0) {
            close(key);
            if (attach.getType() == Type.DNS_READ) {
                throw new Exception("Bad dns reply");
            }
        }
        switch (attach.getType()) {
            case AUTH_READ -> replySocksGreeting(key);
            case CONN_READ -> replySocksConn(key);
            case DNS_READ -> DnsUtils.read(key);
            default -> coupledToWrite(key);
        }

    }

    private void handleWrite(@NotNull SelectionKey key) throws IOException {
        Attachment attach = ((Attachment) key.attachment());
        if (!tryWriteToBuffer(key)) {
            close(key);
        } else if (outIsEmpty(key)) {
            switch (attach.getType()) {
                case AUTH_WRITE -> authWrite(key);
                case DNS_WRITE -> DnsUtils.write(key);
                default -> {
                    if (!((Attachment) key.attachment()).isCoupled()) {
                        close(key);
                    } else {
                        coupledToRead(key);
                    }
                }
            }
        }
    }

    private void coupledToWrite(SelectionKey key) throws IOException {
        if (isDecoupled(key)) {
            close(key);
            return;
        }
        ((Attachment) key.attachment()).addCoupledWrite();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
    }

    private void coupledToRead(SelectionKey key) throws IOException {
        if (isDecoupled(key)) {
            close(key);
            return;
        }
        clearBuffers(key);
        ((Attachment) key.attachment()).addCoupledRead();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }

    private void authWrite(SelectionKey key) {
        clearBuffers(key);
        key.interestOps(SelectionKey.OP_READ);
        ((Attachment) key.attachment()).setType(Type.CONN_READ);
    }

    private void replySocksConn(SelectionKey key) throws IllegalArgumentException, IOException {
        if (SocksMessage.inTooSmall(key, Constants.CONN_SMALLNESS)) {
            return;
        }
        if (SocksMessage.wrongVersion(key)) {
            throw new IllegalArgumentException("Wrong version, SOCKS5 expected");
        }
        if (SocksMessage.wrongCommand(key)) {
            throw new IllegalArgumentException("Wrong command, 0x01 expected");
        }
        if (SocksMessage.wrongAddressType(key)) {
            throw new IllegalArgumentException("Wrong address type, ipv4 or hostname expected");
        }

        if (SocksMessage.isIpv4(key)) {
            InetAddress address = InetAddress.getByAddress(SocksMessage.getIpv4(key));
            int port = SocksMessage.getPort(key, Constants.IP_PORT_POSITION);

            couple(address, port, key);
            key.interestOps(0);
        } else if (SocksMessage.isHost(key)) {
            if (SocksMessage.inTooSmall(key, Constants.HOST_SMALLNESS)) {
                return;
            }
            String hostname = SocksMessage.getHost(key);
            int port = SocksMessage.getPort(key, Constants.HOST_PORT_POSITION);

            key.interestOps(0);

            registerHostResolver(hostname, port, key);
        }

    }

    private void registerHostResolver(String hostname, int port, @NotNull SelectionKey key) throws IOException {
        DatagramChannel dnsChan = DatagramChannel.open();
        dnsChan.connect(ResolverConfig.getCurrentConfig().server());
        dnsChan.configureBlocking(false);

        SelectionKey dnsKey = dnsChan.register(key.selector(), SelectionKey.OP_WRITE);
        Attachment dnsAttach = new Attachment(port, key, Type.DNS_WRITE);

        Message message = new Message();
        Record record = Record.newRecord(Name.fromString(hostname + '.').canonicalize(), org.xbill.DNS.Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        Header header = message.getHeader();
        header.setFlag(Flags.AD);
        header.setFlag(Flags.RD);

        dnsAttach.getIn().put(message.toWire());
        dnsAttach.useInAsOut();

        dnsKey.attach(dnsAttach);
    }

    private void replySocksGreeting(@NotNull SelectionKey key) throws IllegalArgumentException {
        // get Attachment
        Attachment attachment = ((Attachment) key.attachment());
        // data not received in full
        if (SocksMessage.inTooSmall(key, Constants.AUTH_SMALLNESS)) {
            logger.log(Level.INFO, "Buffer to small, wait more data");
            // user use not socks5
        } else if (SocksMessage.wrongVersion(key)) {
            logger.log(Level.OFF, "Wrong Socks version");
            key.cancel();
            // user did not provide authentication methods
        } else if (!SocksMessage.methodsReceived(key)) {
            logger.log(Level.OFF, "No options to connect");
            key.cancel();
            // user did not provide authentication method without authentication :)
        } else if (SocksMessage.noAuthNotFound(key)) {
            throw new IllegalArgumentException("auth: noAuth method not found");
        } else {
            clearBuffers(key);
            SocksMessage.putMessage(((Attachment) key.attachment()).getOut(), Constants.SOCKS_VERSION, Constants.NO_AUTH);
            logger.log(Level.INFO, "Proxy-server accept user connection");
            attachment.setType(Type.AUTH_WRITE);
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
}
