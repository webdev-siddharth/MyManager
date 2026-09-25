# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to those specified
# in the Android SDK tools proguard config.

# Readable stack traces in release builds
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room entities (instantiated reflectively by Room) ---
-keep class com.core2studio.mymanager.data.local.entity.** { *; }

# --- Firestore POJOs: doc.toObject(...) / .set(obj) map field names by reflection ---
# R8 renames these fields otherwise, so release builds write keys like "a","b"
# while updateProfile()/saveBusinessInfo() write literal keys such as "businessName".
-keep class com.core2studio.mymanager.data.auth.UserProfile { *; }

# --- Kotlinx serialization ---
# App models are (de)serialized via reflection: Json.encodeToString<T>() looks up
# Companion.serializer() and the generated $$serializer class by name.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.core2studio.mymanager.**$$serializer { *; }
-keepclassmembers class com.core2studio.mymanager.** {
    *** Companion;
}
-keepclasseswithmembers class com.core2studio.mymanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- OkHttp / Okio (warnings only; shipped by Firebase/Coil consumers) ---
-dontwarn okhttp3.**
-dontwarn okio.**
