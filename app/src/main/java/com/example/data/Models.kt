package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Student(
    val id: String = "",
    val name: String = "",
    val cn: String = "",
    val oldClass: String = "",
    val newClass: String = "",
    val sch: String = ""
)

@JsonClass(generateAdapter = true)
data class TeacherInfo(
    val g: String = "",
    val p: String = ""
)

@JsonClass(generateAdapter = true)
data class ClassNotice(
    val text: String = "",
    val icon: String = "info",
    val color: String = "#FF9500",
    val style: String = "soft"
)

@JsonClass(generateAdapter = true)
data class DutyDay(
    val wipe: List<String> = emptyList(),
    val sweep: List<String> = emptyList(),
    val arrange: List<String> = emptyList(),
    val trash: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RosterHijau(
    val Monday: DutyDay = DutyDay(),
    val Tuesday: DutyDay = DutyDay(),
    val Wednesday: DutyDay = DutyDay(),
    val Thursday: DutyDay = DutyDay(),
    val Friday: DutyDay = DutyDay(),
    val unitKebersihan: String = "-"
)

@JsonClass(generateAdapter = true)
data class AboutData(
    val title: String = "Student Allocation List 2026",
    val version: String = "Version 6.0 (Secured Edition)",
    val desc: String = "Check allocations, view origin maps, and use Quick Actions (Long Press).",
    val design: String = "Designed with Smooth iOS Performance in mind",
    val footer: String = "Hosted on Vercel | Secured by Firebase"
)

@JsonClass(generateAdapter = true)
data class UpdateScreenSettings(
    val enabled: String = "false",
    val timestamp: Long = 0L,
    val iconAdded: String = "new_releases",
    val iconRemoved: String = "build_circle",
    val changelogAdded: String = "",
    val changelogRemoved: String = ""
)

@JsonClass(generateAdapter = true)
data class AppSettings(
    val theme: String = "dynamic",
    val maintenance: String = "false",
    val disableGlass: String = "dynamic",
    val hideTheme: String = "false",
    val hideStats: String = "false",
    val forceCoachMarks: String = "false",
    val textStyle: String = "original",
    val aboutData: AboutData = AboutData(),
    val updateScreen: UpdateScreenSettings = UpdateScreenSettings()
)

@JsonClass(generateAdapter = true)
data class AuditLog(
    val timestamp: Long = 0L,
    val user: String = "",
    val changes: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FeedbackItem(
    val id: String = "",
    val text: String = "",
    val user: String = "",
    val timestamp: Long = 0L
)

@JsonClass(generateAdapter = true)
data class UserProfileSync(
    val favorites: List<String> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val theme: String = "light",
    val density: String = "comfort",
    val reduceLag: Boolean = false,
    val language: String = "en",
    val userThemeType: String = "default",
    val userThemeColors: List<String> = listOf("#e0c3fc", "#8ec5fc", "#4facfe"),
    val userDoodleStrokes: String = "" 
)

@JsonClass(generateAdapter = true)
data class DoodleStroke(
    val c: String = "#FFFFFF",
    val w: Int = 4,
    val e: Boolean = false,
    val p: List<Float> = emptyList()
)
