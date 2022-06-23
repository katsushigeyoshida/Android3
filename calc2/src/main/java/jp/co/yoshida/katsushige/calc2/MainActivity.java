package jp.co.yoshida.katsushige.calc2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

import jp.co.yoshida.katsushige.mylib.YLib;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "Calc2 MainActivity";

    TextView mDisplayPanel;
    TextView mSubDispayPanel;
    Button mKey0;
    Button mKey1;
    Button mKey2;
    Button mKey3;
    Button mKey4;
    Button mKey5;
    Button mKey6;
    Button mKey7;
    Button mKey8;
    Button mKey9;
    Button mKeyDot;
    Button mKeyPlMi;
    Button mKeyEQ;
    Button mKeyAdd;
    Button mKeySub;
    Button mKeyMul;
    Button mKeyDiv;
    Button mKeyBS;
    Button mKeyCE;
    Button mKeyAC;
    Button mKeyE0;          //  dummy
    Button mKeyE1;
    Button mKeyE2;
    Button mKeyE3;
    Button mKeyE4;
    Button mKeyE5;          //
    Button mKeyD0;
    Button mKeyD1;
    Button mKeyD2;
    Button mKeyD3;
    Button mKeyD4;
    Button mKeyD5;          //  剰余  modulo
    Button mKeyC0;
    Button mKeyC1;
    Button mKeyC2;
    Button mKeyC3;          //  1/x Reciprocal 逆数
    Button mKeyC4;          //  %
    Button mKeyC5;          //  ! Factrial 階乗
    Button mKeyB0;
    Button mKeyB1;
    Button mKeyB2;
    Button mKeyB3;
    Button mKeyB4;
    Button mKeyB5;
    Button mKeyA0;
    Button mKeyA1;
    Button mKeyA2;
    Button mKeyA3;
    Button mKeyA4;
    Button mKeyA5;

    //  キーの種類　数字、単項演算子二項演算子、多項演算子、定数、操作キー
    enum KEYTYPE {NON,NUMBER,UNARY,BINARY,TEMARY,CONSTANT,OPERATOR};
    private HashMap<String, KEYTYPE> mKeyTypeMap;               //  キーの種類を割り付ける
    private HashMap<String, String> mKeyHelpMap;                //  キーの説明を割り付ける
    private int mDigitNumber = 14;                              //  実数の丸め有効桁数
    private String mKeyBuffer = "";                             //  入力キーのバッファ
    private Stack<String> mInputDataStack;                      //  入力した数値または演算子をスタック
    private Stack<String> mInputDataTempStack;                  //  入力した数値または演算子をスタック
    private ArrayList<String> mStatisticsData;                  //  統計計算用データ
    private ArrayList<String> mOperationHistoryList;            //  操作履歴
    //    private String mOperationBuffer;                            //  操作履歴保存バッファ
    private String mStatisticsTempData = "";                    //  統計計算用一時データ
    private boolean mShiftMode = false;                         //  Shiftの状態化
    private boolean mStatisticsMode = false;                    //  統計モード
    private String[] mModeMenu = {"計算式処理","カスタム計算"};   //  MODEのメニュー
    private  String[][] mConvertMenu = {
            {"度(時)(ddd.dddd)→度分秒(時分秒)(ddd.mmss)に変換","deg2dms"},
            {"度分秒(時分秒)(ddd.mm.ss)→度(時)(ddd.dddd)に変換","dms2deg"}
    };
    private String[][] mStatisticsMenu = {{"統計計算メニュー",""}};
    private String[][] mDateMenu={
            {"今日を西暦(yyyy.mmdd)で表示する","today"},
            {"西暦(yyyy.mmdd)→ユリウス日(ddd)に変換","date2JulianDay"},
            {"ユリウス日(ddd)→西暦(yyyy.mmdd)に変換","julianDay2DateYear"},
            {"西暦(yyyy.mmdd)→和暦(GGyy年mm月dd日)に変換","date2JpnCal"},
            {"西暦(yyyy.mmdd)→令和(yy.mmdd)に変換","date2Reiwa"},
            {"西暦(yyyy.mmdd)→平成(yy.mmdd)に変換","date2Heisei"},
            {"西暦(yyyy.mmdd)→昭和(yy.mmdd)に変換","date2Shyouwa"},
            {"西暦(yyyy.mmdd)→大正(yy.mmdd)に変換","date2Taisyou"},
            {"西暦(yyyy.mmdd)→明治(yy.mmdd)に変換","date2Meiji"},
            {"令和(yy.mmdd)→西暦(yyyy.mmdd)に変換","reiwa2Date"},
            {"平成(yy.mmdd)→西暦(yyyy.mmdd)に変換","heisei2Date"},
            {"昭和(yy.mmdd)→西暦(yyyy.mmdd)に変換","shyouwa2Date"},
            {"大正(yy.mmdd)→西暦(yyyy.mmdd)に変換","taisyou2Date"},
            {"明治(yy.mmdd)→西暦(yyyy.mmdd)に変換","meiji2Date"},
            {"西暦(yyyy.mmdd)を入力して年数差","diffYears"},
            {"西暦(yyyy.mmdd)を入力して日数差","diffDays"},
            {"西暦(yyyy.mmdd)から旧歴(yyyy年mm月dd日)","date2Kyureki"},
            {"西暦(yyyy.mmdd)から六曜","date2Rokuyo"},
            {"西暦(yyyy.mmdd)から十干十二支","date2JyukanJyunishi"},
            {"西暦(yyyy.mmdd)から曜日","getDayWeek"},
            {"月齢を求める","moonAge"},
            {"日出の時間(hh.mmss)を求める","getSunRise"},
            {"日没の時間(hh.mmss)を求める","getSunSet"},
    };
    private String[][] mUnitConversionConst = {
            {"長さ メートル(m) → 尺","3.3"},
            {"長さ メートル(m) → 間","0.55"},
            {"長さ メートル(m) → 里","0.00025"},
            {"長さ メートル(m) → インチ","39.3701"},
            {"長さ メートル(m) → フィート","3.28084"},
            {"長さ メートル(m) → ヤード","1.09361"},
            {"長さ メートル(m) → マイル","0.000621"},
            {"面積 平方メートル(㎡) → 坪","0.3025"},
            {"面積 平方メートル(㎡) → 反","0.001008"},
            {"面積 平方メートル(㎡) → 町","0.000101"},
            {"面積 平方メートル(㎡) → アール","0.01"},
            {"面積 平方メートル(㎡) → 平方フィート","10.7639"},
            {"面積 平方メートル(㎡) → 平方マイル","0.0000004"},
            {"面積 平方メートル(㎡) → エーカー","0.000247"},
            {"体積 立法メートル(㎥) → リットル","0.001"},
            {"体積 立法メートル(㎥) → 合","0.001"},
            {"体積 立法メートル(㎥) → ガロン(英)","0.00022"},
            {"体積 立法メートル(㎥) → ガロン(米)","0.00026"},
            {"体積 立法メートル(㎥) → 立法インチ","0.06102"},
            {"体積 立法メートル(㎥) → 立法フィート","0.000035"},
            {"体積 立法メートル(㎥) → バレル","0.000006"},
            {"質量 グラム(g) → 貫","0.000267"},
            {"質量 グラム(g) → 斤","0.001667"},
            {"質量 グラム(g) → オンス","0.035274"},
            {"質量 グラム(g) → ポンド","0.0022046"},
            {"質量 グラム(g) → トン","0.000001"},
            {"質量 グラム(g) → トン(英)","0.00000098"},
            {"質量 グラム(g) → トン(米)","0.0000011"},
    };
    private String[] mDispMenu;
    private double mLatitude = 35.566095; 	        // 緯度		大田区矢口1-23-1
    private double mLongitude = 139.693404;	        // 経度

    private String mSaveDirectory;                  //  作業ディレクトリ/データ保存ディレクトリ
    private String mSaveStatisticsDirectory = "";   //  統計データファイル保存ディレクトリ
    private String mStatisticsFileName = "";        //  統計データファイル名

    private YLib ylib;
    private YCalc ycalc;
    private YDate ydate;
    private Vibrator vib;                   //  バイブレーションの設定にはManifestに権限追加必要

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");

        ylib = new YLib();
        ycalc = new YCalc();
        ydate= new YDate();
        vib = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
        ylib.checkStragePermission(this);

        init();
        initKeyMap();

        mInputDataStack = new Stack<String>();
        mInputDataTempStack = new Stack<String>();
        mStatisticsData = new ArrayList<String>();
        mOperationHistoryList = new ArrayList<String>();

        setSaveDirectory();
        setFunctionKey();
        mSubDispayPanel.setText("ボタンを長押しするとヘルプを表示すると思います");
    }

    public void onClick(View view) {
        Button bt = (Button)view;
        setKey(bt.getText().toString());
    }

    @Override
    public boolean onLongClick(View v) {
        Button bt = (Button)v;
        mSubDispayPanel.setText(mKeyHelpMap.get(bt.getText().toString()));
        return true;
    }

    /**
     * キーの種別により処理を分ける
     * @param key   入力キー
     */
    private void setKey(String key) {
        KEYTYPE keyType = mKeyTypeMap.get(key);
        if (keyType == null) {
            return;
        } else if (keyType == KEYTYPE.NUMBER) {
            setNumberKey(key);      //  数値入力
        } else if (keyType == KEYTYPE.UNARY) {
            setUnaryKey(key);       //  単項演算子
        } else if (keyType == KEYTYPE.BINARY) {
            setBinaryKey(key);      //  二項演算子
        } else if (keyType == KEYTYPE.CONSTANT) {
            setConstantKey(key);    //  定数
        } else if (keyType == KEYTYPE.OPERATOR) {
            setOperate(key);        //  操作キー処理
        }
        vib.vibrate(10);
    }

    /**
     * 定数の入力処理
     * @param key   入力キー
     */
    private void setConstantKey(String key) {
        double result = 0d;
        if (key.compareTo("PI") == 0) {             //  円周率
            result = Math.PI;
        } else if (key.compareTo("E") == 0) {       //  自然対数の底
            result = Math.E;
        } else if (key.compareTo("today") == 0) {   //  今日の日付(yyyy.mmdd)
            Date date = new Date();
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'.'MMdd");
            result = Double.valueOf(sdf1.format(date));
        }
        mKeyBuffer = String.valueOf(ycalc.getEfficientRound(result, mDigitNumber));
        mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
        mInputDataStack.push(mKeyBuffer);
        mKeyBuffer= "";
        setFunctionKey();
    }

    /***
     * 単項演算子の処理
     * @param key       単項演算子のキーワード
     */
    private void setUnaryKey(String key) {
        Log.d(TAG,"setUnaryKey:" + key);
        double x=0d, result=0d;
        if (mKeyBuffer.isEmpty()) {
            if (!mStatisticsMode) {
                if (0 < mInputDataStack.size()) {
                    String buf = mInputDataStack.pop();
                    if (ylib.isFloat(buf))
                        x = Double.valueOf(buf);
                    else
                        return;
                } else
                    return;
            }
        } else {
            x = Double.valueOf(mKeyBuffer);
        }
        try {
            if (key.compareTo("2√") == 0) {                 //  平方根
                result = Math.sqrt(x);
            } else if (key.compareTo("3√") == 0) {          //  立方根
                result = Math.cbrt(x);
            } else if (key.compareTo("1/x") == 0) {         //  逆数
                result = 1d / x;
            } else if (key.compareTo("%") == 0) {           //  パーセント(100倍)
                result = x * 100d;
            } else if (key.compareTo("n!") == 0) {          //  階乗
                result = ycalc.factorial((int) x);
            } else if (key.compareTo("sin") == 0) {         //  正弦
                result = Math.sin(x);
            } else if (key.compareTo("cos") == 0) {         //  余弦
                result = Math.cos(x);
            } else if (key.compareTo("tan") == 0) {         //  正接
                result = Math.tan(x);
            } else if (key.compareTo("asin") == 0) {        //  逆正弦
                result = Math.asin(x);
            } else if (key.compareTo("acos") == 0) {        //  逆余弦
                result = Math.acos(x);
            } else if (key.compareTo("atan") == 0) {        //  逆正接
                result = Math.atan(x);
            } else if (key.compareTo("sinh") == 0) {        //  双曲線正弦
                result = Math.sinh(x);
            } else if (key.compareTo("cosh") == 0) {        //  双曲線余弦
                result = Math.cosh(x);
            } else if (key.compareTo("tahn") == 0) {        //  双曲線正接
                result = Math.tanh(x);
            } else if (key.compareTo("ln") == 0) {          //  自然対数
                result = Math.log(x);
            } else if (key.compareTo("log") == 0) {         //  10が底の対数
                result = Math.log10(x);
            } else if (key.compareTo("x^2") == 0) {         //  平方
                result = x * x;
            } else if (key.compareTo("x^3") == 0) {         //  立法
                result = x * x * x;
            } else if (key.compareTo("e^x") == 0) {         //  Eの指数
                result = Math.pow(Math.E,x);
            } else if (key.compareTo("10^x") == 0) {        //  10の指数
                result = Math.pow(10,x);
            } else if (key.compareTo("rad") == 0) {         //  度をラジアンに変換
                result = x * Math.PI / 180d;
            } else if (key.compareTo("deg") == 0) {         //  ラジアンを度に変換
                result = x * 180d / Math.PI;
            } else if (key.compareTo("round") == 0) {       //  四捨五入
                result = Math.round(x);
            } else if (key.compareTo("ceil") == 0) {        //  切上げ
                result = Math.ceil(x);
            } else if (key.compareTo("floor") == 0) {       //  切捨て
                result = Math.floor(x);
            } else if (key.compareTo("rint") == 0) {        //  浮動小数点の整数部
                result = Math.rint(x);
            } else if (key.compareTo("deg2dms") == 0) {     //  度を度分秒に変換
                result = ycalc.deg2dms(x);
            } else if (key.compareTo("dms2deg") == 0) {     //  度分秒を度に変換
                result = ycalc.dms2deg(x);
            } else if (key.compareTo("date2JulianDay") == 0) {      //  年月日をユリウス日に変換
                result = ydate.date2JulianDay(x);
            } else if (key.compareTo("julianDay2DateYear") == 0) {  //  ユリウス日を年月日に変換
                result = ydate.julianDay2DateYear(x);
            } else if (key.compareTo("date2JpnCal") == 0) {     //  西暦を和暦に変換
                mKeyBuffer = ydate.date2JpnCal(x);
            } else if (key.compareTo("date2Reiwa") == 0) {     //  西暦を令和に変換
                result = ydate.date2Reiwa(x);
            } else if (key.compareTo("date2Heisei") == 0) {     //  西暦を平成に変換
                result = ydate.date2Heisei(x);
            } else if (key.compareTo("date2Shyouwa") == 0) {    //  西暦を昭和に変換
                result = ydate.date2Shyouwa(x);
            } else if (key.compareTo("date2Taisyou") == 0) {    //  西暦を大正に変換
                result = ydate.date2Taishou(x);
            } else if (key.compareTo("date2Meiji") == 0) {      //  西暦を明治に変換
                result = ydate.date2Meiji(x);
            } else if (key.compareTo("reiwa2Date") == 0) {     //  令和を西暦に変換
                result = ydate.reiwa2Date(x);
            } else if (key.compareTo("heisei2Date") == 0) {     //  平成を西暦に変換
                result = ydate.heisei2Date(x);
            } else if (key.compareTo("shyouwa2Date") == 0) {    //  昭和とを西暦に変換
                result = ydate.shyouwa2Date(x);
            } else if (key.compareTo("taisyou2Date") == 0) {    //  大正を西暦に変換
                result = ydate.taisyou2Date(x);
            } else if (key.compareTo("meiji2Date") == 0) {      //  明治を西暦に変換
                result = ydate.meiji2Date(x);
            } else if (key.compareTo("date2Kyureki") == 0) {    //  西暦を旧歴に変換
                mKeyBuffer = ydate.date2Kyureki(x);
            } else if (key.compareTo("date2Rokuyo") == 0) {     //  西暦から六曜を求める
                mKeyBuffer = ydate.date2Rokuyo(x);
            } else if (key.compareTo("date2JyukanJyunishi") == 0) { //  西暦から十干十二支を求める
                mKeyBuffer = ydate.date2JyukanJyunishi(x);
            } else if (key.compareTo("getDayWeek") == 0) {      //  西暦から曜日を求める
                mKeyBuffer = ydate.getDayWeek(x);
            } else if (key.compareTo("moonAge") == 0) {         //  西暦から月齢を求める
                result = ydate.moonAge(x);
            } else if (key.compareTo("getSunRise") == 0) {      //  西暦から日の出時間を求める
                result = ydate.getSunRise(x,mLatitude, mLongitude);
            } else if (key.compareTo("getSunSet") == 0) {       //  西暦から日の入り時間を求める7
                result = ydate.getSunSet(x,mLatitude, mLongitude);
            } else if (key.compareTo("n") == 0) {               //  統計データのデータ数
                result = mStatisticsData.size();
            } else if (key.compareTo("xm") == 0) {              //  統計データのXデータのの平均
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                result = ycalc.getListMean(xlist);
            } else if (key.compareTo("ym") == 0) {              //  統計データのyデータのの平均
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getListMean(ylist);
            } else if (key.compareTo("σx") == 0) {              //  統計データのXの標準偏差
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                result = ycalc.getStdDev(xlist);
            } else if (key.compareTo("σy") == 0) {              //  統計データのXの標準偏差
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getStdDev(ylist);
            } else if (key.compareTo("σxy") == 0) {             //  統計データの共分散
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getCovarince(xlist, ylist);
            } else if (key.compareTo("ρ") == 0) {               //  統計データの相関係数
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getCorelation(xlist, ylist);
            } else if (key.compareTo("a") == 0) {               //  回帰分析の傾き
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getRegA(xlist, ylist);
            } else if (key.compareTo("b") == 0) {               //  回帰分析の切片
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getRegB(xlist, ylist);
            } else if (key.compareTo("Σx") == 0) {              //  xデータの総和
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                result = ycalc.getSumList(xlist);
            } else if (key.compareTo("Σy") == 0) {              //  yデータの総和
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getSumList(ylist);
            } else if (key.compareTo("Σx^2") == 0) {            //  xデータの二乗の総和
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                result = ycalc.getSqrSumList(xlist);
            } else if (key.compareTo("Σy^2") == 0) {            //  yデータの二乗の総和
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getSqrSumList(ylist);
            } else if (key.compareTo("Σyy") == 0)  {            //  x*yデータの総和
                ArrayList<Double> xlist = ycalc.getXlist(mStatisticsData);
                ArrayList<Double> ylist = ycalc.getYlist(mStatisticsData);
                result = ycalc.getXYSumList(xlist, ylist);
            }
        } catch (Exception e) {
            mDisplayPanel.setText("ERROR");
            return;
        }
        //  有効桁で丸める
        if (key.compareTo("date2JpnCal") != 0 &&
                key.compareTo("date2Kyureki") != 0 &&
                key.compareTo("date2Rokuyo") != 0 &&
                key.compareTo("date2JyukanJyunishi") != 0 &&
                key.compareTo("getDayWeek") != 0 )
            mKeyBuffer = String.valueOf(ycalc.getEfficientRound(result, mDigitNumber));

        mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
        mInputDataStack.push(mKeyBuffer);
        mKeyBuffer= "";
        if (!mStatisticsMode)
            setFunctionKey();
    }

    /**
     * 操作キーの処理 AC,CE,x⇔y,PUSH,POP,data,x,y,list,save,load,グラフ,shift,変換,統計,年月日,場所,履歴,計算式
     * @param key
     */
    private void setOperate(String key) {
        Log.d(TAG,"setOperate:"+key+" "+mKeyBuffer);
        if (key.compareTo("AC")==0) {                   //  オールクリア
            mSubDispayPanel.setText("");
            mInputDataStack.clear();
        } else if (key.compareTo("CE")==0) {            //  入力バッファのみクリア
        } else if (key.compareTo("x⇔y")==0) {          //  データの入れ替え
            if (2 <= mInputDataStack.size() && 0 < mKeyBuffer.length()) {
                String buf1 = mInputDataStack.pop();    //  直前のデータ(演算子であること)
                String buf2 = mInputDataStack.pop();    //  ひとつ前のデータ8数値であること)
                if (!ylib.isFloat(buf1) && ylib.isFloat(buf2)) {
                    mInputDataStack.push(mKeyBuffer);   //  入力バッファ上のデータをスタック
                    mInputDataStack.push(buf1);           //  直前のデータをスタック(演算子のはず)
                    mKeyBuffer = buf2;                     //  入力バッファに一つ前のデータを入れる
                    mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
                }
            }
        } else if (key.compareTo("PUSH")==0) {          //  表示データの一時保存
            mInputDataTempStack.push(mDisplayPanel.getText().toString());   //  一時スタックに入れる
        } else if (key.compareTo("POP")==0) {           //  一時保存したデータを取出す
            if (0 < mInputDataTempStack.size()) {
                mKeyBuffer = mInputDataTempStack.pop(); //  入力バッファにpushデータを入れる
                mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
                Log.d(TAG,"setOperate: pop "+" "+mKeyBuffer);
            }
        } else if (key.compareTo("data")==0) {          //  統計データの保存
            if (0 < mStatisticsTempData.length()) {
                mStatisticsData.add(mStatisticsTempData + "," + mDisplayPanel.getText().toString());
                mStatisticsTempData = "";
            } else {
                mStatisticsData.add(mDisplayPanel.getText().toString());
            }
            mStatisticsTempData = "";
        } else if (key.compareTo("x,y")==0) {           //  二次元統計データの入力
            mStatisticsTempData = mDisplayPanel.getText().toString();
        } else if (key.compareTo("list")==0) {          //  統計データをリスト表示
            setStatisticsDataDialog("統計データ", 0);
        } else if (key.compareTo("edit")==0) {          //  統計データの変更
            setStatisticsDataDialog("統計データの変更", 1);
        } else if (key.compareTo("save")==0) {          //  統計データをファイルに保存する
            saveStatisticsDataDialog();
        } else if (key.compareTo("load")==0) {          //  統計データをファイルから取り込む
            ylib.fileSelectDialog(this, mSaveStatisticsDirectory, "*.csv", false, iLoadStatisticsData);
        } else if (key.compareTo("clear")==0) {         //  統計データを全削除
            mStatisticsData.clear();
        } else if (key.compareTo("del")==0) {           //  統計の最終データを削除
            setStatisticsDataDialog("統計データの削除", 2);
        } else if (key.compareTo("グラフ")==0) {        //  統計データをグラフ表示する
            dispGraph();
        } else if (key.compareTo("shift")==0) {         //  ファンクションキーを変更する
            if (mShiftMode && !mStatisticsMode)
                setFunctionKey();
            else if (mShiftMode && mStatisticsMode)
                setFunctionStatisticsKey();
            else
                setFunctionShiftKey();
        } else if (key.compareTo("変換")==0) {
            mDispMenu = new String[mConvertMenu.length];
            for (int i = 0; i < mConvertMenu.length; i++)
                mDispMenu[i] = mConvertMenu[i][0];
            setFunctionMenuDialog("データ変換",mDispMenu);
        } else if (key.compareTo("統計")==0) {
            if (mStatisticsMode)
                setFunctionKey();
            else
                setFunctionStatisticsKey();
        } else if (key.compareTo("年月日")==0) {
            mDispMenu = new String[mDateMenu.length];
            for (int i = 0; i < mDateMenu.length; i++)
                mDispMenu[i] = mDateMenu[i][0];
            setFunctionMenuDialog("年月日", mDispMenu);
        } else if (key.compareTo("場所")==0) {
            setFunctionMenuDialog("場所の設定",ydate.arrayLocation);
        } else if (key.compareTo("定数")==0) {
            mDispMenu = new String[mUnitConversionConst.length];
            for (int i = 0; i < mUnitConversionConst.length; i++)
                mDispMenu[i] = mUnitConversionConst[i][0];
            setFunctionMenuDialog("単位変換定数", mDispMenu);
        } else if (key.compareTo("履歴")==0) {

        } else if (key.compareTo("3Dグラフ")==0) {
            Intent intent = new Intent(MainActivity.this, Graph3DActivity.class);
            startActivity(intent);
        } else if (key.compareTo("関数グラフ")==0) {
            Intent intent = new Intent(MainActivity.this, FuncGraphActivity.class);
            startActivity(intent);
        } else if (key.compareTo("計算式")==0) {        //  計算式処理画面に移行する
            Intent intent = new Intent(MainActivity.this, ExpressCalc.class);
            startActivity(intent);
        }
        //  表示バッファクリア
        if (key.compareTo("AC")==0 ||
                key.compareTo("CE")==0 ||
                key.compareTo("PUSH")==0 ||
                key.compareTo("data")==0 ||
                key.compareTo("x,y")==0 ) {
            mKeyBuffer = "";
            mDisplayPanel.setText("");
        }
    }

    /**
     * 二項演算子が入力された時の処理、処理後はキーバッファをクリアする
     * @param key
     */
    private void setBinaryKey(String key) {
        Log.d(TAG,"setBinaryKey0: "+key+":"+mKeyBuffer+" :"+stackDisp());
        String data1,data2,operater;
        if (!mKeyBuffer.isEmpty()) {
            //  キーバッファに数値データが入っている場合、スタックに保存
            if (ylib.isFloat(mKeyBuffer)) {
                mInputDataStack.push(mKeyBuffer);
            }
        }
        if (mInputDataStack.isEmpty()) {
            //  スタックにデータがない場合には処理を行わない
            mKeyBuffer = "";
            return;
        }
        Log.d(TAG,"setBinaryKey1: "+mInputDataStack.size()+":"+mKeyBuffer+" :"+stackDisp());

        if (3 <= mInputDataStack.size()) {
            //  演算データがそろっている場合(数値、演算子、数値など)
            data1 = mInputDataStack.pop();
            if (ylib.isFloat(data1)) {
                //  直前のデータが数値の時に演算を行う
                operater = mInputDataStack.pop();
                data2 = mInputDataStack.pop();
                mKeyBuffer = calcData(data2, data1, operater);      //  演算処理
                mInputDataStack.push(mKeyBuffer);
                mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
            } else {
                //  直前データが数値以外の時データを戻す
                mInputDataStack.push(data1);
            }
            if (key.compareTo("＝") != 0) {
                //  = キー以外の時演算子と推定してスタックに保存
                mInputDataStack.push(key);
            }
        } else if (3 > mInputDataStack.size()) {
            //  演算データがまだそろっていない時
            data1 = mInputDataStack.pop();
            if (ylib.isFloat(data1)) {
                mInputDataStack.push(data1);
            }
            if (key.compareTo("＝") != 0) {
                mInputDataStack.push(key);
            }
            mSubDispayPanel.setText(data1+key);
        }
        Log.d(TAG,"setBinaryKey2: "+mInputDataStack.size()+":"+mKeyBuffer+" :"+stackDisp());

        mKeyBuffer = "";
        setFunctionKey();
    }

    /**
     * スタックしたすべてのデータを文字列にする
     * @return
     */
    private String stackDisp() {
        ArrayList<String> stackData = new ArrayList<String>();
        while (0 < mInputDataStack.size()) {
            stackData.add(mInputDataStack.pop());
        }
        String buf = "";
        for (int i = stackData.size() - 1; 0 <= i; i--) {
            buf += " "+stackData.get(i);
            mInputDataStack.push(stackData.get(i));
        }
        return buf;
    }

    /**
     * 二項演算子による計算処理
     * 計算式はサブディスプレイに表示
     * @param data1         x 数値の文字データ
     * @param data2         y 数値の文字データ
     * @param ope           演算子の記号
     * @return              計算結果(文字データ)
     */
    private String calcData(String data1, String data2, String ope) {
        mSubDispayPanel.setText(data1+ope+data2);
        double x=0,y=0,result=0;
        if (ylib.isFloat(data1) && ylib.isFloat(data2) && !ylib.isFloat(ope)) {
            x = Double.valueOf(data1);
            y = Double.valueOf(data2);
        } else {
            return "";
        }
        if (ope.compareTo("＋")==0) {
            result = x + y;
        } else if (ope.compareTo("－")==0) {
            result = x - y;
        } else if (ope.compareTo("Ｘ")==0) {
            result = x * y;
        } else if (ope.compareTo("／")==0) {
            result = x / y;
        } else if (ope.compareTo("x^y")==0) {
            result = Math.pow(x, y);
        } else if (ope.compareTo("x^1/y")==0) {
            result = Math.pow(x, 1/y);
        } else if (ope.compareTo("mod")==0) {
            result = x % y;
        } else if (ope.compareTo("順列")==0) {
            result = ycalc.permutation((int)x, (int)y);     //  xPy
        } else if (ope.compareTo("組合せ")==0) {
            result = ycalc.combination((int)x, (int)y);     //  xCy
        } else if (ope.compareTo("diffYears")==0) {         //  年数の差
            result = ydate.diffYears(x, y);
        } else if (ope.compareTo("diffDays")==0) {          //  日にちの差
            result = ydate.diffDays(x, y);
        } else {
            return  "";
        }
        return String.valueOf(ycalc.getEfficientRound(result, mDigitNumber));
    }

    /**
     * 数値データ入力処理 キー入力した値をバッファリングして表示
     * 表示は３桁区切り、処理数値: 0-9,.,±,BS,exp
     * @param key       数値文字データ
     */
    private void setNumberKey(String key) {
        if (key.compareTo(".")==0) {
            if (0 <= mKeyBuffer.indexOf(".")) {
                return;
            } else if (mKeyBuffer.length() == 0) {
                mKeyBuffer += "0.";
            } else {
                mKeyBuffer += key;
            }
        }else if (key.compareTo("±")==0) {
            if (mKeyBuffer.length() == 0)
                return;
            int m = mKeyBuffer.indexOf("E");
            if (0 <= m) {
                if (mKeyBuffer.charAt(m+1) == '-') {
                    mKeyBuffer = mKeyBuffer.substring(0, m+1) + mKeyBuffer.substring(m + 2);
                } else {
                    mKeyBuffer = mKeyBuffer.substring(0, m+1) + "-" + mKeyBuffer.substring(m + 1);
                }
            } else {
                if (mKeyBuffer.charAt(0) == '-') {
                    mKeyBuffer = mKeyBuffer.substring(1);
                } else {
                    mKeyBuffer = "-" + mKeyBuffer;
                }
            }
        }else if (key.compareTo("BS")==0) {
            if (mKeyBuffer.length() == 0)
                return;
            mKeyBuffer = mKeyBuffer.substring(0, mKeyBuffer.length() - 1);
            if (mKeyBuffer.length() == 1 && mKeyBuffer.charAt(0) == '-')
                mKeyBuffer = "";
        }else if (key.compareTo("exp")==0) {
            if (mKeyBuffer.length() == 0 || 0 <= mKeyBuffer.indexOf("E")) {
                return;
            } else {
                mKeyBuffer += "E";
            }
        } else {
            if (mKeyBuffer.length()==1) {
                if (mKeyBuffer.charAt(0) == '0')
                    mKeyBuffer = key;
                else
                    mKeyBuffer += key;
            } else if (mKeyBuffer.length()==2) {
                if (mKeyBuffer.charAt(0)=='-' && mKeyBuffer.charAt(1) == '0')
                    mKeyBuffer = mKeyBuffer.charAt(0) + key;
                else
                    mKeyBuffer += key;
            } else
                mKeyBuffer += key;
        }
//        Log.d(TAG,"setNumberKey:"+mKeyBuffer);
//        mKeyBuffer = ylib.zeroSupressNumber(mKeyBuffer);
        mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
    }

    private void setModeMenuDialog() {
        new AlertDialog.Builder(this)
                .setTitle("操作メニュー")
                .setItems(mModeMenu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, mModeMenu[which]+" が選択", Toast.LENGTH_LONG).show();
                        if (which == 0) {
                            Intent intent = new Intent(MainActivity.this, ExpressCalc.class);
                            startActivity(intent);
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * 関数の選択メニュー表示ダイヤログ
     * @param menu          関数の項目リスト
     */
    private void setFunctionMenuDialog(final String title, final String[] menu) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( MainActivity.this, which+" が選択", Toast.LENGTH_LONG).show();
                        if (title.compareTo("データ変換")==0) {
                            setKey(mConvertMenu[which][1]);
                        } else if (title.compareTo("統計")==0) {
                            setKey(mStatisticsMenu[which][1]);
                        } else if (title.compareTo("年月日")==0) {
                            setKey(mDateMenu[which][1]);
                        } else if (title.compareTo("場所設定")==0) {
                            String[] loc = ydate.getLocationSplit(ydate.arrayLocation[which]);
                            mDisplayPanel.setText(loc[0]+"("+loc[1]+","+loc[2]+")");
                            mLatitude = Double.valueOf(loc[1]);
                            mLongitude = Double.valueOf(loc[2]);
                        } else if (title.compareTo("単位変換定数")==0) {
                            setConstantData(Double.valueOf(mUnitConversionConst[which][1]));
                        }
                    }
                })
                .create()
                .show();
    }

    /***
     * 登録された統計データをリスト表示する
     * @param title         ダイヤログのタイトル
     * @param type          統計データの処理方法(0:メニューを表示, 1:データ値を変更,2:データの削除
     */
    private void setStatisticsDataDialog(final String title, final int type) {
        mDispMenu = new String[mStatisticsData.size()];
        for (int i = 0; i < mStatisticsData.size(); i++)
            mDispMenu[i] = mStatisticsData.get(i);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(mDispMenu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( MainActivity.this,
                                which+" "+mStatisticsData.get(which)+" が選択", Toast.LENGTH_LONG).show();
                        switch (type) {
                            case 0 :        //  メニュー表示
                                setDataEditMenuDialog(mStatisticsData.get(which)+" のデータ編集", which);
                                break;
                            case 1 :        //  データ編集
                                EditTextDialog("データ変更",which);
                                break;
                            case 2 :        //  データ削除
                                mStatisticsData.remove(which);
                                break;
                        }
                    }
                })
                .create()
                .show();
    }

    /***
     * 指定のデータの編集メニューの選択表示
     * @param title     ダイヤログのタイトル
     * @param no        データNo
     */
    private void setDataEditMenuDialog(final String title, final int no) {
        final String[] menu = {"変更","削除","全削除"};
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( MainActivity.this,
                                which+" "+menu[which]+" が選択", Toast.LENGTH_LONG).show();
                        switch (which) {
                            case 0:
                                EditTextDialog("データ変更",no);
                                break;
                            case 1:
                                mStatisticsData.remove(no);
                                break;
                            case 2:
                                mStatisticsData.clear();
                                break;
                        }
                    }
                })
                .create()
                .show();
    }

    /***
     * 統計データの編集ダイヤログ (EditTextの2段表示)
     * @param title     ダイヤログのタイトル
     * @param no        データNo
     */
    private void EditTextDialog(final String title,final int no) {
        String[] text = mStatisticsData.get(no).split(",");
        final EditText editText1 = new EditText(this);
        editText1.setWidth(100);
        editText1.setGravity(Gravity.RIGHT);
        editText1.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText1.setText(0<text.length?text[0]:"");
        final EditText editText2 = new EditText(this);
        editText2.setWidth(100);
        editText2.setGravity(Gravity.RIGHT);
        editText2.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText2.setText(1<text.length?text[1]:"");
        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(editText1);
        mLinearLayout.addView(editText2);
        new AlertDialog.Builder(this)
                //	.setIcon(R.drawable.icon)
                .setTitle(title)
                .setView(mLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "EditTextDialog:　OKボタン " + editText1.getText().toString()+
                                " "+ editText2.getText().toString());
                        /* OKボタンをクリックした時の処理 */
                        String text = editText1.getText().toString();
                        if (0 < editText2.getText().length())
                            text += "," + editText2.getText().toString();
                        mStatisticsData.set(no, text);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* Cancel ボタンをクリックした時の処理 */
                    }
                })
                .show();

    }

    /***
     * 統計データをファイル名を入力して保存
     */
    private void saveStatisticsDataDialog() {
        final EditText editText = new EditText(this);
        editText.setText(mStatisticsFileName);
        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(editText);
        new AlertDialog.Builder(this)
                //	.setIcon(R.drawable.icon)
                .setTitle("データ保存ファイル名")
                .setView(mLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "EditTextDialog:　OKボタン " + editText.getText().toString());
                        /* OKボタンをクリックした時の処理 */
                        saveStatisticsData(editText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* Cancel ボタンをクリックした時の処理 */
                    }
                })
                .show();
    }

    /**
     * 統計データを取り込む関数型インタフェース
     */
    Consumer<String> iLoadStatisticsData = new Consumer<String>() {
        @Override
        public void accept(String path) {
            loadStatisticsData(path);
            mStatisticsFileName = ylib.getNameWithoutExt(path);
        }
    };

    /***
     * 統計データをファイルに保存する
     * @param fileName      ファイル名(ディレクトリと拡張しなし)
     */
    private void saveStatisticsData(String fileName) {
        mStatisticsFileName = fileName;
        String path = mSaveStatisticsDirectory + "/" + fileName + ".csv";
        String buffer = "";
        for (String key : mStatisticsData) {
            buffer += key + "\n";
        }
        if (0 < buffer.length()) {
            ylib.writeFileData(path, buffer);
        }
    }

    /**
     * 単位変換の乗数をパネルやキーバッファに設定する
     * @param data
     */
    private void setConstantData(double data) {
        mKeyBuffer = String.valueOf(ycalc.getEfficientRound(data, mDigitNumber));
        mDisplayPanel.setText(ylib.setDigitSeparator(mKeyBuffer));
        mInputDataStack.push(mKeyBuffer);
        mKeyBuffer= "";
        setFunctionKey();
    }

    /***
     * 統計データをファイルから取り込む
     * @param path      フルパス名
     */
    private void loadStatisticsData(String path) {
        if (!ylib.existsFile(path)) {
            Toast.makeText(this, "ファイルが存在していません\n"+path, Toast.LENGTH_LONG).show();
            return ;
        }
        //	ファイルデータの取り込み
        mStatisticsData.clear();
        ylib.readTextFile(path, mStatisticsData);
    }

    /***
     * 統計データのグラフ表示画面に切り替える
     */
    private void dispGraph() {
        if (0 < mStatisticsFileName.length()) {
            Intent intent = new Intent(this, GraphActivity.class);
            intent.putExtra("STATISTICSDIRECTORY", mSaveStatisticsDirectory);
            intent.putExtra("STATISTICSFILENAME", mStatisticsFileName);
            startActivity(intent);
        } else {
            Toast.makeText(this, "データが設定されていないか保存されていません", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * キーの機能設定
     */
    private void initKeyMap() {
        mKeyTypeMap = new HashMap<String, KEYTYPE>();
        mKeyHelpMap = new HashMap<String, String>();

        //  数値キー
        mKeyTypeMap.put("0", KEYTYPE.NUMBER);
        mKeyTypeMap.put("1", KEYTYPE.NUMBER);
        mKeyTypeMap.put("2", KEYTYPE.NUMBER);
        mKeyTypeMap.put("3", KEYTYPE.NUMBER);
        mKeyTypeMap.put("4", KEYTYPE.NUMBER);
        mKeyTypeMap.put("5", KEYTYPE.NUMBER);
        mKeyTypeMap.put("6", KEYTYPE.NUMBER);
        mKeyTypeMap.put("7", KEYTYPE.NUMBER);
        mKeyTypeMap.put("8", KEYTYPE.NUMBER);
        mKeyTypeMap.put("9", KEYTYPE.NUMBER);
        mKeyTypeMap.put(".", KEYTYPE.NUMBER);
        mKeyTypeMap.put("±", KEYTYPE.NUMBER);
        mKeyHelpMap.put("±", "符号を反転する");
        mKeyTypeMap.put("BS", KEYTYPE.NUMBER);
        mKeyHelpMap.put("BS", "1文字分だけ戻す");
        mKeyTypeMap.put("exp", KEYTYPE.NUMBER);
        mKeyHelpMap.put("exp", "指数を入力");
        //  二項演算子
        mKeyTypeMap.put("＝", KEYTYPE.BINARY);
        mKeyTypeMap.put("＋", KEYTYPE.BINARY);
        mKeyTypeMap.put("－", KEYTYPE.BINARY);
        mKeyTypeMap.put("Ｘ", KEYTYPE.BINARY);
        mKeyTypeMap.put("／", KEYTYPE.BINARY);
        mKeyTypeMap.put("mod", KEYTYPE.BINARY);
        mKeyHelpMap.put("mod", "割り算の余りを求める x mod y");
        mKeyTypeMap.put("x^y", KEYTYPE.BINARY);
        mKeyHelpMap.put("x^y", "累乗を求める x ^ y");
        mKeyTypeMap.put("x^1/y", KEYTYPE.BINARY);
        mKeyHelpMap.put("x^1/y", "累乗を求める x ^ 1/y");
        mKeyTypeMap.put("順列", KEYTYPE.BINARY);      //  nPr
        mKeyHelpMap.put("順列", "順列(nPr)を計算する x 順列 y");   //  nPr
        mKeyTypeMap.put("組合せ", KEYTYPE.BINARY);     //  nCr
        mKeyHelpMap.put("組合せ", "組合せ(nCr)を計算する x 組合せ y");    //  nCr
        mKeyTypeMap.put("diffYears", KEYTYPE.BINARY);
        mKeyTypeMap.put("diffDays", KEYTYPE.BINARY);
        //  操作キー
        mKeyTypeMap.put("AC", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("AC", "すべてのデータをクリアする");
        mKeyTypeMap.put("CE", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("CE", "入力データをクリアする");
        mKeyTypeMap.put("x⇔y", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("x⇔y", "入力したxとyを入れ替える");
        mKeyTypeMap.put("PUSH", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("PUSH", "計算結果をスタック(一時保存)する");
        mKeyTypeMap.put("POP", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("POP", "スタック(一時保存)したデータを呼び出す");
        mKeyTypeMap.put("MODE", KEYTYPE.OPERATOR);
        mKeyTypeMap.put("shift", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("shift", "ファンクションキーを切り替える");
        mKeyTypeMap.put("変換", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("変換", "データの変換メニューを出す");
        mKeyTypeMap.put("統計", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("統計", "ファンクションキーを統計計算用に切り替える");
        mKeyTypeMap.put("年月日", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("年月日", "日付に関するの計算メニュー");
        mKeyTypeMap.put("定数", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("定数", "単位変換定数のメニュー");
        mKeyTypeMap.put("場所", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("場所", "日の出、日没を計算するための場所の設定メニュー(起動時は東京)");
        mKeyTypeMap.put("履歴", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("履歴", "計算結果の履歴 (未サポート)");
        mKeyTypeMap.put("3Dグラフ", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("3Dグラフ", "関数から三次元グラフを表示する");
        mKeyTypeMap.put("関数グラフ", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("関数グラフ", "関数からグラフを表示する");
        mKeyTypeMap.put("計算式", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("計算式", "計算式を入力して計算する画面への切り替え");
        //  統計計算用
        mKeyTypeMap.put("data", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("data", "統計データの格納");
        mKeyTypeMap.put("x,y", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("x,y", "2元データの格納(x[x,y]y[data])");
        mKeyTypeMap.put("list", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("list", "格納データのリスト表示と編集");
        mKeyTypeMap.put("edit", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("edit", "統計デーを最終データを変更する");
        mKeyTypeMap.put("del", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("del", "統計デーを最終データを削除する");
        mKeyTypeMap.put("clear", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("clear", "統計デーをクリアする");
        mKeyTypeMap.put("save", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("save", "統計データの保存");
        mKeyTypeMap.put("load", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("load", "統計データの読出し");
        mKeyTypeMap.put("グラフ", KEYTYPE.OPERATOR);
        mKeyHelpMap.put("グラフ", "2元データのグラフ表示");
        //  単項演算子
        mKeyTypeMap.put("x^2", KEYTYPE.UNARY);
        mKeyHelpMap.put("x^2", "数値を２乗する");
        mKeyTypeMap.put("x^3", KEYTYPE.UNARY);
        mKeyHelpMap.put("x^3", "数値を３乗する");
        mKeyTypeMap.put("rad", KEYTYPE.UNARY);
        mKeyHelpMap.put("rad", "度をラジアンに変換する");
        mKeyTypeMap.put("deg", KEYTYPE.UNARY);
        mKeyHelpMap.put("deg", "ラジアンを度に変換する");
        mKeyTypeMap.put("2√", KEYTYPE.UNARY);
        mKeyHelpMap.put("2√", "平方根を求める");
        mKeyTypeMap.put("3√", KEYTYPE.UNARY);
        mKeyHelpMap.put("3√", "立方根を求める");
        mKeyTypeMap.put("1/x", KEYTYPE.UNARY);
        mKeyHelpMap.put("1/x", "逆数を求める");
        mKeyTypeMap.put("%", KEYTYPE.UNARY);
        mKeyHelpMap.put("%", "% にする(1/100にする)");
        mKeyTypeMap.put("n!", KEYTYPE.UNARY);
        mKeyHelpMap.put("n!", "階乗を求める");
        mKeyTypeMap.put("sin", KEYTYPE.UNARY);
        mKeyHelpMap.put("sin", "三角関数の正弦値を求める");
        mKeyTypeMap.put("cos", KEYTYPE.UNARY);
        mKeyHelpMap.put("cos", "三角関数の余弦値を求める");
        mKeyTypeMap.put("tan", KEYTYPE.UNARY);
        mKeyHelpMap.put("tan", "三角関数の正接値を求める");
        mKeyTypeMap.put("asin", KEYTYPE.UNARY);
        mKeyHelpMap.put("asin", "三角関数の逆正弦値を求める");
        mKeyTypeMap.put("acos", KEYTYPE.UNARY);
        mKeyHelpMap.put("acos", "三角関数の逆余弦値を求める");
        mKeyTypeMap.put("atan", KEYTYPE.UNARY);
        mKeyHelpMap.put("atan", "三角関数の逆正接値を求める");
        mKeyTypeMap.put("sinh", KEYTYPE.UNARY);
        mKeyHelpMap.put("sinh", "双曲線の正弦値を求める");
        mKeyTypeMap.put("cosh", KEYTYPE.UNARY);
        mKeyHelpMap.put("cosh", "双曲線の余弦値を求める");
        mKeyTypeMap.put("tanh", KEYTYPE.UNARY);
        mKeyHelpMap.put("tanh", "双曲線の正接値を求める");
        mKeyTypeMap.put("ln", KEYTYPE.UNARY);
        mKeyHelpMap.put("ln", "自然対数を求める");
        mKeyTypeMap.put("log", KEYTYPE.UNARY);
        mKeyHelpMap.put("log", "10の対数を求める");
        mKeyTypeMap.put("round", KEYTYPE.UNARY);
        mKeyHelpMap.put("round", "四捨五入する");
        mKeyTypeMap.put("ceil", KEYTYPE.UNARY);
        mKeyHelpMap.put("ceil", "切り上げる");
        mKeyTypeMap.put("floor", KEYTYPE.UNARY);
        mKeyHelpMap.put("floor", "切り捨てる");
        mKeyTypeMap.put("abs", KEYTYPE.UNARY);
        mKeyHelpMap.put("abs", "絶対値");
        mKeyTypeMap.put("rint", KEYTYPE.UNARY);
        mKeyHelpMap.put("rint", "浮動小数点の整数部だけにする");

        mKeyTypeMap.put("deg2dms", KEYTYPE.UNARY);
        mKeyTypeMap.put("dms2deg", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2JulianDay", KEYTYPE.UNARY);
        mKeyTypeMap.put("julianDay2DateYear", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2JpnCal", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Reiwa", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Heisei", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Shyouwa", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Taisyou", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Meiji", KEYTYPE.UNARY);
        mKeyTypeMap.put("reiwa2Date", KEYTYPE.UNARY);
        mKeyTypeMap.put("heisei2Date", KEYTYPE.UNARY);
        mKeyTypeMap.put("shyouwa2Date", KEYTYPE.UNARY);
        mKeyTypeMap.put("taisyou2Date", KEYTYPE.UNARY);
        mKeyTypeMap.put("meiji2Date", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Kyureki", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2Rokuyo", KEYTYPE.UNARY);
        mKeyTypeMap.put("date2JyukanJyunishi", KEYTYPE.UNARY);
        mKeyTypeMap.put("getDayWeek", KEYTYPE.UNARY);
        mKeyTypeMap.put("moonAge", KEYTYPE.UNARY);
        mKeyTypeMap.put("getSunRise", KEYTYPE.UNARY);
        mKeyTypeMap.put("getSunSet", KEYTYPE.UNARY);
        //  統計計算
        mKeyTypeMap.put("n", KEYTYPE.UNARY);
        mKeyHelpMap.put("n", "データ数(2元データの場合は対の和");
        mKeyTypeMap.put("xm", KEYTYPE.UNARY);
        mKeyHelpMap.put("xm", "格納データのxの平均 1/n*Σxi");
        mKeyTypeMap.put("ym", KEYTYPE.UNARY);
        mKeyHelpMap.put("ym", "格納データのyの平均 1/n*Σyi");
        mKeyTypeMap.put("σx", KEYTYPE.UNARY);
        mKeyHelpMap.put("σx", "標準偏差 sqrt(1/n*Σ(xi-xm)^2)");
        mKeyTypeMap.put("σy", KEYTYPE.UNARY);
        mKeyHelpMap.put("σy", "標準偏差 sqrt(1/n*Σ(yi-ym)^2)");
        mKeyTypeMap.put("σxy", KEYTYPE.UNARY);
        mKeyHelpMap.put("σxy", "共分散　1/n*Σ(xi-xm)(yi-ym)");
        mKeyTypeMap.put("ρ", KEYTYPE.UNARY);
        mKeyHelpMap.put("ρ", "相関係数 σxy/σx・σy (-1≦ρ≦1");
        mKeyTypeMap.put("a", KEYTYPE.UNARY);
        mKeyHelpMap.put("a", "回帰分析の傾き y = ax + b");
        mKeyTypeMap.put("b", KEYTYPE.UNARY);
        mKeyHelpMap.put("b", "回帰分析の切片 y = ax + b");
        mKeyTypeMap.put("Σx", KEYTYPE.UNARY);
        mKeyHelpMap.put("Σx", "xデータの総和");
        mKeyTypeMap.put("Σy", KEYTYPE.UNARY);
        mKeyHelpMap.put("Σy", "yデータの総和");
        mKeyTypeMap.put("Σx^2", KEYTYPE.UNARY);
        mKeyHelpMap.put("Σx^2", "xデータの二乗の総和");
        mKeyTypeMap.put("Σy^2", KEYTYPE.UNARY);
        mKeyHelpMap.put("Σy^2", "yデータの二乗の総和");
        mKeyTypeMap.put("Σxy", KEYTYPE.UNARY);
        mKeyHelpMap.put("Σxy", "x*yのデータの総和");

        //  定数
        mKeyTypeMap.put("PI", KEYTYPE.CONSTANT);
        mKeyHelpMap.put("PI", "円周率");
        mKeyTypeMap.put("E", KEYTYPE.CONSTANT);
        mKeyHelpMap.put("E", "ネピア数(自然対数の底)");
        mKeyTypeMap.put("today", KEYTYPE.CONSTANT);
    }

    /**
     * 通常のファンクションキーの設定
     */
    private void setFunctionKey() {
        mShiftMode = false;
        mStatisticsMode = false;
        mKeyA0.setText("shift");
        mKeyA1.setText("変換");
        mKeyA2.setText("統計");
        mKeyA2.setBackgroundColor(Color.rgb(0xd7, 0xd7, 0xd7));
        mKeyA3.setText("3Dグラフ");
        mKeyA3.setBackgroundColor(Color.rgb(0xe0,0xe0,0x00));
        mKeyA4.setText("関数グラフ");
        mKeyA4.setBackgroundColor(Color.rgb(0xe0,0xe0,0x00));
        mKeyA5.setText("計算式");
        mKeyA5.setBackgroundColor(Color.rgb(0xe0,0xe0,0x00));
        mKeyE0.setText("   ");      //  dummy
        mKeyE1.setText("   ");
        mKeyE2.setText("年月日");
        mKeyE3.setText("定数");
        mKeyE4.setText("履歴");      //
        mKeyE5.setText("場所");
        mKeyB0.setText("sin");
        mKeyB1.setText("cos");
        mKeyB2.setText("tan");
        mKeyB3.setText("ln");
        mKeyB4.setText("log");
        mKeyB5.setText("PI");
        mKeyC0.setText("x^y");
        mKeyC1.setText("2√");
        mKeyC2.setText("3√");
        mKeyC3.setText("1/x");
        mKeyC4.setText("%");
        mKeyC5.setText("n!");       //  階乗
        mKeyD0.setText("PUSH");
        mKeyD1.setText("POP");
        mKeyD2.setText("x⇔y");
        mKeyD3.setText("exp");
        mKeyD4.setText("rad");      //  deg → rad
        mKeyD5.setText("mod");
    }

    /**
     * Shiftキーを押した時のファンクションキーの設定
     */
    private void setFunctionShiftKey() {
        mShiftMode = true;
        if (mStatisticsMode) {
            mKeyD0.setText("edit");
            mKeyD1.setText("clear");
            mKeyD2.setText("load");
            mKeyD3.setText("save");
        } else {
            mKeyA0.setText("shift");
            mKeyA1.setText("   ");
            mKeyA2.setText("   ");
            mKeyA2.setBackgroundColor(Color.rgb(0xd7, 0xd7, 0xd7));
            mKeyA3.setText("   ");
            mKeyA3.setBackgroundColor(Color.rgb(0xd7, 0xd7, 0xd7));
            mKeyA4.setText("   ");
            mKeyA4.setBackgroundColor(Color.rgb(0xd7, 0xd7, 0xd7));
            mKeyA5.setText("計算式");
            mKeyA5.setBackgroundColor(Color.rgb(0xd7,0xd7,0xd7));
            mKeyE0.setText("round");      //  dummy
            mKeyE1.setText("ceil");
            mKeyE2.setText("floor");
            mKeyE3.setText("rint");
            mKeyE4.setText("   ");      //
            mKeyE5.setText("   ");
            mKeyB0.setText("asin");
            mKeyB1.setText("acos");
            mKeyB2.setText("atan");
            mKeyB3.setText("e^x");
            mKeyB4.setText("10^x");
            mKeyB5.setText("E");
            mKeyC0.setText("x^1/y");
            mKeyC1.setText("x^2");
            mKeyC2.setText("x^3");
            mKeyC3.setText("1/x");
            mKeyC4.setText("順列");      //  順列 nPr
            mKeyC5.setText("組合せ");    //  組合せ nCr
            mKeyD0.setText("PUSH");
            mKeyD1.setText("POP");
            mKeyD2.setText("x⇔y");
            mKeyD3.setText("exp");      //  xxEyy
            mKeyD4.setText("deg");      //  rad → deg
            mKeyD5.setText("mod");
        }
    }

    private void setFunctionStatisticsKey() {
        mShiftMode = false;
        mStatisticsMode = true;
        mKeyA0.setText("shift");
        mKeyA1.setText("");
        mKeyA2.setText("統計");
        mKeyA2.setBackgroundColor(Color.rgb(0xff,0xf1,0x76));
        mKeyA3.setText("");
        mKeyA3.setBackgroundColor(Color.rgb(0xd7,0xd7,0xd7));
        mKeyA4.setText("");
        mKeyA4.setBackgroundColor(Color.rgb(0xd7,0xd7,0xd7));
        mKeyA5.setText("計算式");
        mKeyA5.setBackgroundColor(Color.rgb(0xd7,0xd7,0xd7));
        mKeyE0.setText("   ");      //  dummy
        mKeyE1.setText("   ");
        mKeyE2.setText("   ");
        mKeyE3.setText("   ");
        mKeyE4.setText("   ");      //
        mKeyE5.setText("   ");
        mKeyB0.setText("Σx");
        mKeyB1.setText("Σy");
        mKeyB2.setText("Σx^2");
        mKeyB3.setText("Σy^2");
        mKeyB4.setText("Σxy");
        mKeyB5.setText("グラフ");
        mKeyC0.setText("σx");
        mKeyC1.setText("σy");
        mKeyC2.setText("σxy");
        mKeyC3.setText("ρ");
        mKeyC4.setText("a");
        mKeyC5.setText("b");
        mKeyD0.setText("data");
        mKeyD1.setText("x,y");
        mKeyD2.setText("del");
        mKeyD3.setText("n");
        mKeyD4.setText("xm");
        mKeyD5.setText("ym");
    }

    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//	画面を縦に固定する
        Integer winWidth = 	getWindowsWidth();
        this.setTitle("でんたく３号");

        //  表示部
        mDisplayPanel = (TextView)findViewById(R.id.displayPanel);  //  計算結果表示
        mDisplayPanel.setText("");
        mSubDispayPanel = (TextView)findViewById(R.id.textView);    //  入力データ
        mSubDispayPanel.setText("");

        //  数値キーの設定
        mKey0 = (Button)findViewById(R.id.bt_0);
        mKey1 = (Button)findViewById(R.id.bt_1);
        mKey2 = (Button)findViewById(R.id.bt_2);
        mKey3 = (Button)findViewById(R.id.bt_3);
        mKey4 = (Button)findViewById(R.id.bt_4);
        mKey5 = (Button)findViewById(R.id.bt_5);
        mKey6 = (Button)findViewById(R.id.bt_6);
        mKey7 = (Button)findViewById(R.id.bt_7);
        mKey8 = (Button)findViewById(R.id.bt_8);
        mKey9 = (Button)findViewById(R.id.bt_9);
        mKeyDot = (Button)findViewById(R.id.bt_dot);
        mKeyPlMi = (Button)findViewById(R.id.bt_pm);
        mKeyBS = (Button)findViewById(R.id.bt_bs);
        mKeyEQ = (Button)findViewById(R.id.bt_eq);

        mKeyAdd = (Button)findViewById(R.id.bt_add);
        mKeySub = (Button)findViewById(R.id.bt_sub);
        mKeyMul = (Button)findViewById(R.id.bt_mul);
        mKeyDiv = (Button)findViewById(R.id.bt_div);
        mKeyCE = (Button)findViewById(R.id.bt_ce);
        mKeyAC = (Button)findViewById(R.id.bt_ac);

        //  ファンクションキーの設定
        mKeyE0 = (Button)findViewById(R.id.button20);
        mKeyE1 = (Button)findViewById(R.id.button21);
        mKeyE2 = (Button)findViewById(R.id.button22);
        mKeyE3 = (Button)findViewById(R.id.button23);
        mKeyE4 = (Button)findViewById(R.id.button24);
        mKeyE5 = (Button)findViewById(R.id.button25);

        mKeyD0 = (Button)findViewById(R.id.button14);
        mKeyD1 = (Button)findViewById(R.id.button15);
        mKeyD2 = (Button)findViewById(R.id.button16);
        mKeyD3 = (Button)findViewById(R.id.button17);
        mKeyD4 = (Button)findViewById(R.id.button18);
        mKeyD5 = (Button)findViewById(R.id.button19);

        mKeyC0 = (Button)findViewById(R.id.button7);
        mKeyC1 = (Button)findViewById(R.id.button8);
        mKeyC2 = (Button)findViewById(R.id.button9);
        mKeyC3 = (Button)findViewById(R.id.button10);
        mKeyC4 = (Button)findViewById(R.id.button12);
        mKeyC5 = (Button)findViewById(R.id.button13);

        mKeyB0 = (Button)findViewById(R.id.button);
        mKeyB1 = (Button)findViewById(R.id.button2);
        mKeyB2 = (Button)findViewById(R.id.button3);
        mKeyB3 = (Button)findViewById(R.id.button4);
        mKeyB4 = (Button)findViewById(R.id.button5);
        mKeyB5 = (Button)findViewById(R.id.button6);

        mKeyA0 = (Button)findViewById(R.id.bt_shift);
        mKeyA1 = (Button)findViewById(R.id.bt_hyp);
        mKeyA2 = (Button)findViewById(R.id.bt_stat);
        mKeyA3 = (Button)findViewById(R.id.bt_coef);
        mKeyA4 = (Button)findViewById(R.id.bt_history);
        mKeyA5 = (Button)findViewById(R.id.bt_mode);

        mKey0.setOnClickListener(this);
        mKey1.setOnClickListener(this);
        mKey2.setOnClickListener(this);
        mKey3.setOnClickListener(this);
        mKey4.setOnClickListener(this);
        mKey5.setOnClickListener(this);
        mKey6.setOnClickListener(this);
        mKey7.setOnClickListener(this);
        mKey8.setOnClickListener(this);
        mKey9.setOnClickListener(this);
        mKeyDot.setOnClickListener(this);
        mKeyPlMi.setOnClickListener(this);
        mKeyBS.setOnClickListener(this);
        mKeyEQ.setOnClickListener(this);

        mKeyAdd.setOnClickListener(this);
        mKeySub.setOnClickListener(this);
        mKeyMul.setOnClickListener(this);
        mKeyDiv.setOnClickListener(this);
        mKeyCE.setOnClickListener(this);
        mKeyAC.setOnClickListener(this);

        mKeyA0.setOnClickListener(this);
        mKeyA1.setOnClickListener(this);
        mKeyA2.setOnClickListener(this);
        mKeyA3.setOnClickListener(this);
        mKeyA4.setOnClickListener(this);
        mKeyA5.setOnClickListener(this);

        mKeyB0.setOnClickListener(this);
        mKeyB1.setOnClickListener(this);
        mKeyB2.setOnClickListener(this);
        mKeyB3.setOnClickListener(this);
        mKeyB4.setOnClickListener(this);
        mKeyB5.setOnClickListener(this);

        mKeyC0.setOnClickListener(this);
        mKeyC1.setOnClickListener(this);
        mKeyC2.setOnClickListener(this);
        mKeyC3.setOnClickListener(this);
        mKeyC4.setOnClickListener(this);
        mKeyC5.setOnClickListener(this);

        mKeyD0.setOnClickListener(this);
        mKeyD1.setOnClickListener(this);
        mKeyD2.setOnClickListener(this);
        mKeyD3.setOnClickListener(this);
        mKeyD4.setOnClickListener(this);
        mKeyD5.setOnClickListener(this);

        mKeyE0.setOnClickListener(this);
        mKeyE1.setOnClickListener(this);
        mKeyE2.setOnClickListener(this);
        mKeyE3.setOnClickListener(this);
        mKeyE4.setOnClickListener(this);
        mKeyE5.setOnClickListener(this);


        mKey0.setOnLongClickListener(this);
        mKey1.setOnLongClickListener(this);
        mKey2.setOnLongClickListener(this);
        mKey3.setOnLongClickListener(this);
        mKey4.setOnLongClickListener(this);
        mKey5.setOnLongClickListener(this);
        mKey6.setOnLongClickListener(this);
        mKey7.setOnLongClickListener(this);
        mKey8.setOnLongClickListener(this);
        mKey9.setOnLongClickListener(this);
        mKeyDot.setOnLongClickListener(this);
        mKeyPlMi.setOnLongClickListener(this);
        mKeyBS.setOnLongClickListener(this);
        mKeyEQ.setOnLongClickListener(this);

        mKeyAdd.setOnLongClickListener(this);
        mKeySub.setOnLongClickListener(this);
        mKeyMul.setOnLongClickListener(this);
        mKeyDiv.setOnLongClickListener(this);
        mKeyCE.setOnLongClickListener(this);
        mKeyAC.setOnLongClickListener(this);

        mKeyA0.setOnLongClickListener(this);
        mKeyA1.setOnLongClickListener(this);
        mKeyA2.setOnLongClickListener(this);
        mKeyA3.setOnLongClickListener(this);
        mKeyA4.setOnLongClickListener(this);
        mKeyA5.setOnLongClickListener(this);

        mKeyB0.setOnLongClickListener(this);
        mKeyB1.setOnLongClickListener(this);
        mKeyB2.setOnLongClickListener(this);
        mKeyB3.setOnLongClickListener(this);
        mKeyB4.setOnLongClickListener(this);
        mKeyB5.setOnLongClickListener(this);

        mKeyC0.setOnLongClickListener(this);
        mKeyC1.setOnLongClickListener(this);
        mKeyC2.setOnLongClickListener(this);
        mKeyC3.setOnLongClickListener(this);
        mKeyC4.setOnLongClickListener(this);
        mKeyC5.setOnLongClickListener(this);

        mKeyD0.setOnLongClickListener(this);
        mKeyD1.setOnLongClickListener(this);
        mKeyD2.setOnLongClickListener(this);
        mKeyD3.setOnLongClickListener(this);
        mKeyD4.setOnLongClickListener(this);
        mKeyD5.setOnLongClickListener(this);

        mKeyE0.setOnLongClickListener(this);
        mKeyE1.setOnLongClickListener(this);
        mKeyE2.setOnLongClickListener(this);
        mKeyE3.setOnLongClickListener(this);
        mKeyE4.setOnLongClickListener(this);
        mKeyE5.setOnLongClickListener(this);

//        mKeyE0.setVisibility(View.INVISIBLE);
//        mKeyE1.setVisibility(View.INVISIBLE);
//        mKeyE2.setVisibility(View.INVISIBLE);
//        mKeyE3.setVisibility(View.INVISIBLE);
//        mKeyE4.setVisibility(View.INVISIBLE);
//        mKeyE5.setVisibility(View.INVISIBLE);
        //  ボタンの文字表示で大文字/小文字を区別する
        mKeyA0.setAllCaps(false);
        mKeyA1.setAllCaps(false);
        mKeyA2.setAllCaps(false);
        mKeyA3.setAllCaps(false);
        mKeyA4.setAllCaps(false);
        mKeyA5.setAllCaps(false);
        mKeyE0.setAllCaps(false);
        mKeyE1.setAllCaps(false);
        mKeyE2.setAllCaps(false);
        mKeyE3.setAllCaps(false);
        mKeyE4.setAllCaps(false);
        mKeyE5.setAllCaps(false);
        mKeyB0.setAllCaps(false);
        mKeyB1.setAllCaps(false);
        mKeyB2.setAllCaps(false);
        mKeyB3.setAllCaps(false);
        mKeyB4.setAllCaps(false);
        mKeyB5.setAllCaps(false);
        mKeyC0.setAllCaps(false);
        mKeyC1.setAllCaps(false);
        mKeyC2.setAllCaps(false);
        mKeyC3.setAllCaps(false);
        mKeyC4.setAllCaps(false);
        mKeyC5.setAllCaps(false);
        mKeyD0.setAllCaps(false);
        mKeyD1.setAllCaps(false);
        mKeyD2.setAllCaps(false);
        mKeyD3.setAllCaps(false);
        mKeyD4.setAllCaps(false);
        mKeyD5.setAllCaps(false);
        mKeyE0.setAllCaps(false);
        mKeyE1.setAllCaps(false);
        mKeyE2.setAllCaps(false);
        mKeyE3.setAllCaps(false);
        mKeyE4.setAllCaps(false);
        mKeyE5.setAllCaps(false);
    }

    /**
     * データファイル保存ディレクトリ
     */
    private void setSaveDirectory() {
        //	データ保存ディレクトリ
        mSaveDirectory = Environment.getExternalStorageDirectory().toString()+"/calc2";
        Log.d(TAG,"setSaveDirectory:"+mSaveDirectory);
        if (!ylib.existsFile(mSaveDirectory) && !ylib.isDirectory(mSaveDirectory)) {
            if (!ylib.mkdir(mSaveDirectory)) {
                Toast.makeText(this, "ディレクトリが作成できません\n"+mSaveDirectory, Toast.LENGTH_LONG).show();
                mSaveDirectory = Environment.getExternalStorageDirectory().toString();
            }
        }
        mSaveStatisticsDirectory = mSaveDirectory + "/StatisticsData";
        if (!ylib.existsFile(mSaveStatisticsDirectory) && !ylib.isDirectory(mSaveStatisticsDirectory)) {
            if (!ylib.mkdir(mSaveStatisticsDirectory)) {
                Toast.makeText(this, "ディレクトリが作成できません\n"+mSaveStatisticsDirectory, Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     *	ウィンドウ幅を取得
     * @return		ウィンドウ幅
     */
    public int getWindowsWidth() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        return display.getWidth();
    }
}
