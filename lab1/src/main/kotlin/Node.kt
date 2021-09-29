data class Node(
    val ip : String,
    val processId : String
    ) : Comparable<Node>{
    override fun compareTo(other: Node): Int {
        if(this.ip.equals(other.ip) && this.processId.equals(other.processId)) return 1
        return -1
    }
}