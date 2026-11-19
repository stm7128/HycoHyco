# AGENTS.md - HycoHyco 開発規約 & エージェント行動指針

このドキュメントは、Android版サードパーティ製ニコニコ動画クライアント **「HycoHyco（ハイコハイコ）」** の開発における絶対的なコード規約・アーキテクチャ原則・エージェントの行動約束を定義します。

---

## 1. エージェントの絶対ルール（約束事）

1. **先回り実装の禁止 & マイルストーン開始時のロードマップ策定（スコープ厳守・MD永続化）**
   - 指示・合意されていないフェーズや機能の先回り実装を禁止します。
   - **各マイルストーンに入る際は、いきなりコード実装からスタートすることを厳禁とします。**
   - 必ず最初に「そのマイルストーンで行う作業の詳細ロードマップ（ステップ分割・作成予定ファイル・設計方針）」を策定して提示し、**合意を得てから**実装に着手してください。
   - **ロードマップの Markdown ファイル永続化の義務化（コンテキスト消失防止）**:
     - ロードマップはチャットの返答内だけで済ませず、必ずリポジトリ直下の `ROADMAP.md` および `doc/` 配下の仕様書に Markdown ファイルとして作成・保存・更新してください。
     - チャット履歴がコンテキスト制限で要約・消失したり、別セッションへ引き継がれたりしても、いつでもファイルから正確な開発計画と進捗を復元・参照できるようにするためです。
   - 実装中も、合意したステップごとに確認・コミットを行いながら進めてください。
2. **ビルド確認の義務化**
   - コードを追加・変更した後は、必ず Gradle ビルド（`./gradlew assembleDebug` 等）を実行し、**コンパイルエラーがないことを確認してから**完了報告を行ってください。
3. **WebView 原則禁止（完全ネイティブ主義 / 認証時のみ例外許可）**
   - 公式アプリの「重さ・キモさ」の原因である WebView の使用は原則禁止とし、すべて **Jetpack Compose ネイティブ** で実装します。
   - **【ユーザー合意済み例外】**: ニコニコアカウントのログイン認証フロー（`https://account.nicovideo.jp/login`）に限り、reCAPTCHA・2FA・外部ID連携等を確実に通過するため一時的な WebView の使用を許可します。ただし、`CookieManager` から `user_session` を抽出した瞬間に WebView を即座に破棄し、以降の通信・画面描画はすべて Ktor + Compose ネイティブで行います。
4. **1ファイル 300行以内の原則**
   - 1つのソースファイルが **300行** を超えないよう、責務ごとにファイル・コンポーネント・クラスを積極的に分割してください。
   - 巨大な God Class や 1ファイルに詰め込まれた Composable 群は禁止します。
5. **すべて日本語で統一**
   - コード内のコメント、KDoc、ログメッセージ、コミットメッセージ、ドキュメントはすべて **日本語** で記述してください。
6. **仕様・設計ドキュメント (`doc/`) の常時同期**
   - リポジトリ直下の `doc/` フォルダに、機能・モジュールごとの仕様書（`.md`）を配置・更新してください。
   - **「ソースコードを読まなくても、`doc/` の Markdown を読むだけで処理フロー・API仕様・クラス責務・状態遷移が完全に理解できる状態」** を維持してください（別セッションや他の LLM への引き継ぎを容易にするため）。
   - 新機能の追加や仕様変更を行った際は、必ず対応する `doc/*.md` もセットで作成・更新してください。
7. **こまめな Git コミット（リバート容易性の確保）**
   - 機能実装・バグ修正・ドキュメント更新など、論理的な区切り（かつビルドが通る状態）ごとに必ず `git commit` を行ってください。
   - 1つの巨大コミットに複数の変更を混ぜず、問題発生時にいつでも `git revert` や `git reset` で安全に巻き戻せる粒度を保つこと。
   - コミットメッセージは日本語の Conventional Commits 形式（例: `feat: 初回テーマ選択画面の実装`, `docs: AGENTS.mdと基本設計書の追加`, `fix: DMCキープアライブの周期修正`）を使用してください。
8. **実機・エミュレータの勝手な自動操作・画面テストの絶対禁止（テストはユーザー主体 / エージェントの権限は `adb install` まで）**
   - エージェントが独断で `adb shell input tap` や `input keyevent` などを用いて端末画面を勝手に操作・テストすることを厳禁とします。
   - エージェントに許可されている実行範囲は、コード実装、ビルド確認（`./gradlew assembleDebug` 等）、および実機・エミュレータへのインストール（`adb install`）までです。
   - 実際の動作確認・UI検証・テストはすべてユーザー本人が実機で行い、フィードバックを行います。
   - ユーザーから「○○のログを確認して」「この画面をキャプチャして」等の明示的な指示がない限り、勝手な自動操作・探索を行ってはなりません。

---

## 2. 技術スタック & ビルド環境

### 採用ライブラリ・技術
- **言語**: Kotlin
- **UI フレームワーク**: Jetpack Compose
- **デザインシステム**:
  - **HyperOS モード**: `top.yukonga.miuix.kmp:miuix-*` (`compose-miuix-ui/miuix`)
  - **Material You モード**: `androidx.compose.material3:material3` (Dynamic Color / Monet)
- **DI (依存性注入)**: Koin (`io.insert-koin:koin-android`, `koin-androidx-compose`)
- **HTTP 通信 & シリアライズ**: Ktor Client (`io.ktor:ktor-client-*`) + `kotlinx.serialization`
- **メディア再生**: AndroidX Media3 / ExoPlayer (`androidx.media3:media3-exoplayer`, `media3-exoplayer-hls`, `media3-session`)
- **ローカルデータ保存**: Jetpack DataStore (Preferences) / Room

### ビルド環境要件（環境非依存設定）
- **Android SDK**: `local.properties`（`sdk.dir=...` / Git管理外）または環境変数 `ANDROID_HOME` で指定すること
- **JDK (Gradle 実行用)**: Java 17 以上を環境変数 `JAVA_HOME`（またはユーザーごとの `~/.gradle/gradle.properties` の `org.gradle.java.home`）で設定すること
- **ADB**: Android SDK `platform-tools` を `PATH` に通した `adb` コマンドを使用すること
- **Android CLI**: `android` コマンドが利用可能な環境では `android-cli` スキルと併用して活用すること（ハードコードされたローカルパスはリポジトリ内に記載しない）

### Android CLI (`android` コマンド) の積極活用指針
本環境では公式 `android` CLI ツールおよびエージェントスキルが導入されています。開発・デバッグ時は以下の機能を積極的に活用してください：
- **公式ドキュメントの検索・参照 (`android docs search "<keyword>"` / `android docs fetch <kb://url>`)**:
  - Media3 (ExoPlayer / MediaSessionService)、Jetpack Compose、PiP (Picture-in-Picture)、Predictive Back などの最新仕様や移行ガイドを確認する際は、古い知識や推測に頼らず `android docs` で公式ナレッジベースを検索すること。
- **ビルド成果物・プロジェクト解析 (`android describe`)**:
  - モジュール構造や生成された APK (`app-debug.apk` 等) の出力パス・存在確認に利用すること。
- **デバイス・エミュレータでのインストール (`android install` / `adb install`)**:
  - 実機やエミュレータ接続時は、`adb install` や `android install` を用いてビルド成果物をインストールすること。
  - **【重要】ルール8の通り、エージェントによる端末画面の勝手なタップ操作やテストは厳禁。動作確認はユーザー本人が行います。ユーザーから明示的な指示（「画面キャプチャを撮って」等）があった場合のみ、補助的に画面取得を行ってください。**
- **専門スキルの追加 (`android skills list` / `android skills add <skill-id>`)**:
  - パフォーマンス計測 (`android-profiler`)、アダプティブ UI (`adaptive`)、セキュリティ監査など、フェーズに応じて公式スキルを検索・導入して活用すること。

---

## 3. アーキテクチャ & コーディング規約

### レイヤー分離 (Clean Architecture + Unidirectional Data Flow)
パッケージルート: `com.github.stm7128.hycohyco`

- **`data/`**: API 通信 (Ktor)、DMC セッション管理、DTO、DataStore/Room、Repository 実装
  - UI や Compose への依存を一切持たせないこと。
- **`domain/`**: ドメインモデル、Repository インターフェース、UseCase
  - 純粋な Kotlin コードのみとし、Android フレームワーク依存を最小限にすること。
  - 通常動画を主軸としつつ、将来的に「生放送」へ拡張できるよう、ストリームやコメント取得の抽象化を意識すること。
- **`player/`**: Media3 プレイヤー制御、Foreground Service (バックグラウンド再生・PiP)、CA（コメントアート）対応弾幕エンジン、ジェスチャー制御
- **`ui/`**: Jetpack Compose 画面、ViewModel (`StateFlow` + イベント)、デザインシステム (`designsystem/`)

### デュアル・デザインシステム（HyperOS × Material You）の掟
- 各画面（Home, Search, Player, Settings 等）の Composable から、特定のテーマ専用コンポーネントを無秩序に直呼び出ししないこと。
- `ui/designsystem/` 配下にテーマ切り替え状態（`AppThemeMode.HYPER_OS` / `AppThemeMode.MATERIAL_YOU`）と共通 UI ラッパーを配置し、**設定切り替え時にアプリ全体の見た目が即座に・破綻なく切り替わる構造**を維持すること。

### コメント描画エンジン（職人 CA 完全再現）の規約
- 弾幕描画は 16:9 の固定仮想キャンバス座標系（例: 1920×1080 または 640×360）をベースにレイアウト計算を行い、実画面サイズへ等倍スケーリングすること。
- アスキーアート（AA）や複数行コメント（`ue`, `shita`, `big`, `small` 等）の行高・文字幅計測を厳密に行い、レイアウト崩れを防ぐこと。
- 描画ループ内（`onDraw`）でのオブジェクト生成（`Paint` や `Path` の毎回インスタンス化）を禁止し、60fps/120fps の滑らかな描画を維持すること。

### Kotlin & Compose スタイル規約
- **State Hoisting**: 画面状態の保持は ViewModel (`uiState: StateFlow`) に集約し、下位の Composable はステートレス（値とコールバックを受け取る形）を基本とすること。
- **マジックナンバーの排除**: アニメーション時間、タイムアウト秒数（DMC キープアライブ間隔など）、座標定数はすべて名前付き定数（`const val`）として定義すること。
- **エラーハンドリング**: 通信エラーや API 仕様変更時にクラッシュさせず、`Result<T>` またはシールドインターフェースを用いて UI へ適切にエラー状態を伝搬させること。

---

## 4. ドキュメント (`doc/`) 構成 & 引き継ぎ規約

他の LLM や別セッションのエージェントが**コードを1行も読まずに全体像と各機能の詳細を把握できるよう**、`doc/` ディレクトリ配下に以下の粒度でドキュメントを維持・更新してください。

```
doc/
 ├── 00_overview.md          # プロジェクト全体像・アーキテクチャ・ディレクトリ構成・進捗ステータス
 ├── NICO_API_REFERENCE.md   # ニコニコ動画 内部API 完全リファレンス & 知見チートシート (全エンドポイント・ヘッダー・注意点)
 ├── 01_theme_design.md      # デュアルテーマ (HyperOS / Material You) の状態管理・共通コンポーネント仕様
 ├── 02_nico_api_dmc.md      # ニコニコ API (watch / nvcomment / 検索) 通信仕様 & DMC セッション・キープアライブ仕様
 ├── 03_player_service.md    # Media3 プレイヤー・バックグラウンド再生・PiP・ミニプレイヤー状態遷移
 ├── 04_danmaku_engine.md    # CA 完全再現コメント描画エンジンの座標計算・フォント計測・NG フィルタ仕様
 └── ...                     # 機能追加に応じて分割作成
```

### ドキュメント記述の必須項目
各機能ドキュメントには、以下の要素を必ず含めてください：
1. **責務と概要**: このモジュールが何を解決するか
2. **関連ファイル一覧**: どの `.kt` ファイルがどの役割を担っているかのマッピング
3. **データフロー / 状態遷移**: Mermaid 図や箇条書きによる入出力・イベントの流れ
4. **注意点・ハマりどころ**: ニコニコ特有の仕様（例: 40秒ごとのハートビート、CAの行高計算比率など）やエッジケース

