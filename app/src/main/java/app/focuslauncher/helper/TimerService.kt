package app.focuslauncher.helper

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import app.focuslauncher.R
import app.focuslauncher.data.Prefs


class TimerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var timerTextView: TextView
    private var startTime: Long = 0
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs

    companion object {
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"

        fun startTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START_TIMER
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        timerTextView = overlayView.findViewById(R.id.timerTextView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 0

        windowManager.addView(overlayView, params)

        // Initialize startTime based on saved preferences
        val savedTime: Long = prefs.instagramTimeSpent ?: 0L
        startTime = if (savedTime > 0) {
            SystemClock.elapsedRealtime() - savedTime
        } else {
            SystemClock.elapsedRealtime()
        }
    }

    private fun updateTimerTextView() {
        val elapsedMillis = SystemClock.elapsedRealtime() - startTime
        val totalElapsedTime = prefs.instagramTimeSpent?.plus(elapsedMillis) ?: elapsedMillis

        val hours = (totalElapsedTime / (1000 * 60 * 60)).toInt()
        val minutes = ((totalElapsedTime / (1000 * 60)) % 60).toInt()
        val seconds = ((totalElapsedTime / 1000) % 60).toInt()

        timerTextView.text = when {
            hours >= 1 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }

        timerTextView.setTextColor(
            when {
                hours >= 3 -> Color.RED
                hours >= 2 -> Color.parseColor("#FFA500")
                hours >= 1 -> Color.YELLOW
                else -> Color.WHITE
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_TIMER -> startTimer()
                ACTION_STOP_TIMER -> stopTimer()
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        if (!running) {
            running = true
            startTime = SystemClock.elapsedRealtime()
            handler.post(timerRunnable)
        }
    }

    private fun stopTimer() {
        if (running) {
            running = false
            handler.removeCallbacks(timerRunnable)

            // Save total elapsed time for Instagram
            val elapsedTime = SystemClock.elapsedRealtime() - startTime
            val totalSpentTime = prefs.instagramTimeSpent?.plus(elapsedTime) ?: elapsedTime
            prefs.instagramTimeSpent = totalSpentTime
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (running) {
                updateTimerTextView()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}