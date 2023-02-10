package com.ishihata_tech.game_sample.bomber.game_screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Bomb(
        gameScreen: GameScreen,
        x: Int,
        y: Int,
        val power: Int)
    : LightSprite(gameScreen, x, y) {

    private var moveTime = 0f
    var remainTime = Constants.BOMB_TIME

    override fun draw(batch: SpriteBatch) {
        val step = (moveTime / 0.2f).toInt() % 3
        batch.draw(
                gameScreen.bombImage,
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
        return remainTime <= 0
    }
}