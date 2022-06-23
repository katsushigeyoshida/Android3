package jp.co.yoshida.katsushige.ytoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.yoshida.katsushige.mylib.ListData;
import jp.co.yoshida.katsushige.mylib.YLib;

public class TimeTable extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "TimeTableActivity";

    private String mSaveDirectory;                  //  ファイル保存ディレクトリ
    private YLib ylib;
    private DataList mDataList;                     //  時刻表データ
    private TableLayout mTableLayout;
    private Spinner mSpArea;
    private Spinner mSpLocation;
    private Spinner mSpRoot;
    private TextView mTvURL;
    private TextView mTvDateIsuue;
    private Button mBtAttension;
    private Button mBtLeftSlide;
    private Button mBtRightSlide;
    private TextView mTvBusTitle;
    private TextView mTvBusTitle1;
    private TextView mTvBusTitle2;
    private TextView mTvBusTitle3;
    private TextView mTvBusTitle4;
    private TextView mTvBusTitle5;
    private ArrayAdapter<String> mAreaAdapter;
    private ArrayAdapter<String> mLocationAdapter;
    private ArrayAdapter<String> mRootAdapter;
    private String mCurAttension;                   //  注意データ
    private int mDispOffset = 0;                    //  時刻表示開始位置のオフセット
    private int mDispSize;                          //  バスの便数(時刻の数)
    private TimeTableData mCurTimeTable;            //  時刻データ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        this.setTitle("バス時刻表");
        ylib = new YLib(this);
        init();

        //  作業用ファイルディレクトリの設定
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt());
        mSaveDirectory = ylib.setSaveDirectory(ylib.getPackageNameWithoutExt()+"/TimeTable");
        //  データの取り込み
        mDataList = new DataList(this, mSaveDirectory);
        mDataList.getDataList();
        for (int i = 0; i < mDataList.mTimeTableList.size(); i++) {
            mDataList.mTimeTableList.get(i).dataTrace();
        }
        //  地域の初期設定
        setAreaSpinner();
        //  初期表示
        if (0 < mDataList.mTimeTableList.size())
            setTimeTableDatat(mDataList.mTimeTableList.get(0));
        else
            Toast.makeText(this, "時刻表データが登録されていません", Toast.LENGTH_LONG).show();;

        //  地域の変更
        mSpArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setLocationSpinner(mAreaAdapter.getItem(mSpArea.getSelectedItemPosition()));
                setRootSpinner(mAreaAdapter.getItem(mSpArea.getSelectedItemPosition()),
                        mLocationAdapter.getItem(mSpLocation.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //  場所の変更
        mSpLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setRootSpinner(mAreaAdapter.getItem(mSpArea.getSelectedItemPosition()),
                        mLocationAdapter.getItem(mSpLocation.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //  ルートの変更
        mSpRoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String area = mAreaAdapter.getItem(mSpArea.getSelectedItemPosition());
                String location = mLocationAdapter.getItem(mSpLocation.getSelectedItemPosition());
                String root=mRootAdapter.getItem(mSpRoot.getSelectedItemPosition());
                mCurTimeTable = mDataList.getTimeTableData(area, location, root);
                mDispOffset = 0;
                setTimeTableDatat(mCurTimeTable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtAttension.getId()) {             //  注意の項目に'*'があれば内容を表示
            ylib.messageDialog(this, "注の表示", mCurAttension);
        } else if (view.getId() == mBtLeftSlide.getId()) {      //  時刻表を左にずらす
            if (0 <mDispOffset) {
                mDispOffset--;
                setTimeTableDatat(mCurTimeTable);
            }
        } else if (view.getId() == mBtRightSlide.getId()) {     //  時刻表を右にずらす
            if (mDispOffset < mDispSize) {
                mDispOffset++;
                setTimeTableDatat(mCurTimeTable);
            }
        } else if (view.getId() == mTvURL.getId()) {            //  URLがあれば表示
            ylib.webDisp(this, mTvURL.getText().toString());
        }
    }

    /**
     * 時刻表データの登録表示
     * @param data      時刻データ
     */
    private void setTimeTableDatat(TimeTableData data) {
        if (data == null)
            return;
        if (0 < mDispOffset)
            mBtLeftSlide.setEnabled(true);
        else
            mBtLeftSlide.setEnabled(false);
        if (mDispOffset < mDispSize - 1)
            mBtRightSlide.setEnabled(true);
        else
            mBtRightSlide.setEnabled(false);

        data.dataTrace();
        mDispSize = data.mBusCount;
        mTvURL.setText(data.mURL);
        mTvDateIsuue.setText(data.mDateIsuue);
        mTableLayout.removeAllViews();
        setDispData("開始日", data.mStartDate);
        setDispData("終了日", data.mEndDate);
        setDispData("運行日", data.mDayWeek);
        setDispData("注意", data.mAtention);
        for (int i=0; i < data.mBusTime.size(); i++) {
            setDispData(data.mBusTime.get(i));
        }
    }

    /**
     * 開始日/終了日/運航日/注意の登録と表示
     * @param title         タイトル名
     * @param datas         配列データ
     */
    private void setDispData(String title, String[] datas) {
        int datasize = datas.length;
        mCurAttension ="";
        TableRow tableRow = (TableRow)getLayoutInflater().inflate(R.layout.table_row,null);
        TextView busStop = (TextView)tableRow.findViewById(R.id.textView3);
        busStop.setText(title);
        TextView bus1 = (TextView)tableRow.findViewById(R.id.textView8);
        bus1.setText(cnvStrinData(title, datas, 0+mDispOffset));
        TextView bus2 = (TextView)tableRow.findViewById(R.id.textView9);
        bus2.setText(cnvStrinData(title, datas, 1+mDispOffset));
        TextView bus3 = (TextView)tableRow.findViewById(R.id.textView10);
        bus3.setText(cnvStrinData(title, datas, 2+mDispOffset));
        TextView bus4 = (TextView)tableRow.findViewById(R.id.textView16);
        bus4.setText(cnvStrinData(title, datas, 3+mDispOffset));
        TextView bus6 = (TextView)tableRow.findViewById(R.id.textView17);
        bus6.setText(cnvStrinData(title, datas, 4+mDispOffset));
        TextView bus7 = (TextView)tableRow.findViewById(R.id.textView20);
        bus7.setText(cnvStrinData(title, datas, 5+mDispOffset));

        mTableLayout.addView(tableRow, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 時刻データの登録表示
     * @param datas             時刻の配列データ([0]はバス停名)
     */
    private void setDispData(String[] datas) {
        int datasize = datas.length;
        TableRow tableRow = (TableRow)getLayoutInflater().inflate(R.layout.table_row,null);
        TextView busStop = (TextView)tableRow.findViewById(R.id.textView3);
        busStop.setText(cnvStrinData("", datas, 0));
        TextView bus1 = (TextView)tableRow.findViewById(R.id.textView8);
        bus1.setText(cnvStrinData("", datas, 1+mDispOffset));
        TextView bus2 = (TextView)tableRow.findViewById(R.id.textView9);
        bus2.setText(cnvStrinData("", datas, 2+mDispOffset));
        TextView bus3 = (TextView)tableRow.findViewById(R.id.textView10);
        bus3.setText(cnvStrinData("", datas, 3+mDispOffset));
        TextView bus4 = (TextView)tableRow.findViewById(R.id.textView16);
        bus4.setText(cnvStrinData("", datas, 4+mDispOffset));
        TextView bus6 = (TextView)tableRow.findViewById(R.id.textView17);
        bus6.setText(cnvStrinData("", datas, 5+mDispOffset));
        TextView bus7 = (TextView)tableRow.findViewById(R.id.textView20);
        bus7.setText(cnvStrinData("", datas, 6+mDispOffset));

        mTableLayout.addView(tableRow, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 時刻表の表示データの変換
     * データがない時は空白、注意データの場合は'*'に変換する
     * @param title         タイトル名
     * @param datas         開始日/終了日/運航日/注意/時刻の配列データ
     * @param n             変換するデータの位置
     * @return              変換データ
     */
    private String cnvStrinData(String title, String[] datas, int n) {
        if (n < datas.length) {
            if (title.compareTo("注意")==0) {
                if (datas[n] != null)
                    if (0 < datas[n].length()) {
                        mCurAttension += (n+1) + "列目: "+datas[n] + "\n";
                        return " *";
                    }
            } else {
                if (datas[n] != null)
                    return "  "+datas[n];
            }
        }
        return "";
    }

    /**
     * 地域spinnerの設定
     */
    private void setAreaSpinner() {
        String[] areaArray = mDataList.getAreaList();
        mAreaAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, areaArray);
        mSpArea.setAdapter(mAreaAdapter);
    }

    /**
     * 場所spinnerの設定
     * @param area          地域名
     */
    private void setLocationSpinner(String area) {
        String[] areaArray = mDataList.getLocalAreaList(area);
        mLocationAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, areaArray);
        mSpLocation.setAdapter(mLocationAdapter);
    }

    /**
     * ルートspinnerの設定
     * @param area          地域名
     * @param localArea     場所名
     */
    private void setRootSpinner(String area, String localArea) {
        String[] areaArray = mDataList.getRootList(area, localArea);
        mRootAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, areaArray);
        mSpRoot.setAdapter(mRootAdapter);
    }

    /**
     * 画面データの初期化
     */
    private void init() {
        mTableLayout = (TableLayout)findViewById(R.id.tableLayout);
        mSpArea = (Spinner)findViewById(R.id.spinner);
        mSpLocation = (Spinner)findViewById(R.id.spinner3);
        mSpRoot = (Spinner)findViewById(R.id.spinner2);
        mTvURL = (TextView)findViewById(R.id.textView19);
        mTvDateIsuue = (TextView)findViewById(R.id.textView12);
        mBtAttension = (Button)findViewById(R.id.button);
        mBtLeftSlide = (Button)findViewById(R.id.button2);
        mBtRightSlide = (Button)findViewById(R.id.button3);

        mTvBusTitle = (TextView)findViewById(R.id.textView4);
        mTvBusTitle1 = (TextView)findViewById(R.id.textView5);
        mTvBusTitle2 = (TextView)findViewById(R.id.textView6);
        mTvBusTitle3 = (TextView)findViewById(R.id.textView7);
        mTvBusTitle4 = (TextView)findViewById(R.id.textView13);
        mTvBusTitle5 = (TextView)findViewById(R.id.textView15);

        mTvURL.setOnClickListener(this);
        mBtAttension.setOnClickListener(this);
        mBtLeftSlide.setOnClickListener(this);
        mBtRightSlide.setOnClickListener(this);
    }

}