package com.ishihata_tech.game_sample.bomber.game_screen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils

class Wall(
        gameScreen: GameScreen,
        x: Int,
        y: Int,
        val isBreakable: Boolean)
    : LightSprite(gameScreen, x, y) {

    companion object {
        private const val TIME_TO_MELT = 30
    }

    private val texture =
            if (isBreakable) gameScreen.breakableWallImage
            else gameScreen.wallImage

    /**
     * 破壊可能な壁が破壊されている途中の状態（1～TIME_TO_MELT）
     */
    private var meltState = 0

    /**
     * 破壊中の場合 true
     */
    val isMelting: Boolean
        get() = meltState > 0

    override fun draw(batch: SpriteBatch) {
        if (meltState > 0) {
            val prevColor = Color(batch.color)
            batch.setColor(1f, 0f, 0f, 1f - meltState.toFloat() / TIME_TO_MELT)
            batch.draw(texture, x.toFloat(), y.toFloat())
            batch.color = prevColor
        } else {
            batch.draw(texture, x.toFloat(), y.toFloat())
        }
    }

    override fun onNextFrame(): Boolean {
        if (meltState > 0) {
            meltState++
            if (meltState >= TIME_TO_MELT) {
                // 一定の確率でパワーアップアイテムが出る
                if (MathUtils.random(100) < 10) {
                    gameScreen.powerUpItems.add(PowerUpItem(gameScreen, x, y))
                }
                return true
            }
        }
        return false
    }

    /**
     * 破壊を開始する
     */
    fun startMelting() {
        if (meltState == 0) {
            meltState = 1
        }
    }
}