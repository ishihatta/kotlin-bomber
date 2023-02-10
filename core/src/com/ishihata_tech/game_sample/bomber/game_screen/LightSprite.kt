package com.ishihata_tech.game_sample.bomber.game_screen

import com.badlogic.gdx.graphics.g2d.SpriteBatch

open class LightSprite(
        val gameScreen: GameScreen,
        var x: Int = 0,
        var y: Int = 0,
        var width: Int = Constants.CHARACTER_SIZE,
        var height: Int = Constants.CHARACTER_SIZE
) {
    /**
     * このスプライトを描画する
     */
    open fun draw(batch: SpriteBatch) {}

    /**
     * 各フレームでの状態遷移処理（移動など）
     *
     * @return このスプライトを削除する場合 true
     */
    open fun onNextFrame(): Boolean { return false }
}