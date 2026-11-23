package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuickTouchPreferences
import com.example.service.FloatingBubbleService
import com.example.service.QuickTouchSharedState
import com.example.service.QuickTouchStateHolder
import com.example.system.SystemController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = QuickTouchPreferences(application)

  val sharedState: StateFlow<QuickTouchSharedState> = QuickTouchStateHolder.state

  private val _showInAppControlSheet = MutableStateFlow(false)
  val showInAppControlSheet: StateFlow<Boolean> = _showInAppControlSheet.asStateFlow()

  init {
    loadSavedCustomIcon()
    refreshStatus()
  }

  fun refreshStatus() {
    val ctx = getApplication<Application>()
    SystemController.refreshAllState(ctx)
    if (SystemController.hasOverlayPermission(ctx)) {
      FloatingBubbleService.start(ctx)
    } else {
      FloatingBubbleService.stop(ctx)
    }
  }

  private fun loadSavedCustomIcon() {
    viewModelScope.launch {
      val bitmap = withContext(Dispatchers.IO) { prefs.loadCustomIconBitmap() }
      QuickTouchStateHolder.setCustomIconBitmap(bitmap)
    }
  }

  fun onCustomImagePicked(uri: Uri) {
    viewModelScope.launch {
      val savedBitmap = withContext(Dispatchers.IO) { prefs.saveCustomIconFromUri(uri) }
      if (savedBitmap != null) {
        QuickTouchStateHolder.setCustomIconBitmap(savedBitmap)
      }
    }
  }

  fun resetCustomIcon() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) { prefs.clearCustomIcon() }
      QuickTouchStateHolder.setCustomIconBitmap(null)
    }
  }

  fun openOverlayPermissionSettings() {
    SystemController.openOverlaySettings(getApplication())
  }

  fun openAccessibilitySettings() {
    SystemController.openAccessibilitySettings(getApplication())
  }

  fun openNotificationAccessSettings() {
    SystemController.openNotificationListenerSettings(getApplication())
  }

  fun volumeUp() {
    SystemController.volumeUp(getApplication())
  }

  fun toggleMute() {
    SystemController.toggleMute(getApplication())
  }

  fun lockScreen() {
    SystemController.performLockScreen(getApplication())
  }

  fun powerMenu() {
    SystemController.performPowerMenu(getApplication())
  }

  fun goHome() {
    SystemController.performHome(getApplication())
  }

  fun openSystemSettings() {
    SystemController.openSystemSettings(getApplication())
  }

  fun setVolumePercent(percent: Int, showUi: Boolean) {
    SystemController.setVolumePercent(getApplication(), percent, showUi)
  }

  fun setBrightnessPercent(percent: Int) {
    SystemController.setBrightnessPercent(getApplication(), percent)
  }

  fun setBrightnessDuringDrag(percent: Int) {
    val ctx = getApplication<Application>()
    if (SystemController.canWriteSettings(ctx)) {
      SystemController.setBrightnessPercent(ctx, percent)
    }
  }

  fun setScreenTimeoutMs(timeoutMs: Int) {
    SystemController.setScreenTimeoutMs(getApplication(), timeoutMs)
  }

  fun cycleBrightness() {
    val current = sharedState.value.systemSnapshot.brightnessPercent
    val next =
      when {
        current < 20 -> 25
        current < 55 -> 60
        current < 90 -> 100
        else -> 25
      }
    SystemController.setBrightnessPercent(getApplication(), next)
  }

  fun openFullControlTray() {
    val ctx = getApplication<Application>()
    SystemController.refreshAllState(ctx)
    if (SystemController.hasOverlayPermission(ctx)) {
      FloatingBubbleService.showExpanded(ctx)
    } else {
      _showInAppControlSheet.value = true
    }
  }

  fun dismissInAppControlSheet() {
    _showInAppControlSheet.value = false
  }
}
