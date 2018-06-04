package com.hornetincorporation.beatroute;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface checkSignUp {
    public void onStart();
    public void onSuccess(DataSnapshot data);
    public void onFailed(DatabaseError databaseError);
}
