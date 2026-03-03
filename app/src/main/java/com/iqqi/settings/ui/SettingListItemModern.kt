package com.iqqi.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingListItemModern(
    title: String,
    summary: String,
    currentValue: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Text(
                text = currentValue,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        },
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxSize()
    )
}