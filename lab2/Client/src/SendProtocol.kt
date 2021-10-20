import java.io.*
import java.net.Socket

class SendProtocol(socket: Socket, file: File) {
    private val primitiveDataTypesSocketInput: DataInputStream
    private val primitiveDataTypesSocketOutput: DataOutputStream
    private val bufferSize: Int
    private val socket: Socket
    private val file: File

    init {
        primitiveDataTypesSocketOutput = DataOutputStream(socket.getOutputStream())
        primitiveDataTypesSocketInput = DataInputStream(socket.getInputStream())
        bufferSize = socket.sendBufferSize
        this.socket = socket
        this.file = file
    }

    fun sendFileName() {
        primitiveDataTypesSocketOutput.writeUTF(file.name)
    }

    fun sendFileSize() {
        primitiveDataTypesSocketOutput.writeLong(file.length())
    }

    fun sendFile() {
        val buffer = ByteArray(bufferSize)
        var bytesAmount: Int
        BufferedInputStream(FileInputStream(file)).use { bufferedFileInput ->
            while (bufferedFileInput.read(buffer).also {
                    bytesAmount = it
                } > 0) {
                primitiveDataTypesSocketOutput.write(buffer, 0, bytesAmount)
            }
        }
        socket.shutdownOutput()
    }

    @Throws(IOException::class)
    fun receiveTransferStatus(): String {
        return primitiveDataTypesSocketInput.readUTF()
    }
}