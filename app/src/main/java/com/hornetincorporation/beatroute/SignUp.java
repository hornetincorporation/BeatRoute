package com.hornetincorporation.beatroute;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends BaseActivity implements View.OnClickListener {

    private ImageView mProfileImage;
    private EditText etUserName;
    private EditText etEmailID;
    private EditText etPhoneNum;
    private EditText etOfficialID;
    private Switch mOfficer;

    private TextView txUserName;
    private TextView txEmailID;
    private TextView txPhoneNum;
    private TextView txOfficialID;
    private TextView txOfficer;

    String sUserId4mSU;
    String sUserName4mSU;
    String sEmailID4mSU;
    String sPhoneNumber4mSU;

    FirebaseDatabase database;
    DatabaseReference beeters;

    ConnectivityManager connectivityManager;
    NetworkInfo activeNetwork;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mProfileImage = findViewById(R.id.profile_pic);

        etUserName = findViewById(R.id.DisplayName);
        etEmailID = findViewById(R.id.EmailID);
        etPhoneNum = findViewById(R.id.PhoneNumber);
        etOfficialID = findViewById(R.id.OfficialID);
        mOfficer = findViewById(R.id.Officer);

        txUserName = findViewById(R.id.tvDisplayName);
        txEmailID = findViewById(R.id.tvEmailID);
        txPhoneNum = findViewById(R.id.tvPhoneNumber);
        txOfficialID = findViewById(R.id.tvOfficialID);
        txOfficer = findViewById(R.id.tvOfficer);

        // Button listeners
        findViewById(R.id.nextbutton).setOnClickListener(this);
        findViewById(R.id.sout_button).setOnClickListener(this);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        if (bundle != null) {
            sUserId4mSU = bundle.getString("UserID4mSU");
            sUserName4mSU = bundle.getString("UserName4mSU");
            sEmailID4mSU = bundle.getString("EmailID4mSU");
            sPhoneNumber4mSU = bundle.getString("PhoneNumber4mSU");
        }

        etUserName.setText(sUserName4mSU);
        etEmailID.setText(sEmailID4mSU);
        etPhoneNum.setText(sPhoneNumber4mSU);

        database = FirebaseDatabase.getInstance();
        beeters = database.getReference("beeters");
        beeters.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected){
            recoverUserInfo();
        }else {
            checkSUData(beeters);
        }
    }

    public void getSignUpData(DatabaseReference dbr, final checkSignUp listener) {
        listener.onStart();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();

        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
                Snackbar.make(findViewById(R.id.sign_up_layout), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSUData(DatabaseReference dbrBeeters) {
        getSignUpData(dbrBeeters, new checkSignUp() {
            @Override
            public void onStart() {
                //DO SOME THING WHEN START GET DATA HERE
            }

            @Override
            public void onSuccess(DataSnapshot data) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (mProgressDialog != null && mProgressDialog.isShowing()) {

                    for (DataSnapshot beeterSnapshot : data.getChildren()) {
                        if (beeterSnapshot.exists()) {
                            if (beeterSnapshot.getKey().toString().equals(sUserId4mSU)) {
                                txUserName.setText(beeterSnapshot.child("BUserName").getValue().toString());
                                txEmailID.setText(beeterSnapshot.child("BEmailID").getValue().toString());
                                txPhoneNum.setText(beeterSnapshot.child("BPhoneNumber").getValue().toString());
                                txOfficialID.setText(beeterSnapshot.child("BOfficialID").getValue().toString());
                                txOfficer.setText(beeterSnapshot.child("BOfficer").getValue().toString());
                                break;
                            }
                        }
                    }
                    updateUI();
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.sign_up_layout), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.nextbutton) {

            if (txOfficialID.getText().toString().length() == 0) {
                if (etUserName.getText().toString().length() > 0 &&
                        etEmailID.getText().toString().length() > 0 &&
                        etPhoneNum.getText().toString().length() > 0 &&
                        etOfficialID.getText().toString().length() > 0) {

                    Map<String, Object> beeter = new HashMap<>();

                    beeter.put("BUserName", etUserName.getText().toString());
                    beeter.put("BEmailID", etEmailID.getText().toString());
                    beeter.put("BPhoneNumber", etPhoneNum.getText().toString());
                    beeter.put("BOfficialID", etOfficialID.getText().toString());
                    if (mOfficer.isChecked()) {
                        beeter.put("BOfficer", mOfficer.getTextOn().toString());
                    } else {
                        beeter.put("BOfficer", mOfficer.getTextOff().toString());
                    }

                    beeters.child(sUserId4mSU).setValue(beeter);

                    checkSUData(beeters);

                    Snackbar.make(findViewById(R.id.sign_up_layout), "User Account created", Snackbar.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(findViewById(R.id.sign_up_layout), "Please fill all fields.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.sign_up_layout), "User Account already exists! Taking to your account.", Snackbar.LENGTH_SHORT).show();
                updateUI();
            }
        } else if (i == R.id.sout_button) {
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
                            movetofirstpage();
                        }
                    });
        }
    }

    private void movetofirstpage() {
        Intent i = new Intent(this, SignIn.class);
        startActivity(i);
        SignUp.this.finish();
    }

    private void updateUI() {

        if (txOfficialID.getText().toString().length() > 0) {

            findViewById(R.id.viewdata4mdb).setVisibility(View.VISIBLE);
            findViewById(R.id.viewdata4msu).setVisibility(View.GONE);

            movetonextpage();

        } else {

            findViewById(R.id.viewdata4mdb).setVisibility(View.GONE);
            findViewById(R.id.viewdata4msu).setVisibility(View.VISIBLE);

        }
    }

    private void movetonextpage() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            saveUserInfo();
        }
        Intent mintent = new Intent(this, MainActivity.class);

        mintent.putExtra("UserID", sUserId4mSU);
//          mintent.putExtra("PhotoURL4mSU", user.getPhotoUrl());
        mintent.putExtra("UserName", txUserName.getText().toString());
        mintent.putExtra("EmailID", txEmailID.getText().toString());
        mintent.putExtra("PhoneNumber", txPhoneNum.getText().toString());
        mintent.putExtra("OfficialID", txOfficialID.getText().toString());
        mintent.putExtra("Officer", txOfficer.getText().toString());

        startActivity(mintent);
        SignUp.this.finish();
    }

    private void saveUserInfo() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.SIGN_UP.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.SIGN_UP.USER_ID, sUserId4mSU);
        editor.putString(Constants.SIGN_UP.USER_NAME, txUserName.getText().toString());
        editor.putString(Constants.SIGN_UP.EMAIL_ID, txEmailID.getText().toString());
        editor.putString(Constants.SIGN_UP.PHONE_NUMBER, txPhoneNum.getText().toString());
        editor.putString(Constants.SIGN_UP.OFFICIAL_ID, txOfficialID.getText().toString());
        editor.putString(Constants.SIGN_UP.OFFICER, txOfficer.getText().toString());
        editor.commit();
    }

    private void recoverUserInfo() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.SIGN_UP.PREF_FILE, Context.MODE_PRIVATE);

        if (sharedPref.contains(Constants.SIGN_UP.USER_ID) && sharedPref.contains(Constants.SIGN_UP.USER_NAME) && sharedPref.contains(Constants.SIGN_UP.EMAIL_ID) && sharedPref.contains(Constants.SIGN_UP.PHONE_NUMBER) && sharedPref.contains(Constants.SIGN_UP.OFFICIAL_ID) && sharedPref.contains(Constants.SIGN_UP.OFFICER)) {
            sUserId4mSU = sharedPref.getString(Constants.SIGN_UP.USER_ID, "DEFAULT");
            txUserName.setText(sharedPref.getString(Constants.SIGN_UP.USER_NAME, "DEFAULT"));
            txEmailID.setText(sharedPref.getString(Constants.SIGN_UP.EMAIL_ID, "DEFAULT"));
            txPhoneNum.setText(sharedPref.getString(Constants.SIGN_UP.PHONE_NUMBER, "DEFAULT"));
            txOfficialID.setText(sharedPref.getString(Constants.SIGN_UP.OFFICIAL_ID, "DEFAULT"));
            txOfficer.setText(sharedPref.getString(Constants.SIGN_UP.OFFICER, "DEFAULT"));
            updateUI();
        }
    }
}