# ============================================================
# Pani — ProGuard / R8 Rules
# Target: release APK < 15 MB on arm64-v8a + armeabi-v7a
# ============================================================

# ---------- Kotlin ----------
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { volatile <fields>; }
-dontwarn kotlin.**

# ---------- Kotlin Serialization ----------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** serializer(...);
}

# ---------- Hilt / Dagger ----------
-keep class dagger.hilt.** { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-dontwarn dagger.hilt.**

# ---------- Room ----------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.**

# ---------- CameraX ----------
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ---------- Media3 / ExoPlayer ----------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
# Required for ExoPlayer decoder selection on low-end devices
-keepclassmembers class * implements androidx.media3.common.util.UnstableApi { *; }

# ---------- Supabase / Ktor ----------
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
# Ktor uses coroutines internals
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# ---------- Retrofit / OkHttp ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ---------- Firebase ----------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
# Firebase Auth uses reflection for phone credential
-keepclassmembers class com.google.firebase.auth.** { *; }

# ---------- Coil ----------
-dontwarn coil.**

# ---------- Gson (used by Retrofit converter) ----------
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ---------- Domain / Data models (prevent stripping serializable fields) ----------
-keep class com.pani.app.data.local.db.entities.** { *; }
-keep class com.pani.app.data.remote.dto.** { *; }
-keep class com.pani.app.domain.model.** { *; }

# ---------- Suppress noisy warnings from indirect deps ----------
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
