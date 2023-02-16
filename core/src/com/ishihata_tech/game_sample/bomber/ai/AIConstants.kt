package com.ishihata_tech.game_sample.bomber.ai

object AIConstants {
    // パワーアップアイテムのスコア
    const val SCORE_OF_POWER_UP_ITEM = 1000
    // 壁を壊すスコア
    const val SCORE_OF_BREAK_WALL = 30
    // 目的地までの移動距離のマイナススコア
    const val SCORE_OF_DISTANCE = 5
    // 爆発している場所のリスク値
    const val RISK_OF_EXPLOSION = 2000
    // 爆弾によっていずれ爆発する場所の最大リスク値
    const val RISK_OF_BOMB = 1000
    // 対戦相手のストレス度に対するスコアの重み
    const val OPPONENT_STRESS_WEIGHT = 10
    // 自分が爆弾設置後、対戦相手の現在位置を通過不能とみなす時間（フレーム数）
    const val OPPONENT_NOT_PASSABLE_TIMEOUT = 60
}