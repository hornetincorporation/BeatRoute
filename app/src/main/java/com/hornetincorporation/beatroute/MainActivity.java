package com.hornetincorporation.beatroute;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
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

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.InetAddress;
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
import java.util.TimeZone;

import static java.text.DateFormat.getDateTimeInstance;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FloatingActionButton.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap map;
    private MapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private GeofencingClient geofencingClient;
    private LocationRequest locationRequest;
    private GeofencingRequest geofenceRequest;
    private List<Geofence> geofenceList;
    private ArrayAdapter routenameList;
    private Marker beeterlocationMarker, geofencelocationMarker, currentlocationMarker, visitedlocationMarker;
    private Location currentLocation;

    private NavigationView navigationView;
    private TextView textLat, textLong;
    private String sUserId, sPhotoURL, sUserName, sEmailID, sPhoneNumber, sOfficialID, sOfficer;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference beatroute, beetpoints, beetroot, beeterlastlocation, beeterlocation, beetrootdraw, beetpointvisited, beetpoint_del;
    GeoFenceLocalDB geoFenceLocalDB;
    BeetRootLocalDB beetRootLocalDB;

    ConnectivityManager connectivityManager;
    NetworkInfo activeNetwork;
    boolean isConnected;

    private int exittracker = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get the extra variables from the intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sUserId = bundle.getString("UserID");
            sPhotoURL = bundle.getString("PhotoURL");
            sUserName = bundle.getString("UserName");
            sEmailID = bundle.getString("EmailID");
            sPhoneNumber = bundle.getString("PhoneNumber");
            sOfficialID = bundle.getString("OfficialID");
            sOfficer = bundle.getString("Officer");
        }
        //initialize text box
        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);
        //initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //initialize drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //initialize navigation menu
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        View headerView = navigationView.getHeaderView(0);
        ImageView drawerImage = (ImageView) headerView.findViewById(R.id.drawer_image);
        TextView drawerUsername = (TextView) headerView.findViewById(R.id.drawer_username);
        TextView drawerAccount = (TextView) headerView.findViewById(R.id.drawer_account);
        // drawerImage.setImageDrawable(R.drawable.ic_menu_camera);
        drawerUsername.setText(sUserName + " (" + sOfficialID + ")");
        drawerAccount.setText(sEmailID);
        if (sOfficer.equals("No")) {
            navigationView.getMenu().findItem(R.id.nav_beeters_location).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_add_beat_points).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_del_beat_points).setVisible(false);
            navigationView.getMenu().findItem(R.id.rep_beeter).setVisible(false);
            navigationView.getMenu().findItem(R.id.rep_beetpoint).setVisible(false);
        }
        //initialize floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        //initialize map
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //create GoogleApiClient
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //create GeofencingClient
        if (geofencingClient == null) {
            geofencingClient = LocationServices.getGeofencingClient(this);
        }
        //initialize arraylist
        geofenceList = new ArrayList<Geofence>();
        routenameList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //initialize database
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        beatroute = firebaseDatabase.getReference();
        //beatroute.keepSynced(true);
        geoFenceLocalDB = new GeoFenceLocalDB(this);
        beetRootLocalDB = new BeetRootLocalDB(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        exittracker = 0;
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        if (checkPermission()) {
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        exittracker = 0;
        if (navigationView.getMenu().findItem(R.id.nav_add_beat_points).isChecked()) {
            geofencelocationMarker = addMarker(geofencelocationMarker, Constants.MARKER.GEOFENCE_LOCATION_MARKER, latLng, "Add New Route/Point", R.drawable.ic_round_location_city_24px, getResources().getColor(R.color.blue), getResources().getColor(R.color.lightblue), null, 1);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        exittracker = 0;
        if (navigationView.getMenu().findItem(R.id.nav_add_beat_points).isChecked()) {
            if (marker.getTitle().trim().equals("Add New Route/Point")) {
                final String BPLoc = marker.getPosition().latitude + ", " + marker.getPosition().longitude;
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(promptsView);
                final AutoCompleteTextView acTVBRName = (AutoCompleteTextView) promptsView
                        .findViewById(R.id.acTVRouteName);
                acTVBRName.setAdapter(routenameList);
                final EditText editTextBPName = (EditText) promptsView.findViewById(R.id.etPointName);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Add",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                        activeNetwork = connectivityManager.getActiveNetworkInfo();
                                        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                                        if (isConnected) {
                                            if (acTVBRName.getText().length() > 0 && editTextBPName.getText().length() > 0) {
                                                beetpoints = beatroute.child("beetpoints").getRef();
                                                //beetpoints.keepSynced(true);
                                                Map<String, Object> beetpoint = new HashMap<>();
                                                beetpoint.put("BPActive", "Yes");
                                                beetpoint.put("BPCreatedDT", getDateTimeInstance().format(new Date()).toString());
                                                beetpoint.put("BPLocation", BPLoc.toString());
                                                beetpoint.put("BPPoint", editTextBPName.getText().toString());
                                                beetpoint.put("BPRoute", acTVBRName.getText().toString());

                                                beetpoints.push().setValue(beetpoint);
                                                Toast.makeText(MainActivity.this, "Beatpoint added successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Please fill both fields.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Your data should be turned on to add beatpoints", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        } else if (navigationView.getMenu().findItem(R.id.nav_del_beat_points).isChecked()) {
            if (!(marker.getTitle().trim().equals("Add New Route/Point") || marker.getTitle().trim().split("'")[0].trim().equals("Route"))) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                builder.setTitle("Delete Beatpoints")
                        .setMessage("Are you sure you want to delete this entry? " + marker.getTitle().trim())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                activeNetwork = connectivityManager.getActiveNetworkInfo();
                                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                                if (isConnected) {
                                    final String br4mmarker = marker.getTitle().trim().split("'")[1];
                                    final String bp4mmarker = marker.getTitle().trim().split("'")[3];
                                    final String bploc4mmarker = marker.getPosition().latitude + ", " + marker.getPosition().longitude;

                                    beetpoint_del = beatroute.child("beetpoints").orderByChild("BPRoute").getRef();

                                    beetpoint_del.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot beetpointSnapshot : dataSnapshot.getChildren()) {
                                                    if (beetpointSnapshot.child("BPRoute").getValue().toString().trim().equals(br4mmarker.trim()) && beetpointSnapshot.child("BPPoint").getValue().toString().trim().equals(bp4mmarker.trim()) && beetpointSnapshot.child("BPLocation").getValue().toString().trim().equals(bploc4mmarker.trim())) {
                                                        beetpointSnapshot.getRef().removeValue();
                                                        Toast.makeText(MainActivity.this, "Beat Point " + marker.getTitle().trim() + " has been deleted", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "Your data should be turned on to delete beatpoints", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        onMarkerClick(marker);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        exittracker = 0;
        int id = item.getItemId();
        if (id == R.id.nav_beatpoints) {
            map.clear();
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_STSQUO_CODE);
            getBeeterLocation();
        } else if (id == R.id.nav_my_route) {
            map.clear();
            drawBeatersRoute();
            getBeeterLocation();
        } else if (id == R.id.nav_points_covered) {
            map.clear();
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_STSQUO_CODE);
            writeBeatPointsVisited();
        } else if (id == R.id.nav_beeters_location) {
            map.clear();
            writeBeatersCurrentLocation();
        } else if (id == R.id.nav_add_beat_points) {
            map.clear();
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_STSQUO_CODE);
        } else if (id == R.id.nav_del_beat_points) {
            map.clear();
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_STSQUO_CODE);
        } else if (id == R.id.rep_beeter) {
            map.clear();
            Toast.makeText(MainActivity.this, "This feature is coming soon - Hornet Incorporation", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.rep_beetpoint) {
            map.clear();
            Toast.makeText(MainActivity.this, "This feature is coming soon - Hornet Incorporation", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sign_off) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent i = new Intent(MainActivity.this, SignIn.class);
                            startActivity(i);
                            MainActivity.this.finish();
                        }
                    });
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.fab) {
            //To track back button pressed
            exittracker = 0;
            Toast.makeText(MainActivity.this, "This feature is coming soon - Hornet Incorporation", Toast.LENGTH_SHORT).show();
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        exittracker = 0;
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "This feature is coming soon - Hornet Incorporation", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exittracker++;
            if (exittracker == 1) {
                Toast.makeText(MainActivity.this, "Please press again to exit activity.", Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeofencePendingIntent());
        googleApiClient.disconnect();
        Intent intent = new Intent("com.hornetincorporation.beatroute.SET_ALARM");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.hornetincorporation.beatroute.SET_ALARM");
        sendBroadcast(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkPermission()) {
            getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_ENABLE_CODE);
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation != null) {
                writeActualLocation(currentLocation);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else askPermission();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (currentLocation != null) {
            writeActualLocation(location);
        } else {
            Toast.makeText(this, R.string.location_gps_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.location_connection_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, R.string.location_connection_suspended, Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION.LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOCATION.LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPermission()) {
                        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        writeActualLocation(currentLocation);
                        locationRequest = new LocationRequest();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(Constants.LOCATION.UPDATE_INTERVAL);
                        locationRequest.setFastestInterval(Constants.LOCATION.FASTEST_INTERVAL);
                        locationRequest.setSmallestDisplacement(Constants.LOCATION.SMALLEST_DISPLACEMENT);
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                        getGeofenceFromDB(Constants.GEOFENCE.GEOFENCE_ENABLE_CODE);
                    }
                } else {
                    permissionsDenied();
                }
                break;
            }
        }
    }

    private void permissionsDenied() {
        askPermission();
    }

    private void getBeeterLocation() {
        if (checkPermission()) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation != null) {
                writeActualLocation(currentLocation);
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(Constants.LOCATION.UPDATE_INTERVAL);
                locationRequest.setFastestInterval(Constants.LOCATION.FASTEST_INTERVAL);
                locationRequest.setSmallestDisplacement(Constants.LOCATION.SMALLEST_DISPLACEMENT);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat.setText("Lat: " + location.getLatitude());
        textLong.setText("Long: " + location.getLongitude());

        if (navigationView.getMenu().findItem(R.id.nav_points_covered).isChecked() || navigationView.getMenu().findItem(R.id.nav_beatpoints).isChecked() || navigationView.getMenu().findItem(R.id.nav_my_route).isChecked()) {
            String title = sUserName + " (" + sOfficialID + ")";
            LatLng bloc = new LatLng(location.getLatitude(), location.getLongitude());
            beeterlocationMarker = addMarker(beeterlocationMarker, Constants.MARKER.BEETER_LOCATION_MARKER, bloc, title, R.drawable.ic_round_person_pin_24px, getResources().getColor(R.color.red), getResources().getColor(R.color.lightred), 15f, 1);
        }
        writeBeetroot(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeBeetroot(LatLng latLng) {
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
            beeterlastloc.put("BUserName", sUserName);
            beeterlastloc.put("BLocation", BRLoc);
            beeterlastloc.put("BDateTime", DTime);
            beeterlastloc.put("BPhotoURL", sPhotoURL);

            beeterlastlocation = beatroute.child("beeterlastlocation").child(sUserId).getRef();
            //beeterlastlocation.keepSynced(true);
            beeterlastlocation.setValue(beeterlastloc);

            beetroot = beatroute.child("beetroots").child(sUserId).getRef();
            //beetroot.keepSynced(true);
            beetroot.child(DateFormat.getDateTimeInstance().format(new Date())).setValue(BRLoc);
        } else {
            //TODO: Save in local db
            boolean isInserted = beetRootLocalDB.insertData(sUserId, sUserName, BRLoc, DTime, sPhotoURL);
        }
    }

    public void getGeofenceFromDB(final int geofenceEnable) {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            beetpoints = beatroute.child("beetpoints").getRef();
            //beetpoints.keepSynced(true);
            beetpoints.orderByChild("BPRoute").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    map.clear();
                    boolean firstRouteFlag = true;
                    String lastRouteName = "";
                    int iColor = 0;
                    int[] androidColors = getResources().getIntArray(R.array.androidcolors);
                    int[] androidLightColors = getResources().getIntArray(R.array.androidlightcolors);
                    int randomAndroidColor = androidColors[iColor];
                    int randomAndroidLightColor = androidLightColors[iColor];
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
                            if (firstRouteFlag) {
                                routenameList.clear();
                                firstRouteFlag = false;
                            }
                            if (lastRouteName.trim().equals("") || !lastRouteName.trim().equals(beatpointSnapshot.child("BPRoute").getValue().toString().trim())) {
                                routenameList.add(beatpointSnapshot.child("BPRoute").getValue().toString());
                                lastRouteName = beatpointSnapshot.child("BPRoute").getValue().toString();
                                int randomnum = new Random().nextInt(androidColors.length);
                                randomAndroidColor = androidColors[iColor];
                                randomAndroidLightColor = androidLightColors[iColor];
                                iColor = iColor + 1;
                                if (iColor >= androidColors.length) {
                                    iColor = 0;
                                }
                            }
                            String gfmarkerTitle = "Route: '" + beatpointSnapshot.child("BPRoute").getValue().toString() + "', Point: '" + beatpointSnapshot.child("BPPoint").getValue().toString() + "'";
                            String[] gflatlng = beatpointSnapshot.child("BPLocation").getValue().toString().split(",");
                            double gflat = Double.parseDouble(gflatlng[0]);
                            double gflng = Double.parseDouble(gflatlng[1]);
                            LatLng gflocation = new LatLng(gflat, gflng);
                            geofencelocationMarker = addMarker(geofencelocationMarker, Constants.MARKER.GEOFENCE_LOCATION_MARKER, gflocation, gfmarkerTitle, R.drawable.ic_round_location_city_24px, randomAndroidColor, randomAndroidLightColor, null, 1);
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
                    Toast.makeText(MainActivity.this, "Cannot retreive geofence information from database", Toast.LENGTH_SHORT).show();
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
                            //map.clear();
                            boolean firstRouteFlag = true;
                            String lastRouteName = "";
                            int iColor = 0;
                            int[] androidColors = getResources().getIntArray(R.array.androidcolors);
                            int[] androidLightColors = getResources().getIntArray(R.array.androidlightcolors);
                            int randomAndroidColor = androidColors[iColor];
                            int randomAndroidLightColor = androidLightColors[iColor];
                            while (cursorBeatPoints.moveToNext()) {
                                if (firstRouteFlag) {
                                    routenameList.clear();
                                    firstRouteFlag = false;
                                }
                                if (lastRouteName.trim().equals("") || !lastRouteName.trim().equals(cursorBeatPoints.getString(4).trim())) {
                                    routenameList.add(cursorBeatPoints.getString(4));
                                    lastRouteName = cursorBeatPoints.getString(4);
                                    int randomnum = new Random().nextInt(androidColors.length);
                                    randomAndroidColor = androidColors[iColor];
                                    randomAndroidLightColor = androidLightColors[iColor];
                                    iColor = iColor + 1;
                                    if (iColor >= androidColors.length) {
                                        iColor = 0;
                                    }
                                }
                                String gfmarkerTitle = "Route: '" + cursorBeatPoints.getString(4) + "', Point: '" + cursorBeatPoints.getString(5) + "'";
                                double gflat = Double.parseDouble(cursorBeatPoints.getString(2));
                                double gflng = Double.parseDouble(cursorBeatPoints.getString(3));
                                LatLng gflocation = new LatLng(gflat, gflng);
                                geofencelocationMarker = addMarker(geofencelocationMarker, Constants.MARKER.GEOFENCE_LOCATION_MARKER, gflocation, gfmarkerTitle, R.drawable.ic_round_location_city_24px, randomAndroidColor, randomAndroidLightColor, null, 1);
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
                                    Snackbar.make(findViewById(R.id.map), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private PendingIntent createGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        intent.putExtra("UserID", sUserId);
//          mintent.putExtra("PhotoURL4mSU", user.getPhotoUrl());
        intent.putExtra("UserName", sUserName);
        intent.putExtra("EmailID", sEmailID);
        intent.putExtra("PhoneNumber", sPhoneNumber);
        intent.putExtra("OfficialID", sOfficialID);
        intent.putExtra("Officer", sOfficer);
        return PendingIntent.getService(this, Constants.GEOFENCE.GEOFENCE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void drawBeatersRoute() {
        beetrootdraw = beatroute.child("beetroots").child(sUserId).getRef();
        //beetrootdraw.keepSynced(true);
        beetrootdraw.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Calendar curCalendar;
                Date beetingdate, curdate;
                if (navigationView.getMenu().findItem(R.id.nav_my_route).isChecked()) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.clickable(true);
                    polylineOptions.color(getResources().getColor(R.color.lightred));
                    for (DataSnapshot bLocSnapshot : dataSnapshot.getChildren()) {
                        beetingdate = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                        try {
                            beetingdate = dateFormat.parse(bLocSnapshot.getKey().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        curCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                        curCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
                        curdate = curCalendar.getTime();

                        long difference = Math.abs(curdate.getTime() - beetingdate.getTime());
                        long differenceDates = difference / Constants.SECS_IN_DAY;
                        if (differenceDates < 1) {
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
                //TODO: Toast
            }
        });
    }

    private void writeBeatPointsVisited() {
        beetpointvisited = beatroute.child("beetpointvisits").child(sUserId).getRef();
        //beetpointvisited.keepSynced(true);
        beetpointvisited.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Calendar curCalendar;
                Date beetpointvisitdate, curdate;
                String lastVisitedRouteName = "";
                if (navigationView.getMenu().findItem(R.id.nav_points_covered).isChecked()) {
                    for (DataSnapshot bPointVisitSnapshot : dataSnapshot.getChildren()) {
                        beetpointvisitdate = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                        try {
                            beetpointvisitdate = dateFormat.parse(bPointVisitSnapshot.getKey().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        curCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                        curCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
                        curdate = curCalendar.getTime();
                        long difference = Math.abs(curdate.getTime() - beetpointvisitdate.getTime());
                        long differenceDates = difference / Constants.SECS_IN_DAY;

                        if (differenceDates < 1) {
                            if (bPointVisitSnapshot.child("BPVTransition").getValue().toString().equals("Stayed for 10 secs in ")) {
                                if (lastVisitedRouteName.trim().equals("") || !lastVisitedRouteName.trim().equals(bPointVisitSnapshot.child("BPVRoute").getValue().toString().trim())) {
                                    String sGFTitle = "Visited Route: '" + bPointVisitSnapshot.child("BPVRoute").getValue().toString() + "', Point: '" + bPointVisitSnapshot.child("BPVPoint").getValue().toString() + "' at " + bPointVisitSnapshot.getKey().toString();
                                    String[] latlong = bPointVisitSnapshot.child("BPVLocation").getValue().toString().split(",");
                                    double gflat = Double.parseDouble(latlong[0]);
                                    double gflong = Double.parseDouble(latlong[1]);
                                    LatLng gflocation = new LatLng(gflat, gflong);
                                    visitedlocationMarker = addMarker(visitedlocationMarker, Constants.MARKER.VISITED_LOCATION_MARKER, gflocation, sGFTitle, R.drawable.ic_round_how_to_reg_24px, getResources().getColor(R.color.darkgreen), getResources().getColor(R.color.lightgreen), null, 2);
                                    lastVisitedRouteName = bPointVisitSnapshot.child("BPVRoute").getValue().toString();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: Toast
            }
        });
    }

    private void writeBeatersCurrentLocation() {
        beeterlocation = beatroute.child("beeterlastlocation").getRef();
        //beeterlocation.keepSynced(true);
        beeterlocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (navigationView.getMenu().findItem(R.id.nav_beeters_location).isChecked()) {
                    int iColor = 0;
                    int[] androidColors = getResources().getIntArray(R.array.androidcolors);
                    int randomAndroidColor;
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
                        randomAndroidColor = androidColors[iColor];
                        currentlocationMarker = addMarker(currentlocationMarker, Constants.MARKER.CURRENT_LOCATION_MARKER, lllocation, sLLTitle, R.drawable.ic_round_person_pin_24px, randomAndroidColor, getResources().getColor(R.color.lightgreen), null, 1);
                        iColor = iColor + 1;
                        if (iColor >= androidColors.length) {
                            iColor = 0;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: Add toast
            }
        });
    }

    private Marker addMarker(Marker marker, int markercode, LatLng latLng, String markerTitle, @DrawableRes int iconID, @ColorInt int color, @ColorInt int lightcolor, Float zoom, int size) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(markerTitle)
                .icon(vectorToBitmap(iconID, color, size));
        if (map != null) {
            if (markercode == Constants.MARKER.BEETER_LOCATION_MARKER) {
                if (marker != null)
                    marker.remove();
            }
            if (markerTitle.trim().equals("Add New Route/Point")) {
                if (marker != null) {
                    if (marker.getTitle().equals("Add New Route/Point")) {
                        marker.remove();
                    }
                }
            }
            if (markercode == Constants.MARKER.GEOFENCE_LOCATION_MARKER) {
                if (!markerTitle.trim().equals("Add New Route/Point")) {
                    CircleOptions circleOptions = new CircleOptions()
                            .center(latLng)
                            .strokeColor(lightcolor)
                            .fillColor(lightcolor)
                            .radius(Constants.GEOFENCE.GEOFENCE_RADIUS);
                    map.addCircle(circleOptions);
                }
            }
            marker = map.addMarker(markerOptions);
            marker.showInfoWindow();
            if (markercode == Constants.MARKER.BEETER_LOCATION_MARKER) {
                if (zoom != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
                    map.animateCamera(cameraUpdate);
                }
            }
        }
        return marker;
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int iconID, @ColorInt int color, int size) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), iconID, null);
        Bitmap bitmap = null;
        Canvas canvas = null;
        if (vectorDrawable != null) {
            DrawableCompat.setTint(vectorDrawable, color);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * size, vectorDrawable.getIntrinsicHeight() * size, Bitmap.Config.ARGB_8888);
            if (bitmap != null) {
                canvas = new Canvas(bitmap);
            }
            if (canvas != null) {
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            }
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static Intent makeNotificationIntent(Context context, String msg, String UserID, String UserN, String Email, String Phone, String OffID, String Officer) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NOTIFICATION MSG", msg);
        intent.putExtra("UserID", UserID);
//      mintent.putExtra("PhotoURL4mSU", user.getPhotoUrl());
        intent.putExtra("UserName", UserN);
        intent.putExtra("EmailID", Email);
        intent.putExtra("PhoneNumber", Phone);
        intent.putExtra("OfficialID", OffID);
        intent.putExtra("Officer", Officer);
        return intent;
    }
}