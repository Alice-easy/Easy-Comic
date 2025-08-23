package com.easycomic.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 用户引导系统
 * 
 * 为新用户提供应用功能介绍和使用指导
 */

/**
 * 引导页面数据类
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

/**
 * 用户引导管理器
 */
object OnboardingManager {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_FEATURE_INTRO_SHOWN = "feature_intro_shown"
    
    /**
     * 检查是否已完成引导
     */
    fun isOnboardingCompleted(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    /**
     * 标记引导为已完成
     */
    fun markOnboardingCompleted(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }
    
    /**
     * 检查功能介绍是否已显示
     */
    fun isFeatureIntroShown(context: android.content.Context, featureKey: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("${KEY_FEATURE_INTRO_SHOWN}_$featureKey", false)
    }
    
    /**
     * 标记功能介绍为已显示
     */
    fun markFeatureIntroShown(context: android.content.Context, featureKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("${KEY_FEATURE_INTRO_SHOWN}_$featureKey", true)
            .apply()
    }
    
    /**
     * 重置引导状态（用于测试或重新显示）
     */
    fun resetOnboarding(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

/**
 * 主要的用户引导屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit
) {
    val context = LocalContext.current
    
    // 引导页面数据
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "欢迎使用Easy Comic",
                description = "专业的漫画阅读器，为您提供最佳的阅读体验。支持ZIP、RAR等多种格式，让您轻松管理和阅读漫画收藏。",
                icon = Icons.Default.AutoStories,
                backgroundColor = Color(0xFF6200EE)
            ),
            OnboardingPage(
                title = "智能书架管理",
                description = "自动识别漫画文件，智能分类整理。支持搜索、筛选和收藏功能，让您的漫画收藏井井有条。",
                icon = Icons.Default.LibraryBooks,
                backgroundColor = Color(0xFF3700B3)
            ),
            OnboardingPage(
                title = "流畅阅读体验",
                description = "优化的阅读器支持多种阅读模式、智能缩放和手势导航。自动记忆阅读进度，随时继续您的阅读之旅。",
                icon = Icons.Default.TouchApp,
                backgroundColor = Color(0xFF6200EE)
            ),
            OnboardingPage(
                title = "个性化设置",
                description = "丰富的主题选择、字体调节和无障碍功能。支持多语言界面，让每个用户都能享受舒适的阅读体验。",
                icon = Icons.Default.Settings,
                backgroundColor = Color(0xFF03DAC6)
            )
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部跳过按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        OnboardingManager.markOnboardingCompleted(context)
                        onOnboardingCompleted()
                    }
                ) {
                    Text("跳过")
                }\n            }\n            \n            // 引导页面内容\n            HorizontalPager(\n                state = pagerState,\n                modifier = Modifier.weight(1f)\n            ) { page ->\n                OnboardingPageContent(\n                    page = pages[page],\n                    modifier = Modifier.fillMaxSize()\n                )\n            }\n            \n            // 底部导航区域\n            Column(\n                modifier = Modifier.padding(32.dp),\n                horizontalAlignment = Alignment.CenterHorizontally,\n                verticalArrangement = Arrangement.spacedBy(24.dp)\n            ) {\n                // 页面指示器\n                PageIndicator(\n                    pageCount = pages.size,\n                    currentPage = pagerState.currentPage\n                )\n                \n                // 导航按钮\n                Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    // 上一页按钮\n                    if (pagerState.currentPage > 0) {\n                        TextButton(\n                            onClick = {\n                                scope.launch {\n                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)\n                                }\n                            }\n                        ) {\n                            Text("上一页")\n                        }\n                    } else {\n                        Spacer(modifier = Modifier.width(80.dp))\n                    }\n                    \n                    // 下一页/完成按钮\n                    if (pagerState.currentPage < pages.size - 1) {\n                        Button(\n                            onClick = {\n                                scope.launch {\n                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)\n                                }\n                            },\n                            modifier = Modifier.width(120.dp)\n                        ) {\n                            Text("下一页")\n                        }\n                    } else {\n                        Button(\n                            onClick = {\n                                OnboardingManager.markOnboardingCompleted(context)\n                                onOnboardingCompleted()\n                            },\n                            modifier = Modifier.width(120.dp)\n                        ) {\n                            Text("开始使用")\n                        }\n                    }\n                }\n            }\n        }\n    }\n}\n\n/**\n * 引导页面内容组件\n */\n@Composable\nprivate fun OnboardingPageContent(\n    page: OnboardingPage,\n    modifier: Modifier = Modifier\n) {\n    Column(\n        modifier = modifier\n            .background(page.backgroundColor.copy(alpha = 0.1f))\n            .padding(32.dp),\n        horizontalAlignment = Alignment.CenterHorizontally,\n        verticalArrangement = Arrangement.Center\n    ) {\n        // 图标\n        Box(\n            modifier = Modifier\n                .size(120.dp)\n                .background(\n                    color = page.backgroundColor,\n                    shape = CircleShape\n                )\n                .padding(24.dp),\n            contentAlignment = Alignment.Center\n        ) {\n            Icon(\n                imageVector = page.icon,\n                contentDescription = null,\n                modifier = Modifier.size(72.dp),\n                tint = Color.White\n            )\n        }\n        \n        Spacer(modifier = Modifier.height(48.dp))\n        \n        // 标题\n        Text(\n            text = page.title,\n            style = MaterialTheme.typography.headlineMedium,\n            fontWeight = FontWeight.Bold,\n            textAlign = TextAlign.Center,\n            color = MaterialTheme.colorScheme.onSurface\n        )\n        \n        Spacer(modifier = Modifier.height(16.dp))\n        \n        // 描述\n        Text(\n            text = page.description,\n            style = MaterialTheme.typography.bodyLarge,\n            textAlign = TextAlign.Center,\n            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),\n            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5\n        )\n    }\n}\n\n/**\n * 页面指示器组件\n */\n@Composable\nprivate fun PageIndicator(\n    pageCount: Int,\n    currentPage: Int,\n    modifier: Modifier = Modifier\n) {\n    Row(\n        modifier = modifier,\n        horizontalArrangement = Arrangement.spacedBy(8.dp)\n    ) {\n        repeat(pageCount) { index ->\n            val isSelected = index == currentPage\n            Box(\n                modifier = Modifier\n                    .size(\n                        width = if (isSelected) 24.dp else 8.dp,\n                        height = 8.dp\n                    )\n                    .background(\n                        color = if (isSelected) {\n                            MaterialTheme.colorScheme.primary\n                        } else {\n                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)\n                        },\n                        shape = RoundedCornerShape(4.dp)\n                    )\n            )\n        }\n    }\n}\n\n/**\n * 功能介绍工具提示\n */\n@Composable\nfun FeatureTooltip(\n    text: String,\n    targetBounds: androidx.compose.ui.geometry.Rect,\n    onDismiss: () -> Unit,\n    modifier: Modifier = Modifier\n) {\n    // 这里可以实现一个浮动的工具提示组件\n    // 用于在用户首次使用某功能时显示说明\n    Card(\n        modifier = modifier\n            .padding(16.dp),\n        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),\n        shape = RoundedCornerShape(12.dp)\n    ) {\n        Column(\n            modifier = Modifier.padding(16.dp)\n        ) {\n            Text(\n                text = text,\n                style = MaterialTheme.typography.bodyMedium\n            )\n            \n            Spacer(modifier = Modifier.height(8.dp))\n            \n            Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.End\n            ) {\n                TextButton(onClick = onDismiss) {\n                    Text("知道了")\n                }\n            }\n        }\n    }\n}\n\n/**\n * 帮助系统组件\n */\n@Composable\nfun HelpDialog(\n    isVisible: Boolean,\n    onDismiss: () -> Unit,\n    helpContent: HelpContent\n) {\n    if (isVisible) {\n        AlertDialog(\n            onDismissRequest = onDismiss,\n            title = {\n                Text(helpContent.title)\n            },\n            text = {\n                Column {\n                    Text(helpContent.description)\n                    \n                    if (helpContent.steps.isNotEmpty()) {\n                        Spacer(modifier = Modifier.height(16.dp))\n                        Text(\n                            "操作步骤：",\n                            style = MaterialTheme.typography.titleSmall,\n                            fontWeight = FontWeight.Bold\n                        )\n                        \n                        helpContent.steps.forEachIndexed { index, step ->\n                            Text(\n                                "${index + 1}. $step",\n                                style = MaterialTheme.typography.bodyMedium,\n                                modifier = Modifier.padding(top = 4.dp)\n                            )\n                        }\n                    }\n                }\n            },\n            confirmButton = {\n                TextButton(onClick = onDismiss) {\n                    Text("知道了")\n                }\n            }\n        )\n    }\n}\n\n/**\n * 帮助内容数据类\n */\ndata class HelpContent(\n    val title: String,\n    val description: String,\n    val steps: List<String> = emptyList()\n)\n\n/**\n * 常用帮助内容\n */\nobject HelpContents {\n    val importComic = HelpContent(\n        title = "如何导入漫画",\n        description = "Easy Comic支持导入ZIP和RAR格式的漫画文件。",\n        steps = listOf(\n            "点击书架页面右上角的"+"按钮",\n            "选择"导入漫画文件"",\n            "在文件管理器中选择漫画文件",\n            "等待导入完成，漫画将出现在书架中"\n        )\n    )\n    \n    val readingControls = HelpContent(\n        title = "阅读器操作",\n        description = "了解阅读器的基本操作方法。",\n        steps = listOf(\n            "点击屏幕左侧/右侧翻页",\n            "双击屏幕缩放页面",\n            "双指捏合手势调整缩放",\n            "长按显示操作菜单",\n            "点击顶部显示/隐藏工具栏"\n        )\n    )\n    \n    val bookshelfManagement = HelpContent(\n        title = "书架管理",\n        description = "管理您的漫画收藏。",\n        steps = listOf(\n            "使用搜索框快速查找漫画",\n            "点击排序按钮改变排列方式",\n            "长按漫画项目进入选择模式",\n            "在选择模式下可批量操作",\n            "点击星标将漫画添加到收藏"\n        )\n    )\n    \n    val settings = HelpContent(\n        title = "个性化设置",\n        description = "根据您的喜好调整应用设置。",\n        steps = listOf(\n            "在设置中选择您喜欢的主题",\n            "调整字体大小和显示效果",\n            "设置阅读器的默认行为",\n            "配置无障碍功能",\n            "选择界面语言"\n        )\n    )\n}\n\n@Preview(showBackground = true)\n@Composable\nfun OnboardingScreenPreview() {\n    MaterialTheme {\n        OnboardingScreen(\n            onOnboardingCompleted = {}\n        )\n    }\n}"