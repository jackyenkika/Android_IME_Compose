package com.iqqi.settings.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqqi.settings.ui.localFontFamily

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    current: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
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
                    // 限制最大高度，避免撐爆 Dialog
//                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    val isSelected = option == current

                    Text(
                        text = optionLabel(option),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                            }
                            .padding(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        style = if (isSelected)
                            MaterialTheme.typography.bodyLarge
                        else
                            MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected)
                            FontWeight.ExtraBold
                        else
                            FontWeight.Normal
                    )
                }
            }
        }
    )
}