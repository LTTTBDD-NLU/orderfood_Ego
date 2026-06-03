# Firebase Realtime Database - giữ lại các model class để deserialize
-keepattributes Signature
-keepattributes *Annotation*

# Giữ tất cả model class (dùng cho Firebase getValue())
-keep class com.ego.restaurant.models.** { *; }

# Firebase Auth
-keepattributes EnclosingMethod
