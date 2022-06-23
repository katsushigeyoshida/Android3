package jp.co.yoshida.katsushige.gpsinfo2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import jp.co.yoshida.katsushige.mylib.YLib;

public class GpxGraphActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "GpxGraphActivity";

    private Spinner mSpGraphType;       //  グラフの種類
    private Spinner mSpFilterType;      //  フィルタの種類
    private Spinner mSpFilterDataCount; //  フィルタのデータ数
    private CheckBox mCbHoldTime;       //  滞留時間削除
    private GpxGraphView mGpxGraphView; //  GPXをグラフ表示するView
    private ArrayAdapter<String> mGraphTypeAdapter;
    private ArrayAdapter<String> mFilterTypeAdapter;
    private ArrayAdapter<String> mFilterDataCountAdapter;


    private String[] mGraphTitle = {"距離/時間","速度/時間","高度/時間","時間/距離","速度/距離","高度/距離"};
    private String[] mFilterType = {"フィルタなし","移動平均","中央値","ローパス"};
    private String[] mMoveAveTitle = {"移動平均なし","3データ","5データ","9データ","17データ","33データ","65データ","129データ"};
    private String[] mMedianTitle = {"中央値なし","3データ","5データ","9データ","17データ","31データ","65データ","129データ"};
    private String[] mLowPassTtle = {"ローパスなし","0.90","0.80","0.70","0.60","0.50","0.40","0.30","0.20","0.10","0.05","0.00"};

    enum GRAPHTYPE {DisTime, SpeedTime, EleTime, TimeDis, SpeedDis, EleDis }

    private final int MENU01 = 1;
    private final int MENU02 = 2;
    private final int MENU09 = 9;

    private String mGpxPath;

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpx_graph);

        Log.d(TAG,"onCreate: ");
        init();
        ylib = new YLib(this);

        //	表示データファイル名の取得
        Intent intent = getIntent();
        setFilterDataCountMenu(mFilterTypeAdapter.getItem(mSpFilterType.getSelectedItemPosition()));
        mGpxPath = intent.getStringExtra("FILEPATH");
        setTitle(ylib.getNameWithoutExt(mGpxPath));

        //  GPXファイルを読み込みと初期値設定
        mGpxGraphView.setGpxFileRead(mGpxPath);
        mGpxGraphView.setGraphType(mGraphTitle[0]);
        mGpxGraphView.setFilter(mFilterType[0], 0, 1.0f);

        mSpGraphType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,"onCreate: mSpGraphType");
                String graphType = mGraphTypeAdapter.getItem(mSpGraphType.getSelectedItemPosition());
                if (mGpxGraphView.setGraphType(graphType))
                    mGpxGraphView.dispGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,"onCreate: mSpFilterType");
                setFilterDataCountMenu(mFilterTypeAdapter.getItem(mSpFilterType.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpFilterDataCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,"onCreate: mSpFilterDataCount");
                String filterType = mFilterTypeAdapter.getItem(mSpFilterType.getSelectedItemPosition());
                if (mSpFilterDataCount.getSelectedItemPosition() == 0) {
                    if (mGpxGraphView.setFilter(filterType, 0, 1f))
                        mGpxGraphView.dispGraph();
                } else {
                    String dataCount = mFilterDataCountAdapter.getItem(mSpFilterDataCount.getSelectedItemPosition());
                    float passRate = 1f;
                    int count = 0;
                    if (filterType.compareTo("ローパス")==0) {
                        passRate = Float.parseFloat(dataCount.replaceAll("[^.0-9]", ""));
                    } else if (filterType.compareTo("移動平均")==0 || filterType.compareTo("中央値")==0) {
                        count = Integer.parseInt(dataCount.replaceAll("[^0-9]", ""));
                    }
                    if (mGpxGraphView.setFilter(filterType, count, passRate))
                        mGpxGraphView.dispGraph();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mGpxGraphView.setHoldTimeOut(mCbHoldTime.isChecked());  //  滞留時間削除
        mGpxGraphView.dispGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add(Menu.NONE, MENU01, Menu.NONE, "追加読込");
        MenuItem item2 = menu.add(Menu.NONE, MENU02, Menu.NONE, "滞留時間削除");
        MenuItem item9 = menu.add(Menu.NONE, MENU09, Menu.NONE, "ヘルプ");
        item1.setIcon(android.R.drawable.ic_menu_upload);
        item2.setIcon(android.R.drawable.ic_menu_upload);
        item9.setIcon(android.R.drawable.ic_menu_help);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case MENU09:                         //	ヘルプ
                break;
            case MENU01:                         //	追加データ
                ylib.fileSelectDialog(ylib.getDir(mGpxPath),"*.gpx", true, iAddGpxData);
                break;
            case MENU02:                        //  滞留時間削除
                mGpxGraphView.setHoldTimeOutReverse();
                mGpxGraphView.dispGraph();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * GPXファイルのデータを追加してグラフを再表示する関数インターフェース
     */
    Consumer<String> iAddGpxData = new Consumer<String>() {
        @Override
        public void accept(String s) {
            mGpxGraphView.setGpxFileRead(s, true);
            mGpxGraphView.dispGraph();
        }
    };

    /**
     * フィルタの種類に応じてデータ数のメニューを設定する
     *
     * @param filterName フィルタの種類
     */
    private void setFilterDataCountMenu(String filterName) {
        String[] filterDataCountMenu = {""};
        if (filterName.compareTo(mFilterType[0])==0) {

        } else if (filterName.compareTo(mFilterType[1])==0) {
            filterDataCountMenu = mMoveAveTitle;
        } else if (filterName.compareTo(mFilterType[2])==0) {
            filterDataCountMenu = mMedianTitle;
        } else if (filterName.compareTo(mFilterType[3])==0) {
            filterDataCountMenu = mLowPassTtle;
        }
        mFilterDataCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterDataCountMenu);
        mSpFilterDataCount.setAdapter(mFilterDataCountAdapter);
    }

    /**
     * 画面の初期化
     */
    private void init() {
        mSpGraphType = (Spinner)findViewById(R.id.spinner6);
        mSpFilterType = (Spinner)findViewById(R.id.spinner7);
        mSpFilterDataCount = (Spinner)findViewById(R.id.spinner8);
        mCbHoldTime = (CheckBox)findViewById(R.id.checkBox);
        mCbHoldTime.setOnCheckedChangeListener(this);
        mCbHoldTime.setChecked(false);
        //  グラフィックViewの設定
        mGpxGraphView = new GpxGraphView(this);
        LinearLayout linearlayout = (LinearLayout)findViewById(R.id.linearLayoutGpx);
        linearlayout.addView(mGpxGraphView);
        //  ドロップダウンリストの設定
        mGraphTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mGraphTitle);
        mSpGraphType.setAdapter(mGraphTypeAdapter);
        mFilterTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mFilterType);
        mSpFilterType.setAdapter(mFilterTypeAdapter);
    }
}
