package jp.co.yoshida.katsushige.ytoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GpsLogger extends AppCompatActivity
        implements View.OnClickListener {

    Button mBtStart;
    Button mBtEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_logger);

        mBtStart = (Button)findViewById(R.id.button);
        mBtEnd = (Button)findViewById(R.id.button2);
        mBtStart.setOnClickListener(this);
        mBtEnd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtStart.getId()) {
            //  Serviceの開始
            Intent intent = new Intent(getApplication(), GpsService.class);
            intent.putExtra("REQUEST_CODE",1);
//            startService(intent);
            startForegroundService(intent);
        } else if (view.getId() == mBtEnd.getId()) {
            //  Serviceの停止
            Intent intent = new Intent(getApplication(), GpsService.class);
            stopService(intent);
        }
    }
}
