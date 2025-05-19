# Benchmark proguard rules
-dontoptimize
-dontusemixedcaseclassnames
-dontpreverify
-dontobfuscate

# Keep all benchmark related classes
-keep class androidx.benchmark.** { *; }
-keep class androidx.profileinstaller.** { *; }

# Don't warn about Kotlin/Compose
-dontwarn kotlinx.**
-dontwarn kotlin.**
-dontwarn org.jetbrains.kotlin.**
-dontwarn androidx.compose.**
