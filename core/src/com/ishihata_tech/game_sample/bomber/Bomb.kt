package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Bomb(
        gameScene: GameScene,
        x: Int,
        y: Int,
        val power: Int)
    : LightSprite(gameScene, x, y) {

    private var moveTime = 0f
    private var remainTime = 5 * 60

    override fun draw(batch: SpriteBatch) {
        val step = (moveTime / 0.2f).toInt() % 3
        batch.draw(
                gameScene.bombImage,
                x.toFloat(),
                y.toFloat(),
                step * width,
                0,
                width,
                height)
    }

    override fun onNextFrame(): Boolean {
        moveTime += Gdx.graphics.deltaTime

        // タイムアウトしたら爆発する
        remainTime--
        return if (remainTime <= 0) {
            gameScene.explosions.add(Explosion(
                    gameScene,
                    x,
                    y,
                    Explosion.Position.CENTER,
                    power))
            gameScene.explosionSound.play()
            true
        } else {
            false
        }
    }
}