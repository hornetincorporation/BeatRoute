package com.hornetincorporation.beatroute;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.text.DateFormat.getDateTimeInstance;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {


    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private GeofencingClient mGeofencingClient;

    List<Geofence> mGeofenceList;

    private Location lastLocation;

    private TextView textLat, textLong;

    private MapFragment mapFragment;

    private String sUserId, sPhotoURL, sUserName, sEmailID, sPhoneNumber, sOfficialID, sOfficer;

    private String GEOFENCE_REQ_ID = "My Geofence";
    private static final long GEO_DURATION = 8 * 60 * 60 * 1000;
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters

    FirebaseDatabase database;
    DatabaseReference beatroute, beetpoints, beetroot, beeterlastlocation, beeterlocation, beetrootdraw;

    ArrayAdapter beetrootname;

    NavigationView navigationView;
    //LatLng gflocation;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mGeofenceList = new ArrayList<Geofence>();

        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        ImageView drawerImage = (ImageView) headerView.findViewById(R.id.drawer_image);
        TextView drawerUsername = (TextView) headerView.findViewById(R.id.drawer_username);
        TextView drawerAccount = (TextView) headerView.findViewById(R.id.drawer_account);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        if (bundle != null) {
            sUserId = bundle.getString("UserID");
            sPhotoURL = bundle.getString("PhotoURL");
            sUserName = bundle.getString("UserName");
            sEmailID = bundle.getString("EmailID");
            sPhoneNumber = bundle.getString("PhoneNumber");
            sOfficialID = bundle.getString("OfficialID");
            sOfficer = bundle.getString("Officer");
        }

        // drawerImage.setImageDrawable(R.drawable.ic_menu_camera);
        drawerUsername.setText(sUserName + " (" + sOfficialID + ")");
        drawerAccount.setText(sEmailID);

        if (sOfficer.equals("No")) {
            navigationView.getMenu().findItem(R.id.nav_add_beat_points).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_beeters_location).setVisible(false);
        }

        // initialize GoogleMaps
        initGMaps();

        // create GoogleApiClient
        createGoogleApi();
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();

        database = FirebaseDatabase.getInstance();
        beatroute = database.getReference();

        beetpoints = beatroute.child("beetpoints").getRef();

        beetrootname = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        beetpoints.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastbeetrootname = "";
                boolean first = true;
                Integer iClr = 0;
                for (DataSnapshot brnSnapshot : dataSnapshot.getChildren()) {
                    if (first) {
                        beetrootname.clear();
                        first = false;
                    }
                    if (lastbeetrootname.trim().equals("") || !lastbeetrootname.trim().equals(brnSnapshot.child("BPRoute").getValue().toString().trim())) {
                        beetrootname.add(brnSnapshot.child("BPRoute").getValue().toString());
                        lastbeetrootname = brnSnapshot.child("BPRoute").getValue().toString();
                        iClr = brnSnapshot.hashCode();
                    }

                    //Geofence details from DB
                    String sGFName = brnSnapshot.getKey().toString();
                    String sGFTitle = "Route: '" + brnSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + brnSnapshot.child("BPPoint").getValue().toString() + "'";

                    String[] latlong = brnSnapshot.child("BPLocation").getValue().toString().split(",");
                    double gflat = Double.parseDouble(latlong[0]);
                    double gflong = Double.parseDouble(latlong[1]);

                    LatLng gflocation = new LatLng(gflat, gflong);

                    markerForGeofence(sGFTitle, gflocation, "#" + Integer.toHexString(iClr).substring(0, 6));
                    drawGeofence(gflocation);
                    //createGeofence(sGFName, gflocation));
                    Geofence geofence = createGeofence(sGFName, gflocation);
                    mGeofenceList.add(geofence);
                }
                GeofencingRequest geofenceRequest = createGeofenceRequest(mGeofenceList);
                addGeofence(geofenceRequest);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void drawGeofenceFromDB() {
        beetpoints = beatroute.child("beetpoints").getRef();

        beetrootname = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        beetpoints.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                map.clear();
                String lastbeetrootname = "";
                boolean first = true;
                Integer iClr = 0;

                for (DataSnapshot brnSnapshot : dataSnapshot.getChildren()) {

                    if (first) {
                        beetrootname.clear();
                        first = false;
                    }

                    if (lastbeetrootname.trim().equals("") || !lastbeetrootname.trim().equals(brnSnapshot.child("BPRoute").getValue().toString().trim())) {
                        beetrootname.add(brnSnapshot.child("BPRoute").getValue().toString());
                        lastbeetrootname = brnSnapshot.child("BPRoute").getValue().toString();
                        iClr = brnSnapshot.hashCode();
                    }
                    //Geofence details from DB
                    String sGFTitle = "Route: '" + brnSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + brnSnapshot.child("BPPoint").getValue().toString() + "'";

                    String[] latlong = brnSnapshot.child("BPLocation").getValue().toString().split(",");
                    double gflat = Double.parseDouble(latlong[0]);
                    double gflong = Double.parseDouble(latlong[1]);

                    LatLng gfloc = new LatLng(gflat, gflong);

                    //builder.include(gfloc);

                    markerForGeofence(sGFTitle, gfloc, "#" + Integer.toHexString(iClr).substring(0, 6));

                    //markerForGeofence(sGFTitle, gfloc);
                    drawGeofence(gfloc);
                }
                //LatLngBounds bounds = builder.build();
                //CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds,10000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch ( item.getItemId() ) {
//            case R.id.geofence: {
//                startGeofence();
//                return true;
//            }
//            case R.id.clear: {
//                clearGeofence();
//                return true;
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
        if (navigationView.getMenu().findItem(R.id.nav_add_beat_points).isChecked()) {
            markerForGeofence("Add New Route/Point", latLng, "#" + Integer.toHexString(100000000).substring(0, 6));
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        if (marker.getTitle().trim().equals("Add New Route/Point")) {

            final String BPLoc = marker.getPosition().latitude + ", " + marker.getPosition().longitude;
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.prompt, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final AutoCompleteTextView acTVBRName = (AutoCompleteTextView) promptsView
                    .findViewById(R.id.acTVRouteName);
            acTVBRName.setAdapter(beetrootname);

            final EditText editTextBPName = (EditText) promptsView.findViewById(R.id.etPointName);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    beetpoints = beatroute.child("beetpoints").getRef();

                                    Map<String, Object> beetpoint = new HashMap<>();
                                    beetpoint.put("BPActive", "Yes");
                                    beetpoint.put("BPCreatedDT", getDateTimeInstance().format(new Date()).toString());
                                    beetpoint.put("BPLocation", BPLoc.toString());
                                    beetpoint.put("BPPoint", editTextBPName.getText().toString());
                                    beetpoint.put("BPRoute", acTVBRName.getText().toString());

                                    beetpoints.push().setValue(beetpoint);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
        return false;

    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 10000;
    private final int FASTEST_INTERVAL = 9000;

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        //recoverGeofenceMarker();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat.setText("Lat: " + location.getLatitude());
        textLong.setText("Long: " + location.getLongitude());

        if (navigationView.getMenu().findItem(R.id.nav_points_covered).isChecked() ||
                navigationView.getMenu().findItem(R.id.nav_beatpoints).isChecked() ||
                navigationView.getMenu().findItem(R.id.nav_my_route).isChecked()) {
            markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        writeBeetroot(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }


    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * (2),
                vectorDrawable.getIntrinsicHeight() * (2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Marker locationMarker;

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = sUserName + " (" + sOfficialID + ")";
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                //.icon(vectorToBitmap(R.drawable.ic_round_person_pin_24px, Color.parseColor("#123456")))
                ;
        if (map != null) {
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 15f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    private void writeBeetroot(LatLng latLng) {
        beeterlastlocation = beatroute.child("beeterlastlocation").child(sUserId).getRef();

        final String BRLoc = latLng.latitude + ", " + latLng.longitude;

        Map<String, Object> beeterlastloc = new HashMap<>();
        beeterlastloc.put("BUserName", sUserName);
        beeterlastloc.put("BLocation", BRLoc);
        //beeterlastloc.put("BDateTime", ServerValue.TIMESTAMP);
        beeterlastloc.put("BDateTime", DateFormat.getDateTimeInstance().format(new Date()));
        beeterlastloc.put("BPhotoURL", sPhotoURL);

        beeterlastlocation.setValue(beeterlastloc);

        beetroot = beatroute.child("beetroots").child(sUserId).getRef();

        beetroot.child(DateFormat.getDateTimeInstance().format(new Date())).setValue(BRLoc);
    }

    private Marker geoFenceMarker;

    private void markerForGeofence(String title, LatLng latLng, String sColor) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        //String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                //.icon(vectorToBitmap(R.drawable.ic_round_location_city_24px, Color.parseColor(sColor)))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_round_location_city_24px))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
//            if (geoFenceMarker != null)
//                geoFenceMarker.remove();
            if (title.trim().equals("Add New Route/Point")) {
                if (geoFenceMarker.getTitle().equals("Add New Route/Point")) {
                    geoFenceMarker.remove();
                }
            }
            geoFenceMarker = map.addMarker(markerOptions);
        }
    }

    // Start Geofence creation process
//    private void startGeofence() {
//        Log.i(TAG, "startGeofence()");
//        if (geoFenceMarker != null) {
//            Geofence geofence = createGeofence(null, geoFenceMarker.getPosition());
//            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
//            addGeofence(geofenceRequest);
//        } else {
//            Log.e(TAG, "Geofence marker is null");
//        }
//    }


    // Create a Geofence
    private Geofence createGeofence(String geoReqId, LatLng latLng) {
        Log.d(TAG, "createGeofence");
        if (geoReqId != null) {
            GEOFENCE_REQ_ID = geoReqId;
        }
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(List<Geofence> geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(final GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        //Snackbar.make(findViewById(R.id.main_layout),"Geofence added: " + request.toString(), Snackbar.LENGTH_SHORT).show();

        if (checkPermission())
            mGeofencingClient.addGeofences(request, createGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences added
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                            //textLong.setText(e.getMessage().toString());
                            //Snackbar.make(findViewById(R.id.main_layout), e.getMessage(), Snackbar.LENGTH_SHORT).show();

                        }
                    });
//            LocationServices.GeofencingApi.addGeofences(
//                    googleApiClient,
//                    request,
//                    createGeofencePendingIntent()
//            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            //saveGeofence();
            //drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence(LatLng latLng) {
        Log.d(TAG, "drawGeofence()");

//        if (geoFenceLimits != null)
//            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            markerForGeofence("Need to add title", latLng, "#" + Integer.toHexString(100000000).substring(0, 6));
            drawGeofence(latLng);
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if (geoFenceMarker != null)
            geoFenceMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
    }

    private void DrawRoute() {

        beetrootdraw = beatroute.child("beetroots").child(sUserId).getRef();

        beetrootdraw.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Date beetingdate, curdate;

                if (navigationView.getMenu().findItem(R.id.nav_my_route).isChecked()) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.clickable(true);

                    for (DataSnapshot bLocSnapshot : dataSnapshot.getChildren()) {
                        beetingdate = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                        try {
                            beetingdate = dateFormat.parse(bLocSnapshot.getKey().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        curdate = Calendar.getInstance().getTime();

                        long difference = Math.abs(curdate.getTime() - beetingdate.getTime());
                        long differenceDates = difference / (24 * 60 * 60 * 1000);

                        if (differenceDates <= 1) {
                            String[] latlong = bLocSnapshot.getValue().toString().split(",");
                            double gflat = Double.parseDouble(latlong[0]);
                            double gflong = Double.parseDouble(latlong[1]);

                            polylineOptions.add(new LatLng(gflat, gflong));
                        }
                    }
                    Polyline myroute = map.addPolyline(polylineOptions);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void beetersLocation() {

        beeterlocation = beatroute.child("beeterlastlocation").getRef();

        beeterlocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (navigationView.getMenu().findItem(R.id.nav_beeters_location).isChecked()) {
                    map.clear();
                    for (DataSnapshot bLocSnapshot : dataSnapshot.getChildren()) {

                        String sUserNameLL = bLocSnapshot.child("BUserName").getValue().toString();
                        String sLastLocation = bLocSnapshot.child("BLocation").getValue().toString();
                        String sDateTimeLL = bLocSnapshot.child("BDateTime").getValue().toString();
                        //String sPhotoLL = bLocSnapshot.child("BPhotoURL").getValue().toString();

                        String sLLTitle = "Last location of " + sUserNameLL + " @ " + sDateTimeLL;

                        String[] latlong = sLastLocation.split(",");
                        double lllat = Double.parseDouble(latlong[0]);
                        double lllong = Double.parseDouble(latlong[1]);

                        LatLng lllocation = new LatLng(lllat, lllong);

                        markerForGeofence(sLLTitle, lllocation, "#" + Integer.toHexString(bLocSnapshot.hashCode()).substring(0, 6));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // create GoogleApiClient
//        createGoogleApi();
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_beatpoints) {
            map.clear();
            drawGeofenceFromDB();
            getLastKnownLocation();
        } else if (id == R.id.nav_my_route) {
            map.clear();
            DrawRoute();
            getLastKnownLocation();
        } else if (id == R.id.nav_points_covered) {
            map.clear();
            getLastKnownLocation();
        } else if (id == R.id.nav_beeters_location) {
            map.clear();
            beetersLocation();
        } else if (id == R.id.nav_add_beat_points) {
            map.clear();
            drawGeofenceFromDB();
        } else if (id == R.id.nav_share) {
            map.clear();

        } else if (id == R.id.nav_send) {
            map.clear();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
