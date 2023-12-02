package com.example.whowhot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Intent serviceIntent;
    private Button btnWhiteList, btnLogList, btnStartBaseService;

    private static final String TAG = "TEST_MAIN";   // Log용 태그

    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnWhiteList = (Button)findViewById(R.id.btnWhiteList);
        btnLogList = (Button)findViewById(R.id.btnLogList);
        btnStartBaseService = (Button)findViewById(R.id.btnStartService);

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
                    btnStartBaseService.setText("서비스 켜기");
                }
                else{
                    startBaseService();
                    btnStartBaseService.setText("서비스 끄기");
                }
            }
        });
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