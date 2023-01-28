package com.ishihata_tech.game_sample.bomber.ai

object AIConstants {
    // パワーアップアイテムのスコア
    const val SCORE_OF_POWER_UP_ITEM = 1000
    // 壁を壊すスコア
    const val SCORE_OF_BREAK_WALL = 20
    // 目的地までの移動距離のマイナススコア
    const val SCORE_OF_DISTANCE = 1
    // 爆発している場所のリスク値
    const val RISK_OF_EXPLOSION = 2000
    // 爆弾によっていずれ爆発する場所の最大リスク値
    const val RISK_OF_BOMB = 1000
}