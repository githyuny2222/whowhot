package com.example.whowhot;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainService extends Service {
    private static final String TAG = "TEST_MAIN_SERVICE";   // log용 태그
    private static final String fileName = "log.txt";    // 로그 저장 파일 이름
    private static final String[] domainFilter = {".com", ".net", ".kr", ".org", ".us"};    // 도메인 검사용
    private static final String REGEX ="(http(s)?:\\/\\/|www.)?(([a-z0-9\\w])(\\.*))+[a-z-]{2,4}([\\/a-z0-9-%#º@?&=+\\w])+(\\.[a-z\\/]{2,4}(\\?[\\/a-z0-9-@%#?&=\\w]+)*)?([가-힣])*";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Data");

    public MainService() { }

    @Override
    public IBinder onBind(Intent intent) { throw new UnsupportedOperationException("Not yet implemented");}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart Main Service");

        TestThread thread = new TestThread();
        thread.start(); // 스레드 시작

        String[] msgs = intent.getStringArrayExtra("msgs"); // 브로드캐스트 리시버에서 메시지 받음
        if(msgs != null) {
            Log.d(TAG, "sender :" +msgs[0]);
            Log.d(TAG, "content :" +msgs[1]);
            checkMessageAndNotify(getApplicationContext(), msgs);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    public String parseURL(String content){
        Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);
        String url = "";
        while (urlMatcher.find()) {
            String urls= urlMatcher.group();
            if (urls.contains(".")) {
                url = urls;
            }
        }
        return url;
    }

    /* URL 화이트리스트 검사 */
    public boolean isWhite(String targetURL) {
        return false;
    }

    /* DB에 추가 */
    private void addToDB(String phoneNumber, UrlData url) {
        myRef.child(phoneNumber).setValue(url);
        Log.d(TAG, "DB 추가");
    }

    /* URL 블랙리스트 검사 */
    public void checkBlacklist(Context context, String targetURL) {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "DB 호출");
                // DB 읽어서 저장된 악성 URL 목록 꺼내오기
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UrlData currData = postSnapshot.getValue(UrlData.class);
                    String currURL = currData.getURL();
                    //Log.d(TAG, "currURL : "+currURL);

                    if (targetURL.equals(currURL)) {
                        Log.d(TAG, "블랙리스트에서 발견");
                        // 사용자에게 알림
                        callDialog(context, currData.getDanger());
                        onDestroy();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    /* URL 검사 */
    public boolean isSafeURL(Context context, String targetURL){
        /* 먼저 화이트 리스트에 걸리는지 체크 */
        if (isWhite(targetURL)) {
            Log.d(TAG, "화이트 리스트에 있음 : 안전");
            return true;
        }

        /* DB검사 */
        checkBlacklist(context, targetURL);

        return false;
    }

    /* 도메인 양식 검사 */
    public boolean isNormalDomain(String url, String[] domainFilter) {
        for (String s : domainFilter) {
            if(url.contains(s)) { // 정상 도메인 발견
                Log.d(TAG, "Domain form found : " + "'" + s + "'");
                return true;   // 정상 도메인 양식 발견
            } else {
                //Log.d(TAG, s + " Not exist");
                continue;
            }
        }
        //Log.d(TAG, "도메인 양식 비정상");
        return false;   // 정상 도메인 양식이 발견 안됨
    }

    /* VirusTotal 검사. 걸린 숫자 리턴 */
    public int checkVirusTotal(String targetURL){
        VirustotalThread vtThread = new VirustotalThread();    // 쓰레드 만듬

        Log.d(TAG,"Virus Total 실행");
        vtThread.setIsRunning(true);
        vtThread.setTargetUrl(targetURL);
        vtThread.start();

        // 결과값 나올때까지 루프
        int result;
        while(true) {
            result = vtThread.getResult();
            if(result < 0) {
                continue;
                //Log.i(TAG, Integer.toString(vtThread.getResult()));
            }
            else {
                Log.d(TAG, "VirusTotal 결과 : "+Integer.toString(result));
                Log.d(TAG, "VirusTotal Thread 중지");
                vtThread.interrupt();
                break;
            }
        }
        return vtThread.getResult();
    }

    /* 추가 검사, 위험도 리턴 */
    public int advancedURLTest(String targetURL){
        int danger = 0;

        // Virus Total 발견되었을 때 위험도 1점 추가
        if(checkVirusTotal(targetURL) > 0){
            Log.d(TAG, "checkVirusTotal : 걸림");
            danger += 1;
        }

        // 정상 도메인이 발견되지 않았을 때 위험도 1점 추가
        if(!isNormalDomain(targetURL, domainFilter)){
            Log.d(TAG, "checkDaminForm : 걸림");
            danger += 1;
        }

        return danger;
    }

    public void notify(Context context, int danger, String phoneNumber, String targetURL){
        if (danger == 0) {
            Log.d(TAG, "위험도 0 : 가벼운 경고");
            Toast.makeText(context.getApplicationContext(), "경고경고", Toast.LENGTH_LONG).show();
        } else {   // 위험도가 1보다 클때
            Log.d(TAG, "위험도 1 : Dialog 호출");
            callDialog(context, danger); // Dialog호출

            /* DB에 추가 */
            UrlData url = new UrlData(danger, targetURL);
            addToDB(phoneNumber, url);
        }
    }

    /* 메시지 위험도 검사하고 알림 */
    private void checkMessageAndNotify(Context context, String[] message) {
        int danger = 0; // 위험도
        String sender = message[0];  //Log.d(TAG, "sender : "+sender);
        String content = message[1]; //Log.d(TAG, "content : "+content);

        /* URL 검사 */
        String targetURL = parseURL(content); // 메시지 내용에서 URL 추출
        Log.d(TAG, "targetURL : " + targetURL);
        if(!targetURL.equals("") && !isSafeURL(context, targetURL)) { // 안전한 URL이 아니면
            danger += advancedURLTest(targetURL);   // 추가 검사하고 위험도 추가
        }

        /* 메시지 내용 검사 */
        danger += checkSafeContent(content);

        /* 위험도에 따라 사용자에게 알림 */
        notify(context, danger, sender, targetURL);
    }

    /* 메시지 내용 검사하는 함수(공공기관 사칭 메시지 등) */
    public int checkSafeContent(String content){
        int danger = 0;
        if(contentTest1(content)){ danger += 1; }
        if(contentTest2(content)){ danger += 1; }

        return danger;
    }

    /* 메시지 String에서 위험도 판별하는 알고리즘 함수 예시 */
    public boolean contentTest1(String content) { return false; }
    public boolean contentTest2(String content) { return false; }

    /* Dialog 호출 */
    public void callDialog(Context context, int danger){
        Log.d(TAG, "callDialog " +danger);
        Bundle bun = new Bundle();
        bun.putInt("danger", danger);
        Intent popupIntent = new Intent(context, AlertDialog.class);
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

    private void loadWhiteListFromFile(){ //파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시
        Log.d(TAG, "Load Whitelist");

        File file = new File(getFilesDir(), fileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str;

        Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        Log.d(TAG, "Path : "+file.getPath());
        Log.d(TAG, "Filename : "+file.getName());

        if (file.exists()){ //파일이 존재하면
            Log.d(TAG, "file exists");
            try { //open file
                Log.d(TAG, "before open file");
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                Log.d(TAG, "before readLine");
                while ((str = bufrd.readLine()) != null){
                    // 여기에 추가할 동작
                    Log.d(TAG, str);
                }
                //file reader close
                Log.d(TAG, "close file");
                bufrd.close();
                fr.close();
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "FILE ERROR : "+e);
            }
        }
        else{
            Log.d(TAG, "file don't exist");
        }
    }

    private void appendToFile(String addText, String fileName){ //리스트뷰의 리스트들을 파일에 저장하는 함수
        File file = new File(getFilesDir(), fileName);
        FileWriter fileWriter = null;

        Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        Log.d(TAG, "Path : "+file.getPath());
        Log.d(TAG, "Filename : "+file.getName());

        Log.d(TAG, "Append");
        try { //open file
            fileWriter = new FileWriter(file, true);
            fileWriter.write(addText+"\n");
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

    private class TestThread extends Thread {
        private static final String TAG = "TestThread";
        private int cnt = 0;

        public TestThread() {
            cnt = 0;
        }

        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                //Log.d(TAG,"서비스 실행중 "+cnt);
                try {
                    cnt++;
                    Thread.sleep(1000);
                    if (cnt > 3) {
                        this.interrupt();
                        onDestroy();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    this.interrupt();
                    Log.e(TAG, "SERVICE ERROR : " + e);
                }
            }
        }
    }

}

