package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class QuickTouchAccessibilityService : AccessibilityService() {

  override fun onServiceConnected() {
    super.onServiceConnected()
    instance = this
    QuickTouchStateHolder.setAccessibilityActive(true)
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    // No window content inspection required; actions are triggered explicitly on user tap.
  }

  override fun onInterrupt() {
    // No-op
  }

  override fun onUnbind(intent: Intent?): Boolean {
    if (instance === this) {
      instance = null
    }
    QuickTouchStateHolder.setAccessibilityActive(false)
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    if (instance === this) {
      instance = null
    }
    QuickTouchStateHolder.setAccessibilityActive(false)
    super.onDestroy()
  }

  fun performLockScreen(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    } else {
      false
    }
  }

  fun performPowerDialog(): Boolean = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)

  fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

  companion object {
    @Volatile
    var instance: QuickTouchAccessibilityService? = null
      private set
  }
}
