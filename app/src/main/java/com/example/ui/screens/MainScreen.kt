package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val curLang by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val isCompact by viewModel.isCompactMode.collectAsState()
    val students by viewModel.students.collectAsState()
    val filterSchOrigin by viewModel.filterSchOrigin.collectAsState()
    val filterPrevClass by viewModel.filterPrevClass.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val isFavOnly by viewModel.isFavOnlyMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val adminClassNotices by viewModel.adminClassNotices.collectAsState()
    val adminTeachers by viewModel.adminTeachers.collectAsState()

    var showStats by remember { mutableStateOf(false) }

    // Derive filtered students
    val activeFilteredStudents = remember(
        students, selectedClass, filterPrevClass, filterSchOrigin, isFavOnly, searchQuery, favorites
    ) {
        var list = students
        if (selectedClass != "All") {
            list = list.filter { it.newClass.contains(selectedClass, ignoreCase = true) }
        }
        if (filterPrevClass.isNotEmpty()) {
            list = list.filter { it.oldClass == filterPrevClass }
        }
        if (filterSchOrigin.isNotEmpty()) {
            list = list.filter { it.sch == filterSchOrigin }
        }
        if (isFavOnly) {
            list = list.filter { favorites.contains(it.id) }
        }
        if (searchQuery.isNotEmpty()) {
            val sq = searchQuery.lowercase()
            list = list.filter {
                it.name.lowercase().contains(sq) || it.cn.lowercase().contains(sq) || it.id.contains(sq)
            }
        }
        list
    }

    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val isAdminPanelOpen by viewModel.isAdminPanelOpen.collectAsState()
    var isMenuOpen by remember { mutableStateOf(false) }

    if (showStats) {
        StatsDialog(viewModel = viewModel, activeFilteredStudents = activeFilteredStudents) {
            showStats = false
        }
    }

    if (selectedStudentId != null) {
        StudentDetailDialog(viewModel = viewModel) {
            viewModel.selectStudentId(null)
        }
    }

    if (isAdminPanelOpen) {
        AdminPanelScreen(viewModel = viewModel)
    }

    if (isMenuOpen) {
        MenuDialog(viewModel = viewModel) {
            isMenuOpen = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LanguageText.getText(curLang, "title").replace("\n", " ")) },
                navigationIcon = {
                    IconButton(onClick = { isMenuOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    if (viewModel.isAdmin) {
                        IconButton(onClick = { viewModel.setAdminPanelOpen(true) }) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                        }
                    }
                    IconButton(onClick = { viewModel.logOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(LanguageText.getText(curLang, "ph")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(
                    onClick = viewModel::toggleFavOnlyMode,
                    modifier = Modifier
                        .background(if (isFavOnly) Color(0xFFFF2D55).copy(alpha=0.1f) else Color.Transparent, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (isFavOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = if (isFavOnly) Color(0xFFFF2D55) else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { showStats = true }) {
                    Icon(Icons.Default.BarChart, contentDescription = "Statistics")
                }
            }

            // Tabs
            val classes = listOf("All", "Biru", "Hijau", "Kuning", "Merah", "Perang", "Ungu")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classes.forEach { cls ->
                    FilterChip(
                        selected = selectedClass == cls,
                        onClick = { viewModel.selectClass(cls) },
                        label = { Text(if (cls == "All") LanguageText.getText(curLang, "all") else "2 $cls") }
                    )
                }
            }

            // Class Header Notice / Teacher
            if (selectedClass != "All") {
                val teacher = adminTeachers[selectedClass]
                if (teacher != null && (teacher.g.isNotEmpty() || teacher.p.isNotEmpty())) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(LanguageText.getText(curLang, "tGuru"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(teacher.g.ifEmpty { "-" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(LanguageText.getText(curLang, "tPenolong"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(teacher.p.ifEmpty { "-" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Student List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (activeFilteredStudents.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(LanguageText.getText(curLang, "empty"), style = MaterialTheme.typography.titleMedium)
                            Text(LanguageText.getText(curLang, "emptySub"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(activeFilteredStudents, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            isCompact = isCompact,
                            isFavorite = favorites.contains(student.id),
                            onStudentClick = { viewModel.selectStudentId(it, activeFilteredStudents) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    isCompact: Boolean,
    isFavorite: Boolean,
    onStudentClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStudentClick(student.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCompact) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isCompact) {
                    Text(
                        text = "${student.id} • ${student.cn.ifEmpty { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
