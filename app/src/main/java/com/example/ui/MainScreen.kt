package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.service.QuickTouchSharedState
import com.example.ui.components.BubbleAvatar
import com.example.ui.components.FullControlPanelContent
import com.example.ui.components.PresetChip
import com.example.ui.components.QuickTouchLogoBadge
import com.example.ui.theme.BorderStrong
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTile
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarDark

@Composable
fun MainScreen(
  state: QuickTouchSharedState,
  showInAppControlSheet: Boolean,
  onToggleOverlayPermission: () -> Unit,
  onToggleAccessibility: () -> Unit,
  onChooseImage: () -> Unit,
  onResetImage: () -> Unit,
  onVolumeUp: () -> Unit,
  onToggleMute: () -> Unit,
  onLockScreen: () -> Unit,
  onPowerMenu: () -> Unit,
  onHome: () -> Unit,
  onOpenSettings: () -> Unit,
  onSelectBrightnessPreset: (Int) -> Unit,
  onOpenFullControlTray: () -> Unit,
  onDismissInAppSheet: () -> Unit,
  onVolumeChanged: (Int) -> Unit,
  onVolumePreset: (Int) -> Unit,
  onBrightnessDuringDrag: (Int) -> Unit,
  onBrightnessChangeFinished: (Int) -> Unit,
  onCycleBright: () -> Unit,
  onSelectTimeout: (Int) -> Unit,
) {
  Scaffold(
    containerColor = PitchBlack,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(TopBarDark)
            .statusBarsPadding(),
      ) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickTouchLogoBadge(size = 38.dp)
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
      }
    },
    bottomBar = {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(PitchBlack)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
      ) {
        Button(
          onClick = onOpenFullControlTray,
          modifier =
            Modifier
              .widthIn(max = 600.dp)
              .fillMaxWidth()
              .height(52.dp)
              .testTag("open_full_control_tray_button"),
          shape = RoundedCornerShape(26.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = PureWhite,
              contentColor = PitchBlack,
            ),
        ) {
          Text(
            text = stringResource(R.string.open_full_control_tray),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 600.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. Floating Overlay Permission Card
        PermissionStatusCard(
          title = stringResource(R.string.permission_overlay_title),
          statusText =
            if (state.isOverlayPermissionGranted) {
              stringResource(R.string.status_enabled)
            } else {
              stringResource(R.string.status_disabled)
            },
          checked = state.isOverlayPermissionGranted,
          testTag = "overlay_permission_card",
          switchTestTag = "overlay_permission_switch",
          onClick = onToggleOverlayPermission,
        )

        // 2. Accessibility Service Card
        PermissionStatusCard(
          title = stringResource(R.string.permission_accessibility_title),
          statusText =
            if (state.isAccessibilityActive) {
              stringResource(R.string.status_active)
            } else {
              stringResource(R.string.status_inactive)
            },
          checked = state.isAccessibilityActive,
          testTag = "accessibility_service_card",
          switchTestTag = "accessibility_service_switch",
          onClick = onToggleAccessibility,
        )

        // 3. Custom Floating Icon Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = SlateCard,
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = stringResource(R.string.custom_icon_title),
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
              )
              Text(
                text = stringResource(R.string.custom_icon_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
              BubbleAvatar(
                customBitmap = state.customIconBitmap,
                size = 70.dp,
                modifier = Modifier.testTag("custom_icon_preview"),
              )

              Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  OutlinedButton(
                    onClick = onChooseImage,
                    modifier =
                      Modifier
                        .height(48.dp)
                        .testTag("choose_image_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderStrong),
                    colors =
                      ButtonDefaults.outlinedButtonColors(
                        containerColor = SlateTile,
                        contentColor = PureWhite,
                      ),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Image,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = stringResource(R.string.choose_image),
                      style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.2.sp),
                    )
                  }

                  if (state.hasCustomIcon) {
                    TextButton(
                      onClick = onResetImage,
                      modifier = Modifier.height(48.dp).testTag("reset_image_button"),
                      colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
                    ) {
                      Text(
                        text = stringResource(R.string.reset_icon),
                        style = MaterialTheme.typography.labelMedium,
                      )
                    }
                  }
                }

                Text(
                  text = "Local PNG/JPG · Auto-cropped to circle",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextDim,
                )
              }
            }
          }
        }

        // 4. Quick Actions Grid
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = SlateCard,
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = stringResource(R.string.quick_actions_title),
              style = MaterialTheme.typography.titleMedium,
              color = PureWhite,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              QuickActionTile(
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                label = stringResource(R.string.action_volume_up),
                testTag = "quick_action_volume_up",
                onClick = onVolumeUp,
                modifier = Modifier.weight(1f),
              )
              QuickActionTile(
                icon = Icons.AutoMirrored.Outlined.VolumeOff,
                label = stringResource(R.string.action_mute),
                highlighted = state.systemSnapshot.isMuted,
                testTag = "quick_action_mute",
                onClick = onToggleMute,
                modifier = Modifier.weight(1f),
              )
              QuickActionTile(
                icon = Icons.Outlined.Lock,
                label = stringResource(R.string.action_lock_screen),
                testTag = "quick_action_lock_screen",
                onClick = onLockScreen,
                modifier = Modifier.weight(1f),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              QuickActionTile(
                icon = Icons.Outlined.PowerSettingsNew,
                label = stringResource(R.string.action_power_menu),
                testTag = "quick_action_power_menu",
                onClick = onPowerMenu,
                modifier = Modifier.weight(1f),
              )
              QuickActionTile(
                icon = Icons.Outlined.Home,
                label = stringResource(R.string.action_home),
                testTag = "quick_action_home",
                onClick = onHome,
                modifier = Modifier.weight(1f),
              )
              QuickActionTile(
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.action_settings),
                testTag = "quick_action_settings",
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 5. Brightness Presets Row
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = SlateCard,
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = stringResource(R.string.brightness_presets_title),
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
              )
              Text(
                text = "${state.systemSnapshot.brightnessPercent}%",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              listOf(0, 25, 60, 100).forEach { percent ->
                val selected =
                  kotlin.math.abs(state.systemSnapshot.brightnessPercent - percent) <= 5
                PresetChip(
                  label = "$percent%",
                  selected = selected,
                  testTag = "main_brightness_preset_$percent",
                  onClick = { onSelectBrightnessPreset(percent) },
                  modifier = Modifier.weight(1f).height(48.dp),
                )
              }
            }
          }
        }
      }
    }
  }

  // Fallback In-App Full Control Panel Dialog when Overlay Permission is not yet enabled
  if (showInAppControlSheet) {
    Dialog(
      onDismissRequest = onDismissInAppSheet,
      properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      val dismissInteraction = remember { MutableInteractionSource() }
      val panelInteraction = remember { MutableInteractionSource() }
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
              interactionSource = dismissInteraction,
              indication = null,
              onClick = onDismissInAppSheet,
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier =
            Modifier.clickable(
              interactionSource = panelInteraction,
              indication = null,
            ) {},
        ) {
          FullControlPanelContent(
            snapshot = state.systemSnapshot,
            onClose = onDismissInAppSheet,
            onLockScreen = {
              onDismissInAppSheet()
              onLockScreen()
            },
            onPowerMenu = {
              onDismissInAppSheet()
              onPowerMenu()
            },
            onCycleBright = onCycleBright,
            onSelectTimeout = onSelectTimeout,
            onSettings = {
              onDismissInAppSheet()
              onOpenSettings()
            },
            onHome = {
              onDismissInAppSheet()
              onHome()
            },
            onVolumeChanged = { percent -> onVolumeChanged(percent) },
            onVolumePreset = onVolumePreset,
            onBrightnessChanged = onBrightnessDuringDrag,
            onBrightnessChangeFinished = onBrightnessChangeFinished,
            onBrightnessPreset = onSelectBrightnessPreset,
          )

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SlateCard,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier =
              Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                  onDismissInAppSheet()
                  onToggleOverlayPermission()
                },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Enable overlay permission to float over all apps",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Enable",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PureWhite,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionStatusCard(
  title: String,
  statusText: String,
  checked: Boolean,
  testTag: String,
  switchTestTag: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    color = SlateCard,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = PureWhite,
        )
        Text(
          text = "Status: $statusText",
          style = MaterialTheme.typography.bodyMedium,
          color = if (checked) SoftWhite else TextSecondary,
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Switch(
        checked = checked,
        onCheckedChange = { onClick() },
        modifier = Modifier.testTag(switchTestTag),
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = PitchBlack,
            checkedTrackColor = SwitchTrackOn,
            checkedBorderColor = PureWhite,
            uncheckedThumbColor = PureWhite,
            uncheckedTrackColor = SwitchTrackOff,
            uncheckedBorderColor = BorderStrong,
          ),
      )
    }
  }
}

@Composable
private fun QuickActionTile(
  icon: ImageVector,
  label: String,
  highlighted: Boolean = false,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .height(72.dp)
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(10.dp),
    color = if (highlighted) PureWhite else SlateTile,
    border = BorderStroke(1.dp, if (highlighted) PureWhite else BorderSubtle),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (highlighted) PitchBlack else PureWhite,
        modifier = Modifier.size(22.dp),
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (highlighted) PitchBlack else SoftWhite,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}
