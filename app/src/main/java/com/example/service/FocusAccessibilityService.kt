package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.data.local.AppDatabase
import com.example.data.repository.FocusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var repository: FocusRepository? = null

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = FocusRepository(db.appLockDao(), applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignore self package and home launcher to avoid lock loops
        if (packageName == applicationContext.packageName || packageName.contains("launcher")) {
            return
        }

        serviceScope.launch {
            val repo = repository ?: return@launch
            val missionSettings = repo.missionSettings.firstOrNull()

            // Check if focus mode is active
            if (missionSettings?.isFocusing == true) {
                val lockedApps = repo.lockedApps.firstOrNull() ?: emptyList()
                val targetApp = lockedApps.find { it.packageName == packageName }

                if (targetApp != null && targetApp.isLocked && !targetApp.isDefaultExcluded) {
                    // Trigger Lock Overlay Activity
                    val intent = Intent(applicationContext, LockOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("LOCKED_PACKAGE", packageName)
                        putExtra("LOCKED_APP_NAME", targetApp.appName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle service interrupt if needed
    }
}
