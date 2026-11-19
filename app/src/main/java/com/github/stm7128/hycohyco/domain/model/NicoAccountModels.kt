package com.github.stm7128.hycohyco.domain.model

/**
 * ログイン中ユーザーのセッション情報
 *
 * @param userId ニコニコユーザーID
 * @param nickname ニックネーム
 * @param iconUrl アイコン画像URL
 * @param isPremium プレミアム会員フラグ
 * @param userSessionCookie "user_session=..." のCookie値 (またはCookie本体)
 */
data class UserSession(
    val userId: Long = 0L,
    val nickname: String = "",
    val iconUrl: String = "",
    val isPremium: Boolean = false,
    val userSessionCookie: String = ""
) {
    /** ログイン中か否か */
    val isLoggedIn: Boolean get() = userSessionCookie.isNotBlank() && userId > 0L

    /** リクエストヘッダ付与用の Cookie ヘッダ文字列 (例: "user_session=user_session_xxx") */
    val formattedCookieHeader: String
        get() = if (userSessionCookie.startsWith("user_session=")) {
            userSessionCookie
        } else if (userSessionCookie.isNotBlank()) {
            "user_session=$userSessionCookie"
        } else {
            ""
        }
}

/**
 * ニコニコユーザーのプロフィール情報
 */
data class NicoUserProfile(
    val userId: Long,
    val nickname: String,
    val description: String = "",
    val iconUrl: String = "",
    val isPremium: Boolean = false
)

/**
 * 認証プロセスの各ステップ結果
 */
sealed interface AuthStepResult {
    /** 認証成功 */
    data class Success(val session: UserSession) : AuthStepResult

    /** 二段階認証 (2FA / OTP) コードの入力が必要 */
    data class RequiresTwoFactor(
        val mfaUrl: String,
        val tempCookies: String,
        val message: String = "二段階認証コード（6桁）を入力してください"
    ) : AuthStepResult

    /** 認証失敗 (パスワード不一致、通信エラー等) */
    data class Failed(val errorMessage: String) : AuthStepResult
}

/**
 * 動画投稿者の詳細プロフィール
 */
data class OwnerProfile(
    val id: Long,
    val nickname: String,
    val iconUrl: String = "",
    val description: String = "",
    val followerCount: Long = 0L
)

/**
 * 公式マイリスト情報
 */
data class CloudMylist(
    val id: Long,
    val name: String,
    val description: String = "",
    val isPublic: Boolean = false,
    val itemsCount: Int = 0
)

/**
 * ユーザー作成シリーズ情報
 */
data class UserSeries(
    val id: Long,
    val title: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val itemsCount: Int = 0
)
