package jp.co.yoshida.katsushige.ytoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YLib;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    //  ListViewにメニューとしてActivityを登録するためのデータクラス
    class SubProgram {
        Class mClass;       //  Activityクラス
        String mName;       //  メニューの登録名
        public SubProgram(Class classs,String name) {
            mClass = classs;
            mName = name;
        }
    }

    ListView mListView;
    ArrayAdapter<String> mSimulationNameAdapter;
    ArrayList<SubProgram> mSubProgram = new ArrayList<SubProgram>();

    YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("道具屋");

        ylib = new YLib(this);
        chkFileAccessPermission();                  //  ファイルアクセスのパーミッションチェック

        init();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Toast.makeText(this, "Selected: " + String.valueOf(position), Toast.LENGTH_SHORT).show();
        if (position < mSubProgram.size()) {
            if (mSubProgram.get(position) != null) {
                Intent intent = new Intent(this, mSubProgram.get(position).mClass);
                startActivity(intent);
            }
        }
    }

    /**
     * 画面コントロールの初期化とActivityの登録
     */
    private void init() {
        //  起動モジュールの登録
        mSubProgram.add(new SubProgram(TermInfoActivity.class,"端末情報"));
        mSubProgram.add(new SubProgram(LocationLogger.class,"位置ロガー"));
        mSubProgram.add(new SubProgram(GpsLogger.class,"GPSロガー"));
        mSubProgram.add(new SubProgram(TimeTable.class,"時刻表"));
        mSubProgram.add(new SubProgram(ListCalender.class,"カレンダリスト"));
        mSubProgram.add(new SubProgram(RegistCalendar.class,"カレンダ登録"));

        mListView = (ListView) findViewById(R.id.listView);
        mSimulationNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item) {
            //  ListViewのセルの各種パラメータ設定
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView)super.getView(position, convertView, parent);
                view.setTextSize(20);       //  文字サイズ
                return view;
            }
        };

        mListView.setAdapter(mSimulationNameAdapter);
        for (int i=0; i<mSubProgram.size(); i++) {
            mSimulationNameAdapter.add(mSubProgram.get(i).mName);
        }

        mListView.setOnItemClickListener(this);
    }

    /**
     *  ファイルアクセスのパーミッションチェック
     */
    private void chkFileAccessPermission() {
        if (30<= Build.VERSION.SDK_INT)
            chkManageAllFilesAccess();
        else
            ylib.checkStragePermission(this);
    }

    /**
     *  MANAGE_ALL_FILES_ACCESS_PERMISSIONの確認(Android11 API30以上)
     */
    private void chkManageAllFilesAccess() {
        File file = new File("/storage/emulated/0/chkManageAllFilesAccess.txt");
        Log.d(TAG,"chkManageAllFilesAccess:");
        try {
            if (file.createNewFile()) {
                Log.d(TAG,"chkManageAllFilesAccess: create " + "OK");
            }
        } catch (Exception e) {
            Log.d(TAG,"chkManageAllFilesAccess: create " + "NG");
            Intent intent = new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
            startActivity(intent);
        }
    }
}
