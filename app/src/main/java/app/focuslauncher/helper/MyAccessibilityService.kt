package app.focuslauncher.helper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import app.focuslauncher.R
import app.focuslauncher.data.Prefs


class MyAccessibilityService : AccessibilityService() {

    private val INSTAGRAM_PACKAGE = "com.instagram.android"
    private var isInstagramOpen = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onServiceConnected() {
        Prefs(applicationContext).lockModeOn = true
        super.onServiceConnected()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val source: AccessibilityNodeInfo = event.source ?: return
            if ((source.className == "android.widget.FrameLayout") and
                (source.contentDescription == getString(R.string.lock_layout_description))
            )
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString()
                if (INSTAGRAM_PACKAGE == packageName) {
                    if (!isInstagramOpen) {
                        isInstagramOpen = true
                        TimerService.startTimer(this)
                    }
                } else {
                    if (isInstagramOpen) {
                        isInstagramOpen = false
                        TimerService.stopTimer(this)
                    }
                }
            }

        } catch (e: Exception) {
            return
        }
    }

    override fun onInterrupt() {

    }


}