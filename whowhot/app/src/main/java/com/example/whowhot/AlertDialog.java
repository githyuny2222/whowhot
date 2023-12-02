package com.example.whowhot;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlertDialog extends Activity {
    private String notiMessage;
    private int danger;

    static final String TAG = "TEST_DIALOG";   // Log.d용 태그

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Data");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bun = getIntent().getExtras();
        danger = bun.getInt("danger");

        setContentView(R.layout.dialog_background_round);
        TextView adMessage = (TextView) findViewById(R.id.confirmTextView);
        Button adButton = (Button) findViewById(R.id.yesButton);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background2);

        // 여기에 알고리즘 넣어서 판별하면 됨.
        if (danger > 1) {
            adMessage.setText("위험");
            linearLayout.setBackground(getDrawable(R.drawable.view_round_red_6));
            adMessage.setTextColor(Color.BLACK);
        } else {
            adMessage.setText("경고");
            linearLayout.setBackground(getDrawable(R.drawable.view_round_blue_6));
            adMessage.setTextColor(Color.WHITE);
        }
        adButton.setOnClickListener(new SubmitOnClickListener());
    }

    private class SubmitOnClickListener implements OnClickListener {
        public void onClick(View v) {
            finish();
        }
    }
}
