package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoQualityOption

/**
 * プレイヤー設定シート内の画質選択セクション
 */
@Composable
fun PlayerSettingsQualitySection(
    availableQualities: List<VideoQualityOption>,
    selectedQualityId: String?,
    onQualitySelected: (String) -> Unit
) {
    if (availableQualities.isEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = "画質",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        availableQualities.forEach { quality ->
            val isSelected = quality.id == selectedQualityId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQualitySelected(quality.id) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quality.label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF0095FF) else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "選択中",
                        tint = Color(0xFF0095FF)
                    )
                }
            }
        }
    }
}
