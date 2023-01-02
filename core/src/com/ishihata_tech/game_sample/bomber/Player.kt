package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.math.absoluteValue

class Player(
        gameScene: GameScene,
        playerNumber: Int,
        x: Int,
        y: Int,
): LightSprite(gameScene, x, y) {
    companion object {
        private const val WALK_SPEED = 2
        private const val TIME_TO_DEATH = 60

        private val KEY_ASSIGN = arrayOf(
                KeyAssign(Input.Keys.A, Input.Keys.W, Input.Keys.D, Input.Keys.S, Input.Keys.NUM_1),
                KeyAssign(Input.Keys.LEFT, Input.Keys.UP, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.SLASH)
        )
    }

    enum class Direction(val position: Int) {
        DOWN(0),
        LEFT(1),
        RIGHT(2),
        UP(3),
    }

    private class KeyAssign(val left: Int, val up: Int, val right: Int, val down: Int, val fire: Int)
    private val keyAssign = KEY_ASSIGN[playerNumber]

    private val playerImage = if (playerNumber == 0) gameScene.player1Image else gameScene.player2Image

    var pushedX = 0
    var pushedY = 0

    private var direction = Direction.DOWN
    private var moveTime = 0f
    var power = 1
    private var walkSoundIsPlaying = false
    private var walkSoundId = 0L
    private var deathState = 0
    val isDead: Boolean
        get() = deathState > 0

    override fun draw(batch: SpriteBatch) {
        if (deathState >= TIME_TO_DEATH) return

        val step = (moveTime / 0.2f).toInt() % 3
        if (isDead) {
            // 死に途中
            val prevColor = Color(batch.color)
            batch.setColor(1f, 0f, 0f, 1f - deathState.toFloat() / TIME_TO_DEATH)
            batch.draw(
                    playerImage,
                    (x - deathState).toFloat(),
                    (y - deathState).toFloat(),
                    (width + deathState * 2).toFloat(),
                    (height + deathState * 2).toFloat(),
                    step * width,
                    direction.position * height,
                    width,
                    height,
                    false,
                    false
            )
            batch.color = prevColor
        } else {
            batch.draw(
                    playerImage,
                    x.toFloat(),
                    y.toFloat(),
                    step * width,
                    direction.position * height,
                    width,
                    height
            )
        }
    }

    /**
     * このフレームでの移動処理
     * この処理の後に別プレイヤーとの当たり判定が入る
     * その後 onNextFrame() が呼ばれる
     */
    fun moveForNextFrame() {
        if (isDead) return

        // 移動前の位置を保存しておく
        val oldX = x
        val oldY = y

        if (Gdx.input.isKeyPressed(keyAssign.left)) {
            direction = Direction.LEFT
            x -= WALK_SPEED
        }
        else if (Gdx.input.isKeyPressed(keyAssign.right)) {
            direction = Direction.RIGHT
            x += WALK_SPEED
        }
        else if (Gdx.input.isKeyPressed(keyAssign.up)) {
            direction = Direction.UP
            y += WALK_SPEED
        }
        else if (Gdx.input.isKeyPressed(keyAssign.down)) {
            direction = Direction.DOWN
            y -= WALK_SPEED
        }

        // 壁との当たり判定
        val detectWalls = gameScene.walls.filter {
            (it.x - x).absoluteValue < Constants.CHARACTER_SIZE && (it.y - y).absoluteValue < Constants.CHARACTER_SIZE
        }.toList()
        if (detectWalls.isNotEmpty()) {
            x = oldX
            y = oldY
        }
        if (detectWalls.count() == 1) {
            val wall = detectWalls.first()
            if (Gdx.input.isKeyPressed(keyAssign.left) ||
                    Gdx.input.isKeyPressed(keyAssign.right)) {
                if (y < wall.y) y -= WALK_SPEED
                if (y > wall.y) y += WALK_SPEED
            }
            else if (Gdx.input.isKeyPressed(keyAssign.up) ||
                    Gdx.input.isKeyPressed(keyAssign.down)) {
                if (x < wall.x) x -= WALK_SPEED
                if (x > wall.x) x += WALK_SPEED
            }
        }

        // 爆弾との当たり判定
        // 32で割り切れる場所からそうでない場所に移動しようとした場合は、移動先に爆弾があったら動かさない
        if (x != oldX && oldX % Constants.CHARACTER_SIZE == 0) {
            val bx = if (x > oldX) oldX + Constants.CHARACTER_SIZE else oldX - Constants.CHARACTER_SIZE
            if (gameScene.bombs.any { it.x == bx && it.y == y }) x = oldX
        } else if (y != oldY && oldY % Constants.CHARACTER_SIZE == 0) {
            val by = if (y > oldY) oldY + Constants.CHARACTER_SIZE else oldY - Constants.CHARACTER_SIZE
            if (gameScene.bombs.any { it.y == by && it.x == x }) y = oldY
        }
        // 32で割り切れない場所から移動しようとした場合は、一番近いマス以外のマスに移動しようとしている場合、移動先に爆弾があったら動かさない
        else if (x != oldX) {
            var bx: Int? = null
            if (oldX % Constants.CHARACTER_SIZE < Constants.CHARACTER_SIZE / 2) {
                if (x > oldX) {
                    bx = (oldX / Constants.CHARACTER_SIZE + 1) * Constants.CHARACTER_SIZE
                }
            } else {
                if (x < oldX) {
                    bx = (oldX / Constants.CHARACTER_SIZE) * Constants.CHARACTER_SIZE
                }
            }
            if (bx != null && gameScene.bombs.any { it.x == bx && it.y == y }) x = oldX
        } else if (y != oldY) {
            var by: Int? = null
            if (oldY % Constants.CHARACTER_SIZE < Constants.CHARACTER_SIZE / 2) {
                if (y > oldY) {
                    by = (oldY / Constants.CHARACTER_SIZE + 1) * Constants.CHARACTER_SIZE
                }
            } else {
                if (y < oldY) {
                    by = (oldY / Constants.CHARACTER_SIZE) * Constants.CHARACTER_SIZE
                }
            }
            if (by != null && gameScene.bombs.any { it.x == x && it.y == by }) y = oldY
        }

        // 実際に移動させる
        if (x != oldX || y != oldY) {
            moveTime += Gdx.graphics.deltaTime
            if (!walkSoundIsPlaying) {
                walkSoundIsPlaying = true
                walkSoundId = gameScene.walkSound.loop()
            }
        } else {
            if (walkSoundIsPlaying) {
                walkSoundIsPlaying = false
                gameScene.walkSound.stop(walkSoundId)
            }
        }
    }

    override fun onNextFrame(): Boolean {
        if (isDead) {
            deathState++
            return false
        }

        // パワーアップアイテムとの当たり判定
        val powerUpItemIterator = gameScene.powerUpItems.iterator()
        while (powerUpItemIterator.hasNext()) {
            val powerUpItem = powerUpItemIterator.next()
            if ((powerUpItem.x - x).absoluteValue < Constants.CHARACTER_SIZE &&
                    (powerUpItem.y - y).absoluteValue < Constants.CHARACTER_SIZE) {
                powerUpItemIterator.remove()
                power++
                gameScene.powerUpSound.play()
            }
        }

        // 爆弾の設置
        if (Gdx.input.isKeyPressed(keyAssign.fire)) {
            val bx = (x + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE * Constants.CHARACTER_SIZE
            val by = (y + Constants.CHARACTER_SIZE / 2) / Constants.CHARACTER_SIZE * Constants.CHARACTER_SIZE
            if (!gameScene.bombs.any { it.x == bx && it.y == by }) {
                gameScene.bombs.add(Bomb(gameScene, bx, by, power))
                gameScene.setBombSound.play()
            }
        }

        // 爆発との当たり判定
        if (gameScene.explosions.any { (it.x - x).absoluteValue < 28 && (it.y - y).absoluteValue < 28 }) {
            deathState = 1
            gameScene.bgmMusic.stop()
            gameScene.crashSound.play()
            if (walkSoundIsPlaying) {
                walkSoundIsPlaying = false
                gameScene.walkSound.stop(walkSoundId)
            }
        }

        return false
    }

    fun pushPosition() {
        pushedX = x
        pushedY = y
    }

    fun popPosition() {
        x = pushedX
        y = pushedY
    }
}