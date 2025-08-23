package com.easycomic.error

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.easycomic.monitoring.CrashReportingManager
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

/**
 * 错误处理和用户反馈管理器
 * 
 * 提供统一的错误处理、用户友好的错误展示和反馈收集功能
 */

/**
 * 错误类型枚举
 */
enum class ErrorType(
    val displayName: String,
    val icon: ImageVector,
    val severity: ErrorSeverity
) {
    // 文件相关错误
    FILE_NOT_FOUND("文件未找到", Icons.Default.FilePresent, ErrorSeverity.MEDIUM),
    FILE_PERMISSION_DENIED("权限不足", Icons.Default.Lock, ErrorSeverity.HIGH),
    FILE_CORRUPTED("文件损坏", Icons.Default.BrokenImage, ErrorSeverity.MEDIUM),
    FILE_FORMAT_UNSUPPORTED("格式不支持", Icons.Default.ErrorOutline, ErrorSeverity.LOW),
    FILE_TOO_LARGE("文件过大", Icons.Default.Storage, ErrorSeverity.MEDIUM),
    
    // 网络相关错误
    NETWORK_UNAVAILABLE("网络不可用", Icons.Default.NetworkCheck, ErrorSeverity.MEDIUM),
    NETWORK_TIMEOUT("网络超时", Icons.Default.AccessTime, ErrorSeverity.MEDIUM),
    SERVER_ERROR("服务器错误", Icons.Default.Cloud, ErrorSeverity.HIGH),
    
    // 系统相关错误
    OUT_OF_MEMORY("内存不足", Icons.Default.Memory, ErrorSeverity.HIGH),
    STORAGE_FULL("存储空间不足", Icons.Default.Storage, ErrorSeverity.HIGH),
    SYSTEM_ERROR("系统错误", Icons.Default.Error, ErrorSeverity.CRITICAL),
    
    // 用户操作错误
    INVALID_INPUT("输入无效", Icons.Default.InputError, ErrorSeverity.LOW),
    OPERATION_CANCELLED("操作已取消", Icons.Default.Cancel, ErrorSeverity.LOW),
    
    // 未知错误
    UNKNOWN_ERROR("未知错误", Icons.Default.HelpOutline, ErrorSeverity.MEDIUM)
}

/**
 * 错误严重程度
 */
enum class ErrorSeverity(val level: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4)
}

/**
 * 错误信息数据类
 */
data class ErrorInfo(
    val type: ErrorType,
    val message: String,
    val technicalMessage: String? = null,
    val exception: Throwable? = null,
    val context: Map<String, String> = emptyMap(),
    val suggestions: List<String> = emptyList(),
    val canRetry: Boolean = false
)

/**
 * 错误处理管理器
 */
object ErrorHandler {
    
    /**
     * 处理错误并显示用户友好的信息
     */
    fun handleError(
        context: Context,
        errorInfo: ErrorInfo,
        onRetry: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        // 记录错误日志
        logError(errorInfo)
        
        // 根据错误严重程度决定处理方式
        when (errorInfo.type.severity) {
            ErrorSeverity.CRITICAL -> {
                // 关键错误：记录崩溃报告
                CrashReportingManager.logCrash(
                    message = errorInfo.message,
                    throwable = errorInfo.exception,
                    customData = mapOf(
                        "error_type" to errorInfo.type.name,
                        "context" to errorInfo.context.toString()
                    )\n                )\n            }\n            ErrorSeverity.HIGH -> {\n                // 高级错误：记录错误报告\n                CrashReportingManager.logError(\n                    message = errorInfo.message,\n                    throwable = errorInfo.exception,\n                    customData = errorInfo.context\n                )\n            }\n            else -> {\n                // 中低级错误：仅本地日志\n                Timber.w(errorInfo.exception, \"Error: ${errorInfo.message}\")\n            }\n        }\n    }\n    \n    /**\n     * 记录错误日志\n     */\n    private fun logError(errorInfo: ErrorInfo) {\n        val logMessage = buildString {\n            appendLine(\"Error Type: ${errorInfo.type.displayName}\")\n            appendLine(\"Message: ${errorInfo.message}\")\n            errorInfo.technicalMessage?.let {\n                appendLine(\"Technical: $it\")\n            }\n            if (errorInfo.context.isNotEmpty()) {\n                appendLine(\"Context: ${errorInfo.context}\")\n            }\n        }\n        \n        Timber.e(errorInfo.exception, logMessage)\n    }\n    \n    /**\n     * 从异常创建错误信息\n     */\n    fun createErrorInfo(exception: Throwable, context: Map<String, String> = emptyMap()): ErrorInfo {\n        val errorType = classifyException(exception)\n        val userMessage = getUserFriendlyMessage(errorType, exception)\n        val suggestions = getErrorSuggestions(errorType)\n        \n        return ErrorInfo(\n            type = errorType,\n            message = userMessage,\n            technicalMessage = exception.message,\n            exception = exception,\n            context = context,\n            suggestions = suggestions,\n            canRetry = canRetryError(errorType)\n        )\n    }\n    \n    /**\n     * 根据异常类型分类错误\n     */\n    private fun classifyException(exception: Throwable): ErrorType {\n        return when (exception) {\n            is java.io.FileNotFoundException -> ErrorType.FILE_NOT_FOUND\n            is SecurityException -> ErrorType.FILE_PERMISSION_DENIED\n            is OutOfMemoryError -> ErrorType.OUT_OF_MEMORY\n            is java.util.zip.ZipException -> ErrorType.FILE_CORRUPTED\n            is java.net.UnknownHostException -> ErrorType.NETWORK_UNAVAILABLE\n            is java.net.SocketTimeoutException -> ErrorType.NETWORK_TIMEOUT\n            is IllegalArgumentException -> ErrorType.INVALID_INPUT\n            else -> ErrorType.UNKNOWN_ERROR\n        }\n    }\n    \n    /**\n     * 获取用户友好的错误消息\n     */\n    private fun getUserFriendlyMessage(errorType: ErrorType, exception: Throwable): String {\n        return when (errorType) {\n            ErrorType.FILE_NOT_FOUND -> \"找不到指定的文件，文件可能已被移动或删除。\"\n            ErrorType.FILE_PERMISSION_DENIED -> \"没有访问文件的权限，请检查应用权限设置。\"\n            ErrorType.FILE_CORRUPTED -> \"文件已损坏，无法正常打开。\"\n            ErrorType.FILE_FORMAT_UNSUPPORTED -> \"不支持的文件格式，请使用ZIP或RAR格式。\"\n            ErrorType.FILE_TOO_LARGE -> \"文件过大，可能导致性能问题。\"\n            ErrorType.NETWORK_UNAVAILABLE -> \"网络连接不可用，请检查网络设置。\"\n            ErrorType.NETWORK_TIMEOUT -> \"网络请求超时，请稍后重试。\"\n            ErrorType.SERVER_ERROR -> \"服务器暂时不可用，请稍后重试。\"\n            ErrorType.OUT_OF_MEMORY -> \"设备内存不足，请关闭其他应用后重试。\"\n            ErrorType.STORAGE_FULL -> \"存储空间不足，请清理设备存储空间。\"\n            ErrorType.SYSTEM_ERROR -> \"系统发生错误，请重启应用。\"\n            ErrorType.INVALID_INPUT -> \"输入的信息不正确，请检查后重新输入。\"\n            ErrorType.OPERATION_CANCELLED -> \"操作已被取消。\"\n            ErrorType.UNKNOWN_ERROR -> \"发生了未知错误：${exception.message ?: \"无详细信息\"}\"\n        }\n    }\n    \n    /**\n     * 获取错误处理建议\n     */\n    private fun getErrorSuggestions(errorType: ErrorType): List<String> {\n        return when (errorType) {\n            ErrorType.FILE_NOT_FOUND -> listOf(\n                \"检查文件是否仍然存在\",\n                \"重新导入文件\",\n                \"联系技术支持\"\n            )\n            ErrorType.FILE_PERMISSION_DENIED -> listOf(\n                \"在设置中授予存储权限\",\n                \"重启应用后重试\",\n                \"将文件移动到可访问的位置\"\n            )\n            ErrorType.FILE_CORRUPTED -> listOf(\n                \"尝试重新下载文件\",\n                \"使用其他解压软件验证文件\",\n                \"联系文件提供方\"\n            )\n            ErrorType.OUT_OF_MEMORY -> listOf(\n                \"关闭其他应用\",\n                \"重启设备\",\n                \"降低图片质量设置\"\n            )\n            ErrorType.NETWORK_UNAVAILABLE -> listOf(\n                \"检查WiFi或移动网络连接\",\n                \"尝试切换网络\",\n                \"稍后重试\"\n            )\n            else -> listOf(\n                \"重试操作\",\n                \"重启应用\",\n                \"联系技术支持\"\n            )\n        }\n    }\n    \n    /**\n     * 判断错误是否可以重试\n     */\n    private fun canRetryError(errorType: ErrorType): Boolean {\n        return when (errorType) {\n            ErrorType.NETWORK_TIMEOUT,\n            ErrorType.NETWORK_UNAVAILABLE,\n            ErrorType.SERVER_ERROR,\n            ErrorType.OUT_OF_MEMORY -> true\n            else -> false\n        }\n    }\n}\n\n/**\n * 错误显示对话框\n */\n@Composable\nfun ErrorDialog(\n    errorInfo: ErrorInfo,\n    isVisible: Boolean,\n    onRetry: (() -> Unit)? = null,\n    onDismiss: () -> Unit,\n    onFeedback: (() -> Unit)? = null\n) {\n    if (isVisible) {\n        AlertDialog(\n            onDismissRequest = onDismiss,\n            icon = {\n                Icon(\n                    imageVector = errorInfo.type.icon,\n                    contentDescription = null,\n                    tint = when (errorInfo.type.severity) {\n                        ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.error\n                        ErrorSeverity.HIGH -> MaterialTheme.colorScheme.error\n                        ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary\n                        ErrorSeverity.LOW -> MaterialTheme.colorScheme.outline\n                    }\n                )\n            },\n            title = {\n                Text(\n                    text = errorInfo.type.displayName,\n                    style = MaterialTheme.typography.titleLarge,\n                    fontWeight = FontWeight.Bold\n                )\n            },\n            text = {\n                Column {\n                    Text(\n                        text = errorInfo.message,\n                        style = MaterialTheme.typography.bodyMedium\n                    )\n                    \n                    if (errorInfo.suggestions.isNotEmpty()) {\n                        Spacer(modifier = Modifier.height(16.dp))\n                        Text(\n                            text = \"建议解决方案：\",\n                            style = MaterialTheme.typography.titleSmall,\n                            fontWeight = FontWeight.Bold\n                        )\n                        \n                        errorInfo.suggestions.forEach { suggestion ->\n                            Text(\n                                text = \"• $suggestion\",\n                                style = MaterialTheme.typography.bodySmall,\n                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)\n                            )\n                        }\n                    }\n                }\n            },\n            confirmButton = {\n                Row {\n                    if (errorInfo.canRetry && onRetry != null) {\n                        TextButton(onClick = onRetry) {\n                            Text(\"重试\")\n                        }\n                    }\n                    \n                    if (onFeedback != null) {\n                        TextButton(onClick = onFeedback) {\n                            Text(\"反馈\")\n                        }\n                    }\n                    \n                    TextButton(onClick = onDismiss) {\n                        Text(\"确定\")\n                    }\n                }\n            }\n        )\n    }\n}\n\n/**\n * 用户反馈管理器\n */\nobject FeedbackManager {\n    \n    /**\n     * 反馈类型\n     */\n    enum class FeedbackType(val displayName: String) {\n        BUG_REPORT(\"错误报告\"),\n        FEATURE_REQUEST(\"功能建议\"),\n        PERFORMANCE_ISSUE(\"性能问题\"),\n        UI_UX_FEEDBACK(\"界面体验\"),\n        GENERAL_FEEDBACK(\"一般反馈\")\n    }\n    \n    /**\n     * 反馈数据类\n     */\n    data class FeedbackData(\n        val type: FeedbackType,\n        val title: String,\n        val description: String,\n        val userEmail: String = \"\",\n        val deviceInfo: String = \"\",\n        val appVersion: String = \"\",\n        val attachLogs: Boolean = false\n    )\n    \n    /**\n     * 提交反馈\n     */\n    fun submitFeedback(\n        context: Context,\n        feedbackData: FeedbackData\n    ) {\n        try {\n            // 构建反馈邮件内容\n            val emailContent = buildFeedbackEmail(feedbackData)\n            \n            // 创建邮件Intent\n            val emailIntent = Intent(Intent.ACTION_SEND).apply {\n                type = \"text/plain\"\n                putExtra(Intent.EXTRA_EMAIL, arrayOf(\"easy@ea.cloudns.ch\"))\n                putExtra(Intent.EXTRA_SUBJECT, \"[Easy Comic] ${feedbackData.type.displayName}: ${feedbackData.title}\")\n                putExtra(Intent.EXTRA_TEXT, emailContent)\n            }\n            \n            // 启动邮件客户端\n            val chooser = Intent.createChooser(emailIntent, \"选择邮件应用\")\n            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)\n            context.startActivity(chooser)\n            \n        } catch (e: Exception) {\n            Timber.e(e, \"Failed to submit feedback\")\n            \n            // 记录反馈提交失败\n            CrashReportingManager.logError(\n                message = \"Feedback submission failed\",\n                throwable = e\n            )\n        }\n    }\n    \n    /**\n     * 构建反馈邮件内容\n     */\n    private fun buildFeedbackEmail(feedbackData: FeedbackData): String {\n        return buildString {\n            appendLine(\"反馈类型：${feedbackData.type.displayName}\")\n            appendLine(\"标题：${feedbackData.title}\")\n            appendLine()\n            appendLine(\"详细描述：\")\n            appendLine(feedbackData.description)\n            appendLine()\n            \n            if (feedbackData.userEmail.isNotEmpty()) {\n                appendLine(\"联系邮箱：${feedbackData.userEmail}\")\n                appendLine()\n            }\n            \n            appendLine(\"技术信息：\")\n            appendLine(\"应用版本：${feedbackData.appVersion}\")\n            appendLine(\"设备信息：${feedbackData.deviceInfo}\")\n            appendLine(\"提交时间：${java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\", java.util.Locale.getDefault()).format(java.util.Date())}\")\n            \n            if (feedbackData.attachLogs) {\n                appendLine()\n                appendLine(\"请在邮件中附加应用日志文件（如有）\")\n            }\n        }\n    }\n    \n    /**\n     * 获取设备信息\n     */\n    fun getDeviceInfo(): String {\n        return buildString {\n            appendLine(\"设备型号：${android.os.Build.MODEL}\")\n            appendLine(\"制造商：${android.os.Build.MANUFACTURER}\")\n            appendLine(\"Android版本：${android.os.Build.VERSION.RELEASE}\")\n            appendLine(\"API级别：${android.os.Build.VERSION.SDK_INT}\")\n            appendLine(\"架构：${android.os.Build.SUPPORTED_ABIS.joinToString(\", \")}\")\n        }\n    }\n}\n\n/**\n * 反馈提交界面\n */\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun FeedbackScreen(\n    onSubmitted: () -> Unit,\n    onCancel: () -> Unit\n) {\n    val context = LocalContext.current\n    \n    var feedbackType by remember { mutableStateOf(FeedbackManager.FeedbackType.GENERAL_FEEDBACK) }\n    var title by remember { mutableStateOf(\"\") }\n    var description by remember { mutableStateOf(\"\") }\n    var userEmail by remember { mutableStateOf(\"\") }\n    var attachLogs by remember { mutableStateOf(false) }\n    var isSubmitting by remember { mutableStateOf(false) }\n    \n    Scaffold(\n        topBar = {\n            TopAppBar(\n                title = { Text(\"反馈与建议\") },\n                navigationIcon = {\n                    IconButton(onClick = onCancel) {\n                        Icon(Icons.Default.Close, contentDescription = \"关闭\")\n                    }\n                },\n                actions = {\n                    TextButton(\n                        onClick = {\n                            if (title.isNotBlank() && description.isNotBlank()) {\n                                isSubmitting = true\n                                val feedbackData = FeedbackManager.FeedbackData(\n                                    type = feedbackType,\n                                    title = title,\n                                    description = description,\n                                    userEmail = userEmail,\n                                    deviceInfo = FeedbackManager.getDeviceInfo(),\n                                    appVersion = \"v0.6.0-alpha\", // 从BuildConfig获取\n                                    attachLogs = attachLogs\n                                )\n                                \n                                FeedbackManager.submitFeedback(context, feedbackData)\n                                isSubmitting = false\n                                onSubmitted()\n                            }\n                        },\n                        enabled = title.isNotBlank() && description.isNotBlank() && !isSubmitting\n                    ) {\n                        if (isSubmitting) {\n                            CircularProgressIndicator(modifier = Modifier.size(16.dp))\n                        } else {\n                            Text(\"提交\")\n                        }\n                    }\n                }\n            )\n        }\n    ) { paddingValues ->\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(paddingValues)\n                .verticalScroll(rememberScrollState())\n                .padding(16.dp),\n            verticalArrangement = Arrangement.spacedBy(16.dp)\n        ) {\n            // 反馈类型选择\n            Text(\n                text = \"反馈类型\",\n                style = MaterialTheme.typography.titleMedium,\n                fontWeight = FontWeight.Bold\n            )\n            \n            FeedbackManager.FeedbackType.values().forEach { type ->\n                Row(\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    RadioButton(\n                        selected = feedbackType == type,\n                        onClick = { feedbackType = type }\n                    )\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Text(type.displayName)\n                }\n            }\n            \n            // 标题输入\n            OutlinedTextField(\n                value = title,\n                onValueChange = { title = it },\n                label = { Text(\"标题 *\") },\n                modifier = Modifier.fillMaxWidth(),\n                singleLine = true\n            )\n            \n            // 描述输入\n            OutlinedTextField(\n                value = description,\n                onValueChange = { description = it },\n                label = { Text(\"详细描述 *\") },\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .height(120.dp),\n                maxLines = 5\n            )\n            \n            // 邮箱输入\n            OutlinedTextField(\n                value = userEmail,\n                onValueChange = { userEmail = it },\n                label = { Text(\"联系邮箱（可选）\") },\n                modifier = Modifier.fillMaxWidth(),\n                singleLine = true\n            )\n            \n            // 附加日志选项\n            Row(\n                verticalAlignment = Alignment.CenterVertically\n            ) {\n                Checkbox(\n                    checked = attachLogs,\n                    onCheckedChange = { attachLogs = it }\n                )\n                Spacer(modifier = Modifier.width(8.dp))\n                Text(\"附加应用日志（帮助诊断问题）\")\n            }\n            \n            // 说明文字\n            Card(\n                modifier = Modifier.fillMaxWidth(),\n                colors = CardDefaults.cardColors(\n                    containerColor = MaterialTheme.colorScheme.surfaceVariant\n                )\n            ) {\n                Column(\n                    modifier = Modifier.padding(16.dp)\n                ) {\n                    Text(\n                        text = \"隐私说明\",\n                        style = MaterialTheme.typography.titleSmall,\n                        fontWeight = FontWeight.Bold\n                    )\n                    Spacer(modifier = Modifier.height(8.dp))\n                    Text(\n                        text = \"我们重视您的隐私。提交的反馈仅用于改进应用，不会用于其他目的。您可以选择是否提供联系方式。\",\n                        style = MaterialTheme.typography.bodySmall\n                    )\n                }\n            }\n        }\n    }\n}\n\n@Preview(showBackground = true)\n@Composable\nfun ErrorDialogPreview() {\n    val sampleError = ErrorInfo(\n        type = ErrorType.FILE_NOT_FOUND,\n        message = \"找不到指定的文件，文件可能已被移动或删除。\",\n        suggestions = listOf(\"检查文件是否仍然存在\", \"重新导入文件\", \"联系技术支持\"),\n        canRetry = true\n    )\n    \n    MaterialTheme {\n        ErrorDialog(\n            errorInfo = sampleError,\n            isVisible = true,\n            onRetry = {},\n            onDismiss = {},\n            onFeedback = {}\n        )\n    }\n}"