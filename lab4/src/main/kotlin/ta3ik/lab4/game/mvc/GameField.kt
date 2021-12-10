package ta3ik.lab4.game.mvc

import java.awt.Point

class GameField(val sizeX: Int = 10, val sizeY: Int = 10) {

    val snakes: MutableList<Snake> = mutableListOf<Snake>()
    val foods: MutableList<Point> = mutableListOf()
    val field: Array<Array<Cell>> = Array(sizeX) { Array(sizeY) { Cell.EMPTY } }
}

enum class Cell {
    SNAKE,
    SNAKE_HEAD,
    FOOD,
    EMPTY,
    WALL
}