package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.MainViewModel
import com.example.data.Student

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsDialog(
    viewModel: MainViewModel,
    activeFilteredStudents: List<Student>,
    onDismissRequest: () -> Unit
) {
    val curLang by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val activeClass by viewModel.selectedClass.collectAsState()

    var statMode by remember { mutableStateOf("origin") } // "origin" (school), "oldClass"
    var statViewType by remember { mutableStateOf("list") } // "list", "map" (bubbles)
    var sortCountBestFirst by remember { mutableStateOf(true) }

    val colorsList = listOf(
        Color(0xFF007AFF), // Blue
        Color(0xFF34C759), // Green
        Color(0xFFFF9500), // Orange
        Color(0xFFFF3B30), // Red
        Color(0xFFAF52DE), // Purple
        Color(0xFF8E8E93), // Gray
        Color(0xFFFF2D55), // Pink
        Color(0xFF5856D6), // Indigo
        Color(0xFF00C7BE)  // Teal
    )

    // Analyze counts based on selection
    val rawData = if (activeFilteredStudents.isNotEmpty()) activeFilteredStudents else emptyList()
    val totalStudents = rawData.size

    val countsMap = remember(rawData, statMode) {
        val m = mutableMapOf<String, Int>()
        rawData.forEach { s ->
            val key = if (statMode == "origin") s.sch.ifEmpty { "-" } else s.oldClass.ifEmpty { "-" }
            m[key] = (m[key] ?: 0) + 1
        }
        m
    }

    val sortedEntries = remember(countsMap, sortCountBestFirst) {
        if (sortCountBestFirst) {
            countsMap.entries.sortedByDescending { it.value }
        } else {
            countsMap.entries.sortedBy { it.key }
        }
    }

    val maxSingleCount = remember(countsMap) {
        if (countsMap.isEmpty()) 1 else countsMap.values.maxOrNull() ?: 1
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (activeClass == "All") {
                                LanguageText.getText(curLang, "allClasses")
                            } else {
                                LanguageText.getText(curLang, "labelStatsTitle")
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = "${LanguageText.getText(curLang, "analyzing")} $totalStudents Students",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Tab Selection Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segment Tab 1
                    val active1 = statMode == "origin"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active1) (if (isDark) Color(0xFF2C2C2E) else Color.White) else Color.Transparent)
                            .clickable { statMode = "origin" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageText.getText(curLang, "statTabOrigin"),
                            fontSize = 12.sp,
                            fontWeight = if (active1) FontWeight.Bold else FontWeight.Medium,
                            color = if (active1) (if (isDark) Color(0xFF32ADE6) else Color(0xFF007AFF)) else (if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f))
                        )
                    }

                    // Segment Tab 2
                    val active2 = statMode == "oldClass"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active2) (if (isDark) Color(0xFF2C2C2E) else Color.White) else Color.Transparent)
                            .clickable { statMode = "oldClass" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageText.getText(curLang, "statTabOldClass"),
                            fontSize = 12.sp,
                            fontWeight = if (active2) FontWeight.Bold else FontWeight.Medium,
                            color = if (active2) (if (isDark) Color(0xFF32ADE6) else Color(0xFF007AFF)) else (if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort order alpha/count toggle
                    IconButton(
                        onClick = { sortCountBestFirst = !sortCountBestFirst },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (sortCountBestFirst) Icons.Default.Sort else Icons.Default.SortByAlpha,
                            contentDescription = "Toggle sorting",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Map View type versus list view toggle
                    IconButton(
                        onClick = { statViewType = if (statViewType == "list") "map" else "list" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (statViewType == "list") Icons.Default.PieChart else Icons.Outlined.BarChart,
                            contentDescription = "Toggle view style",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (totalStudents == 0) {
                    // Empty list state illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = LanguageText.getText(curLang, "empty"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Text(
                                text = LanguageText.getText(curLang, "emptySub"),
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Render layout context
                    if (statViewType == "list") {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Doughnut Arc chart top view
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Pie drawing canvas
                                    Box(
                                        modifier = Modifier
                                            .size(130.dp)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            var startAngle = -90f
                                            sortedEntries.forEachIndexed { index, entry ->
                                                val sweep = (entry.value.toFloat() / totalStudents) * 360f
                                                val color = colorsList[index % colorsList.size]
                                                drawArc(
                                                    color = color,
                                                    startAngle = startAngle,
                                                    sweepAngle = sweep,
                                                    useCenter = false,
                                                    style = Stroke(width = 24.dp.toPx())
                                                )
                                                startAngle += sweep
                                            }
                                        }
                                        // Doughnut inside counting metadata
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalStudents",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                            Text(
                                                text = "Total",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Dynamic Flow chips legend
                                    FlowRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        sortedEntries.take(6).forEachIndexed { index, entry ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(
                                                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(colorsList[index % colorsList.size], CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = entry.key,
                                                    fontSize = 11.sp,
                                                    color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Sized Progress Metrics list mapping
                            items(sortedEntries) { entry ->
                                val index = sortedEntries.indexOf(entry)
                                val color = colorsList[index % colorsList.size]
                                val pct = if (totalStudents > 0) entry.value.toFloat() / totalStudents else 0f
                                val progressPercent = if (maxSingleCount > 0) entry.value.toFloat() / maxSingleCount else 0f

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateSearchQuery(entry.key)
                                            onDismissRequest()
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.key,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                        modifier = Modifier.width(90.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Metric thick bar
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                                CircleShape
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progressPercent)
                                                .background(color, CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "${entry.value}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.width(30.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    } else {
                        // Bubbles Map Screen layout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Center
                            ) {
                                sortedEntries.forEachIndexed { index, entry ->
                                    val scale = 0.5f + (entry.value.toFloat() / maxSingleCount) * 1.5f
                                    val sizeDp = (46 * scale).coerceAtLeast(46f).coerceAtMost(100f).dp
                                    val color = colorsList[index % colorsList.size]

                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(sizeDp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.15f))
                                            .clickable {
                                                viewModel.updateSearchQuery(entry.key)
                                                onDismissRequest()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "${entry.value}",
                                                fontSize = (14 * scale).coerceAtLeast(12f).coerceAtMost(18f).sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = color
                                            )
                                            Text(
                                                text = entry.key,
                                                fontSize = (9 * scale).coerceAtLeast(8f).coerceAtMost(11f).sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = color,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom CTA
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        contentColor = if (isDark) Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = LanguageText.getText(curLang, "btnClose"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
