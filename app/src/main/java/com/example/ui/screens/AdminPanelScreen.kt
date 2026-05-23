package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.MainViewModel
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: MainViewModel) {
    val curLang by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val activeTab by viewModel.adminActiveTab.collectAsState()

    val tabs = listOf("students", "teachers", "settings", "audit", "feedback", "analytics")

    Dialog(
        onDismissRequest = { viewModel.setAdminPanelOpen(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(LanguageText.getText(curLang, "adminTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(LanguageText.getText(curLang, "adminSub"), style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setAdminPanelOpen(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Admin")
                        }
                    },
                    actions = {
                        Button(onClick = {
                            val settings = viewModel.globalSettings.value
                            viewModel.saveAdminDatabaseChanges(settings, {}, {})
                            viewModel.setAdminPanelOpen(false)
                        }, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Save Cloud")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Secondary Scrollable Tab Row
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (tabs.indexOf(activeTab) in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(activeTab)])
                            )
                        }
                    }
                ) {
                    tabs.forEach { tabKey ->
                        val prettyName = LanguageText.getText(curLang, "tab${tabKey.replaceFirstChar { it.titlecase() }}").ifEmpty { tabKey.replaceFirstChar { it.titlecase() } }
                        Tab(
                            selected = activeTab == tabKey,
                            onClick = { viewModel.setAdminActiveTab(tabKey) },
                            text = { Text(prettyName, fontWeight = if (activeTab == tabKey) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }

                // Content Based on Tab
                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    when (activeTab) {
                        "students" -> AdminStudentsTab(viewModel, curLang, isDark)
                        "audit" -> AdminAuditTab(viewModel, curLang, isDark)
                        "feedback" -> AdminFeedbackTab(viewModel, curLang, isDark)
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Work in Progress", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentsTab(viewModel: MainViewModel, curLang: String, isDark: Boolean) {
    val adminStudents by viewModel.adminStudents.collectAsState()
    var search by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(LanguageText.getText(curLang, "adminSearchStudent")) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { /* Add Student Dialogue */ },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        val filtered = adminStudents.filter { it.name.contains(search, true) || it.id.contains(search, true) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${s.id} | ${s.newClass} <- ${s.oldClass}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.queueAdminStudentChanges(s, isDelete = true) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditTab(viewModel: MainViewModel, curLang: String, isDark: Boolean) {
    val logs by viewModel.auditLogs.collectAsState()
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(logs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(log.user, fontWeight = FontWeight.Bold)
                    Text("Date: ${java.util.Date(log.timestamp)}", style = MaterialTheme.typography.labelSmall)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    log.changes.forEach { change ->
                        Text("- $change", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeedbackTab(viewModel: MainViewModel, curLang: String, isDark: Boolean) {
    val feedbacks by viewModel.feedbackList.collectAsState()
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(feedbacks, key = { it.id }) { fb ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fb.user, fontWeight = FontWeight.Bold)
                            Text("Date: ${java.util.Date(fb.timestamp)}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { viewModel.deleteFeedbackItem(fb.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(fb.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
