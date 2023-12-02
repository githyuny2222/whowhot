package com.example.whowhot;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private Button btnAdd, btnDel, btnRet;
    private EditText urlInput;
    private ListView logList;

    private static final String TAG = "TEST_DETAIL";   // Log용 태그
    private static final String fileName = "log.txt";    // 로그 저장 파일 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(TAG,"====== DetailActivity ========");

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDel = (Button) findViewById(R.id.btnDel);
        btnRet = (Button) findViewById(R.id.btnReturn);
        urlInput = (EditText) findViewById(R.id.edit0);
        logList = (ListView) findViewById(R.id.logListView);

        ArrayList<LogListViewItem> logArrayList = new ArrayList<>();
        ArrayList<LogListViewItem> removeYongLogArrayList = new ArrayList<>(); // 원본

        LogListViewAdapter adapter = new LogListViewAdapter(this, R.layout.log_listview_item, logArrayList);
        logList.setAdapter(adapter);

        loadListViewFromFile(logArrayList, fileName);
        adapter.notifyDataSetChanged();

        btnAdd.setEnabled(false);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addStr = (String) urlInput.getText().toString();

                if (addStr.length() > 0){ //입력받은 문자열이 있다면
                    appendToFile(addStr, fileName); // File에 추가
                    urlInput.setText(""); //빈 텍스트로 초기화
                    loadListViewFromFile(logArrayList, fileName); // 리스트뷰 초기화
                    adapter.notifyDataSetChanged(); //리스트 뷰 갱신
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
                    checkedIndex = logList.getCheckedItemPosition(); //체크된 리스트의 인덱스 가져오기
                    if (checkedIndex > -1 && checkedIndex < cnt){ //해당 조건에 만족하면
                        Log.d(TAG, logArrayList.get(checkedIndex).toString()+" 삭제");

                        logArrayList.remove(checkedIndex); //리스트에서 지우고
                        logList.clearChoices(); //체크된 리스트 초기화
                        adapter.notifyDataSetChanged(); //리스트뷰 갱신
                        saveListToFile(logArrayList, fileName); //파일에 저장
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

    private void saveListToFile(ArrayList<LogListViewItem> logList, String fileName){ //리스트뷰의 리스트들을 파일에 저장하는 함수
        File file = new File(getFilesDir(), fileName);
        FileWriter fw = null;
        BufferedWriter bufwr = null;

        try { //open file
            fw = new FileWriter(file);
            bufwr = new BufferedWriter(fw);

            // 임시로 해놓은거임 수정해야됨
            for (LogListViewItem log : logList){
                String str = log.getLogText().replace("\n","\\");
                bufwr.write(str);
                bufwr.newLine();
            }

            //write data to the file
            bufwr.flush();
            Log.d(TAG, file.getAbsolutePath()+"에 저장");

        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "FILE OPEN ERROR : "+e);
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
            Log.e(TAG, "FILE CLOSE ERROR : "+e);
        }
    }

    private void loadListViewFromFile(ArrayList<LogListViewItem> logArrayList, String fileName){ //파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시
        File file = new File(getFilesDir(), fileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str;

        if (file.exists()){ //파일이 존재하면
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                logArrayList.clear(); // 리스트 지움

                while ((str = bufrd.readLine()) != null){   // 파일 한줄씩 읽음
                    LogListViewItem item = new LogListViewItem(StringToLogString(str));
                    logArrayList.add(item); // ArrayList에 추가
                }
                //file close?
                bufrd.close();
                fr.close();
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "FILE OPEN ERROR : "+e);
            }
        }
    }

    private void appendToFile(String addText, String fileName){ //리스트뷰의 리스트들을 파일에 저장하는 함수
        File file = new File(getFilesDir(), fileName);
        FileWriter fileWriter = null;

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

    public String StringToLogString(String str){
        Log.d(TAG, "str :" +str);
        String logStr = str.replace("\\", "\n");
        Log.d(TAG, "logStr :" +logStr);

        return logStr;
    }
}