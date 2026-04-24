package com.example.githubuploader.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubuploader.GitHubUploaderApp
import com.example.githubuploader.data.model.Snippet

@Composable
fun SnippetsScreen(
    viewModel: SnippetsViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember { (context.applicationContext as GitHubUploaderApp).preferences }
    
    var snippets by remember { mutableStateOf(preferences.getSnippets()) }
    var selectedIndex by remember { mutableStateOf(0) }
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("文本片段", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (snippets.isNotEmpty()) {
                    OutlinedTextField(
                        value = snippets[selectedIndex].name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("已选片段") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        snippets.forEachIndexed { index, snippet ->
                            OutlinedButton(
                                onClick = { selectedIndex = index },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(snippet.name.take(5))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = snippets[selectedIndex].content,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("内容") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("snippet", snippets[selectedIndex].content)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                editName = snippets[selectedIndex].name
                                editContent = snippets[selectedIndex].content
                                isEditing = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("编辑")
                        }
                    }
                }
            }
        }
        
        if (isEditing) {
            EditSnippetDialog(
                name = editName,
                content = editContent,
                onNameChange = { editName = it },
                onContentChange = { editContent = it },
                onSave = {
                    val updatedSnippets = snippets.toMutableList()
                    if (selectedIndex in updatedSnippets.indices) {
                        updatedSnippets[selectedIndex] = Snippet(editName, editContent)
                        snippets = updatedSnippets
                        preferences.saveSnippets(updatedSnippets)
                    }
                    isEditing = false
                },
                onDismiss = { isEditing = false }
            )
        }
    }
}

@Composable
fun EditSnippetDialog(
    name: String,
    content: String,
    onNameChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑片段") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("内容") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

class SnippetsViewModel : androidx.lifecycle.ViewModel()
