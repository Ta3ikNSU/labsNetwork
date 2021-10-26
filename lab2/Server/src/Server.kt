import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Server {
    private val SPEED_CHECK_DELAY = 3

    fun receiveFile(socket: Socket, scheduledExecutorService: ScheduledExecutorService) {
        try {
            socket.getInputStream().use { socketInput ->
                socket.getOutputStream().use { socketOutput ->
                    val receiveProtocol = ReceiveProtocol(socket)
                    val file: File = receiveProtocol.receiveFileName()
                    val speedCounter = SpeedCounter(file)
                    receiveProtocol.receiveFileSize()
                    val task = scheduledExecutorService.scheduleAtFixedRate(
                        speedCounter,
                        SPEED_CHECK_DELAY.toLong(),
                        SPEED_CHECK_DELAY.toLong(),
                        TimeUnit.SECONDS
                    )
                    try {
                        receiveProtocol.receiveFile()
                        receiveProtocol.sendTransferStatus()
//                        scheduledExecutorService.awaitTermination(SPEED_CHECK_DELAY.toLong(), TimeUnit.SECONDS)
                    } finally {
                        speedCounter.run()
                        task.cancel(false)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}

fun main(args: Array<String>) {
    if (args.size < 1) {
        return
    }

    val threadPool = Executors.newCachedThreadPool()
    val scheduledExecutorService = Executors.newScheduledThreadPool(1)

    try {
        val port = args[0].toInt()
        ServerSocket(port).use { serverSocket ->
            while (!serverSocket.isClosed) {
                val clientSocket = serverSocket.accept()
                threadPool.execute {
                    Server().receiveFile(clientSocket, scheduledExecutorService)
                }
            }
        }
    } catch (e: IOException) {
    } catch (e: IllegalArgumentException) {
    } finally {
        threadPool.shutdown()
        scheduledExecutorService.shutdown()
    }
}