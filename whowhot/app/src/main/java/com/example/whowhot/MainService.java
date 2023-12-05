package com.example.whowhot;

import static java.lang.String.format;

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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainService extends Service {
    private static final String TAG = "TEST_MAIN_SERVICE";   // log용 태그
    private static final String logfileName = "log.txt";    // 로그 저장 파일 이름
    private static final String[] domainFilter = {".com", ".net", ".kr", ".org", ".us", ".ng", ".biz", ".info"};    // 도메인 검사용
    private static final String REGEX ="(http(s)?:\\/\\/|www.)?(([a-z0-9\\w])(\\.*))+[a-z-]{2,4}([\\/a-z0-9-%#º@?&=+\\w])+(\\.[a-z\\/]{2,4}(\\?[\\/a-z0-9-@%#?&=\\w]+)*)?([가-힣])*";
    private static final String phoneREGEX = "\\b(\\+?[0-9]+[-.\\s]?\\(?[0-9]+\\)?[-.\\s]?[0-9]+[-.\\s]?[0-9]+[-.\\s]?[0-9]+)\\b";
    private static final String[] msgCardFilter = { "BC카드", "KB국민카드", "NH농협카드", "롯데카드", "삼성카드",
            "신한카드", "우리카드", "하나카드", "현대카드" };
    private static final String[][] msgCardPhone = {{"BC카드", "15884000", "15664000"}, {"KB국민카드","15881688"}, {"NH농협카드","16444000"}, {"롯데카드", "15888100"},
            {"삼성카드", "15888700"}, {"신한카드", "15447000"}, {"우리카드", "15889955", "15999955"}, {"하나카드", "18001111"},{"현대카드", "15776000"}};

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
            //Log.d(TAG, "sender :" +msgs[0]);
            //Log.d(TAG, "content :" +msgs[1]);
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
    private boolean isWhite(String targetURL){ //파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시
        Log.d(TAG, "Load Whitelist");
        boolean iswhite_flag = false;

        File file = new File(getFilesDir(), "WhiteList.txt");
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str;

        //Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        //Log.d(TAG, "Path : "+file.getPath());
        //Log.d(TAG, "Filename : "+file.getName());

        if (file.exists()){ //파일이 존재하면
            try { //open file
                Log.d(TAG, "file open");
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                while ((str = bufrd.readLine()) != null){
                    Log.d(TAG, "WhiteList : "+str);
                    if(targetURL.equals(str)){
                        iswhite_flag = true;
                        Log.d(TAG, "화이트리스트에 있는 URL");
                        break;
                    }
                }

                //file reader close
                Log.d(TAG, "close file");
                bufrd.close();
                fr.close();
                return iswhite_flag;
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "FILE ERROR : "+e);
            }
        }
        else{
            Log.d(TAG, "file don't exist");
        }
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
        if (isWhite(targetURL)) { // 화이트리스트에 걸리면 안전
            return true;
        }
        else { // 화이트 리스트에 안걸리면
            /* DB검사 */
            checkBlacklist(context, targetURL);
            return false;
        }
    }

    /* 메시지에서 전화번호 파싱 */
    public String parsePhone(String content){
        Pattern pattern = Pattern.compile(phoneREGEX);
        Matcher phoneMatcher = pattern.matcher(content);
        if (phoneMatcher.find()){
            return phoneMatcher.group();
        }
        else
            return "";
    }

    /* 메시지 String에서 위험도 판별하는 알고리즘 함수 예시 */
    public boolean contentTest1(String content) { return false; }
    public boolean contentTest2(String content) { return false; }

    /*발신자 전화번호가 해당 카드사가 아닌 것 판별하는 함수*/
    public boolean isNormalCardPhone(String sender, String content){
        for (String[] cardInfo : msgCardPhone){
            if (content.contains(cardInfo[0])){ // 만약 문자 중에 배열에 있는 카드가 존재하면 cardInfo[0]에 포함
                Log.d(TAG,"카드 발견: " + cardInfo[0]);
                for (String phoneInfo : cardInfo) {
                    if (sender.contains(phoneInfo)) { //발신자 전화번호가 해당 카드사의 전화번호와 일치하면 log를 출력
                        Log.d(TAG, "발신자 번호: " + phoneInfo);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* 메시지 내용이 안전한지 검사하는 함수 */
    public int checkSafeContent(String sender,String content, String targetPhone){
        int danger = 0;
        if(contentTest1(content)){ danger += 1; }
        if(contentTest2(content)){ danger += 1; }
        if(!isNormalCardPhone(sender, content)) {danger += 1;}
        return danger;
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

    /* 위험도 받아서 경고 다이얼로그 출력하는 함수 */
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
        int type = 0;   // 문자 유형
        String sender = message[0]; // 발신자
        String content = message[1]; // 메시지 내용

        /* URL 검사 */
        String targetURL = parseURL(content); // 메시지 내용에서 URL 추출
        Log.d(TAG, "targetURL : " + targetURL);
        if(!targetURL.equals("")) { // 메시지에 URL이 존재할때
            if (isSafeURL(context, targetURL)) { // 안전한 URL이면
                Log.d(TAG, "안전한 URL");
                return;
            }
            else{   // 안전한 URL이 아니면
                danger += advancedURLTest(targetURL);   // 추가 검사하고 위험도 추가
                String txt = ""; // 로그에 넣을 스트링
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
                String time = sdf.format (System.currentTimeMillis());
                Log.d("Test", time);
                txt += time + "\\";
                txt += sender + "\\";

                String str_type = "default";
                switch (type){
                    case 0: str_type = "0"; break;
                    case 1: str_type = "1"; break;
                    case 2: str_type = "2"; break;
                    case 3: str_type = "3"; break;
                    case 4: str_type = "4"; break;
                }
                txt += str_type;

                //appendToLogFile(txt,"log.txt"); // 로그에 추가 "날짜시간\발신자\유형"
            }
        }

        /* 메시지 내용 검사 */
        String targetPhone = parsePhone(content);
        Log.d(TAG,"Phone Num: " + targetPhone);
        danger += checkSafeContent(content, sender, targetPhone);

        /* 위험도에 따라 사용자에게 알림 */
        notify(context, danger, sender, targetURL);
    }


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

    /* log 저장하는 함수 */
    private void appendToLogFile(String addText, String fileName){ //리스트뷰의 리스트들을 파일에 저장하는 함수
        File file = new File(getFilesDir(), fileName);
        FileWriter fileWriter = null;

        Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        Log.d(TAG, "Path : "+file.getPath());
        Log.d(TAG, "Filename : "+file.getName());

        Log.d(TAG, "Append");
        try { //open file
            fileWriter = new FileWriter(file, true);
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

    /* 그냥 살아있나 확인하는 쓰레드 */
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
                    if (cnt > 3) {  // 3초 지났으면 꺼짐
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

