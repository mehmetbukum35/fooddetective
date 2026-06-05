# Project-specific R8 / ProGuard rules.
# Release builds enable code shrinking and resource shrinking in app/build.gradle.kts.

# Keep useful stack traces for crash reports and Play Console diagnostics.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Retrofit uses annotations and generic signatures to create API implementations.
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# OkHttp / Okio optional platform classes may not exist on Android.
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson models used by Retrofit/Room sync should keep their field names.
-keep class com.mehmetbukum.fooddetective.data.** { *; }

# Room already ships consumer rules, but keeping database/DAO classes is safer for release builds.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ML Kit / Google Play Services optional classes.
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# CameraX optional implementation warnings.
-dontwarn androidx.camera.**
