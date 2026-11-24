package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val fadeAlpha = remember { Animatable(0f) }
  val entryScale = remember { Animatable(0.88f) }

  val pulseTransition = rememberInfiniteTransition(label = "splash_pulse")
  val pulseScale by
    pulseTransition.animateFloat(
      initialValue = 0.97f,
      targetValue = 1.045f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "splash_pulse_scale",
    )

  LaunchedEffect(Unit) {
    fadeAlpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    )
  }

  LaunchedEffect(Unit) {
    entryScale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    )
    delay(1850L)
    onSplashFinished()
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(PureBlack)
        .padding(32.dp)
        .testTag("splash_screen"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.alpha(fadeAlpha.value),
    ) {
      Image(
        painter = painterResource(id = R.drawable.monkey_emblem),
        contentDescription = stringResource(R.string.cd_app_logo),
        contentScale = ContentScale.Fit,
        modifier =
          Modifier
            .size(188.dp)
            .scale(entryScale.value * pulseScale)
            .clip(CircleShape),
      )
      Spacer(modifier = Modifier.height(26.dp))
      Text(
        text = stringResource(R.string.made_by_monkey),
        color = PureWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
      )
    }
  }
}
