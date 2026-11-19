package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.TimeZone

/**
 * 過去ログコメント表示中であることを通知し、最新コメントへ戻す操作を提供するバナー
 */
@Composable
fun PastLogActiveBanner(
    dateLabel: String,
    onResetToLatest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFF9800).copy(alpha = 0.14f),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$dateLabel 時点の過去ログを表示中",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "最新に戻す",
                color = Color(0xFF0095FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onResetToLatest() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * 過去ログコメント取得のための日付選択ダイアログ
 *
 * 選択された日付の日本時間 (Asia/Tokyo) 23:59:59 の UNIX 秒を算出し、
 * nvcomment API の `additionals.when` パラメータ用に返す。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastCommentDatePickerDialog(
    onDateSelected: (timestampSec: Long, dateLabel: String) -> Unit,
    onDismiss: () -> Unit
) {
    // 日本時間における「今日の終了時点（UTC基準の日付ミリ秒上限）」を計算
    val todayUtcEndMillis = remember {
        val jstNow = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                jstNow.get(Calendar.YEAR),
                jstNow.get(Calendar.MONTH),
                jstNow.get(Calendar.DAY_OF_MONTH),
                23,
                59,
                59
            )
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // ニコニコ動画サービス開始年（2007年）〜今日までを選択可能とする
    val selectableDates = remember(todayUtcEndMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayUtcEndMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                val maxYear = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo")).get(Calendar.YEAR)
                return year in 2007..maxYear
            }
        }
    }

    val currentYear = remember {
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo")).get(Calendar.YEAR)
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayUtcEndMillis - 86_400_000L, // 初期値は前日
        yearRange = 2007..currentYear,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val (timestampSec, label) = convertUtcDateMillisToJstEndOfDay(selectedMillis)
                        onDateSelected(timestampSec, label)
                    } else {
                        onDismiss()
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("この日のコメントを表示")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "過去ログの日付を選択",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

/**
 * Material3 DatePicker が返す UTC 日付ミリ秒を、日本時間 (Asia/Tokyo) の
 * 当日 23:59:59 の UNIX 秒と表示ラベル ("yyyy/MM/dd") に変換する。
 */
private fun convertUtcDateMillisToJstEndOfDay(utcMillis: Long): Pair<Long, String> {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val year = utcCal.get(Calendar.YEAR)
    val month = utcCal.get(Calendar.MONTH)
    val day = utcCal.get(Calendar.DAY_OF_MONTH)

    val jstCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo")).apply {
        clear()
        set(year, month, day, 23, 59, 59)
        set(Calendar.MILLISECOND, 0)
    }
    val timestampSec = jstCal.timeInMillis / 1000L
    val dateLabel = "%04d/%02d/%02d".format(year, month + 1, day)
    return timestampSec to dateLabel
}
