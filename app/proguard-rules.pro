# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\ToolDevelopment\AndroidStudioSDK/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

#-ignorewarnings
#
#-keep class * {
#    public private *;
#}

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes *Annotation*
-keepattributes Signature

-dontwarn com.kyleduo.**
-keep class com.kyleduo.** { *; }

-dontwarn com.squareup.picasso.**
-keep class com.squareup.picasso.** { *; }

-dontwarn com.cybozu.labs.langdetect.**
-keep class com.cybozu.labs.langdetect.** { *; }

-dontwarn com.android.support.**
-keep class com.android.support.** { *; }

-dontwarn org.junit.**
-keep class org.junit.** { *; }

-dontwarn javax.xml.**
-keep class javax.xml.** { *; }

-dontwarn org.hamcrest.**
-keep class org.hamcrest.** { *; }

-dontwarn com.google.common.**
#-keep class com.google.common { *; }
#
-dontwarn net.arnx.jsonic.**
#-keep class net.arnx.jsonic { *; }

## Google Play Services 4.3.23 specific rules ##
## https://developer.android.com/google/play-services/setup.html#Proguard ##

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

-printconfiguration config.txt