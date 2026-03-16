package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.iqqi.keyboard.model.ImeLanguage

@Composable
fun LanguageMenu(
    languages: List<ImeLanguage>,
    current: ImeLanguage?,
    onSelect: (ImeLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = { onDismiss() },
        properties = PopupProperties(focusable = true)
    ) {
        Box {
            // 半透明背景遮罩
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onDismiss() }
            )

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(languages) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(lang) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.name.name,
                                modifier = Modifier.weight(1f)
                            )
                            if (lang.locale == current?.locale) {
                                Text("✓")
                            }
                        }
                    }
                }
            }
        }
    }
}