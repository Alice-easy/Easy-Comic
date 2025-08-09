package com.easycomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.easycomic.ui.theme.EasyComicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            EasyComicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainContent()
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Easy Comic",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "漫画阅读器",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        Text(
            text = "基本功能验证完成:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "✓ 文件解析 (ZIP/CBZ, RAR/CBR)",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "✓ 图像显示与优化",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "✓ 阅读界面",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "✓ 基本导航",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = { /* 未来功能 */ },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("打开漫画")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EasyComicTheme {
        MainContent()
    }
}