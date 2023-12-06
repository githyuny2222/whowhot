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

public class MyDialog extends Activity {
    private int danger;

    static final String TAG = "TEST_DIALOG";   // Log.d용 태그

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bun = getIntent().getExtras();
        if(bun != null) { danger = bun.getInt("danger"); }

        setContentView(R.layout.dialog_background_round);
        TextView adMessage = (TextView) findViewById(R.id.confirmTextView);
        Button yesButton = (Button) findViewById(R.id.yesButton);
        Button detailButton = (Button) findViewById(R.id.detailButton);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background2);

        // 위험도 2일때 위험 1일때 주의
        if (danger > 1) {
            adMessage.setText("위험!\n위험한 메시지를 발견했습니다");
            linearLayout.setBackground(getDrawable(R.drawable.view_round_red_6));
            adMessage.setTextColor(Color.BLACK);
        } else {
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
}
