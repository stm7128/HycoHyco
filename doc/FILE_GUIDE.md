# HycoHyco 全ソースファイル解説リファレンス

本ドキュメントは、`app/src/main/java/com/github/stm7128/hycohyco/` 配下の全 Kotlin ソースファイル（244 ファイル）および主要なプロジェクト設定ファイルの役割をレイヤー・パッケージ別に整理したコードガイドです。

---

## 目次

1. [ルート・ビルド・設定ファイル](#1-ルートビルド設定ファイル)
2. [エントリーポイント・DI (`HycoHycoApp.kt`, `di/`, `util/`)](#2-エントリーポイントdi-util)
3. [データ層 (`data/`)](#3-データ層-data)
4. [ドメイン層 (`domain/`)](#4-ドメイン層-domain)
5. [プレイヤー・弾幕・ジェスチャーエンジン (`player/`)](#5-プレイヤー弾幕ジェスチャーエンジン-player)
6. [UI レイヤー (`ui/`)](#6-ui-レイヤー-ui)

---

## 1. ルート・ビルド・設定ファイル

| ファイル | 役割・概要 |
| :--- | :--- |
| `build.gradle.kts` | ルート Gradle ビルドスクリプト（プラグイン共通定義） |
| `settings.gradle.kts` | プロジェクト名および `:app` モジュール・リポジトリ設定 |
| `gradle.properties` | Gradle JVM ヒープ・並列実行・Build Cache・Configuration Cache・Kotlin インクリメンタルビルド設定 |
| `gradle/libs.versions.toml` | Kotlin・Compose BOM・Miuix・Media3・Ktor・Koin・Glance 等のバージョンカタログ |
| `app/build.gradle.kts` | アプリモジュール設定（`compileSdk=36` / `minSdk=26` / `targetSdk=36`、依存関係、IR 並列コンパイル `-Xbackend-threads=4`、META-INF 除外） |
| `app/src/main/AndroidManifest.xml` | 権限（通信・MediaPlayback FGS・通知）、`MainActivity`（Deep Link / PiP 設定）、`HycoPlaybackService`、`OfflineDownloadService`、`HycoPlayerWidgetReceiver` 定義 |
| `.github/workflows/android-ci.yml` | GitHub Actions CI（Temurin JDK 17 での Kotlin 300 行チェック & `:app:assembleDebug` 検証） |

---

## 2. エントリーポイント・DI (`di/`, `util/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `HycoHycoApp.kt` | `Application` クラス。アプリ起動時の Koin DI コンテナ初期化（`appModule`, `viewModelModule`）を行う |
| `di/AppModule.kt` | DataStore（設定・認証・NG）、HTTP クライアント、各 API クライアント、SQLite DB、Repository、`NicoPlaybackManager`、`OfflineDownloadManager`、`BackupManager` 等の Koin シングルトン定義 |
| `di/ViewModelModule.kt` | `MainViewModel`、`HomeViewModel`、`SearchViewModel`、`PlayerViewModel`、`ShortsViewModel`、`LibraryViewModel` の Koin `viewModel` 定義 |
| `util/NetworkHelper.kt` | `ConnectivityManager` を用いた Wi-Fi / 有線 LAN 接続判定ヘルパー（画質自動切替用） |

---

## 3. データ層 (`data/`)

### 3.1 API クライアント (`data/api/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `HycoHttpClientProvider.kt` | アプリ共通の単一 `OkHttpClient` および Ktor `HttpClient` インスタンスを一元提供（コネクションプール共有・JSON 設定） |
| `NicoApiCommon.kt` | ニコニコ各 API 共通定数（`NicoApiConstants`）・標準ヘッダー適用ヘルパー・共通 `Json` パーサー |
| `NicoAuthApiClient.kt` | ログイン中のアカウントプロフィール（`/v1/users/me`）取得・セッション検証クライアント |
| `NicoCloudApiClient.kt` | 公式クラウドライブラリ API（あとで見る・マイリスト一覧/詳細・フォロー新着タイムライン）取得・追加・削除クライアント |
| `NicoCloudHistoryApiClient.kt` | クラウド各種履歴（視聴履歴・いいね履歴・ニコられた/ニコった履歴）取得・削除クライアント |
| `NicoCommentApiClient.kt` | `nvcomment` API（`/v1/threads`）から最新コメントおよび日時指定の過去ログコメントを取得 |
| `NicoDicApiClient.kt` | ニコニコ大百科 API（`api.dic.nicovideo.jp`）照会クライアント（タグ「百」記事有無・要約補完） |
| `NicoInteractionApiClient.kt` | 双方向操作クライアント（いいね送信/解除、コメント投稿 `postKey` 取得＆投稿・削除、ニコる送信/解除、投稿者フォロー/解除） |
| `NicoRecommendApiClient.kt` | レコメンド API（`/v1/recommend`）から動画の関連動画・ホーム「おすすめ」フィードを取得 |
| `NicoSearchApiClient.kt` | スナップショット検索 API v2 による動画検索・タグ検索・`contentId` 指定の一括バッチメタデータ補完 |
| `NicoShortsApiClient.kt` | ニコニコショート専用フィード・連続視聴プレイリスト・ショート検索 API クライアント |
| `NicoTagApiClient.kt` | 動画タグ追加・削除 API クライアント（`/v2/videos/{watchId}/tags`）および編集キー期限切れ例外 `TagKeyExpiredException` |
| `NicoUserApiClient.kt` | ユーザープロフィール・投稿動画・公開マイリスト・シリーズ・フォロー/フォロワー・ユーザーキーワード検索クライアント |
| `NicoVideoApiClient.kt` | 動画基本情報 API（`/v1/videos?watchIds=`）クライアント（検索未反映の最新動画補完用） |
| `NicoWatchApiClient.kt` | Watch API（`v3` / `v3_guest`）および Domand HLS ストリームアクセス権（`access-rights/hls`）取得クライアント |

### 3.2 データモデル DTO (`data/model/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `CloudHistoryDto.kt` | クラウド視聴履歴・いいね履歴・ニコる履歴 API レスポンス DTO 群 |
| `DomandAccessRightDto.kt` | Domand `access-rights/hls` リクエスト／レスポンス DTO |
| `InteractionDtos.kt` | いいね・コメント投稿 `postKey`・フォロー・ニコる操作レスポンス DTO |
| `NicoAccountDtos.kt` | ユーザーセッション確認用 `/v1/users/me` レスポンス DTO |
| `NicoCloudDtos.kt` | 公式「あとで見る」「マイリスト」「フォロー新着」レスポンス DTO 群 |
| `NicoShortsDtos.kt` | ショート連続視聴プレイリスト・ショート検索 API レスポンス DTO |
| `NicoVideoDtos.kt` | `/v1/videos` 基本情報 API レスポンス DTO |
| `NvCommentDto.kt` | `nvcomment` `/v1/threads` リクエスト／レスポンス DTO |
| `RecommendDto.kt` | `/v1/recommend` レコメンド動画・推薦レシピ情報 DTO |
| `SnapshotSearchDto.kt` | スナップショット検索 API v2 ルート・メタ・検索結果アイテム DTO |
| `TagDtos.kt` | Watch API・タグ編集 API 共通のタグ DTO |
| `UserSearchDto.kt` | ユーザーキーワード検索（`api.cas.nicovideo.jp`）レスポンス DTO |
| `WatchResponseDto.kt` | Watch v3 / v3_guest API ルート DTO（動画詳細・ストリーム情報・投稿者・タグ・シリーズ等） |

### 3.3 ローカル保存・DB・バックアップ・オフライン DL (`data/local/`, `data/backup/`, `data/download/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `data/local/AuthPreferencesDataSource.kt` | `user_session` Cookie やユーザーID等のログインセッション情報を DataStore で永続化 |
| `data/local/NgPreferencesDataSource.kt` | コメント NG 設定（NG スコア閾値・NG ワード・正規表現・NG ユーザー ID・184 非表示）を DataStore で永続化 |
| `data/local/PlayerPreferencesDataSource.kt` | ジェスチャーモード・自動再生・レジューム再生・ダブルタップ秒数・画質・音声共存・自動 PiP・ショートタブ・弾幕設定を DataStore で永続化 |
| `data/local/ThemePreferencesDataSource.kt` | アプリテーマ（HyperOS / Material You）・ダークモード・Dynamic Color・AMOLED・初期ホームタブを DataStore で永続化 |
| `data/local/db/HycoDatabaseHelper.kt` | 視聴履歴・ローカルプレイリスト・検索履歴を管理する SQLite ヘルパー |
| `data/local/db/HycoDatabaseMigrations.kt` | SQLite テーブル定義およびスキーママイグレーション処理 |
| `data/local/db/HycoDatabaseBatchImporter.kt` | JSON バックアップ復元時の大量履歴／プレイリスト向けトランザクション一括挿入拡張 |
| `data/local/db/DownloadDao.kt` | オフラインダウンロード管理テーブル（`downloaded_videos`）用 SQLite DAO |
| `data/backup/BackupModels.kt` | 一括 JSON バックアップ用ルート DTO（`BackupRootDto`）および復元結果サマリ |
| `data/backup/BackupManager.kt` | 各種設定・ローカルプレイリスト・視聴履歴・検索履歴・NG 設定の JSON エクスポート／インポート実行マネージャー |
| `data/download/HlsPlaylistParser.kt` | HLS（`.m3u8`）プレイリストおよび AES-128 暗号化キー情報解析ユーティリティ |
| `data/download/HlsSegmentDecryptor.kt` | AES-128-CBC 暗号化 HLS セグメントの復号ユーティリティ |
| `data/download/LocalHlsPlaylistGenerator.kt` | 結合済みローカル fMP4 に対する BYTERANGE VOD 形式 `.m3u8` 生成ユーティリティ |
| `data/download/OfflineDownloader.kt` | HLS ストリーム（映像・音声結合＆復号）・コメント JSON・サムネイルのダウンロード本体 |
| `data/download/OfflineDownloadManager.kt` | ダウンロード予約キュー管理および `OfflineDownloadService` 起動制御マネージャー |
| `data/download/OfflineDownloadService.kt` | 進捗通知付きバックグラウンド保存を実行する Foreground Service |

### 3.4 Repository 実装 & マッパー (`data/repository/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `AuthRepositoryImpl.kt` | ログインセッション抽出・保存・プロフィール検証・ログアウト実装 |
| `CloudLibraryRepositoryImpl.kt` | 公式クラウドライブラリ（あとで見る・マイリスト・フォロー新着・投稿動画等）の取得・編集実装 |
| `DownloadRepositoryImpl.kt` | `DownloadDao` を通じた保存済みダウンロード管理およびストレージ削除実装 |
| `InteractionRepositoryImpl.kt` | いいね・コメント投稿/削除・ニコる・フォローおよびクラウド各種履歴操作の実装 |
| `LibraryRepositoryImpl.kt` | 端末視聴履歴・レジューム位置・ローカルプレイリスト・検索履歴と最新メタデータ補完の実装 |
| `ThemeRepositoryImpl.kt` | `ThemePreferencesDataSource` をラップするテーマ設定リポジトリ実装 |
| `VideoDetailMapper.kt` | Watch API DTO からドメインモデル `VideoDetail` へのマッピング処理 |
| `VideoRepositoryImpl.kt` | 動画詳細・HLS ストリーム・コメント・検索・ランキング・おすすめ・ショート取得実装 |
| `VideoTagMutationHelper.kt` | タグ追加/削除時の編集キー補完・期限切れ自動リトライ・Watch キャッシュ無効化ヘルパー |
| `WatchDataCache.kt` | 動画再生開始時の `getVideoDetail` / `getStreamSession` / `getVideoComments` 重複通信を防ぐ短時間インメモリキャッシュ |

---

## 4. ドメイン層 (`domain/`)

### 4.1 ドメインモデル (`domain/model/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `CloudHistoryModels.kt` | クラウド視聴履歴・いいね履歴・ニコる履歴・ページネーションコンテナモデル |
| `CommentCommandParser.kt` | ニコニココメントコマンド（位置・サイズ・色・フォント・184 等）のパーサー |
| `CommentItem.kt` | コメント位置・サイズ・フォント・色および個別コメントモデル `CommentItem` |
| `CommentNgRule.kt` | NG 判定設定 `CommentNgConfig` と高速フィルターエンジン `CommentNgFilter` |
| `DanmakuSettings.kt` | 弾幕表示領域モード（`DanmakuAreaMode`）およびカスタマイズ設定 |
| `DownloadStatus.kt` | オフライン保存ステータス Enum（待機・DL 中・完了・失敗等） |
| `DownloadType.kt` | 保存種別 Enum（動画＋弾幕 / 音声＋弾幕） |
| `DownloadedVideoItem.kt` | 保存済みダウンロード項目のメタデータ・容量モデル |
| `HomeMode.kt` | ホーム画面タブ種別（おすすめ / ランキング / フォロー新着 / ショート） |
| `HomeRecommendFeed.kt` | ホーム「おすすめ」タブの推薦動画（理由タグ付き）およびフィードモデル |
| `InteractionModels.kt` | いいね操作結果モデル `LikeResult` |
| `LibraryModels.kt` | 端末視聴履歴・ローカルプレイリスト・検索履歴ドメインモデル |
| `NicoAccountModels.kt` | ログインセッション情報モデル `UserSession` |
| `PlayerGestureMode.kt` | スライド音量/輝度ジェスチャー有効範囲 Enum（常時 / 全画面のみ / 無効） |
| `QualityPreference.kt` | デフォルト画質プリファレンス Enum（AUTO〜1080p） |
| `SearchFilter.kt` | 検索ターゲット種別（キーワード / タグ / ユーザー / ショート）および公式並び替え14項目 |
| `ThemeSettings.kt` | アプリデザイン種別（`HYPER_OS` / `MATERIAL_YOU`）・ダーク設定・Dynamic Color・AMOLED |
| `VideoDetail.kt` | 動画詳細ドメインモデル（タグ・投稿者・シリーズ・統計・コメント/いいね状態等） |
| `VideoSortOrder.kt` | ユーザー投稿動画・マイリスト・プレイリスト向け共通ソート順序定義＆ソート拡張 |
| `VideoSummary.kt` | 一覧・検索・ランキングカード共通の動画サマリモデル（表示フォーマッタ付き） |

### 4.2 Repository 契約・UseCase・ユーティリティ (`domain/repository/`, `domain/usecase/`, `domain/util/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `domain/repository/AuthRepository.kt` | アカウント認証・セッション監視契約インターフェース |
| `domain/repository/CloudLibraryRepository.kt` | 公式クラウドライブラリ・投稿者動画・フォロー/フォロワー取得契約 |
| `domain/repository/DownloadRepository.kt` | オフライン保存一覧・容量・削除契約 |
| `domain/repository/InteractionRepository.kt` | いいね・コメント・ニコる・フォロー・クラウド履歴契約 |
| `domain/repository/LibraryRepository.kt` | 端末視聴履歴・ローカルプレイリスト・検索履歴契約 |
| `domain/repository/ThemeRepository.kt` | テーマ・初期ホームタブ設定契約 |
| `domain/repository/VideoRepository.kt` | 動画詳細・ストリーム・コメント・過去ログ・検索・おすすめ・タグ操作契約 |
| `domain/usecase/OfflinePlaybackResolver.kt` | ローカル保存済み映像・音声・ローカル HLS・コメント JSON を解決して再生データ化する UseCase |
| `domain/util/DateTimeFormatters.kt` | スレッドセーフかつ高効率な日時フォーマットユーティリティ |
| `domain/util/NicoUrlParser.kt` | ニコニコ URL・ID・共有テキスト（動画 / ショート / ユーザー / マイリスト / シリーズ / `?from=` 秒数）の統一パーサー |
| `domain/util/SuspendCatching.kt` | コルーチン `CancellationException` を握りつぶさず再スローする安全な `suspendRunCatching` |

---

## 5. プレイヤー・弾幕・ジェスチャーエンジン (`player/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `NicoPlaybackManager.kt` | Media3 `ExoPlayer` ラッパー。HLS/ローカル再生準備・再生速度・リピート・AudioFocus 共存・進捗監視・ウィジェット更新を統合制御 |
| `SleepTimerManager.kt` | スリープタイマー（15/30/60 分・動画終了時停止）制御マネージャー |
| `model/PlaybackState.kt` | 再生状態を表す `PlaybackState` シールドインターフェース（`Idle`, `Buffering`, `Ready`, `Ended`, `Error`） |
| `danmaku/DanmakuAreaMode.kt` | 弾幕表示領域モードのプレイヤー層エイリアス |
| `danmaku/DanmakuConfig.kt` | 16:9 / 9:16 仮想キャンバス座標系・文字サイズ・スクロール時間定数 |
| `danmaku/DanmakuRenderItem.kt` | 事前計算済み弾幕アイテムおよび時刻・倍速に応じた仮想 X 座標計算ロジック |
| `danmaku/DanmakuLayoutEngine.kt` | コメント長・複数行・CA 計測に基づく衝突回避レーン割り当てエンジン |
| `danmaku/DanmakuCanvas.kt` | 事前確保 `Paint`/`Typeface` による 120fps ゼロアロケーション弾幕 Canvas レンダラー（黒文字白縁取り対応） |
| `gesture/PlayerGestureState.kt` | ジェスチャー操作中のオーバーレイ HUD 状態定義 |
| `gesture/PlayerGestureBox.kt` | タップ・ダブルタップシーク・長押し 2.0x 倍速・左右上下ドラッグ輝度/音量調整・下スワイプ最小化検知コンテナ |
| `gesture/PlayerGestureHud.kt` | 輝度/音量/シークの中央 HUD および長押し 2.0x の画面上部コンパクトピル HUD |
| `service/HycoPlaybackService.kt` | Media3 `MediaSessionService`。バックグラウンド再生・メディア通知・ウィジェット連携を行う Foreground Service |
| `service/HycoMediaNotificationProvider.kt` | カスタムメディア通知および Xiaomi HyperOS 3 スーパー島（`miui.focus.param.media`）連携プロバイダー |

---

## 6. UI レイヤー (`ui/`)

### 6.1 アクティビティ・メインシェル・共通 (`ui/`, `ui/main/`, `ui/common/`, `ui/setup/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `ui/MainActivity.kt` | アプリの `ComponentActivity`。Deep Link / `ACTION_SEND` 処理、PiP 状態通知、Edge-to-Edge 制御を行う |
| `ui/MainViewModel.kt` | `ThemeRepository`・`PlayerPreferencesDataSource` と連携したアプリ全体のテーマ・ジェスチャー・ショートタブ設定管理 |
| `ui/common/HycoUrlShareSheet.kt` | 動画・ショート・ユーザー・マイリスト・シリーズのネイティブ共有シート＆クリップボードコピー機能（`?from=` / `nicovideo.gay` 置換対応） |
| `ui/setup/ThemeSetupScreen.kt` | 初回起動時の HyperOS / Material You テーマ選択画面 |
| `ui/main/MainTab.kt` | ボトムナビゲーションタブ定義（ホーム / 検索 / ショート / ライブラリ / 設定） |
| `ui/main/MainShellTopBar.kt` | 各タブに対応したヘッダーバー（ホームはアイコン＋ロゴ表示） |
| `ui/main/MainShellScreen.kt` | メイン画面骨格。画面切替・全画面制御・Deep Link 遷移・タブ内サブナビゲーションを統括 |
| `ui/main/MainShellPlayerOverlays.kt` | 展開プレイヤーシートとドラッグ移動可能ミニプレイヤーのオーバーレイホスト |
| `ui/main/MainShellAuxiliaryOverlays.kt` | ショート全画面オーバーレイと単体ユーザー/マイリスト/シリーズ詳細シートのホスト |

### 6.2 デュアル・デザインシステム (`ui/designsystem/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `HycoColorScheme.kt` | HyperOS / Material You 共通のセマンティックカラースキーム定義 `HycoColorScheme` |
| `HycoTheme.kt` | `MiuixTheme` と `MaterialTheme` をシームレスに統合・切替するルートテーマラッパー |
| `components/HycoAsyncImage.kt` | LRU メモリキャッシュ＆重複リクエスト排除を備えたサムネイル画像ローダー |
| `components/HycoBounceModifier.kt` | タップ押下時の物理スプリングバウンス `Modifier.hycoBounceClick` |
| `components/HycoCard.kt` | HyperOS（スクワークル）と Material You（M3 カード）を自動切替するカード |
| `components/HycoControls.kt` | 共通ボタン・スイッチ・スライダー等のコントロール群 |
| `components/HycoInputs.kt` | 共通テキスト入力フィールド・検索バー |
| `components/HycoNavigation.kt` | 共通 TopAppBar・アイコンペア定義 `HycoNavIcon` |
| `components/HycoPullToRefresh.kt` | `MiuixPullToRefresh` と M3 `PullToRefreshBox` を統合した Pull-to-Refresh コンテナ |
| `components/HycoScaffold.kt` | 共通 Scaffold ラッパー |
| `components/VideoItemCard.kt` | 動画一覧・検索・ランキング・履歴・プレイリスト共通の動画カード（進捗バー・4大指標対応） |
| `components/VideoSortSelector.kt` | 動画一覧向けソートバーおよびソート選択ボトムシート |
| `components/VideoTagChipGroup.kt` | 動画・ショート共通の折り返しタグチップ一覧（ロック鍵・大百科「百」バッジ・追加ボタン） |
| `components/VideoTagDialogs.kt` | タグ長押しメニュー・大百科要約プレビュー・タグ追加/削除ダイアログ |

### 6.3 ホーム画面 (`ui/home/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `HomeViewModel.kt` | おすすめ・ランキング（期間切替）・フォロー新着（カーソル追加読込）・ショートフィードの状態管理 |
| `HomeScreen.kt` | ホーム画面タブ切替・ランキング表示＆各フィードコンテナ |
| `HomeRecommendFeedView.kt` | パーソナライズ「おすすめ」フィード（おすすめタグ抽出・理由タグ付きカード） |
| `HomeFollowFeedView.kt` | フォロー中投稿者の新着動画フィード＆共通ヘッダー更新ボタン `HomeRefreshButton` |
| `HomeShortsFeedView.kt` | 9:16 縦型 2 列グリッドのショート動画フィード |

### 6.4 検索画面 (`ui/search/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `SearchViewModel.kt` | 動画・タグ・ユーザー・ショート検索、ページネーション（無限スクロール）、検索履歴＆ピン留め管理 |
| `SearchScreen.kt` | 検索バー・検索対象切替・ソート連携を備えた検索画面本体 |
| `SearchHistorySection.kt` | 検索履歴・お気に入りタグピン留め・人気タグショートカット表示 |
| `SearchResultsContent.kt` | 検索結果リスト（動画無限スクロール・ユーザー結果・Pull-to-Refresh） |
| `SearchSortSelector.kt` | スナップショット検索 API 準拠 全14項目の並び替えボタン＆ボトムシート |
| `DirectVideoJumpCard.kt` | 検索バーに入力された動画/ユーザー/マイリスト/シリーズ ID・URL への直接遷移カード |
| `UserSearchViews.kt` | ユーザーキーワード検索の結果リスト＆アイテムカード |

### 6.5 ライブラリ・クラウド履歴・オフライン (`ui/library/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `LibraryUiState.kt` | ライブラリタブ種別・履歴サブタブ（端末/視聴/いいね/ニコられた/ニコった）・マイリストサブタブ定義 |
| `LibraryViewModel.kt` | 端末履歴・ローカルプレイリスト・ダウンロード管理と基本状態管理 |
| `LibraryCloudHistoryOperations.kt` | `LibraryViewModel` のクラウド各種履歴（取得・ページネーション・削除・いいね解除・ニコる解除）拡張 |
| `LibraryCloudMylistOperations.kt` | `LibraryViewModel` の公式「あとで見る」「マイリスト」取得・並び替え・削除拡張 |
| `LibraryScreen.kt` | ライブラリ画面本体（サブタブ切替・プレイリスト作成ダイアログ） |
| `LibraryTabContainers.kt` | 履歴統合コンテナおよびマイリスト統合コンテナ |
| `WatchHistoryViews.kt` | 端末視聴履歴一覧（進捗率・経過時間表示・個別/全削除） |
| `PlaylistViews.kt` | ローカルプレイリスト一覧・新規作成・削除ビュー |
| `PlaylistDetailView.kt` | ローカルプレイリスト詳細（全曲連続再生・シャッフル再生・ソート・動画削除） |
| `DownloadViews.kt` | オフラインダウンロード一覧画面（総使用容量表示・全削除・リスト表示） |
| `DownloadItemCard.kt` | 個別ダウンロード項目の進捗バー・ステータス・再生・キャンセル/削除カード |
| `CloudHistoryPagingComponents.kt` | クラウド履歴共通のローディング/エラー/空状態スキャフォールド＆末尾自動追加読込 |
| `CloudHistoryViews.kt` | クラウド視聴履歴（再生位置レジューム付き）・いいね履歴（お礼メッセージ確認・解除）ビュー |
| `CloudNicoruHistoryViews.kt` | ニコられた履歴・ニコった履歴の統合一覧ビュー |
| `CloudNicoruHistoryCard.kt` | ニコられた/ニコった 1件分のコメントカード（時間ジャンプ・注目バッジ・ニコる解除） |
| `CloudLibraryViews.kt` | 公式「あとで見る」および公式マイリスト一覧ビュー |
| `CloudMylistDetailView.kt` | 公式マイリスト詳細ビュー（ソート・連続再生・アイテム削除） |
| `MylistDetailSheet.kt` | Deep Link や概要欄リンクから単体マイリストを直接表示するボトムシート |

### 6.6 通常・全画面・ミニプレイヤー (`ui/player/`)

#### 状態管理・ViewModel・操作拡張
| ファイル | 役割・概要 |
| :--- | :--- |
| `PlayerUiState.kt` | 展開/ミニ状態・動画詳細・弾幕・NG・画質/速度・キュー・いいね/フォロー・タイマー・レジューム設定等のプレイヤー全体 UI 状態 |
| `PlayerViewModel.kt` | 動画読み込み・Media3 再生・コメント取得・画質切替・リトライ・レジューム保存を司る中核 ViewModel |
| `PlayerPlaybackControls.kt` | 再生キュー操作・ジェスチャー・ダブルタップ秒数・弾幕/NG・音声共存・自動 PiP・レジューム ON/OFF 拡張関数群 |
| `PlayerInteractionOperations.kt` | いいね送信/解除・お礼メッセージ・フォロー・コメント投稿/削除操作拡張 |
| `PlayerNicoruOperations.kt` | コメントへのニコる送信/解除操作拡張 |
| `PlayerPastLogOperations.kt` | 指定日時の過去ログコメント取得・最新コメント復帰操作拡張 |
| `PlayerPlaylistOperations.kt` | ローカルプレイリスト・公式マイリスト・あとで見るへの追加操作拡張 |
| `PlayerSeriesOperations.kt` | シリーズ次枠自動再生・5秒カウントダウン制御拡張 |
| `PlayerTagOperations.kt` | 動画タグ追加・削除および大百科バッジ非同期補完拡張 |
| `PlayerOfflinePlaybackHelper.kt` | オフライン動画再生時の擬似 `VideoDetail`・弾幕構築・レジューム復元ヘルパー |
| `PlayerPlaylistHelper.kt` | プレイリスト追加ロジックヘルパー |
| `PlayerQualityResolver.kt` | Wi-Fi / モバイル通信環境に応じた初期画質プリファレンス解決ヘルパー |
| `PlayerRecommendOperations.kt` | 関連動画（レコメンド）非同期取得ヘルパー |
| `PlayerResumeHelper.kt` | 視聴位置保存（`saveCurrentProgress`）および自動レジューム/明示位置シーク判定ヘルパー（`restoreResumePosition`） |

#### プレイヤー画面・サーフェス・全画面・ミニプレイヤー UI
| ファイル | 役割・概要 |
| :--- | :--- |
| `PlayerSheetContent.kt` | 展開・全画面・PiP 時のプレイヤー全体ホスト |
| `PlayerSinglePaneLayout.kt` | 縦画面スマートフォン向けの 1 ペイン縦スクロールレイアウト |
| `PlayerTwoPaneLayout.kt` | 画面幅/向きに応じた 2 ペイン / 1 ペイン分岐（`PlayerResponsiveLayout`）および横画面 2 ペインレイアウト |
| `FullscreenPlayerContainer.kt` | 全画面再生時の映像縮小×右サイドパネル展開コンテナおよび `BackHandler` 制御 |
| `FullscreenSidePanel.kt` | 全画面右側のダークサイドパネル本体（「関連動画 / コメント」切替・いいね/コメ投稿クイック操作） |
| `FullscreenRecommendList.kt` | 全画面サイドパネル内「関連動画」タブの大型 16:9 サムネイル動画リスト |
| `FloatingMiniPlayer.kt` | 画面内をドラッグ移動できる YouTube スタイル 16:9 リアルタイム映像＋弾幕フローティング小窓 |
| `VideoPlayerSurface.kt` | Media3 `PlayerView`・120fps `DanmakuCanvas`・`PlayerGestureBox`・`keepScreenOn` を統合した再生サーフェス |
| `VideoPlayerControlsOverlay.kt` | 再生/一時停止・シークバー・設定/PiP/弾幕/自動再生・全画面切替・サイドパネル開閉オーバーレイ |
| `HycoVideoSeekBar.kt` | 弾幕ヒートマップ波形重ね描画に対応したスマート動画シークバー |
| `InteractiveMiniBottomSeekBar.kt` | コントロール非表示時も直接タップ/ドラッグ可能な画面下端 2.dp シークバー |
| `NextVideoCountdownOverlay.kt` | シリーズ次枠自動再生カウントダウンオーバーレイ |
| `PlayerFeedbackOverlay.kt` | 共有シートおよび操作フィードバック通知バナーオーバーレイ |
| `PlayerPredictiveBack.kt` | Android 14+ 予測型「戻る」スワイプ縮小アニメーション状態管理 |
| `PipHelper.kt` | 動画アスペクト比に応じた Picture-in-Picture モード遷移ヘルパー |

#### 動画詳細・コメント・設定・共有ボトムシート群
| ファイル | 役割・概要 |
| :--- | :--- |
| `VideoInfoSection.kt` | 動画タイトル・再生/コメ/いいね統計・アクションボタン・投稿者カード・タグ群・概要欄 |
| `VideoOwnerCard.kt` | 投稿者アイコン・名前・ID・フォローボタンカード |
| `DescriptionLinkParser.kt` | 概要欄 HTML 装飾解析とインラインタップ（動画ID・タイムスタンプ・ユーザー・マイリスト・シリーズ・外部 URL）生成 |
| `VideoChapterBar.kt` | 概要欄タイムスタンプからチャプターチップ一覧を抽出・表示するバー |
| `SeriesSection.kt` | 動画所属シリーズ名および前後動画ナビゲーションカード |
| `PlayerBottomTabs.kt` | 視聴画面下部の「コメント」／「関連動画」タブ切替バー |
| `RecommendVideoSection.kt` | 関連動画一覧セクション |
| `CommentListSection.kt` | 時間自動追従スクロール・弾幕/NG 設定展開・過去ログ対応コメント一覧セクション |
| `CommentListHeader.kt` | コメント件数・追従トグル・過去ログ日付選択・設定開閉ヘッダー |
| `CommentRowItem.kt` | ニコるヒートマップ背景・ニコるボタン・長押し NG 登録対応のコメント行アイテム |
| `DanmakuHeatmapHelper.kt` | コメント密度を 60 バケットに集計・平滑化するヒートマップ計算ヘルパー |
| `DanmakuSettingsPanel.kt` | プレイヤー内で展開する弾幕カスタマイズ＆簡易 NG 設定パネル |
| `CommentInputSheet.kt` | 184・色・位置・サイズコマンド選択付きコメント投稿ボトムシート |
| `CommentNgSheet.kt` | コメント長押し時の NG ユーザー/NG ワード登録シート |
| `CommentDeleteDialog.kt` | 自コメント削除確認ダイアログ |
| `PastCommentDatePickerDialog.kt` | 過去ログ日時選択ダイアログおよび過去ログ表示中バナー |
| `PlayerDialogsAndSheets.kt` | プレイヤー上に重なる全シート（設定/プレイリスト追加/投稿者詳細/お礼/コメ投稿）の統合ホスト |
| `PlayerSettingsSheet.kt` | 再生設定ボトムシート本体（再生速度・タイマー・各種オプション・オフライン保存・画質選択） |
| `PlayerSettingsTimerSection.kt` | スリープタイマー・音声共存・ホーム時自動 PiP トグルセクション |
| `PlayerSettingsPlaybackOptionsSection.kt` | リピート・自動再生・レジューム再生・ジェスチャー・ダブルタップ秒数・`nicovideo.gay` セクション |
| `PlayerSettingsDownloadSection.kt` | 動画＋弾幕 / 音声のみオフライン保存トリガーセクション |
| `PlayerSettingsQualitySection.kt` | 画質一覧選択セクション |
| `AddToPlaylistSheet.kt` | ローカルプレイリスト・公式マイリスト・あとで見るへの保存統合シート |
| `LikeThanksDialog.kt` | 投稿者「いいね！」お礼メッセージ表示ダイアログ |
| `PlayerShareSheet.kt` | 動画/URL/再生位置付き共有シート（`nicovideo.gay` 置換対応） |

### 6.7 ニコニコショート (`ui/shorts/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `ShortsUiState.kt` | ショートプリフェッチキャッシュおよび `VerticalPager` 連続再生画面の状態モデル |
| `ShortsViewModel.kt` | ショート動画の連続再生制御・速度/リピート管理・ExoPlayer 協調制御 ViewModel |
| `ShortsQueueOperations.kt` | ショート詳細/コメント取得・前後プリフェッチ・無限キュー延長・履歴同期拡張 |
| `ShortsInteractionOperations.kt` | ショートにおけるいいね・フォロー・コメント・ニコる・プレイリスト・オフライン保存操作拡張 |
| `ShortsTagOperations.kt` | ショート詳細シートにおけるタグ追加・削除・大百科バッジ補完拡張 |
| `ShortsPagerScreen.kt` | 全画面縦スワイプ `VerticalPager` ショート視聴画面本体 |
| `ShortPageContent.kt` | ページ単位の独立 `TextureView` `PlayerView`・縦型弾幕・`keepScreenOn` 表示コンポーザブル |
| `ShortsOverlayControls.kt` | ショート画面上のタイトル・再生情報・本編動画誘導・極細シークバーオーバーレイ |
| `ShortsControlComponents.kt` | ショート上部バー（閉じる/弾幕切替）および右側アクションボタン群 |
| `ShortsDetailSheet.kt` | ショート動画タイトル全文・統計・概要欄・タグ・保存カードを表示する詳細シート |
| `ShortDetailDownloadCard.kt` | ショート詳細シート内のオフライン保存操作カード |
| `ShortsCommentSheet.kt` | ショート用コメント一覧・投稿トリガーボトムシート |
| `ShortsDialogsAndSheets.kt` | ショート画面内の詳細/コメント/NG/共有/プレイリスト追加シート統合ホスト |

### 6.8 ユーザー・シリーズ・設定・ウィジェット (`ui/user/`, `ui/series/`, `ui/settings/`, `ui/widget/`)

| ファイル | 役割・概要 |
| :--- | :--- |
| `ui/user/UserDetailState.kt` | 投稿者/ユーザー詳細シートの状態・タブ別ページネーションホルダー |
| `ui/user/UserDetailSheet.kt` | ユーザー詳細ボトムシート（投稿動画/シリーズ/マイリスト/フォロー中/フォロワー） |
| `ui/user/UserHeaderSection.kt` | ユーザーアイコン・ニックネーム・説明・フォロー・URL 共有ヘッダー |
| `ui/user/UserTabViews.kt` | ユーザー投稿動画（ソート対応）・公開マイリスト・シリーズ一覧タブ |
| `ui/user/UserRelationshipTab.kt` | フォロー中・フォロワー一覧タブ（無限スクロール対応） |
| `ui/user/UserItemDetailView.kt` | ユーザー詳細内の個別シリーズ／マイリスト動画ドリルダウンビュー |
| `ui/series/SeriesDetailSheet.kt` | Deep Link・検索・概要欄から単体シリーズを直接展開するボトムシート |
| `ui/settings/SettingsScreen.kt` | メイン設定画面本体（各設定セクション・画質選択・ログイン/プロフィールシート連携） |
| `ui/settings/NicoAccountCard.kt` | ログイン状態表示・ログイン/ログアウト/マイプロフィール遷移カード |
| `ui/settings/LoginSheet.kt` | WebView 自動認証および `user_session` 手動入力を切り替えるログインシート |
| `ui/settings/NicoWebLoginView.kt` | 公式ログイン画面用 WebView（Cookie 抽出後即破棄・2FA/reCAPTCHA 対応） |
| `ui/settings/SettingsThemeSection.kt` | デザインシステム・ダークモード・Dynamic Color・AMOLED 設定セクション |
| `ui/settings/SettingsGeneralSection.kt` | 初期ホームタブ・ショートタブ・ジェスチャー・スキップ秒数・自動再生・レジューム再生・自動 PiP・音声共存・共有設定セクション |
| `ui/settings/SettingsDanmakuSection.kt` | 弾幕表示 ON/OFF・不透明度・文字サイズ・速度・表示領域カスタマイズセクション |
| `ui/settings/SettingsNgFilterSection.kt` | NG 共有レベル（閾値）・184 非表示・NG ワード/ユーザー ID/正規表現管理セクション |
| `ui/settings/SettingsBackupSection.kt` | 全データの JSON バックアップ書き出し／読み込み復元セクション |
| `ui/settings/SettingsAppInfoSection.kt` | アプリバージョン・システム情報セクション |
| `ui/settings/QualitySelectorSheet.kt` | Wi-Fi / モバイル通信別デフォルト画質選択ボトムシート |
| `ui/widget/HycoPlayerWidgetKeys.kt` | ホーム画面ウィジェット用 DataStore キーおよび操作インテント定数 |
| `ui/widget/HycoPlayerWidget.kt` | Jetpack Compose Glance 製ホーム画面再生コントローラーウィジェット |
| `ui/widget/HycoPlayerWidgetReceiver.kt` | ウィジェットライフサイクルおよびウィジェット上操作（再生/停止/10秒スキップ）ハンドリング BroadcastReceiver |
| `ui/widget/HycoWidgetUpdater.kt` | 再生状態・動画情報の変化をウィジェットへ非同期配信・再描画するユーティリティ |
