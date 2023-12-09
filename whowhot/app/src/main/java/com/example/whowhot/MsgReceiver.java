package com.example.whowhot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class MsgReceiver extends BroadcastReceiver {
    static final String TAG = "TEST_RECEIVER";   // log용 태그

    private final String[] msgs = {"0", "1"};

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Bundle bundle = intent.getExtras();

        // 메시지 받은거 String으로 바꿔서 msgs에 넣어줌
        if (bundle != null) {
            messageToString(bundle);
        }

        // MainService 실행하고 메시지 String 전달
        Intent serviceIntent = new Intent(context, MainService.class);
        serviceIntent.putExtra("msgs", msgs);
        context.startService(serviceIntent);
        Log.d(TAG, "send Intnet");
    }

    /* 메시지 Bundle 받은거 String으로 바꿔줌 */
    private void messageToString(Bundle bundle) {
        // Bundle을 Object로
        Object[] objs = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[objs.length];

        // Object를 SmsMessage로
        for (int i = 0; i < objs.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
        }

        // SmsMessage를 String으로
        if (messages.length > 0) {
            String sender = messages[0].getOriginatingAddress();
            msgs[0] = sender;   // 수신자 추가

            String content = ""; // 메시지 내용 넣을 String
            for (SmsMessage m : messages) { // 메시지 한줄씩 추가
                content += m.getMessageBody();
            }
            msgs[1] = content;  // 메시지 내용 추가
        }
    }
}