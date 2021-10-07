# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class nameRes to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file nameRes.
#-renamesourcefileattribute SourceFile

# Retrofit

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# OkHttp

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#Gson

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

## Android architecture componentsWrapper: Lifecycle
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}

-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keepclassmembers class androidx.arch.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.paging.** { *; }

-keep class androidx.arch.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.paging.** { *; }

-dontwarn androidx.arch.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.paging.**

-keep public class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager {
    public <init>(...);
}

 -keep class **.R$* {
         <fields>;
 }

# Response/Request models
-keep class com.belcobtm.data.rest.atm.response.** { *; }
-keep class com.belcobtm.data.rest.authorization.request.** { *; }
-keep class com.belcobtm.data.rest.authorization.response.** { *; }
-keep class com.belcobtm.data.rest.referral.request.** { *; }
-keep class com.belcobtm.data.rest.referral.response.** { *; }
-keep class com.belcobtm.data.rest.settings.request.** { *; }
-keep class com.belcobtm.data.rest.settings.response.** { *; }
-keep class com.belcobtm.data.rest.trade.request.** { *; }
-keep class com.belcobtm.data.rest.trade.response.** { *; }
-keep class com.belcobtm.data.rest.transaction.request.** { *; }
-keep class com.belcobtm.data.rest.transaction.response.** { *; }
-keep class com.belcobtm.data.rest.wallet.request.** { *; }
-keep class com.belcobtm.data.rest.wallet.response.** { *; }
-keep class com.belcobtm.data.rest.unlink.response.** { *; }
-keep class com.belcobtm.data.websockets.chat.model.** { *; }

# Objects serialized over Moshi
-keep class com.belcobtm.domain.settings.item.VerificationStateDataItem { *; }
-keep class com.belcobtm.domain.settings.item.VerificationCountryDataItem { *; }

-keep class com.belcobtm.presentation.core.extensions.** { *; }

# Wallet Core library
-keep class wallet.core.jni.** { *; }