package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

public class BlockGame extends AppCompatActivity
        implements SensorEventListener {

    private BlockGameView mBlockGameView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    public static int mAccelCount = 0;
    public static float accel_x = 0.0f;
    public static float accel_y = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_block_game);
        this.setTitle("ブロック崩し");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//	画面を縦に固定する

        requestWindowFeature(Window.FEATURE_NO_TITLE);     //  ウインドウタイトルバーを非表示にする　有効?
        mBlockGameView = new BlockGameView(this);   //  BlockGameViewを生成
        setContentView(mBlockGameView);                    //  BlockGameViewを画面に指定

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (list.size() > 0)
            mAccelerometer = list.get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  センサーの取得タイミング
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME); //  20ms間隔
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //  傾き・加速度の取得
        if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        accel_x = sensorEvent.values[0];  //  X軸方向に上下(-9.81～9.81)
        accel_y = sensorEvent.values[1];  //  Y軸方向に上下
        if (mAccelCount == 0) {
            if (1.0f < accel_x)
                mAccelCount = 1;
            if (-1.0f > accel_x)
                mAccelCount = -1;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
