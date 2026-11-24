package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.components.FullControlPanelContent
import com.example.ui.components.QuickTouchLogoBadge
import com.example.ui.theme.BorderStrong
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextSecondary

@Composable
fun MainScreen(
  state: QuickTouchSharedState,
  showInAppControlSheet: Boolean,
  onToggleOverlayPermission: () -> Unit,
  onToggleAccessibility: () -> Unit,
  onToggleNotificationAccess: () -> Unit,
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
    containerColor = PureBlack,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        QuickTouchLogoBadge(size = 46.dp)
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 21.sp,
            ),
            color = PureWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = stringResource(R.string.brand_subtitle),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Medium,
              letterSpacing = 1.1.sp,
            ),
            color = TextSecondary,
            maxLines = 1,
          )
        }
      }
    },
    bottomBar = {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
      ) {
        OutlinedButton(
          onClick = onOpenFullControlTray,
          modifier =
            Modifier
              .widthIn(max = 560.dp)
              .fillMaxWidth()
              .height(54.dp)
              .testTag("open_full_control_tray_button"),
          shape = RoundedCornerShape(50),
          border = BorderStroke(1.5.dp, PureWhite),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = PureBlack,
              contentColor = PureWhite,
            ),
        ) {
          Text(
            text = stringResource(R.string.open_control_tray),
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
            ),
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(PureBlack)
          .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. Centered Luminous Floating Bubble Preview Orb + Change / Reset controls
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.padding(vertical = 4.dp),
        ) {
          LuminousBubblePreviewOrb(
            customBitmap = state.customIconBitmap,
            modifier = Modifier.testTag("custom_icon_preview"),
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = onChooseImage,
              modifier =
                Modifier
                  .height(44.dp)
                  .testTag("choose_image_button"),
              shape = RoundedCornerShape(50),
              border = BorderStroke(1.dp, BorderStrong.copy(alpha = 0.7f)),
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor = Color(0xFF0C0E12),
                  contentColor = PureWhite,
                ),
              contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
              Text(
                text = stringResource(R.string.change_bubble_icon),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Medium,
                  letterSpacing = 0.2.sp,
                ),
              )
            }

            TextButton(
              onClick = onResetImage,
              enabled = state.hasCustomIcon,
              modifier = Modifier.height(44.dp).testTag("reset_image_button"),
              colors =
                ButtonDefaults.textButtonColors(
                  contentColor = TextSecondary,
                  disabledContentColor = TextDim,
                ),
            ) {
              Text(
                text = stringResource(R.string.reset_short),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Normal,
                  letterSpacing = 0.1.sp,
                ),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Three Slim Horizontal Capsule-Pill Permission Rows
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CapsulePermissionRow(
            title = stringResource(R.string.permission_overlay_title),
            checked = state.isOverlayPermissionGranted,
            testTag = "overlay_permission_card",
            switchTestTag = "overlay_permission_switch",
            onClick = onToggleOverlayPermission,
          )

          CapsulePermissionRow(
            title = stringResource(R.string.permission_accessibility_title),
            checked = state.isAccessibilityActive,
            testTag = "accessibility_service_card",
            switchTestTag = "accessibility_service_switch",
            onClick = onToggleAccessibility,
          )

          CapsulePermissionRow(
            title = stringResource(R.string.permission_notification_access_title),
            checked = state.isNotificationAccessEnabled,
            testTag = "notification_access_card",
            switchTestTag = "notification_access_switch",
            onClick = onToggleNotificationAccess,
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. 3x2 Grid of Circular Dark-Bordered Quick-Action Buttons
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            CircularQuickActionButton(
              icon = Icons.AutoMirrored.Outlined.VolumeUp,
              label = stringResource(R.string.action_volume_up),
              testTag = "quick_action_volume_up",
              onClick = onVolumeUp,
            )
            CircularQuickActionButton(
              icon = Icons.AutoMirrored.Outlined.VolumeOff,
              label = stringResource(R.string.action_mute),
              highlighted = state.systemSnapshot.isMuted,
              testTag = "quick_action_mute",
              onClick = onToggleMute,
            )
            CircularQuickActionButton(
              icon = Icons.Outlined.Lock,
              label = stringResource(R.string.action_lock_screen),
              testTag = "quick_action_lock_screen",
              onClick = onLockScreen,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            CircularQuickActionButton(
              icon = Icons.Outlined.PowerSettingsNew,
              label = stringResource(R.string.action_power_menu),
              testTag = "quick_action_power_menu",
              onClick = onPowerMenu,
            )
            CircularQuickActionButton(
              icon = Icons.Outlined.Home,
              label = stringResource(R.string.action_home),
              testTag = "quick_action_home",
              onClick = onHome,
            )
            CircularQuickActionButton(
              icon = Icons.Outlined.Settings,
              label = stringResource(R.string.action_settings),
              testTag = "quick_action_settings",
              onClick = onOpenSettings,
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
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
            .background(Color.Black.copy(alpha = 0.78f))
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
            shape = RoundedCornerShape(50),
            color = SlateCard,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier =
              Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .clickable {
                  onDismissInAppSheet()
                  onToggleOverlayPermission()
                },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
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
private fun LuminousBubblePreviewOrb(
  customBitmap: Bitmap?,
  modifier: Modifier = Modifier,
) {
  val imageBitmap = remember(customBitmap) { customBitmap?.asImageBitmap() }
  Box(
    modifier =
      modifier
        .size(136.dp)
        .drawBehind {
          drawCircle(
            brush =
              Brush.radialGradient(
                colorStops =
                  arrayOf(
                    0.55f to Color.Transparent,
                    0.78f to Color.White.copy(alpha = 0.28f),
                    1.0f to Color.Transparent,
                  ),
                radius = size.minDimension / 2f,
              ),
            radius = size.minDimension / 2f,
          )
        },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier
          .requiredSize(114.dp)
          .aspectRatio(1f)
          .clip(CircleShape)
          .background(PureBlack)
          .border(2.5.dp, PureWhite, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap,
          contentDescription = stringResource(R.string.cd_custom_icon_preview),
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().clip(CircleShape),
        )
      } else {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
          val radius = size.minDimension / 2f
          drawCircle(
            color = PureWhite,
            radius = radius * 0.88f,
            style = Stroke(width = 2.4.dp.toPx()),
          )
          drawCircle(
            color = PureWhite,
            radius = radius * 0.34f,
          )
        }
      }
    }
  }
}

@Composable
private fun CapsulePermissionRow(
  title: String,
  checked: Boolean,
  testTag: String,
  switchTestTag: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(54.dp)
        .clip(RoundedCornerShape(50))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(50),
    color = Color(0xFF0A0B0D),
    border =
      BorderStroke(
        width = 1.dp,
        color = if (checked) BorderStrong else BorderSubtle,
      ),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
        color = SoftWhite,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
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
private fun CircularQuickActionButton(
  icon: ImageVector,
  label: String,
  highlighted: Boolean = false,
  testTag: String,
  onClick: () -> Unit,
) {
  val contentTint = if (highlighted) PitchBlack else PureWhite
  val labelTint = if (highlighted) PitchBlack else SoftWhite
  Surface(
    modifier =
      Modifier
        .requiredSize(86.dp)
        .aspectRatio(1f)
        .clip(CircleShape)
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = CircleShape,
    color = if (highlighted) PureWhite else Color(0xFF0A0C0F),
    contentColor = contentTint,
    border = BorderStroke(1.dp, if (highlighted) PureWhite else BorderSubtle),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentTint,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        text = label,
        style =
          MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
          ),
        color = labelTint,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}
