package jp.co.yoshida.katsushige.calc2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import javax.microedition.khronos.opengles.GL10;

import jp.co.yoshida.katsushige.mylib.YLib;

public class Graph3DActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener{

    private static final String TAG = "Graph3DActivity";

    private LinearLayout mLinearLayout;
    private TextView mTvFunctionTitle;
    private EditText mEtFunction;
    private TextView mTvFRangeTitle;
    private EditText mEtFRangeMin;
    private EditText mEtFRangeMax;
    private EditText mEtFDivide;
    private TextView mTvSRangeTitle;
    private EditText mEtSRangeMin;
    private EditText mEtSRangeMax;
    private EditText mEtSDivide;
    private EditText mEtZRate;
    private RadioGroup mRgFunctionType;
    private RadioButton mRbNormal;
    private RadioButton mRbParametric;
    private Button mBtExecute;
    private Button mBtDelete;
    private Button mBtFunctionMenu;

    private GLViewViewer mGLViewViewer;
    private YLib mYlib;

    private String mSaveDirectory;
    private String mDataFileName = "Func3DPlot.csv";
    private String mDataFilePath;
    private Point3D mMin = new Point3D(-100, 100, 0);
    private Point3D mMax = new Point3D(-100, 100, 0);
    private double mFirstMin = -100;        //  第一パラメータ最小値
    private double mFirstMax = 100;
    private int mFirstDivide = 40;
    private double mSecoundMin = -100;      //  第二パラメータ
    private double mSecoundMax = 100;
    private int mSecoundDivide = 40;
//    private double mZmin = 0;
//    private double mZmax = 0;
    private double mZRate = 3;
    enum FUNCTION_TYPE {NON,NORMAL, PARAMETRIC}
    private FUNCTION_TYPE mFunctionType = FUNCTION_TYPE.NORMAL;

    private Point3D[][] mVertexData;
    private float[] mVertex;
    private float[][] mColor;
    private int[] mVertexSize;
    private Map<String, String[]> mFuncData;        //  関数式(タイトル,関数式、パラメータ)
    private boolean mError = false;
    private String mErrorMsg = "";
    private String[] mFunctionMenu = {
            "[x] 引数","[y] 引数","[s] 媒介変数の引数","[t] 媒介変数の引数","[@] sum/productで使う引数",
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
        setContentView(R.layout.activity_graph3_d);
        this.setTitle("三次元関数グラフ");
        Log.d(TAG,"onCreate: ");

        mYlib = new YLib();
        init();

        mEtFunction.setText("[x]*[y]");
        mEtFRangeMin.setText(String.valueOf(mFirstMin));
        mEtFRangeMax.setText(String.valueOf(mFirstMax));
        mEtFDivide.setText(String.valueOf(mFirstDivide));
        mEtSRangeMin.setText(String.valueOf(mSecoundMin));
        mEtSRangeMax.setText(String.valueOf(mSecoundMax));
        mEtSDivide.setText(String.valueOf(mSecoundDivide));
        mEtZRate.setText(String.valueOf(mZRate));
        mRgFunctionType.check(mRbNormal.getId());

        //  データ保存パスの設定
        mSaveDirectory = mYlib.setSaveDirectory(getPackageName().substring(getPackageName().lastIndexOf('.')+1));
        mDataFilePath = mSaveDirectory+"/"+mDataFileName;

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
        Log.d(TAG,"onClick: "+view.getId());
        if (view.getId() == mBtExecute.getId()) {
            Toast.makeText(this,"実行ボタンが押されました",Toast.LENGTH_SHORT).show();
            execute();
        } else if (view.getId() == mBtDelete.getId()) {
            //  現在表示されている関数をリストから削除する
            if (mEtFunction.getText().length() <= 0)
                return;
            String key = getFunctionKey(mEtFunction.getText().toString().trim());
            if (mFuncData.containsKey(key)) {
                mFuncData.remove(key);
                mEtFunction.setText("");
            }
        } else if (view.getId() == mBtFunctionMenu.getId()) {
            //  関数の選択メニューの表示して入力
            mYlib.setMenuDialog(this, "関数追加", mFunctionMenu, iFunctionSet);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        Log.d(TAG,"onLongClick: "+view.getId());
        if (view.getId() == mEtFunction.getId()) {
            //  関数式の選択メニューの表示
            String[] funcMenu = new String[mFuncData.size()];
            int n = 0;
            for (String key : mFuncData.keySet())
                funcMenu[n++] = key;
            Arrays.sort(funcMenu);
            mYlib.setMenuDialog(this, "関数式", funcMenu, iOperation);
        }
        return true;
//        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.d(TAG,"onCheckedChanged:Compound ");
        if (mRbParametric.isChecked()) {
            mTvFunctionTitle.setText("関数 x=f(s,t)..");
            mTvFRangeTitle.setText("範囲 s min");
            mTvSRangeTitle.setText("範囲 t min");
        }
        if (mRbNormal.isChecked()) {
            mRbNormal.setChecked(true);
            mTvFunctionTitle.setText("関数 z=f(x,y)");
            mTvFRangeTitle.setText("範囲 x min");
            mTvSRangeTitle.setText("範囲 y min");
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        Log.d(TAG,"onCheckedChanged:Radio ");
    }

    /**
     * 関数式の選択メニューに対して式とパラメータを設定する関数インターフェス
     */
    Consumer<String> iOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            if (mFuncData.containsKey(s)) {
                String[]data = mFuncData.get(s);
                if (7 < data.length) {
                    mEtFunction.setText(data[0]);
                    mEtFRangeMin.setText(data[1]);
                    mEtFRangeMax.setText(data[2]);
                    mEtFDivide.setText(data[3]);
                    mEtSRangeMin.setText(data[4]);
                    mEtSRangeMax.setText(data[5]);
                    mEtSDivide.setText(data[6]);
                    mEtZRate.setText(data[7]);
                }
                if (8 < data.length) {
                    setFunctionType(string2FunctionType(data[8]));
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
            String buf = mEtFunction.getText().toString();
            //  カーソル位置に文字を挿入
            buf = buf.substring(0, mEtFunction.getSelectionStart())+
                    s.substring(0, s.indexOf(' '))+
                    buf.substring(mEtFunction.getSelectionEnd());
            mEtFunction.setText(buf);
            //  カーソルを挿入した文字の後ろに移動する
            mEtFunction.setSelection(buf.indexOf(s.substring(0, s.indexOf(' ')))+
                    s.substring(0, s.indexOf(' ')).length());
        }
    };

    /**
     * 現在画面に設定されているデータをリストに登録する
     */
    private void dataRegist() {
        String[] data = new String[9];
        data[0] = mEtFunction.getText().toString().trim();
        data[1] = mEtFRangeMin.getText().toString();
        data[2] = mEtFRangeMax.getText().toString();
        data[3] = mEtFDivide.getText().toString();
        data[4] = mEtSRangeMin.getText().toString();
        data[5] = mEtSRangeMax.getText().toString();
        data[6] = mEtSDivide.getText().toString();
        data[7] = mEtZRate.getText().toString();
        data[8] = getFunctionType().toString();
        String key = getFunctionKey(mEtFunction.getText().toString().trim());
        mFuncData.put(key, data);
    }

    /**
     * 計算式の種類でラジオボタンを設定する
     * @param funcType      計算式の種類
     */
    private void setFunctionType(FUNCTION_TYPE funcType) {
        if (funcType==FUNCTION_TYPE.PARAMETRIC) {
            mRbParametric.setChecked(true);
            mTvFunctionTitle.setText("関数 x=f(s,t)..");
            mTvFRangeTitle.setText("範囲 s min");
            mTvSRangeTitle.setText("範囲 t min");
        } else {
            mRbNormal.setChecked(true);
            mTvFunctionTitle.setText("関数 z=f(x,y)");
            mTvFRangeTitle.setText("範囲 x min");
            mTvSRangeTitle.setText("範囲 y min");
        }
    }

    /**
     * ラジオボタンで設定されている計算式の種類を取得する
     * @return
     */
    private FUNCTION_TYPE getFunctionType() {
        if (mRbNormal.isChecked())
            return FUNCTION_TYPE.NORMAL;
        if (mRbParametric.isChecked())
            return FUNCTION_TYPE.PARAMETRIC;
        return FUNCTION_TYPE.NON;
    }

    /**
     * 初期化 画面ウィジットの関連付けと設定
     */
    private void init() {
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout3);
        mTvFunctionTitle = (TextView)findViewById(R.id.textView14);
        mTvFRangeTitle = (TextView)findViewById(R.id.textView15);
        mTvSRangeTitle = (TextView)findViewById(R.id.textView18);
        mEtFunction = (EditText)findViewById(R.id.editText12);
        mEtFRangeMin = (EditText)findViewById(R.id.editText13);
        mEtFRangeMax = (EditText)findViewById(R.id.editText14);
        mEtFDivide = (EditText)findViewById(R.id.editText15);
        mEtSRangeMin = (EditText)findViewById(R.id.editText16);
        mEtSRangeMax = (EditText)findViewById(R.id.editText17);
        mEtSDivide = (EditText)findViewById(R.id.editText18);
        mEtZRate = (EditText)findViewById(R.id.editText19);
        mRgFunctionType = (RadioGroup)findViewById(R.id.radioGroup1);
        mRbNormal = (RadioButton)findViewById(R.id.radioButton6);
        mRbParametric = (RadioButton)findViewById(R.id.radioButton7);
        mBtExecute = (Button)findViewById(R.id.button29);
        mBtDelete = (Button)findViewById(R.id.button30);
        mBtFunctionMenu = (Button)findViewById(R.id.button31);

        mEtFunction.setOnLongClickListener(this);
        mBtExecute.setOnClickListener(this);
        mBtDelete.setOnClickListener(this);
        mBtFunctionMenu.setOnClickListener(this);
        mRbNormal.setOnCheckedChangeListener(this);
        mRbParametric.setOnCheckedChangeListener(this);

        mGLViewViewer = new GLViewViewer(this);
        mLinearLayout.addView(mGLViewViewer);

        //  フォーカスをボタンに移して起動時にキーボードが出るのを防ぐ
        mBtExecute.setFocusable(true);
        mBtExecute.setFocusableInTouchMode(true);
        mBtExecute.requestFocus();
    }

    /**
     * [実行]ボタン 計算式を実行してグラフを表示
     */
    private void execute() {
        try {
            YCalc calc= new YCalc();
            mFirstMin = calc.expression(mEtFRangeMin.getText().toString());
            mFirstMax = calc.expression(mEtFRangeMax.getText().toString());
            mFirstDivide = (int)calc.expression(mEtFDivide.getText().toString());
            mSecoundMin = calc.expression(mEtSRangeMin.getText().toString());
            mSecoundMax = calc.expression(mEtSRangeMax.getText().toString());
            mSecoundDivide = (int)calc.expression(mEtSDivide.getText().toString());
            mZRate = calc.expression(mEtZRate.getText().toString());
        } catch(Exception e) {
            mYlib.messageDialog(this, "エラー", "入力値に間違いがあります");
            return;
        }
        makeFunctionData(mEtFunction.getText().toString());
        cnvBufferData();
        mGLViewViewer.setVertexData(mVertex, GL10.GL_TRIANGLE_STRIP, mVertexSize, mColor);
//        mGLViewViewer.setVertexData(cubeVertex, GL10.GL_TRIANGLES);
//        mGLViewViewer.setVertexData(cube2Vertex, GL10.GL_TRIANGLE_STRIP, cube2VertexSize, cube2Color);
        mGLViewViewer.requestRender();

        dataRegist();
    }

    /**
     * 計算式を評価して値を求める
     * @param calc          計算式(NORMALは1つ、PARAMETRICは3つの式を持つ
     * @param funcType      計算式の種類
     * @param f             座標値または媒介変数 (x/s)
     * @param s             座標値または媒介変数 (y/t)
     * @return              計算結果 (3次元データ x,y,z)
     */
    private Point3D funxtion(YCalc[] calc, FUNCTION_TYPE funcType,double f, double s) {
        Point3D pos = new Point3D();
        mError = false;
        mErrorMsg = "";
        if (funcType==FUNCTION_TYPE.NORMAL) {
            //  Normal 陽関数
            pos.X = (float)f;
            pos.Y = (float)s;
            calc[0].setArgvalue("[x]", "(" + f + ")");
            calc[0].setArgvalue("[y]", "(" + s + ")");
            pos.Z = (float)calc[0].calculate();
            mError = calc[0].mError;
            mErrorMsg = calc[0].mErrorMsg;
        } else if (2 < calc.length && funcType==FUNCTION_TYPE.PARAMETRIC) {
            //  Parametric 媒介変数
            calc[0].setArgvalue("[s]", "(" + f + ")");
            calc[0].setArgvalue("[t]", "(" + s + ")");
            pos.X = (float)calc[0].calculate();
            calc[1].setArgvalue("[s]", "(" + f + ")");
            calc[1].setArgvalue("[t]", "(" + s + ")");
            pos.Y = (float)calc[1].calculate();
            calc[2].setArgvalue("[s]", "(" + f + ")");
            calc[2].setArgvalue("[t]", "(" + s + ")");
            pos.Z = (float)calc[2].calculate();
            mError = calc[0].mError || calc[1].mError || calc[2].mError;
            mErrorMsg = calc[0].mErrorMsg + calc[1].mErrorMsg + calc[2].mErrorMsg;
        } else {
            mError = true;
            mErrorMsg = "計算式の種類と関数があっていない";
        }
        return pos;
    }

    /**
     * 計算式から座標データを求め、表示用データを作成する
     * @param function
     */
    private void makeFunctionData(String function) {
        Log.d(TAG, "makeFunctionData: " + mFirstMin + " " + mFirstMax + " " + mSecoundMin + " " + mSecoundMax + " " + mZRate);
        int xdiv = mFirstDivide;
        int ydiv = mSecoundDivide;

        if ((mFirstMax - mFirstMin) <= 0 || (mSecoundMax - mSecoundMin) <= 0) {
            mYlib.messageDialog(this, "エラー", "範囲指定が不正です");
            return;
        }

        FUNCTION_TYPE funcType = getFunctionType(function);
        ArrayList<String> functions = getFunctionList(function);
        if (funcType == FUNCTION_TYPE.PARAMETRIC && functions.size() < 3) {
            mYlib.messageDialog(this, "エラー", "パラメトリックで計算式が足りません");
            return;
        } else if (funcType == FUNCTION_TYPE.NON) {
            mYlib.messageDialog(this, "エラー", "計算式が定まっていません");
            return;
        }
        //  計算式の設定と内部引数の処理
        ArrayList<String[]> arguments = getArguments(function);
        YCalc[] calc = new YCalc[functions.size()];
        for (int i = 0; i < functions.size(); i++) {
            calc[i] = new YCalc();
            calc[i].setExpression(functions.get(i));
            for (String[] val : arguments) {
                calc[i].setArgvalue(val[0], "(" + val[1] + ")");
            }
        }

        //  座標データを求める
        mVertexData = new Point3D[xdiv + 1][ydiv + 1];
        for (int i = 0; i <= xdiv; i++) {
            for (int j = 0; j <= ydiv; j++) {
                double f = mFirstMin + (mFirstMax - mFirstMin) / xdiv * i;
                double s = mSecoundMin + (mSecoundMax - mSecoundMin) / ydiv * j;
                Point3D pos = funxtion(calc, funcType, f, s);
                mVertexData[i][j] = pos;
                //  領域の最小値と最大値をもとめる
                if (i == 0 && j == 0) {
                    mMin = pos;
                    mMax = pos;
                } else {
                    mMin = mMin.min(pos);
                    mMax = mMax.max(pos);
                }
            }
        }
        Log.d(TAG, "makeFunctionData: " + mVertexData.length + " " + mMin.toString() + " : " + mMax.toString());
    }

    private void cnvBufferData() {
        int xdiv = mFirstDivide;
        int ydiv = mSecoundDivide;
        float xrange = (float) (mMax.X - mMin.X);
        float yrange = (float) (mMax.Y - mMin.Y);
        float zrange = (float) (mMax.Z - mMin.Z);
        //  座標データを表示バッファに格納する
        mVertex = new float[xdiv*ydiv*4*3];
        mColor = new float[xdiv*ydiv][4];
        mVertexSize = new int[xdiv*ydiv];
        int pos = 0;
        int sizeIndex = 0;
        int colorIndex = 0;
        for (int i=0; i<xdiv; i++) {
            for (int j=0; j<ydiv; j++) {
                mVertex[pos++] = (float)mVertexData[i][j].X / xrange;
                mVertex[pos++] = (float)mVertexData[i][j].Y / yrange;
                mVertex[pos++] = (float)mVertexData[i][j].Z / zrange / (float)mZRate;
                mVertex[pos++] = (float)mVertexData[i+1][j].X / xrange;
                mVertex[pos++] = (float)mVertexData[i+1][j].Y / yrange;
                mVertex[pos++] = (float)mVertexData[i+1][j].Z / zrange / (float)mZRate;
                mVertex[pos++] = (float)mVertexData[i][j+1].X / xrange;
                mVertex[pos++] = (float)mVertexData[i][j+1].Y / yrange;
                mVertex[pos++] = (float)mVertexData[i][j+1].Z / zrange / (float)mZRate;
                mVertex[pos++] = (float)mVertexData[i+1][j+1].X / xrange;
                mVertex[pos++] = (float)mVertexData[i+1][j+1].Y / yrange;
                mVertex[pos++] = (float)mVertexData[i+1][j+1].Z / zrange / (float)mZRate;
                mVertexSize[sizeIndex++] = 4;
                float col = (float)((mVertexData[i][j].Z - mMin.Z) / zrange);
                mColor[colorIndex][0] = col<0.5?0.0f:col*2.0f;
                mColor[colorIndex][1] = col<0.5?col*2.0f:2.0f-col*2.0f;
                mColor[colorIndex][2] = col<0.5?01.0f-col*2.0f:0.0f;
                mColor[colorIndex][3] = 0.0f;
                colorIndex++;
            }
        }
        Log.d(TAG,"makeFunctionData: "+mVertex.length+" "+mColor.length+" "+mVertexSize.length);
    }

    /**
     * 計算式から変数を見て計算式の種類を求める
     * @param function      計算式
     * @return              計算式の種類
     */
    private FUNCTION_TYPE getFunctionType(String function) {
        FUNCTION_TYPE funcType = FUNCTION_TYPE.NON;
        String[] functions = function.split(";");
        for (int i=0; i<functions.length; i++){
            if (functions[i].charAt(0)=='$') {
                continue;
            } else if (0 <= functions[i].indexOf("[x]") || 0 <= functions[i].indexOf("[y]")) {
                if (funcType==FUNCTION_TYPE.NON || funcType==FUNCTION_TYPE.NORMAL) {
                    funcType = FUNCTION_TYPE.NORMAL;
                } else {
                    funcType = FUNCTION_TYPE.NON;
                    break;
                }
            } else if (0 <= functions[i].indexOf("[s]") || 0 <= functions[i].indexOf("[t]")) {
                if (funcType==FUNCTION_TYPE.NON || funcType==FUNCTION_TYPE.PARAMETRIC) {
                    funcType = FUNCTION_TYPE.PARAMETRIC;
                } else {
                    funcType = FUNCTION_TYPE.NON;
                    break;
                }
            }
        }
        return funcType;
    }

    /**
     * 計算式の文字列を個々の計算式のリストに分解する(コメント行や引数を含まないものは除く)
     * @param function  計算式
     * @return          計算式リスト
     */
    private ArrayList<String> getFunctionList(String function) {
        ArrayList<String> functionList = new ArrayList<String>();
        String[] functions = function.split(";");
        for (int i = 0; i < functions.length; i++) {
            if (functions[i].charAt(0) == '$') {
                continue;
            } else if (0 <= functions[i].indexOf("[x]") || 0 <= functions[i].indexOf("[y]")) {
                functionList.add(functions[i]);
            } else if (0 <= functions[i].indexOf("[s]") || 0 <= functions[i].indexOf("[t]")) {
                functionList.add(functions[i]);
            }
        }
        return functionList;
    }

    /**
     * 計算式から変数をリストに抽出する
     * @param function      計算式
     * @return              変数リスト
     */
    private ArrayList<String[]> getArguments(String function) {
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
     * 計算式から登録用のキータイトルを抽出する
     * 先頭がコメントの場合はコメントがキータイトルに計算式の場合は計算式の種類に合わせて計算式をキータイトルにする
     * @param function      計算式
     * @return              キータイトル
     */
    private String getFunctionKey(String function) {
        if(function==null || function.length() <=0)
            return "";
        String[] key = function.split(";");
        if (key[0].charAt(0)=='$') {
            return key[0].trim();       //  タイトル/コメント
        } else if (0 <= key[0].indexOf("[x]") || 0 <= key[0].indexOf("[y]")) {
            return key[0].trim();       //  陽関数
        } else if (2 < key.length && (0 <= key[0].indexOf("[s]") || 0 <= key[0].indexOf("[t]"))) {
            return key[0].trim() + ";" + key[1].trim() + ";" + key[2].trim();   //  パラメトリック関数
        }
        return function.trim();
    }

    /**
     * 計算式の種別文字列を種別に変換
     * @param val   計算式の種別文字列
     * @return      計算式の種別
     */
    private FUNCTION_TYPE string2FunctionType(String val) {
        if (val.compareTo(FUNCTION_TYPE.NON.toString())==0)
            return FUNCTION_TYPE.NORMAL;
        else if (val.compareTo(FUNCTION_TYPE.PARAMETRIC.toString()) == 0)
            return FUNCTION_TYPE.PARAMETRIC;
        else
            return FUNCTION_TYPE.NON;
    }

    /**
     * 計算式の種別を文字列に変換する
     * @param funcType      計算式の種別
     * @return              文字列
     */
    private String functionType2String(FUNCTION_TYPE funcType) {
        return funcType.toString();
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
                if (8 < buf.length) {
                    String[] data = new String[9];
                    data[0] = buf[0];           //  関数式
                    data[1] = buf[1];           //  Xmin
                    data[2] = buf[2];           //  Xmax
                    data[3] = buf[3];           //  X分割数
                    data[4] = buf[4];           //  Ymin
                    data[5] = buf[5];           //  Ymax
                    data[6] = buf[6];           //  Y分割
                    data[7] = buf[7];           //  Z縮小率
                    data[8] = buf[8];           //  種別
                    String key = getFunctionKey(buf[0]);
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
        String buf = "関数式,Xmin,Xmax,X分割数,Ymin,Ymax,Y分割,Z縮小率,種別高さ自動\n";
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

    /**
     * サンプル3Dデータ
     */
    private float[] cubeVertex = {
            -5.0f, -5.0f, -5.0f,
            5.0f, -5.0f, -5.0f,
            -5.0f, 5.0f, -5.0f,

            5.0f, -5.0f, -5.0f,
            5.0f, 5.0f, -5.0f,
            -5.0f, 5.0f, -5.0f,

            5.0f, -5.0f, -5.0f,
            5.0f, -5.0f, 5.0f,
            5.0f, 5.0f, -5.0f,

            5.0f, -5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, -5.0f,

            5.0f, -5.0f, 5.0f,
            -5.0f, -5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,

            -5.0f, -5.0f, 5.0f,
            -5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,

            -5.0f, -5.0f, 5.0f,
            -5.0f, -5.0f, -5.0f,
            -5.0f, 5.0f, 5.0f,

            -5.0f, -5.0f, -5.0f,
            -5.0f, 5.0f, -5.0f,
            -5.0f, 5.0f, 5.0f,

            -5.0f, 5.0f, -5.0f,
            5.0f, 5.0f, -5.0f,
            -5.0f, 5.0f, 5.0f,

            5.0f, 5.0f, -5.0f,
            5.0f, 5.0f, 5.0f,
            -5.0f, 5.0f, 5.0f,

            -5.0f, -5.0f, 5.0f,
            5.0f, -5.0f, 5.0f,
            -5.0f, -5.0f, -5.0f,

            5.0f, -5.0f, 5.0f,
            5.0f, -5.0f, -5.0f,
            -5.0f, -5.0f, -5.0f
    };

    private float[] cube2Vertex = {
            // 前
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            // 後
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            // 左
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            // 右
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            // 上
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            // 底
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f
    };

    private int[] cube2VertexSize = {4,4,4,4,4,4};
    private float[][] cube2Color = {
            {1.0f, 0.0f, 0.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f},
            {0.0f, 0.0f, 1.0f, 0.0f},
            {1.0f, 1.0f, 0.0f, 0.0f},
            {0.0f, 1.0f, 1.0f, 0.0f},
            {1.0f, 0.0f, 1.0f, 0.0f},
    };

}
