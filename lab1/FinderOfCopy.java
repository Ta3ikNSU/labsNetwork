import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class FinderOfCopy {
    public final int PORT = 10000;
    private DatagramSocket datagramSocket = null;
    private MulticastSocket multicastSocket = null;
    private byte[] buf = new byte[256];
    private String myIP;
    private InetAddress inetAddress;
    private Set <Pair<String, String>> machineSet= new HashSet<>();


    public FinderOfCopy(String ip) throws Exception {
        find(ip);
    }

    public void find(String ip) throws Exception {
        myIP = InetAddress.getLocalHost().getHostAddress();

        inetAddress = InetAddress.getByName(ip);
        datagramSocket = new DatagramSocket();
        multicastSocket = new MulticastSocket(PORT);
        multicastSocket.joinGroup(inetAddress);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String sendBuf = "DISCONNECT " + myIP + " " + ProcessHandle.current().pid();
            DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
            try {
                datagramSocket.send(packet);
            } catch (IOException ignored) {
            }
        }));

        start(inetAddress);
    }

    private void start(InetAddress inetAddress) throws IOException {
        connect();
        datagramSocket.setBroadcast(true);
        while (true) {
            receiveMsg();
        }
    }

    private void connect() throws IOException {
        String sendBuf = "CONNECT " + myIP + " " + ProcessHandle.current().pid();
        DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
        datagramSocket.send(packet);
    }


    private void receiveMsg() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(packet);
        msgHandler(packet);
    }


    public void msgHandler(DatagramPacket packet) throws IOException {
        String[] dataArgs = new String(packet.getData(), 0, packet.getLength()).split(" +");
        switch (dataArgs[0]) {
            case "CONNECT": {
                if (!dataArgs[2].equals(String.valueOf(ProcessHandle.current().pid())) || !dataArgs[1].equals(String.valueOf(myIP))) {
                    System.out.println(" + " + dataArgs[1] + " " + dataArgs[2]);
                    machineSet.add(new Pair<>(dataArgs[1], dataArgs[2]));
                    answer();
                }

                break;
            }
            case "OLD_MACHINE": {
                if (!dataArgs[2].equals(String.valueOf(ProcessHandle.current().pid())) || !dataArgs[1].equals(String.valueOf(myIP))) {
                    if(!machineSet.contains(new Pair<>(dataArgs[1], dataArgs[2]))){
                        machineSet.add(new Pair<>(dataArgs[1], dataArgs[2]));
                        System.out.println(" * " + dataArgs[1] + " " + dataArgs[2]);
                    }
                }

                break;
            }
            case "DISCONNECT": {
                if (!dataArgs[2].equals(String.valueOf(ProcessHandle.current().pid())) || !dataArgs[1].equals(String.valueOf(myIP))) {

                    if(machineSet.contains(new Pair<>(dataArgs[1], dataArgs[2]))){
                        machineSet.remove(new Pair<>(dataArgs[1], dataArgs[2]));
                        System.out.println(" - " + dataArgs[1] + " " + dataArgs[2]);
                    }
                }

                break;
            }
        }
    }

    public void answer() throws IOException {
        String sendBuf = "OLD_MACHINE " + myIP + " " + ProcessHandle.current().pid();
        DatagramPacket packet = new DatagramPacket(sendBuf.getBytes(StandardCharsets.UTF_8), sendBuf.length(), inetAddress, PORT);
        datagramSocket.send(packet);
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
