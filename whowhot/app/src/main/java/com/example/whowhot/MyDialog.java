package com.example.whowhot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MyDialog extends Activity {
    private int danger;
    private int alert_config;

    static final String TAG = "TEST_DIALOG";   // Log.d용 태그

    public MyDialog(){

    }

    public MyDialog(int danger, int alert_config) {
        this.danger = danger;
        this.alert_config = alert_config;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_background_round);

        Log.d(TAG, "MyDialog Created");

        danger = getIntent().getIntExtra("danger", -1);
        alert_config = getIntent().getIntExtra("alert_config", -1);

        if(danger == -1 || alert_config == -1){ Log.d(TAG, "정보를 못받아옴"); finish(); }

        TextView adMessage = (TextView) findViewById(R.id.confirmTextView);
        Button yesButton = (Button) findViewById(R.id.yesButton);
        Button detailButton = (Button) findViewById(R.id.detailButton);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background2);

        Log.d(TAG, "danger 값 : " + danger);
        Log.d(TAG, "config 값 : " + alert_config);

        if(alert_config == -1){ // -1이면 파일 없을때
            Log.d(TAG, "alert_config value error");
            onDestroy();
        }

        // config 2면 전부, 1이면 2일때만, 0이면 안함
        // 위험도 2일때 위험 1일때 주의
        if(alert_config == 0){ Log.d(TAG, "탐지 범위 설정 '안함'"); finish(); }    // config 0이면 다이얼로그 아예 안띄움
        if (danger > 1) {
            adMessage.setText("위험!\n위험한 메시지를 발견했습니다");
            linearLayout.setBackground(getDrawable(R.drawable.view_round_red_6));
            adMessage.setTextColor(Color.BLACK);
        } else {
            if(alert_config == 1){ Log.d(TAG, "'일부'"); finish(); } // config 1이면 위험도 1인건 다이얼로그 안띄움
            adMessage.setText("주의!\n의심스러운 메시지를 발견했습니다");
            linearLayout.setBackground(getDrawable(R.drawable.view_round_orange_6));
            adMessage.setTextColor(Color.BLACK);
        }

        yesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        detailButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                startActivity(intent);
            }
        });

        //requestWindowFeature(Window.FEATURE_NO_TITLE); // 이건 왜있는거지?
    }
}
