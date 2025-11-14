package com.example.safemiles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class VoiceService extends Service {

    private TextToSpeech tts;
    private static final String CHANNEL_ID = "voice_alert_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createVoiceChannel();

        // Start foreground notification (required for Android 8+)
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeMiles Voice Alert")
                .setContentText("Preparing voice alert…")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        startForeground(1, notification);

        // Initialize TTS
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                speak();
            }
        });
    }

    private void speak() {
        if (tts != null) {
            tts.speak(
                    "You are heading to a black spot. Drive carefully.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "tts1"
            );

            // Stop service after 5 seconds
            new android.os.Handler().postDelayed(this::stopSelf, 5000);
        }
    }

    private void createVoiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SafeMiles Voice Alerts",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
