package com.example.whowhot;

import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

public class CheckBlack {
    private static final String TAG = "TEST_CHECK_BLACK";
    private boolean isBlack;

    public CheckBlack() {
    }

    public CheckBlack(boolean isBlack) {
        this.isBlack = isBlack;
    }

    public boolean check(DataSnapshot dataSnapshot, String targetURL){
        Log.d(TAG, "check() function");
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            UrlData urlData = snapshot.getValue(UrlData.class);
            String currURL = urlData.getURL();
            Log.d(TAG, "currURL : " + currURL);

            //비교
            if(currURL.equals(targetURL)){
                Log.d(TAG, "발견! targetURL : " + targetURL);
                return true;
            }
        }
        Log.d(TAG, "발견 안됨! targetURL : " + targetURL);
        return false;
    }

    public boolean isBlack() {
        return isBlack;
    }

    public void setBlack(boolean black) {
        isBlack = black;
    }
}
