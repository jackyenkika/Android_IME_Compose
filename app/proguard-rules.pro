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
