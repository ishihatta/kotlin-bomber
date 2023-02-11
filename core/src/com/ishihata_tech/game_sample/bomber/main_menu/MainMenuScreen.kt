package com.ishihata_tech.game_sample.bomber.main_menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.ishihata_tech.game_sample.bomber.MyGame
import com.ishihata_tech.game_sample.bomber.game_screen.PlayerType

class MainMenuScreen(private val game: MyGame) : ScreenAdapter() {
    companion object {
        private const val MENU_ITEM_X = 300f
        private const val MENU_ITEM_Y_START = 220f
        private const val MENU_ITEM_Y_STEP = 40f
    }

    enum class MenuItem(val screenText: String, val playerType1: PlayerType, val playerType2: PlayerType) {
        HUMAN_VS_AI("HUMAN VS AI", PlayerType.HUMAN, PlayerType.AI),
        AI_VS_HUMAN("AI VS HUMAN", PlayerType.AI, PlayerType.HUMAN),
        HUMAN_VS_HUMAN("HUMAN VS HUMAN", PlayerType.HUMAN, PlayerType.HUMAN),
        AI_VS_AI("AI VS AI", PlayerType.AI, PlayerType.AI)
    }

    // ステージ
    private val stage = Stage(StretchViewport(800f, 480f))
    // ロゴ画像
    private val logoTexture = Texture(Gdx.files.internal("logo.png"))
    private val logoImage = Image(logoTexture)
    // カーソル画像
    private val cursorTexture = Texture(Gdx.files.internal("pipo-charachip018b.png"))
    private val cursorImage = Image(TextureRegion(cursorTexture, 32, 32))
    // フォント
    private val font16: BitmapFont
    // メニューアイテムのラベル
    private val menuItemLabel: List<Label>

    // BGM
    private val bgmMusic: Music = Gdx.audio.newMusic(Gdx.files.internal("title_bgm.mp3"))
    // ゲーム開始ジングルの読み込み
    private val startGameSound: Sound = Gdx.audio.newSound(Gdx.files.internal("start_game.mp3"))

    // 選択中のアイテムのインデックス
    private var cursor = 0
    // 前のフレームでカーソル移動した方向
    private var previousCursorMove = 0

    // 画面遷移エフェクト進行中フラグ
    private var isGoingToGameScreen = false

    init {
        // フォントの生成
        FreeTypeFontGenerator(Gdx.files.internal("m12.ttf")).also { fontGenerator ->
            font16 = fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 16
            })
            fontGenerator.dispose()
        }

        // ロゴ
        logoImage.setPosition(400f, 360f, Align.center)
        stage.addActor(logoImage)

        // メニューアイテム
        menuItemLabel = MenuItem.values().mapIndexed { index, item ->
            Label(item.screenText, Label.LabelStyle(font16, Color(0.7f, 0.7f, 0.7f, 1f))).also {
                it.setPosition(MENU_ITEM_X, MENU_ITEM_Y_START - MENU_ITEM_Y_STEP * index)
                stage.addActor(it)
            }
        }

        // カーソル
        cursorImage.setPosition(MENU_ITEM_X - 40, MENU_ITEM_Y_START - MENU_ITEM_Y_STEP * cursor - 8f)
        stage.addActor(cursorImage)

        // BGM再生
        bgmMusic.play()
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        stage.act(delta)
        stage.draw()

        // キー操作
        val menuItems = MenuItem.values()
        if (!isGoingToGameScreen) {
            // カーソル移動
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                if (previousCursorMove != -1) {
                    cursor--
                    if (cursor < 0) cursor = menuItems.size - 1
                    previousCursorMove = -1
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                if (previousCursorMove != 1) {
                    cursor++
                    if (cursor >= menuItems.size) cursor = 0
                    previousCursorMove = 1
                }
            } else {
                previousCursorMove = 0
            }
            // カーソルの画面反映
            cursorImage.y = MENU_ITEM_Y_START - MENU_ITEM_Y_STEP * cursor - 8f
            menuItemLabel.forEachIndexed { index, label ->
                label.setColor(if (index == cursor) 1f else 0.7f, 0.7f, 0.7f, 1f)
            }

            // 決定キー
            if (Gdx.input.isKeyPressed(Input.Keys.SLASH) ||
                    Gdx.input.isKeyPressed(Input.Keys.NUM_1) ||
                    Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                // BGM停止
                bgmMusic.stop()
                // ジングル再生
                startGameSound.play(0.5f)
                // 画面遷移開始
                isGoingToGameScreen = true
                val selectedLabel = menuItemLabel[cursor]
                stage.root.addAction(SequenceAction(
                        Actions.repeat(12, SequenceAction(
                                Actions.run { selectedLabel.setColor(0.5f, 0.5f, 0.5f, 1f) },
                                Actions.delay(0.1f),
                                Actions.run { selectedLabel.setColor(1f, 0.7f, 0.7f, 1f) },
                                Actions.delay(0.1f)
                        )),
                        Actions.fadeOut(0.3f),
                        Actions.run {
                            val selectedItem = menuItems[cursor]
                            game.goToGameScreen(selectedItem.playerType1, selectedItem.playerType2)
                        }
                ))
            }
        }
    }

    override fun dispose() {
        logoTexture.dispose()
        cursorTexture.dispose()
        font16.dispose()
        startGameSound.dispose()
        bgmMusic.dispose()
        stage.dispose()
    }
}