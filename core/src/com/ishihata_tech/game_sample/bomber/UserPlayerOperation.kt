package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

class UserPlayerOperation(playerNumber: Int) : PlayerOperation {
    companion object {
        private val KEY_ASSIGN = arrayOf(
                KeyAssign(Input.Keys.A, Input.Keys.W, Input.Keys.D, Input.Keys.S, Input.Keys.NUM_1),
                KeyAssign(Input.Keys.LEFT, Input.Keys.UP, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.SLASH)
        )
    }
    private class KeyAssign(val left: Int, val up: Int, val right: Int, val down: Int, val fire: Int)
    private val keyAssign = KEY_ASSIGN[playerNumber]

    override val playerInput: PlayerOperation.PlayerInput
        get() {
            val move = if (Gdx.input.isKeyPressed(keyAssign.left)) {
                PlayerOperation.Move.LEFT
            } else if (Gdx.input.isKeyPressed(keyAssign.right)) {
                PlayerOperation.Move.RIGHT
            } else if (Gdx.input.isKeyPressed(keyAssign.up)) {
                PlayerOperation.Move.UP
            } else if (Gdx.input.isKeyPressed(keyAssign.down)) {
                PlayerOperation.Move.DOWN
            } else {
                PlayerOperation.Move.NONE
            }
            val fire = Gdx.input.isKeyPressed(keyAssign.fire)
            return PlayerOperation.PlayerInput(move, fire)
        }
}