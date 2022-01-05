import socks.SocksProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.IllegalFormatFlagsException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proxy {
    private static final Integer MIN_PORT = 1024;
    private static final Integer MAX_PORT = 65535;
    private static final Logger logger = Logger.getLogger(Proxy.class.getName());

    private static @NotNull Integer parsePort(String portString) {
        int port = Integer.parseInt(portString);
        if (port < MIN_PORT || port > MAX_PORT) {
            logger.log(Level.OFF, "Incorrect port");
            throw new IllegalArgumentException("Incorrect port");
        }
        return port;
    }

    public static void main(String @NotNull [] args) {
        if (args.length < 1) {
            logger.log(Level.OFF, "SOCKSServer args : port");
            throw new IllegalFormatFlagsException("Enter port");
        }
        int port = parsePort(args[0]);
        SocksProxyServer server = new SocksProxyServer(port);
        server.run();
        logger.log(Level.OFF, "SOCKS5 server starting on " + port);
    }
}
