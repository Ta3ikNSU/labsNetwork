package ta3ik.lab4.game.mvc

import ta3ik.lab4.observation.Observer
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel


class Render(model: Model?) : Observer, JFrame("SnakeGame") {
    private var model: Model
    private val sizeSquare = 50
    private val sizeWindowsPanel = 30

    init {
        this.model = model!!
        this.preferredSize =
            Dimension(model.field.sizeX * sizeSquare, model.field.sizeY * sizeSquare + sizeWindowsPanel)
        this.isResizable = false
        this.pack()
        val panel = JPanel()
        this.contentPane.add(panel)
        this.isVisible = true
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.color = Color.red
        // забор
        g.fillRect(0, sizeWindowsPanel, sizeSquare, this.height)
        g.fillRect(0, sizeWindowsPanel, this.width, sizeSquare)
        g.fillRect(this.width - sizeSquare, sizeWindowsPanel, this.width, this.height)
        g.fillRect(0, this.height - sizeSquare, this.width, this.height)


        // еда
        g.color = Color.YELLOW
        for (i in model.field.foods) {
            g.fillOval(i.x * sizeSquare, i.y * sizeSquare + sizeWindowsPanel, sizeSquare, sizeSquare)
        }


        var index = 0
        for (i in model.field.snakes) {
            // тело змеи
            g.color = i.color
            for (cord in i.parts) {
                g.fillRect(cord.x * sizeSquare, cord.y * sizeSquare + sizeWindowsPanel, sizeSquare, sizeSquare)
            }

//            // глаза змеи
//            g.color = Color.BLACK
//            g.fillOval(i.parts[0].x * sizeSquare + 5, i.parts[0].y * sizeSquare + sizeWindowsPanel + 8, 5, 5)
//            g.fillOval(i.parts[0].x * sizeSquare + 20, i.parts[0].y * sizeSquare + sizeWindowsPanel + 8, 5, 5)
//
//            // рот змеи
//            g.fillRect(i.parts[0].x * sizeSquare + 5, i.parts[0].y * sizeSquare + sizeWindowsPanel + 22, 21, 4)

            // лицо змеи
            g.drawImage(
                i.image,
                i.parts[0].x * sizeSquare, i.parts[0].y * sizeSquare + sizeWindowsPanel,
                sizeSquare, sizeSquare, null
            )
            index = (index + 1) % 7
        }
        // разметка поля
        g.color = Color.BLACK
        for (i in 1 until model.field.sizeX) {
            g.drawLine(i * sizeSquare, 0 + sizeWindowsPanel, i * sizeSquare, this.width + sizeWindowsPanel)
            g.drawLine(0, i * sizeSquare + sizeWindowsPanel, this.height, i * sizeSquare + sizeWindowsPanel)
        }
    }

    override fun printField(field: GameField?) {
        repaint()
    }

    fun getFrame(): JFrame {
        return this
    }

}