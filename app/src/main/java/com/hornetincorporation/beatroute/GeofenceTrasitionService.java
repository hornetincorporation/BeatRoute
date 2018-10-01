package com.hornetincorporation.beatroute;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;

public class GeofenceTrasitionService extends IntentService {
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetwork;
    boolean isConnected;
    BeetPointVisitLocalDB beetPointVisitLocalDB;
    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();

    public GeofenceTrasitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }
        String UID = intent.getStringExtra("UserID");
        String UNM = intent.getStringExtra("UserName");
        String EML = intent.getStringExtra("EmailID");
        String PHN = intent.getStringExtra("PhoneNumber");
        String OID = intent.getStringExtra("OfficialID");
        String OFR = intent.getStringExtra("Officer");
        //String UID = intent.getStringExtra("PhotoURL4mSU");

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Location location = geofencingEvent.getTriggeringLocation();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String slatlng = latitude + ", " + longitude;

            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences, UID, slatlng);

            // Send notification details as a String
            sendNotification(geofenceTransitionDetails, UID, UNM, EML, PHN, OID, OFR);
        }
    }

    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences, String UserID, String latlng) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesListName = new ArrayList<>();
        ArrayList<String> triggeringGeofencesListID = new ArrayList<>();
        ArrayList<String> Route = new ArrayList<>();
        ArrayList<String> Point = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesListName.add(geofence.getRequestId().split("~")[1]);
            triggeringGeofencesListID.add(geofence.getRequestId().split("~")[0]);
            Route.add(geofence.getRequestId().split("~")[1].split("'")[1]);
            Point.add(geofence.getRequestId().split("~")[1].split("'")[3]);
        }
        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            status = "Stayed for 10 secs in ";
            //Store beet point visits in db only when user stays for more than 10 secs
            final String DTime = DateFormat.getDateTimeInstance().format(new Date());
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                beetPointVisitLocalDB = new BeetPointVisitLocalDB(this);
                Cursor cursorTabExist = beetPointVisitLocalDB.checkTableExists();
                if (cursorTabExist.getCount() > 0) {
                    while (cursorTabExist.moveToNext()) {
                        if (cursorTabExist.getString(0).equals(Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME)) {
                            Cursor cursorBeetPointVisit = beetPointVisitLocalDB.getAllData();
                            if (cursorBeetPointVisit.getCount() > 0) {
                                while (cursorBeetPointVisit.moveToNext()) {
                                    Map<String, Object> beetpointvisit = new HashMap<>();
                                    beetpointvisit.put("BPVTransition", cursorBeetPointVisit.getString(3));
                                    beetpointvisit.put("BPVBeetPointID", cursorBeetPointVisit.getString(4));
                                    beetpointvisit.put("BPVBeetRouteNPoint", cursorBeetPointVisit.getString(5));
                                    beetpointvisit.put("BPVLocation", cursorBeetPointVisit.getString(6));
                                    beetpointvisit.put("BPVPoint", cursorBeetPointVisit.getString(7));
                                    beetpointvisit.put("BPVRoute", cursorBeetPointVisit.getString(8));

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("beetpointvisits/" + cursorBeetPointVisit.getString(2));
                                    databaseReference.child(cursorBeetPointVisit.getString(9)).setValue(beetpointvisit);
                                }
                            }
                            Integer deletedRows = beetPointVisitLocalDB.deleteAllData();
                        }
                    }
                }

                Map<String, Object> beetpointvisit = new HashMap<>();
                beetpointvisit.put("BPVTransition", status);
                beetpointvisit.put("BPVBeetPointID", TextUtils.join(", ", triggeringGeofencesListID));
                beetpointvisit.put("BPVBeetRouteNPoint", TextUtils.join(", ", triggeringGeofencesListName));
                beetpointvisit.put("BPVLocation", latlng);
                beetpointvisit.put("BPVPoint", TextUtils.join(", ", Point));
                beetpointvisit.put("BPVRoute", TextUtils.join(", ", Route));

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("beetpointvisits/" + UserID);
                databaseReference.child(getDateTimeInstance().format(new Date()).toString()).setValue(beetpointvisit);
            } else {
                //TODO: Save in local db
                boolean isInserted = beetPointVisitLocalDB.insertData(UserID, status, TextUtils.join(", ", triggeringGeofencesListID), TextUtils.join(", ", triggeringGeofencesListName), latlng, TextUtils.join(", ", Point), TextUtils.join(", ", Route), DTime);
            }
        }
        return status + TextUtils.join(", ", triggeringGeofencesListName);
    }

    private void sendNotification(String msg, String UserID, String UserN, String Email, String Phone, String OffID, String Officer) {
        Log.i(TAG, "sendNotification: " + msg);
        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(getApplicationContext(), msg, UserID, UserN, Email, Phone, OffID, Officer);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(Constants.MAINACTIVITY.MAINACTIVITY_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.GEOFENCE.GEOFENCE_NOTIFICATION_CHANNEL_ID, Constants.GEOFENCE.GEOFENCE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription(Constants.GEOFENCE.GEOFENCE_NOTIFICATION_CHANNEL_DESC);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 500, 100});
            notificationChannel.enableVibration(true);
            notificatioMng.createNotificationChannel(notificationChannel);
        }
        notificatioMng.notify(Constants.GEOFENCE.GEOFENCE_NOTIFICATION_ID, createNotification(msg, notificationPendingIntent, Constants.GEOFENCE.GEOFENCE_NOTIFICATION_CHANNEL_ID));
    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent, String sChannel) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, sChannel);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_round_swap_calls_24px)
                .setColor(Color.RED)
                .setTicker("Beat Route Notification from Hornet Incorporation")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(msg)
                .setContentText("Beat Route Notification from Hornet Incorporation")
                .setContentIntent(notificationPendingIntent)
                .setContentInfo(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
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