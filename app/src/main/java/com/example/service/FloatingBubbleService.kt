package com.example.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.QuickTouchPreferences
import com.example.system.MediaSessionController
import com.example.system.SystemController
import com.example.ui.components.BubbleAvatar
import com.example.ui.components.CompactPillTray
import com.example.ui.components.FullControlPanelContent
import com.example.ui.components.MiniMediaPlayerCard
import com.example.ui.theme.QuickTouchTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FloatingBubbleService : Service() {

  private lateinit var windowManager: WindowManager
  private lateinit var prefs: QuickTouchPreferences
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private var currentOverlayView: View? = null
  private var currentLifecycleOwner: OverlayLifecycleOwner? = null
  private var bubbleParams: WindowManager.LayoutParams? = null
  private var snapAnimator: ValueAnimator? = null

  private var bubbleX: Int = 0
  private var bubbleY: Int = 280
  private var isSnappedRight: Boolean = true
  private var edgeMarginPx: Int = 16
  private var bubbleSizePx: Int = 160

  override fun onCreate() {
    super.onCreate()
    windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    prefs = QuickTouchPreferences(this)

    val density = resources.displayMetrics.density
    edgeMarginPx = (10 * density).roundToInt()
    bubbleSizePx = (56 * density).roundToInt()

    val (screenW, _) = getScreenDimensions()
    val defaultX = (screenW - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
    val defaultY = (200 * density).roundToInt()
    val savedPos = prefs.getBubblePosition(defaultX, defaultY)
    isSnappedRight = prefs.isBubbleSnappedRight(true)
    bubbleX = if (isSnappedRight) defaultX else edgeMarginPx
    bubbleY = savedPos.second.coerceAtLeast(edgeMarginPx)

    QuickTouchStateHolder.setSnappedRight(isSnappedRight)
    if (QuickTouchStateHolder.state.value.customIconBitmap == null) {
      QuickTouchStateHolder.setCustomIconBitmap(prefs.loadCustomIconBitmap())
    }
    QuickTouchStateHolder.setOverlayServiceRunning(true)
    MediaSessionController.startListening(this)

    startForegroundNotification()

    serviceScope.launch {
      QuickTouchStateHolder.state
        .map { it.overlayStage }
        .distinctUntilChanged()
        .collect { stage ->
          if (Settings.canDrawOverlays(this@FloatingBubbleService)) {
            renderStage(stage)
          } else {
            stopSelf()
          }
        }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (!Settings.canDrawOverlays(this)) {
      stopSelf()
      return START_NOT_STICKY
    }

    when (intent?.action) {
      ACTION_STOP -> {
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_SHOW_EXPANDED -> {
        SystemController.refreshAllState(this)
        QuickTouchStateHolder.setOverlayStage(OverlayStage.EXPANDED)
      }
      ACTION_COLLAPSE -> {
        QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
      }
      else -> {
        if (currentOverlayView == null) {
          renderStage(QuickTouchStateHolder.state.value.overlayStage)
        }
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    removeCurrentOverlay()
    QuickTouchStateHolder.setOverlayServiceRunning(false)
    QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
    serviceScope.cancel()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun startForegroundNotification() {
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
          CHANNEL_ID,
          getString(R.string.overlay_notification_channel_name),
          NotificationManager.IMPORTANCE_MIN,
        ).apply { setShowBadge(false) }
      manager.createNotificationChannel(channel)
    }

    val openAppIntent =
      Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
    val pendingIntent =
      PendingIntent.getActivity(
        this,
        0,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val notification: Notification =
      NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_quicktouch_notif)
        .setContentTitle(getString(R.string.overlay_notification_title))
        .setContentText(getString(R.string.overlay_notification_text))
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .build()

    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    }
  }

  private fun overlayWindowType(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }
  }

  private fun renderStage(stage: OverlayStage) {
    removeCurrentOverlay()
    when (stage) {
      OverlayStage.BUBBLE -> {
        MediaSessionController.resetTransientPauseFlag()
        showCollapsedBubble()
      }
      OverlayStage.PILL -> {
        MediaSessionController.refreshActiveMedia(this)
        showHorizontalPillTray()
      }
      OverlayStage.EXPANDED -> {
        MediaSessionController.resetTransientPauseFlag()
        showExpandedControlPanel()
      }
    }
  }

  private fun showCollapsedBubble() {
    val (screenW, screenH) = getScreenDimensions()
    val clampedX =
      if (isSnappedRight) {
        (screenW - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
      } else {
        edgeMarginPx
      }
    val clampedY = bubbleY.coerceIn(edgeMarginPx, (screenH - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx))
    bubbleX = clampedX
    bubbleY = clampedY

    val params =
      WindowManager.LayoutParams(
        bubbleSizePx,
        bubbleSizePx,
        overlayWindowType(),
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
      ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = bubbleX
        y = bubbleY
      }
    bubbleParams = params

    val lifecycleOwner = createLifecycleOwner()
    val composeView =
      createOverlayComposeView(lifecycleOwner) {
        val sharedState by QuickTouchStateHolder.state.collectAsState()
        BubbleAvatar(
          customBitmap = sharedState.customIconBitmap,
          size = 56.dp,
          modifier =
            Modifier.clickable {
              SystemController.refreshAllState(this@FloatingBubbleService)
              MediaSessionController.refreshActiveMedia(this@FloatingBubbleService)
              QuickTouchStateHolder.setOverlayStage(OverlayStage.PILL)
            },
        )
      }

    val container =
      DraggableBubbleContainer(
        context = this,
        onDragMove = { newX, newY ->
          val (w, h) = getScreenDimensions()
          val maxW = (w - (currentOverlayView?.width?.takeIf { it > 0 } ?: bubbleSizePx) - edgeMarginPx)
            .coerceAtLeast(edgeMarginPx)
          val maxH = (h - (currentOverlayView?.height?.takeIf { it > 0 } ?: bubbleSizePx) - edgeMarginPx)
            .coerceAtLeast(edgeMarginPx)
          params.x = newX.coerceIn(edgeMarginPx, maxW)
          params.y = newY.coerceIn(edgeMarginPx, maxH)
          bubbleX = params.x
          bubbleY = params.y
          runCatching { windowManager.updateViewLayout(currentOverlayView, params) }
        },
        onDragEnd = { snapToNearestEdge(params) },
      ).apply { addView(composeView) }

    attachOverlayView(container, params, lifecycleOwner)
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun showHorizontalPillTray() {
    val (screenW, screenH) = getScreenDimensions()
    val clampedY = bubbleY.coerceIn(edgeMarginPx, (screenH - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx))

    val params =
      WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        overlayWindowType(),
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT,
      ).apply {
        gravity = Gravity.TOP or (if (isSnappedRight) Gravity.END else Gravity.START)
        x = edgeMarginPx
        y = clampedY
      }

    val lifecycleOwner = createLifecycleOwner()
    val composeView =
      createOverlayComposeView(lifecycleOwner) {
        val sharedState by QuickTouchStateHolder.state.collectAsState()
        val maxPillWidthDp =
          ((screenW / resources.displayMetrics.density) - 88f).coerceAtLeast(220f).dp
        val cardWidthDp = minOf(296.dp, maxPillWidthDp)

        val pillAndMediaColumn: @Composable (Alignment.Horizontal) -> Unit = { horizontalAlign ->
          Column(horizontalAlignment = horizontalAlign) {
            CompactPillTray(
              isMuted = sharedState.systemSnapshot.isMuted,
              onVolumeUp = { SystemController.volumeUp(this@FloatingBubbleService) },
              onMuteToggle = { SystemController.toggleMute(this@FloatingBubbleService) },
              onLockScreen = {
                QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
                SystemController.performLockScreen(this@FloatingBubbleService)
              },
              onPowerMenu = {
                QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
                SystemController.performPowerMenu(this@FloatingBubbleService)
              },
              onHome = {
                QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
                SystemController.performHome(this@FloatingBubbleService)
              },
              onSettings = {
                QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
                SystemController.openSystemSettings(this@FloatingBubbleService)
              },
              onExpand = {
                SystemController.refreshAllState(this@FloatingBubbleService)
                QuickTouchStateHolder.setOverlayStage(OverlayStage.EXPANDED)
              },
              modifier = Modifier.widthIn(max = maxPillWidthDp),
            )

            AnimatedVisibility(
              visible = sharedState.mediaSnapshot.isVisible,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically(),
            ) {
              Column {
                Spacer(modifier = Modifier.height(8.dp))
                MiniMediaPlayerCard(
                  media = sharedState.mediaSnapshot,
                  onPrevious = { MediaSessionController.skipToPrevious() },
                  onPlayPause = { MediaSessionController.togglePlayPause() },
                  onNext = { MediaSessionController.skipToNext() },
                  modifier = Modifier.width(cardWidthDp),
                )
              }
            }
          }
        }

        val bubbleContent: @Composable () -> Unit = {
          BubbleAvatar(
            customBitmap = sharedState.customIconBitmap,
            size = 56.dp,
            modifier =
              Modifier.clickable {
                QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
              },
          )
        }

        Row(
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.Center,
        ) {
          if (sharedState.isSnappedRight) {
            pillAndMediaColumn(Alignment.End)
            Spacer(modifier = Modifier.width(8.dp))
            bubbleContent()
          } else {
            bubbleContent()
            Spacer(modifier = Modifier.width(8.dp))
            pillAndMediaColumn(Alignment.Start)
          }
        }
      }

    composeView.setOnTouchListener { _, event ->
      if (event.action == MotionEvent.ACTION_OUTSIDE) {
        QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
        true
      } else {
        false
      }
    }

    attachOverlayView(composeView, params, lifecycleOwner)
  }

  private fun showExpandedControlPanel() {
    val params =
      WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        overlayWindowType(),
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
      ).apply { gravity = Gravity.CENTER }

    val lifecycleOwner = createLifecycleOwner()
    val composeView =
      createOverlayComposeView(lifecycleOwner) {
        val sharedState by QuickTouchStateHolder.state.collectAsState()
        val panelInteraction = remember { MutableInteractionSource() }
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.56f))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) { QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE) }
              .padding(horizontal = 20.dp, vertical = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          FullControlPanelContent(
            snapshot = sharedState.systemSnapshot,
            onClose = { QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE) },
            onLockScreen = {
              QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
              SystemController.performLockScreen(this@FloatingBubbleService)
            },
            onPowerMenu = {
              QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
              SystemController.performPowerMenu(this@FloatingBubbleService)
            },
            onCycleBright = {
              val current = sharedState.systemSnapshot.brightnessPercent
              val next =
                when {
                  current < 20 -> 25
                  current < 55 -> 60
                  current < 90 -> 100
                  else -> 25
                }
              SystemController.setBrightnessPercent(this@FloatingBubbleService, next)
            },
            onSelectTimeout = { timeoutMs ->
              SystemController.setScreenTimeoutMs(this@FloatingBubbleService, timeoutMs)
            },
            onSettings = {
              QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
              SystemController.openSystemSettings(this@FloatingBubbleService)
            },
            onHome = {
              QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
              SystemController.performHome(this@FloatingBubbleService)
            },
            onVolumeChanged = { percent ->
              SystemController.setVolumePercent(this@FloatingBubbleService, percent, showUi = false)
            },
            onVolumePreset = { percent ->
              SystemController.setVolumePercent(this@FloatingBubbleService, percent, showUi = true)
            },
            onBrightnessChanged = { percent ->
              if (SystemController.canWriteSettings(this@FloatingBubbleService)) {
                SystemController.setBrightnessPercent(this@FloatingBubbleService, percent)
              }
            },
            onBrightnessChangeFinished = { percent ->
              SystemController.setBrightnessPercent(this@FloatingBubbleService, percent)
            },
            onBrightnessPreset = { percent ->
              SystemController.setBrightnessPercent(this@FloatingBubbleService, percent)
            },
            modifier =
              Modifier.clickable(
                interactionSource = panelInteraction,
                indication = null,
              ) {},
          )
        }
      }

    composeView.isFocusableInTouchMode = true
    composeView.setOnKeyListener { _, keyCode, event ->
      if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
        QuickTouchStateHolder.setOverlayStage(OverlayStage.BUBBLE)
        true
      } else {
        false
      }
    }

    attachOverlayView(composeView, params, lifecycleOwner)
    composeView.requestFocus()
  }

  private fun snapToNearestEdge(params: WindowManager.LayoutParams) {
    val view = currentOverlayView ?: return
    val (screenW, _) = getScreenDimensions()
    val viewW = view.width.takeIf { it > 0 } ?: bubbleSizePx
    val centerX = params.x + viewW / 2
    val snapRight = centerX >= screenW / 2
    val targetX = if (snapRight) (screenW - viewW - edgeMarginPx).coerceAtLeast(edgeMarginPx) else edgeMarginPx

    isSnappedRight = snapRight
    QuickTouchStateHolder.setSnappedRight(snapRight)

    snapAnimator?.cancel()
    val startX = params.x
    snapAnimator =
      ValueAnimator.ofInt(startX, targetX).apply {
        duration = 210L
        interpolator = DecelerateInterpolator()
        addUpdateListener { anim ->
          val v = currentOverlayView ?: return@addUpdateListener
          params.x = anim.animatedValue as Int
          bubbleX = params.x
          runCatching { windowManager.updateViewLayout(v, params) }
        }
        start()
      }
    bubbleX = targetX
    bubbleY = params.y
    prefs.saveBubblePosition(targetX, params.y, snapRight)
  }

  private fun createLifecycleOwner(): OverlayLifecycleOwner {
    return OverlayLifecycleOwner().apply {
      onCreate()
      onResume()
    }
  }

  private fun createOverlayComposeView(
    lifecycleOwner: OverlayLifecycleOwner,
    content: @Composable () -> Unit,
  ): ComposeView {
    return ComposeView(this).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
      setViewTreeLifecycleOwner(lifecycleOwner)
      setViewTreeViewModelStoreOwner(lifecycleOwner)
      setViewTreeSavedStateRegistryOwner(lifecycleOwner)
      setContent { QuickTouchTheme { content() } }
    }
  }

  private fun attachOverlayView(
    view: View,
    params: WindowManager.LayoutParams,
    lifecycleOwner: OverlayLifecycleOwner,
  ) {
    view.setViewTreeLifecycleOwner(lifecycleOwner)
    view.setViewTreeViewModelStoreOwner(lifecycleOwner)
    view.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
    currentOverlayView = view
    currentLifecycleOwner = lifecycleOwner
    runCatching { windowManager.addView(view, params) }
  }

  private fun removeCurrentOverlay() {
    snapAnimator?.cancel()
    snapAnimator = null
    currentLifecycleOwner?.let {
      runCatching {
        it.onPause()
        it.onDestroy()
      }
    }
    currentLifecycleOwner = null
    currentOverlayView?.let { view ->
      runCatching { windowManager.removeViewImmediate(view) }
    }
    currentOverlayView = null
    bubbleParams = null
  }

  private fun getScreenDimensions(): Pair<Int, Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val bounds = windowManager.currentWindowMetrics.bounds
      bounds.width() to bounds.height()
    } else {
      val metrics = DisplayMetrics()
      @Suppress("DEPRECATION")
      windowManager.defaultDisplay.getMetrics(metrics)
      metrics.widthPixels to metrics.heightPixels
    }
  }

  private inner class DraggableBubbleContainer(
    context: Context,
    private val onDragMove: (Int, Int) -> Unit,
    private val onDragEnd: () -> Unit,
  ) : FrameLayout(context) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downRawX = 0f
    private var downRawY = 0f
    private var startWindowX = 0
    private var startWindowY = 0
    private var isDragging = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
      when (ev.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
          snapAnimator?.cancel()
          downRawX = ev.rawX
          downRawY = ev.rawY
          startWindowX = bubbleParams?.x ?: bubbleX
          startWindowY = bubbleParams?.y ?: bubbleY
          isDragging = false
          return false
        }
        MotionEvent.ACTION_MOVE -> {
          val dx = abs(ev.rawX - downRawX)
          val dy = abs(ev.rawY - downRawY)
          if (dx > touchSlop || dy > touchSlop) {
            isDragging = true
            return true
          }
        }
      }
      return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
      when (event.actionMasked) {
        MotionEvent.ACTION_MOVE -> {
          val dx = (event.rawX - downRawX).roundToInt()
          val dy = (event.rawY - downRawY).roundToInt()
          onDragMove(startWindowX + dx, startWindowY + dy)
          return true
        }
        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_CANCEL -> {
          if (isDragging) {
            isDragging = false
            onDragEnd()
            return true
          }
        }
      }
      return super.onTouchEvent(event)
    }
  }

  companion object {
    const val ACTION_START = "com.example.action.START_OVERLAY"
    const val ACTION_STOP = "com.example.action.STOP_OVERLAY"
    const val ACTION_SHOW_EXPANDED = "com.example.action.SHOW_EXPANDED"
    const val ACTION_COLLAPSE = "com.example.action.COLLAPSE"

    private const val CHANNEL_ID = "quicktouch_overlay_channel"
    private const val NOTIFICATION_ID = 4091

    fun start(context: Context) {
      if (!Settings.canDrawOverlays(context)) return
      val intent = Intent(context, FloatingBubbleService::class.java).apply { action = ACTION_START }
      ContextCompat.startForegroundService(context, intent)
    }

    fun showExpanded(context: Context) {
      if (!Settings.canDrawOverlays(context)) return
      val intent =
        Intent(context, FloatingBubbleService::class.java).apply { action = ACTION_SHOW_EXPANDED }
      ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
      context.stopService(Intent(context, FloatingBubbleService::class.java))
    }
  }
}
