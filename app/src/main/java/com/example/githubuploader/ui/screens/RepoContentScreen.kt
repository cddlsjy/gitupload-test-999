package com.example.githubuploader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubuploader.GitHubUploaderApp
import com.example.githubuploader.data.model.RepoContentItem
import com.example.githubuploader.data.remote.GitHubRepository
import kotlinx.coroutines.launch

@Composable
fun RepoContentScreen(
    viewModel: RepoContentViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember { (context.applicationContext as GitHubUploaderApp).preferences }
    val scope = rememberCoroutineScope()
    
    var repoUrl by remember { mutableStateOf(preferences.repoUrl) }
    var contents by remember { mutableStateOf<List<RepoContentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("仓库内容", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("仓库地址") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val gitHubRepo = GitHubRepository()
                            val ownerRepo = gitHubRepo.parseRepoUrl(repoUrl)
                            
                            if (ownerRepo != null) {
                                val result = gitHubRepo.getRepoContents(
                                    ownerRepo.first,
                                    ownerRepo.second,
                                    "",
                                    preferences.branch
                                )
                                result.fold(
                                    onSuccess = { contents = it },
                                    onFailure = { errorMessage = it.message }
                                )
                            } else {
                                errorMessage = "无效的仓库地址"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("刷新")
                }
            }
        }

        errorMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (contents.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(contents) { item ->
                        RepositoryItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun RepositoryItem(item: RepoContentItem) {
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = {
            Text(
                if (item.type == "file") "大小: ${item.size ?: 0} 字节" else "目录"
            )
        }
    )
    HorizontalDivider()
}

class RepoContentViewModel : androidx.lifecycle.ViewModel()
