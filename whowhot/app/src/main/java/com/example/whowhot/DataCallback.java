package com.example.whowhot;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface DataCallback {
    void onSuccess(DataSnapshot dataSnapshot);
    void onError(DatabaseError databaseError);
}

