# Add project specific ProGuard rules here.

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.saicomputer.sms.**$$serializer { *; }
-keepclassmembers class com.saicomputer.sms.** {
    *** Companion;
}
-keepclasseswithmembers class com.saicomputer.sms.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, Exceptions

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# WebView JS interface (PDF preview)
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
