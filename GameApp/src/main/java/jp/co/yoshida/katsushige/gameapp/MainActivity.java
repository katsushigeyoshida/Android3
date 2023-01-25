package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YLib;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    class SubProgram {
        Class mClass;
        String mName;
        public SubProgram(Class classs,String name) {
            mClass = classs;
            mName = name;
        }
    }

    ListView mListView;
    Spinner mSimulationNameSelect;
    ArrayAdapter<String> mSimulationNameAdapter;
    ArrayList<SubProgram> mSubProgram = new ArrayList<SubProgram>();

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ylib = new YLib();
        chkFileAccessPermission();                  //  ファイルアクセスのパーミッションチェック

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);	//	画面を縦に固定する

        init();
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

    private void init() {
        mSubProgram.add(new SubProgram(BlockGame.class,"ブロック崩し"));
        mSubProgram.add(new SubProgram(LifeGame.class,"ライフゲーム"));
        mSubProgram.add(new SubProgram(AllWhite.class,"白にしろ"));
        mSubProgram.add(new SubProgram(SlideGame.class,"スライドゲーム"));
        mSubProgram.add(new SubProgram(Sudoku.class,"数独"));
        mSubProgram.add(new SubProgram(RubicCube.class,"ルービックキューブ"));
        mSubProgram.add(new SubProgram(Tetris.class,"テトリス"));
        mSubProgram.add(new SubProgram(Bomb.class,"マインスィーパ"));
//        mSubProgram.add(new SubProgram(MineSweeper.class,"マインスィーパ"));

        mListView = (ListView) findViewById(R.id.rootListView);
        mSimulationNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mListView.setAdapter(mSimulationNameAdapter);
        for (int i=0; i<mSubProgram.size(); i++) {
            mSimulationNameAdapter.add(mSubProgram.get(i).mName);
        }

        mListView.setOnItemClickListener(this);
    }
}
