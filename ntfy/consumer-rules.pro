# Ntfy library ProGuard rules

# Keep the public API
-keep public class com.purpletear.ntfy.Ntfy { *; }
-keep public class com.purpletear.ntfy.NtfyClient { *; }
-keep public class com.purpletear.ntfy.NtfyConfig { *; }
-keep public class com.purpletear.ntfy.NtfyException { *; }

# Keep data classes
-keepclassmembers class com.purpletear.ntfy.NtfyConfig { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
