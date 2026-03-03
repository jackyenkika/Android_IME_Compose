package com.iqqi.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iqqi.data.SettingsRepository
import com.iqqi.settings.CandidateHeight
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.SettingsViewModel
import com.iqqi.settings.ThemeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {

    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val viewModel = remember { SettingsViewModel(repository) }

    var showKeyboardHeightDialog by remember { mutableStateOf(false) }
    var showCandidateHeightDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val enableSound by viewModel.enableSound.collectAsState()
    val currentKeyboardHeight by viewModel.currentKeyboardHeight.collectAsState()
    val currentCandidateHeight by viewModel.currentCandidateHeight.collectAsState()

    val themeColor by viewModel.themeColor.collectAsState()

    KeyboardTheme(themeColor = themeColor) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Keyboard Settings") }
                )
            }
        ) { padding ->

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                item {
                    SettingsCategoryCard(title = "Sound") {

                        SettingSwitchItemModern(
                            title = "Key Sound",
                            summary = "Play sound when pressing keys",
                            checked = enableSound,
                            onCheckedChange = {
                                viewModel.toggleSound(it)
                            }
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    SettingsCategoryCard(title = "Layout") {

                        SettingListItemModern(
                            title = "Keyboard Height",
                            summary = "Adjust the overall keyboard size",
                            currentValue = "${(currentKeyboardHeight.scale * 100).toInt()}%",
                            onClick = { showKeyboardHeightDialog = true }
                        )
                        HorizontalDivider()

                        SettingListItemModern(
                            title = "Candidate Height",
                            summary = "Adjust the overall candidate size",
                            currentValue = "${(currentCandidateHeight.scale * 100).toInt()}%",
                            onClick = { showCandidateHeightDialog = true }
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    SettingsCategoryCard(title = "Appearance") {

                        SettingListItemModern(
                            title = "Theme Color",
                            summary = "Change primary accent color",
                            currentValue = themeColor.name,
                            onClick = { showThemeDialog = true }
                        )

                        HorizontalDivider()
                    }
                }
            }

            if (showKeyboardHeightDialog) {
                SettingsSelectionDialog(
                    title = "Keyboard Height",
                    options = KeyboardHeight.entries,
                    current = currentKeyboardHeight,
                    optionLabel = { "${(it.scale * 100).toInt()}%" },
                    onSelect = {
                        viewModel.setKeyboardHeight(it)
                        showKeyboardHeightDialog = false
                    },
                    onDismiss = { showKeyboardHeightDialog = false }
                )
            }

            if (showCandidateHeightDialog) {
                SettingsSelectionDialog(
                    title = "Candidate Height",
                    options = CandidateHeight.entries,
                    current = currentCandidateHeight,
                    optionLabel = { "${(it.scale * 100).toInt()}%" },
                    onSelect = {
                        viewModel.setCandidateHeight(it)
                        showCandidateHeightDialog = false
                    },
                    onDismiss = { showCandidateHeightDialog = false }
                )
            }

            if (showThemeDialog) {
                SettingsSelectionDialog(
                    title = "Theme Color",
                    options = ThemeColor.entries,
                    current = themeColor,
                    optionLabel = { it.name },
                    onSelect = {
                        viewModel.setThemeColor(it)
                        showThemeDialog = false
                    },
                    onDismiss = { showThemeDialog = false }
                )
            }
        }
    }
}