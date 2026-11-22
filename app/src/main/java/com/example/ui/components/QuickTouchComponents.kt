package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.system.SystemControlSnapshot
import com.example.ui.theme.BorderStrong
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SlateTile
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.TextSecondary

@Composable
fun QuickTouchLogoBadge(
  size: Dp = 36.dp,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(PitchBlack)
        .border(1.5.dp, PureWhite.copy(alpha = 0.85f), CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.16f)) {
      val radius = this.size.minDimension / 2f
      drawCircle(
        color = PureWhite.copy(alpha = 0.45f),
        radius = radius,
        style = Stroke(width = 1.2.dp.toPx()),
      )
      drawCircle(
        color = PureWhite,
        radius = radius * 0.56f,
        style = Stroke(width = 2.dp.toPx()),
      )
      drawLine(
        color = PureWhite,
        start = Offset(center.x + radius * 0.25f, center.y + radius * 0.25f),
        end = Offset(center.x + radius * 0.68f, center.y + radius * 0.68f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
  }
}

@Composable
fun BubbleAvatar(
  customBitmap: Bitmap?,
  size: Dp = 58.dp,
  modifier: Modifier = Modifier,
) {
  val imageBitmap = remember(customBitmap) { customBitmap?.asImageBitmap() }
  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(PureBlack)
        .border(1.8.dp, PureWhite, CircleShape)
        .padding(3.dp)
        .clip(CircleShape)
        .border(0.8.dp, PureWhite.copy(alpha = 0.45f), CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap,
        contentDescription = stringResource(R.string.cd_floating_bubble),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().clip(CircleShape),
      )
    } else {
      Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.2f)) {
        val radius = this.size.minDimension / 2f
        drawCircle(
          color = PureWhite,
          radius = radius * 0.88f,
          style = Stroke(width = 1.8.dp.toPx()),
        )
        drawCircle(
          color = PureWhite,
          radius = radius * 0.32f,
        )
      }
    }
  }
}

@Composable
fun CompactPillTray(
  isMuted: Boolean,
  onVolumeUp: () -> Unit,
  onMuteToggle: () -> Unit,
  onLockScreen: () -> Unit,
  onPowerMenu: () -> Unit,
  onHome: () -> Unit,
  onSettings: () -> Unit,
  onExpand: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.height(52.dp),
    shape = RoundedCornerShape(26.dp),
    color = PureBlack.copy(alpha = 0.96f),
    border = BorderStroke(1.dp, BorderStrong.copy(alpha = 0.7f)),
    shadowElevation = 8.dp,
  ) {
    Row(
      modifier =
        Modifier
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      PillIconButton(
        icon = Icons.AutoMirrored.Outlined.VolumeUp,
        contentDescription = stringResource(R.string.action_volume_up),
        testTag = "pill_volume_up",
        onClick = onVolumeUp,
      )
      PillIconButton(
        icon = Icons.AutoMirrored.Outlined.VolumeOff,
        contentDescription = stringResource(R.string.action_mute),
        highlighted = isMuted,
        testTag = "pill_mute",
        onClick = onMuteToggle,
      )
      PillIconButton(
        icon = Icons.Outlined.Lock,
        contentDescription = stringResource(R.string.action_lock_screen),
        testTag = "pill_lock",
        onClick = onLockScreen,
      )
      PillIconButton(
        icon = Icons.Outlined.PowerSettingsNew,
        contentDescription = stringResource(R.string.action_power_menu),
        testTag = "pill_power",
        onClick = onPowerMenu,
      )
      PillIconButton(
        icon = Icons.Outlined.Home,
        contentDescription = stringResource(R.string.action_home),
        testTag = "pill_home",
        onClick = onHome,
      )
      PillIconButton(
        icon = Icons.Outlined.Settings,
        contentDescription = stringResource(R.string.action_settings),
        testTag = "pill_settings",
        onClick = onSettings,
      )
      Box(
        modifier =
          Modifier
            .padding(horizontal = 2.dp)
            .size(width = 1.dp, height = 22.dp)
            .background(BorderSubtle),
      )
      PillIconButton(
        icon = Icons.Outlined.OpenInFull,
        contentDescription = stringResource(R.string.action_expand),
        testTag = "pill_expand",
        onClick = onExpand,
      )
    }
  }
}

@Composable
private fun PillIconButton(
  icon: ImageVector,
  contentDescription: String,
  highlighted: Boolean = false,
  testTag: String,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .size(width = 40.dp, height = 48.dp)
        .clip(CircleShape)
        .then(
          if (highlighted) {
            Modifier.background(PureWhite.copy(alpha = 0.14f), CircleShape)
          } else {
            Modifier
          },
        )
        .clickable(onClick = onClick)
        .testTag(testTag),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = PureWhite,
      modifier = Modifier.size(20.dp),
    )
  }
}

@Composable
fun FullControlPanelContent(
  snapshot: SystemControlSnapshot,
  onClose: () -> Unit,
  onLockScreen: () -> Unit,
  onPowerMenu: () -> Unit,
  onCycleBright: () -> Unit,
  onSelectTimeout: (Int) -> Unit,
  onSettings: () -> Unit,
  onHome: () -> Unit,
  onVolumeChanged: (Int) -> Unit,
  onVolumePreset: (Int) -> Unit,
  onBrightnessChanged: (Int) -> Unit,
  onBrightnessChangeFinished: (Int) -> Unit,
  onBrightnessPreset: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var sliderVol by remember(snapshot.volumePercent) {
    mutableFloatStateOf(snapshot.volumePercent / 100f)
  }
  var sliderBright by remember(snapshot.brightnessPercent) {
    mutableFloatStateOf(snapshot.brightnessPercent / 100f)
  }

  Surface(
    modifier = modifier.widthIn(max = 380.dp).fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = SlateCard,
    border = BorderStroke(1.dp, BorderStrong.copy(alpha = 0.65f)),
    shadowElevation = 16.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          QuickTouchLogoBadge(size = 28.dp)
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PureWhite,
          )
        }
        IconButton(
          onClick = onClose,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("panel_close_button"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.cd_close_tray),
            tint = TextSecondary,
          )
        }
      }

      // Action buttons row 1: Lock, Power, Bright
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        PanelCircleAction(
          icon = Icons.Outlined.Lock,
          label = stringResource(R.string.action_lock),
          testTag = "panel_lock_button",
          onClick = onLockScreen,
        )
        PanelCircleAction(
          icon = Icons.Outlined.PowerSettingsNew,
          label = stringResource(R.string.action_power),
          testTag = "panel_power_button",
          onClick = onPowerMenu,
        )
        PanelCircleAction(
          icon = Icons.Outlined.WbSunny,
          label = stringResource(R.string.action_bright),
          testTag = "panel_bright_button",
          onClick = onCycleBright,
        )
      }

      // Action buttons row 2: Timeout, Settings, Home
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        PanelCircleAction(
          icon = Icons.Outlined.Schedule,
          label = stringResource(R.string.action_timeout),
          testTag = "panel_timeout_button",
          onClick = {
            val next =
              when {
                snapshot.screenTimeoutMs < 60_000 -> 120_000
                snapshot.screenTimeoutMs < 300_000 -> 600_000
                else -> 30_000
              }
            onSelectTimeout(next)
          },
        )
        PanelCircleAction(
          icon = Icons.Outlined.Settings,
          label = stringResource(R.string.action_settings),
          testTag = "panel_settings_button",
          onClick = onSettings,
        )
        PanelCircleAction(
          icon = Icons.Outlined.Home,
          label = stringResource(R.string.action_home),
          testTag = "panel_home_button",
          onClick = onHome,
        )
      }

      // Timeout preset chips (30s, 2m, 10m)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = stringResource(R.string.action_timeout),
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary,
          modifier = Modifier.widthIn(min = 58.dp),
        )
        listOf(
          30_000 to "30s",
          120_000 to "2m",
          600_000 to "10m",
        ).forEach { (ms, label) ->
          val selected = isTimeoutSelected(snapshot.screenTimeoutMs, ms)
          PresetChip(
            label = label,
            selected = selected,
            testTag = "timeout_chip_$label",
            onClick = { onSelectTimeout(ms) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      // Volume Slider + Mute / 50% / 100%
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector =
              if (snapshot.isMuted || snapshot.volumePercent == 0) {
                Icons.AutoMirrored.Outlined.VolumeOff
              } else {
                Icons.AutoMirrored.Outlined.VolumeUp
              },
            contentDescription = stringResource(R.string.action_volume_up),
            tint = PureWhite,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Slider(
            value = sliderVol,
            onValueChange = { fraction ->
              sliderVol = fraction
              onVolumeChanged((fraction * 100).toInt())
            },
            colors =
              SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = BorderSubtle,
              ),
            modifier = Modifier.weight(1f).testTag("panel_volume_slider"),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (snapshot.isMuted) "Mute" else "${snapshot.volumePercent}%",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(42.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val volPresets = listOf(0 to "Mute", 50 to "50%", 100 to "100%")
          volPresets.forEach { (percent, label) ->
            val selected =
              if (percent == 0) {
                snapshot.isMuted || snapshot.volumePercent == 0
              } else {
                !snapshot.isMuted && kotlin.math.abs(snapshot.volumePercent - percent) <= 6
              }
            PresetChip(
              label = label,
              selected = selected,
              testTag = "volume_preset_${label.lowercase()}",
              onClick = { onVolumePreset(percent) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // Brightness Slider + 0% / 25% / 60% / 100%
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = stringResource(R.string.action_bright),
            tint = PureWhite,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Slider(
            value = sliderBright,
            onValueChange = { fraction ->
              sliderBright = fraction
              onBrightnessChanged((fraction * 100).toInt())
            },
            onValueChangeFinished = {
              onBrightnessChangeFinished((sliderBright * 100).toInt())
            },
            colors =
              SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = BorderSubtle,
              ),
            modifier = Modifier.weight(1f).testTag("panel_brightness_slider"),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${snapshot.brightnessPercent}%",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(42.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(0, 25, 60, 100).forEach { percent ->
            val selected = kotlin.math.abs(snapshot.brightnessPercent - percent) <= 5
            PresetChip(
              label = "$percent%",
              selected = selected,
              testTag = "panel_brightness_preset_$percent",
              onClick = { onBrightnessPreset(percent) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

private fun isTimeoutSelected(currentMs: Int, targetMs: Int): Boolean {
  return when (targetMs) {
    30_000 -> currentMs <= 45_000
    120_000 -> currentMs in 45_001..240_000
    else -> currentMs > 240_000
  }
}

@Composable
private fun PanelCircleAction(
  icon: ImageVector,
  label: String,
  testTag: String,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier =
      Modifier
        .width(78.dp)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .padding(vertical = 4.dp)
        .testTag(testTag),
  ) {
    Box(
      modifier =
        Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(SlateElevated)
          .border(1.dp, BorderSubtle, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = PureWhite,
        modifier = Modifier.size(24.dp),
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = SoftWhite,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun PresetChip(
  label: String,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .height(40.dp)
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(10.dp),
    color = if (selected) PureWhite else SlateTile,
    border = BorderStroke(1.dp, if (selected) PureWhite else BorderSubtle),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (selected) PitchBlack else SoftWhite,
        maxLines = 1,
      )
    }
  }
}
