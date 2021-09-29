import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.abs

class FinderOfCopy(
    private val date: Date = Date(),
    private val hashMap: HashMap<Node, Long> = HashMap<Node, Long>(),
    private val myIp: String? = InetAddress.getLocalHost().hostAddress,
    private val PORT: Int = 10000,
    private val pid: Long = ProcessHandle.current().pid(),
) {

    fun find(ip: String) {
        val datagramSocket = DatagramSocket()
        val multicastSocket = MulticastSocket(PORT)
        val inetAddress = InetAddress.getByName(ip)
        var byteArray = ByteArray(256)
        multicastSocket.soTimeout = 10000
        multicastSocket.joinGroup(inetAddress)
        datagramSocket.broadcast = true
        var timeLastSend: Long = 0
        println("My info: $myIp $pid");
        while (true) {
            //recv data
            var datagramPacketRecv = DatagramPacket(byteArray, byteArray.size)
            try {
                multicastSocket.receive(datagramPacketRecv)
                msgHandler(datagramPacketRecv)
            } catch (ex: Exception) {
            }
            //send data
            if (abs(timeLastSend - date.time) > 10000 || timeLastSend.equals(0)) {
                val buffer = "$myIp $pid"

                val datagramPacketSend =
                    DatagramPacket(buffer.toByteArray(StandardCharsets.UTF_8), buffer.length, inetAddress, PORT)
                datagramSocket.send(datagramPacketSend)
            } else {
                if (timeLastSend.equals(0)) timeLastSend = date.time
            }
            if (abs(timeLastSend - date.time) > 60000) {
                healthChecker()
            }
        }
    }

    private fun msgHandler(packet: DatagramPacket) {
        var args = String(packet.data, 0, packet.length, StandardCharsets.UTF_8).split(" ").toTypedArray()
        if (!args[1].equals(pid.toString()) || !args[0].equals(myIp)) { //self checker
            if (hashMap[Node(args[0], args[1])] == null) {
                hashMap[Node(args[0], args[1])] = date.time //add new copy
                val ipAdd = args[0]
                val pidAdd = args[1]
                println("+ $ipAdd $pidAdd")
            } else {
                hashMap[Node(args[0], args[1])] = date.time //update time
            }
        }
    }

    private fun healthChecker() {
        for (it in hashMap.toList().iterator()) {
            if (abs(it.second - date.time) > 100000) {
                hashMap.remove(it.first)
                val ipRemove = it.first
                val pidRemove = it.second
                println("- $ipRemove $pidRemove")
            }
        }
    }
}