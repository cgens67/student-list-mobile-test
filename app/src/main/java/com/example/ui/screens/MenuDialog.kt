package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.MainViewModel

@Composable
fun MenuDialog(
    viewModel: MainViewModel,
    onDismissRequest: () -> Unit
) {
    val curLang by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(viewModel = viewModel, onDismissRequest = { showAboutDialog = false })
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp, topStart = 0.dp, bottomStart = 0.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(LanguageText.getText(curLang, "menu").uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                
                MenuRow(
                    icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    text = LanguageText.getText(curLang, "tDark"),
                    onClick = { viewModel.toggleDarkMode() }
                )
                
                MenuRow(
                    icon = Icons.Default.Language,
                    text = LanguageText.getText(curLang, "tLang"),
                    onClick = { viewModel.toggleLanguage() }
                )
                
                MenuRow(
                    icon = Icons.Default.ViewCompact,
                    text = LanguageText.getText(curLang, "tCompact"),
                    onClick = { viewModel.toggleCompactMode() }
                )
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))

                MenuRow(
                    icon = Icons.Default.Info,
                    text = LanguageText.getText(curLang, "about"),
                    onClick = { showAboutDialog = true }
                )

                MenuRow(
                    icon = Icons.Default.ColorLens,
                    text = "Doodle Overlay",
                    onClick = {
                        viewModel.toggleDoodleMode()
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
fun MenuRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AboutDialog(viewModel: MainViewModel, onDismissRequest: () -> Unit) {
    val curLang by viewModel.currentLanguage.collectAsState()
    val aboutData = viewModel.globalSettings.value.aboutData

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(aboutData.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(aboutData.version, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text(aboutData.desc, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(aboutData.design, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(aboutData.footer, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth()) {
                    Text(LanguageText.getText(curLang, "btnClose"))
                }
            }
        }
    }
}
