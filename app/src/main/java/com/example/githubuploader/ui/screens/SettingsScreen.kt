package com.example.githubuploader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubuploader.GitHubUploaderApp
import com.example.githubuploader.data.remote.TokenProvider

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember { (context.applicationContext as GitHubUploaderApp).preferences }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("显示", "基本", "构建", "排除")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> DisplaySettings(preferences)
                1 -> BasicSettings(preferences)
                2 -> BuildSettings(preferences)
                3 -> ExcludeSettings(preferences)
            }
        }
    }
}

@Composable
fun DisplaySettings(preferences: com.example.githubuploader.data.local.PreferencesManager) {
    var fontScale by remember { mutableStateOf(preferences.fontScale) }
    var dialogScale by remember { mutableStateOf(preferences.dialogScale) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("显示设置", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("字体缩放: ${String.format("%.1f", fontScale)}")
                Slider(
                    value = fontScale,
                    onValueChange = { fontScale = it },
                    valueRange = 0.8f..2.0f,
                    steps = 11
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("对话框缩放: ${String.format("%.1f", dialogScale)}")
                Slider(
                    value = dialogScale,
                    onValueChange = { dialogScale = it },
                    valueRange = 0.8f..2.0f,
                    steps = 11
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            fontScale = 1.0f
                            dialogScale = 1.0f
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("重置")
                    }
                    Button(
                        onClick = {
                            preferences.fontScale = fontScale
                            preferences.dialogScale = dialogScale
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun BasicSettings(preferences: com.example.githubuploader.data.local.PreferencesManager) {
    var token by remember { mutableStateOf(preferences.token) }
    var defaultBranch by remember { mutableStateOf(preferences.branch) }
    var uploadUnpack by remember { mutableStateOf(preferences.uploadUnpack) }
    var uploadBuild by remember { mutableStateOf(preferences.uploadBuild) }
    var newRepoDesc by remember { mutableStateOf(preferences.newRepoDesc) }
    var newRepoPrivate by remember { mutableStateOf(preferences.newRepoPrivate) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("基本设置", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("GitHub 令牌") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = defaultBranch,
                    onValueChange = { defaultBranch = it },
                    label = { Text("默认分支") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uploadUnpack,
                        onCheckedChange = { uploadUnpack = it }
                    )
                    Text("默认上传 unpack.yml")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uploadBuild,
                        onCheckedChange = { uploadBuild = it }
                    )
                    Text("默认上传 build.yml")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newRepoDesc,
                    onValueChange = { newRepoDesc = it },
                    label = { Text("默认新仓库描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = newRepoPrivate,
                        onCheckedChange = { newRepoPrivate = it }
                    )
                    Text("默认创建私有仓库")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        preferences.token = token
                        TokenProvider.setToken(token)
                        preferences.branch = defaultBranch
                        preferences.uploadUnpack = uploadUnpack
                        preferences.uploadBuild = uploadBuild
                        preferences.newRepoDesc = newRepoDesc
                        preferences.newRepoPrivate = newRepoPrivate
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存设置")
                }
            }
        }
    }
}

@Composable
fun BuildSettings(preferences: com.example.githubuploader.data.local.PreferencesManager) {
    var buildBranch by remember { mutableStateOf(preferences.buildBranch) }
    var javaVersion by remember { mutableStateOf(preferences.javaVersion) }
    var javaHome by remember { mutableStateOf(preferences.javaHome) }
    var gradleVersion by remember { mutableStateOf(preferences.gradleVersion) }
    var buildType by remember { mutableStateOf(preferences.buildType) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("构建配置", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = buildBranch,
                    onValueChange = { buildBranch = it },
                    label = { Text("构建分支") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = javaVersion,
                    onValueChange = { javaVersion = it },
                    label = { Text("Java 版本") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = javaHome,
                    onValueChange = { javaHome = it },
                    label = { Text("JAVA_HOME (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = gradleVersion,
                    onValueChange = { gradleVersion = it },
                    label = { Text("Gradle 版本") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = buildType,
                    onValueChange = { buildType = it },
                    label = { Text("构建类型 (debug/release)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        preferences.buildBranch = buildBranch
                        preferences.javaVersion = javaVersion
                        preferences.javaHome = javaHome
                        preferences.gradleVersion = gradleVersion
                        preferences.buildType = buildType
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存构建设置")
                }
            }
        }
    }
}

@Composable
fun ExcludeSettings(preferences: com.example.githubuploader.data.local.PreferencesManager) {
    var excludePatterns by remember { mutableStateOf(preferences.excludePatterns) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("打包排除规则", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("每行一个规则，支持通配符 (*, ?)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = excludePatterns,
                    onValueChange = { excludePatterns = it },
                    label = { Text("排除规则") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    maxLines = 20
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            excludePatterns = preferences.getDefaultExcludes()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("恢复默认")
                    }
                    Button(
                        onClick = {
                            preferences.excludePatterns = excludePatterns
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

class SettingsViewModel : androidx.lifecycle.ViewModel()
