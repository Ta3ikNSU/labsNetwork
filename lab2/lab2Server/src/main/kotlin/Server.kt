import java.net.*
import java.io.*
import java.util.concurrent.Executors

class Server {
    lateinit var serverSocket : ServerSocket;
    lateinit var socket : Socket;
    val executors = Executors.newFixedThreadPool(4);
    fun init(port : Int){
        serverSocket = ServerSocket(port);
    }

    fun startServer(){
        while(true){
            socket
        }
    }
}