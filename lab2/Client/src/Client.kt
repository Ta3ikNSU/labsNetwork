import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException

class Client {
    fun sendFile(clientSocket: Socket, file: File) {
        try {
            clientSocket.getInputStream().use { socketInput ->
                clientSocket.getOutputStream().use { socketOutput ->
                    val sendProtocol = SendProtocol(clientSocket, file)
                    sendProtocol.sendFileName()
                    sendProtocol.sendFileSize()
                    sendProtocol.sendFile()
                    val status: String = sendProtocol.receiveTransferStatus()
                    println(status)
                }
            }
        } catch (e: IOException) {

        }
    }
}

fun main(args: Array<String>) {
    if (args.size < 3) {
        return
    }
    val filePath = args[0]
    val file = File(filePath)
    if (!file.isFile) {
        return
    }
    try {
        val serverAddress = InetAddress.getByName(args[1])
        val serverPort = args[2].toInt()
        Socket(serverAddress, serverPort).use { socket ->
            Client().sendFile(socket, file)
        }
    } catch (_: UnknownHostException) {
    } catch (_: IllegalArgumentException) {
    }
}


