-dontwarn **

-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-adaptresourcefilecontents

-keepattributes *Annotation*,Signature
-keep,allowoptimization public class com.launium.ghostify.client.mixin.**
-keepclassmembers,allowobfuscation public class com.launium.ghostify.client.mixin.** {
    *;
}
-keepclassmembers public class com.launium.ghostify.client.mixin.** {
    @org.spongepowered.asm.mixin.Shadow *;
}
-keepclassmembers,allowoptimization public class com.launium.ghostify.client.GhostifyClient {
    <methods>;
}
-keep,allowobfuscation,allowoptimization public class com.launium.ghostify.client.GhostifyModMenuIntegration {
    <methods>;
}

-keepclassmembers class * {
    *** on*(...);
    void method_***(***);
    void afterTick(...);
    void afterEntities(...);
    int compareTo(...);
    @com.launium.ghostify.client.annotations.SkipObfuscation public <methods>;
}

-repackageclasses lau4sk1d.ghostify
