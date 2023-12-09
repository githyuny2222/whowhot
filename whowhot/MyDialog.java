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
    static final String configFileName = "config.txt";  // config 파일 불러오기용

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bun = getIntent().getExtras();
        if(bun != null) { danger = bun.getInt("danger"); }

        alert_config = getConfig();
        Log.d(TAG, "config 값 : "+alert_config);

        if(alert_config == -1){ // -1이면 파일 없을때
            Log.d(TAG, "alert_config value error");
            onDestroy();
        }

        setContentView(R.layout.dialog_background_round);
        TextView adMessage = (TextView) findViewById(R.id.confirmTextView);
        Button yesButton = (Button) findViewById(R.id.yesButton);
        Button detailButton = (Button) findViewById(R.id.detailButton);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background2);

        // config 2면 전부, 1이면 2일때만, 0이면 안함
        // 위험도 2일때 위험 1일때 주의
        if(alert_config == 0){ Log.d(TAG, "'안함'"); finish(); }    // config 0이면 다이얼로그 아예 안띄움
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
    }

    /* Dialog 호출 */
    public void callDialog(Context context, int danger){
        Log.d(TAG, "callDialog danger : " +danger);
        Bundle bun = new Bundle();
        bun.putInt("danger", danger);
        bun.putInt("alert_config", alert_config);
        Intent popupIntent = new Intent(context, MyDialog.class);
        popupIntent.putExtras(bun);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, popupIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        try {
            pendingIntent.send(); // MyDialog로 보내기
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            Log.e(TAG, "DIALOG Error: " + e.getMessage(), e);
        }
    }

    /* 컨피그를 읽어와서 alert_config 설정하는 함수 */
    private int getConfig() {
        File file = new File(getFilesDir(), configFileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String readStr = "";

        if (file.exists()) { // 파일이 존재하면
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                String str = "";
                while ((str = bufrd.readLine()) != null) {   // 파일 한줄씩 읽음
                    readStr += str + "\n";
                }
                // 파일 닫음
                bufrd.close();
                fr.close();
                // 숫자만 추출해줌
                int intStr = Integer.valueOf(readStr.replaceAll("[^0-9]", ""));
                return intStr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "FILE OPEN ERROR : " + e);
            }
        }
        return -1;
    }
}
