# This files contains the ProGuard configuration for official dependencies like Gson, Room, Retrofit, etc.


# Play services ------------------------------------
-keep class com.google.android.gms.providerinstaller.** { *; }
-keep class com.google.android.play.core.splitinstall.** { *; }
-keep class com.google.android.play.core.splitcompat.** { *; }
# Firebase END ------------------------------------

# GSON ------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
# GSON END ------------------------------------


# Conscrypt ------------------------------------
-keep class org.conscrypt.** { *; }
-keep class com.android.org.conscrypt.** { *; }
# Conscrypt END --------------------------------


# Coroutines ------------------------------------
-dontwarn java.util.concurrent.**
-dontwarn kotlinx.coroutines.**
# Coroutines END --------------------------------

# Compose ------------------------------------
-keep class androidx.compose.** { *; }
-keep class androidx.ui.** { *; }
# Compose END --------------------------------


# OKHTTP ------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.* { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
# OKHTTP END ------------------------------------

# DAGGER ------------------------------------
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keep class dagger.* { *; }
-keep class javax.inject.* { *; }

# DAGGER
-dontwarn com.google.errorprone.annotations.*
# DAGGER END ------------------------------------

# All other dependencies ------------------------------------
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.javax.net.ssl.**
-dontwarn org.openjsse.net.ssl.**
-dontwarn org.openjsse.sun.security.ssl.**
# All other dependencies END ------------------------------------

# Lottie ------------------------------------
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** {*;}
# Lottie END ------------------------------------


# Room ------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
# Room END ------------------------------------

# Glide ------------------------------------
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-keep enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
# END Glide --------------------------------


# Retrofit ------------------------------------
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
-dontwarn javax.annotation.**
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.http.* <fields>;
}
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# Retrofit END ------------------------------------

-dontwarn java.lang.invoke.StringConcatFactory