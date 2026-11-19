package com.github.stm7128.hycohyco.ui.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

private const val TAG = "NicoWebLogin"

/**
 * ニコニコ公式ログイン画面を表示し、user_session Cookie を自動/手動で検知・抽出するコンポーネント。
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun NicoWebLoginView(
    onSessionExtracted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var hasExtracted by remember { mutableStateOf(false) }

    fun checkAndExtractSession(currentUrl: String?): Boolean {
        if (hasExtracted) return true
        val cm = CookieManager.getInstance()
        val targets = listOf(
            "https://account.nicovideo.jp",
            "https://nicovideo.jp",
            "https://www.nicovideo.jp",
            "https://nvapi.nicovideo.jp",
            currentUrl ?: ""
        ).distinct().filter { it.isNotBlank() }

        for (target in targets) {
            val cookies = cm.getCookie(target) ?: continue
            val candidate = cookies.split(";")
                .map { it.trim() }
                .firstOrNull { it.startsWith("user_session=") }
                ?.substringAfter("user_session=")
                ?.trim()

            if (!candidate.isNullOrBlank() && candidate != "deleted") {
                Log.d(TAG, "user_session 発見 ($target): ${candidate.take(15)}...")
                hasExtracted = true
                onSessionExtracted("user_session=$candidate")
                // セッション抽出直後に WebView と Cookie を即座に破棄・消去 (AGENTS.md ルール3)
                webViewInstance?.apply {
                    stopLoading()
                    clearHistory()
                    loadUrl("about:blank")
                    removeAllViews()
                    destroy()
                }
                webViewInstance = null
                runCatching { CookieManager.getInstance().removeAllCookies(null) }
                return true
            }
        }
        return false
    }

    LaunchedEffect(Unit) {
        while (!hasExtracted) {
            delay(1000)
            checkAndExtractSession(webViewInstance?.url)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                stopLoading()
                clearHistory()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
            webViewInstance = null
            runCatching { CookieManager.getInstance().removeAllCookies(null) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val found = checkAndExtractSession(webViewInstance?.url)
                    if (!found) {
                        Log.d(TAG, "手動取得: まだ user_session がありません (url: ${webViewInstance?.url})")
                    }
                },
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ログイン完了 (Cookie取得)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { webViewInstance?.reload() },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "再読み込み")
            }
        }

        if (isLoading && progress < 1.0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        setOnTouchListener { v, event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                            }
                            false
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                isLoading = newProgress < 100
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                Log.d(TAG, "onPageStarted: $url")
                                checkAndExtractSession(url)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                Log.d(TAG, "onPageFinished: $url")
                                checkAndExtractSession(url)
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                Log.d(TAG, "doUpdateVisitedHistory: $url")
                                checkAndExtractSession(url)
                            }
                        }

                        loadUrl("https://account.nicovideo.jp/login?site=niconico")
                        webViewInstance = this
                    }
                }
            )
        }
    }
}
