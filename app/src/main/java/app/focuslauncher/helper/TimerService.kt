package app.focuslauncher.helper

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import app.focuslauncher.R

class TimerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var timerTextView: TextView
    private var startTime: Long = 0
    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        fun startTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = "START_TIMER"
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = "STOP_TIMER"
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "START_TIMER" -> startTimer()
                "STOP_TIMER" -> stopTimer()
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
        running = false
        handler.removeCallbacks(timerRunnable)
        timerTextView.text = "00:00"
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (running) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                val seconds = (elapsedMillis / 1000).toInt()
                val minutes = seconds / 60
                val displaySeconds = seconds % 60

                timerTextView.text = String.format("%02d:%02d", minutes, displaySeconds)
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
