package com.easycomic.accessibility.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 无障碍设置界面
 */
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "无障碍设置",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "此页面用于配置无障碍功能设置",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccessibilitySettingsScreenPreview() {
    MaterialTheme {
        AccessibilitySettingsScreen(
            onNavigateBack = {}
        )
    }
}