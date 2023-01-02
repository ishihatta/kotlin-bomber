package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Explosion(gameScene: GameScene,
                x: Int,
                y: Int,
                private val position: Position,
                private val spread: Int)
    : LightSprite(gameScene, x, y) {

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
        if (position == Position.CENTER && remainTime == TIME_TO_LIVE) {
            expandExplosion(-width, 0)
            expandExplosion(width, 0)
            expandExplosion(0, -height)
            expandExplosion(0, height)
        }
        remainTime--
        return remainTime <= 0
    }

    private fun expandExplosion(xx: Int, yy: Int) {
        for (n in 1..spread) {
            val px = x + xx * n
            val py = y + yy * n

            // 壁があるか？
            gameScene.walls.find { it.x == px && it.y == py }?.also { wall ->
                // 壁の破壊
                if (wall.isBreakable) {
                    wall.startMelting()
                }
                return
            }
            // 爆弾があったら誘爆する
            val bombIndex = gameScene.bombs.indexOfFirst { it.x == px && it.y == py }
            if (bombIndex >= 0) {
                val bomb = gameScene.bombs[bombIndex]
                gameScene.bombs.removeIndex(bombIndex)
                gameScene.newExplosions.add(Explosion(gameScene, px, py, Position.CENTER, bomb.power))
                gameScene.explosionSound.play()
                return
            }
            // パワーアップアイテムがあったら破壊する
            val powerUpItemIndex = gameScene.powerUpItems.indexOfFirst { it.x == px && it.y == py }
            if (powerUpItemIndex >= 0) {
                gameScene.powerUpItems.removeIndex(powerUpItemIndex)
                return
            }
            // 新しい爆発を生成する
            val position: Position = if (xx == 0) {
                if (n == spread) {
                    if (yy > 0f) Position.TOP else Position.BOTTOM
                } else {
                    Position.VERTICAL
                }
            } else {
                if (n == spread) {
                    if (xx > 0f) Position.RIGHT else Position.LEFT
                } else {
                    Position.HORIZONTAL
                }
            }
            gameScene.newExplosions.add(Explosion(gameScene, px, py, position, spread))
        }
    }
}