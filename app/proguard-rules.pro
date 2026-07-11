# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

################################################################################
# kotlinx.serialization
################################################################################

-keepattributes *Annotation*, InnerClasses

# Keep generated serializers and the synthesized Companion / serializer() members
# for all project models annotated with @Serializable.
-keep,includedescriptorclasses class io.soma.cryptobook.**$$serializer { *; }
-keepclassmembers class io.soma.cryptobook.** {
    *** Companion;
}
-keepclasseswithmembers class io.soma.cryptobook.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# kotlinx-serialization-json specific.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all enums (serialized by name).
-keepclassmembers enum * { *; }

################################################################################
# OkHttp / Retrofit  https://square.github.io/okhttp/ & https://square.github.io/retrofit/
################################################################################

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# https://github.com/square/okhttp/blob/339732e3a1b78be5d792860109047f68a011b5eb/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro#L11-L14
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**

# Related to this issue on https://github.com/square/retrofit/issues/3880
-keep,allowobfuscation,allowshrinking class kotlin.Result
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit service interfaces are only ever implemented by a runtime java.lang.reflect.Proxy,
# so under R8 full mode's closed-world assumption R8 sees no implementor and rewrites the
# `(ApiService) retrofit.create(ApiService::class.java)` check-cast into an unconditional
# `throw new ClassCastException()`, crashing at startup. A plain `-keep` of the service
# interfaces (no allowobfuscation) marks them as externally instantiable, so R8 emits the
# real check-cast which the Proxy satisfies at runtime.
#
# NOTE: the standard `-if interface * { @retrofit2.http.* <methods>; } -keep,allowobfuscation
# interface <1>` rule (also shipped by Retrofit's own consumer rules) is NOT enough here — the
# `allowobfuscation` form does not defeat the cast-to-throw optimization. All Retrofit services
# in this project are named `*ApiService`; keep new ones matching this pattern.
-keep interface io.soma.cryptobook.**ApiService { *; }
