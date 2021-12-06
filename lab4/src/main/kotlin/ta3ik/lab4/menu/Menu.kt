package ta3ik.lab4.menu

import org.springframework.boot.autoconfigure.SpringBootApplication
import ta3ik.lab4.game.mvc.Controller
import ta3ik.lab4.game.mvc.GameField
import ta3ik.lab4.observation.Observer
import java.awt.EventQueue
import java.awt.Frame
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*

class Menu(title: String) : JFrame(), MouseListener {
    init {
        createAndRenderMenu(title)
    }

    private fun createAndRenderMenu(title: String) {
        setTitle(title)
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(300, 200)
        setLocationRelativeTo(null)
        isVisible = true
    }

    override fun mouseClicked(e: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mousePressed(e: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mouseReleased(e: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mouseEntered(e: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mouseExited(e: MouseEvent?) {
        TODO("Not yet implemented")
    }
}

fun main(){
    EventQueue.invokeLater {
        Controller();
    }
}


//fun main(){
//    EventQueue.invokeLater {
//        Menu("Snake")
//    }
//}

