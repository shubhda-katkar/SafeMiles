package com.example.safemiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Build;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) {
            Log.e("GeofenceReceiver", "Error receiving geofence event");
            return;
        }

        int transition = event.getGeofenceTransition();

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // 🎤 START VOICE SERVICE
            Intent serviceIntent = new Intent(context, VoiceService.class);
            serviceIntent.putExtra("message", "You are heading to a black spot. Drive carefully.");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }


            // (Your existing notification code can remain if you want a visual alert too)
        }
    }
}
