package com.markstech.remotephone

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.markstech.remotephone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenCaptureService::class.java)
                .putExtra("resultCode", result.resultCode)
                .putExtra("data", result.data)
            startForegroundService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // وضع "الهدف" (الجهاز اللي هيتم التحكم فيه): توليد كود ومشاركة الشاشة
        binding.btnShareScreen.setOnClickListener {
            val code = SignalingClient.generateRoomCode()
            binding.tvRoomCode.text = code
            ensureAccessibilityEnabled()

            val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(manager.createScreenCaptureIntent())

            SignalingClient(code).listenForOffer { offerSdp ->
                // TODO: مرّر offerSdp لـ WebRTCClient، كوّن answer، ابعته بـ sendAnswer()
            }
        }

        // وضع "المتحكِّم": إدخال الكود والاتصال
        binding.btnConnect.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (TextUtils.isEmpty(code) || code.length != 6) {
                binding.etCode.error = "أدخل كود من 6 أرقام"
                return@setOnClickListener
            }
            val signaling = SignalingClient(code)
            // TODO: أنشئ WebRTCClient كـ "caller"، كوّن offer، ابعته بـ sendOffer()
            // وبعدين استقبل answer عبر listenForAnswer() وابدأ عرض الفيديو + إرسال ControlCommand
        }
    }

    private fun ensureAccessibilityEnabled() {
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        if (!enabledServices.contains(packageName)) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
