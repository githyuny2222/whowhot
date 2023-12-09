package com.example.whowhot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainService extends Service {
    /* 탐지 유형별 Code */
    private static final int DOM = 1;
    private static final int KW = 2;
    private static final int VT = 3;
    private static final int CRD = 4;
    private static final int BNK = 5;
    private static final int GOV = 6;
    private static final int DLV = 7;
    private static final int WHITE = 100;
    private static final int BLACK = 200;

    private static final String TAG = "TEST_MAIN_SERVICE";   // log용 태그
    private static final String logFileName = "log.txt";    // 로그 파일 이름
    private static final String whitelistFileName = "WhiteList.txt";    // 화이트리스트 파일 이름
    private static final String[] domainFilter = {".com", ".net", ".kr", ".org", ".us", ".ng", ".biz", ".info"};    // 도메인 검사용

    DatabaseManager databaseManager = new DatabaseManager("Data");

    public MainService() { }

    @Override
    public IBinder onBind(Intent intent) { throw new UnsupportedOperationException("Not yet implemented");}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart Main Service");

        // 브로드캐스트 리시버에서 메시지 받음
        String[] msgs = intent.getStringArrayExtra("msgs");
        if(msgs != null) {  // 빈 메시지가 아니면
            //Log.d(TAG, "sender :" +msgs[0]);
            //Log.d(TAG, "content :" +msgs[1]);
            testA_URL(getApplicationContext(), msgs);   // 판별 함수 실행
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

    /* URL 화이트리스트 검사 */
    private boolean isWhite(String targetURL){ //파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시
        boolean iswhite_flag = false;

        File file = new File(getFilesDir(), whitelistFileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str_in_whitelist;

        if (file.exists()){ //파일이 존재하면
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                while ((str_in_whitelist = bufrd.readLine()) != null){
                    //Log.d(TAG, "str_in_whitelist : "+str_in_whitelist);
                    if(targetURL.contains(str_in_whitelist)){   // URL이 화이트리스트에 있으면
                        iswhite_flag = true;
                        break;
                    }
                }

                //file reader close
                bufrd.close();
                fr.close();
                return iswhite_flag;
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "FILE ERROR : "+e);
            }
        }
        else{
            Log.d(TAG, "File don't exist");
        }
        return false;
    }

    /* URL 블랙리스트 검사 */
    public void isBlack(Context context, String sender, String content, String targetURL){
        Log.d(TAG, "DB 검사 실행");

        // Read DB
        databaseManager.readDataOnce(new DataCallback() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                // 데이터 처리
                boolean result_black = false;   // 블랙리스트인지 판단할 변수
                if(dataSnapshot != null) {  // DB에 아무것도 없으면 블랙리스트 아님
                    CheckBlack checkBlack = new CheckBlack();   // 블랙리스트 판별 클래스
                    result_black = checkBlack.check(dataSnapshot, targetURL);  // 블랙리스트 검사해서 bool값 줌
                    Log.d(TAG, "result_black : "+result_black);
                }
                if(result_black){
                    Log.d(TAG, "블랙이니까 그냥 통과시킴");
                }
                else{
                    int type = advancedURLTest(targetURL);
                    testB_Sender(context, sender, content, type);
                }
            }

            @Override
            public void onError(DatabaseError databaseError) {
                // 오류 처리
                System.out.println("Read data failed: " + databaseError.toException());
            }
        });
    }
    public void little_function(){
        Log.d(TAG, "Test little function!");
    }

    /* VirusTotal 검사. 걸린 숫자 리턴 */
    public int checkVirusTotal(String targetURL){
        VirustotalThread vtThread = new VirustotalThread();    // 쓰레드 만듬

        //Log.d(TAG,"Virus Total 실행");
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
                //Log.d(TAG, "VirusTotal 결과 : "+Integer.toString(result));
                //Log.d(TAG, "VirusTotal Thread 중지");
                vtThread.interrupt();
                break;
            }
        }
        return vtThread.getResult();
    }

    /* 도메인 양식 검사 */
    public boolean isNormalDomain(String url, String[] domainFilter) {
        for (String s : domainFilter) {
            if(url.contains(s)) { // 정상 도메인 발견
                //Log.d(TAG, "Domain form found : " + "'" + s + "'");
                return true;   // 정상 도메인 양식 발견
            } else {
                //Log.d(TAG, s + " Not exist");
                continue;
            }
        }
        //Log.d(TAG, "도메인 양식 비정상");
        return false;   // 정상 도메인 양식이 발견 안됨
    }

    /* 추가 검사, 메시지 유형 리턴 */
    public int advancedURLTest(String targetURL){
        int type = 0;

        // Virus Total 검사
        if(checkVirusTotal(targetURL) > 0){
            Log.d(TAG, "1.4 VirusTotal 검사 결과 : 위험");
            type = VT;
        }else{ Log.d(TAG, "1.4 VirusTotal 검사 결과 : 양호"); }

        // 도메인 양식 검사
        if(!isNormalDomain(targetURL, domainFilter)){
            Log.d(TAG, "1.5 도메인 양식 검사 결과 : 위험");
            type = DOM;
        } else{ Log.d(TAG, "1.5 도메인 양식 검사 결과 : 양호"); }

        return type;
    }

    /* 검사 1. URL 검사 : 화이트리스트, 블랙리스트, 추가검사로 URL 판별 */
    public int checkURL(Context context, String sender, String content){
        Log.d(TAG, "------------------------ 1.URL검사 -----------------------------");
        int type = 0; // 기본 0, 화이트 100, 블랙 200, 추가검사 type
        Parser parser = new Parser();   // 파싱 해주는 클래스

        String targetURL = parser.parseURL(content); // 메시지 내용에서 URL 추출

        if(targetURL.equals("")) { // URL이 없는 메시지일때
            Log.d(TAG, "1.1 URL 탐색 결과 : 비어있는 URL");
            return -1;
        }
        Log.d(TAG, "1.1 URL 탐색 결과 : " + targetURL);

        /* 화이트리스트 -> 블랙리스트 -> 추가검사 */
        if(isWhite(targetURL)) { // 화이트리스트에 걸리면 리턴
            Log.d(TAG, "1.2 화이트리스트 검사 : 화이트리스트에 존재하는 URL");
            return 100;
        }
        Log.d(TAG, "1.2 화이트리스트 검사 : 화이트리스트에 존재하지 않는 URL");

        isBlack(context, sender, content, targetURL);
        //Log.d(TAG, "1.3 블랙리스트 검사 : 블랙리스트에 존재하는 URL");
        return 200; // isBlack()에서 빠져나오면 URL도 있고 white에도 없는 경우임
    }

    /* 검사 2. 발신자 검사 : 사칭 메시지를 색출 */
    public int checkSender(String sender, String content){
        int type = 0;
        SenderChecker senderChecker = new SenderChecker();
        type = senderChecker.checkSender(sender, content);
        return type;
    }

    /* 검사 3. 키워드 검사 : 메시지 내용의 String을 검사 */
    public int checkContent(String content){
        int type = 0;
        String targetPhone = "";
        Parser parser = new Parser();

        targetPhone = parser.parsePhone(content); // 메시지에서 전화번호 파싱
        Log.d(TAG,"Phone Num: " + targetPhone);

        return type;
    }

    /* 위험도 받아서 경고 다이얼로그 출력하는 함수 */
    public void alert(Context context, int danger){
        if (danger == 0) {
            Log.d(TAG, "위험도 0");
        } else {   // 위험도가 1보다 클때
            Log.d(TAG, "위험도 1이상 : Dialog 호출");
            MyDialog dialog = new MyDialog();
            dialog.callDialog(context, danger); // Dialog호출
        }
    }

    /* Type에 따라 Danger 설정 */
    public int setDanger(int type){
        int danger=0;
        switch (type){
            case 0: break;
            case DOM: danger=1; break;
            case KW: danger=1; break;
            case CRD: danger=2; break;
            case BNK: danger=2; break;
            case GOV: danger=2; break;
            case DLV: danger=2; break;
            case VT: danger=2; break;
        }
        return danger;
    }

    /* DB 때문에 순차적으로 검사해야됨 */
    private void testA_URL(Context context, String[] message) {
        Log.d(TAG, "TestA!!");
        int danger = 0; // 위험도
        int type = 0;   // 메시지 유형 코드
        String type_str = "";   // 메시지 유형 문자열
        String sender = message[0]; // 발신자
        String content = message[1]; // 메시지 내용

        /* 검사 1. URL 검사 */
        int result_checkURL = checkURL(context, sender, content);
        if(result_checkURL == -1){ Log.d(TAG, "기본 URL판별 결과 : URL 발견되지 않음"); result_checkURL=0; }
        if(result_checkURL == WHITE){ Log.d(TAG, "기본 URL 판별 결과 : 화이트리스트에 있음"); return; }
        if(result_checkURL == BLACK){ Log.d(TAG, "기본 URL 판별 결과 : 추가 검사 필요"); return; }
        type = result_checkURL; // 화이트, 블랙 아니면 type 반환하고 sender검사

        // advanced test는 black에서 하니까 여기서는 -1인 경우만 있음
        testB_Sender(context, sender, content, type);
    }

    /* 이렇게 나누면 순사적으로 실행하지 않을까 */
    public void testB_Sender(Context context, String sender, String content, int type){
        Log.d(TAG, "TestB!!");
        int danger = 0;

        /* 검사 2. 발신자 검사 */
        int result_checkSender = checkSender(sender, content);
        if(type < result_checkSender){ // 발신자 검사 결과 걸렸을 경우
            Log.d(TAG, "발신자 검사 : 걸림" + type + result_checkSender);
            type = result_checkSender;
        }
        else {  // 검사1, 검사2 둘다 안걸렸을 경우
            /* 검사 3. 메시지 키워드 검사 */
            int result_checkContent = checkContent(content);
            if(type < result_checkContent){ // 발신자 검사 결과 걸렸을 경우
                Log.d(TAG, "키워드 검사 : 걸림");
                type = result_checkSender;
            }
            else{
                Log.d(TAG, "키워드 검사 : 양호");
            }
        }

        /* 위험도 산출 */
        danger = setDanger(type);

        /* 로깅 및 DB추가 */
        logging(sender, type);
        if(danger > 0){ addToDB(sender, content, danger, type); } // 위험도 0은 패스, 1,2는 저장

        /* 위험도에 따라 사용자에게 알림 */
        alert(context, type);
    }

    /* type 코드로 받아서 log에 저장할 String으로 변환 */
    private String convertTypeToString(int type) {
        String type_str="";
        switch(type){
            case 0: type_str="안전한 문자"; break;
            case CRD: type_str="카드사 사칭 스미싱"; break;
            case BNK: type_str="은행사 사칭 스미싱"; break;
            case GOV: type_str="정부기관 사칭 스미싱"; break;
            case DLV: type_str="택배사 사칭 스미싱"; break;
            case VT: type_str="위험한 악성 URL"; break;
            case DOM: type_str="의심스러운 도메인"; break;
            case KW: type_str="의심스러운 내용의 메시지"; break;
        }
        return type_str;
    }

    /* log 파일에 저장하는 함수 */
    private void appendToLogFile(String addText){ // 파일에 로그 저장
        File file = new File(getFilesDir(), logFileName);
        FileWriter fileWriter = null;

        //Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        //Log.d(TAG, "Path : "+file.getPath());
        //Log.d(TAG, "Filename : "+file.getName());

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

    /* 로그 저장하는 함수 */
    private void logging(String sender, int type){
        String logStr = ""; // 추가할 log

        // 날짜 시각 구하기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        String time = sdf.format (System.currentTimeMillis());
        //Log.d(TAG, time);

        logStr += time + "\\";
        logStr += "발신자 : " + sender + "\\";
        logStr += "판별 결과 : " + convertTypeToString(type) + "\n";

        appendToLogFile(logStr); // 로그에 추가 "날짜시간\발신자\유형"
        Log.d(TAG, "logging : 로그 추가");
    }

    /* DB에 추가하는 함수 */
    private void addToDB(String sender, String content, int danger, int type) {
        Parser parser = new Parser();
        String url = parser.parseURL(content);
        UrlData urlData = new UrlData(danger, url, type);

        databaseManager.writeData(sender, urlData);

        Log.d(TAG, "[DB 추가] sender : " + sender + ", URL : " + url + ", danger : " + danger + ", type : " + type);
    }
}

