# Keep line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep data classes used by Room (entities)
-keep class com.example.torque.room.entity.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }

# Keep Sentry
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Gson/Serialization if added later
#-keepattributes Signature
#-keepattributes *Annotation*
