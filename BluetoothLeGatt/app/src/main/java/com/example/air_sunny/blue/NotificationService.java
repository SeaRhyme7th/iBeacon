package com.example.air_sunny.blue;

/**
 * Created by jerry123 on 2016/11/2.
 */

import android.app.Service;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {

    private static final String TAG = "Notification";

    private NotificationManager notificationManager;
    //推送id
    private int NOTIFY_NUM = 100;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //发送一条前台通知
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("IBeacon提示");
        builder.setContentText("您已经进入IBeacon设备的范围");
        long[] vibrate = {0, 100};
        builder.setVibrate(vibrate);
        builder.setSmallIcon(R.drawable.ic_launcher);
        notificationManager.notify(NOTIFY_NUM, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
