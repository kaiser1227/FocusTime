package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.local.AppLockDao
import com.example.data.local.entities.FocusStreakEntity
import com.example.data.local.entities.LockedAppEntity
import com.example.data.local.entities.MissionSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusRepository(
    private val dao: AppLockDao,
    private val context: Context
) {

    val lockedApps: Flow<List<LockedAppEntity>> = dao.getAllLockedApps()
    val missionSettings: Flow<MissionSettingsEntity?> = dao.getMissionSettings()
    val focusStreaks: Flow<List<FocusStreakEntity>> = dao.getFocusStreaks()

    // Default excluded packages: Phone, SMS, Camera
    private val defaultExcludedPackages = setOf(
        "com.android.dialer",
        "com.samsung.android.dialer",
        "com.google.android.dialer",
        "com.android.phone",
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.google.android.apps.messaging",
        "com.android.camera",
        "com.android.camera2",
        "com.sec.android.app.camera",
        "com.google.android.GoogleCamera"
    )

    suspend fun loadAndSyncInstalledApps(): List<LockedAppEntity> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val currentLockedMap = dao.getLockedAppsSync().associateBy { it.packageName }

        val syncedList = mutableListOf<LockedAppEntity>()
        for (appInfo in installedApps) {
            val pkg = appInfo.packageName
            // Skip launcher self or system framework core if needed, but show launchable user apps
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent == null && isSystemApp) continue // Skip non-launchable system services

            val label = pm.getApplicationLabel(appInfo).toString()
            val isExcluded = defaultExcludedPackages.contains(pkg) || isDefaultExcludedByName(label)

            val existing = currentLockedMap[pkg]
            val entity = LockedAppEntity(
                packageName = pkg,
                appName = label,
                isLocked = if (isExcluded) false else (existing?.isLocked ?: false),
                isDefaultExcluded = isExcluded
            )
            syncedList.add(entity)
        }

        dao.insertOrUpdateApps(syncedList)
        syncedList
    }

    private fun isDefaultExcludedByName(label: String): Boolean {
        val lower = label.lowercase()
        return lower.contains("전화") || lower.contains("phone") ||
               lower.contains("문자") || lower.contains("메시지") || lower.contains("message") ||
               lower.contains("카메라") || lower.contains("camera")
    }

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) = withContext(Dispatchers.IO) {
        val app = dao.getLockedAppsSync().find { it.packageName == packageName }
        if (app != null && !app.isDefaultExcluded) {
            dao.insertOrUpdateApp(app.copy(isLocked = isLocked))
        }
    }

    suspend fun updateMissionSettings(
        meditation: Boolean,
        resolution: Boolean,
        friendApproval: Boolean,
        customResolution: String = "",
        friendCode: String = "1234"
    ) = withContext(Dispatchers.IO) {
        val current = dao.getMissionSettingsSync() ?: MissionSettingsEntity()
        val updated = current.copy(
            meditationEnabled = meditation,
            resolutionEnabled = resolution,
            friendApprovalEnabled = friendApproval,
            customResolution = customResolution,
            friendCode = friendCode
        )
        dao.updateMissionSettings(updated)
    }

    suspend fun setFocusMode(isFocusing: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getMissionSettingsSync() ?: MissionSettingsEntity()
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var endured = current.todayEnduredSeconds
        if (current.lastUpdatedDate != todayStr) {
            endured = 0L // Reset daily counter on new day
        }

        val updated = if (isFocusing) {
            current.copy(
                isFocusing = true,
                focusStartTime = now,
                todayEnduredSeconds = endured,
                lastUpdatedDate = todayStr
            )
        } else {
            val addedSeconds = if (current.focusStartTime > 0) (now - current.focusStartTime) / 1000 else 0
            val totalSeconds = endured + addedSeconds
            recordTodayStreak(todayStr, totalSeconds)
            current.copy(
                isFocusing = false,
                focusStartTime = 0L,
                todayEnduredSeconds = totalSeconds,
                lastUpdatedDate = todayStr
            )
        }
        dao.updateMissionSettings(updated)
    }

    private suspend fun recordTodayStreak(todayStr: String, totalSeconds: Long) {
        val cal = Calendar.getInstance()
        var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 1=Mon .. 7=Sun logic
        if (dayOfWeek == 0) dayOfWeek = 7 // Sunday = 7

        val isSuccess = totalSeconds >= 1800 // Success if at least 30 mins endured today
        val streak = FocusStreakEntity(
            date = todayStr,
            dayOfWeek = dayOfWeek,
            durationSeconds = totalSeconds,
            isSuccess = isSuccess
        )
        dao.insertOrUpdateStreak(streak)
    }
}
