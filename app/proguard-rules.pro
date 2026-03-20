-renamesourcefileattribute SourceFile

# 保留所有 JNI class
-keep class kika.qwt9.inputmethod.Resource.** { *; }
-keep class kika.qwt9.inputmethod.** { *; }
-keep class com.iqqi.ime.** { *; }

# 保留 native method
-keepclasseswithmembernames class * {
    native <methods>;
}

# JNI classes
-keep class * {
    native <methods>;
}

# keep enum (JNI sometimes uses them)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#防止混淆導致engine crash
-keep class com.lib.cimenginecoreso.** { *; }

#需加入，因為cim有httpRequest
-keepclassmembers class com.lib.cimenginecoreso.** {
    native <methods>;
}