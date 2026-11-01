# RemotePhone

هيكل مشروع أندرويد (Kotlin) للتحكم عن بُعد بين جهازين عليهم نفس التطبيق،
عبر كود اقتران من 6 أرقام + WebRTC (P2P، فيديو الشاشة + قناة بيانات للتحكم باللمس).

## الخطوات المتبقية عشان يبقى شغال فعليًا

1. **Firebase**:
   - اعمل مشروع على https://console.firebase.google.com
   - فعّل Realtime Database (في وضع test mode مبدئيًا، وقيّد القواعد لاحقًا).
   - حمّل `google-services.json` وحطه في `app/`.
   - ضيف `apply plugin: 'com.google.gms.google-services'` (موجود بالفعل في build.gradle).

2. **WebRTC**:
   - أكمل `WebRTCClient.kt` (مش مبني لسه) عشان يربط:
     - `PeerConnectionFactory` + `PeerConnection`
     - `ScreenCapturerAndroid` كمصدر فيديو من `MediaProjection` (في `ScreenCaptureService`)
     - `DataChannel` لبعت/استقبال `ControlCommand.toJson()/fromJson()`
   - استخدم سيرفرات STUN عامة للبداية (`stun:stun.l.google.com:19302`)، وفي حالة الشبكات
     الصعبة (CGNAT) هتحتاج TURN server (مثلا Metered.ca له خطة مجانية محدودة).

3. **توليد `gradlew`**: المشروع محتاج Gradle Wrapper عشان الـ GitHub Action يشتغل:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   وادفع ملفات `gradlew`, `gradlew.bat`, `gradle/wrapper/*` مع الكود.

4. **الرفع على GitHub**:
   ```
   git init
   git remote add origin https://github.com/MarkSamehTech/RemotePhone.git
   git add .
   git commit -m "Initial RemotePhone skeleton"
   git push -u origin main
   ```
   بعد البوش، الـ Action هيبني APK تلقائيًا وتقدر تنزّله من تبويب Actions > Artifacts.

## ليه التطبيق مايتعتبرش "خطر" على أجهزة الأندرويد

- اطلب صلاحية Accessibility و MediaProjection بوصف واضح (موجود في
  `accessibility_service_config.xml` و `strings.xml`) — مش صلاحيات مموّهة.
- اعرض إشعار دائم (Foreground Notification) طول وقت مشاركة الشاشة — ده مطلوب
  من النظام نفسه من Android 10+ ومهم للشفافية.
- لو هتنشره على Google Play: صنّفه ضمن سياسة "Remote Support Apps" الخاصة
  بجوجل، واملأ Accessibility Declaration Form في Play Console بوضوح.
- متستخدمش تشفير/تمويه للكود (obfuscation) بشكل مبالغ فيه، ووقّع الـ APK
  بمفتاح ثابت معروف.
- خلي الكود مفتوح المصدر على الريبو (زي ما هو عندك) — ده بيقلل جدًا احتمال
  إن Play Protect أو مضاد الفيروسات يصنّفه كـ RAT ضار.

## ملاحظة مهمة

التطبيق ده بيدي تحكم كامل في جهاز تاني، فلازم الطرف التاني يكون عارف وموافق
وهو اللي بيفعّل Accessibility ويشارك شاشته بنفسه - الاستخدام المقصود هنا هو
دعم فني/تحكم بين جهازينك انت الشخصيين، مش تحكم في جهاز حد من غير علمه.
