package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.QuickTouchTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )

    setContent {
      val sharedState by viewModel.sharedState.collectAsStateWithLifecycle()
      val showInAppSheet by viewModel.showInAppControlSheet.collectAsStateWithLifecycle()

      val photoPickerLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri ->
            if (uri != null) {
              viewModel.onCustomImagePicked(uri)
            }
          },
        )

      val lifecycleOwner = LocalLifecycleOwner.current
      DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.refreshStatus()
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
      }

      QuickTouchTheme {
        MainScreen(
          state = sharedState,
          showInAppControlSheet = showInAppSheet,
          onToggleOverlayPermission = viewModel::openOverlayPermissionSettings,
          onToggleAccessibility = viewModel::openAccessibilitySettings,
          onChooseImage = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
          },
          onResetImage = viewModel::resetCustomIcon,
          onVolumeUp = viewModel::volumeUp,
          onToggleMute = viewModel::toggleMute,
          onLockScreen = viewModel::lockScreen,
          onPowerMenu = viewModel::powerMenu,
          onHome = viewModel::goHome,
          onOpenSettings = viewModel::openSystemSettings,
          onSelectBrightnessPreset = viewModel::setBrightnessPercent,
          onOpenFullControlTray = viewModel::openFullControlTray,
          onDismissInAppSheet = viewModel::dismissInAppControlSheet,
          onVolumeChanged = { percent -> viewModel.setVolumePercent(percent, showUi = false) },
          onVolumePreset = { percent -> viewModel.setVolumePercent(percent, showUi = true) },
          onBrightnessDuringDrag = viewModel::setBrightnessDuringDrag,
          onBrightnessChangeFinished = viewModel::setBrightnessPercent,
          onCycleBright = viewModel::cycleBrightness,
          onSelectTimeout = viewModel::setScreenTimeoutMs,
        )
      }
    }
  }
}
