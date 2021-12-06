package ta3ik.lab4.game.mvc

import ta3ik.lab4.observation.Observable
import ta3ik.lab4.observation.Observer
import java.awt.Point
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


class Model : Observable {
    var observers: MutableList<Observer> = mutableListOf()
    var field: GameField = GameField()
    val scheduler = Timer()
    var sync = 0;

    init {
//        scheduler.schedule(
//            100, 1000
//        ) {
//            if(sync == 0) move()
//        }
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

    fun checker(x: Int, y: Int, snake: Snake) {
        val head = snake.parts[0]
        when (field.field[head.x][head.y - 1]) {
            Cell.SNAKE_HEAD -> {
                for (j in field.snakes) {
                    if (j.parts[0].x == head.x && j.parts[0].y == head.y) {
                        field.snakes.remove(snake)
                        field.snakes.remove(j)
                        return
                    }
                }
            }
            Cell.EMPTY -> {
                snake.parts.removeAt(snake.parts.size - 1)
                snake.parts.add(Point(head.x + x, head.y + y))
                return
            }
            Cell.SNAKE, Cell.WALL -> {
                field.snakes.remove(snake);
                return
            }
            Cell.FOOD -> {
                snake.parts.add(Point(head.x + x, head.y + y))
                return
            }
        }
    }

    fun move() {
        while(sync == 1){
            sleep(1)
        }
        sync = 1
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
        sync = 0
    }


}