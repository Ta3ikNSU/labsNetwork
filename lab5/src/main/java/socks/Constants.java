package socks;

public class Constants {
    public static final byte HOST_SMALLNESS = -1;
    public static final byte HOST_PORT_POSITION = -1;
    public static final byte STATUS_GRANTED = 0x00;
    public static final byte NO_AUTH = 0x00;
    public static final byte NOB = 0x00;
    public static final byte CONNECT = 0x01;
    public static final byte IP_V4 = 0x01;
    public static final byte AUTH_SMALLNESS = 2;
    public static final byte PORT_LEN = 2;
    public static final byte HOST = 0x03;
    public static final byte CONN_SMALLNESS = 4;
    public static final byte SOCKS_VERSION = 0x05;
    public static final byte HOST_START = 5;
    public static final byte IP_PORT_POSITION = 8;
}
