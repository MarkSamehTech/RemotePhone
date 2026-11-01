package com.markstech.remotephone

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.view.KeyEvent as AndroidKeyEvent

/**
 * بتستقبل أوامر من [RemoteControlSession] وتحوّلها لإيماءات حقيقية على الشاشة.
 * الخدمة دي لازم تتفعّل يدويًا من المستخدم عبر: الإعدادات > تسهيل الاستخدام.
 */
class ControlAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ControlAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun execute(command: ControlCommand) {
        val metrics: DisplayMetrics = resources.displayMetrics
        val w = metrics.widthPixels
        val h = metrics.heightPixels

        when (command) {
            is ControlCommand.Tap -> tap(command.xRatio * w, command.yRatio * h)
            is ControlCommand.LongPress -> longPress(command.xRatio * w, command.yRatio * h)
            is ControlCommand.Swipe -> swipe(
                command.startXRatio * w, command.startYRatio * h,
                command.endXRatio * w, command.endYRatio * h,
                command.durationMs
            )
            is ControlCommand.KeyEvent -> performGlobalAction(mapKeyToGlobalAction(command.keyCode))
        }
    }

    private fun tap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun longPress(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 600))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long) {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun mapKeyToGlobalAction(code: Int): Int = when (code) {
        AndroidKeyEvent.KEYCODE_BACK -> GLOBAL_ACTION_BACK
        AndroidKeyEvent.KEYCODE_HOME -> GLOBAL_ACTION_HOME
        AndroidKeyEvent.KEYCODE_APP_SWITCH -> GLOBAL_ACTION_RECENTS
        else -> GLOBAL_ACTION_BACK
    }
}
