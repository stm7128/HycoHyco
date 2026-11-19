<div align="center">
  <img src="doc/assets/app_icon.svg" width="96" alt="HycoHyco App Icon" />
  <h1>HycoHyco（ハイコハイコ）</h1>
  <p><strong>HyperOS × Material You 対応・完全ネイティブ Jetpack Compose 製 ニコニコ動画 Android クライアント</strong></p>
  <p>
    <img alt="Android" src="https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white" />
    <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white" />
    <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Native-4285F4?logo=jetpackcompose&logoColor=white" />
    <img alt="License" src="https://img.shields.io/badge/License-Apache--2.0-blue.svg" />
  </p>
</div>

---

## 概要

**HycoHyco** は、WebView 主体の動作の重さを排し、動画視聴からコメント弾幕・検索・マイリスト・オフライン保存までを **Jetpack Compose + Media3 (ExoPlayer) + Ktor** の完全ネイティブ実装で構築した非公式ニコニコ動画クライアントです（※アカウントログイン時の 2FA / reCAPTCHA 通過のみ一時的な WebView 認証を使用し、セッション取得後に即座に破棄します）。

**HyperOS（miuix）** と **Material You（Material 3 / Dynamic Color）** のデュアルデザインシステムを備え、設定画面からアプリ全体のデザインを即座に切り替えられます。

> **名前の由来**: **HyperOS + niconico** を組み合わせた造語です（いい名前が思いつかなかったため、ストレートに名付けました）。
>
> **開発スタイルについて**: 本プロジェクトは **AI エージェントを活用したバイブコーディング（AI 支援開発）** によって構築されています（要件・UX 方針の決定、実機での動作確認およびフィードバックは作者が行っています）。AI 生成コードが含まれることを明示したうえで公開していますので、その点をご理解のうえご利用・ご参照ください。

---

## 主な機能

- **デュアルデザインシステム & 大画面対応**
  - **HyperOS モード**（`compose-miuix-ui/miuix` 準拠）と **Material You モード**（Dynamic Color / AMOLED Pure Black 対応）をいつでも切り替え可能
  - 横画面・折りたたみ・タブレット向け **2ペインレイアウト**
  - Android 14+ 予測型「戻る」ジェスチャー（`PredictiveBackHandler`）による滑らかなプレイヤー縮小
- **高機能ネイティブプレイヤー & システム連携**
  - Media3 (ExoPlayer) による HLS ストリーミング再生・画質切替（Wi-Fi / モバイル通信別初期画質）・再生速度調整・スリープタイマー・音声共存再生
  - **全画面サイドパネル**: フルスクリーン視聴のまま右側から「関連動画 / コメント」を展開して連続再生・コメント閲覧が可能
  - **バックグラウンド再生 & PiP（小窓再生）**: 無料会員でもバックグラウンド再生 / PiP 対応、ニコニコ大百科・外部リンク遷移時の自動 PiP 小窓化、Xiaomi HyperOS スーパー島（Focus Notification）＆ Glance ホームウィジェット対応
  - **マルチタスク小窓（ミニプレイヤー）**: 下スワイプ縮小／ドラッグ移動可能なフローティング再生バー
  - **ジェスチャー操作**: 左右上下スワイプによる輝度・音量調整、ダブルタップシーク（秒数カスタマイズ）、長押し 2.0x 倍速再生（上部コンパクトピル HUD）、画面スリープ防止（`keepScreenOn`）
  - **前回の続きから再生（レジューム再生）** の ON/OFF 切り替え（明示的な `?from=` タイムスタンプジャンプや視聴履歴記録は独立して維持）
- **120fps CA（コメントアート）対応 弾幕描画エンジン**
  - `16:9`（ショート時は `9:16`）仮想キャンバス座標系とゼロアロケーション Compose `Canvas` による滑らかな描画
  - 黒・暗色コメントの自動白縁取り、不透明度・サイズ・速度・表示領域カスタマイズ、ニコるヒートマップ波形、過去ログ日時指定取得、コメント投稿 / 削除 / ニコる送信
  - 高度な NG フィルター（NG スコア閾値・正規表現・NG ワード・NG ユーザー ID・184 非表示）
- **ニコニコショート & タグ・大百科連携**
  - `/shorts/ssXXXX` の縦型 `VerticalPager` 連続再生＆縦型最適化弾幕
  - タグ一覧表示、ロックアイコン・大百科「百」バッジ、タグ検索ジャンプ、タグ追加・削除
- **探索・ライブラリ・完全オフライン保存・バックアップ**
  - おすすめタブ・ランキング（期間切替）・フォロー新着タイムライン・検索（無限スクロール・履歴・ピン留め）・Pull-to-Refresh・各種 Deep Link（動画 / ショート / ユーザー / マイリスト / シリーズ）
  - 動画（映像+音声）・音声のみ・弾幕コメント一式のローカル HLS 結合オフライン保存＆圏外再生
  - ログイン不要の無制限ローカルプレイリスト＆ローカル視聴履歴
  - ログイン時の公式マイリスト・あとで見る・フォロー新着・各種クラウド履歴（視聴・いいね！・ニコられた・ニコった）対応
  - 設定・プレイリスト・視聴履歴・検索履歴・NG ルールの **JSON 一括エクスポート / インポート**

---

## 技術スタック

| カテゴリ | 採用技術 |
| :--- | :--- |
| 言語 / UI | Kotlin `2.4.20` / Jetpack Compose (BOM `2024.10.01`) |
| デザインシステム | `top.yukonga.miuix.kmp:miuix-*` `0.9.4` / Material 3 `1.3.1` |
| メディア再生 | AndroidX Media3 (ExoPlayer / HLS / MediaSession) `1.5.0` |
| 通信 / シリアライズ | Ktor Client `3.0.1` + `kotlinx.serialization` `1.7.3` |
| DI / ローカル保存 | Koin `4.0.0` / Jetpack DataStore / SQLite |
| ウィジェット | Jetpack Compose Glance `1.1.1` |

---

## ビルド環境・手順

### 必要環境
- **JDK**: Java 17（`JAVA_HOME` 環境変数を設定、またはユーザーの `~/.gradle/gradle.properties` で指定）
- **Android SDK**: `compileSdk = 36` / `minSdk = 26` / `targetSdk = 36`

### ビルド手順

1. リポジトリをクローンします。
   ```bash
   git clone https://github.com/stm7128/HycoHyco.git
   cd HycoHyco
   ```
2. Android SDK のパスを設定します（環境変数 `ANDROID_HOME` を設定するか、ルート直下に `local.properties` を作成）。
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```
3. デバッグ APK をビルドします。
   ```bash
   ./gradlew assembleDebug
   ```
   生成された APK は `app/build/outputs/apk/debug/app-debug.apk` に出力されます。

### 署名付き Release APK のビルド（R8 有効）
- [keystore.properties.example](keystore.properties.example) をルート直下に `keystore.properties` としてコピーし、署名キーストア（`.jks`）とパスワードを設定したうえで実行します（いずれも `.gitignore` 対象）。
  ```bash
  ./gradlew assembleRelease
  ```
- GitHub Actions で自動リリースする場合は、リポジトリの Secrets に `ANDROID_KEYSTORE_BASE64` / `ANDROID_KEYSTORE_PASSWORD` / `ANDROID_KEY_ALIAS` / `ANDROID_KEY_PASSWORD` を登録し、`v*` タグ（例: `v1.0.0`）を Push すると [.github/workflows/release.yml](.github/workflows/release.yml) が起動します。

> **開発ガイドライン**: 1 ソースファイル 300 行以内・Clean Architecture（`data/`・`domain/`・`player/`・`ui/`）で設計されています。詳細は [AGENTS.md](AGENTS.md) および [doc/00_overview.md](doc/00_overview.md) を参照してください。

---

## ドキュメント

- [doc/00_overview.md](doc/00_overview.md): プロジェクト全体構成・アーキテクチャ概要
- [doc/FILE_GUIDE.md](doc/FILE_GUIDE.md): 全ソースファイル・モジュール解説リファレンス
- [doc/NICO_API_REFERENCE.md](doc/NICO_API_REFERENCE.md): ニコニコ動画 API 解析リファレンス

---

## 免責事項

- 本ソフトウェアは **個人の趣味・技術検証として開発された非公式サードパーティ製クライアント** であり、株式会社ドワンゴおよび「ニコニコ動画」公式サービスとは一切関係ありません。
- 外部サービスの仕様変更等により一部または全部の機能が動作しなくなる場合があります。
- 本ソフトウェアの利用および再配布はすべて自己責任にて行ってください。

---

## ライセンス

[Apache License 2.0](LICENSE)
