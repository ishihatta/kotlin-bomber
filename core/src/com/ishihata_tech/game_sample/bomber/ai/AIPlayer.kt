package com.ishihata_tech.game_sample.bomber.ai

import com.ishihata_tech.game_sample.bomber.ai.AIConstants.RISK_OF_BOMB
import com.ishihata_tech.game_sample.bomber.ai.AIConstants.SCORE_OF_BREAK_WALL
import com.ishihata_tech.game_sample.bomber.ai.AIConstants.SCORE_OF_DISTANCE
import com.ishihata_tech.game_sample.bomber.ai.AIConstants.SCORE_OF_POWER_UP_ITEM
import com.ishihata_tech.game_sample.bomber.game_screen.Bomb
import com.ishihata_tech.game_sample.bomber.game_screen.Constants
import com.ishihata_tech.game_sample.bomber.game_screen.GameScreen
import com.ishihata_tech.game_sample.bomber.game_screen.PlayerOperation
import kotlin.math.absoluteValue

class AIPlayer(private val gameScreen: GameScreen, private val playerNumber: Int): PlayerOperation {
    // 前フレームでの自分の位置
    private var previousMyPositionX = 0
    private var previousMyPositionY = 0
    // 前フレームで移動しようとした場合は true
    private var previousWantToMode = false
    // 対戦相手のストレス度に対するスコアの重みにプラスする値
    private var opponentStressWeightPlus = 0
    // 対戦相手の位置を通過できないと認識するタイマー（1以上だと対戦相手の現在位置を「通過不能」と判断する）
    private var opponentPositionIsNotPassableTimer = 0

    override val playerInput: PlayerOperation.PlayerInput
        get() = operatePlayer()

    private fun operatePlayer(): PlayerOperation.PlayerInput {
        val player = gameScreen.players[playerNumber]

        // 前フレームで移動したかったけど移動できなかった場合、対戦相手のストレス度に対するスコアの重みを増やす
        if (previousWantToMode && previousMyPositionX == player.x && previousMyPositionY == player.y) {
            opponentStressWeightPlus++
            println("player=$playerNumber opponentStressWeightPlus=$opponentStressWeightPlus")
        } else {
            if (opponentStressWeightPlus > 0) opponentStressWeightPlus--
        }
        previousMyPositionX = player.x
        previousMyPositionY = player.y

        // 相手プレイヤーの座標
        val opponentPlayer = gameScreen.players[1 - playerNumber]
        val opponentX = (opponentPlayer.x + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE
        val opponentY = (opponentPlayer.y + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE

        // マップを作成する
        val field = Field(gameScreen)

        // 現在の対戦相手のストレス
        val opponentStress = calcOpponentStress(field, opponentX, opponentY)

        // 広さ優先で自キャラの位置から探索
        val searchQueue = ArrayDeque<FieldElement>()
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
            var score = -fieldElement.risk - distance * SCORE_OF_DISTANCE
            var fire = false
            // パワーアップアイテムがある場所には行きたい！
            if (fieldElement.fieldObject == FieldElement.FieldObject.POWER_UP_ITEM) {
                score += SCORE_OF_POWER_UP_ITEM
            }
            // この場所に爆弾を置いて得られるメリットを計算する
            if (fieldElement.fieldObject != FieldElement.FieldObject.BOMB && !(x == opponentX && y == opponentY)) {
                // 爆弾が置かれた状態を再現する
                val fieldIfBombSet = Field(field)
                // この爆弾で破壊できる壁の数
                val breakCount = fieldIfBombSet.addBomb(Bomb(gameScreen, x * Constants.CHARACTER_SIZE, y * Constants.CHARACTER_SIZE, player.power))
                // 逃げ場があるか確認する
                if (fieldIfBombSet.checkIfEscapable(x, y, opponentX, opponentY)) {
                    // 破壊できる壁があればスコア加算
                    if (breakCount > 0) {
                        score += breakCount * SCORE_OF_BREAK_WALL
                        fire = true
                    }
                    // 対戦相手にいやがらせできればスコア加算
                    if (!opponentPlayer.isDead) {
                        val opponentStressPlus = calcOpponentStress(fieldIfBombSet, opponentX, opponentY) - opponentStress
                        if (opponentStressPlus > 0) {
                            // スコアに加算する重みの計算
                            // 「動きたいのに動けない」状況が続くと現在位置に爆弾を置く場合の重みが大きくなる
                            val weight = AIConstants.OPPONENT_STRESS_WEIGHT +
                                    if (fieldElement == myElement) opponentStressWeightPlus else 0
                            score += opponentStressPlus * weight
                            fire = true
                        }
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
                // 「対戦相手位置を通過不能と認識する」場合は、対戦相手位置は通過不能とする
                if (opponentPositionIsNotPassableTimer > 0) {
                    if (opponentX == nextElement.x && opponentY == nextElement.y) return@forEach
                }
                // この場所のリスクが高すぎる場合はここには行かない
                if (nextElement.risk > RISK_OF_BOMB * 9 / 10 && nextElement.risk > fieldElement.risk) return@forEach
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
        if (fireFlag) {
            // 爆弾を設置する場合は「相手位置は通過不能と認識する」タイマーをセットする
            opponentPositionIsNotPassableTimer = AIConstants.OPPONENT_NOT_PASSABLE_TIMEOUT
        } else if (opponentPositionIsNotPassableTimer > 0) {
            opponentPositionIsNotPassableTimer--
        }
        // 目的地への経路のうち、現在地の次の位置を取得する
        var f = maxScoreFieldElement
        while (f.previousElement != myElement) {
            f = f.previousElement ?: break
        }
        val fx = f.x * Constants.CHARACTER_SIZE
        val fy = f.y * Constants.CHARACTER_SIZE
        previousWantToMode = fx != player.x || fy != player.y
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

    /**
     * 対戦相手の移動範囲のうち何パーセントを「いずれ爆発する」状態にしているか
     */
    private fun calcOpponentStress(field: Field, opponentX: Int, opponentY: Int): Int {
        // 到達可能で、かつ距離が5以下の場所を探索する
        val checked = BooleanArray(GameScreen.MAP_WIDTH * GameScreen.MAP_HEIGHT)
        val searchQueue = ArrayDeque<FieldElement>()
        searchQueue.addLast(field.getElement(opponentX, opponentY))
        checked[opponentX + opponentY * GameScreen.MAP_WIDTH] = true
        // 移動可能な範囲
        var movableSpace = 0
        // 危険な範囲
        var dangerousSpace = 0
        while (searchQueue.isNotEmpty()) {
            val element = searchQueue.removeFirst()
            val ex = element.x
            val ey = element.y
            movableSpace++
            if (element.risk > 0) {
                dangerousSpace++
            }
            arrayOf(
                    field.getElement(ex - 1, ey),
                    field.getElement(ex + 1, ey),
                    field.getElement(ex, ey - 1),
                    field.getElement(ex, ey + 1)
            ).forEach {
                val idx = it.x + it.y * GameScreen.MAP_WIDTH
                val distance = (opponentX - it.x).absoluteValue + (opponentY - it.y).absoluteValue
                if (distance <= 5 && !checked[idx] && it.isPassable) {
                    searchQueue.addLast(it)
                    checked[idx] = true
                }
            }
        }
        return dangerousSpace * 100 / movableSpace
    }
}