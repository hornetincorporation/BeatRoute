package com.hornetincorporation.beatroute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignIn extends BaseActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
//        ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
//        Toast.makeText(this, cq.toString(), Toast.LENGTH_SHORT).show();

        if (!isConnected) {
            recoverUserInfo();
        } else {
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
//            if (isNetworkAvailable())
//            {
//                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
//            }
            if (!isConnected) {
                recoverUserInfo();
            } else {
                signIn();
            }
        } else if (i == R.id.sign_out_button) {
            signOut();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.SIGN_IN.SIGN_IN_REQUEST_CODE);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.SIGN_IN.SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Snackbar.make(findViewById(R.id.sign_in_layout), "Google Sign In Failed", Snackbar.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Snackbar.make(findViewById(R.id.sign_in_layout), "Firebase Sign In Failed", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

            Intent i = new Intent(this, SignUp.class);
            i.putExtra("UserID4mSU", user.getUid());
            i.putExtra("PhotoURL4mSU", user.getPhotoUrl());
            i.putExtra("UserName4mSU", user.getDisplayName());
            i.putExtra("EmailID4mSU", user.getEmail());
            i.putExtra("PhoneNumber4mSU", user.getPhoneNumber());
            startActivity(i);
            SignIn.this.finish();
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    private void recoverUserInfo() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.SIGN_UP.PREF_FILE, Context.MODE_PRIVATE);
        if (sharedPref.contains(Constants.SIGN_UP.USER_ID) && sharedPref.contains(Constants.SIGN_UP.USER_NAME) && sharedPref.contains(Constants.SIGN_UP.EMAIL_ID) && sharedPref.contains(Constants.SIGN_UP.PHONE_NUMBER) && sharedPref.contains(Constants.SIGN_UP.OFFICIAL_ID) && sharedPref.contains(Constants.SIGN_UP.OFFICER)) {

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

            Intent i = new Intent(this, SignUp.class);
            i.putExtra("UserID4mSU", sharedPref.getString(Constants.SIGN_UP.USER_ID, "DEFAULT"));
            //i.putExtra("PhotoURL4mSU", sharedPref.getString(Constants.SIGN_UP.PHOTO_URL, "DEFAULT"));
            i.putExtra("UserName4mSU", sharedPref.getString(Constants.SIGN_UP.USER_NAME, "DEFAULT"));
            i.putExtra("EmailID4mSU", sharedPref.getString(Constants.SIGN_UP.EMAIL_ID, "DEFAULT"));
            i.putExtra("PhoneNumber4mSU", sharedPref.getString(Constants.SIGN_UP.PHONE_NUMBER, "DEFAULT"));
            startActivity(i);
            SignIn.this.finish();
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            Snackbar.make(findViewById(R.id.sign_in_layout), "You should login atleast once with the data turned on", Snackbar.LENGTH_SHORT).show();
        }
    }

//    public static boolean isNetworkAvailable () {
//        boolean success = false;
//        try {
//            URL url = new URL("https://google.com");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setConnectTimeout(10000);
//            connection.connect();
//            success = connection.getResponseCode() == 200;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return success;
//    }
}