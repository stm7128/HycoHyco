package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField

private val NG_SCORE_LEVELS = listOf(
    -100 to "強 (-100)",
    -1000 to "中 (-1000)",
    -4800 to "弱 (-4800)",
    -100000 to "無効"
)

/**
 * メイン設定画面用のコメント NG フィルター管理セクション
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsNgFilterSection(
    ngConfig: CommentNgConfig,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors
    var wordInput by remember { mutableStateOf("") }
    var userIdInput by remember { mutableStateOf("") }

    val submitWord = {
        val trimmed = wordInput.trim()
        if (trimmed.isNotEmpty() && !ngConfig.ngWords.contains(trimmed)) {
            onUpdateNgConfig(ngConfig.copy(ngWords = ngConfig.ngWords + trimmed))
            wordInput = ""
        }
    }
    val submitUserId = {
        val trimmed = userIdInput.trim()
        if (trimmed.isNotEmpty() && !ngConfig.ngUserIds.contains(trimmed)) {
            onUpdateNgConfig(ngConfig.copy(ngUserIds = ngConfig.ngUserIds + trimmed))
            userIdInput = ""
        }
    }

    Column(modifier = modifier) {
        Text(
            text = "コメント NG フィルター",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        HycoCard {
            Column {
                // 1. 184 (匿名) コメント非表示
                HycoPreferenceRow(
                    title = "184 (匿名) コメントを非表示",
                    summary = "コマンド 184 付きの匿名コメントを一括で非表示にします",
                    trailing = {
                        HycoSwitch(
                            checked = ngConfig.hide184Comments,
                            onCheckedChange = { hide ->
                                onUpdateNgConfig(ngConfig.copy(hide184Comments = hide))
                            }
                        )
                    }
                )

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 2. NG 共有レベル（最小許容スコア閾値）
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "荒らし対策（NG共有レベル）",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "指定スコア未満の低評価コメントを自動非表示にします",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NG_SCORE_LEVELS.forEach { (threshold, label) ->
                            HycoFilterChip(
                                selected = ngConfig.minScoreThreshold == threshold,
                                onClick = { onUpdateNgConfig(ngConfig.copy(minScoreThreshold = threshold)) },
                                label = label
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 3. NG ワード管理
                NgTagEditorBlock(
                    title = "NG ワード (${ngConfig.ngWords.size}件)",
                    placeholder = "非表示にするワードを入力...",
                    emptyLabel = "登録されている NG ワードはありません",
                    inputValue = wordInput,
                    onInputValueChange = { wordInput = it },
                    onSubmit = submitWord,
                    tags = ngConfig.ngWords,
                    onRemoveTag = { target ->
                        onUpdateNgConfig(ngConfig.copy(ngWords = ngConfig.ngWords - target))
                    }
                )

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 4. NG ユーザー ID 管理
                NgTagEditorBlock(
                    title = "NG ユーザー ID (${ngConfig.ngUserIds.size}件)",
                    placeholder = "非表示にするユーザーIDを入力...",
                    emptyLabel = "登録されている NG ユーザーはありません（コメント長押しからも追加可能）",
                    inputValue = userIdInput,
                    onInputValueChange = { userIdInput = it },
                    onSubmit = submitUserId,
                    tags = ngConfig.ngUserIds,
                    onRemoveTag = { target ->
                        onUpdateNgConfig(ngConfig.copy(ngUserIds = ngConfig.ngUserIds - target))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NgTagEditorBlock(
    title: String,
    placeholder: String,
    emptyLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    tags: List<String>,
    onRemoveTag: (String) -> Unit,
) {
    val colors = HycoTheme.colors

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        HycoTextField(
            value = inputValue,
            onValueChange = onInputValueChange,
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailingIcon = {
                IconButton(
                    onClick = onSubmit,
                    enabled = inputValue.isNotBlank(),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "追加",
                        tint = if (inputValue.isNotBlank()) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (tags.isEmpty()) {
            Text(
                text = emptyLabel,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        modifier = Modifier.clickable { onRemoveTag(tag) }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                color = colors.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "$tag を削除",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
