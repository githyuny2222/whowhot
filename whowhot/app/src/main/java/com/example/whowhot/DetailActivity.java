package com.example.whowhot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private Button btnRet;
    private ListView logList;
    private ArrayList<LogListViewItem> logArrayList;
    private LogListViewAdapter adapter;

    private static final String TAG = "TEST_DETAIL";   // Log용 태그
    private static final String logFileName = "log.txt";    // 로그 저장 파일 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(TAG,"====== DetailActivity ========");

        btnRet = (Button) findViewById(R.id.btnReturn);
        logList = (ListView) findViewById(R.id.logListView);

        logArrayList = new ArrayList<>();

        adapter = new LogListViewAdapter(this, R.layout.log_listview_item, logArrayList);
        logList.setAdapter(adapter);

        loadListViewFromFile(logArrayList);
        adapter.notifyDataSetChanged();

        btnRet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // 로그제거 버튼 클릭리스너
    public void onClickDelete(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String str = "정말로 로그를 모두 지우시겠습니까?";
        builder.setTitle("로그 삭제").setMessage(str);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Log.d(TAG, "로그 제거!");
                deleteLog();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Log.d(TAG, "로그 제거 취소!");
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadListViewFromFile(ArrayList<LogListViewItem> logArrayList){ //파일로부터 줄 단위로 텍스트를 읽어오고, 리스트뷰에 표시
        File file = new File(getFilesDir(), logFileName);
        FileReader fr = null;
        BufferedReader bufrd = null;
        String str;

        if (file.exists()){ //파일이 존재하면
            try { //open file
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);

                logArrayList.clear(); // 리스트 지움

                while ((str = bufrd.readLine()) != null){   // 파일 한줄씩 읽음
                    LogListViewItem item = new LogListViewItem(stringToLogString(str));
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

    public void deleteLog(){
        File file = new File(getFilesDir(), logFileName);
        FileWriter fileWriter = null;

        //Log.d(TAG, "AbsolutePath : "+file.getAbsolutePath());
        //Log.d(TAG, "Path : "+file.getPath());
        //Log.d(TAG, "Filename : "+file.getName());

        try { //open file
            fileWriter = new FileWriter(file, false);

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

        loadListViewFromFile(logArrayList);
        adapter.notifyDataSetChanged();
    }

    public String stringToLogString(String str){
        //Log.d(TAG, "str :" +str);
        String logStr = str.replace("\\", "\n");
        //Log.d(TAG, "logStr :" +logStr);

        return logStr;
    }
}