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

# Room – keep only the concrete database class and annotated entities
-keep class com.example.autoavp.data.local.AutoAvpDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Hilt – the library ships its own consumer proguard rules.
# Only keep app-side annotated ViewModels so Hilt can instantiate them.
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { <init>(...); }

# ML Kit – the library ships its own consumer proguard rules.
# Keep only the public result model classes used via reflection for barcode/text.
-keep class com.google.mlkit.vision.text.Text { *; }
-keep class com.google.mlkit.vision.text.Text$* { *; }
-keep class com.google.mlkit.vision.barcode.common.Barcode { *; }
-keep class com.google.mlkit.vision.barcode.common.Barcode$* { *; }
-dontwarn com.google.mlkit.**
