package jp.co.yoshida.katsushige.calc2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.yoshida.katsushige.mylib.YLib;

/**
 *  計算式をグラフ表示する
 *
 *  与えられた関数でグラフの描画を行う。
 *  1.一般的な一次元の方程式(直交座標) y = f(x)
 *  2.媒介変数(パラメトリック)を使った式 x = f(t); y = g(t)
 *  3.極方程式(極座標) r = f(t)
 *  記述例: $複数関数;f1([x]);f2([x]);f3([x])
 *          $変数指定;f([a],[x]);[a]=3
 *          $パラメトリック;f([t]);g([t])
 *          $極方程式;f[t])
 *  画面
 *      関数式(f(x)の選択・入力、x または t の設定範囲と分割数、関数の種類
 *      y の表示範囲、yの範囲を自動設定、、アスペクト比の固定(1:1)の設定
 *      関数、実行、削除ボタン
 *  関数式
 *      関数式は;で区切れば複数設定可能
 *      関数式の先頭に$をつけるとタイトルまたはコメントとして扱われ計算式から除外
 *      関数式の中で[]で囲まれたものは変数として扱われる[x]または[t]はグラフのx軸の値となる
 *      その他の[]は式の中で変数の値を設定できる。式の中に=が入っていると式ではなく代入文として扱う
 *      例 変数代入: $タイトル;sin([x])*[a];[a]=0.2    ⇒ y=sin(x)*0.2と同じ
 *      　 複数の式: $タイトル;[x]^2+[x];[x]^2;[x]     ⇒ y=x^2+xとy=x^2、y=xの3つの関数をグラフ表示する
 *  種別
 *  　　y=f(x) : 一般式 [x]を変数としてyを求めxyグラフ表示する
 *  　　x=f(t);y=g(t) : [t]を媒介変数とする2つの式からxyグラフ表示をする
 *  　　　　　　　　　　2つの式をセットで扱い複数の式にも対応している
 *  　　r=f(t) : tを角度(Radian)、rを半径とする極座標からグラフを表示する
 */
public class FuncGraphActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "FuncGraphActivity";

    private TextView mTvFunctionTitle;
    private TextView mTvRangTitle;
    private TextView mTvRangYTitle;
    private EditText mEdFunction;
    private EditText mEdRangeMin;
    private EditText mEdRangeMax;
    private EditText mEdDivideCount;
    private EditText mEdRangeYMin;
    private EditText mEdRangeYMax;
    private RadioButton mRbNormalFunction;
    private RadioButton mRbPametricFunction;
    private RadioButton mRbPolarFunction;
    private RadioGroup mRgFuncType;
    private CheckBox mCbAutoHeight;
    private CheckBox mCbAspectFix;
    private Button mBtExecute;
    private Button mBtFunction;
    private Button mBtRemove;
    private LinearLayout mLinearLayout;
    private FuncGraphView mFuncGraphView;

    private YLib mYlib;

    private String mSaveDirectory;
    private String mDataFileName = "FuncPlot.csv";
    private String mDataFilePath;
    private Map<String, String[]> mFuncData;        //  関数式(タイトル,関数式、パラメータ)
    private ArrayList<PointF[]> mPlotDatas;         //  描画用座標データ
    private double mXmin = 0;
    private double mXmax = 100;
    private double mYmin = 0;
    private double mYmax = 100;
    private double mDivCount = 100;
    private String[] mFunctionType = { "Normal", "Parametric", "Polar" };
    private String[] mFunctionMenu = {
            "[x] 引数","[t] 媒介変数と極方程式の引数","[@] sum/productで使う引数",
            "sin(x) 正弦","cos(x) 余弦","tan(x) 正接","asin(x) 逆正弦",
            "acos(x) 逆余弦","atan(x) 逆正接","atan2(x,y) 逆正接",
            "sinh(x) 双曲線正弦","cosh(x) 双曲線余弦","tanh(x) 双曲線正接",
            "pow(x,y) 累乗","sqrt(x) 平方根","exp(x) eの累乗","ln(x) eの自然対数",
            "log(x) 10の対数","log(x,y) 底を指定した対数",
            "abs(x) 絶対値","ceil(x) 切上げ","floor(x) 切捨て","round(x) 四捨五入",
            "rint(x) 整数化(整数部)","sign(x) 符号化(1/0/-1)",
            "max(x,y) 大きい方","min(x,y) 小さい方","combi(n,r) 組合せnCr","permu(n,r) 順列nPr",
            "RAD(x) 度をRadianに変換","DEG() Radianを度に変換",
            "PI 円周率","E 自然対数の底e","fact(n) 階乗n!","fib(n) フィボナッチ数列",
            "equals(x,y) 比較 x==y ⇒ 1,x!=y ⇒ 0",
            "compare(x,y) 比較 x > y ⇒ 1,x==y ⇒ 0,x<y ⇒ -1",
            "sum(f([@]),n,k) 級数の和 計算式f([@])にnからkまでを入れた合計",
            "product(f([@]),n,k) 級数の積 計算式f([@])にnからkまでを入れた積",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_func_graph);

        mYlib = new YLib();

        init();

        //  データ保存パスの設定
        mSaveDirectory = mYlib.setSaveDirectory(getPackageName().substring(getPackageName().lastIndexOf('.')+1));
        mDataFilePath = mSaveDirectory+"/"+mDataFileName;
        Log.d(TAG,"onCreate: "+mSaveDirectory);

        //  初期設定
        mEdFunction.setText("sqrt([x])");
        mEdRangeMin.setText("0");
        mEdRangeMax.setText("100");
        mEdDivideCount.setText("100");
        mEdRangeYMin.setText("0");
        mEdRangeYMax.setText("100");
        mCbAutoHeight.setChecked(false);
        mRgFuncType.check(mRbNormalFunction.getId());

        //  関数式データをファイルから読み出して登録する
        mFuncData = new HashMap<String,String[]>();
        if (mYlib.checkStragePermission(this))
            loadFuncData(mDataFilePath);
    }

    @Override
    protected void onDestroy() {
        //  関数式データをファイルに保存
        saveFuncData(mDataFilePath);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtExecute.getId()) {
            mBtExecute.setEnabled(false);
            Toast.makeText(this,"計算開始",Toast.LENGTH_SHORT).show();
            //  関数式からグラフデータを作成
            setParametor();                         //  データ範囲や分割数をグローバルに設定する
            if (mRbNormalFunction.isChecked()) {
                makeFunctionData(mEdFunction.getText().toString());
            } else if (mRbPametricFunction.isChecked()) {
                makeParametricData(mEdFunction.getText().toString());
            } else if (mRbPolarFunction.isChecked()) {
                makePollarData(mEdFunction.getText().toString());
            } else {
                return;
            }
            dataRegist();                           //  関数式を登録する
            dispGraph();                            //  データをグラフ表示する
            mBtExecute.setEnabled(true);
        } else if (view.getId() == mBtFunction.getId()) {
            //  関数の選択メニューの表示して入力
            mYlib.setMenuDialog(this, "関数追加", mFunctionMenu, iFunctionSet);
        } else if (view.getId() == mBtRemove.getId()) {
            //  現在表示されている関数をリストから削除する
            String key = getFunctionKey(mEdFunction.getText().toString().trim(),getFunctionType());
            if (mFuncData.containsKey(key)) {
                mFuncData.remove(key);
                mEdFunction.setText("");
            }
        }
    }


    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == mEdFunction.getId()) {
            //  関数式の選択メニューの表示
            String[] funcMenu = new String[mFuncData.size()];
            int n = 0;
            for (String key : mFuncData.keySet())
                funcMenu[n++] = key;
            Arrays.sort(funcMenu);
            mYlib.setMenuDialog(this, "関数式", funcMenu, iOperation);
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        dispGraph();                            //  データをグラフ表示する
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (mRbNormalFunction.isChecked()) {
        } else if (mRbPametricFunction.isChecked()) {
        } else if (mRbPolarFunction.isChecked()) {
        }
    }

    /**
     * 作成された座標データを標示する
     */
    private void dispGraph() {
        double ymin = mYmin;
        double ymax = mYmax;
        if (!mCbAutoHeight.isChecked()) {
            //  高さ範囲が自動でない場合設定値を使う
            if ((mEdRangeYMin.getText()==null || mEdRangeYMax.getText()==null) ||
                    (mEdRangeYMin.getText().length()==0 || mEdRangeYMax.getText().length()==0)) {
                Toast.makeText(this,"範囲データが設定されていません",Toast.LENGTH_LONG).show();
                return;
            }
            YCalc calc = new YCalc();
            ymin = calc.expression(mEdRangeYMin.getText().toString());
            if (calc.mError) {
                Toast.makeText(this,calc.mErrorMsg,Toast.LENGTH_LONG).show();
                return;
            }
            ymax = calc.expression(mEdRangeYMax.getText().toString());
            if (calc.mError) {
                Toast.makeText(this,calc.mErrorMsg,Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (mXmax <= mXmin || ymax <= ymin) {
            Toast.makeText(this,"範囲の設定で表示領域が取られていません",Toast.LENGTH_LONG).show();
            return;
        }
        //  アスペクト比固定のチェックボックスの変更を反映してデータをグラフ表示する
        mFuncGraphView.setDatas(mPlotDatas,mXmin,ymin,mXmax,ymax, mCbAspectFix.isChecked());
        mFuncGraphView.dispGraph();
    }

    /**
     * 関数式の選択メニューに対して式とパラメータを設定する関数インターフェス
     */
    Consumer<String> iOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            if (mFuncData.containsKey(s)) {
                String[]data = mFuncData.get(s);
                if (3 < data.length) {
                    mEdFunction.setText(data[0]);
                    mEdRangeMin.setText(data[1]);
                    mEdRangeMax.setText(data[2]);
                    mEdDivideCount.setText(data[3]);
                }
                if (4 < data.length)
                    setFunctionType(data[4]);
                if (7 < data.length) {
                    mEdRangeYMin.setText(data[5]);
                    mEdRangeYMax.setText(data[6]);
                    if (data[7]!=null)
                        mCbAutoHeight.setChecked(0<data[7].length()?Boolean.valueOf(data[7]):true);
                    else
                        mCbAutoHeight.setChecked(true);
                }
            }
        }
    };

    /**
     * 関数入力のエディットボックスに選択した関数を挿入する関数インターフェス
     */
    Consumer<String> iFunctionSet = new Consumer<String>() {
        @Override
        public void accept(String s) {
            String buf = mEdFunction.getText().toString();
            //  カーソル位置に文字を挿入
            buf = buf.substring(0, mEdFunction.getSelectionStart())+
                    s.substring(0, s.indexOf(' '))+
                    buf.substring(mEdFunction.getSelectionEnd());
            mEdFunction.setText(buf);
            //  カーソルを挿入した文字の後ろに移動する
            mEdFunction.setSelection(buf.indexOf(s.substring(0, s.indexOf(' ')))+
                    s.substring(0, s.indexOf(' ')).length());
        }
    };

    /**
     * 関数のパラメータ(範囲、分割数)を変数に設定する
     */
    private void setParametor() {
        YCalc calc = new YCalc();
        mXmin = calc.expression(mEdRangeMin.getText().toString());
        mXmax = calc.expression(mEdRangeMax.getText().toString());
        mDivCount = calc.expression(mEdDivideCount.getText().toString());
        mYmin = calc.expression(mEdRangeYMin.getText().toString());
        mYmax = calc.expression(mEdRangeYMax.getText().toString());
    }

    /**
     * 通常の方程式からグラフデータを求める(y=f(x)
     * @param function      関数式
     */
    private void makeFunctionData(String function) {
        Log.d(TAG,"makeFunctionData: "+ function+" "+mXmin+" "+mXmax+" "+mDivCount);
        String[] buf = function.split(";");
        String errorMsg = "";
        YCalc calc = new YCalc();
        mPlotDatas = new ArrayList<PointF[]>();
        double xStep = (mXmax - mXmin) / mDivCount;
        boolean firstPoint = true;
        ArrayList<String[]> argValues = getArgValue(function);

        for (int i = 0; i < buf.length; i++) {
            if (0 < buf[i].length() && !(buf[i].charAt(0)=='$') && buf[i].indexOf('=') < 0) {
                ArrayList<PointF> plotDataList = new ArrayList<PointF>();
                calc.setExpression(buf[i]);
                for (double x = mXmin; x < mXmax+xStep; x += xStep) {
                    if (mXmax < x)
                        x = mXmax;
                    for (String[] val : argValues) {
                        calc.setArgvalue(val[0], "("+val[1]+")");
                    }
                    calc.setArgvalue("[x]", "("+x+")");
                    double y = calc.calculate();
                    if (!calc.mError) {
                        PointF point = new PointF((float) x, (float) y);
//                        Log.d(TAG, "makeFunctionData: " + x + " " + y);
                        if (!Double.isInfinite(y) && !Double.isNaN(y)) {
                            plotDataList.add(point);
                            if (firstPoint) {
                                mYmin = mYmax = y;
                                firstPoint = false;
                            } else {
                                mYmin = Math.min(mYmin, y);
                                mYmax = Math.max(mYmax, y);
                            }
                        } else {
                            errorMsg = "値不定か無限大が存在します";
                        }
                    } else {
                        errorMsg = calc.mErrorMsg;
                    }
                }
                if (0 < errorMsg.length())
                    Toast.makeText(this,errorMsg, Toast.LENGTH_LONG).show();
                if (0 < plotDataList.size()) {
                    PointF[] plotdatas = plotDataList.toArray(new PointF[plotDataList.size()]);
                    mPlotDatas.add(plotdatas);
                }
            }
        }
    }

    /**
     * 媒介変数(パラメトリック)を用いた式のグラフデータを求める(x=f(t);y=g(t))
     * @param function      関数式
     * @return              結果の可否
     */
    private void makeParametricData(String function) {
        Log.d(TAG,"makeParametricData: "+ function+" "+mXmin+" "+mXmax+" "+mDivCount);
        String[] buf = function.split(";");
        String errorMsg = "";
        YCalc calcX = new YCalc();
        YCalc calcY = new YCalc();
        mPlotDatas = new ArrayList<PointF[]>();
        double tStep = (mXmax - mXmin) / mDivCount;
        boolean firstPoint = true;
        ArrayList<String[]> argValues = getArgValue(function);

        int n = 0;
        int fx = -1, fy = -1;
        while (n < buf.length) {
            int i;
            //  x = f(t) を検索
            for (i = n; i < buf.length; i++) {
                if (0 < buf[i].length() && !(buf[i].charAt(0)=='$') && buf[i].indexOf('=') < 0) {
                    fx = i;
                    break;
                }
            }
            if (fx < n)
                return ;
            n = i + 1;
            //  y = g(t) を検索
            for (i = n; i < buf.length; i++) {
                if (0 < buf[i].length() && !(buf[i].charAt(0)=='$') && buf[i].indexOf('=') < 0) {
                    fy = i;
                    break;
                }
            }
            if (fy < n)
                return ;
            ArrayList<PointF> plotDataList = new ArrayList<PointF>();
            calcX.setExpression(buf[fx]);
            calcY.setExpression(buf[fy]);

            double tmin = mXmin;
            double tmax = mXmax;
            for (double t = tmin; t < tmax && n <= mDivCount; t += tStep) {
                if (tmax < t)
                    t = tmax;
                for (String[] val : argValues) {
                    calcX.setArgvalue(val[0], "(" + val[1] + ")");
                    calcY.setArgvalue(val[0], "(" + val[1] + ")");
                }
                calcX.setArgvalue("[t]", "(" + t + ")");
                calcY.setArgvalue("[t]", "(" + t + ")");
                double x = calcX.calculate();
                double y = calcY.calculate();
                //  エラーデータ、無限大、
                if (!calcX.mError) {
//                    Log.d(TAG,"makeParametricData: "+x+" "+y);
                    plotDataList.add(new PointF((float)x, (float)y));
                    if (!Double.isInfinite(x) && !Double.isNaN(x) &&
                            !calcY.mError && !Double.isInfinite(y) && !Double.isNaN(y)) {
                        if (firstPoint) {
                            mXmin = mXmax = x;
                            mYmin = mYmax = y;
                            firstPoint = false;
                        } else {
                            mXmin = Math.min(mXmin, x);
                            mXmax = Math.max(mXmax, x);
                            mYmin = Math.min(mYmin, y);
                            mYmax = Math.max(mYmax, y);
                        }
                    } else {
                        errorMsg = "値不定か無限大が存在します";
                    }
                } else {
                    if (calcX.mError)
                        errorMsg = calcX.mErrorMsg;
                    else if (calcY.mError)
                        errorMsg = calcY.mErrorMsg;
                }
            }
            if (0 < errorMsg.length())
                Toast.makeText(this,errorMsg, Toast.LENGTH_LONG).show();
            if (0 < plotDataList.size()) {
                PointF[] plotdatas = plotDataList.toArray(new PointF[plotDataList.size()]);
                mPlotDatas.add(plotdatas);
                Log.d(TAG,"makeParametricData: "+mPlotDatas.size()+" "+plotdatas.length);
            }
        }
    }

    /**
     * 極方程式のグラフデータを求める(r=f(t))
     * @param function      関数式
     * @return              結果の可否
     */
    private void makePollarData(String function) {
        Log.d(TAG,"makePollarData: "+ function+" "+" "+mXmin+" "+mXmax+" "+mDivCount);
        String[] buf = function.split(";");
        String errorMsg = "";
        YCalc calc = new YCalc();
        mPlotDatas = new ArrayList<PointF[]>();
        double tStep = (mXmax - mXmin) / mDivCount;
        boolean firstPoint = true;
        ArrayList<String[]> argValues = getArgValue(function);

        for (int i = 0; i < buf.length; i++) {
            if (0 < buf[i].length() && !(buf[i].charAt(0)=='$') && buf[i].indexOf('=') < 0) {
                ArrayList<PointF> plotDataList = new ArrayList<PointF>();
                calc.setExpression(buf[i]);
                double tmin = this.mXmin;
                double tmax = this.mXmax;
                for (double t = tmin; t < tmax+tStep; t += tStep) {
                    if (tmax < t)
                        t = tmax;
                    for (String[] val : argValues) {
                        calc.setArgvalue(val[0], "("+val[1]+")");
                    }
                    calc.setArgvalue("[t]", "(" + t + ")");
                    double r = calc.calculate();
                    if (!calc.mError && !Double.isInfinite(r) && !Double.isNaN(r)) {
                        double x = r * Math.cos(t);
                        double y = r * Math.sin(t);
                        PointF data = new PointF((float) x, (float) y);
//                        Log.d(TAG, "makePollarData: " + t + " " + r);
                        plotDataList.add(data);
                        if (firstPoint) {
                            mXmin = mXmax = x;
                            mYmin = mYmax = y;
                            firstPoint = false;
                        } else {
                            mXmin = Math.min(mXmin, x);
                            mXmax = Math.max(mXmax, x);
                            mYmin = Math.min(mYmin, y);
                            mYmax = Math.max(mYmax, y);
                        }
                    } else {
                        errorMsg = "値不定か無限大が存在します";
                    }
                }
                if (0 < errorMsg.length())
                    Toast.makeText(this,errorMsg, Toast.LENGTH_LONG).show();
                if (0 < plotDataList.size()) {
                    PointF[] plotdatas = plotDataList.toArray(new PointF[plotDataList.size()]);
                    mPlotDatas.add(plotdatas);
                }
            }
        }
    }

    /**
     * 現在画面に設定されているデータをリストに登録する
     */
    private void dataRegist() {
        String[] data = new String[8];
        data[0] = mEdFunction.getText().toString().trim();
        data[1] = mEdRangeMin.getText().toString();
        data[2] = mEdRangeMax.getText().toString();
        data[3] = mEdDivideCount.getText().toString();
        data[4] = getFunctionType();
        data[5] = mEdRangeYMin.getText().toString();
        data[6] = mEdRangeYMax.getText().toString();
        data[7] = String.valueOf(mCbAutoHeight.isChecked());
        String key = getFunctionKey(mEdFunction.getText().toString().trim(), data[4]);
        mFuncData.put(key, data);
    }

    /**
     * 関数式から引数([a]=22...)リストを抽出する
     * @param function      関数式
     * @return              引数リスト
     */
    private ArrayList<String[]> getArgValue(String function) {
        ArrayList<String[]> argValues = new ArrayList<String[]>();
        String[] buf = function.split(";");
        for(int i=0; i < buf.length; i++) {
            if (0 < buf[i].length() && 0 < buf[i].indexOf("=")) {
                String[] argVal = new String[2];
                String[] arg = buf[i].split("=");
                if (1 < arg.length&& 0 <= arg[0].indexOf("[")) {
                    argVal[0] = arg[0].trim();
                    argVal[1] = arg[1].trim();
                    argValues.add(argVal);
                }
            }
        }
        return argValues;
    }

    /**
     * 関数式からタイトルキーワードを抽出
     * @param function      関数式
     * @return              タイトルキーワード
     */
    private String getFunctionKey(String function, String type) {
        String[] key = function.split(";");
        if (type.compareTo(mFunctionType[1]) == 0) {
            if (!(key[0].charAt(0)=='$') && 1<key.length)
                return key[0]+";"+key[1];
        }
        return key[0];
    }

    /**
     * ラジオボタンから関数式の種類を取得する
     * @return
     */
    private String getFunctionType() {
        if (mRbNormalFunction.isChecked()) {
            return mFunctionType[0];            //  一般的な方程式
        } else if (mRbPametricFunction.isChecked()) {
            return mFunctionType[1];            //  パラメトリック
        } else if (mRbPolarFunction.isChecked()) {
            return mFunctionType[2];            //  極方程式
        }
        return mFunctionType[0];
    }

    /**
     * 関数式の種類に応じてRadioButtonを設定する
     * @param type      関数式の種類
     */
    private void setFunctionType(String type) {
        //  種別のラジオボタンを設定
        if (type.compareTo(mFunctionType[0])==0)
            mRbNormalFunction.setChecked(true);
        else if (type.compareTo(mFunctionType[1])==0)
            mRbPametricFunction.setChecked(true);
        else if (type.compareTo(mFunctionType[2])==0)
            mRbPolarFunction.setChecked(true);
        else
            mRbNormalFunction.setChecked(true);
        //  種別による範囲タイトルの変更
        if (type.compareTo(mFunctionType[0])==0)
            mTvRangTitle.setText("範囲 X min");
        else
            mTvRangTitle.setText("範囲 t min");
    }

    /**
     * ファイルから関数式データを取得する
     * @param path      ファイルパス
     * @return          取得の可否
     */
    private boolean loadFuncData(String path) {
        if (!mYlib.existsFile(path)) {
//            Toast.makeText(this, "ファイルが存在していません\n"+path, Toast.LENGTH_LONG).show();
            return false;
        }
        //	ファイルデータの取り込み
        List<String> fileData = new ArrayList<String>();
        mYlib.readTextFile(path, fileData);
        mFuncData.clear();
        for (int i = 0; i < fileData.size(); i++) {
            String[] buf = mYlib.splitCsvString(fileData.get(i));
            if (buf[0].compareTo("関数式")!=0) {
                if (3 < buf.length) {
                    String[] data = new String[8];
                    data[0] = buf[0];           //  関数式
                    data[1] = buf[1];           //  Xmin
                    data[2] = buf[2];           //  Xmax
                    data[3] = buf[3];           //  分割数
                    if (4 < buf.length)         //  種別
                        data[4] = buf[4];
                    else
                        data[4] = mFunctionType[0];
                    if (7 < buf.length) {
                        data[5] = buf[5];       //  Ymin
                        data[6] = buf[6];       //  Ymax
                        data[7] = buf[7];       //  高さ自動
                    }
                    String key = getFunctionKey(buf[0], data[4]);
                    mFuncData.put(key,data);
                }
            }
        }
        return true;
    }

    /**
     * 関数式データをファイルに保存する
     * @param path      ファイルパス
     */
    private void saveFuncData(String path) {
        if (mFuncData==null)
            return;
        String buf = "関数式,Xmin,Xmax,分割数,種別,Ymin,Ymax,高さ自動\n";
        for (HashMap.Entry<String, String[]> entry  : mFuncData.entrySet()) {
            String[] data = entry.getValue();
            if (0 < data.length) {
                for (int i = 0; i < data.length - 1; i++)
                    buf += "\"" + data[i] + "\",";
                buf += "\"" + data[data.length - 1] + "\"\n";
            }
        }
        mYlib.writeFileData(path, buf);
    }

    /***
     * 画面の初期化
     */
    private void init() {
        mTvFunctionTitle = (TextView)findViewById(R.id.textView7);
        mTvRangTitle = (TextView)findViewById(R.id.textView8);
        mTvRangYTitle = (TextView)findViewById(R.id.textView9);
        mEdFunction = (EditText)findViewById(R.id.editText6);
        mEdRangeMin = (EditText)findViewById(R.id.editText7);
        mEdRangeMax = (EditText)findViewById(R.id.editText8);
        mEdRangeYMin = (EditText)findViewById(R.id.editText10);
        mEdRangeYMax = (EditText)findViewById(R.id.editText11);
        mEdDivideCount = (EditText)findViewById(R.id.editText9);
        mRbNormalFunction =(RadioButton)findViewById(R.id.radioButton);
        mRbPametricFunction = (RadioButton)findViewById(R.id.radioButton2);
        mRbPolarFunction = (RadioButton)findViewById(R.id.radioButton4);
        mRgFuncType = (RadioGroup)findViewById(R.id.radioGroup);
        mCbAutoHeight = (CheckBox)findViewById(R.id.checkBox4);
        mCbAspectFix = (CheckBox)findViewById(R.id.checkBox3);
        mBtExecute = (Button)findViewById(R.id.button26);
        mBtFunction = (Button)findViewById(R.id.button28);
        mBtRemove = (Button)findViewById(R.id.button27);
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout2);
        mFuncGraphView = new FuncGraphView(this);
        mLinearLayout.addView(mFuncGraphView);

        mEdFunction.setOnLongClickListener(this);
        mBtExecute.setOnClickListener(this);
        mBtFunction.setOnClickListener(this);
        mBtRemove.setOnClickListener(this);
        mCbAutoHeight.setOnCheckedChangeListener(this);
        mCbAspectFix.setOnCheckedChangeListener(this);

        //  フォーカスをボタンに移して起動時にキーボードが出るのを防ぐ
        mBtExecute.setFocusable(true);
        mBtExecute.setFocusableInTouchMode(true);
        mBtExecute.requestFocus();
    }
}
