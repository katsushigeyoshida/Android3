package jp.co.yoshida.katsushige.ytoolapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class GpsService extends Service {

    private static String TAG = "GpsService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);

        ShowNotification(intent, requestCode);

        return START_NOT_STICKY;    //  強制終了時 サービスは再起動されない
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    private void ShowNotification(Intent intent,  int requestCode) {
        Context context = getApplicationContext();
        String channelId = "default";
        String title = context.getString(R.string.app_name);

        PendingIntent pendingIntent =       //  intentを予約して指定のタイミングで発行する(Notificationを起動させるため)
                PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle(title)                     //  Notification飲むタイトル
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .setContentText("GPS")                      //  Notificationの説明
                    .setAutoCancel(true)                        //  タップするとキャンセル(消える)
                    .setContentIntent(pendingIntent)            //  Notificationをタップした際にActivityを起動するIntent
                    .setWhen(System.currentTimeMillis())        //  Notificationを表示する時間
                    .build();
            startForeground(1, notification);
        }
    }
}
