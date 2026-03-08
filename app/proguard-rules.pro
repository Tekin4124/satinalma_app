# Uygulamaya özel ProGuard kuralları

# Hilt
-keepattributes *Annotation*
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Data classes
-keepclassmembers class com.tekin.satinalma.domain.model.** {
    *;
}

# Enum'lar
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Navigation
-keep class androidx.navigation.** { *; }
