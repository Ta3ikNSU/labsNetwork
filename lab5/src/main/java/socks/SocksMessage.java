package socks;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocksMessage {

    private static final Logger logger = Logger.getLogger(SocksMessage.class.getName());

    public static void putMessage(@NotNull ByteBuffer in, byte... flags) {
        in.put(flags);
    }


    public static boolean inTooSmall(SelectionKey key, byte smallness) {
        if (smallness != Constants.HOST_SMALLNESS) {
            return (((Attachment) key.attachment()).getIn().position() < smallness);
        } else {
            int len = getHostLen(key);
            return (((Attachment) key.attachment()).getIn().position() < Constants.HOST_START + len + Constants.PORT_LEN);
        }
    }

    public static boolean wrongVersion(@NotNull SelectionKey key) {
        return (((Attachment) key.attachment()).getIn().array()[0] != Constants.SOCKS_VERSION);
    }

    public static boolean wrongCommand(@NotNull SelectionKey key) {
        return (((Attachment) key.attachment()).getIn().array()[1] != Constants.CONNECT);
    }

    public static boolean wrongAddressType(SelectionKey key) {
        return (!isIpv4(key)) && (!isHost(key));
    }

    public static boolean isIpv4(@NotNull SelectionKey key) {
        return (((Attachment) key.attachment()).getIn().array()[3] == Constants.IP_V4);
    }

    public static boolean isHost(@NotNull SelectionKey key) {
        return (((Attachment) key.attachment()).getIn().array()[3] == Constants.HOST);
    }

    public static boolean methodsReceived(@NotNull SelectionKey key) {
        ByteBuffer in = ((Attachment) key.attachment()).getIn();
        byte methodsCount = in.array()[1];
        int pos = in.position();
        return (pos - 2 == methodsCount);
    }

    public static boolean noAuthNotFound(@NotNull SelectionKey key) {
        byte[] data = ((Attachment) key.attachment()).getIn().array();
        int methodsCount = data[1];
        boolean res = true;
        for (int i = 0; i < methodsCount; i++) {
            if (data[i + 2] == Constants.NO_AUTH) {
                res = false;
                break;
            }
        }
        return res;
    }

    public static byte @NotNull [] getIpv4(@NotNull SelectionKey key) {
        byte[] data = ((Attachment) key.attachment()).getIn().array();
        byte[] ip = new byte[]{data[4], data[5], data[6], data[7]};
        logger.log(Level.INFO, "Get server ip success: " + data[4] + "." + data[5] + "." + data[6] + "." + data[7]);
        return ip;
    }

    public static int getPort(SelectionKey key, int pos) {
        int actualPos = pos;
        if (pos == Constants.HOST_PORT_POSITION) {
            actualPos = Constants.HOST_START + getHostLen(key);
        }
        byte[] data = ((Attachment) key.attachment()).getIn().array();
        return ((data[actualPos] & 0xFF) << 8) | (data[actualPos + 1] & 0xFF);
    }

    public static int getHostLen(@NotNull SelectionKey key) {
        return ((Attachment) key.attachment()).getIn().array()[4];
    }

    public static @NotNull String getHost(@NotNull SelectionKey key) {
        byte[] data = ((Attachment) key.attachment()).getIn().array();
        String hostName = new String(Arrays.copyOfRange(data, Constants.HOST_START, Constants.HOST_START + getHostLen(key)));
        logger.log(Level.INFO, "Proxy resolving hostname: " + hostName);
        return hostName;
    }
}
