package com.example.weekendguide.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.weekendguide.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.graphics.Path

class MarkerIconRepositoryImpl(private val context: Context) : MarkerIconRepository {

    private val memoryBitmapCache = mutableMapOf<String, Bitmap>()

    override suspend fun preload(types: Set<String>) = withContext(Dispatchers.IO) {
        val visitedStates = listOf(true, false)
        val favoriteStates = listOf(true, false)
        types.forEach { type ->
            visitedStates.forEach { visited ->
                favoriteStates.forEach { favorite ->
                    val key = keyFor(type, visited, favorite)
                    if (memoryBitmapCache.containsKey(key)) return@forEach
                    val file = File(context.cacheDir, "marker_$key.png")
                    val bitmap: Bitmap? = if (file.exists()) {
                        try {
                            BitmapFactory.decodeFile(file.absolutePath)
                        } catch (_: Throwable) { null }
                    } else null

                    val bmp = bitmap ?: run {
                        val resId = getDrawableResId(type)
                        val created = createMarkerBitmapSafe(resId, visited, favorite)
                        try {
                            file.outputStream().use { out ->
                                created.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                        } catch (_: Throwable) {}
                        created
                    }

                    memoryBitmapCache[key] = bmp
                }
            }
        }
    }

    override fun getCachedBitmap(type: String, isVisited: Boolean, isFavorite: Boolean): Bitmap? {
        val key = keyFor(type, isVisited, isFavorite)
        return memoryBitmapCache[key]
    }

    private fun keyFor(type: String, visited: Boolean, favorite: Boolean) =
        "$type-$visited-$favorite"

    private fun createMarkerBitmapSafe(resId: Int?, isVisited: Boolean, isFavorite: Boolean): Bitmap {
        return try {
            createMarkerBitmap(resId, isVisited, isFavorite)
        } catch (_: Throwable) {
            val fallback = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            fallback.eraseColor(Color.GRAY)
            fallback
        }
    }

    private fun createMarkerBitmap(resId: Int?, isVisited: Boolean, isFavorite: Boolean): Bitmap {
        val sizePx = 120
        val center = sizePx / 2f

        val colorCircle = when {
            isVisited -> Color.parseColor("#4CAF50")  // green
            isFavorite -> Color.parseColor("#FFEB3B") // yellow
            else -> Color.parseColor("#2196F3")       // Google blue
        }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. marker
        val paintWhite = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val path = Path().apply {
            addCircle(center, center - sizePx * 0.1f, sizePx * 0.4f, Path.Direction.CW)
            moveTo(center - sizePx * 0.15f, center + sizePx * 0.2f)
            lineTo(center + sizePx * 0.15f, center + sizePx * 0.2f)
            lineTo(center, sizePx.toFloat())
            close()
        }
        canvas.drawPath(path, paintWhite)

        // 2. circle
        val paintColor = Paint().apply {
            color = colorCircle
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center - sizePx * 0.1f, sizePx * 0.3f, paintColor)

        // 3. icon

        val drawable: Drawable? = try {
            resId?.let { ContextCompat.getDrawable(context, it) }
        } catch (_: Throwable) {
            null
        }

        drawable?.let {
            it.mutate()
            it.setTint(Color.WHITE)

            val iconSize = (sizePx * 0.35).toInt()
            val left = (center - iconSize / 2).toInt()
            val top = (center - iconSize / 2 - sizePx * 0.1f).toInt()
            it.setBounds(left, top, left + iconSize, top + iconSize)
            try { it.draw(canvas) } catch (_: Throwable) {}
        }

        return bitmap
    }


    private fun getDrawableResId(type: String): Int? {
        return when (type) {
            "architecture" -> R.drawable.ic_architecture
            "nature" -> R.drawable.ic_nature
            "museum" -> R.drawable.ic_museum
            "fun" -> R.drawable.ic_fun
            "zoo" -> R.drawable.ic_zoo
            "water" -> R.drawable.ic_water
            "active" -> R.drawable.ic_active
            "hiking" -> R.drawable.ic_hiking
            "cycling" -> R.drawable.ic_cycling
            "culture" -> R.drawable.ic_culture
            else -> null
        }?.takeIf { it != 0 }
    }
}
