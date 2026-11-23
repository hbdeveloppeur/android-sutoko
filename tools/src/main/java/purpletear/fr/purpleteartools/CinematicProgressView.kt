package purpletear.fr.purpleteartools

import android.os.SystemClock
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ProgressBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CinematicProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val stroke = 2f * resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    private val ring = RectF()
    private var durationMillis = 0L
    private var elapsedMillis = 0L
    private var resumedAt = 0L
    private var running = false
    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            val elapsed = elapsedMillis + SystemClock.uptimeMillis() - resumedAt
            progress = (elapsed.toFloat() / durationMillis).coerceAtMost(0.99f)
            if (elapsed < durationMillis) postDelayed(this, 50)
        }
    }

    var progress: Float = 0f
        set(value) {
            field = if (value.isFinite()) value.coerceIn(0f, 1f) else 0f
            invalidate()
        }

    init {
        contentDescription = context.getString(R.string.cinematic_progress)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val safeArea = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.translationY = safeArea.top.toFloat()
            view.translationX = if (layoutDirection == LAYOUT_DIRECTION_RTL) safeArea.left.toFloat() else -safeArea.right.toFloat()
            insets
        }
    }

    fun start(durationMillis: Long) {
        stop()
        this.durationMillis = durationMillis
        elapsedMillis = 0
        progress = 0f
        visibility = VISIBLE
        resume()
    }

    fun pause() {
        if (!running) return
        elapsedMillis += SystemClock.uptimeMillis() - resumedAt
        running = false
        removeCallbacks(tick)
    }

    fun resume() {
        if (running || durationMillis <= 0 || elapsedMillis >= durationMillis) return
        running = true
        resumedAt = SystemClock.uptimeMillis()
        post(tick)
    }

    fun complete() {
        stop()
        progress = 1f
        visibility = INVISIBLE
    }

    fun stop() {
        running = false
        durationMillis = 0
        removeCallbacks(tick)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = (minOf(width, height) - stroke) / 2f
        ring.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius)
        paint.color = 0x26FFFFFF
        canvas.drawOval(ring, paint)
        if (progress > 0f) {
            paint.color = 0xB3FFFFFF.toInt()
            canvas.drawArc(ring, -90f, progress * 360f, false, paint)
        }
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = ProgressBar::class.java.name
        info.rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(
            AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT, 0f, 1f, progress
        )
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}
