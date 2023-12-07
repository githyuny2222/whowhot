package com.example.whowhot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Intent serviceIntent;
    private Button btnWhiteList, btnLogList, btnStartBaseService, btnHelp;
    private TextView txtOnOff;
    private SeekBar seekBar;
    private static final String configFileName = "config.txt";

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
        seekBar = (SeekBar)findViewById(R.id.seekBar1);

        // 퍼미션 받기
        permissionCheck();

        if(BaseService.isServiceRunning(getApplicationContext())) {
            txtOnOff.setText("실시간 탐지 ON");
            btnStartBaseService.setForeground(getResources().getDrawable(R.drawable.on,null));
            configload();
        }
        else{
            txtOnOff.setText("실시간 탐지 OFF");
            btnStartBaseService.setForeground(getResources().getDrawable(R.drawable.off,null));
            configload();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 시크바를 조작하고 있는 중
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 시크바를 처음 터치했을 때
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                configset(seekBar.getProgress());
            }
        });

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
                    btnStartBaseService.setForeground(getResources().getDrawable(R.drawable.off,null));
                }
                else{
                    startBaseService();
                    txtOnOff.setText("실시간 탐지 ON");
                    btnStartBaseService.setForeground(getResources().getDrawable(R.drawable.on,null));
                }
            }
        });
    }

    // ? 버튼
    public void onClickHelp(View view) {
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

    private void appendToconfigFile(String addText){ // 파일에 로그 저장
        File file = new File(getFilesDir(), configFileName);
        FileWriter fileWriter = null;

        try { //open file
            fileWriter = new FileWriter(file, false);
            fileWriter.write(addText);
            fileWriter.flush();
            Log.d(TAG, file.getAbsolutePath()+"에 저장");

        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "FILE OPEN ERROR : "+e);
        }

        try { //close file
            if (fileWriter != null){
                fileWriter.close();
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "FILE CLOSE ERROR : "+e);
        }
    }

    /* 컨피그 저장하는 함수 */
    private void configset(int seekset){
        String logStr = "config : " + seekset; // 추가할 log
        appendToconfigFile(logStr); // 로그에 추가 "날짜시간\발신자\유형"
        Log.d(TAG, "configset : 컨피그 설정 완료");
    }

    /* 컨피그를 읽어와서 시크바 설정하는 함수 */
    private void configload(){
        File file = new File(getFilesDir(), configFileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str = null;
        String readStr = "";
        int i = 0;
        
        if (file.exists()) { // 파일이 존재하면
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                while ((str = bufrd.readLine()) != null){   // 파일 한줄씩 읽음
                    readStr += str + "\n";
                }
                // 파일 닫음
                bufrd.close();
                fr.close();
                // 숫자만 추출해줌
                int intStr = Integer.valueOf(readStr.replaceAll("[^0-9]", ""));
                seekBar.setProgress(intStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "FILE OPEN ERROR : " + e);
                }
        }
    }

}

