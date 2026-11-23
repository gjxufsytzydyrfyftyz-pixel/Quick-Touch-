package com.example.service

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.system.MediaSessionController

class QuickTouchNotificationListenerService : NotificationListenerService() {

  override fun onListenerConnected() {
    super.onListenerConnected()
    isConnected = true
    QuickTouchStateHolder.setNotificationAccessEnabled(true)
    MediaSessionController.startListening(applicationContext)
  }

  override fun onListenerDisconnected() {
    isConnected = false
    QuickTouchStateHolder.setNotificationAccessEnabled(false)
    MediaSessionController.stopListening()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      runCatching {
        requestRebind(ComponentName(this, QuickTouchNotificationListenerService::class.java))
      }
    }
    super.onListenerDisconnected()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    // Active media sessions are tracked directly via MediaSessionManager.
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    // Active media sessions are tracked directly via MediaSessionManager.
  }

  override fun onUnbind(intent: Intent?): Boolean {
    isConnected = false
    QuickTouchStateHolder.setNotificationAccessEnabled(false)
    MediaSessionController.stopListening()
    return super.onUnbind(intent)
  }

  companion object {
    @Volatile
    var isConnected: Boolean = false
      private set
  }
}
