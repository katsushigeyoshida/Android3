package jp.co.yoshida.katsushige.gpsinfo2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Date;

import jp.co.yoshida.katsushige.mylib.ListData;
import jp.co.yoshida.katsushige.mylib.YLib;

public class GpsOnService extends Service
        implements LocationListener, SensorEventListener {

    private static final String TAG = "GpsOnService";

    //  センサーマネジャー
    private LocationManager mLocationManager;   //  位置センサーマネジャー
    private SensorManager mSensorManager;       //  歩数センサーマネジャー(StepCounter)
    private boolean mStepFlag = true;           //  歩数センサー機能対応フラグ

    private String mSaveDirectory;              //  データ保存ディレクトリ

    private GpxFile mGpxFile = null;            //  GPXファイル作成クラス
    private boolean mGpxFileKeep = true;        //	随時GPXデータをファイルに保存
    private boolean mGpxFileSave = true;        //	GPXデータのファイル保存
    private String mGpxFilePath = "";
    private long mGPSminTime = 15 * 1000;       //	GPS間隔(ms)
    private float mGPSminDistance = 0;          //	GPS間隔(m)
    private boolean mDataFilter = true;         //	位置測定データにフィルタをかける
    private double mLimitSpeed = 0;             //	記録制限速度(km/h)
    private double mLimitDistance = 0;          //	移動距離の制限値(km)
    //  前回値
    private Location mPreLocation = null;       //  前回位置
    private Location mFirstLocation = null;     //  初回位置

    private boolean mContinueStart = false;     //  継続処理フラグ

    //  歩数カウンタ
    private int mStartStepNum = 0;              //	開始歩数
    private int mCurrentStepNum = 0;            //	現在の歩数
    private int mStepCount = 0;                 //   歩数

    private static final int NOTI_ID = 1;

    private boolean mBeep = true;               //	位置測定でビープ音
    private boolean mVibrator = true;           //  位置測定でバイブレーションを使う
    private int mBeepTime = 50;                 //  測定時のビープ(ms)
    private long mViabratorTime = 500L;         //  測定時のバイブレーション時間(ms)

    private int mDigitNo = 3;                   //   小数点以下表示桁数
    private Intent mIntent;

    private Vibrator mVib;                      //  バイブレータ
    private GpsInfoLib gilib;
    private YLib ylib;

    /**
     * サービスが最初に作成された時のコールバック
     * このメソッドはonStartCommandやonBindよりも前に呼び出され、サービスが起動中の場合は
     * 呼ばれません。
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"onCreate");

        ylib = new YLib();
        //	データ保存ディレクトリの設定
        mSaveDirectory = ylib.getStrPreferences("SAVEDIRECTORY", this);
        gilib = new GpsInfoLib(this, mSaveDirectory);
        GetLocationPreferences();                               //  GPS設定値の更新(GPS取得間隔)

        //	位置情報
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling  Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        //  GPS設定値の更新
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mGPSminTime, mGPSminDistance, this);
//        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//        }

        //	センサー管理クラスの取得
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //	歩数センサーの確認
        if (!supportedStepSensor(this)) {
            mStepFlag = false;
            Toast.makeText(this, "歩数センサーに対応していません", Toast.LENGTH_SHORT).show();
        }
        //	StepDetctorとStepCounterとの違い
        //	StepDetectorは起動から実際に歩くまでコールバックは行われない
        //	StepCounterは起動直後に歩いていなくてもコールバックする
        //  StepCounterの設定
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        boolean supportBatch = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);

        // Vibratorクラスのインスタンス取得とvibratorとbeepの設定
        mVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        outBeep(1);

        //	GPXデータファイルの設定
        mGpxFile = new GpxFile(mSaveDirectory + "/", mGpxFileKeep);
        mGpxFile.Init(gilib.getDataFileName());   //  ファイル名の設定とlogカウントと累積距離の初期化
        mGpxFile.setContext(this);

    }

    /**
     * startServiceでサービスが開始要求を受けたときのコールバック
     * このメソッドはサービスの開始ポイント
     * このメソッドの戻り値は、サービスがシステムから不意にkillされた場合の動作を決定
     * bindServiceを呼び出してサービスをバインドした場合、このメソッドは呼ばれません。
     * 戻り値:
     * START_NOT_STICKY:            サービスを起動するペンディングインテントが存在しない限りサービスは再起動されません 。
     * START_STICKY:                システムはサービスを新たにインスタンス化し、サービスの再起動を行います。
     * START_REDELIVER_INTENT:      システムはサービスを新たにインスタンス化し、サービスの再起動を行います。
     * START_STICKY_COMPATIBILITY:  システムにより再起動されることが保障されません。
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!gilib.getGpxDataContinue()) {
            //  新規
            Log.d(TAG, "onStartCommand:　新規開始");
            Toast.makeText(this, "新規　GPS記録 開始", Toast.LENGTH_LONG).show();
            mContinueStart = false;                 //  継続ではなく新規
            gilib.setFirstTime(0);                  //  測定時間クリア
            gilib.setGpxSaveCount(0);               //  測定回数をクリア
            gilib.setTotalDistance(0);              //  累積距離クリア
            gilib.setTotalTime(0);                  //  経過時間クリア
            mGpxFile.startData();				    //	ヘッダファイル作成
            gilib.setGpxDataContinue(true);
            mStartStepNum = 0;                              //  歩数センサー開始時の歩数
            mCurrentStepNum = 0;                            //  歩数センサーの現在の歩数
            mStepCount = 0;                                 //  開始時からの歩数
        } else {
            //  継続動作
            Log.d(TAG, "onStartCommand: 継続開始");
            Toast.makeText(this, "継続　GPS記録 開始", Toast.LENGTH_LONG).show();
            mContinueStart = true;                  //  継続
            //	データ保存開始
            mGpxFile.setGpxLogCount(gilib.getGpxSaveCount());		//	ファイルが継続される場合に備えて前回のカウント数をセットして置く
            mGpxFile.setTotalDistance(gilib.getTotalDistance());  //  移動距離を継続させる
            mGpxFile.setTotalTime(gilib.getTotalTime());
            mStepCount = gilib.getStepCount();      //  歩数の継続カウント
        }
        //  Foregroundの動作のためNotificationを設定
        mIntent = intent;
        ShowNotification(intent, 1, getString(R.string.app_name), "GPS記録中");

        return START_NOT_STICKY;            //  強制終了時 サービスは再起動されない
//        return START_REDELIVER_INTENT;      //  強制終了で自動再起動、インテントを引き継ぐ
//        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * サービスが破棄される時のコールバック
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");

        //  ロケーションサービスの終了
        mLocationManager.removeUpdates(this);
        if (!gilib.getGpxDataContinue()) {
            //  測定終了(一時中断ではないとき)
            if (mGpxFile != null)
                mGpxFile.close();
        }
        gilib.setStepCount(mStepCount);       //  saveMemoData()を実行し後に歩数を保存
        outBeep(3);

        super.onDestroy();
    }

    /**
     * bindServiceサービスがバインドされた時のコールバック
     * サービスがバインドを拒否したい場合はnullを返すようにします。
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind:");
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (!mContinueStart && mFirstLocation == null) {
                Log.d(TAG, "onLocationChanged:　初回");
                //  初回時の処理
                mFirstLocation = new Location(location);
                //	初回値の保存
                if (gilib.getFirstTime() == 0) {
                    gilib.setFirstTime(location.getTime());                   //  測定時間
                    gilib.setFirstLatitude((float) location.getLatitude());   //  緯度
                    gilib.setFirstLongitude((float) location.getLongitude()); //  経度
                    gilib.setFirstElevator((float) location.getAltitude());   //  標高
                    gilib.setMaxElevator((float) location.getAltitude());     //  最大標高
                    gilib.setMinElevator((float) location.getAltitude());     //  最小標高
                }
            }
            if (mPreLocation == null) {
                Log.d(TAG, "onLocationChanged:　前回値なし");
                //  前回値がない場合
                mPreLocation = new Location(location);
                gilib.setPreLatitude((float) location.getLatitude());         //  緯度
                gilib.setPreLongitude((float) location.getLongitude());       //  経度
            } else if (mGPSminTime <= ylib.locLapTime(location, mPreLocation)               //  時間差(msec)
                    && mGPSminDistance <= ylib.locDistance(location, mPreLocation) * 1000) {//  移動距離(m)
                //	データの登録(前回値から変更があった場合のみ)
                if (putLocationData(location)) {                        //  位置データをファイルに登録
                    Log.d(TAG, "onLocationChanged:　登録");
                    //	前回から距離と時間を求める
                    mPreLocation.setLatitude(gilib.getPreLatitude());
                    mPreLocation.setLongitude(gilib.getPreLongitude());
                    mPreLocation = new Location(location);
                    gilib.setPreLatitude((float) location.getLatitude());
                    gilib.setPreLongitude((float) location.getLongitude());
                    gilib.updateMaxElevator((float) location.getAltitude());
                    gilib.updateMinElevator((float) location.getAltitude());
                    GetLocationPreferences();                               //  GPS設定値の更新(GPS取得間隔)
//                    GetPreferencesDataFilter();                       //  設定値の取得
                    ShowNotification(mIntent, 1,  getString(R.string.app_name),
                            "GPS記録中 "+mGpxFile.getGpxLogCount()+"回　" +
                                    String.format("%,.1fkm",(float)mGpxFile.getTotalDistance())+" " +
                                    String.format("%,.1f分",(float)mGpxFile.getTotalTime() / 60f)+" " +
                                    String.format("%,d歩",mStepCount));
                    Log.d(TAG, "onLocationChanged:　minTime:"+mGPSminTime+
                            " minDis="+mGPSminDistance+" beep:"+mBeep+" vib="+mVibrator);
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, "onLocationChanged:　Exception Error "+e.getMessage());
//            mGpxFile.addErrorData("onLocationChanged Exception Error:" + e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "onStatusChanged:");

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled:");

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled:");

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //	歩数検知(端末が起動してからの累計歩数)
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            //  歩数センサーによる歩数検知
            mCurrentStepNum = (int)sensorEvent.values[0];
            if (mStartStepNum == 0) {
                mStartStepNum = mCurrentStepNum;
            }
            mStepCount = mCurrentStepNum - mStartStepNum + gilib.getStepCount();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 位置データを出力する
     * @param location
     */
    private boolean putLocationData(Location location) {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG,"putLocationData: GPSが設定されていません");
            mGpxFile.addErrorData("putLocationData Error:" + "GPSが設定されていません");
            Toast.makeText(this, "GPSが設定されていません", Toast.LENGTH_LONG).show();
            return false;
        }
        //  GPS LocationManagerの設定値更新
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"putLocationData: checkSelfPermission");
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return false;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mGPSminTime, mGPSminDistance, this);

        gilib.setGpxSaveCount(mGpxFile.getGpxLogCount());             //  GPS取得回数の保存
        gilib.setTotalDistance((float) mGpxFile.getTotalDistance());  //  累積移動距離を保存
        gilib.setTotalTime(mGpxFile.getTotalTime());                  //  経過時間

        //  位置データをファイルに出力
        if (mGpxFile.addData(location)) {
            Toast.makeText(this, "GPS記録中: " + mGpxFile.getGpxLogCount() +
                    "  歩数: " + String.valueOf(mStepCount), Toast.LENGTH_SHORT).show();
            if (mGpxFile.getGpxLogCount()==1)
                outBeep(3);
            else
                outBeep(1);
            return true;
        } else {
            Log.d(TAG, "putLocationData:　addData Error");
            return false;
        }
    }

    /**
     * ステータスバーへの通知
     * FOREGROUND_SERVICEのpermissionの追加要(API28)
     * @param intent
     * @param requestCode
     * @param title             通知タイトル
     * @param discription       説明補足内容
     */
    private void ShowNotification(Intent intent,  int requestCode, String title, String discription) {
        Context context = getApplicationContext();
        String channelId = "default";

        PendingIntent pendingIntent =       //  intentを予約して指定のタイミングで発行する(Notificationを起動させるため)
                PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        //  通知チャンネルの設定(IMPORTANCE_DEFAULT(通知,音・バイブレーションあり),IMPORTANCE_LOW(通知あり、音・バイブレーションなし)
        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_LOW);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle(title)                     //  Notification飲むタイトル
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .setContentText(discription)                //  Notificationの説明
//                    .setAutoCancel(true)                        //  タップするとキャンセル(消える)
                    .setOngoing(true)                           //  「継続的」
                    .setContentIntent(pendingIntent)            //  Notificationをタップした際にActivityを起動するIntent
                    .setWhen(System.currentTimeMillis())        //  Notificationを表示する時間
                    .build();
            startForeground(1, notification);
        }
    }

    /**
     * GPS設定値の更新
     */
    private void GetLocationPreferences() {
        mGPSminTime = gilib.GetPreferencesGPSminTime();           //  測定最小時間間隔
        mGPSminDistance = gilib.GetPreferencesGPSminDistance();   //  測定最小距離
        mBeep = gilib.getPreferencesBeep();                       //  ビープ音
        mVibrator = gilib.getPreferencesVibrator();               //  バイブレータ
    }

    /**
     * 計測途中で変更可能な設定値
     */
    private void GetPreferencesDataFilter() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(GpsOnService.this);
        mDataFilter = prefs.getBoolean("datafilter", false);                    // ログファイル出力
        mLimitSpeed = Float.valueOf(prefs.getString("limitspeed", "0"));       // ログ出力制限速度(km/h)
        mLimitDistance = Float.valueOf(prefs.getString("limitdistance", "0")); // ログ出力制限速度(km)
        if (mGpxFile != null) {
            mGpxFile.setDataFilter(mDataFilter);
            mGpxFile.setLimitSpeed(mLimitSpeed);
            mGpxFile.setLimitDistance(mLimitDistance);
        }
    }


    /**
     * ビープ音とバイブレータを鳴らす
     * @param n     鳴らす時間
     */
    private void outBeep(int n) {
        if (mBeep) ylib.beep(mBeepTime * n);
        if (mVibrator) mVib.vibrate(mViabratorTime * n);
    }

    /**
     * 歩数カウントに対応している場合はtrue
     * @return
     */
    private boolean supportedStepSensor(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT // APIレベルチェック
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) // ステップカウンタチェック
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR); // ステップディテクタチェック
    }
}
