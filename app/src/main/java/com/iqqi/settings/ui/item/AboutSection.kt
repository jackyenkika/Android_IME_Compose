package com.iqqi.settings.ui.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iqqi.ime.R
import com.iqqi.settings.ui.localFontFamily

@Composable
fun AboutSection() {

    val version = rememberAppVersion()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Icon(
            painter = painterResource(id = R.drawable.img_app_icon),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "KIKA Keyboard",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = localFontFamily.current
            )
        )

        Text(
            text = version,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = localFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "© 2026 KIKA Project",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = localFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "All rights reserved",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = localFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun rememberAppVersion(): String {

    val context = LocalContext.current

    return remember {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}