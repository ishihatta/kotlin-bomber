package com.ishihata_tech.game_sample.bomber.game_screen

import com.ishihata_tech.game_sample.bomber.ai.AIPlayer

/**
 * プレイヤータイプ（人間かAIか）
 */
enum class PlayerType {
    HUMAN {
        override fun generatePlayerOperation(gameScreen: GameScreen, playerNumber: Int): PlayerOperation {
            return UserPlayerOperation(playerNumber)
        }
    },
    AI {
        override fun generatePlayerOperation(gameScreen: GameScreen, playerNumber: Int): PlayerOperation {
            return AIPlayer(gameScreen, playerNumber)
        }
    };

    abstract fun generatePlayerOperation(gameScreen: GameScreen, playerNumber: Int): PlayerOperation
}