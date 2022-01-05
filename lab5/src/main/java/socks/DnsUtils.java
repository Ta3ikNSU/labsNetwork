package socks;

import org.jetbrains.annotations.NotNull;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static socks.SocksProxyServer.*;

public class DnsUtils {

    private static final Logger logger = Logger.getLogger(DnsUtils.class.getName());

    public static void read(@NotNull SelectionKey key) throws IOException {

        Attachment attach = (Attachment) key.attachment();

        Message message = new Message(attach.getIn().array());
        Optional<Record> maybeRecord = message.getSection(Section.ANSWER).stream().findAny();
        if (maybeRecord.isPresent()) {
            InetAddress address = InetAddress.getByName(maybeRecord.get().rdataToString());

            ((Attachment) attach.getPair().attachment()).couple(address, attach.getPort(), attach.getPair());
            ((Attachment) attach.getPair().attachment()).setType(Type.NONE);
            int[] ip = new int[4];
            for (int i = 0; i < 4; i++) {
                ip[i] = address.getAddress()[i] & 0xFF;
            }
            logger.log(Level.INFO, "Server ip success resolved:  " + Arrays.toString(ip));
            key.interestOps(0);
            partiallyClose(key);
        } else {
            close(key);
            throw new RuntimeException("Host cannot be resolved");
        }
    }

    public static void write(SelectionKey key) {
        clearBuffers(key);
        key.interestOpsOr(SelectionKey.OP_READ);
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
        ((Attachment) key.attachment()).setType(Type.DNS_READ);
    }
}
