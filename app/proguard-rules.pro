# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Analytics specific rules
-keep class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keep class com.google.firebase.analytics.connector.AnalyticsConnector { *; }

# Room database rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep all entity classes
-keep class com.pseddev.singventory.model.** { *; }

# Navigation component rules
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.navigation.NavArgs
-keep class * extends androidx.navigation.NavArgsLazy

# ViewBinding and DataBinding rules
-keep class * extends androidx.viewbinding.ViewBinding {
    public static *** inflate(android.view.LayoutInflater);
    public static *** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static *** bind(android.view.View);
}

# Fragment and Activity rules
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.appcompat.app.AppCompatActivity

# Preserve all annotations
-keepattributes *Annotation*

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AndroidX and Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-keep class androidx.** { *; }
-dontwarn androidx.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Kotlin metadata for proper R8 optimization
-keep class kotlin.Metadata { *; }

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}