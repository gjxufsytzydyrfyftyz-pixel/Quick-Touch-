package com.example.system

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.example.R
import com.example.service.QuickTouchNotificationListenerService
import com.example.service.QuickTouchStateHolder
import kotlin.math.min

data class MediaPlaybackSnapshot(
  val isVisible: Boolean = false,
  val isPlaying: Boolean = false,
  val title: String = "",
  val artist: String = "",
  val albumArt: Bitmap? = null,
  val packageName: String = "",
)

object MediaSessionController {

  private val mainHandler = Handler(Looper.getMainLooper())
  private var sessionManager: MediaSessionManager? = null
  private var activeController: MediaController? = null
  private var listenerComponent: ComponentName? = null
  private var isRegistered = false
  private var keepOpenAfterLocalPause = false

  private val sessionsChangedListener =
    MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
      updateFromControllers(controllers.orEmpty())
    }

  private val controllerCallback =
    object : MediaController.Callback() {
      override fun onPlaybackStateChanged(state: PlaybackState?) {
        refreshActiveMediaInternal()
      }

      override fun onMetadataChanged(metadata: MediaMetadata?) {
        refreshActiveMediaInternal()
      }

      override fun onSessionDestroyed() {
        keepOpenAfterLocalPause = false
        refreshActiveMediaInternal()
      }
    }

  fun isNotificationAccessEnabled(context: Context): Boolean {
    if (QuickTouchNotificationListenerService.isConnected) return true
    val expected =
      ComponentName(context, QuickTouchNotificationListenerService::class.java).flattenToString()
    val flat =
      Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        ?: return false
    return flat.split(':').any { it.equals(expected, ignoreCase = true) }
  }

  fun startListening(context: Context) {
    val appCtx = context.applicationContext
    if (!isNotificationAccessEnabled(appCtx)) {
      stopListening()
      return
    }
    val manager =
      (appCtx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager) ?: return
    val component = ComponentName(appCtx, QuickTouchNotificationListenerService::class.java)

    sessionManager = manager
    listenerComponent = component

    if (!isRegistered) {
      runCatching {
        manager.addOnActiveSessionsChangedListener(sessionsChangedListener, component, mainHandler)
        isRegistered = true
      }
    }
    refreshActiveMedia(appCtx)
  }

  fun stopListening() {
    runCatching { activeController?.unregisterCallback(controllerCallback) }
    activeController = null
    if (isRegistered) {
      runCatching { sessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener) }
      isRegistered = false
    }
    keepOpenAfterLocalPause = false
    QuickTouchStateHolder.updateMediaSnapshot(MediaPlaybackSnapshot())
  }

  fun resetTransientPauseFlag() {
    keepOpenAfterLocalPause = false
    refreshActiveMediaInternal()
  }

  fun refreshActiveMedia(context: Context) {
    val appCtx = context.applicationContext
    if (!isNotificationAccessEnabled(appCtx)) {
      QuickTouchStateHolder.updateMediaSnapshot(MediaPlaybackSnapshot())
      return
    }
    if (sessionManager == null) {
      sessionManager =
        appCtx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    }
    if (listenerComponent == null) {
      listenerComponent = ComponentName(appCtx, QuickTouchNotificationListenerService::class.java)
    }
    refreshActiveMediaInternal(appCtx)
  }

  fun skipToPrevious() {
    runCatching { activeController?.transportControls?.skipToPrevious() }
  }

  fun togglePlayPause() {
    val controller = activeController ?: return
    val state = controller.playbackState?.state
    val currentlyPlaying =
      state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
    runCatching {
      if (currentlyPlaying) {
        keepOpenAfterLocalPause = true
        controller.transportControls.pause()
      } else {
        keepOpenAfterLocalPause = false
        controller.transportControls.play()
      }
    }
  }

  fun skipToNext() {
    runCatching { activeController?.transportControls?.skipToNext() }
  }

  private fun refreshActiveMediaInternal(context: Context? = null) {
    val manager = sessionManager ?: return
    val component = listenerComponent ?: return
    val controllers = runCatching { manager.getActiveSessions(component) }.getOrNull() ?: return
    updateFromControllers(controllers, context)
  }

  private fun updateFromControllers(
    controllers: List<MediaController>,
    context: Context? = null,
  ) {
    val playingController =
      controllers.firstOrNull { ctrl ->
        val st = ctrl.playbackState?.state
        st == PlaybackState.STATE_PLAYING || st == PlaybackState.STATE_BUFFERING
      }

    val chosen =
      when {
        playingController != null -> playingController
        keepOpenAfterLocalPause && activeController != null -> {
          val matchingPaused =
            controllers.firstOrNull { it.sessionToken == activeController?.sessionToken }
          val st = matchingPaused?.playbackState?.state
          if (st == PlaybackState.STATE_PAUSED) matchingPaused else null
        }
        else -> null
      }

    if (chosen == null) {
      keepOpenAfterLocalPause = false
      runCatching { activeController?.unregisterCallback(controllerCallback) }
      activeController = null
      QuickTouchStateHolder.updateMediaSnapshot(MediaPlaybackSnapshot())
      return
    }

    if (activeController?.sessionToken != chosen.sessionToken) {
      runCatching { activeController?.unregisterCallback(controllerCallback) }
      activeController = chosen
      runCatching { chosen.registerCallback(controllerCallback, mainHandler) }
    }

    val state = chosen.playbackState?.state
    val isPlaying = state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
    val metadata = chosen.metadata

    val rawTitle =
      metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        ?: ""
    val rawArtist =
      metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        ?: ""

    val rawBitmap =
      metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

    val scaledArt = rawBitmap?.let { scaleDownArtwork(it, 128) }
    val fallbackTitle = context?.getString(R.string.media_unknown_track) ?: "Playing media"

    QuickTouchStateHolder.updateMediaSnapshot(
      MediaPlaybackSnapshot(
        isVisible = true,
        isPlaying = isPlaying,
        title = rawTitle.ifBlank { fallbackTitle },
        artist = rawArtist.trim(),
        albumArt = scaledArt,
        packageName = chosen.packageName.orEmpty(),
      ),
    )
  }

  private fun scaleDownArtwork(source: Bitmap, maxDim: Int): Bitmap {
    if (source.isRecycled) return source
    val size = min(source.width, source.height)
    if (source.width <= maxDim && source.height <= maxDim) {
      return source.copy(Bitmap.Config.ARGB_8888, false) ?: source
    }
    val xOffset = (source.width - size) / 2
    val yOffset = (source.height - size) / 2
    val square = Bitmap.createBitmap(source, xOffset, yOffset, size, size)
    val scaled = Bitmap.createScaledBitmap(square, maxDim, maxDim, true)
    if (scaled !== square) {
      square.recycle()
    }
    return scaled
  }
}
