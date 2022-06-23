package jp.co.yoshida.katsushige.ytoolapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import jp.co.yoshida.katsushige.mylib.YLib;

public class ListCalender extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "ListCalender";

    private Cursor mCursor;
    private EditText mEdSearchWord;
    private Button mBtPrev;
    private Button mBtNext;
    private Button mBtSearch;
    private Spinner mSpOrganizer;
    private ArrayAdapter<String> mOrganizerAdapter;
    private TextView mTvCalender;
    private ListView mLvCalendarList;
    private ArrayAdapter<String> mCalendarListAdapter;
    private ArrayList<String[]> mCalendarList;
    private ArrayList<String> mCalenderOrganizer;
    private int mCurIndex = 0;
    private HashSet<String> mOrganizerList;
    private HashSet<String> mSearchWord;
    private String mSelectOrganizer = "";
    private String mSaveDirectory ="";
    private String mSearchWordPath = "";

    Format df;
    Format tf;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    ClipboardManager cm = null;

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calender);
        this.setTitle("カレンダーリスト");

        init();

        Log.d(TAG,"onCreate:");
        ylib = new YLib(this);
        this.cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //  作業用ファイルディレクトリの設定
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt());
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt()+"/CalendarList");
        mSearchWordPath = mSaveDirectory + "/" + "SeachWordList.csv";
        mSearchWord = new HashSet<>();
        loadSearchWordData(mSearchWordPath);

        //  カレンダデータ取得の初期化
        initCalendarData();
        //  取得した最初のデータの内容表示
        SetTextView(0);
        //  データのリスト表示
        setCalenderList();
        //  オーガナイザーの設定
        setCalenderOrganizer();

        //  分類の項目が変更された時
        mSpOrganizer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectOrganizer = mOrganizerAdapter.getItem(position);
                Log.d(TAG, "Organizer: " + mSelectOrganizer);
                setCalenderList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        saveSearchWordData(mSearchWordPath);
        super.onDestroy();
    }
    @Override
    public void onClick(View view) {
        Log.d(TAG,"onClick:"+view.toString());
        switch (view.getId()) {
            case R.id.button :      //  次のデータ
                SetTextView(1);
                break;
            case R.id.button2 :     //  前のデータ
                SetTextView(2);
                break;
            case R.id.button3 :     //  検索フィルタの設定
                mSearchWord.add(mEdSearchWord.getText().toString());
                setCalenderList();
//                Intent intent = new Intent(this, RegistCalender.class);
//                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.textView8 :
                this.cm.setText(mTvCalender.getText());
                Toast.makeText(this, "内容をコピーしました。", Toast.LENGTH_SHORT).show();
                break;
            case R.id.editTextWord :
                dispSearchWordMenu();
                break;
        }
        return true;        //  trueでonClickイベントを出さない
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String text = mCalendarListAdapter.getItem(position);
        Log.d(TAG,"onItemClick:"+position+" "+text);
        mCurIndex = position;
        SetTextView(3);
    }


    /***
     * データの内容の表示
     * @param id    0:最初のデータ 1:次のデータ 2:前のデータ
     */
    private void SetTextView(int id) {
        switch (id) {
            case 0 :            //  最初のデータ
                mCurIndex = 0;
                break;
            case 1 :            //  次のデータ
                if (mCurIndex < mCalendarListAdapter.getCount())
                    mCurIndex++;
                break;
            case 2:             //  前のデータ
                if (0 < mCurIndex)
                    mCurIndex--;
                break;
        }
        try {
            if (mCalendarListAdapter.getCount() <= mCurIndex)
                return;
            String dateStr = mCalendarListAdapter.getItem(mCurIndex).substring(0, 16);
            String title = mCalendarListAdapter.getItem(mCurIndex).substring(17);
            int i;
            for (i = 0; i < mCalendarList.size(); i++)
                if (mCalendarList.get(i)[1].compareTo(dateStr) == 0 &&
                        mCalendarList.get(i)[2].compareTo(title) == 0)
                    break;
            Log.d(TAG, "SetTextView: "+mCurIndex+" ["+dateStr+"]["+title+"] "+i+" ["+mCalendarList.get(i)[1]+"]");
            if (i < mCalendarList.size()) {
                mTvCalender.setText(
                        mCalendarList.get(i)[0] + "\n" +    //  オーガナイザー
                        mCalendarList.get(i)[1] + "   " +   //  日付
                        mCalendarList.get(i)[2] + "\n" +    //  タイトル
                        mCalendarList.get(i)[3] + "\n" +    //  内容
                        "場所: " + mCalendarList.get(i)[4]); //  場所
            }
        } catch (Exception e) {
            Toast.makeText(this, "エラー: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /***
     * カレンダデータをリストビューに登録
     */
    private void setCalenderList() {
        mCalendarListAdapter.clear();
        for (int i = 0; i < mCalendarList.size(); i++) {
            if ((mSelectOrganizer.compareTo("すべて") == 0 || 0 <= mCalendarList.get(i)[0].indexOf(mSelectOrganizer)) &&
                (0 <= mCalendarList.get(i)[2].indexOf(mEdSearchWord.getText().toString())))
                mCalendarListAdapter.add(mCalendarList.get(i)[1] + " " + mCalendarList.get(i)[2]);
        }
    }

    /***
     * カレンダのDBデータをArrayListに取り込みソートする
     */
    private void getCalendarData() {
        Log.d(TAG, "getCalendarData:");
        mCalendarList.clear();
        mCalenderOrganizer.clear();
        mOrganizerList.clear();
        mOrganizerList.add("すべて");
        //  DBデータの先頭に移動
        boolean success = mCursor.moveToFirst();
        //  DBデータからArrayListにコピーする
        while (success) {
            try {
                int pos = mCursor.getPosition();
                String[] data = {
                        mCursor.getString(1),           //  オーガナイザー
                        sdf.format(mCursor.getLong(2)), //  日付
                        mCursor.getString(3),           //  タイトル
                        mCursor.getString(4),           //  内容
                        mCursor.getString(5),           //  場所
                };
                Log.d(TAG, "getCalendarData:" + data[1]+" "+data[2]);
                mCalendarList.add(data);
                mOrganizerList.add(mCursor.getString(1).substring(0, mCursor.getString(1).indexOf('@')));
                if(mCursor.isLast())
                    break;
            } catch (Exception e) {
                Toast.makeText(this, "エラー: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            success = mCursor.moveToNext();
        }
//        //  ArrayListのデータをソートする(API24以上でソートを使用する場合)
//        mCalendarList.sort(new Comparator<String[]>() {     //  API24以上
//            @Override
//            public int compare(String[] o1, String[] o2) {
//                return o2[0].compareTo(o1[0]);
//            }
//        });
        //  ArrayListのデータをソートする(API24以前でもソートを使用する場合)
        Collections.sort(mCalendarList, new CalendarComparator());
    }

    /**
     * ソートのコンパレータ
     */
    public class CalendarComparator implements Comparator<String[]> {
        @Override
        public int compare(String[] o1, String[] o2) {
            return o2[1].compareTo(o1[1]);
        }
    }

    /**
     * オーガナイザーのデータをスピナーに設定
     */
    private void setCalenderOrganizer() {
        //  分類の設定
        mOrganizerAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, mOrganizerList.toArray(new String[0]));
        mSpOrganizer.setAdapter(mOrganizerAdapter);
    }

    /**
     * カレンダデータ取得のための初期化
     */
    private void initCalendarData() {
        //  カレンダから取得する項目の設定
        String[] COLS = new String[]{
                CalendarContract.Events._ID,                //  ID(long)
                CalendarContract.Events.ORGANIZER,          //  オーガナイザー
                CalendarContract.Events.DTSTART,            //  日付(long)
                CalendarContract.Events.TITLE,              //  タイトル
                CalendarContract.Events.DESCRIPTION,        //  内容
                CalendarContract.Events.EVENT_LOCATION,     //  場所
                CalendarContract.Events.CALENDAR_COLOR,     //  色(integer)

        };
        try {
            //  カーソル(データ)の取得(Runtime Permissionを設定しておく)
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "エラー: "+"カレンダの権限が設定されていません", Toast.LENGTH_LONG).show();
                Log.d(TAG,"onCreate: "+ "Calendar Permission error");
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            Log.d(TAG,"onCreate: query");
            mCursor = getContentResolver().query(
                    CalendarContract.Events.CONTENT_URI,    //  データの種類
                    COLS,                                   //  項目(null 全項目)
                    null,                          //   フィルタ条件(null フィルタなし)
                    null,                       //  フィルタ用パラメータ
                    null);                         //   ソート
            //  カレンダのデータの取得
            getCalendarData();
        } catch (Exception e) {
            Toast.makeText(this,"エラー: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            mEdSearchWord.setText(s);
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
        mEdSearchWord   = (EditText)findViewById(R.id.editTextWord);
        mTvCalender     = (TextView)findViewById(R.id.textView8);
        mLvCalendarList = (ListView)findViewById(R.id.listView);
        mCalendarListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mLvCalendarList.setAdapter(mCalendarListAdapter);
        mLvCalendarList.setOnItemClickListener(this);
        mBtPrev   = (Button)findViewById(R.id.button);
        mBtNext   = (Button)findViewById(R.id.button2);
        mBtSearch = (Button)findViewById(R.id.button3);
        mBtPrev.setOnClickListener(this);
        mBtNext.setOnClickListener(this);
        mBtSearch.setOnClickListener(this);
        mTvCalender.setOnLongClickListener(this);
        mEdSearchWord.setOnLongClickListener(this);
        mSpOrganizer = (Spinner)findViewById(R.id.spinner);

        df = DateFormat.getDateFormat(this);
        tf = DateFormat.getTimeFormat(this);
        mCalendarList = new ArrayList<String[]>();
        mCalenderOrganizer = new ArrayList<String>();
        mOrganizerList = new HashSet<String>();
        mTvCalender.setText("");
        mEdSearchWord.setText("");
    }
}