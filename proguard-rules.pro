-dontwarn **

-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-adaptresourcefilecontents

-keepattributes *Annotation*,Signature
-keep,allowoptimization public class com.launium.ghostify.client.mixin.*
-keepclassmembers,allowobfuscation public class com.launium.ghostify.client.mixin.* {
    *;
}
-keepclassmembers,allowoptimization public class com.launium.ghostify.client.GhostifyClient {
    <methods>;
}

-keepclassmembers class * {
    *** on*(...);
    void method_***(***);
    void afterTick(...);
    int compareTo(...);
}

-repackageclasses lau4sk1d.ghostify
