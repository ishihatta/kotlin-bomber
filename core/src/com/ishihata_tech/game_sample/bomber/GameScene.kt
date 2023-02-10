package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import com.ishihata_tech.game_sample.bomber.ai.AIPlayer
import kotlin.math.absoluteValue

class GameScene(private val playerType1: PlayerType, private val playerType2: PlayerType) : Disposable {
    companion object {
        const val MAP_WIDTH = 25
        const val MAP_HEIGHT = 15
    }

    /**
     * プレイヤータイプ（人間かAIか）
     */
    enum class PlayerType {
        HUMAN {
            override fun generatePlayerOperation(gameScene: GameScene, playerNumber: Int): PlayerOperation {
                return UserPlayerOperation(0)
            }
        },
        AI {
            override fun generatePlayerOperation(gameScene: GameScene, playerNumber: Int): PlayerOperation {
                return AIPlayer(gameScene, playerNumber)
            }
        };

        abstract fun generatePlayerOperation(gameScene: GameScene, playerNumber: Int): PlayerOperation
    }

    /**
     * ゲーム状態
     */
    enum class State {
        PLAYING,
        PLAYER1_WON,
        PLAYER2_WON,
        DRAW_GAME,
    }

    // ゲーム状態
    private var state = State.PLAYING

    // フォント
    private val font16: BitmapFont
    private val font32: BitmapFont

    // テクスチャの読み込み
    val wallImage = Texture(Gdx.files.internal("wall.png"))
    val breakableWallImage = Texture(Gdx.files.internal("breakable_wall.png"))
    val player1Image = Texture(Gdx.files.internal("pipo-charachip018b.png"))
    val player2Image = Texture(Gdx.files.internal("pipo-charachip018a.png"))
    val bombImage = Texture(Gdx.files.internal("pipo-simpleenemy01b.png"))
    val explosionImage = Texture(Gdx.files.internal("explosion.png"))
    val powerUpImage = Texture(Gdx.files.internal("pipo-etcchara003.png"))

    // 効果音の読み込み
    val explosionSound: Sound = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"))
    val setBombSound: Sound = Gdx.audio.newSound(Gdx.files.internal("set_bomb.mp3"))
    val walkSound: Sound = Gdx.audio.newSound(Gdx.files.internal("walk.mp3"))
    val powerUpSound: Sound = Gdx.audio.newSound(Gdx.files.internal("power_up.mp3"))
    val crashSound: Sound = Gdx.audio.newSound(Gdx.files.internal("crash.mp3"))

    // BGMの読み込み
    val bgmMusic: Music = Gdx.audio.newMusic(Gdx.files.internal("Daily_News.mp3")).apply {
        isLooping = true
        volume = 0.7f
    }

    // スプライトの配列
    val players = Array<Player>()
    val walls = Array<Wall>()
    val bombs = Array<Bomb>()
    val explosions = Array<Explosion>()
    val powerUpItems = Array<PowerUpItem>()

    // カメラ
    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 480f)
    }
    // スプライトバッチ
    private val batch = SpriteBatch()

    init {
        // フォントの生成
        FreeTypeFontGenerator(Gdx.files.internal("m12.ttf")).also { fontGenerator ->
            font16 = fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 16
            })
            font32 = fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 32
            })
            fontGenerator.dispose()
        }
        // ゲーム開始
        startGame()
    }

    private fun startGame() {
        state = State.PLAYING

        // Playerの生成
        players.clear()
        players.add(Player(
                this,
                0,
                playerType1.generatePlayerOperation(this, 0),
                Constants.CHARACTER_SIZE,
                480 - Constants.CHARACTER_SIZE * 2
        ))
        players.add(Player(
                this,
                1,
                playerType2.generatePlayerOperation(this, 1),
                800 - Constants.CHARACTER_SIZE * 2,
                Constants.CHARACTER_SIZE
        ))

        // 外壁の生成
        walls.clear()
        for (x in 0 until MAP_WIDTH) {
            val xf = x * Constants.CHARACTER_SIZE
            walls.add(Wall(this, xf, 0, false))
            walls.add(Wall(this, xf, 14 * Constants.CHARACTER_SIZE, false))
        }
        for (y in 1 until MAP_HEIGHT - 1) {
            val yf = y * Constants.CHARACTER_SIZE
            walls.add(Wall(this, 0, yf, false))
            walls.add(Wall(this, 24 * Constants.CHARACTER_SIZE, yf, false))
        }

        // 壁の生成
        for (y in 1 until MAP_HEIGHT - 1) {
            val yf = y * Constants.CHARACTER_SIZE
            for (x in 1 until MAP_WIDTH - 1) {
                val xf = x * Constants.CHARACTER_SIZE
                if (x % 2 == 0 && y % 2 == 0) {
                    // 壊せない壁
                    walls.add(Wall(this, xf, yf, false))
                } else {
                    // 壊せる壁
                    if (x < 3 && y > MAP_HEIGHT - 4 || x > MAP_WIDTH - 4 && y < 3) {
                        // プレイヤー出現位置の近くには壁は作らない
                    } else if (MathUtils.random(100) < 50) {
                        walls.add(Wall(this, xf, yf, true))
                    }
                }
            }
        }

        // その他のオブジェクトの初期化
        bombs.clear()
        explosions.clear()
        powerUpItems.clear()

        // 効果音の初期化
        explosionSound.stop()
        setBombSound.stop()
        walkSound.stop()
        powerUpSound.stop()
        crashSound.stop()

        // BGMの再生
        bgmMusic.play()
    }

    fun render() {
        ScreenUtils.clear(0f, 0.7f, 0f, 1f)
        camera.update()
        batch.projectionMatrix = camera.combined

        // 描画
        batch.begin()
        // 各種オブジェクトの描画
        arrayOf(walls, bombs, powerUpItems, explosions, players).forEach {
            it.forEach { sprite -> sprite.draw(batch) }
        }
        // ゲーム終了時の描画
        when (state) {
            State.PLAYING -> Unit
            State.PLAYER1_WON -> {
                font32.setColor(0.7f, 0f, 0f, 1f)
                font32.draw(batch, "PLAYER 1 WIN", 0f, 256f, 800f, Align.center, false)
            }
            State.PLAYER2_WON -> {
                font32.setColor(0f, 0f, 1f, 1f)
                font32.draw(batch, "PLAYER 2 WIN", 0f, 256f, 800f, Align.center, false)
            }
            State.DRAW_GAME -> {
                font32.setColor(1f, 1f, 1f, 1f)
                font32.draw(batch, "DRAW GAME", 0f, 256f, 800f, Align.center, false)
            }
        }
        // 画面上部に表示する各プレイヤーの状態描画
        font16.setColor(0.7f, 0f, 0f, 1f)
        font16.draw(batch, "PLAYER 1 POWER ${players[0].power}", 0f, 480f)
        font16.setColor(0f, 0f, 1f, 1f)
        font16.draw(batch, "PLAYER 2 POWER ${players[1].power}", 800f - 16f * 16, 480f)
        batch.end()

        // プレイヤーの移動処理
        players.forEach {
            it.pushPosition()
            it.moveForNextFrame()
        }

        // プレイヤー同士の衝突回避
        playersCollisionDetect()

        // プレイヤー、パワーアップアイテム、壁、爆発の状態変化
        arrayOf(players, powerUpItems, walls, explosions).forEach {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().onNextFrame()) {
                    iterator.remove()
                }
            }
        }

        // 爆弾の状態変化
        val newExplodeBomb = Array<Bomb>()
        bombs.iterator().also { iterator ->
            while (iterator.hasNext()) {
                val bomb = iterator.next()
                if (bomb.onNextFrame()) {
                    // 爆発した
                    iterator.remove()
                    // 爆発した爆弾をリストに入れておく
                    newExplodeBomb.add(bomb)
                }
            }
        }

        // 爆発の生成
        if (!newExplodeBomb.isEmpty) {
            explosionSound.play()
            newExplodeBomb.forEach { bomb ->
                explosions.add(Explosion(this, bomb.x, bomb.y, Explosion.Position.CENTER))
                expandExplosion(bomb, -1, 0)
                expandExplosion(bomb, 1, 0)
                expandExplosion(bomb, 0, -1)
                expandExplosion(bomb, 0, 1)
            }
        }

        // ゲーム状態の変化
        if (state == State.PLAYING) {
            if (players[0].isDead && players[1].isDead) {
                state = State.DRAW_GAME
            } else if (players[0].isDead) {
                state = State.PLAYER2_WON
            } else if (players[1].isDead) {
                state = State.PLAYER1_WON
            }
        } else {
            // ゲームが終わっている状態でスペースキーが押されると最初からになる
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                startGame()
            }
        }
    }

    /**
     * プレイヤー同士の衝突回避
     * 1. 移動後の位置が重なっていなければどちらも移動できる
     * 2. 重なっている場合、片方の移動をキャンセルすることで重なりがなくなり、かつもう片方の移動をキャンセルしても重なりがなくならない場合は
     *    重なりがなくなる方の移動をキャンセルする
     * 3. 1、2 どちらでもない場合は両方の移動をキャンセルする
     */
    private fun playersCollisionDetect() {
        if (players[0].isDead || players[1].isDead) return
        if ((players[0].x - players[1].x).absoluteValue < Constants.CHARACTER_SIZE &&
                (players[0].y - players[1].y).absoluteValue < Constants.CHARACTER_SIZE) {
            val player0IsNotCancelable = (players[0].pushedX - players[1].x).absoluteValue < Constants.CHARACTER_SIZE &&
                    (players[0].pushedY - players[1].y).absoluteValue < Constants.CHARACTER_SIZE
            val player1IsNotCancelable = (players[0].x - players[1].pushedX).absoluteValue < Constants.CHARACTER_SIZE &&
                    (players[0].y - players[1].pushedY).absoluteValue < Constants.CHARACTER_SIZE
            if (!player0IsNotCancelable && player1IsNotCancelable) {
                players[0].popPosition()
            } else if (player0IsNotCancelable && !player1IsNotCancelable) {
                players[1].popPosition()
            } else {
                players[0].popPosition()
                players[1].popPosition()
            }
        }
    }

    /**
     * 指定した爆弾位置から指定した方向に爆発を生成する
     */
    private fun expandExplosion(bomb: Bomb, xx: Int, yy: Int) {
        for (n in 1..bomb.power) {
            val px = bomb.x + xx * n * Constants.CHARACTER_SIZE
            val py = bomb.y + yy * n * Constants.CHARACTER_SIZE

            // 壁があるか？
            walls.find { it.x == px && it.y == py }?.also { wall ->
                // 壁の破壊
                if (wall.isBreakable) {
                    wall.startMelting()
                }
                return
            }
            // 爆弾があったら誘爆する
            val bombIndex = bombs.indexOfFirst { it.x == px && it.y == py }
            if (bombIndex >= 0) {
                bombs[bombIndex].remainTime = 1
                return
            }
            // パワーアップアイテムがあったら破壊する
            val powerUpItemIndex = powerUpItems.indexOfFirst { it.x == px && it.y == py }
            if (powerUpItemIndex >= 0) {
                powerUpItems.removeIndex(powerUpItemIndex)
                return
            }
            // 新しい爆発を生成する
            val position: Explosion.Position = if (xx == 0) {
                if (n == bomb.power) {
                    if (yy > 0f) Explosion.Position.TOP else Explosion.Position.BOTTOM
                } else {
                    Explosion.Position.VERTICAL
                }
            } else {
                if (n == bomb.power) {
                    if (xx > 0f) Explosion.Position.RIGHT else Explosion.Position.LEFT
                } else {
                    Explosion.Position.HORIZONTAL
                }
            }
            explosions.add(Explosion(this, px, py, position))
        }
    }

    override fun dispose() {
        player1Image.dispose()
        player2Image.dispose()
        wallImage.dispose()
        breakableWallImage.dispose()
        bombImage.dispose()
        explosionImage.dispose()
        powerUpImage.dispose()
        batch.dispose()

        explosionSound.dispose()
        setBombSound.dispose()
        walkSound.dispose()
        powerUpSound.dispose()
        crashSound.dispose()

        bgmMusic.dispose()

        font16.dispose()
        font32.dispose()
    }
}