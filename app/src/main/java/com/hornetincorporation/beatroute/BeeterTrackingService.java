package com.hornetincorporation.beatroute;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BeeterTrackingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient googleApiClient;
    private GeofencingClient geofencingClient;
    private LocationRequest locationRequest;
    private GeofencingRequest geofenceRequest;
    private List<Geofence> geofenceList;
    private Location currentLocation;

    String sUserId4mSU;
    String sUserName4mSU;
    String sEmailID4mSU;
    String sPhoneNumber4mSU;
    String sOfficialID4mSU;
    String sOfficer4mSU;
    String sPhotoURL = null;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database, firebaseDatabase;
    private DatabaseReference beatroute, beetpoints, beeters, beeterlastlocation, beetroot;

    GeoFenceLocalDB geoFenceLocalDB;
    BeetRootLocalDB beetRootLocalDB;

    ConnectivityManager connectivityManager;
    NetworkInfo activeNetwork;
    boolean isConnected;

    @Override
    public void onCreate() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (geofencingClient == null) {
            geofencingClient = LocationServices.getGeofencingClient(this);
        }

        geofenceList = new ArrayList<Geofence>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        beatroute = firebaseDatabase.getReference();

        geoFenceLocalDB = new GeoFenceLocalDB(this);
        beetRootLocalDB = new BeetRootLocalDB(this);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //if (!isConnected) {
        recoverUserInfo();
        //} else {
        //    getUserInfofromDB();
        //}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        googleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Constants.LOCATION.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION.FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(Constants.LOCATION.SMALLEST_DISPLACEMENT);

        //initialize geofence
        if (checkPermission()) {
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_ENABLE_CODE);
        }
        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (checkPermission()) {
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_ENABLE_CODE);
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation != null) {
                writeBeetroot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else {
            Toast.makeText(this, R.string.location_gps_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (currentLocation != null) {
            writeBeetroot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.location_gps_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, R.string.location_connection_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, R.string.location_connection_suspended, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeofencePendingIntent());
        googleApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private PendingIntent createGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        intent.putExtra("UserID", sUserId4mSU);
//          mintent.putExtra("PhotoURL4mSU", user.getPhotoUrl());
        intent.putExtra("UserName", sUserName4mSU);
        intent.putExtra("EmailID", sEmailID4mSU);
        intent.putExtra("PhoneNumber", sPhoneNumber4mSU);
        intent.putExtra("OfficialID", sOfficialID4mSU);
        intent.putExtra("Officer", sOfficer4mSU);
        return PendingIntent.getService(this, Constants.GEOFENCE.GEOFENCE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void recoverUserInfo() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.SIGN_UP.PREF_FILE, Context.MODE_PRIVATE);

        if (sharedPref.contains(Constants.SIGN_UP.USER_ID) && sharedPref.contains(Constants.SIGN_UP.USER_NAME) && sharedPref.contains(Constants.SIGN_UP.EMAIL_ID) && sharedPref.contains(Constants.SIGN_UP.PHONE_NUMBER) && sharedPref.contains(Constants.SIGN_UP.OFFICIAL_ID) && sharedPref.contains(Constants.SIGN_UP.OFFICER)) {
            sUserId4mSU = sharedPref.getString(Constants.SIGN_UP.USER_ID, "DEFAULT");
            sUserName4mSU = sharedPref.getString(Constants.SIGN_UP.USER_NAME, "DEFAULT");
            sEmailID4mSU = sharedPref.getString(Constants.SIGN_UP.EMAIL_ID, "DEFAULT");
            sPhoneNumber4mSU = sharedPref.getString(Constants.SIGN_UP.PHONE_NUMBER, "DEFAULT");
            sOfficialID4mSU = sharedPref.getString(Constants.SIGN_UP.OFFICIAL_ID, "DEFAULT");
            sOfficer4mSU = sharedPref.getString(Constants.SIGN_UP.OFFICER, "DEFAULT");
        }
    }

    private void getUserInfofromDB() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sUserId4mSU = currentUser.getUid();

            database = FirebaseDatabase.getInstance();
            beeters = database.getReference("beeters");
            beeters.keepSynced(true);

            beeters.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot beeterSnapshot : dataSnapshot.getChildren()) {
                        if (beeterSnapshot.exists()) {
                            if (beeterSnapshot.getKey().toString().equals(sUserId4mSU)) {
                                sUserName4mSU = beeterSnapshot.child("BUserName").getValue().toString();
                                sEmailID4mSU = beeterSnapshot.child("BEmailID").getValue().toString();
                                sPhoneNumber4mSU = beeterSnapshot.child("BPhoneNumber").getValue().toString();
                                sOfficialID4mSU = beeterSnapshot.child("BOfficialID").getValue().toString();
                                sOfficer4mSU = beeterSnapshot.child("BOfficer").getValue().toString();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            //Stop service or check for blank userid and dont save the location; i chose the second option
        }
    }

    public void getGeofenceFromDB(final int geofenceEnable) {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            //firebaseDatabase.setPersistenceEnabled(true);
            beatroute = firebaseDatabase.getReference();
            beetpoints = beatroute.child("beetpoints").getRef();
            //beetpoints.keepSynced(true);
            beetpoints.orderByChild("BPRoute").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Cursor cursorTabExist = geoFenceLocalDB.checkTableExists();
                        if (cursorTabExist.getCount() > 0) {
                            while (cursorTabExist.moveToNext()) {
                                if (cursorTabExist.getString(0).equals(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME)) {
                                    Integer deletedRows = geoFenceLocalDB.deleteAllData();
                                }
                            }
                        }
                        for (DataSnapshot beatpointSnapshot : dataSnapshot.getChildren()) {
                            String gfmarkerTitle = "Route: '" + beatpointSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + beatpointSnapshot.child("BPPoint").getValue().toString() + "'";
                            String[] gflatlng = beatpointSnapshot.child("BPLocation").getValue().toString().split(",");
                            double gflat = Double.parseDouble(gflatlng[0]);
                            double gflng = Double.parseDouble(gflatlng[1]);
                            LatLng gflocation = new LatLng(gflat, gflng);
                            //Save to local db
                            boolean isInserted = geoFenceLocalDB.insertData(beatpointSnapshot.getKey().toString() + "~Route: '" + beatpointSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + beatpointSnapshot.child("BPPoint").getValue().toString() + "'", gflatlng[0], gflatlng[1], beatpointSnapshot.child("BPRoute").getValue().toString(), beatpointSnapshot.child("BPPoint").getValue().toString());
                            if (geofenceEnable == Constants.GEOFENCE.GEOFENCE_ENABLE_CODE) {
                                String geofenceReqID = beatpointSnapshot.getKey().toString() + "~Route: '" + beatpointSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + beatpointSnapshot.child("BPPoint").getValue().toString() + "'";
                                Geofence geofence = new Geofence.Builder()
                                        .setRequestId(geofenceReqID)
                                        .setCircularRegion(gflocation.latitude, gflocation.longitude, Constants.GEOFENCE.GEOFENCE_RADIUS)
                                        .setExpirationDuration(Constants.GEOFENCE.GEOFENCE_DURATION)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                                        .setLoiteringDelay(Constants.GEOFENCE.GEOFENCE_LOITERING_DELAY)
                                        .build();
                                geofenceList.add(geofence);
                            }
                        }
                        if (geofenceEnable == Constants.GEOFENCE.GEOFENCE_ENABLE_CODE) {
                            geofenceRequest = new GeofencingRequest.Builder()
                                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                                    .addGeofences(geofenceList)
                                    .build();
                            geofencingClient.addGeofences(geofenceRequest, createGeofencePendingIntent());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //TODO: Change text to string file
                    Toast.makeText(BeeterTrackingService.this, "Cannot retreive geofence information from database", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //TODO: Recover from local DB
            Cursor cursorTabExist = geoFenceLocalDB.checkTableExists();
            if (cursorTabExist.getCount() > 0) {
                while (cursorTabExist.moveToNext()) {
                    if (cursorTabExist.getString(0).equals(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME)) {
                        Cursor cursorBeatPoints = geoFenceLocalDB.getAllData();
                        if (cursorBeatPoints.getCount() > 0) {
                            while (cursorBeatPoints.moveToNext()) {
                                String gfmarkerTitle = "Route: '" + cursorBeatPoints.getString(4) + "', Point: '" + cursorBeatPoints.getString(5) + "'";
                                double gflat = Double.parseDouble(cursorBeatPoints.getString(2));
                                double gflng = Double.parseDouble(cursorBeatPoints.getString(3));
                                LatLng gflocation = new LatLng(gflat, gflng);
                                if (geofenceEnable == Constants.GEOFENCE.GEOFENCE_ENABLE_CODE) {
                                    String geofenceReqID = cursorBeatPoints.getString(1);
                                    Geofence geofence = new Geofence.Builder()
                                            .setRequestId(geofenceReqID)
                                            .setCircularRegion(gflocation.latitude, gflocation.longitude, Constants.GEOFENCE.GEOFENCE_RADIUS)
                                            .setExpirationDuration(Constants.GEOFENCE.GEOFENCE_DURATION)
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                                            .setLoiteringDelay(Constants.GEOFENCE.GEOFENCE_LOITERING_DELAY)
                                            .build();
                                    geofenceList.add(geofence);
                                }
                            }
                            if (geofenceEnable == Constants.GEOFENCE.GEOFENCE_ENABLE_CODE) {
                                try {
                                    geofenceRequest = new GeofencingRequest.Builder()
                                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                                            .addGeofences(geofenceList)
                                            .build();
                                    geofencingClient.addGeofences(geofenceRequest, createGeofencePendingIntent());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(BeeterTrackingService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void writeBeetroot(LatLng latLng) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        beatroute = firebaseDatabase.getReference();

        final String BRLoc = latLng.latitude + ", " + latLng.longitude;
        final String DTime = DateFormat.getDateTimeInstance().format(new Date());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            Cursor cursorTabExist = beetRootLocalDB.checkTableExists();
            if (cursorTabExist.getCount() > 0) {
                while (cursorTabExist.moveToNext()) {
                    if (cursorTabExist.getString(0).equals(Constants.BEETROOT_LOCAL_DB.TABLE_NAME)) {
                        Cursor cursorBeetRoot = beetRootLocalDB.getAllData();
                        if (cursorBeetRoot.getCount() > 0) {
                            while (cursorBeetRoot.moveToNext()) {
                                Map<String, Object> beeterlastloc = new HashMap<>();
                                beeterlastloc.put("BUserName", cursorBeetRoot.getString(2));
                                beeterlastloc.put("BLocation", cursorBeetRoot.getString(3));
                                beeterlastloc.put("BDateTime", cursorBeetRoot.getString(4));
                                beeterlastloc.put("BPhotoURL", cursorBeetRoot.getString(5));

                                beeterlastlocation = beatroute.child("beeterlastlocation").child(cursorBeetRoot.getString(1)).getRef();
                                //beeterlastlocation.keepSynced(true);
                                beeterlastlocation.setValue(beeterlastloc);

                                beetroot = beatroute.child("beetroots").child(cursorBeetRoot.getString(1)).getRef();
                                //beetroot.keepSynced(true);
                                beetroot.child(cursorBeetRoot.getString(4)).setValue(cursorBeetRoot.getString(3));
                            }
                        }
                        Integer deletedRows = beetRootLocalDB.deleteAllData();
                    }
                }
            }

            Map<String, Object> beeterlastloc = new HashMap<>();
            beeterlastloc.put("BUserName", sUserName4mSU);
            beeterlastloc.put("BLocation", BRLoc);
            beeterlastloc.put("BDateTime", DTime);
            beeterlastloc.put("BPhotoURL", sPhotoURL);
            if (sUserId4mSU != null) {
                beeterlastlocation = beatroute.child("beeterlastlocation").child(sUserId4mSU).getRef();
                //beeterlastlocation.keepSynced(true);
                beeterlastlocation.setValue(beeterlastloc);

                beetroot = beatroute.child("beetroots").child(sUserId4mSU).getRef();
                //beetroot.keepSynced(true);
                beetroot.child(DateFormat.getDateTimeInstance().format(new Date())).setValue(BRLoc);
            } else {
                Toast.makeText(this, "UserID is blank", Toast.LENGTH_SHORT).show();
            }
        } else {
            //TODO: Save in local db
            boolean isInserted = beetRootLocalDB.insertData(sUserId4mSU, sUserName4mSU, BRLoc, DTime, sPhotoURL);
        }
    }
}