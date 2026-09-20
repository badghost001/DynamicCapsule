package com.dynamiccapsule.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.dynamiccapsule.app.model.CapsuleStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CapsuleService : LifecycleService() {
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + Job())
    private var overlay: View? = null
    private var expanded = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(42, notification())
        if (Settings.canDrawOverlays(this)) attachOverlay()
        serviceScope.launch { CapsuleStore.state.collectLatest { render(it.title, it.subtitle) } }
    }

    private fun attachOverlay() {
        if (overlay != null) return
        val text = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(13f)
            setPadding(30, 16, 30, 16)
            setBackgroundColor(Color.BLACK)
            setOnClickListener { expanded = !expanded; CapsuleStore.toggleExpanded() }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 18 }
        getSystemService(WindowManager::class.java).addView(text, params)
        overlay = text
    }

    private fun render(title: String, subtitle: String) {
        (overlay as? TextView)?.text = if (expanded) "$title\n$subtitle" else "$title  ·  $subtitle"
    }

    override fun onDestroy() {
        overlay?.let { getSystemService(WindowManager::class.java).removeView(it) }
        overlay = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("capsule", "Dynamic Capsule", NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun notification(): Notification = NotificationCompat.Builder(this, "capsule")
        .setSmallIcon(android.R.drawable.ic_popup_sync)
        .setContentTitle("Dynamic Capsule is active")
        .setContentText("Live activity controls are ready")
        .setOngoing(true)
        .build()
}
