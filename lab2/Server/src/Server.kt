
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level

object Server {
    private const val ONE_THREAD = 1
    private const val SPEED_CHECK_DELAY = 3
    private val threadPool = Executors.newCachedThreadPool()
    private fun receiveFile(socket: Socket) {
        val scheduledExecutorService = Executors.newScheduledThreadPool(ONE_THREAD)
        try {
            socket.getInputStream().use { socketInput ->
                socket.getOutputStream().use { socketOutput ->
                    val receiveProtocol = ReceiveProtocol(socket)
                    val file: File = receiveProtocol.receiveFileName()
                    val speedCounter = SpeedCounter(file)
                    val fileName = file.name
                    receiveProtocol.receiveFileSize()
                    scheduledExecutorService.scheduleAtFixedRate(
                        speedCounter,
                        SPEED_CHECK_DELAY.toLong(),
                        SPEED_CHECK_DELAY.toLong(),
                        TimeUnit.SECONDS
                    )
                    receiveProtocol.receiveFile()
                    receiveProtocol.sendTransferStatus()
                    scheduledExecutorService.awaitTermination(SPEED_CHECK_DELAY.toLong(), TimeUnit.SECONDS)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            scheduledExecutorService.shutdown()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 1) {
            return
        }
        try {
            val port = args[0].toInt()
            ServerSocket(port).use { serverSocket ->
                while (!serverSocket.isClosed) {
                    val clientSocket = serverSocket.accept()
                    threadPool.execute { receiveFile(clientSocket) }
                }
            }
        } catch (e: IOException) {
        } catch (e: IllegalArgumentException) {
        } finally {
            threadPool.shutdown()
        }
    }
}