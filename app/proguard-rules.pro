# Default rules are sufficient: the app uses no reflection-based serialization.

# Offline speech recognition (Vosk over JNA): native code calls these by name.
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keep class org.vosk.** { *; }
-dontwarn java.awt.**
