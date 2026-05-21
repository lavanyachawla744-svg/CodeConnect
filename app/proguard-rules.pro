# Keep OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
