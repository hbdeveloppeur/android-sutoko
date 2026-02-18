# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class mname to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file mname.
#-renamesourcefileattribute SourceFile


-assumenosideeffects class fr.purpletear.sutoko.presentation.util.LogCompositionsKt

-keep class fr.purpletear.friendzone.** { *; }
-keep class fr.purpletear.friendzone2.** { *; }
-keep class friendzone3.purpletear.fr.friendzon3.** { *; }
-keep class fr.purpletear.friendzone4.** { *; }
-keep class com.purpletear.smsgame.** { *; }
-keep class com.purpletear.smartads.** { *; }
-keep class fr.purpletear.sutoko.shop.** { *; }
-keep class purpletear.fr.purpleteartools.** { *; }
-keep class com.universalvideoview.** { *; }
-keep class org.conscrypt.OpenSSLKey.** { *; }

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep interface androidx.annotation.Keep
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation


-dontwarn java.lang.invoke.StringConcatFactory