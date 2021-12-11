package ta3ik.lab4.game.mvc

import ta3ik.lab4.observation.Observable
import ta3ik.lab4.observation.Observer
import java.awt.Point
import java.lang.Thread.sleep


class Model : Observable {
    var observers: MutableList<Observer> = mutableListOf()
    @Volatile
    var field: GameField = GameField()
    private var removeSnake : MutableSet<Snake> = mutableSetOf()
    val repaintThread = Thread{
        while(true){
            sleep(200)
            move()
        }
    }


    override fun addObserver(observer: Observer?) {
        if (observer != null) {
            observers.add(observer)
        }
        notifyObservers()
    }

    override fun notifyObservers() {
        for (observer in observers) {
            observer.printField(field)
        }
    }

    private fun checker(x: Int, y: Int, snake: Snake) {
        val head = snake.parts[0]
        when (field.field[head.x + x][head.y + y]) {
            Cell.SNAKE_HEAD -> {
                for (j in field.snakes) {
                    if (j.parts[0].x == head.x && j.parts[0].y == head.y) {
                        removeSnake.add(j)
                    }
                }
                removeSnake.add(snake)
                return
            }
            Cell.EMPTY -> {
                field.field[head.x][head.y] =
                    if(snake.parts.size > 1) Cell.SNAKE
                    else Cell.EMPTY
                snake.parts.removeAt(snake.parts.size - 1)
                snake.parts.add(Point(head.x + x, head.y + y))
                field.field[head.x + x][head.y + y] = Cell.SNAKE_HEAD
                return
            }
            Cell.SNAKE, Cell.WALL -> {
                removeSnake.add(snake)
                return
            }
            Cell.FOOD -> {
                snake.parts.add(Point(head.x + x, head.y + y))
                field.field[head.x][head.y - 1] = Cell.SNAKE_HEAD
                return
            }
        }
    }
    @Synchronized
    fun move() {
        for (i in field.snakes) {
            when (i.dir) {
                Direction.DOWN -> {
                    checker(0, -1, i)
                }
                Direction.RIGHT -> {
                    checker(+1, 0, i)
                }
                Direction.UP -> {
                    checker(0, +1, i)
                }
                Direction.LEFT -> {
                    checker(-1, 0, i)
                }
            }
        }
        println("move")
        notifyObservers()
    }
}