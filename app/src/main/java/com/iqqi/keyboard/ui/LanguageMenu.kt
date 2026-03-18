package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqqi.keyboard.model.ImeLanguage

@Composable
fun LanguageMenu(
    languages: List<ImeLanguage>,
    current: ImeLanguage?,
    onSelect: (ImeLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val style = localKeyboardStyle.current

    Box(
        Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {

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

                item {
                    Text(
                        "Change Keyboard",
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.W600
                    )
                }

                items(languages) { lang ->

                    val isCurrent = lang.locale == current?.locale

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(
                                if (isCurrent) style.keyPressedColor
                                else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(lang) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (isCurrent) {
                            Text("✓", Modifier.padding(end = 12.dp))
                        } else {
                            Box(Modifier.width(20.dp))
                        }

                        Text(
                            text = lang.name.displayName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}