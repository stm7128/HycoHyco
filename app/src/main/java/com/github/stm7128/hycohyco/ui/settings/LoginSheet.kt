package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.UserSession
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import kotlinx.coroutines.launch

private enum class LoginMode {
    WEB_LOGIN,
    COOKIE_IMPORT
}

/**
 * ニコニコログイン用ボトムシート。
 * 公式ログイン画面 (WebView) による自動セッション取得、および Cookie 直接貼り付けに対応。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSheet(
    authRepository: AuthRepository,
    onDismissRequest: () -> Unit,
    onLoginSuccess: (UserSession) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(LoginMode.WEB_LOGIN) }
    var cookieInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processSessionCookie(cookieValue: String) {
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = authRepository.importSessionCookie(cookieValue)
            isLoading = false
            result.onSuccess { session ->
                onLoginSuccess(session)
                sheetState.hide()
                onDismissRequest()
            }.onFailure { e ->
                errorMessage = "ログインセッションの取得に失敗しました: ${e.localizedMessage}"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (mode == LoginMode.WEB_LOGIN) "ニコニコ公式ログイン" else "Cookie直接貼り付け",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (mode == LoginMode.WEB_LOGIN) {
                            "ログインが完了すると自動でセッションを保存して閉じます"
                        } else {
                            "ブラウザの user_session Cookie を入力してください"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる")
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                HycoCard(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "アカウント情報を取得中...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                when (mode) {
                    LoginMode.WEB_LOGIN -> {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            NicoWebLoginView(
                                onSessionExtracted = { sessionCookie ->
                                    processSessionCookie(sessionCookie)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = { errorMessage = null; mode = LoginMode.COOKIE_IMPORT }) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Cookie直接貼り付けでログイン", fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }

                    LoginMode.COOKIE_IMPORT -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = cookieInput,
                            onValueChange = { cookieInput = it },
                            label = { Text("user_session=...") },
                            placeholder = { Text("user_session_123456_abcdef...") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (cookieInput.isBlank()) {
                                    errorMessage = "Cookie を入力してください"
                                    return@Button
                                }
                                processSessionCookie(cookieInput)
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Cookie でログイン", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { errorMessage = null; mode = LoginMode.WEB_LOGIN },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Webログイン画面に戻る", fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
