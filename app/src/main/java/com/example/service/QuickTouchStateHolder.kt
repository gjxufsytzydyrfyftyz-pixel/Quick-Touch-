package com.example.service

import android.graphics.Bitmap
import com.example.system.MediaPlaybackSnapshot
import com.example.system.SystemControlSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class OverlayStage {
  BUBBLE,
  PILL,
  EXPANDED,
}

data class QuickTouchSharedState(
  val isOverlayPermissionGranted: Boolean = false,
  val isAccessibilityActive: Boolean = false,
  val isNotificationAccessEnabled: Boolean = false,
  val canWriteSettings: Boolean = false,
  val isOverlayServiceRunning: Boolean = false,
  val overlayStage: OverlayStage = OverlayStage.BUBBLE,
  val isSnappedRight: Boolean = true,
  val customIconBitmap: Bitmap? = null,
  val hasCustomIcon: Boolean = false,
  val systemSnapshot: SystemControlSnapshot = SystemControlSnapshot(),
  val mediaSnapshot: MediaPlaybackSnapshot = MediaPlaybackSnapshot(),
)

object QuickTouchStateHolder {
  private val _state = MutableStateFlow(QuickTouchSharedState())
  val state: StateFlow<QuickTouchSharedState> = _state.asStateFlow()

  fun updatePermissions(
    overlayGranted: Boolean,
    accessibilityActive: Boolean,
    notificationAccessEnabled: Boolean,
    canWrite: Boolean,
  ) {
    _state.update {
      it.copy(
        isOverlayPermissionGranted = overlayGranted,
        isAccessibilityActive = accessibilityActive,
        isNotificationAccessEnabled = notificationAccessEnabled,
        canWriteSettings = canWrite,
      )
    }
  }

  fun setAccessibilityActive(active: Boolean) {
    _state.update { it.copy(isAccessibilityActive = active) }
  }

  fun setNotificationAccessEnabled(enabled: Boolean) {
    _state.update { it.copy(isNotificationAccessEnabled = enabled) }
  }

  fun setOverlayServiceRunning(running: Boolean) {
    _state.update { it.copy(isOverlayServiceRunning = running) }
  }

  fun setOverlayStage(stage: OverlayStage) {
    _state.update { it.copy(overlayStage = stage) }
  }

  fun setSnappedRight(snappedRight: Boolean) {
    _state.update { it.copy(isSnappedRight = snappedRight) }
  }

  fun setCustomIconBitmap(bitmap: Bitmap?) {
    _state.update {
      it.copy(
        customIconBitmap = bitmap,
        hasCustomIcon = bitmap != null,
      )
    }
  }

  fun updateSystemSnapshot(snapshot: SystemControlSnapshot) {
    _state.update { it.copy(systemSnapshot = snapshot) }
  }

  fun updateMediaSnapshot(snapshot: MediaPlaybackSnapshot) {
    _state.update { it.copy(mediaSnapshot = snapshot) }
  }
}
