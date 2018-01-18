# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/pzoli/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}



-keepclassmembers class com.webkey.** { *; }
-keepclassmembers class com.google.** { *; }

-dontwarn android.support.**
-dontwarn org.mockito.**
-dontwarn com.google.common.**
-dontwarn org.objenesis.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
}


#-dontobfuscate
#-dontoptimize

-dontusemixedcaseclassnames
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
