# --- kotlinx.serialization -------------------------------------------------
# Serializers are found through generated companions; R8 must not rename them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class ** {
    public static ** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class uz.sharif.vocabbrain.**$$serializer { *; }
-keepclassmembers class uz.sharif.vocabbrain.** {
    *** Companion;
}

# Type-safe navigation routes are resolved by their serial names at runtime.
-keep class uz.sharif.vocabbrain.navigation.Screen { *; }
-keep class uz.sharif.vocabbrain.navigation.Screen$* { *; }

# --- PdfBox-Android -------------------------------------------------------
# Loads fonts and codecs by name from its bundled resources.
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**

# --- Room -----------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# --- Firestore ------------------------------------------------------------
# Documents are read field by field in this app, but the SDK still reflects on models.
-keepclassmembers class uz.sharif.vocabbrain.feature.word.data.local.WordEntity { *; }
