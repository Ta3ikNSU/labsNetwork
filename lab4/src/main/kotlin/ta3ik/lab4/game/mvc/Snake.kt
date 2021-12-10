package ta3ik.lab4.game.mvc

import java.awt.Color
import java.awt.Image
import java.awt.Point
import java.io.File
import javax.imageio.ImageIO

class Snake(
    val color: Color,
    private val index : Int,
    private val head : Point,
    var dir : Direction = Direction.UP
) {
    val parts : MutableList<Point> = mutableListOf(head);
    val image : Image = ImageIO.read(File("C:\\Users\\Ta3ik\\IdeaProjects\\lab4\\src\\main\\resources\\ta3ik\\lab4\\$index.png"))
}