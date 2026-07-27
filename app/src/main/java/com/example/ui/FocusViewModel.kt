package com.example.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FocusStreakEntity
import com.example.data.local.entities.LockedAppEntity
import com.example.data.local.entities.MissionSettingsEntity
import com.example.data.repository.FocusRepository
import com.example.service.FocusAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AppItemUiState(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean,
    val isDefaultExcluded: Boolean
)

data class WeeklyStreakItem(
    val dayName: String, // 월, 화, 수, 목, 금, 토, 일
    val dayOfWeek: Int, // 1=Mon .. 7=Sun
    val isDone: Boolean
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FocusRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppItemUiState>>(emptyList())
    val installedApps: StateFlow<List<AppItemUiState>> = _installedApps.asStateFlow()

    private val _missionSettings = MutableStateFlow(MissionSettingsEntity())
    val missionSettings: StateFlow<MissionSettingsEntity> = _missionSettings.asStateFlow()

    private val _weeklyStreaks = MutableStateFlow<List<WeeklyStreakItem>>(emptyList())
    val weeklyStreaks: StateFlow<List<WeeklyStreakItem>> = _weeklyStreaks.asStateFlow()

    private val _isAccessibilityPermissionGranted = MutableStateFlow(false)
    val isAccessibilityPermissionGranted: StateFlow<Boolean> = _isAccessibilityPermissionGranted.asStateFlow()

    private val _isOverlayPermissionGranted = MutableStateFlow(false)
    val isOverlayPermissionGranted: StateFlow<Boolean> = _isOverlayPermissionGranted.asStateFlow()

    private val _currentLiveFocusSeconds = MutableStateFlow(0L)
    val currentLiveFocusSeconds: StateFlow<Long> = _currentLiveFocusSeconds.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FocusRepository(db.appLockDao(), application)

        viewModelScope.launch {
            repository.loadAndSyncInstalledApps()
        }

        // Observe Locked Apps
        viewModelScope.launch {
            repository.lockedApps.collectLatest { list ->
                _installedApps.value = list.map {
                    AppItemUiState(
                        packageName = it.packageName,
                        appName = it.appName,
                        isLocked = it.isLocked,
                        isDefaultExcluded = it.isDefaultExcluded
                    )
                }
            }
        }

        // Observe Mission Settings
        viewModelScope.launch {
            repository.missionSettings.collectLatest { settings ->
                if (settings != null) {
                    _missionSettings.value = settings
                }
            }
        }

        // Observe Focus Streaks
        viewModelScope.launch {
            repository.focusStreaks.collectLatest { streaks ->
                updateWeeklyStreaksList(streaks)
            }
        }

        // Live timer tick during focus
        viewModelScope.launch {
            while (true) {
                val settings = _missionSettings.value
                if (settings.isFocusing && settings.focusStartTime > 0) {
                    val activeElapsed = (System.currentTimeMillis() - settings.focusStartTime) / 1000
                    _currentLiveFocusSeconds.value = settings.todayEnduredSeconds + activeElapsed
                } else {
                    _currentLiveFocusSeconds.value = settings.todayEnduredSeconds
                }
                delay(1000)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            repository.toggleAppLock(packageName, isLocked)
        }
    }

    fun updateMissions(meditation: Boolean, resolution: Boolean, friendApproval: Boolean) {
        viewModelScope.launch {
            repository.updateMissionSettings(
                meditation = meditation,
                resolution = resolution,
                friendApproval = friendApproval,
                customResolution = _missionSettings.value.customResolution,
                friendCode = _missionSettings.value.friendCode
            )
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val current = _missionSettings.value.isFocusing
            repository.setFocusMode(!current)
        }
    }

    fun checkPermissions(context: Context) {
        // Accessibility Check
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val isAccGranted = enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
            it.resolveInfo.serviceInfo.name == FocusAccessibilityService::class.java.name
        }
        _isAccessibilityPermissionGranted.value = isAccGranted

        // Overlay Check
        val isOverlayGranted = Settings.canDrawOverlays(context)
        _isOverlayPermissionGranted.value = isOverlayGranted
    }

    private fun updateWeeklyStreaksList(streaks: List<FocusStreakEntity>) {
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        val streakMap = streaks.associateBy { it.dayOfWeek }

        val items = (1..7).map { dayIndex ->
            val isDone = streakMap[dayIndex]?.isSuccess ?: false
            WeeklyStreakItem(
                dayName = days[dayIndex - 1],
                dayOfWeek = dayIndex,
                isDone = isDone
            )
        }
        _weeklyStreaks.value = items
    }
}
