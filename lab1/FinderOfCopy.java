import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class FinderOfCopy {
    public static final int PORT = 10000;

    private static DatagramSocket datagramSocket = null;
    private static MulticastSocket multicastSocket = null;
    private static byte[] buf = new byte[256];
    private static String myIP;
    private static InetAddress inetAddress;
    public static void find(String ip) throws Exception {
        myIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println(myIP);
        inetAddress = InetAddress.getByName(ip);
        datagramSocket = new DatagramSocket();
        multicastSocket = new MulticastSocket(PORT);
        multicastSocket.joinGroup(inetAddress);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("finalize");
            String sendBuf = "DISCONNECT " + myIP;
            DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
            try {
                datagramSocket.send(packet);
            } catch (IOException ignored) {
            }
        }));

        start(inetAddress);
    }

    private static void start(InetAddress inetAddress) throws IOException {
        connect();
        datagramSocket.setBroadcast(true);
        System.out.println("connect msg has been sended");
        while (true) {
            receiveMsg();
            System.out.println("msg was recieve");
        }
    }

    private static void connect() throws IOException {
        String sendBuf = "CONNECT " + myIP;
        DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
        datagramSocket.send(packet);
    }


    private static void receiveMsg() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(packet);
        msgHandler(packet);
    }


    public static void msgHandler(DatagramPacket packet) {
        String[] dataArgs = new String(packet.getData(), 0, packet.getLength()).split(" +");
        switch (dataArgs[0]) {
            case "CONNECT": {
                System.out.println(" + " + dataArgs[1]);
                answer();
                break;
            }
            case "OLD_MACHINE": {
                System.out.println(" OLD_MACHINE " + dataArgs[1]);
                answer();
                break;
            }
            case "DISCONNECT": {
                System.out.println(" - " + dataArgs[1]);
                break;
            }
        }
    }

    public static void answer() {
        String sendBuf = "OLD_MACHINE " + myIP;
        DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            System.out.println("finalize");
            String sendBuf = "DISCONNECT " + myIP;
            DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
        } finally {
            super.finalize();
        }
    }

    private enum InstanceMsg {
        CONNECT,
        OLD_MACHINE,
        DISCONNECT;

        @Override
        public String toString() {
            return switch (this) {
                case CONNECT -> "CONNECT";
                case OLD_MACHINE -> "OLD_MACHINE";
                case DISCONNECT -> "DISCONNECT";
            };
        }
    }
}
