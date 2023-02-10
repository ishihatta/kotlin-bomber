package com.ishihata_tech.game_sample.bomber.game_screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class PowerUpItem(
        gameScreen: GameScreen,
        x: Int,
        y: Int)
    : LightSprite(gameScreen, x, y) {

    private var moveTime = 0f

    override fun draw(batch: SpriteBatch) {
        val step = (moveTime / 0.2f).toInt() % 3
        batch.draw(
                gameScreen.powerUpImage,
                x.toFloat(),
                y.toFloat(),
                step * width,
                0,
                width,
                height)
    }

    override fun onNextFrame(): Boolean {
        moveTime += Gdx.graphics.deltaTime
        return false
    }
}