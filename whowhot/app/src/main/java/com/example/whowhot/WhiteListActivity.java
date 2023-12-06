package com.example.whowhot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class WhiteListActivity extends Activity {
    private static final String fileName = "WhiteList.txt";    // 화이트리스트 저장 파일 이름
    private static final String TAG = "TEST_WHITE";   // Log용 태그
    private static final String[] defaultWhitelist = {"youtube.com", "google.com", "naver.com",
            "namu.wiki", "dcinside.com", "tistory.com",
            "coupang.com", "daum.net", "twitch.tv", "fmkorea.com",
            "inven.co.kr", "arca.live", "kakao.com", "google.co.kr",
            "nexon.com", "instagram.com", "aliexpress.com", "ruliweb.com",
            "afreecatv.com", "twitter.com" };   // 화이트리스트에 2023년 가장 많이 접속된 사이트 top 20 기본 추가

    private Button btnAdd, btnDel, btnRet;
    private EditText urlInput;
    private ListView list;
    private ArrayAdapter adapter;
    private ArrayList<String> whiteList = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);
        Log.d(TAG,"====== WhiteListActivity ========");

        //final ArrayList<String> whiteList = new ArrayList<String>();
        urlInput = (EditText) findViewById(R.id.edit0);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDel = (Button) findViewById(R.id.btnDel);
        btnRet = (Button) findViewById(R.id.btnReturn);
        list = (ListView) findViewById(R.id.whitelist);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,whiteList);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setAdapter(adapter);

        loadWhiteListFromFile();
        adapter.notifyDataSetChanged();

        btnAdd.setEnabled(false);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strNew = (String) urlInput.getText().toString();

                if (strNew.length() > 0){ //입력받은 문자열이 있다면
                    whiteList.add(strNew); //whiteList에 추가
                    urlInput.setText(""); //빈 텍스트로 초기화
                    adapter.notifyDataSetChanged(); //리스트 뷰 갱신
                    saveWhiteListToFile(); //리스트 뷰에 있는 것 저장
                }
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cnt;
                int checkedIndex;

                cnt = adapter.getCount();

                if (cnt > 0){
                    checkedIndex = list.getCheckedItemPosition(); //체크된 리스트의 인덱스 가져오기
                    if (checkedIndex > -1 && checkedIndex < cnt){ //해당 조건에 만족하면
                        Log.d(TAG, whiteList.get(checkedIndex)+" 삭제");
                        whiteList.remove(checkedIndex); //리스트에서 지우고
                        list.clearChoices(); //체크된 리스트 초기화
                        adapter.notifyDataSetChanged(); //리스트뷰 갱신
                        saveWhiteListToFile(); //파일에 저장
                    }
                }
            }
        });

        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0){
                    btnAdd.setEnabled(true);
                }
                else {
                    btnAdd.setEnabled(false);
                }
            }
        });

        btnRet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveWhiteListToFile(){ //리스트뷰의 리스트들을 파일에 저장하는 함수
        File file = new File(getFilesDir(), fileName);
        FileWriter fw = null;
        BufferedWriter bufwr = null;

        try { //open file
            fw = new FileWriter(file);
            bufwr = new BufferedWriter(fw);

            for (String str : whiteList){
                bufwr.write(str);
                bufwr.newLine();
            }
            //write data to the file
            bufwr.flush();
            Log.d(TAG, file.getAbsolutePath()+"에 저장");

        } catch (Exception e){
            e.printStackTrace();
        }

        try { //close file
            if (bufwr != null){
                bufwr.close();
            }
            if (fw != null){
                fw.close();
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "save file Error : "+e);
        }
    }

    /* 파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시 */
    private void loadWhiteListFromFile(){
        Log.d(TAG, "loadWhiteList");
        File file = new File(getFilesDir(), fileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str;

        if (file.exists()){ //파일이 존재하면
            Log.d(TAG, "file exists!");
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                while ((str = bufrd.readLine()) != null){
                    whiteList.add(str);
                }
                //file close?
                bufrd.close();
                fr.close();
            } catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "load file Error : "+e);
            }
        }
        else{
            fillWhiteList();
            loadWhiteListFromFile();
        }
    }

    /* 화이트리스트를 자주 접속되는 사이트로 채워넣는 함수 */
    public void fillWhiteList(){
        Log.d(TAG, "fillWhiteList");
        File file = new File(getFilesDir(), fileName);
        FileWriter fw = null;
        BufferedWriter bufwr = null;

        try { //open file
            fw = new FileWriter(file);
            bufwr = new BufferedWriter(fw);

            for (String str : defaultWhitelist){
                bufwr.write(str);   // 기본 화이트리스트 추가
                bufwr.newLine();
            }
            //write data to the file
            bufwr.flush();
            Log.d(TAG, file.getAbsolutePath()+"에 저장");

        } catch (Exception e){
            e.printStackTrace();
        }

        try { //close file
            if (bufwr != null){
                bufwr.close();
            }
            if (fw != null){
                fw.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}