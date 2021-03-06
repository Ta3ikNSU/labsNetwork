import java.io.*
import java.net.Socket

class ReceiveProtocol(socket: Socket) {
    private val primitiveDataTypesSocketInput: DataInputStream
    private val primitiveDataTypesSocketOutput: DataOutputStream
    private val bufferSize: Int
    private lateinit var fileName: String
    private lateinit var file: File
    private var fileSize: Long = 0

    init {
        primitiveDataTypesSocketInput = DataInputStream(socket.getInputStream())
        primitiveDataTypesSocketOutput = DataOutputStream(socket.getOutputStream())
        bufferSize = socket.receiveBufferSize
    }

    @Throws(IOException::class)
    fun receiveFileName(): File {
        fileName = primitiveDataTypesSocketInput.readUTF()
        file = createUniqueFile()
        return file
    }

    @Throws(IOException::class)
    fun receiveFileSize() {
        fileSize = primitiveDataTypesSocketInput.readLong()
    }

    @Throws(IOException::class)
    fun receiveFile() {


        val bytes = ByteArray(bufferSize)
        var bytesAmount: Int
        BufferedOutputStream(FileOutputStream(file.path)).use { bufferedFileOutput ->
            while (primitiveDataTypesSocketInput.read(bytes).also {
                    bytesAmount = it
                } > 0) {
                bufferedFileOutput.write(bytes, 0, bytesAmount)
            }
        }
    }

    @Throws(IOException::class)
    fun sendTransferStatus() {
        val status =
            if (fileSize == file.length()) "File transfer successful! File name: " + file.name else "File transfer failed"
        primitiveDataTypesSocketOutput.writeUTF(status)
    }

    @Throws(IOException::class)
    private fun createUniqueFile(): File {
        val filePath = System.getProperty("user.dir") + File.separator + fileName
        println(filePath);
        val file = File(File(filePath).name)
        try {
            file.createNewFile();
        } catch (ex: Exception) {
            println(ex.printStackTrace());
        }
        return file
    }
}