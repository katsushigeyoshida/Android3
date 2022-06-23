package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import jp.co.yoshida.katsushige.mylib.YLib;

public class LifeGame extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "LifeGame";

    Spinner mSpInitPattern;             //  初期パターン
    SeekBar mSbWaitTime;                //  表示速度
    TextView mTvWaitTime;               //  表示速度の設定値
    Button mBtStart;                    //  開始ボタン
    Button mBtEnd;                      //  終了ボタン
    Button mBtStop;                     //  中断ボタン
    Button mBtRestart;                  //  再開ボタン
    Button mBtPattern;                  //  初期パターン作成
    LinearLayout mLinearLayout;         //  Viewのコンテナ
    LifeGameView mLifeGameView;
    ArrayAdapter<String> mInitPatterAdapter;

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_game);
        this.setTitle("ライフゲーム");

        ylib = new YLib(this);
        ylib.checkStragePermission(this);

        init();

        mSpInitPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                //showToast(Integer.toString(spinner.getSelectedItemPosition()));
                Log.d(TAG, "onCreate:mSpInitPattern" + spinner.getSelectedItemPosition());
                mLifeGameView.setParameter(getPattern());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtStart.getId()) {
            //   開始
            mBtStart.setEnabled(false);
            mBtEnd.setEnabled(true);
            mBtRestart.setEnabled(false);
            mBtStop.setEnabled(true);
            mBtPattern.setEnabled(false);
            mSpInitPattern.setEnabled(false);
            mLifeGameView.start();
        } else if (view.getId() == mBtEnd.getId()) {
            //  終了
            mBtStart.setEnabled(true);
            mBtEnd.setEnabled(false);
            mBtRestart.setEnabled(false);
            mBtStop.setEnabled(false);
            mBtPattern.setEnabled(true);
            mSpInitPattern.setEnabled(true);
            mLifeGameView.end();
        } else if (view.getId() == mBtStop.getId()) {
            //  中断
            mBtStart.setEnabled(false);
            mBtEnd.setEnabled(true);
            mBtRestart.setEnabled(true);
            mBtStop.setEnabled(false);
            mLifeGameView.stop();
        } else if (view.getId() == mBtRestart.getId()) {
            //  再開
            mBtStart.setEnabled(false);
            mBtEnd.setEnabled(true);
            mBtRestart.setEnabled(false);
            mBtStop.setEnabled(true);
            mLifeGameView.restart();
        } else if (view.getId() == mBtPattern.getId()) {
            //  パターン作成
            if (mBtPattern.getText().toString().compareTo("作成")==0) {
                mBtStart.setEnabled(false);
                mBtPattern.setText("完了");
                mLifeGameView.creatPattern();
            } else {
                mLifeGameView.completePattern();
                mBtStart.setEnabled(true);
                mBtPattern.setText("作成");
                mSpInitPattern.setSelection(mLifeGameView.mPatternNo);
                mLifeGameView.setParameter(getPattern());
            }
        }
    }

    /**
     * 初期パターンの取得
     * @return      パターンNo
     */
    private int getPattern() {
        return mSpInitPattern.getSelectedItemPosition();
    }

    /**
     * 初期化
     */
    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//	画面を縦に固定する

        mSpInitPattern = (Spinner)findViewById(R.id.spinner4);
        mSbWaitTime = (SeekBar)findViewById(R.id.seekBar);
        mTvWaitTime = (TextView)findViewById(R.id.textView12);
        mBtStart = (Button)findViewById(R.id.button12);
        mBtEnd = (Button)findViewById(R.id.button13);
        mBtStop = (Button)findViewById(R.id.button14);
        mBtRestart = (Button)findViewById(R.id.button15);
        mBtPattern = (Button)findViewById(R.id.button11);
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout3);

        //  表示速度シークバーの初期設定
        mSbWaitTime.setMax(500);
        mSbWaitTime.setProgress(300);
        mTvWaitTime.setText(String.valueOf(300));
        mSbWaitTime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar deekbar, int progress, boolean fromUser) {
                        mTvWaitTime.setText(String.valueOf(progress));
                        mLifeGameView.setWaitTime(progress);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        //  ボタンのClickListenerの設定
        mBtStart.setOnClickListener(this);
        mBtEnd.setOnClickListener(this);
        mBtStop.setOnClickListener(this);
        mBtRestart.setOnClickListener(this);
        mBtPattern.setOnClickListener(this);
        //  ボタンの有効無効の設定
        mBtStart.setEnabled(true);
        mBtEnd.setEnabled(false);
        mBtStop.setEnabled(false);
        mBtRestart.setEnabled(false);

        //  表示Viewの設定
        mLifeGameView = new LifeGameView(this);
        mLinearLayout.addView(mLifeGameView);

        //  初期パターン
        mInitPatterAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mLifeGameView.mPatternName);
        mSpInitPattern.setAdapter(mInitPatterAdapter);
    }
}
