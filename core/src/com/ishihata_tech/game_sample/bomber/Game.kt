package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.ApplicationAdapter

class Game : ApplicationAdapter() {
    private var gameScene: GameScene? = null

    override fun create() {
        gameScene = GameScene(GameScene.PlayerType.AI, GameScene.PlayerType.AI)
    }

    override fun render() {
        gameScene?.render()
    }

    override fun dispose() {
        gameScene?.dispose()
        gameScene = null
    }
}