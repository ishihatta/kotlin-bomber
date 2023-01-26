package com.ishihata_tech.game_sample.bomber.ai

import com.ishihata_tech.game_sample.bomber.Bomb
import com.ishihata_tech.game_sample.bomber.Constants
import com.ishihata_tech.game_sample.bomber.GameScene
import com.ishihata_tech.game_sample.bomber.PlayerOperation

class AIPlayer(private val gameScene: GameScene, private val playerNumber: Int): PlayerOperation {
    override val playerInput: PlayerOperation.PlayerInput
        get() = operatePlayer()

    private fun operatePlayer(): PlayerOperation.PlayerInput {
        // マップを作成する
        val field = Field(gameScene)

        // 広さ優先で自キャラの位置から探索
        val searchQueue = ArrayDeque<FieldElement>()
        val player = gameScene.players[playerNumber]
        val myX = (player.x + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE
        val myY = (player.y + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE
        val myElement = field.getElement(myX, myY)
        myElement.distance = 0
        myElement.cost = 0
        searchQueue.add(myElement)
        var maxScore = -myElement.risk
        var maxScoreFieldElement = myElement
        var maxScoreFire = false
        while (searchQueue.isNotEmpty()) {
            val fieldElement = searchQueue.removeFirst()
            val x = fieldElement.x
            val y = fieldElement.y
            val distance = field.getElement(x, y).distance

            // この場所のスコアと爆弾設置の可否を計算する
            var score = -fieldElement.risk - distance
            var fire = false
            // パワーアップアイテムがある場所には行きたい！
            if (fieldElement.fieldObject == FieldElement.FieldObject.POWER_UP_ITEM) {
                score += 100
            }
            // この場所に爆弾を置いて得られるメリットを計算する
            if (fieldElement.fieldObject != FieldElement.FieldObject.BOMB) {
                // 爆弾が置かれた状態を再現する
                val fieldIfBombSet = Field(field)
                // この爆弾で破壊できる壁の数
                val breakCount = fieldIfBombSet.addBomb(Bomb(gameScene, x * Constants.CHARACTER_SIZE, y * Constants.CHARACTER_SIZE, player.power))
                // 逃げ場があるか確認する
                if (fieldIfBombSet.checkIfEscapable(x, y)) {
                    if (breakCount > 0) {
                        score += breakCount * 6
                        fire = true
                    }
                }
            }
            if (score > maxScore) {
                maxScore = score
                maxScoreFieldElement = fieldElement
                maxScoreFire = fire
            }

            val nextDistance = distance + 1
            arrayOf(Pair(x - 1, y), Pair(x + 1, y), Pair(x, y - 1), Pair(x, y + 1)).forEach {
                val nextElement = field.getElement(it.first, it.second)
                // 通れない場所には行けない
                if (!nextElement.isPassable) return@forEach
                // この場所のリスクが高すぎる場合はここには行かない
                if (nextElement.risk > 90 && nextElement.risk > fieldElement.risk) return@forEach
                // この場所にたどり着くまでのコストを計算し、すでにそれより低いコストで移動できる経路が計算済みなら何もしない
                val cost = fieldElement.cost + nextElement.risk
                if (cost >= nextElement.cost) return@forEach

                nextElement.previousElement = fieldElement
                nextElement.distance = nextDistance
                nextElement.cost = cost
                searchQueue.add(nextElement)
            }
        }

        // 移動処理
        // すでに目的地に到着しており、かつ目的が爆弾設置なら爆弾を置く
        val fireFlag = maxScoreFieldElement == myElement && maxScoreFire
        // 目的地への経路のうち、現在地の次の位置を取得する
        var f = maxScoreFieldElement
        while (f.previousElement != myElement) {
            f = f.previousElement ?: break
        }
        val fx = f.x * Constants.CHARACTER_SIZE
        val fy = f.y * Constants.CHARACTER_SIZE
        if (fx > player.x) {
            // 右に移動
            return PlayerOperation.PlayerInput(PlayerOperation.Move.RIGHT, fireFlag)
        } else if (fx < player.x) {
            // 左に移動
            return PlayerOperation.PlayerInput(PlayerOperation.Move.LEFT, fireFlag)
        } else if (fy > player.y) {
            // 上に移動
            return PlayerOperation.PlayerInput(PlayerOperation.Move.UP, fireFlag)
        } else if (fy < player.y) {
            // 下に移動
            return PlayerOperation.PlayerInput(PlayerOperation.Move.DOWN, fireFlag)
        }
        return PlayerOperation.PlayerInput(PlayerOperation.Move.NONE, fireFlag)
    }
}