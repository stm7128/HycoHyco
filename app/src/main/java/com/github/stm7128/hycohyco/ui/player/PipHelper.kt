package com.github.stm7128.hycohyco.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational

/**
 * Android Picture-in-Picture (PiP) モード遷移ヘルパー
 */
object PipHelper {

    /** 16:9 動画アスペクト比 */
    private val ASPECT_RATIO_16_9 = Rational(16, 9)

    /**
     * PiP モードへの遷移を試行する
     *
     * @param activity 遷移元 Activity
     * @param aspectRatioWidth アスペクト比の幅 (デフォルト: 16)
     * @param aspectRatioHeight アスペクト比の高さ (デフォルト: 9)
     * @return 遷移が受け入れられた場合 true
     */
    fun enterPip(
        activity: Activity,
        aspectRatioWidth: Int = 16,
        aspectRatioHeight: Int = 9
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity.isInPictureInPictureMode) return false
            val validRatio = if (aspectRatioWidth > 0 && aspectRatioHeight > 0) {
                val floatRatio = aspectRatioWidth.toFloat() / aspectRatioHeight.toFloat()
                if (floatRatio in 0.42f..2.38f) {
                    Rational(aspectRatioWidth, aspectRatioHeight)
                } else {
                    ASPECT_RATIO_16_9
                }
            } else {
                ASPECT_RATIO_16_9
            }
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(validRatio)
                .build()
            return runCatching { activity.enterPictureInPictureMode(params) }.getOrDefault(false)
        }
        return false
    }
}
