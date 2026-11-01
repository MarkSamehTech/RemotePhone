package com.markstech.remotephone

import android.app.*
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * فورغراوند سيرفس إجباري لاستخدام MediaProjection على Android 10+.
 * بيبعت الإشعار الدائم اللي بيوضّح للمستخدم إن شاشته بتتسجّل/تتشارك الآن —
 * مطلوب من أندرويد نفسه ومهم عشان الشفافية (ومنع اعتبار التطبيق خبيث).
 */
class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = manager.getMediaProjection(resultCode, data)

        // TODO: اربط mediaProjection هنا بـ WebRTCClient لإنشاء VideoTrack من الشاشة
        // عبر VideoCapturer مخصص (ScreenCapturerAndroid من مكتبة webrtc-sdk).

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val channelId = "screen_share_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "مشاركة الشاشة", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("جلسة تحكم عن بُعد نشطة")
            .setContentText("جهازك متصل ومشاركة شاشته حاليًا")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        mediaProjection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
