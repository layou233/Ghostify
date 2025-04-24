-dontwarn ***
-dontoptimize
-dontshrink

-keep public class com.launium.ghostify.client.mixin.*

-keepattributes Signature
-keep public class com.launium.ghostify.client.GhostifyClient {
    <methods>;
}

-keepclassmembers class * {
    *** on*(...);
    void method_***(***);
    void afterTick(...);
    int compareTo(...);
}

-repackageclasses lau4sk1d.ghostify
