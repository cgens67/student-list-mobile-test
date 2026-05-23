package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val email: String, val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = AppRoomDatabase.getDatabase(context)
    private val dao = db.studentDao()

    private val moshi = Moshi.Builder().build()
    private val doodleAdapter = moshi.adapter<List<DoodleStroke>>(
        Types.newParameterizedType(List::class.java, DoodleStroke::class.java)
    )

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Core Data Flows (Cached from Firebase Snapshot list -> synched to Room -> read as dynamic lists)
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<Map<String, TeacherInfo>>(emptyMap())
    val teachers: StateFlow<Map<String, TeacherInfo>> = _teachers.asStateFlow()

    private val _notices = MutableStateFlow<Map<String, ClassNotice>>(emptyMap())
    val notices: StateFlow<Map<String, ClassNotice>> = _notices.asStateFlow()

    private val _dutyRoster = MutableStateFlow<RosterHijau>(RosterHijau())
    val dutyRoster: StateFlow<RosterHijau> = _dutyRoster.asStateFlow()

    private val _globalSettings = MutableStateFlow<AppSettings>(AppSettings())
    val globalSettings: StateFlow<AppSettings> = _globalSettings.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    // User preference state synced across devices
    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isCompactMode = MutableStateFlow(false)
    val isCompactMode: StateFlow<Boolean> = _isCompactMode.asStateFlow()

    private val _reduceLag = MutableStateFlow(false)
    val reduceLag: StateFlow<Boolean> = _reduceLag.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _userThemeType = MutableStateFlow("default")
    val userThemeType: StateFlow<String> = _userThemeType.asStateFlow()

    private val _userThemeColors = MutableStateFlow(listOf("#e0c3fc", "#8ec5fc", "#4facfe"))
    val userThemeColors: StateFlow<List<String>> = _userThemeColors.asStateFlow()

    private val _doodleStrokes = MutableStateFlow<List<DoodleStroke>>(emptyList())
    val doodleStrokes: StateFlow<List<DoodleStroke>> = _doodleStrokes.asStateFlow()

    // Runtime Interactive State (Local Only)
    private val _selectedClass = MutableStateFlow("All")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterPrevClass = MutableStateFlow("")
    val filterPrevClass: StateFlow<String> = _filterPrevClass.asStateFlow()

    private val _filterSchOrigin = MutableStateFlow("")
    val filterSchOrigin: StateFlow<String> = _filterSchOrigin.asStateFlow()

    private val _sortType = MutableStateFlow("name") // "name", "class", "birthday"
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _isFavOnlyMode = MutableStateFlow(false)
    val isFavOnlyMode: StateFlow<Boolean> = _isFavOnlyMode.asStateFlow()

    private val _isFiltersOverlayOpen = MutableStateFlow(false)
    val isFiltersOverlayOpen: StateFlow<Boolean> = _isFiltersOverlayOpen.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<String?>(null)
    val selectedStudentId: StateFlow<String?> = _selectedStudentId.asStateFlow()

    private val _isDrawingOverlayOpen = MutableStateFlow(false)
    val isDrawingOverlayOpen: StateFlow<Boolean> = _isDrawingOverlayOpen.asStateFlow()

    private val _activeDrawingColor = MutableStateFlow("#FFFFFF")
    val activeDrawingColor: StateFlow<String> = _activeDrawingColor.asStateFlow()

    private val _activeDrawingBrushSize = MutableStateFlow(4)
    val activeDrawingBrushSize: StateFlow<Int> = _activeDrawingBrushSize.asStateFlow()

    private val _isDrawingEraserMode = MutableStateFlow(false)
    val isDrawingEraserMode: StateFlow<Boolean> = _isDrawingEraserMode.asStateFlow()

    private val _coachMarkStepIndex = MutableStateFlow(-1) // -1 means tutorial completed or skipped
    val coachMarkStepIndex: StateFlow<Int> = _coachMarkStepIndex.asStateFlow()

    private val _startupUpdateStepIndex = MutableStateFlow(0) // 0 = closed, 1 = added features chapter, 2 = removed modifications chapter
    val startupUpdateStepIndex: StateFlow<Int> = _startupUpdateStepIndex.asStateFlow()

    private val _adminActiveTab = MutableStateFlow("students") // "students", "teachers", "settings", "audit", "feedback", "analytics"
    val adminActiveTab: StateFlow<String> = _adminActiveTab.asStateFlow()

    private val _adminSelectedStudentId = MutableStateFlow("NEW")
    val adminSelectedStudentId: StateFlow<String> = _adminSelectedStudentId.asStateFlow()

    private val _adminClassNotices = MutableStateFlow<Map<String, ClassNotice>>(emptyMap())
    val adminClassNotices: StateFlow<Map<String, ClassNotice>> = _adminClassNotices.asStateFlow()

    private val _adminTeachers = MutableStateFlow<Map<String, TeacherInfo>>(emptyMap())
    val adminTeachers: StateFlow<Map<String, TeacherInfo>> = _adminTeachers.asStateFlow()

    private val _adminStudents = MutableStateFlow<List<Student>>(emptyList())
    val adminStudents: StateFlow<List<Student>> = _adminStudents.asStateFlow()

    private val _adminDutyRoster = MutableStateFlow<RosterHijau>(RosterHijau())
    val adminDutyRoster: StateFlow<RosterHijau> = _adminDutyRoster.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null
    private var schoolInfoListenerRegistration: ListenerRegistration? = null
    private var auditLogsListenerRegistration: ListenerRegistration? = null
    private var isFirebaseInitSucceeded = false
    val isAdmin: Boolean
        get() = (authState.value as? AuthState.Authenticated)?.email?.lowercase() == "loochinsiang5735@gmail.com"

    init {
        // Initialize user local preferences from SharedPreferences prior to loading Firebase to prevent UI flashing
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean("theme_dark", false)
        _isCompactMode.value = prefs.getBoolean("is_compact", false)
        _reduceLag.value = prefs.getBoolean("reduce_lag", false)
        _currentLanguage.value = prefs.getString("current_lang", "en") ?: "en"
        _userThemeType.value = prefs.getString("user_theme_type", "default") ?: "default"
        
        val colorsStr = prefs.getString("user_theme_colors", "#e0c3fc,#8ec5fc,#4facfe") ?: "#e0c3fc,#8ec5fc,#4facfe"
        _userThemeColors.value = colorsStr.split(",")

        val doodleStr = prefs.getString("user_doodle_strokes", "") ?: ""
        if (doodleStr.isNotEmpty()) {
            try {
                _doodleStrokes.value = doodleAdapter.fromJson(doodleStr) ?: emptyList()
            } catch (e: Exception) {
                // ignore
            }
        }

        // Load database offline fallback values
        viewModelScope.launch {
            loadDatabaseFallbacks()
        }

        // Programmatic dynamic Firebase setup (No google-services.json required!)
        viewModelScope.launch {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey("AIzaSyBz0OfcaW50Ug7omcs-U_WSIo0BjdVKZdI")
                        .setApplicationId("1:867205730183:web:534666800bc7b2513fcd22")
                        .setProjectId("studentlist2026")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
                firebaseAuth = FirebaseAuth.getInstance()
                isFirebaseInitSucceeded = true
                bindAuthListener()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Firebase setup failed: ${e.message}")
            }
        }
    }

    private suspend fun loadDatabaseFallbacks() {
        val cachedStudents = dao.getAllStudents().first()
        if (cachedStudents.isNotEmpty()) {
            _students.value = cachedStudents.map {
                Student(it.id, it.name, it.cn, it.oldClass, it.newClass, it.sch)
            }
        }

        val cachedTeachers = dao.getAllTeachers().first()
        if (cachedTeachers.isNotEmpty()) {
            _teachers.value = cachedTeachers.associate {
                it.className to TeacherInfo(it.guru, it.penolong)
            }
        }

        val cachedNotices = dao.getAllNotices().first()
        if (cachedNotices.isNotEmpty()) {
            _notices.value = cachedNotices.associate {
                it.className to ClassNotice(it.text, it.icon, it.color, it.style)
            }
        }

        val cachedDuty = dao.getAllDutyRosters().first()
        if (cachedDuty.isNotEmpty()) {
            var m = DutyDay()
            var t = DutyDay()
            var w = DutyDay()
            var th = DutyDay()
            var f = DutyDay()
            var uk = "-"
            cachedDuty.forEach {
                val d = DutyDay(
                    wipe = if (it.wipeList.isEmpty()) emptyList() else it.wipeList.split(","),
                    sweep = if (it.sweepList.isEmpty()) emptyList() else it.sweepList.split(","),
                    arrange = if (it.arrangeList.isEmpty()) emptyList() else it.arrangeList.split(","),
                    trash = if (it.trashList.isEmpty()) emptyList() else it.trashList.split(",")
                )
                when (it.day) {
                    "Monday" -> m = d
                    "Tuesday" -> t = d
                    "Wednesday" -> w = d
                    "Thursday" -> th = d
                    "Friday" -> f = d
                }
                if (it.unitKebersihan.isNotEmpty()) uk = it.unitKebersihan
            }
            _dutyRoster.value = RosterHijau(m, t, w, th, f, uk)
        }
    }

    private fun bindAuthListener() {
        firebaseAuth?.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(user.email ?: "", user.uid)
                onAccountConnected(user.uid)
            } else {
                _authState.value = AuthState.Unauthenticated
                onAccountDisconnected()
            }
        }
    }

    private fun onAccountConnected(uid: String) {
        val fs = FirebaseFirestore.getInstance()

        // Sync and listen for schoolInfo configuration document
        schoolInfoListenerRegistration?.remove()
        schoolInfoListenerRegistration = fs.collection("appData").document("schoolInfo")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    viewModelScope.launch {
                        _students.value = parseStudentsList(snapshot.get("students"))
                        _teachers.value = parseTeachersMap(snapshot.get("teachers"))
                        _notices.value = parseNoticesMap(snapshot.get("classNotices"))
                        _dutyRoster.value = parseDutyRoster(snapshot.get("dutyData"))
                        _globalSettings.value = parseAppSettings(snapshot.get("settings"))

                        // Initialize Admin states if empty or first load
                        _adminStudents.value = _students.value
                        _adminTeachers.value = _teachers.value
                        _adminClassNotices.value = _notices.value
                        _adminDutyRoster.value = _dutyRoster.value

                        // Persist to Room local storage cache
                        saveDbLocalCache()

                        // Trigger Startup Update Dialogue if enabled
                        val combinedText = (_globalSettings.value.updateScreen.changelogAdded) +
                                (_globalSettings.value.updateScreen.changelogRemoved)
                        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                        val lastSeen = prefs.getString("last_seen_changelog", "") ?: ""
                        if (_globalSettings.value.updateScreen.enabled == "true" && lastSeen != combinedText) {
                            _startupUpdateStepIndex.value = 1
                        }

                        // Trigger Coach Marks walking guide if forced by admin of if first connected account
                        if (_globalSettings.value.forceCoachMarks == "true") {
                            _coachMarkStepIndex.value = 0
                        }
                    }
                }
            }

        // Sync and listen for logs chronologically
        auditLogsListenerRegistration?.remove()
        auditLogsListenerRegistration = fs.collection("appData").document("auditLogs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    _auditLogs.value = parseAuditLogs(snapshot.get("logs"))
                }
            }

        // Fetch User synced settings
        fs.collection("users").document(uid).get().addOnSuccessListener { d ->
            if (d.exists()) {
                val fList = d.get("favorites") as? List<*>
                _favorites.value = fList?.mapNotNull { it?.toString() } ?: emptyList()

                val sHistory = d.get("searchHistory") as? List<*>
                _searchHistory.value = sHistory?.mapNotNull { it?.toString() } ?: emptyList()

                val themeStr = d.getString("theme")
                if (themeStr != null) {
                    _isDarkMode.value = (themeStr == "dark")
                }

                val densStr = d.getString("density")
                if (densStr != null) {
                    _isCompactMode.value = (densStr == "compact")
                }

                _reduceLag.value = d.getBoolean("reduceLag") ?: false
                _currentLanguage.value = d.getString("language") ?: "en"

                _userThemeType.value = d.getString("themeType") ?: "default"
                val uColors = d.get("themeColors") as? List<*>
                _userThemeColors.value = uColors?.mapNotNull { it?.toString() } ?: listOf("#e0c3fc", "#8ec5fc", "#4facfe")

                val strokeStr = d.getString("doodleStrokes") ?: ""
                _doodleStrokes.value = if (strokeStr.isNotEmpty()) {
                    try {
                        doodleAdapter.fromJson(strokeStr) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else emptyList()

                val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
                prefs.putBoolean("theme_dark", _isDarkMode.value)
                prefs.putBoolean("is_compact", _isCompactMode.value)
                prefs.putBoolean("reduce_lag", _reduceLag.value)
                prefs.putString("current_lang", _currentLanguage.value)
                prefs.putString("user_theme_type", _userThemeType.value)
                prefs.putString("user_theme_colors", _userThemeColors.value.joinToString(","))
                prefs.putString("user_doodle_strokes", strokeStr)
                prefs.apply()
            }
        }
    }

    private fun onAccountDisconnected() {
        schoolInfoListenerRegistration?.remove()
        auditLogsListenerRegistration?.remove()
        schoolInfoListenerRegistration = null
        auditLogsListenerRegistration = null
    }

    private suspend fun saveDbLocalCache() {
        dao.deleteAllStudents()
        dao.insertStudents(_students.value.map {
            StudentEntity(it.id, it.name, it.cn, it.oldClass, it.newClass, it.sch)
        })

        dao.deleteAllTeachers()
        dao.insertTeachers(_teachers.value.map {
            TeacherEntity(it.key, it.value.g, it.value.p)
        })

        dao.deleteAllNotices()
        dao.insertNotices(_notices.value.map {
            ClassNoticeEntity(it.key, it.value.text, it.value.icon, it.value.color, it.value.style)
        })

        dao.deleteAllDutyRosters()
        val roster = _dutyRoster.value
        val list = listOf(
            DutyRosterEntity("Monday", roster.Monday.wipe.joinToString(","), roster.Monday.sweep.joinToString(","), roster.Monday.arrange.joinToString(","), roster.Monday.trash.joinToString(","), roster.unitKebersihan),
            DutyRosterEntity("Tuesday", roster.Tuesday.wipe.joinToString(","), roster.Tuesday.sweep.joinToString(","), roster.Tuesday.arrange.joinToString(","), roster.Tuesday.trash.joinToString(","), roster.unitKebersihan),
            DutyRosterEntity("Wednesday", roster.Wednesday.wipe.joinToString(","), roster.Wednesday.sweep.joinToString(","), roster.Wednesday.arrange.joinToString(","), roster.Wednesday.trash.joinToString(","), roster.unitKebersihan),
            DutyRosterEntity("Thursday", roster.Thursday.wipe.joinToString(","), roster.Thursday.sweep.joinToString(","), roster.Thursday.arrange.joinToString(","), roster.Thursday.trash.joinToString(","), roster.unitKebersihan),
            DutyRosterEntity("Friday", roster.Friday.wipe.joinToString(","), roster.Friday.sweep.joinToString(","), roster.Friday.arrange.joinToString(","), roster.Friday.trash.joinToString(","), roster.unitKebersihan)
        )
        dao.insertDutyRosters(list)
    }

    // Auth Actions
    fun login(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isFirebaseInitSucceeded) {
            onError("Firebase is not initialized yet.")
            return
        }
        _authState.value = AuthState.Loading
        firebaseAuth?.signInWithEmailAndPassword(email, pword)
            ?.addOnSuccessListener {
                onSuccess()
            }?.addOnFailureListener {
                _authState.value = AuthState.Unauthenticated
                onError(it.localizedMessage ?: "Login failed")
            }
    }

    fun signUp(email: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isFirebaseInitSucceeded) {
            onError("Firebase is not initialized yet.")
            return
        }
        _authState.value = AuthState.Loading
        firebaseAuth?.createUserWithEmailAndPassword(email, pword)
            ?.addOnSuccessListener {
                onSuccess()
                // Force coach mark steps immediately for a fresh account signup
                _coachMarkStepIndex.value = 0
            }?.addOnFailureListener {
                _authState.value = AuthState.Unauthenticated
                onError(it.localizedMessage ?: "Signup failed")
            }
    }

    fun logOut() {
        firebaseAuth?.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    // User preference synchronizations
    fun toggleFavorite(studentId: String) {
        val curr = _favorites.value.toMutableList()
        if (curr.contains(studentId)) {
            curr.remove(studentId)
        } else {
            curr.add(studentId)
        }
        _favorites.value = curr
        syncUserPrefsCloud()
    }

    fun clearAllFavorites() {
        _favorites.value = emptyList()
        syncUserPrefsCloud()
    }

    fun addSearchTerm(term: String) {
        val termClean = term.trim()
        if (termClean.isEmpty()) return
        val list = _searchHistory.value.toMutableList()
        list.remove(termClean)
        list.add(0, termClean)
        if (list.size > 5) list.removeAt(5)
        _searchHistory.value = list
        syncUserPrefsCloud()
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        syncUserPrefsCloud()
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putBoolean("theme_dark", _isDarkMode.value).apply()
        syncUserPrefsCloud()
    }

    fun toggleLayoutDensity() {
        _isCompactMode.value = !_isCompactMode.value
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putBoolean("is_compact", _isCompactMode.value).apply()
        syncUserPrefsCloud()
    }

    fun toggleReduceLag() {
        _reduceLag.value = !_reduceLag.value
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putBoolean("reduce_lag", _reduceLag.value).apply()
        syncUserPrefsCloud()
    }

    fun changeLanguage(lang: String) {
        _currentLanguage.value = lang
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putString("current_lang", lang).apply()
        syncUserPrefsCloud()
    }

    fun changeUserBackgroundPreset(type: String) {
        _userThemeType.value = type
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putString("user_theme_type", type).apply()
        syncUserPrefsCloud()
    }

    fun changeCustomColors(colors: List<String>) {
        _userThemeColors.value = colors
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putString("user_theme_colors", colors.joinToString(",")).apply()
        syncUserPrefsCloud()
    }

    fun addDoodleStroke(stroke: DoodleStroke) {
        val list = _doodleStrokes.value.toMutableList()
        list.add(stroke)
        _doodleStrokes.value = list
        syncUserPrefsCloud()
    }

    fun undoDoodleStroke() {
        val list = _doodleStrokes.value.toMutableList()
        if (list.isNotEmpty()) {
            list.removeAt(list.size - 1)
        }
        _doodleStrokes.value = list
        syncUserPrefsCloud()
    }

    fun clearDoodleStrokes() {
        _doodleStrokes.value = emptyList()
        syncUserPrefsCloud()
    }

    private fun syncUserPrefsCloud() {
        val uid = (authState.value as? AuthState.Authenticated)?.uid ?: return
        val listJson = doodleAdapter.toJson(_doodleStrokes.value)

        // Save locally instantly
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
        prefs.putString("user_doodle_strokes", listJson)
        prefs.apply()

        // Upload in background task
        FirebaseFirestore.getInstance().collection("users").document(uid).set(
            mapOf(
                "favorites" to _favorites.value,
                "searchHistory" to _searchHistory.value,
                "theme" to if (_isDarkMode.value) "dark" else "light",
                "density" to if (_isCompactMode.value) "compact" else "comfort",
                "reduceLag" to _reduceLag.value,
                "language" to _currentLanguage.value,
                "themeType" to _userThemeType.value,
                "themeColors" to _userThemeColors.value,
                "doodleStrokes" to listJson
            )
        )
    }

    // Main App Runtime UI mutators
    fun selectClass(cls: String) {
        _selectedClass.value = cls
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setYearFilter(y: String) {
        _filterPrevClass.value = y
    }

    fun setSchFilter(sch: String) {
        _filterSchOrigin.value = sch
    }

    fun toggleFavOnlyMode() {
        _isFavOnlyMode.value = !_isFavOnlyMode.value
    }

    fun setSortType(type: String) {
        if (_sortType.value == type) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _sortType.value = type
            _sortAscending.value = true
        }
    }

    fun toggleFiltersOverlay() {
        _isFiltersOverlayOpen.value = !_isFiltersOverlayOpen.value
    }

    fun selectStudentId(id: String?, activeFilteredList: List<Student> = emptyList()) {
        _selectedStudentId.value = id
        if (id != null && activeFilteredList.isNotEmpty()) {
            _studentListSelectionIndex.value = activeFilteredList.indexOfFirst { it.id == id }
        }
    }

    private val _studentListSelectionIndex = MutableStateFlow(-1)
    
    fun navigateSelectedStudent(direction: Int, activeFilteredList: List<Student>) {
        if (activeFilteredList.isEmpty()) return
        val currentInd = activeFilteredList.indexOfFirst { it.id == _selectedStudentId.value }
        if (currentInd != -1) {
            val target = currentInd + direction
            if (target in activeFilteredList.indices) {
                _selectedStudentId.value = activeFilteredList[target].id
                _studentListSelectionIndex.value = target
            }
        }
    }

    fun setDrawingMode(active: Boolean) {
        _isDrawingOverlayOpen.value = active
    }

    fun setDrawingColor(color: String) {
        _activeDrawingColor.value = color
        _isDrawingEraserMode.value = false
    }

    fun setDrawingBrushSize(size: Int) {
        _activeDrawingBrushSize.value = size
    }

    fun setDrawingEraser(active: Boolean) {
        _isDrawingEraserMode.value = active
    }

    fun nextCoachMark() {
        if (_coachMarkStepIndex.value != -1) {
            val next = _coachMarkStepIndex.value + 1
            _coachMarkStepIndex.value = if (next > 5) -1 else next
        }
    }

    fun closeCoachMarks() {
        _coachMarkStepIndex.value = -1
    }

    fun closeChangelogScreen() {
        _startupUpdateStepIndex.value = 0
        // Save preferences so it never pops up again until a newer timestamp is set
        val combinedText = (_globalSettings.value.updateScreen.changelogAdded) +
                (_globalSettings.value.updateScreen.changelogRemoved)
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
            .putString("last_seen_changelog", combinedText).apply()
    }

    fun nextChangelogStep() {
        if (_startupUpdateStepIndex.value == 1) {
            if (_globalSettings.value.updateScreen.changelogRemoved.trim().isNotEmpty()) {
                _startupUpdateStepIndex.value = 2
            } else {
                closeChangelogScreen()
            }
        } else {
            closeChangelogScreen()
        }
    }

    // Feedback System submission
    fun submitFeedback(text: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isFirebaseInitSucceeded) {
            onError("Connection issue.")
            return
        }
        val userEmail = (authState.value as? AuthState.Authenticated)?.email ?: "anonymous@studentlist"
        val docId = System.currentTimeMillis().toString() + "_" + (100..999).random()
        FirebaseFirestore.getInstance().collection("feedback").document(docId).set(
            mapOf(
                "id" to docId,
                "text" to text.trim(),
                "user" to userEmail,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onError(it.localizedMessage ?: "Failed submitting feedback")
        }
    }

    private val _isAdminPanelOpen = MutableStateFlow(false)
    val isAdminPanelOpen: StateFlow<Boolean> = _isAdminPanelOpen.asStateFlow()

    private val _isDoodleModeEnabled = MutableStateFlow(false)
    val isDoodleModeEnabled: StateFlow<Boolean> = _isDoodleModeEnabled.asStateFlow()

    fun toggleDarkMode() {
        val current = _isDarkMode.value
        _isDarkMode.value = !current
    }

    fun toggleLanguage() {
        val current = _currentLanguage.value
        _currentLanguage.value = if (current == "en") "ms" else "en"
    }

    fun toggleCompactMode() {
        val current = _isCompactMode.value
        _isCompactMode.value = !current
    }

    fun toggleDoodleMode() {
        _isDoodleModeEnabled.value = !_isDoodleModeEnabled.value
    }

    fun setAdminPanelOpen(isOpen: Boolean) {
        _isAdminPanelOpen.value = isOpen
    }

    // Admin panel data loaders
    fun setAdminActiveTab(tab: String) {
        _adminActiveTab.value = tab
        if (tab == "feedback") {
            loadAdminFeedback()
        }
    }

    fun setAdminSelectedStudentId(id: String) {
        _adminSelectedStudentId.value = id
    }

    private fun loadAdminFeedback() {
        if (!isAdmin) return
        FirebaseFirestore.getInstance().collection("feedback")
            .get().addOnSuccessListener { qs ->
                val list = qs.documents.mapNotNull { d ->
                    FeedbackItem(
                        id = d.getString("id") ?: d.id,
                        text = d.getString("text") ?: "",
                        user = d.getString("user") ?: "",
                        timestamp = d.getLong("timestamp") ?: 0L
                    )
                }.sortedByDescending { it.timestamp }
                _feedbackList.value = list
            }
    }

    fun deleteFeedbackItem(id: String) {
        if (!isAdmin) return
        FirebaseFirestore.getInstance().collection("feedback").document(id).delete()
            .addOnSuccessListener {
                loadAdminFeedback()
            }
    }

    // Admin form temporary mutators (Applying to Queue on memory)
    fun queueAdminStudentChanges(student: Student, isDelete: Boolean = false) {
        val current = _adminStudents.value.toMutableList()
        current.removeAll { it.id == student.id }
        if (!isDelete) {
            current.add(student)
        }
        _adminStudents.value = current
    }

    fun queueAdminClassTeacherAndNotice(
        className: String,
        teacher: TeacherInfo,
        noticeText: String,
        noticeIcon: String,
        noticeColor: String,
        noticeStyle: String
    ) {
        val tMap = _adminTeachers.value.toMutableMap()
        tMap[className] = teacher
        _adminTeachers.value = tMap

        val nMap = _adminClassNotices.value.toMutableMap()
        nMap[className] = ClassNotice(noticeText, noticeIcon, noticeColor, noticeStyle)
        _adminClassNotices.value = nMap
    }

    fun queueAdminDutyCoordinator(ukId: String) {
        val r = _adminDutyRoster.value
        _adminDutyRoster.value = r.copy(unitKebersihan = ukId)
    }

    fun queueAdminAddDuty(day: String, category: String, studentId: String) {
        val r = _adminDutyRoster.value
        val dayRoster = when (day) {
            "Monday" -> r.Monday
            "Tuesday" -> r.Tuesday
            "Wednesday" -> r.Wednesday
            "Thursday" -> r.Thursday
            else -> r.Friday
        }
        val newList = when (category) {
            "wipe" -> dayRoster.wipe.toMutableList().also { if (!it.contains(studentId) && it.size < 2) it.add(studentId) }
            "sweep" -> dayRoster.sweep.toMutableList().also { if (!it.contains(studentId) && it.size < 2) it.add(studentId) }
            "arrange" -> dayRoster.arrange.toMutableList().also { if (!it.contains(studentId) && it.size < 2) it.add(studentId) }
            else -> dayRoster.trash.toMutableList().also { if (!it.contains(studentId) && it.size < 2) it.add(studentId) }
        }
        val modifiedDay = when (category) {
            "wipe" -> dayRoster.copy(wipe = newList)
            "sweep" -> dayRoster.copy(sweep = newList)
            "arrange" -> dayRoster.copy(arrange = newList)
            else -> dayRoster.copy(trash = newList)
        }
        _adminDutyRoster.value = when (day) {
            "Monday" -> r.copy(Monday = modifiedDay)
            "Tuesday" -> r.copy(Tuesday = modifiedDay)
            "Wednesday" -> r.copy(Wednesday = modifiedDay)
            "Thursday" -> r.copy(Thursday = modifiedDay)
            else -> r.copy(Friday = modifiedDay)
        }
    }

    fun queueAdminRemoveDuty(day: String, category: String, studentId: String) {
        val r = _adminDutyRoster.value
        val dayRoster = when (day) {
            "Monday" -> r.Monday
            "Tuesday" -> r.Tuesday
            "Wednesday" -> r.Wednesday
            "Thursday" -> r.Thursday
            else -> r.Friday
        }
        val newList = when (category) {
            "wipe" -> dayRoster.wipe.filter { it != studentId }
            "sweep" -> dayRoster.sweep.filter { it != studentId }
            "arrange" -> dayRoster.arrange.filter { it != studentId }
            else -> dayRoster.trash.filter { it != studentId }
        }
        val modifiedDay = when (category) {
            "wipe" -> dayRoster.copy(wipe = newList)
            "sweep" -> dayRoster.copy(sweep = newList)
            "arrange" -> dayRoster.copy(arrange = newList)
            else -> dayRoster.copy(trash = newList)
        }
        _adminDutyRoster.value = when (day) {
            "Monday" -> r.copy(Monday = modifiedDay)
            "Tuesday" -> r.copy(Tuesday = modifiedDay)
            "Wednesday" -> r.copy(Wednesday = modifiedDay)
            "Thursday" -> r.copy(Thursday = modifiedDay)
            else -> r.copy(Friday = modifiedDay)
        }
    }

    // Push local edits back to live Firestore database
    fun saveAdminDatabaseChanges(newSettings: AppSettings, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isAdmin) {
            onError("Unauthorized.")
            return
        }

        val fs = FirebaseFirestore.getInstance()

        // --- Diffing changes for Audit Log entry ---
        val changeLogs = mutableListOf<String>()
        _adminStudents.value.forEach { newS ->
            val oldS = _students.value.find { it.id == newS.id }
            if (oldS == null) {
                changeLogs.add("Added Student: ${newS.name} (${newS.id})")
            } else if (oldS != newS) {
                changeLogs.add("Edited Student: ${newS.name} (${newS.id})")
            }
        }
        _students.value.forEach { oldS ->
            if (_adminStudents.value.find { it.id == oldS.id } == null) {
                changeLogs.add("Deleted Student: ${oldS.name} (${oldS.id})")
            }
        }

        if (_adminTeachers.value != _teachers.value) changeLogs.add("Updated Class Teachers")
        if (_adminClassNotices.value != _notices.value) changeLogs.add("Updated Class Notices")
        if (_adminDutyRoster.value != _dutyRoster.value) changeLogs.add("Updated Duty Roster (2 Hijau)")
        if (newSettings != _globalSettings.value) changeLogs.add("Updated App Settings")

        if (changeLogs.isNotEmpty()) {
            val userEmail = (authState.value as? AuthState.Authenticated)?.email ?: "Admin"
            val newLog = AuditLog(
                timestamp = System.currentTimeMillis(),
                user = userEmail,
                changes = changeLogs
            )
            val updatedLogs = _auditLogs.value.toMutableList()
            updatedLogs.add(0, newLog)
            if (updatedLogs.size > 50) updatedLogs.removeAt(updatedLogs.size - 1)

            _auditLogs.value = updatedLogs
            fs.collection("appData").document("auditLogs").set(mapOf("logs" to updatedLogs))
        }

        // Upload new school Info
        val docData = mapOf(
            "students" to _adminStudents.value,
            "teachers" to _adminTeachers.value,
            "classNotices" to _adminClassNotices.value,
            "dutyData" to mapOf("Hijau" to _adminDutyRoster.value),
            "settings" to newSettings
        )

        fs.collection("appData").document("schoolInfo").set(docData)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onError(it.localizedMessage ?: "Failed updating info")
            }
    }

    // Parsing helpers for Maps / Objects directly
    private fun parseStudentsList(v: Any?): List<Student> {
        val list = v as? List<*> ?: return emptyList()
        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            Student(
                id = m["id"]?.toString() ?: "",
                name = m["name"]?.toString() ?: "",
                cn = m["cn"]?.toString() ?: "",
                oldClass = m["oldClass"]?.toString() ?: "",
                newClass = m["newClass"]?.toString() ?: "",
                sch = m["sch"]?.toString() ?: ""
            )
        }
    }

    private fun parseTeachersMap(v: Any?): Map<String, TeacherInfo> {
        val map = v as? Map<*, *> ?: return emptyMap()
        return map.entries.associate { entry ->
            val key = entry.key?.toString() ?: ""
            val m = entry.value as? Map<*, *>
            key to TeacherInfo(
                g = m?.get("g")?.toString() ?: "",
                p = m?.get("p")?.toString() ?: ""
            )
        }
    }

    private fun parseNoticesMap(v: Any?): Map<String, ClassNotice> {
        val map = v as? Map<*, *> ?: return emptyMap()
        return map.entries.associate { entry ->
            val key = entry.key?.toString() ?: ""
            val nVal = entry.value
            val notice = if (nVal is Map<*, *>) {
                ClassNotice(
                    text = nVal["text"]?.toString() ?: "",
                    icon = nVal["icon"]?.toString() ?: "info",
                    color = nVal["color"]?.toString() ?: "#FF9500",
                    style = nVal["style"]?.toString() ?: "soft"
                )
            } else {
                ClassNotice(text = nVal?.toString() ?: "")
            }
            key to notice
        }
    }

    private fun parseDutyRoster(v: Any?): RosterHijau {
        val map = v as? Map<*, *> ?: return RosterHijau()
        val h = map["Hijau"] as? Map<*, *> ?: return RosterHijau()
        return RosterHijau(
            Monday = parseDutyDayInternal(h["Monday"]),
            Tuesday = parseDutyDayInternal(h["Tuesday"]),
            Wednesday = parseDutyDayInternal(h["Wednesday"]),
            Thursday = parseDutyDayInternal(h["Thursday"]),
            Friday = parseDutyDayInternal(h["Friday"]),
            unitKebersihan = h["unitKebersihan"]?.toString() ?: "-"
        )
    }

    private fun parseDutyDayInternal(v: Any?): DutyDay {
        val m = v as? Map<*, *> ?: return DutyDay()
        return DutyDay(
            wipe = (m["wipe"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            sweep = (m["sweep"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            arrange = (m["arrange"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            trash = (m["trash"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        )
    }

    private fun parseAppSettings(v: Any?): AppSettings {
        val m = v as? Map<*, *> ?: return AppSettings()
        val abt = m["aboutData"] as? Map<*, *>
        val upd = m["updateScreen"] as? Map<*, *>
        return AppSettings(
            theme = m["theme"]?.toString() ?: "dynamic",
            maintenance = m["maintenance"]?.toString() ?: "false",
            disableGlass = m["disableGlass"]?.toString() ?: "dynamic",
            hideTheme = m["hideTheme"]?.toString() ?: "false",
            hideStats = m["hideStats"]?.toString() ?: "false",
            forceCoachMarks = m["forceCoachMarks"]?.toString() ?: "false",
            textStyle = m["textStyle"]?.toString() ?: "original",
            aboutData = AboutData(
                title = abt?.get("title")?.toString() ?: "Student Allocation List 2026",
                version = abt?.get("version")?.toString() ?: "Version 6.0 (Secured Edition)",
                desc = abt?.get("desc")?.toString() ?: "Check allocations, view origin maps, and use Quick Actions.",
                design = abt?.get("design")?.toString() ?: "Designed with Smooth iOS Performance in mind",
                footer = abt?.get("footer")?.toString() ?: "Hosted on Vercel | Secured by Firebase"
            ),
            updateScreen = UpdateScreenSettings(
                enabled = upd?.get("enabled")?.toString() ?: "false",
                timestamp = upd?.get("timestamp")?.toString()?.toLongOrNull() ?: 0L,
                iconAdded = upd?.get("iconAdded")?.toString() ?: "new_releases",
                iconRemoved = upd?.get("iconRemoved")?.toString() ?: "build_circle",
                changelogAdded = upd?.get("changelogAdded")?.toString() ?: upd?.get("changelog")?.toString() ?: "",
                changelogRemoved = upd?.get("changelogRemoved")?.toString() ?: ""
            )
        )
    }

    private fun parseAuditLogs(v: Any?): List<AuditLog> {
        val list = v as? List<*> ?: return emptyList()
        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            AuditLog(
                timestamp = m["timestamp"]?.toString()?.toLongOrNull() ?: 0L,
                user = m["user"]?.toString() ?: "",
                changes = (m["changes"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            )
        }
    }

    override fun onCleared() {
        onAccountDisconnected()
        super.onCleared()
    }
}
