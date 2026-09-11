# Keep DataStore Preferences
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Keep Coroutines debug agent
-dontwarn kotlinx.coroutines.**

# Keep WorkManager Worker classes invoked via reflection
-keep class com.example.nightscreen.scheduling.ScheduleWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep preference data models
-keep class com.example.nightscreen.data.model.** { *; }

