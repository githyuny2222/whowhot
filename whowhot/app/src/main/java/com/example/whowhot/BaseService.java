package com.example.whowhot;

import static androidx.constraintlayout.motion.widget.Debug.getName;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/* 앱 살아있게만 해주는 서비스 */
public class BaseService extends Service {
    MsgReceiver msgReceiver;
    private static final String TAG = "TEST_BASE_SERVICE";   // log용 태그

    public BaseService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Base Service");
        createNotification(); //notification 생성

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate Base Service");
        super.onCreate();

        // 리시버 켜기
        msgReceiver = new MsgReceiver();
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(msgReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy Base Service");
        super.onDestroy();

        // 리시버 종료
        unregisterReceiver(msgReceiver);
    }

    // Notification 생성
    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("실시간 스미싱 탐지중");
        builder.setContentTitle("Whowhot");
        builder.setOngoing(true);

        Intent notificationIntent = new Intent(this, com.example.whowhot.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(new NotificationChannel("1","WhoWhot Service", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    // 서비스 돌아가는지 알려주는 함수
    public static boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (BaseService.class.getName().equals(rsi.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}