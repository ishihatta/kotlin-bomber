package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class PowerUpItem(
        gameScene: GameScene,
        x: Int,
        y: Int)
    : LightSprite(gameScene, x, y) {

    private var moveTime = 0f

    override fun draw(batch: SpriteBatch) {
        val step = (moveTime / 0.2f).toInt() % 3
        batch.draw(
                gameScene.powerUpImage,
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