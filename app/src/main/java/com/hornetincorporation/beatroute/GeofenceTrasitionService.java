package com.hornetincorporation.beatroute;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;


public class GeofenceTrasitionService extends IntentService {

    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTrasitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }
        String UN = intent.getStringExtra("Username");
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences, UN);

            // Send notification details as a String
            sendNotification(geofenceTransitionDetails);
        }
    }


    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences, String UName) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesListName = new ArrayList<>();
        ArrayList<String> triggeringGeofencesListID = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesListName.add(geofence.getRequestId().split("~")[1]);
            triggeringGeofencesListID.add(geofence.getRequestId().split("~")[0]);
        }
        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
            status = "Stayed for 10 secs in ";

        //Store beet point visits in db
        Map<String, Object> beetpointvisit = new HashMap<>();
        beetpointvisit.put("BPVTransition", status);
        beetpointvisit.put("BPVBeetPointID", TextUtils.join(", ", triggeringGeofencesListID));
        beetpointvisit.put("BPVBeetRouteNPoint", TextUtils.join(", ", triggeringGeofencesListName));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("beetpointvisits/" + UName);
        databaseReference.child(getDateTimeInstance().format(new Date()).toString()).setValue(beetpointvisit);
        return status + TextUtils.join(", ", triggeringGeofencesListName);
    }

    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), msg
        );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificatioMng.createNotificationChannel(notificationChannel);
        }

        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent, NOTIFICATION_CHANNEL_ID));

    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent, String sChannel) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, sChannel);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_round_place_24px)
                .setColor(Color.RED)
                .setTicker("Geofence Notification!")
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setContentInfo("Info");

        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}