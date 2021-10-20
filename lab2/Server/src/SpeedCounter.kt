
import java.io.File

class SpeedCounter(private val file: File) : Runnable {
    private val fileName: String = file.name
    private var oldFileSize: Long = 0
    private var callCounter = 0

    override fun run() {
        callCounter++
        val currentFileSize = file.length()
        val sizeDifference = currentFileSize - oldFileSize
        if (0L == sizeDifference) {
            return
        }
        println(fileName + " instant download speed: " + sizeDifference / (SPEED_CHECK_DELAY.toFloat() * KILO) + "KBs")
        println(fileName + " average download speed: " + currentFileSize / (SPEED_CHECK_DELAY.toFloat() * callCounter * KILO) + "KBs")
        oldFileSize = currentFileSize
    }

    companion object {
        private const val SPEED_CHECK_DELAY = 3
        private const val KILO = 1024
    }
}