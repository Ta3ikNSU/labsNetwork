package ta3ik.lab4.observation

import ta3ik.lab4.game.mvc.GameField

interface Observer {

    fun printField(field: GameField?)
}