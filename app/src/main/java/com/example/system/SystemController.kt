package com.example.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.R
import com.example.service.QuickTouchAccessibilityService
import com.example.service.QuickTouchStateHolder
import kotlin.math.roundToInt

data class SystemControlSnapshot(
  val volumePercent: Int = 50,
  val isMuted: Boolean = false,
  val brightnessPercent: Int = 50,
  val screenTimeoutMs: Int = 30_000,
)

object SystemController {

  private var lastNonZeroVolumeIndex: Int = -1

  fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

  fun canWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

  fun isAccessibilityEnabled(context: Context): Boolean {
    if (QuickTouchAccessibilityService.instance != null) return true
    val expectedComponent =
      ComponentName(context, QuickTouchAccessibilityService::class.java).flattenToString()
    val enabledServices =
      Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
      ) ?: return false
    return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
  }

  fun isNotificationAccessEnabled(context: Context): Boolean =
    MediaSessionController.isNotificationAccessEnabled(context)

  fun refreshAllState(context: Context) {
    val overlay = hasOverlayPermission(context)
    val accessibility = isAccessibilityEnabled(context)
    val notificationAccess = isNotificationAccessEnabled(context)
    val canWrite = canWriteSettings(context)
    QuickTouchStateHolder.updatePermissions(overlay, accessibility, notificationAccess, canWrite)
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
    if (notificationAccess) {
      MediaSessionController.startListening(context)
    } else {
      MediaSessionController.stopListening()
    }
  }

  fun readSystemSnapshot(context: Context): SystemControlSnapshot {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val isMuted = currentVol == 0 || audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
    val volPercent = ((currentVol.toFloat() / maxVol.toFloat()) * 100f).roundToInt().coerceIn(0, 100)

    val rawBrightness =
      runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
      }.getOrDefault(128)
    val brightnessPercent =
      ((rawBrightness.toFloat() / 255f) * 100f).roundToInt().coerceIn(0, 100)

    val timeoutMs =
      runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
      }.getOrDefault(30_000)

    return SystemControlSnapshot(
      volumePercent = if (isMuted) 0 else volPercent,
      isMuted = isMuted,
      brightnessPercent = brightnessPercent,
      screenTimeoutMs = timeoutMs,
    )
  }

  fun openOverlaySettings(context: Context) {
    val intent =
      Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
      ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
      .onFailure {
        val fallback =
          Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        runCatching { context.startActivity(fallback) }
      }
  }

  fun openAccessibilitySettings(context: Context) {
    val intent =
      Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { context.startActivity(intent) }
  }

  fun openNotificationListenerSettings(context: Context) {
    val intent =
      Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { context.startActivity(intent) }
  }

  fun openWriteSettings(context: Context) {
    val intent =
      Intent(
        Settings.ACTION_MANAGE_WRITE_SETTINGS,
        Uri.parse("package:${context.packageName}"),
      ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
      .onFailure {
        val fallback =
          Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        runCatching { context.startActivity(fallback) }
      }
  }

  fun openSystemSettings(context: Context) {
    val intent =
      Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
  }

  fun volumeUp(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.adjustStreamVolume(
      AudioManager.STREAM_MUSIC,
      AudioManager.ADJUST_RAISE,
      AudioManager.FLAG_SHOW_UI,
    )
    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    if (currentVol > 0) {
      lastNonZeroVolumeIndex = currentVol
    }
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
  }

  fun toggleMute(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val currentlyMuted = currentVol == 0 || audioManager.isStreamMute(AudioManager.STREAM_MUSIC)

    if (currentlyMuted) {
      audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        AudioManager.ADJUST_UNMUTE,
        0,
      )
      val restoreVol =
        when {
          lastNonZeroVolumeIndex > 0 -> lastNonZeroVolumeIndex
          audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0 ->
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
          else -> (maxVol * 0.4f).roundToInt().coerceAtLeast(1)
        }
      audioManager.setStreamVolume(
        AudioManager.STREAM_MUSIC,
        restoreVol,
        AudioManager.FLAG_SHOW_UI,
      )
    } else {
      lastNonZeroVolumeIndex = currentVol
      audioManager.setStreamVolume(
        AudioManager.STREAM_MUSIC,
        0,
        AudioManager.FLAG_SHOW_UI,
      )
    }
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
  }

  fun setVolumePercent(context: Context, percent: Int, showUi: Boolean = false) {
    val clamped = percent.coerceIn(0, 100)
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val targetIndex = ((clamped / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
    if (targetIndex > 0) {
      lastNonZeroVolumeIndex = targetIndex
      if (audioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
      }
    }
    val flags = if (showUi) AudioManager.FLAG_SHOW_UI else 0
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, flags)
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
  }

  fun setBrightnessPercent(context: Context, percent: Int): Boolean {
    if (!canWriteSettings(context)) {
      Toast.makeText(
        context,
        "Allow 'Modify system settings' to adjust brightness",
        Toast.LENGTH_SHORT,
      ).show()
      openWriteSettings(context)
      return false
    }
    val clamped = percent.coerceIn(0, 100)
    val brightnessValue = ((clamped / 100f) * 255f).roundToInt().coerceIn(0, 255)
    runCatching {
      Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
      )
      Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        brightnessValue,
      )
    }
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
    return true
  }

  fun setScreenTimeoutMs(context: Context, timeoutMs: Int): Boolean {
    if (!canWriteSettings(context)) {
      Toast.makeText(
        context,
        "Allow 'Modify system settings' to change screen timeout",
        Toast.LENGTH_SHORT,
      ).show()
      openWriteSettings(context)
      return false
    }
    runCatching {
      Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_OFF_TIMEOUT,
        timeoutMs,
      )
    }
    QuickTouchStateHolder.updateSystemSnapshot(readSystemSnapshot(context))
    return true
  }

  fun cycleScreenTimeout(context: Context): Int {
    val current = readSystemSnapshot(context).screenTimeoutMs
    val next =
      when {
        current < 60_000 -> 120_000
        current < 300_000 -> 600_000
        else -> 30_000
      }
    setScreenTimeoutMs(context, next)
    return next
  }

  fun performLockScreen(context: Context) {
    val service = QuickTouchAccessibilityService.instance
    if (service == null) {
      Toast.makeText(
        context,
        "Enable ${context.getString(R.string.app_name)} in Accessibility Settings to lock screen",
        Toast.LENGTH_SHORT,
      ).show()
      openAccessibilitySettings(context)
      return
    }
    if (!service.performLockScreen()) {
      Toast.makeText(
        context,
        "Lock screen requires Android 9 or higher",
        Toast.LENGTH_SHORT,
      ).show()
    }
  }

  fun performPowerMenu(context: Context) {
    val service = QuickTouchAccessibilityService.instance
    if (service == null) {
      Toast.makeText(
        context,
        "Enable ${context.getString(R.string.app_name)} in Accessibility Settings for Power Menu",
        Toast.LENGTH_SHORT,
      ).show()
      openAccessibilitySettings(context)
      return
    }
    service.performPowerDialog()
  }

  fun performHome(context: Context) {
    val service = QuickTouchAccessibilityService.instance
    if (service != null) {
      service.performHome()
    } else {
      val homeIntent =
        Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_HOME)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      runCatching { context.startActivity(homeIntent) }
    }
  }
}
