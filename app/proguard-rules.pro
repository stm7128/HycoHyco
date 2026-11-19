# HycoHyco Proguard & R8 Rules

# 1. Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class com.github.stm7128.hycohyco.** { *; }
-keep,allowobfuscation,allowshrinking class com.github.stm7128.hycohyco.data.api.** { *; }
-keep,allowobfuscation,allowshrinking class com.github.stm7128.hycohyco.data.model.** { *; }
-keep,allowobfuscation,allowshrinking class com.github.stm7128.hycohyco.data.backup.** { *; }

# 2. Ktor Client & OkHttp
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }

# 3. AndroidX Media3 / ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# 4. Koin
-dontwarn org.koin.**
-keep class org.koin.** { *; }
