package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class QuickTouchPreferences(context: Context) {

  private val appContext = context.applicationContext
  private val prefs =
    appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun getCustomIconPath(): String? = prefs.getString(KEY_CUSTOM_ICON_PATH, null)

  fun loadCustomIconBitmap(): Bitmap? {
    val path = getCustomIconPath() ?: return null
    val file = File(path)
    if (!file.exists()) return null
    return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
  }

  fun saveCustomIconFromUri(uri: Uri): Bitmap? {
    return runCatching {
      val decoded = decodeScaledBitmap(uri, MAX_ICON_SIZE_PX) ?: return null
      val square = centerCropAndScale(decoded, TARGET_ICON_SIZE_PX)
      if (decoded !== square) {
        decoded.recycle()
      }
      val destFile = File(appContext.filesDir, CUSTOM_ICON_FILENAME)
      FileOutputStream(destFile).use { out ->
        square.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
      prefs.edit().putString(KEY_CUSTOM_ICON_PATH, destFile.absolutePath).apply()
      square
    }.getOrNull()
  }

  fun clearCustomIcon() {
    val path = getCustomIconPath()
    if (path != null) {
      runCatching { File(path).delete() }
    }
    prefs.edit().remove(KEY_CUSTOM_ICON_PATH).apply()
  }

  fun getBubblePosition(defaultX: Int, defaultY: Int): Pair<Int, Int> {
    val x = prefs.getInt(KEY_BUBBLE_X, defaultX)
    val y = prefs.getInt(KEY_BUBBLE_Y, defaultY)
    return x to y
  }

  fun saveBubblePosition(x: Int, y: Int, snappedRight: Boolean) {
    prefs.edit()
      .putInt(KEY_BUBBLE_X, x)
      .putInt(KEY_BUBBLE_Y, y)
      .putBoolean(KEY_BUBBLE_SNAPPED_RIGHT, snappedRight)
      .apply()
  }

  fun isBubbleSnappedRight(defaultValue: Boolean = true): Boolean =
    prefs.getBoolean(KEY_BUBBLE_SNAPPED_RIGHT, defaultValue)

  private fun decodeScaledBitmap(uri: Uri, maxDim: Int): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val source = ImageDecoder.createSource(appContext.contentResolver, uri)
      return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val width = info.size.width
        val height = info.size.height
        val largest = maxOf(width, height)
        if (largest > maxDim) {
          val ratio = maxDim.toFloat() / largest.toFloat()
          decoder.setTargetSize(
            (width * ratio).toInt().coerceAtLeast(1),
            (height * ratio).toInt().coerceAtLeast(1),
          )
        }
      }
    }

    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    appContext.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, boundsOptions)
    }
    val sampleSize = calculateInSampleSize(boundsOptions, maxDim, maxDim)
    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return appContext.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOptions)
    }
  }

  private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int,
  ): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
      val halfHeight = height / 2
      val halfWidth = width / 2
      while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  private fun centerCropAndScale(source: Bitmap, targetSize: Int): Bitmap {
    val size = min(source.width, source.height)
    val xOffset = (source.width - size) / 2
    val yOffset = (source.height - size) / 2
    val cropped = Bitmap.createBitmap(source, xOffset, yOffset, size, size)
    val scaled =
      if (size != targetSize) {
        Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
      } else {
        cropped
      }
    if (scaled !== cropped && cropped !== source) {
      cropped.recycle()
    }
    return scaled
  }

  companion object {
    private const val PREFS_NAME = "quicktouch_assist_prefs"
    private const val KEY_CUSTOM_ICON_PATH = "custom_icon_path"
    private const val KEY_BUBBLE_X = "bubble_x"
    private const val KEY_BUBBLE_Y = "bubble_y"
    private const val KEY_BUBBLE_SNAPPED_RIGHT = "bubble_snapped_right"
    private const val CUSTOM_ICON_FILENAME = "custom_bubble_icon.png"
    private const val MAX_ICON_SIZE_PX = 512
    private const val TARGET_ICON_SIZE_PX = 256
  }
}
