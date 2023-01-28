package com.ishihata_tech.game_sample.bomber.ai

import com.ishihata_tech.game_sample.bomber.Bomb
import com.ishihata_tech.game_sample.bomber.Constants
import com.ishihata_tech.game_sample.bomber.GameScene
import com.ishihata_tech.game_sample.bomber.ai.AIConstants.RISK_OF_BOMB
import com.ishihata_tech.game_sample.bomber.ai.AIConstants.RISK_OF_EXPLOSION

class Field {
    private val elements: Array<FieldElement>

    constructor() {
        elements = Array(GameScene.MAP_WIDTH * GameScene.MAP_HEIGHT) {
            val x = it % GameScene.MAP_WIDTH
            val y = it / GameScene.MAP_WIDTH
            FieldElement(x = x, y = y)
        }
    }

    constructor(src: Field) {
        elements = Array(GameScene.MAP_WIDTH * GameScene.MAP_HEIGHT) {
            src.elements[it].copy()
         }
    }

    constructor(gameScene: GameScene) : this() {
        // 壁をマップに追加
        gameScene.walls.forEach {
            val x = it.x / Constants.CHARACTER_SIZE
            val y = it.y / Constants.CHARACTER_SIZE
            getElement(x, y).apply {
                fieldObject = if (it.isBreakable)
                    FieldElement.FieldObject.BREAKABLE_WALL else FieldElement.FieldObject.UNBREAKABLE_WALL
                willBroken = it.isMelting
            }
        }
        // パワーアップアイテムをマップに追加
        gameScene.powerUpItems.forEach {
            val x = it.x / Constants.CHARACTER_SIZE
            val y = it.y / Constants.CHARACTER_SIZE
            getElement(x, y).fieldObject = FieldElement.FieldObject.POWER_UP_ITEM
        }
        // 爆弾をマップに追加
        gameScene.bombs.forEach(::addBomb)
        // 爆発を危険領域としてマップに追加
        gameScene.explosions.forEach {
            val x = it.x / Constants.CHARACTER_SIZE
            val y = it.y / Constants.CHARACTER_SIZE
            getElement(x, y).risk = RISK_OF_EXPLOSION
        }
    }

    fun getElement(x: Int, y: Int): FieldElement {
        return elements[y * GameScene.MAP_WIDTH + x]
    }

    /**
     * 指定位置からリスクのない場所へ移動できるか確認する
     */
    fun checkIfEscapable(x: Int, y: Int): Boolean {
        val checked = BooleanArray(GameScene.MAP_WIDTH * GameScene.MAP_HEIGHT)
        val searchQueue = ArrayDeque<FieldElement>()
        searchQueue.addLast(getElement(x, y))
        checked[x + y * GameScene.MAP_WIDTH] = true
        while (searchQueue.isNotEmpty()) {
            val element = searchQueue.removeFirst()
            val ex = element.x
            val ey = element.y
            arrayOf(getElement(ex - 1, ey), getElement(ex + 1, ey),getElement(ex, ey - 1),getElement(ex, ey + 1)).forEach {
                val idx = it.x + it.y * GameScene.MAP_WIDTH
                if (!checked[idx] && it.isPassable) {
                    if (it.risk == 0) return true
                    searchQueue.addLast(it)
                    checked[idx] = true
                }
            }
        }
        return false
    }

    /**
     * 爆弾を配置する
     *
     * @param bomb 配置する爆弾
     * @return この爆弾で破壊する壁の数
     */
    fun addBomb(bomb: Bomb): Int {
        val x = bomb.x / Constants.CHARACTER_SIZE
        val y = bomb.y / Constants.CHARACTER_SIZE
        val element = getElement(x, y)
        element.fieldObject = FieldElement.FieldObject.BOMB
        // この爆弾のリスク
        val risk = (Constants.BOMB_TIME - bomb.remainTime) * (RISK_OF_BOMB * 9 / 10) / Constants.BOMB_TIME + (RISK_OF_BOMB / 10)
        // リスクのセット
        element.risk = risk
        val breakCount = arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)).count {
            addBombRiskToFieldMap(bomb, risk, it.first, it.second)
        }
        return breakCount
    }

    /**
     * 爆弾が爆発したときのリスクを計算する
     *
     * @param bomb 爆弾
     * @param risk この爆弾のリスク
     * @param xx X方向に走査するベクトル
     * @param yy Y方向に走査するベクトル
     * @return 壁を破壊する場合 true
     */
    private fun addBombRiskToFieldMap(bomb: Bomb, risk: Int, xx: Int, yy: Int): Boolean {
        val x = bomb.x / Constants.CHARACTER_SIZE
        val y = bomb.y / Constants.CHARACTER_SIZE
        for (i in 1..bomb.power) {
            val px = x + xx * i
            val py = y + yy * i
            val fieldElement = getElement(px, py)
            if (fieldElement.fieldObject == FieldElement.FieldObject.BREAKABLE_WALL && !fieldElement.willBroken) {
                fieldElement.willBroken = true
                return true
            }
            if (fieldElement.fieldObject != FieldElement.FieldObject.NONE) {
                return false
            }
            if (risk > fieldElement.risk) {
                fieldElement.risk = risk
            }
        }
        return false
    }
}