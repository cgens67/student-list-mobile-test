package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Favorites Carousel
                val favoriteStudents = activeFilteredStudents.filter { favorites.contains(it.id) }
                if (favoriteStudents.isNotEmpty()) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val favState = androidx.compose.material3.carousel.rememberCarouselState { favoriteStudents.size }
                    androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel(
                        state = favState,
                        preferredItemWidth = 240.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    ) { i ->
                        val student = favoriteStudents[i]
                        StudentCard(
                            student = student,
                            isCompact = isCompact,
                            isFavorite = true,
                            onStudentClick = { viewModel.selectStudentId(it, activeFilteredStudents) },
                            modifier = Modifier.maskClip(RoundedCornerShape(24.dp))
                        )
                    }
                }

                // Student List Carousel
                val otherStudents = if (favoriteStudents.isNotEmpty()) {
                    activeFilteredStudents.filter { !favorites.contains(it.id) }
                } else {
                    activeFilteredStudents
                }
                
                if (otherStudents.isEmpty() && favoriteStudents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(LanguageText.getText(curLang, "empty"), style = MaterialTheme.typography.titleMedium)
                            Text(LanguageText.getText(curLang, "emptySub"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (otherStudents.isNotEmpty()) {
                    Text(
                        text = if (favoriteStudents.isNotEmpty()) "Other Students" else LanguageText.getText(curLang, "all"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val state = androidx.compose.material3.carousel.rememberCarouselState { otherStudents.size }
                    androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel(
                        state = state,
                        preferredItemWidth = 240.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    ) { i ->
                        val student = otherStudents[i]
                        StudentCard(
                            student = student,
                            isCompact = isCompact,
                            isFavorite = false,
                            onStudentClick = { viewModel.selectStudentId(it, activeFilteredStudents) },
                            modifier = Modifier.maskClip(RoundedCornerShape(24.dp))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    isCompact: Boolean,
    isFavorite: Boolean,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onStudentClick(student.id) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${student.id} • ${student.cn.ifEmpty { "-" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (student.newClass.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Class ${student.newClass}") }
                    )
                }
            }
        }
    }
}
