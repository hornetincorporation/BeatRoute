package com.hornetincorporation.beatroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.annotations.NotNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUp extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "SignUpActivity";

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
        findViewById(R.id.backbutton).setOnClickListener(this);

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

    }

    @Override
    protected void onStart() {
        super.onStart();

        beeters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot beeterSnapshot : dataSnapshot.getChildren()) {
                    if (beeterSnapshot.exists()) {
                        if (beeterSnapshot.getKey().toString().equals(sUserId4mSU)) {
                            txUserName.setText(beeterSnapshot.child("BUserName").getValue().toString());
                            txEmailID.setText(beeterSnapshot.child("BEmailID").getValue().toString());
                            txPhoneNum.setText(beeterSnapshot.child("BPhoneNumber").getValue().toString());
                            txOfficialID.setText(beeterSnapshot.child("BOfficialID").getValue().toString());
                            txOfficer.setText(beeterSnapshot.child("BOfficer").getValue().toString());
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
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

                    Snackbar.make(findViewById(R.id.main_layout), "User Account created", Snackbar.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(findViewById(R.id.main_layout), "Please fill all fields.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.main_layout), "User Account already exists! Taking to your account.", Snackbar.LENGTH_SHORT).show();
                movetonextpage();
            }
        }
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

        Intent mintent = new Intent(this, MainActivity.class);

        mintent.putExtra("UserID", sUserId4mSU);
//          mintent.putExtra("PhotoURL4mSU", user.getPhotoUrl());
        mintent.putExtra("UserName", txUserName.getText().toString());
        mintent.putExtra("EmailID", txEmailID.getText().toString());
        mintent.putExtra("PhoneNumber", txPhoneNum.getText().toString());
        mintent.putExtra("OfficialID", txOfficialID.getText().toString());
        mintent.putExtra("Officer", txOfficer.getText().toString());

        startActivity(mintent);
    }
}