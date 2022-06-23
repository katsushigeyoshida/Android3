package jp.co.yoshida.katsushige.calc2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import jp.co.yoshida.katsushige.mylib.YLib;

public class GraphActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "Calc2 GraphActivity";

    Spinner mSpDatas;
    ArrayAdapter<String> mDatasAdapter;
    RadioButton mRdHistGram;
    RadioButton mRdScatterPlots;
    RadioGroup mRgGraphType;
    CheckBox mCbRegressionCurve;
    CheckBox mCbDataValue;
    EditText mEdMinX;
    EditText mEdMaxX;
    EditText mEdMinY;
    EditText mEdMaxY;
    EditText mEdDivideCount;
    Button mBtRefresh;
    LinearLayout mLinearLayout;         //  Viewのコンテナ
    GraphView mGraphView;

    private String mSaveDirectory;
    private String mSaveStatisticsDirectory;
    private String mStatisticsFileName;

    private YCalc ycalc;
    private YLib ylib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        ylib = new YLib();
        ycalc = new YCalc();

        Intent intent = getIntent();
        mSaveStatisticsDirectory = intent.getStringExtra("STATISTICSDIRECTORY");
        mStatisticsFileName = intent.getStringExtra("STATISTICSFILENAME");

        init();
        mGraphView.mSaveStatisticsDirectory = mSaveStatisticsDirectory;
        setGraphData(mStatisticsFileName);  //  グラフの領域を求める
        setGraphArea();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBtRefresh.getId()) {
            //  グラフの表示を更新する
            mGraphView.mRegressionCurve = mCbRegressionCurve.isChecked();
            mGraphView.mDataValue = mCbDataValue.isChecked();
            setGraphArea();
            mGraphView.dispGraph();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mRdHistGram.isChecked()) {
            //  ヒストグラム表示
            mCbRegressionCurve.setEnabled(false);
            mEdDivideCount.setEnabled(true);
        } else {
            //  散布図表示
            mCbRegressionCurve.setEnabled(true);
            mEdDivideCount.setEnabled(false);
        }
    }

    /**
     * グラフデータをファイルから読み込んでパラメータを設定する
     * @param fileName      データファイル名
     */
    private void setGraphData(String fileName) {
        mGraphView.setData(fileName);   //  パラメータの設定
        if (1 < ycalc.getXYListOrder(mGraphView.mStatisticsData)) {
            setScatterPlots();          //  散布図の表示(1次元データ)
        } else {
            setHistGram();              //  ヒストグラムの表示(2次元データ)
        }
    }

    /**
     * ヒストグラムのデータ領域を求める
     */
    private void setHistGram() {
        mRdHistGram.setChecked(true);
        mGraphView.mGraphType = GraphView.GRAPHTYPE.HISTGRAM;
        //  グラフの表示領域を求める
        double xmin = ycalc.getMinList(mGraphView.mStatisticsDataX);
        double xmax = ycalc.getMaxList(mGraphView.mStatisticsDataX);
        double xRangeMin = ylib.graphRangeMin(xmin, xmax);
        double xRangeMax = ylib.graphRangeMax(xmin, xmax);
        //  ヒストグラムの分布を求める
        int[] map = ycalc.getDataMap(mGraphView.mStatisticsDataX, xRangeMin, xRangeMax,
                Integer.valueOf(mEdDivideCount.getText().toString()));
        int maxCount = 0;
        //  mapの最大値をもとめる
        for (int key : map)
            maxCount = maxCount < key ? key : maxCount;
        double yRangeMin = 0;
        double yRangeMax = ylib.graphRangeMax(0, maxCount);
        //  データ領域をコントロールに設定する
        mEdMinX.setText(String.valueOf(xRangeMin));
        mEdMaxX.setText(String.valueOf(xRangeMax));
        mEdMinY.setText(String.valueOf(yRangeMin));
        mEdMaxY.setText(String.valueOf(yRangeMax));
    }

    /**
     * 散布図のデータ領域を求める
     */
    private void setScatterPlots() {
        mRdScatterPlots.setChecked(true);
        mGraphView.mGraphType = GraphView.GRAPHTYPE.SCATTERPLOTS;
        //  グラフの表示範囲意を求める
        double xmin = ycalc.getMinList(mGraphView.mStatisticsDataX);
        double xmax = ycalc.getMaxList(mGraphView.mStatisticsDataX);
        double ymin = ycalc.getMinList(mGraphView.mStatisticsDataY);
        double ymax = ycalc.getMaxList(mGraphView.mStatisticsDataY);
        double xRangeMin = ylib.graphRangeMin(xmin, xmax);
        double xRangeMax = ylib.graphRangeMax(xmin, xmax);
        double yRangeMin = ylib.graphRangeMin(ymin, ymax);
        double yRangeMax = ylib.graphRangeMax(ymin, ymax);
        mCbRegressionCurve.setChecked(false);
        mEdMinX.setText(String.valueOf(xRangeMin));
        mEdMaxX.setText(String.valueOf(xRangeMax));
        mEdMinY.setText(String.valueOf(yRangeMin));
        mEdMaxY.setText(String.valueOf(yRangeMax));
    }

    /**
     * グラフ領域を設定する
     */
    private void setGraphArea() {
        mGraphView.setGraphArea(Float.valueOf(mEdMinX.getText().toString()),Float.valueOf(mEdMaxY.getText().toString()),
                Float.valueOf(mEdMaxX.getText().toString()),Float.valueOf(mEdMinY.getText().toString()));
        mGraphView.mDivideCount = Integer.valueOf(mEdDivideCount.getText().toString());
    }

    /**
     * 初期化
     */
    private void init() {

        List<String> fileList = ylib.getFileList(mSaveStatisticsDirectory, "*.csv", false);
        String[] files = new String[fileList.size()];
        for (int i = 0; i < fileList.size(); i++)
            files[i] = ylib.getNameWithoutExt(fileList.get(i));

        mSpDatas = (Spinner)findViewById(R.id.spinner);
        mDatasAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, files);
        mSpDatas.setAdapter(mDatasAdapter);
        mSpDatas.setSelection(mDatasAdapter.getPosition(mStatisticsFileName));

        mSpDatas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStatisticsFileName = mDatasAdapter.getItem(position);
                mGraphView.mDataFileName = mStatisticsFileName;
                setGraphData(mStatisticsFileName);
                setGraphArea();
                mGraphView.dispGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRdHistGram = (RadioButton)findViewById(R.id.radioButton3);
        mRdScatterPlots = (RadioButton)findViewById(R.id.radioButton5);
        mRgGraphType = (RadioGroup)findViewById(R.id.radioGroup);
        mRgGraphType.setOnCheckedChangeListener(this);
        mEdMinX = (EditText)findViewById(R.id.editText);
        mEdMaxX = (EditText)findViewById(R.id.editText2);
        mEdMinY = (EditText)findViewById(R.id.editText3);
        mEdMaxY = (EditText)findViewById(R.id.editText4);
        mEdDivideCount = (EditText)findViewById(R.id.editText5);
        mEdDivideCount.setText("10");
        mCbRegressionCurve = (CheckBox)findViewById(R.id.checkBox);
        mCbDataValue = (CheckBox)findViewById(R.id.checkBox2);
        mCbDataValue.setChecked(false);
        mBtRefresh = (Button)findViewById(R.id.button11);
        mBtRefresh.setOnClickListener(this);

        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        mGraphView  = new GraphView(this);
        mLinearLayout.addView(mGraphView);

        //  フォーカスをボタンに移して起動時にキーボードが出るのを防ぐ
        mSpDatas.setFocusable(true);
        mSpDatas.setFocusableInTouchMode(true);
        mSpDatas.requestFocus();
    }
}
