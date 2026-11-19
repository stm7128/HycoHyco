package com.github.stm7128.hycohyco.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.data.backup.BackupManager
import com.github.stm7128.hycohyco.domain.util.DateTimeFormatters
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 設定画面のデータ管理・バックアップセクション
 */
@Composable
fun SettingsBackupSection(
    modifier: Modifier = Modifier,
    backupManager: BackupManager = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    // エクスポート用 SAF ランチャー
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val result = backupManager.exportBackup(context, uri)
                isProcessing = false
                result.fold(
                    onSuccess = { count ->
                        Toast.makeText(context, "バックアップを保存しました ($count 件)", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "エクスポート失敗: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    // インポート用 SAF ランチャー
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val result = backupManager.importBackup(context, uri)
                isProcessing = false
                result.fold(
                    onSuccess = { summary ->
                        Toast.makeText(
                            context,
                            "データを復元しました (プレイリスト:${summary.playlistCount}, 履歴:${summary.watchHistoryCount}, NG:${summary.ngWordCount})",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "復元失敗: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "データ管理・バックアップ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HycoCard {
            Column {
                HycoPreferenceRow(
                    title = "バックアップを書き出し (JSON)",
                    summary = "設定、NG、プレイリスト、視聴履歴をファイルに保存します",
                    onClick = {
                        val dateStr = DateTimeFormatters.formatCurrentForBackup()
                        exportLauncher.launch("hycohyco_backup_$dateStr.json")
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "エクスポート",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                HycoPreferenceRow(
                    title = "バックアップから復元 (JSON)",
                    summary = "以前書き出した JSON ファイルから全データを復元します",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "インポート",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}
