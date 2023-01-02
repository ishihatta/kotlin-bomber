# これは何？
Kotlin で書いた簡単な対戦2Dアクションゲームです。Windows, MacOS, Linux 等のデスクトップで動作し、キーボードで操作します。

# ゲームの内容
二人対戦専用のボン○ーマンです。ルールは以下の通りです。

* アイテムは火力アップのみ
* 爆弾は無限に置ける
* 死んだら負け

# プレイ動画

https://user-images.githubusercontent.com/40629744/210214223-5026a6b1-8405-493b-9c45-cc59f73d4bcb.mp4

# ビルド方法

```shell
./gradlew :desktop:dist
```

上記コマンドでビルドされ `desktop/biuld/lib` ディレクトリに `desktop-1.0.jar` ファイルが作成されます。

# 起動方法
## JAR ファイルからの実行
上記ビルド方法で作成された JAR ファイルは以下のコマンドで実行できます。

```shell
java -jar desktop-1.0.jar
```

ただし MacOS ではこのコマンドではエラーになります。その場合は以下のコマンドで実行できます。

```shell
java -XstartOnFirstThread -jar desktop-1.0.jar
```

## Gradle タスクからの実行
JAR ファイルを作成しなくても以下のように Gradle タスクで実行することも可能です。

```shell
./gradlew :desktop:run
```

# 操作方法（キーアサイン）

|       | Player 1 | Player 2 |
|-------|----------|----------|
| 上に移動  | W        | カーソル上    |
| 右に移動  | D        | カーソル右    |
| 下に移動  | S        | カーソル下    |
| 左に移動  | A        | カーソル左    |
| 爆弾を置く | 1        | /        |

# 使用フレームワーク
ゲームフレームワークとして [libGDX](https://libgdx.com/) を使っています。

# 使用素材
## 画像
以下のサイトで無償配布されている画像を使わせていただいています。

* [ぴぽや倉庫](https://pipoya.net/sozai/)

## サウンド
以下のサイトで無償配布されている効果音およびBGMの音源を使わせていただいています。

* [DOVA-SYNDROME](https://dova-s.jp/)
