package socks;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Type {
    NONE,
    CONN_READ,
    CONN_WRITE,
    DNS_READ,
    DNS_WRITE,
    AUTH_READ,
    AUTH_WRITE,
    WRITE_AND_CLOSE,
    READ_AND_CLOSE
}

@Data
class Attachment {
    private final static int BUFFER_CAPACITY = 4096;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Type type;
    private ByteBuffer in;
    private ByteBuffer out;
    private SelectionKey pair;

    // for connect
    private int port;

    public Attachment() {
        this.in = ByteBuffer.allocate(BUFFER_CAPACITY);
        this.out = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    public Attachment(Type type) {
        this();
        this.type = type;
    }

    public Attachment(int port, SelectionKey key, Type type) {
        this(type);
        this.port = port;
        this.pair = key;
    }

    public void decouple() {
        SelectionKey sendoff = this.pair;
        if (sendoff != null) {
            this.pair = null;
            if (sendoff.isValid()) {
                sendoff.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    public void couple(@NotNull InetAddress connectAddress, int connectPort, @NotNull SelectionKey parentKey) throws IOException {
        logger.log(Level.FINE, "Coupling on " + connectAddress + connectPort);

        SocketChannel coupleChannel = SocketChannel.open();
        boolean isConnected = coupleChannel.connect(new InetSocketAddress(connectAddress, connectPort));
        coupleChannel.configureBlocking(false);

        this.pair = coupleChannel.register(parentKey.selector(), SelectionKey.OP_CONNECT);

        Attachment coupledAttachment = new Attachment();
        coupledAttachment.setPair(parentKey);
        this.pair.attach(coupledAttachment);
        this.in.clear();

        if (isConnected) {
            logger.log(Level.INFO, "Finished immediately");
            coupledAttachment.finishCouple();
        }
    }

    public void finishCouple() {
        logger.log(Level.INFO, "Connect user to server");
        SocksMessage.putMessage(this.in,
                Constants.SOCKS_VERSION,
                Constants.STATUS_GRANTED,
                Constants.NOB,
                Constants.IP_V4,
                Constants.NOB, Constants.NOB, Constants.NOB, Constants.NOB,
                Constants.NOB, Constants.NOB);
        Attachment coupledAttachment = ((Attachment) this.pair.attachment());
        this.out = coupledAttachment.getIn();
        coupledAttachment.setOut(this.in);
        pair.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void addCoupledWrite() {
        pair.interestOpsOr(SelectionKey.OP_WRITE);
        ((Attachment) this.pair.attachment()).setType(Type.NONE);
        type = Type.NONE;
    }

    public void addCoupledRead() {
        pair.interestOpsOr(SelectionKey.OP_READ);
        ((Attachment) this.pair.attachment()).setType(Type.NONE);
        type = Type.NONE;
    }

    public boolean isCoupled() {
        return (pair != null);
    }

    public void useInAsOut() {
        this.out = this.in;
    }
}
