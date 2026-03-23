package com.iqqi.settings.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iqqi.settings.BackgroundImage
import com.iqqi.settings.ui.localFontFamily

@Composable
fun SettingImageSelectionDialog(
    title: String,
    options: List<BackgroundImage>,
    current: BackgroundImage,
    onSelect: (BackgroundImage) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = localFontFamily.current
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { bg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(bg) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bg.resId != null) {
                            // 使用 Coil AsyncImage 非阻塞加載
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(bg.resId)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = bg.label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 12.dp)
                            )
                        }

                        val isSelected = bg == current
                        Text(
                            text = bg.label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    )
}