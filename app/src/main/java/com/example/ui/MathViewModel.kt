package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MathDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.ProgressEntity
import com.example.data.model.QuizScoreEntity
import com.example.data.repository.MathRepository
import com.example.math.QuizGenerator
import com.example.math.QuizQuestion
import com.example.math.QuizTopic
import com.example.math.TableRange
import com.example.ui.customization.AppCustomizationState
import com.example.ui.customization.CustomizationPreferences
import com.example.ui.customization.GlobalCustomConfig
import com.example.ui.customization.SectionCustomConfig
import com.example.ui.customization.SectionKey
import com.example.ui.theme.AppThemeMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tables : Screen("tables")
    object TableDetail : Screen("table_detail")
    object Primes : Screen("primes")
    object SquaresCubes : Screen("squares_cubes")
    object Formulas : Screen("formulas")
    object FormulaDetail : Screen("formula_detail")
    object Quizzes : Screen("quizzes")
    object QuizPlay : Screen("quiz_play")
    object Bookmarks : Screen("bookmarks")
    object PracticeTutor : Screen("practice_tutor")
    object Dashboard : Screen("dashboard")
    object Customize : Screen("customize")
}

data class WeeklyDayStat(
    val dayNameBn: String,
    val dateStr: String,
    val count: Int,
    val isToday: Boolean,
    val isMet: Boolean
)

data class QuizSessionState(
    val topic: QuizTopic = QuizTopic.MULTIPLICATION,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val correctCount: Int = 0,
    val isFinished: Boolean = false
)

class MathViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MathRepository
    private val prefs = application.getSharedPreferences("math_master_prefs", android.content.Context.MODE_PRIVATE)

    // Study Streaks & Daily Activity
    private val _streakDays = MutableStateFlow(1)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _longestStreak = MutableStateFlow(1)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _weeklyActivities = MutableStateFlow<List<WeeklyDayStat>>(emptyList())
    val weeklyActivities: StateFlow<List<WeeklyDayStat>> = _weeklyActivities.asStateFlow()

    // Customization System State
    private val customPrefs = CustomizationPreferences(application)
    private val _customizationState = MutableStateFlow(customPrefs.loadCustomizationState())
    val customizationState: StateFlow<AppCustomizationState> = _customizationState.asStateFlow()

    // Live Preview Customization State (Allows previewing live changes before saving or canceling)
    private val _previewCustomizationState = MutableStateFlow(customPrefs.loadCustomizationState())
    val previewCustomizationState: StateFlow<AppCustomizationState> = _previewCustomizationState.asStateFlow()

    init {
        val db = MathDatabase.getInstance(application)
        repository = MathRepository(db.mathDao())
        initStreakAndActivity()
    }

    // App Navigation Stack (custom backstack for seamless BackHandler support)
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    val currentScreen: Screen
        get() = _screenStack.value.lastOrNull() ?: Screen.Home

    // Active Selection State
    private val _selectedTableNumber = MutableStateFlow(1)
    val selectedTableNumber: StateFlow<Int> = _selectedTableNumber.asStateFlow()

    private val _selectedFormulaId = MutableStateFlow("sq_1")
    val selectedFormulaId: StateFlow<String> = _selectedFormulaId.asStateFlow()

    private val _selectedTableRange = MutableStateFlow(TableRange.RANGE_1_10)
    val selectedTableRange: StateFlow<TableRange> = _selectedTableRange.asStateFlow()

    // Theme Mode - Loaded from persistent SharedPreferences
    private val initialTheme: AppThemeMode = try {
        val savedName = prefs.getString("selected_theme_mode", AppThemeMode.SYSTEM.name)
        AppThemeMode.valueOf(savedName ?: AppThemeMode.SYSTEM.name)
    } catch (_: Exception) {
        AppThemeMode.SYSTEM
    }

    private val _themeMode = MutableStateFlow(initialTheme)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Persistent Database State
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizScores: StateFlow<List<QuizScoreEntity>> = repository.quizScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressList: StateFlow<List<ProgressEntity>> = repository.progressList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quiz Session
    private val _quizSession = MutableStateFlow(QuizSessionState())
    val quizSession: StateFlow<QuizSessionState> = _quizSession.asStateFlow()

    // Step-by-Step Interactive Practice Tutor State
    private val _tutorNumber = MutableStateFlow(7)
    val tutorNumber: StateFlow<Int> = _tutorNumber.asStateFlow()
    private val _tutorStep = MutableStateFlow(1) // 1 to 10
    val tutorStep: StateFlow<Int> = _tutorStep.asStateFlow()
    private val _tutorUserAnswer = MutableStateFlow("")
    val tutorUserAnswer: StateFlow<String> = _tutorUserAnswer.asStateFlow()
    private val _tutorFeedback = MutableStateFlow<String?>(null)
    val tutorFeedback: StateFlow<String?> = _tutorFeedback.asStateFlow()


    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        if (current.lastOrNull() != screen) {
            _screenStack.value = current + screen
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            return true
        }
        return false
    }

    fun navigateToHome() {
        _screenStack.value = listOf(Screen.Home)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("selected_theme_mode", mode.name).apply()
    }

    fun selectRange(range: TableRange) {
        _selectedTableRange.value = range
    }

    fun openTable(number: Int) {
        _selectedTableNumber.value = number
        navigateTo(Screen.TableDetail)
    }

    fun openFormula(formulaId: String) {
        _selectedFormulaId.value = formulaId
        navigateTo(Screen.FormulaDetail)
    }

    fun isBookmarked(id: String): Boolean {
        return bookmarks.value.any { it.id == id }
    }

    fun toggleBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            val currently = isBookmarked(bookmark.id)
            repository.toggleBookmark(bookmark, currently)
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    fun isLearned(itemId: String): Boolean {
        return progressList.value.find { it.itemId == itemId }?.isLearned ?: false
    }

    fun toggleLearned(itemId: String) {
        viewModelScope.launch {
            val currently = isLearned(itemId)
            repository.toggleLearned(itemId, currently)
            if (!currently) {
                recordDailyActivity(2)
            }
        }
    }

    // Quiz Operations
    fun startQuiz(topic: QuizTopic, count: Int = 10) {
        val questions = QuizGenerator.generateQuiz(topic, count)
        _quizSession.value = QuizSessionState(
            topic = topic,
            questions = questions,
            currentIndex = 0,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            correctCount = 0,
            isFinished = false
        )
        navigateTo(Screen.QuizPlay)
    }

    fun selectQuizOption(index: Int) {
        val current = _quizSession.value
        if (!current.isAnswerSubmitted && !current.isFinished) {
            _quizSession.value = current.copy(selectedOptionIndex = index)
        }
    }

    fun submitQuizAnswer() {
        val current = _quizSession.value
        if (current.selectedOptionIndex == null || current.isAnswerSubmitted || current.isFinished) return

        val currentQ = current.questions.getOrNull(current.currentIndex) ?: return
        val isCorrect = current.selectedOptionIndex == currentQ.correctIndex
        val updatedCorrectCount = if (isCorrect) current.correctCount + 1 else current.correctCount

        _quizSession.value = current.copy(
            isAnswerSubmitted = true,
            correctCount = updatedCorrectCount
        )
        if (isCorrect) {
            recordDailyActivity(1)
        }
    }

    fun nextQuizQuestion() {
        val current = _quizSession.value
        if (!current.isAnswerSubmitted) return

        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.questions.size) {
            _quizSession.value = current.copy(
                currentIndex = nextIndex,
                selectedOptionIndex = null,
                isAnswerSubmitted = false
            )
        } else {
            // Quiz completed
            val total = current.questions.size
            val percentage = if (total > 0) (current.correctCount * 100) / total else 0
            _quizSession.value = current.copy(isFinished = true)

            viewModelScope.launch {
                repository.recordQuizScore(
                    QuizScoreEntity(
                        topicId = current.topic.id,
                        topicTitleBn = current.topic.titleBn,
                        score = current.correctCount,
                        totalQuestions = total,
                        percentage = percentage
                    )
                )
                recordDailyActivity(4)
            }
        }
    }

    // Step-by-Step Practice Tutor
    fun startTutor(number: Int = 5) {
        _tutorNumber.value = number
        _tutorStep.value = 1
        _tutorUserAnswer.value = ""
        _tutorFeedback.value = null
        navigateTo(Screen.PracticeTutor)
    }

    fun setTutorNumber(number: Int) {
        _tutorNumber.value = number
        _tutorStep.value = 1
        _tutorUserAnswer.value = ""
        _tutorFeedback.value = null
    }

    fun setTutorAnswerInput(input: String) {
        // filter only digits
        _tutorUserAnswer.value = input.filter { it.isDigit() }
    }

    fun checkTutorStep() {
        val num = _tutorNumber.value
        val step = _tutorStep.value
        val expected = num * step
        val userAnsInt = _tutorUserAnswer.value.toIntOrNull()

        if (userAnsInt == null) {
            _tutorFeedback.value = "অনুগ্রহ করে উত্তর লিখুন"
            return
        }

        if (userAnsInt == expected) {
            recordDailyActivity(1)
            if (step < 10) {
                _tutorFeedback.value = "সঠিক উত্তর! পরবর্তী ধাপে যাচ্ছি..."
                _tutorStep.value = step + 1
                _tutorUserAnswer.value = ""
            } else {
                _tutorFeedback.value = "অভিনন্দন! আপনি সফলভাবে ${num} এর নামতা সম্পন্ন করেছেন!"
                viewModelScope.launch {
                    repository.recordPractice("table_${num}")
                    repository.toggleLearned("table_${num}", currentlyLearned = false)
                    recordDailyActivity(3)
                }
            }
        } else {
            _tutorFeedback.value = "ভুল হয়েছে! সঠিক উত্তর: $num × $step = $expected"
        }
    }


    // --- Streak & Daily Activity Helpers ---
    private fun getTodayDateStr(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun getYesterdayDateStr(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun getDayOfWeekBn(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "শনি"
            Calendar.SUNDAY -> "রবি"
            Calendar.MONDAY -> "সোম"
            Calendar.TUESDAY -> "মঙ্গল"
            Calendar.WEDNESDAY -> "বুধ"
            Calendar.THURSDAY -> "বৃহঃ"
            Calendar.FRIDAY -> "শুক্র"
            else -> ""
        }
    }

    private fun initStreakAndActivity() {
        val today = getTodayDateStr()
        val yesterday = getYesterdayDateStr()
        val lastDate = prefs.getString("streak_last_active_date", null)
        var currentStreak = prefs.getInt("streak_current_days", 0)
        var longestStreak = prefs.getInt("streak_longest_days", 0)

        if (lastDate == null) {
            // First time initialization
            currentStreak = 1
            longestStreak = 1
            prefs.edit()
                .putString("streak_last_active_date", today)
                .putInt("streak_current_days", 1)
                .putInt("streak_longest_days", 1)
                .putInt("act_$today", maxOf(prefs.getInt("act_$today", 0), 1))
                .apply()
        } else if (lastDate == yesterday) {
            if (currentStreak < 1) currentStreak = 1
        } else if (lastDate != today) {
            // Missed more than a day
            currentStreak = 1
            prefs.edit().putInt("streak_current_days", 1).apply()
        }

        if (longestStreak < currentStreak) {
            longestStreak = currentStreak
            prefs.edit().putInt("streak_longest_days", longestStreak).apply()
        }

        _streakDays.value = currentStreak
        _longestStreak.value = longestStreak
        refreshWeeklyStats()
    }

    fun recordDailyActivity(count: Int = 1) {
        val today = getTodayDateStr()
        val yesterday = getYesterdayDateStr()
        val currentTodayCount = prefs.getInt("act_$today", 0)
        prefs.edit().putInt("act_$today", currentTodayCount + count).apply()

        val lastDate = prefs.getString("streak_last_active_date", null)
        var currentStreak = prefs.getInt("streak_current_days", 1)
        var longestStreak = prefs.getInt("streak_longest_days", 1)

        if (lastDate == yesterday) {
            currentStreak += 1
        } else if (lastDate != null && lastDate != today) {
            currentStreak = 1
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        prefs.edit()
            .putString("streak_last_active_date", today)
            .putInt("streak_current_days", currentStreak)
            .putInt("streak_longest_days", longestStreak)
            .apply()

        _streakDays.value = currentStreak
        _longestStreak.value = longestStreak
        refreshWeeklyStats()
    }

    fun refreshWeeklyStats() {
        val list = mutableListOf<WeeklyDayStat>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(cal.time)

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(dayCal.time)
            val dayName = getDayOfWeekBn(dayCal)
            val count = prefs.getInt("act_$dateStr", 0)
            list.add(
                WeeklyDayStat(
                    dayNameBn = dayName,
                    dateStr = dateStr,
                    count = count,
                    isToday = dateStr == todayStr,
                    isMet = count >= 2
                )
            )
        }
        _weeklyActivities.value = list
    }

    // --- Customization Management ---

    fun updatePreviewGlobal(updater: (GlobalCustomConfig) -> GlobalCustomConfig) {
        val current = _previewCustomizationState.value
        _previewCustomizationState.value = current.copy(global = updater(current.global))
    }

    fun updatePreviewSection(key: SectionKey, updater: (SectionCustomConfig) -> SectionCustomConfig) {
        val current = _previewCustomizationState.value
        val sectionCurrent = current.sections[key.id] ?: SectionCustomConfig()
        val updated = updater(sectionCurrent)
        val newMap = current.sections.toMutableMap()
        newMap[key.id] = updated
        _previewCustomizationState.value = current.copy(sections = newMap)
    }

    fun resetPreviewSection(key: SectionKey) {
        val current = _previewCustomizationState.value
        val newMap = current.sections.toMutableMap()
        newMap.remove(key.id)
        _previewCustomizationState.value = current.copy(sections = newMap)
    }

    fun resetPreviewCategory(category: String) {
        val current = _previewCustomizationState.value
        when (category) {
            "LAYOUT" -> {
                _previewCustomizationState.value = current.copy(
                    global = current.global.copy(
                        layoutType = com.example.ui.customization.CustomLayoutType.COMFORTABLE_LIST,
                        gridColumns = 2,
                        horizontalGapDp = 12,
                        verticalGapDp = 12,
                        cornerRadiusDp = 16
                    )
                )
            }
            "TYPOGRAPHY" -> {
                _previewCustomizationState.value = current.copy(
                    global = current.global.copy(
                        fontWeight = com.example.ui.customization.CustomFontWeight.MEDIUM,
                        titleFontSizeSp = 16,
                        subtitleFontSizeSp = 12,
                        interfaceScalePercent = 100
                    )
                )
            }
            "GLOBAL_THEME" -> {
                _previewCustomizationState.value = current.copy(
                    global = current.global.copy(
                        customAccentHex = null,
                        customPrimaryActionHex = null,
                        customLightBackgroundHex = null,
                        customLightCardSurfaceHex = null,
                        customLightTextColorHex = null,
                        customDarkBackgroundHex = null,
                        customDarkCardSurfaceHex = null,
                        customDarkTextColorHex = null
                    )
                )
            }
            "SECTIONS" -> {
                _previewCustomizationState.value = current.copy(sections = emptyMap())
            }
        }
    }

    fun saveCustomization() {
        val toSave = _previewCustomizationState.value
        customPrefs.saveCustomizationState(toSave)
        _customizationState.value = toSave
    }

    fun discardCustomizationPreview() {
        _previewCustomizationState.value = _customizationState.value
    }

    fun resetAllCustomizations() {
        customPrefs.clearAll()
        val clean = AppCustomizationState()
        _customizationState.value = clean
        _previewCustomizationState.value = clean
    }
}

