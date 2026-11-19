package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode

/**
 * 投稿者からの「いいね！お礼メッセージ」ダイアログ
 *
 * @param thanksMessage 投稿者が設定したお礼メッセージ
 * @param onDismissRequest ダイアログを閉じるコールバック
 * @param ownerName 投稿者名
 */
@Composable
fun LikeThanksDialog(
    thanksMessage: String,
    onDismissRequest: () -> Unit,
    ownerName: String = "",
    modifier: Modifier = Modifier
) {
    val themeMode = LocalAppThemeMode.current
    val isHyperOs = themeMode == AppThemeMode.HYPER_OS
    val cornerRadius = if (isHyperOs) 20.dp else 16.dp

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius)),
            shape = RoundedCornerShape(cornerRadius),
            color = HycoTheme.colors.surfaceContainer,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ハートアイコンバッジ
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4081).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "いいね",
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "いいね！しました",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HycoTheme.colors.onSurface
                )

                if (ownerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$ownerName からのメッセージ",
                        fontSize = 12.sp,
                        color = HycoTheme.colors.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // お礼メッセージ本文カード
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = HycoTheme.colors.background.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = thanksMessage,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = HycoTheme.colors.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = "閉じる",
                            fontWeight = FontWeight.SemiBold,
                            color = HycoTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}
