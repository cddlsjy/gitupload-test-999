package com.example.githubuploader.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubuploader.GitHubUploaderApp
import com.example.githubuploader.data.local.PreferencesManager
import com.example.githubuploader.data.remote.GitHubRepository
import com.example.githubuploader.data.remote.TokenProvider
import com.example.githubuploader.domain.UploadManager
import com.example.githubuploader.domain.ZipPacker
import kotlinx.coroutines.launch

@Composable
fun UploadScreen(
    viewModel: UploadViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember { (context.applicationContext as GitHubUploaderApp).preferences }
    val scope = rememberCoroutineScope()
    
    var token by remember { mutableStateOf(preferences.token) }
    var isAuthenticated by remember { mutableStateOf(preferences.token.isNotEmpty()) }
    var repoUrl by remember { mutableStateOf(preferences.repoUrl) }
    var createNewRepo by remember { mutableStateOf(preferences.createNew) }
    var newRepoName by remember { mutableStateOf(preferences.newRepoName) }
    var newRepoDesc by remember { mutableStateOf(preferences.newRepoDesc) }
    var newRepoPrivate by remember { mutableStateOf(preferences.newRepoPrivate) }
    var uploadUnpack by remember { mutableStateOf(preferences.uploadUnpack) }
    var uploadBuild by remember { mutableStateOf(preferences.uploadBuild) }
    var ymlFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<Uri?>(null) }
    var selectedZip by remember { mutableStateOf<Uri?>(null) }
    var logs by remember { mutableStateOf<List<UploadManager.UploadLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val ymlPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        ymlFiles = ymlFiles + uris.filter { it.toString().endsWith(".yml") || it.toString().endsWith(".yaml") }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        selectedFolder = uri
    }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedZip = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isAuthenticated) {
            AuthSection(
                token = token,
                onTokenChange = { token = it },
                onAuthenticate = {
                    preferences.token = token
                    TokenProvider.setToken(token)
                    isAuthenticated = token.isNotEmpty()
                }
            )
        } else {
            RepoSection(
                repoUrl = repoUrl,
                createNewRepo = createNewRepo,
                newRepoName = newRepoName,
                newRepoDesc = newRepoDesc,
                newRepoPrivate = newRepoPrivate,
                onRepoUrlChange = { repoUrl = it },
                onCreateNewRepoChange = { createNewRepo = it },
                onNewRepoNameChange = { newRepoName = it },
                onNewRepoDescChange = { newRepoDesc = it },
                onNewRepoPrivateChange = { newRepoPrivate = it }
            )

            FileSection(
                ymlFiles = ymlFiles,
                selectedFolder = selectedFolder,
                selectedZip = selectedZip,
                uploadUnpack = uploadUnpack,
                uploadBuild = uploadBuild,
                onAddYmlFiles = { ymlPickerLauncher.launch(arrayOf("*/*")) },
                onSelectFolder = { folderPickerLauncher.launch(null) },
                onSelectZip = { zipPickerLauncher.launch(arrayOf("application/zip", "*/*")) },
                onRemoveYml = { uri -> ymlFiles = ymlFiles - uri },
                onClearFolder = { selectedFolder = null },
                onClearZip = { selectedZip = null },
                onUploadUnpackChange = { uploadUnpack = it },
                onUploadBuildChange = { uploadBuild = it }
            )

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val uploadManager = UploadManager(context, preferences)
                        
                        try {
                            val gitHubRepo = GitHubRepository()
                            val ownerRepo = gitHubRepo.parseRepoUrl(repoUrl)
                            
                            if (ownerRepo != null) {
                                var zipFile: java.io.File? = null
                                if (selectedFolder != null) {
                                    zipFile = ZipPacker.packFolder(
                                        context,
                                        selectedFolder!!,
                                        preferences.getExcludePatternList()
                                    )
                                }
                                
                                uploadManager.uploadFilesSequence(
                                    owner = ownerRepo.first,
                                    repo = ownerRepo.second,
                                    branch = preferences.branch,
                                    ymlFiles = ymlFiles,
                                    zipFile = zipFile,
                                    uploadUnpack = uploadUnpack,
                                    uploadBuild = uploadBuild,
                                    onLog = { log -> logs = logs + log }
                                )
                            }
                        } catch (e: Exception) {
                            logs = logs + UploadManager.UploadLog(
                                timestamp = "",
                                message = "错误: ${e.message}",
                                isError = true
                            )
                        }
                        
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && repoUrl.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("上传文件")
            }

            LogsSection(logs = logs, onClearLogs = { logs = emptyList() })
        }
    }
}

@Composable
fun AuthSection(
    token: String,
    onTokenChange: (String) -> Unit,
    onAuthenticate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GitHub 认证", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                label = { Text("个人访问令牌") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAuthenticate, modifier = Modifier.fillMaxWidth()) {
                Text("认证")
            }
        }
    }
}

@Composable
fun RepoSection(
    repoUrl: String,
    createNewRepo: Boolean,
    newRepoName: String,
    newRepoDesc: String,
    newRepoPrivate: Boolean,
    onRepoUrlChange: (String) -> Unit,
    onCreateNewRepoChange: (Boolean) -> Unit,
    onNewRepoNameChange: (String) -> Unit,
    onNewRepoDescChange: (String) -> Unit,
    onNewRepoPrivateChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("仓库设置", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCreateNewRepoChange(false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("使用现有")
                }
                OutlinedButton(
                    onClick = { onCreateNewRepoChange(true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("创建新仓库")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (createNewRepo) {
                OutlinedTextField(
                    value = newRepoName,
                    onValueChange = onNewRepoNameChange,
                    label = { Text("仓库名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newRepoDesc,
                    onValueChange = onNewRepoDescChange,
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = newRepoPrivate,
                        onCheckedChange = onNewRepoPrivateChange
                    )
                    Text("私有仓库")
                }
            } else {
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = onRepoUrlChange,
                    label = { Text("仓库地址") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://github.com/owner/repo") }
                )
            }
        }
    }
}

@Composable
fun FileSection(
    ymlFiles: List<Uri>,
    selectedFolder: Uri?,
    selectedZip: Uri?,
    uploadUnpack: Boolean,
    uploadBuild: Boolean,
    onAddYmlFiles: () -> Unit,
    onSelectFolder: () -> Unit,
    onSelectZip: () -> Unit,
    onRemoveYml: (Uri) -> Unit,
    onClearFolder: () -> Unit,
    onClearZip: () -> Unit,
    onUploadUnpackChange: (Boolean) -> Unit,
    onUploadBuildChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("选择上传文件", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onAddYmlFiles, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加YAML")
                }
                OutlinedButton(onClick = onSelectFolder, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("选择文件夹")
                }
                OutlinedButton(onClick = onSelectZip, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("选择ZIP")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = uploadUnpack, onCheckedChange = onUploadUnpackChange)
                Text("上传 unpack.yml")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = uploadBuild, onCheckedChange = onUploadBuildChange)
                Text("上传 build.yml")
            }
            
            if (ymlFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("已选择的YAML文件:")
                ymlFiles.forEach { uri ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(uri.lastPathSegment ?: "未知", modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveYml(uri) }) {
                            Icon(Icons.Default.Close, contentDescription = "移除")
                        }
                    }
                }
            }
            
            selectedFolder?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已选文件夹: ${it.lastPathSegment}", modifier = Modifier.weight(1f))
                    IconButton(onClick = onClearFolder) {
                        Icon(Icons.Default.Close, contentDescription = "移除")
                    }
                }
            }
            
            selectedZip?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已选ZIP: ${it.lastPathSegment}", modifier = Modifier.weight(1f))
                    IconButton(onClick = onClearZip) {
                        Icon(Icons.Default.Close, contentDescription = "移除")
                    }
                }
            }
        }
    }
}

@Composable
fun LogsSection(
    logs: List<UploadManager.UploadLog>,
    onClearLogs: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("上传日志", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClearLogs) {
                    Text("清除")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (logs.isEmpty()) {
                Text("暂无日志", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(logs) { log ->
                        Text(
                            text = if (log.timestamp.isNotEmpty()) "${log.timestamp}: ${log.message}" else log.message,
                            color = if (log.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

class UploadViewModel : androidx.lifecycle.ViewModel()
