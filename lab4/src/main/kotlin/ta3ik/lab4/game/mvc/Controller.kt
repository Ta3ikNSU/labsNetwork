package ta3ik.lab4.game.mvc

import java.awt.Color
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.KeyListener

class Controller : KeyListener {
    private var model: Model = Model()
    private var render: Render = Render(model)
    private val snake: Snake =
        Snake(
            Color((Math.random()).toFloat(), (Math.random()).toFloat(), (Math.random()).toFloat()),
            4,
            Point(5, 5)
        )

    init {
        model.addObserver(render)
        model.field.snakes.add(snake)
        model.field.field[snake.parts[0].x][snake.parts[0].y] = Cell.SNAKE_HEAD
        render.getFrame().addKeyListener(this)
    }

    override fun keyTyped(e: KeyEvent?) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            VK_W -> if(snake.dir != Direction.DOWN || snake.parts.size == 1) snake.dir = Direction.UP
            VK_A -> if(snake.dir != Direction.RIGHT || snake.parts.size == 1) snake.dir = Direction.LEFT
            VK_S -> if(snake.dir != Direction.UP || snake.parts.size == 1) snake.dir = Direction.DOWN
            VK_D -> if(snake.dir != Direction.LEFT || snake.parts.size == 1) snake.dir = Direction.RIGHT
        }
        model.move()
    }

    override fun keyReleased(e: KeyEvent?) {
    }
}