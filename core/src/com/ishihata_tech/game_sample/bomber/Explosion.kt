package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Explosion(gameScene: GameScene,
                x: Int,
                y: Int,
                private val position: Position,
) : LightSprite(gameScene, x, y) {

    companion object {
        const val TIME_TO_LIVE = 30
    }

    enum class Position(val srcY: Int) {
        CENTER(0),
        VERTICAL(Constants.CHARACTER_SIZE),
        HORIZONTAL(Constants.CHARACTER_SIZE * 2),
        LEFT(Constants.CHARACTER_SIZE * 3),
        TOP(Constants.CHARACTER_SIZE * 4),
        RIGHT(Constants.CHARACTER_SIZE * 5),
        BOTTOM(Constants.CHARACTER_SIZE * 6),
    }

    private var remainTime = TIME_TO_LIVE

    override fun draw(batch: SpriteBatch) {
        val srcX = if (remainTime < 3 || remainTime > TIME_TO_LIVE - 3) Constants.CHARACTER_SIZE else 0
        batch.draw(gameScene.explosionImage, x.toFloat(), y.toFloat(), srcX, position.srcY, width, height)
    }

    override fun onNextFrame(): Boolean {
        remainTime--
        return remainTime <= 0
    }
}