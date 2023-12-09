package com.example.whowhot;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseManager {

    private DatabaseReference databaseReference;

    public DatabaseManager(String path) {
        this.databaseReference = FirebaseDatabase.getInstance().getReference(path);
    }

    public void readDataOnce(final DataCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        });
    }

    public void writeData(String key, UrlData value) {
        // 데이터 쓰기
        databaseReference.child(key).setValue(value);
    }
    // 다른 데이터베이스 작업을 추가할 수 있습니다.
}
