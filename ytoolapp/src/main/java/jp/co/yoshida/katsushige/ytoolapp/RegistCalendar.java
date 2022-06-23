package jp.co.yoshida.katsushige.ytoolapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import jp.co.yoshida.katsushige.mylib.YLib;

public class RegistCalendar extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "RegistCalendar";

    private EditText mEtBiginDate;
    private EditText mEtBiginTime;
    private EditText mEtEndDate;
    private EditText mEtEndTime;
    private EditText mEtTitle;
    private EditText mEtLocation;
    private EditText mEtDiscription;
    private Button mBtSetCalendar;
    private Button mBtCopySave;
    private String mSaveDirectory;
    private String mSearchWordPath;
    private HashSet<String> mSearchWord;
    enum CUREDITTEXT { TITLE, LOCATION, DISCRITION}
    private CUREDITTEXT mCurEditText;

    ClipboardManager cm = null;

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_calendar);
        setTitle("カレンダ登録");

        init();

        ylib = new YLib(this);
        this.cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //  作業用ファイルディレクトリの設定
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt());
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt()+"/CalendarList");
        mSearchWordPath = mSaveDirectory + "/" + "CopyWordList.csv";
        mSearchWord = new HashSet<>();
        loadSearchWordData(mSearchWordPath);
    }

    @Override
    protected void onDestroy() {
        saveSearchWordData(mSearchWordPath);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setCalButton:    //  登録ボタン
                setCalendarData();
                break;
            case R.id.button4:
                Log.d(TAG,"clipboard: ");
                ClipData cd = cm.getPrimaryClip();              //クリップボードからClipDataを取得
                if (cd != null) {
                    ClipData.Item item = cd.getItemAt(0);   //クリップデータからItemを取得
                    Log.d(TAG,"clipboard: "+item.getText().toString());
                    mSearchWord.add(item.getText().toString());
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.edTitle :
                mCurEditText = CUREDITTEXT.TITLE;
                dispSearchWordMenu();
                break;
            case R.id.edLocation :
                mCurEditText = CUREDITTEXT.LOCATION;
                dispSearchWordMenu();
                break;
            case R.id.edDiscription :
                mCurEditText = CUREDITTEXT.DISCRITION;
                dispSearchWordMenu();
                break;
        }
        return true;        //  trueでonClickイベントを出さない
    }

    /**
     * 設定されたデーでGoogleカレンダに登録する
     */
    private void setCalendarData() {
        Calendar cal = Calendar.getInstance();
        //  日付と時間の取得
        //  開始日時
        cal.set(getYear(mEtBiginDate.getText().toString()),getMonth(mEtBiginDate.getText().toString()),
                getDay(mEtBiginDate.getText().toString()),
                getHour(mEtBiginTime.getText().toString()),getMin(mEtBiginTime.getText().toString()));
        long beginTime = cal.getTimeInMillis();
        //  終了日時
        cal.set(getYear(mEtEndDate.getText().toString()),getMonth(mEtEndDate.getText().toString()),
                getDay(mEtEndDate.getText().toString()),
                getHour(mEtEndTime.getText().toString()),getMin(mEtEndTime.getText().toString()));
        long endTime = cal.getTimeInMillis();
        //  終日かどうか
        boolean allday = false;
        //  イベントタイトル
        String title = mEtTitle.getText().toString();
        //  イベントのメモ・内容
        String description = mEtDiscription.getText().toString();
        //  場所
        String location = mEtLocation.getText().toString();
        //  データをセットしてカレンダを開く
        registCalendar(beginTime, endTime, allday, title, description, location);
    }

    /**
     * Google カレンダーデータを登録する
     * AndroidSDK逆引きハントブック section-240　762p
     * 実行するとGoogleカレンダーが開く
     *     <uses-permission android:name="android.permission.WRITE_CALENDAR" /> 追加
     *
     * @param beginTime     開始日時
     * @param endTime       修理用日時
     * @param allDay        終日フラグ
     * @param title         イベントタイトル
     * @param description   イベントの内容
     * @param location      イベントの場所
     */
    private void registCalendar(long beginTime, long endTime, boolean allDay, String title, String description, String location) {

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location);

        startActivity(intent);
    }

    private int getYear(String date) {
        String[] str = date.split("/", 0);
        return Integer.valueOf(str[0]);
    }

    private int getMonth(String date) {
        String[] str = date.split("/", 0);
        return Integer.valueOf(str[1]) - 1;
    }

    private int getDay(String date) {
        String[] str = date.split("/", 0);
        return Integer.valueOf(str[2]);
    }

    private int getHour(String time) {
        String[] str = time.split(":", 0);
        return Integer.valueOf(str[0]);
    }

    private int getMin(String time) {
        String[] str = time.split(":", 0);
        return Integer.valueOf(str[1]);
    }

    /**
     * 検索単語をメニュー表示する
     */
    private void dispSearchWordMenu() {
        String[] funcMenu = new String[mSearchWord.size()];
        int n = 0;
        for (String key : mSearchWord)
            funcMenu[n++] = key;
        Arrays.sort(funcMenu);
        ylib.setMenuDialog(this, "検索単語", funcMenu, iOperation);
    }

    Consumer<String> iOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            if (mCurEditText == CUREDITTEXT.TITLE) {
                String buf = mEtTitle.getText().toString();
                buf = buf.substring(0, mEtTitle.getSelectionStart())+s+
                        buf.substring(mEtTitle.getSelectionEnd());
                mEtTitle.setText(buf);
            } else if (mCurEditText == CUREDITTEXT.LOCATION) {
                String buf = mEtLocation.getText().toString();
                buf = buf.substring(0, mEtLocation.getSelectionStart())+s+
                        buf.substring(mEtLocation.getSelectionEnd());
                mEtLocation.setText(buf);
            } else if (mCurEditText == CUREDITTEXT.DISCRITION) {
                String buf = mEtDiscription.getText().toString();
                buf = buf.substring(0, mEtDiscription.getSelectionStart())+s+
                        buf.substring(mEtDiscription.getSelectionEnd());
                mEtDiscription.setText(buf);
            }
        }
    };

    /**
     * 検索単語データをファイルから取り込む
     * @param path
     */
    private void loadSearchWordData(String path) {
        List<String> fileData = ylib.loadTextData(path);
        if (fileData != null) {
            mSearchWord = new HashSet<String>();
            for (String word : fileData)
                mSearchWord.add(word);
        }
    }

    /**
     * 検索単語データをファイルに保存
     * @param path
     */
    private void saveSearchWordData(String path) {
        List<String> fileData = new ArrayList<String>();
        if (mSearchWord != null) {
            for (String text : mSearchWord)
                fileData.add(text);
            ylib.saveTextData(path, fileData);
        }
    }

    /**
     * 画面コントロールの初期化
     */
    private void init() {
        mEtBiginDate = (EditText)findViewById(R.id.edBiginDate);
        mEtBiginTime = (EditText)findViewById(R.id.edBiginTime);
        mEtEndDate   = (EditText)findViewById(R.id.edEndDate);
        mEtEndTime   = (EditText)findViewById(R.id.edEndTime);
        mEtTitle     = (EditText)findViewById(R.id.edTitle);
        mEtLocation  = (EditText)findViewById(R.id.edLocation);
        mEtDiscription = (EditText)findViewById(R.id.edDiscription);
        mBtSetCalendar = (Button)findViewById(R.id.setCalButton);
        mBtCopySave = (Button)findViewById(R.id.button4);

        Calendar cal = Calendar.getInstance();
        String today = "" + cal.get(Calendar.YEAR) + "/" +
                (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH);
        String now = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

        mEtBiginDate.setText(today);
        mEtBiginTime.setText(now);
        mEtEndDate.setText(today);
        mEtEndTime.setText(now);
        mEtTitle.setText("");
        mEtLocation.setText("");
        mEtDiscription.setText("");
        mBtSetCalendar.setOnClickListener(this);
        mBtCopySave.setOnClickListener(this);
        mEtTitle.setOnLongClickListener(this);
        mEtLocation.setOnLongClickListener(this);
        mEtDiscription.setOnLongClickListener(this);
    }
}