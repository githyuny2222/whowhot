package com.example.whowhot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Intent serviceIntent;
    private Button btnWhiteList, btnLogList, btnStartBaseService, btnHelp;
    private TextView txtOnOff;

    private static final String TAG = "TEST_MAIN";   // Log용 태그

    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnWhiteList = (Button)findViewById(R.id.btnWhiteList);
        btnLogList = (Button)findViewById(R.id.btnLogList);
        btnStartBaseService = (Button)findViewById(R.id.btnStartService);
        btnHelp = (Button)findViewById(R.id.btn_info) ;
        txtOnOff = (TextView)findViewById(R.id.txt_onoff);

        // 퍼미션 받기
        permissionCheck();

        /* 버튼 클릭하면 화이트리스트 액티비티 불러옴 */
        btnWhiteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WhiteListActivity.class);
                startActivity(intent);
            }
        });

        /* 버튼 클릭하면 상세정보 액티비티 불러옴 */
        btnLogList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                startActivity(intent);
            }
        });

        // 베이스 서비스 버튼
        btnStartBaseService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BaseService.isServiceRunning(getApplicationContext())) {
                    stopBaseService();
                    txtOnOff.setText("실시간 탐지 OFF");
                }
                else{
                    startBaseService();
                    txtOnOff.setText("실시간 탐지 ON");
                }
            }
        });
    }

    // ? 버튼
    public void onHelpClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String str = "모두 : 스미싱 의심/위험 메시지를 탐지해요\n일부 : 스미싱 위험 메시지만 탐지해요\n안함 : 스미싱을 탐지하지 않아요";
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#5F00FF")), 9, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1414")), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1414")), 34, 36, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#1BAA0B")), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#DDBB44")), 25, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1414")), 47, 49, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle("탐지 범위 설정이란?").setMessage(ssb);

        builder.setPositiveButton("알겠어요!", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                //Log.d(TAG, "알겠어요!");
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 키보드 숨기기
    void keyBordHide() {
        Window window = getWindow();
        new WindowInsetsControllerCompat(window, window.getDecorView()).hide(WindowInsetsCompat.Type.ime());
    }

    public void startBaseService(){
        Log.d(TAG,"Base Service 서비스 실행");
        serviceIntent = new Intent(this, BaseService.class);
        startService(serviceIntent);
    }

    public void stopBaseService(){
        Log.d(TAG,"Base Service 종료");
        serviceIntent = new Intent(this, BaseService.class);
        stopService(serviceIntent);
    }

    // 권한 체크
    private void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);

        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            //권한 요청
            permission.requestPermission();
        }
    }

    // Request Permission에 대한 결과 값 받아와
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            permission.requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

