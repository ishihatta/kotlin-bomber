package com.ishihata_tech.game_sample.bomber

interface PlayerOperation {
    enum class Move {
        NONE, LEFT, UP, RIGHT, DOWN
    }
    class PlayerInput(val move: Move, val fire: Boolean)

    val playerInput: PlayerInput
}