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
import kotlin.math.absoluteValue

class GameScene : Disposable {
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

    // 爆発を一時的に生成するための配列
    val newExplosions = Array<Explosion>()

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
        players.add(Player(this, 0, Constants.CHARACTER_SIZE, 480 - Constants.CHARACTER_SIZE * 2))
        players.add(Player(this, 1, 800 - Constants.CHARACTER_SIZE * 2, Constants.CHARACTER_SIZE))

        // 外壁の生成
        walls.clear()
        for (x in 0..24) {
            val xf = x * Constants.CHARACTER_SIZE
            walls.add(Wall(this, xf, 0, false))
            walls.add(Wall(this, xf, 14 * Constants.CHARACTER_SIZE, false))
        }
        for (y in 1..13) {
            val yf = y * Constants.CHARACTER_SIZE
            walls.add(Wall(this, 0, yf, false))
            walls.add(Wall(this, 24 * Constants.CHARACTER_SIZE, yf, false))
        }

        // 壁の生成
        for (y in 1..13) {
            val yf = y * Constants.CHARACTER_SIZE
            for (x in 1..23) {
                val xf = x * Constants.CHARACTER_SIZE
                if (x % 2 == 0 && y % 2 == 0) {
                    // 壊せない壁
                    walls.add(Wall(this, xf, yf, false))
                } else {
                    // 壊せる壁
                    if (x < 3 && y > 11 || x > 21 && y < 3) {
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
        newExplosions.clear()

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

        // プレイヤー、パワーアップアイテム、壁、爆発, 爆弾の状態変化
        arrayOf(players, powerUpItems, walls, explosions, bombs).forEach {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().onNextFrame()) {
                    iterator.remove()
                }
            }
        }

        // 新たに生成された爆発を追加する
        explosions.addAll(newExplosions)
        newExplosions.clear()

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