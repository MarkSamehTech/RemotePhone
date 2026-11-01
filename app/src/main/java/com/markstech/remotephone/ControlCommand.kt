package com.markstech.remotephone

import org.json.JSONObject

/**
 * أمر تحكم بسيط يُرسَل عبر WebRTC DataChannel من جهاز "المتحكِّم" إلى جهاز "الهدف".
 * الإحداثيات نسبية (0.0 - 1.0) عشان تشتغل صح مهما اختلفت دقة شاشة الجهازين.
 */
sealed class ControlCommand {
    data class Tap(val xRatio: Float, val yRatio: Float) : ControlCommand()
    data class Swipe(
        val startXRatio: Float, val startYRatio: Float,
        val endXRatio: Float, val endYRatio: Float,
        val durationMs: Long
    ) : ControlCommand()
    data class LongPress(val xRatio: Float, val yRatio: Float) : ControlCommand()
    data class KeyEvent(val keyCode: Int) : ControlCommand() // رجوع/الرئيسية/حديثة

    fun toJson(): String = when (this) {
        is Tap -> JSONObject().put("type", "tap").put("x", xRatio).put("y", yRatio).toString()
        is Swipe -> JSONObject().put("type", "swipe")
            .put("x1", startXRatio).put("y1", startYRatio)
            .put("x2", endXRatio).put("y2", endYRatio)
            .put("duration", durationMs).toString()
        is LongPress -> JSONObject().put("type", "long_press").put("x", xRatio).put("y", yRatio).toString()
        is KeyEvent -> JSONObject().put("type", "key").put("code", keyCode).toString()
    }

    companion object {
        fun fromJson(raw: String): ControlCommand? {
            val o = JSONObject(raw)
            return when (o.getString("type")) {
                "tap" -> Tap(o.getDouble("x").toFloat(), o.getDouble("y").toFloat())
                "swipe" -> Swipe(
                    o.getDouble("x1").toFloat(), o.getDouble("y1").toFloat(),
                    o.getDouble("x2").toFloat(), o.getDouble("y2").toFloat(),
                    o.getLong("duration")
                )
                "long_press" -> LongPress(o.getDouble("x").toFloat(), o.getDouble("y").toFloat())
                "key" -> KeyEvent(o.getInt("code"))
                else -> null
            }
        }
    }
}
