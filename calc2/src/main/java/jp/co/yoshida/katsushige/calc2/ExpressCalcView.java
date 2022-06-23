package jp.co.yoshida.katsushige.calc2;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.yoshida.katsushige.mylib.YDraw;
import jp.co.yoshida.katsushige.mylib.YGButton;
import jp.co.yoshida.katsushige.mylib.YLib;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

public class ExpressCalcView extends SurfaceView
        implements SurfaceHolder.Callback {


    private static final String TAG = "ExpressCalcView";

    private Context mC = null;              //
    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private int mViewHeight;
    private YGButton ygbutton;
    private YCalc ycalc;
    private YLib ylib;
    private Vibrator vib;                       //  バイブレーションの設定にはManifestに権限追加必要
    private ClipboardManager cm = null;         //  クリップボードマネージャー

    private float mTextSize;                    //  文字の大きさ
    private float mBorderWidth = 2f;
    private float mDispButtonHeight = 80f;      //  表示ボタンの高さ
    private int mNumberButtonColCount = 5;      //  数値ボタンの配列の列数
    private int mFuncButtonColCount = 6;        //  関数メニューボタンの列数
    private float mNumButtonRatio = 0.55f;      //  数値ボタンの縦横比
    private float mFuncButtonRatio = 0.5f;      //  関数ボタンの縦横比

    private final int mTitleId = 101;           //  計算式タイトル
    private final int mCommentId= 102;          //  計算式のコメント
    private final int mCalcResultId = 103;      //  計算結果
    private final int mExpressionId = 104;      //  計算式
    private final int mCustumInputId = 200;     //  カスタム計算の入力ID
    private final int mCustumTitleId = 250;     //  カスタム計算の入力タイトルID
    private final int mNumberId = 0;            //  0～9までの数値
    private final int mDotId = 10;
    private final int mAddId = 11;
    private final int mSubId = 12;
    private final int mMulId = 13;
    private final int mDivId = 14;
    private final int mEqId = 15;
    private final int mDummy = 16;
    private final int mCeId = 17;
    private final int mAcId = 18;
    private final int mBsId = 19;
    private final int mPMId = 20;
    private final int mPrePareId = 21;          //  括弧(
    private final int mSufPareId = 22;          //  括弧)
    private final int mModId = 23;              //  剰余 %
    private final int mPowId = 24;              //  累乗 ^
    private final int mCommaId = 25;            //  カンマ(,)
    private final int mTorigonoId = 30;         //  三角関数
    private final int mExpLogId = 31;           //  指数/対数
    private final int mIntegerId = 32;          //  整数化
    private final int mOrderId = 33;            //  整列、順列、組合せ
    private final int mConvtId = 34;            //  変換
    private final int mConstId = 35;            //  定数
    private final int mEtcFuncId = 36;          //  その他の関数
    private final int mAddListId = 50;          //  登録
    private final int mDelListId = 51;          //  削除
    private final int mEditListId = 52;         //  編集
    private final int mArgPareId = 55;          //  引数の大括弧表示
    private final int mFuncArgId = 56;          //  sum/productの引数表示
    private final int mHelpId = 57;             //  ヘルプ
    private final int mDummy2 = 58;
    private final int mLeftMoveId = 59;            //  カーソル移動
    private final int mRightMoveId = 60;          //  カーソル移動

    private float mExpressinBottom;             //  計算式入力領域のBottom座標
    //  入力キーIDと表示タイトル
    private Map<Integer,String> mKeyIdmap = new HashMap<Integer, String>();
    //  ファンクションキーのアサインID
    private int[] mFunctionsId = {mTorigonoId,mExpLogId,mIntegerId,mOrderId,mConvtId,mConstId,
            mEtcFuncId, mEditListId,mDelListId,mArgPareId,mFuncArgId,mHelpId,
            mModId, mPowId};
    //  数値キーのアサインID
    private int[] mNumbersId = {mPrePareId, mSufPareId, mCommaId, mLeftMoveId, mRightMoveId,
            mNumberId+7,mNumberId+8,mNumberId+9,mCeId,mAcId,
            mNumberId+4,mNumberId+5,mNumberId+6,mSubId,mDivId,
            mNumberId+1,mNumberId+2,mNumberId+3,mAddId,mMulId,
            mNumberId,mDotId,mPMId,mEqId,mBsId};

    //  関数メニュー
    private String[] mTorigonoMenu = {      //  三角関数メニュー
            "sin(x) 正弦",
            "cos(x) 余弦",
            "tan(x) 正接",
            "asin(x) 逆正弦",
            "acos(x) 逆余弦",
            "atan(x) 逆正接",
            "atan2(x,y) 逆正接",
            "sinh(x) 双曲線正弦",
            "cosh(x) 双曲線余弦",
            "tanh(x) 双曲線正接",
            "asinh(x) 逆双曲線正弦",
            "acosh(x) 逆双曲線余弦",
            "atanh(x) 逆双曲線正接"
    };
    private String[] mExpLogMenu = {        //  指数/対数メニュー
            "pow(x,y) 累乗",
            "sqrt(x) 平方根",
            "exp(x) eの累乗",
            "ln(x) eの自然対数",
            "log(x) 10の対数",
            "log(x,y) 底を指定した対数"
    };
    private String[] mIntegerMenu = {       //  整数化メニュー
            "abs(x) 絶対値",
            "ceil(x) 切上げ",
            "floor(x) 切捨て",
            "round(x) 四捨五入",
            "rint(x) 整数化(整数部)",
            "sign(x) 符号化(1/0/-1)"
    };
    private String[] mOrderMenu = {         //  比較/順列・組合せメニュー
            "max(x,y) 大きい方",
            "min(x,y) 小さい方",
            "combi(n,r) 組合せnCr",
            "permu(n,r) 順列nPr"
    };
    private String[] mConvtMenu = {         //  変換メニュー
            "RAD(x) 度をRadianに変換",
            "DEG(x) Radianを度に変換",
            "deg2hour(x) 度を時単位に変換する",
            "hour2deg(x) 時単位を度に変換する",
            "rad2hour(x) ラジアンを時単位に変換する",
            "hour2rad(x) 時単位をラジアンに変換する",
            "deg2dms(x) 度(dd.dddd)を度分秒(dd.mmss)に変換",
            "dms2deg(x) 度分秒(dd.mmss)を度(dd.dddd)に変換",
            "hour2hms(x) 時(hh.hhhh) → 時分秒(hh.mmss)",
            "hms2hour(x) 時分秒(hh.mmss) → 時(hh.hhhh)"
    };
    private String[] mConstMenu = {         //  定数メニュー
            "PI 円周率",
            "E 自然対数の底e"
    };
    private String[] mEtcFuncMenu = {       //  その他のメニュー
            "fact(n) 階乗n!",
            "fib(n) フィボナッチ数列",
            "gcd(x,y) 最大公約数",
            "lcm(x,y) 最小公倍数",
            "JD(y,m,d) 西暦年月日からユリウス日を求める",
            "MJD(y,m,d) 西暦年月日から準ユリウス日を求める",
            "equals(x,y) 等価判定 x==y ⇒ 1,x!=y ⇒ 0",
            "lt(x,y) 大小判定(less than) x > y ⇒ 1,以外は0",
            "gt(x,y) 大小判定(greater than) x < y ⇒ 1,以外は0",
            "compare(x,y) 大小判定 x > y ⇒ 1,x==y ⇒ 0,x<y ⇒ -1",
            "sum(f([@]),n,k) nからkまで連続した値を計算式f([@])演算した値の合計を求める",
            "sum(f([@]),n1,n2,...nm) n1からnmまで値を計算式f([@])演算した値の合計を求める",
            "product(f([@]),n,k) nからkまで連続した値を計算式f([@])演算した値の積を求める",
            "product(f([@]),n1,n2,...nm) n1からnmまで値を計算式f([@])演算した値の積を求める",
            "repeat(f([@],[%]),i,n,k) 計算式の[@]にnからkまで入れて繰返す,[%]に計算結果が入る,iは[%]の初期値",
    };
    private String[] mArgSampleMenu = {     //  引数メニュー
            "[@] sum/product 関数で使用する引数",
            "[#] 前回の計算結果",
            "[%] repeat関数の前回値",
            "[x] サンプル",
            "[y] サンプル",
            "[a] サンプル"
    };

    private String mTitleBuf = "";                                  //  計算式タイトルバッファ
    private String mCommentBuf = "";                                //  計算式のコメントバッファ
    private String mInputBuf = "";                                  //  計算式入力バッファ
    private boolean mCustumMode = false;                            //  カスタム計算モード
    private int mCurInputId = mExpressionId;                        //  現在の入力ボタンID
    private int mCurTempId;                                         //  一時ID
    private Map<String, String[]> mDataMap = new LinkedHashMap<String, String[]>();    //  計算式データリスト
    private String[][] mCalculateList = {
            //  計算式のタイトル, 計算式, コメント
            {"計算式を入力して計算する", "", "計算式を入れて=ボタンを押すと計算します。タイトルを押すと登録されている計算式が選択できます。"},
            {"BMIを計算する", "[体重kg]/([身長m]*[身長m])", "(標準18.5-25)"},
            {"適正体重 kg", "([身長m]*[身長m])*22", ""},
            {"高度(m)から気温(℃)を計算", "[地上の気温(℃)]-0.0065*[高度(m)]", ""},
            {"高度(m)から気圧(hPa)を計算", "[地上の気圧(hPa)]*pow((1-0.0065*[高度(m)]/([地上の気温(℃)]+273.15)),5.257)", "(地上気圧1013.3hPa)"},
            {"気圧(hPa)から高度(m)を計算", "(pow(([地上の気圧(hPa)]/[現在地の気圧(hPa)]),1/5.25)-1)*([地上の気温(℃)]+273.15))/0.0065", "(地上気圧1013.3hPa)"},
            {"ランニングの消費カロリkcal", "[メッツ]*[体重kg]*[運動時間hour]*1.05", "(時速8.3k→9メッツ 9.6k→9.8メッツ 11.3k→11メッツ)"},
            {"ウォーキングの消費カロリkcal", "[メッツ]*[体重kg]*[運動時間hour]*1.05", "(時速3.2k→2.8ッツ 4k→3メッツ 4.8k→3.5メッツ)"},
            {"基礎代謝量(男)(kcal/日)", "13.397*[体重kg]+4.799*[身長cm]-5.677*[年齢]+88.362", ""},
            {"基礎代謝量(女)(kcal/日)", "9.247*[体重kg]+3.098*[身長cm]-4.33*[年齢]+447.593", ""},
            {"男性肺活量(ml)", "(27.63-0.112*[年齢])*[身長cm]", ""},
            {"女性肺活量(ml)", "(21.78-0.101*[年齢])*[身長cm]", ""},
            {"ローンの返済額(月額)","[借入額]*[利率]*pow(1+[利率],[返済回数])/(pow(1+[利率],[返済回数])-1)",
                    "元利均等返済(毎月同じ金額を返す)で利率は月利(年利の1/12)とする。消費者金融の金利は年18%(月利1.5%),住宅ローンは年利1～1.5%ぐらい"},
            {"ローンの返済額(延べ返済額)","[借入額]*[利率]*pow(1+[利率],[返済回数])/(pow(1+[利率],[返済回数])-1)*[返済回数]",
                    "元利均等返済(毎月同じ金額を返す)で利率は月利(年利の1/12)とする。消費者金融の金利は年18%(月利1.5%),住宅ローンは年利1～1.5%ぐらい"},
            {"ブリクマン指数(喫煙が人体に与える影響)","[1日の喫煙本数]*[喫煙年数]",
                    "400を越えると癌の危険性が高い。1200を越えると吸わない人の64倍肺がんになりやすい"},
    };

    private String[] mCopyPasteMenu = {"コピー","貼付け"};
    private String mHelpText =
            "計算式を入力して計算するソフトです。\n" +
                    "計算方法は２つあり、１つは計算式を入力してそのまま計算、"+
                    "２つ目は引数付きの計算式を登録して引数の数値を入力して計算する\n"+
                    "一番上のタイトルを押すと計算式のメニューが出るのでその中から"+
                    "選択して行う。\n"+
                    "\n1.計算式入力による\n"+
                    "　操作部は下部の数値入力と関数やその他の操作部に分かれています。\n"+
                    "数値入力部は数値と四則演算子などがあり、式を入力したのちに[=]を押すと"+
                    "計算します。[BS]は一文字分戻す、[CE]は一区切り分戻し、[AC]で計算式をクリアします。\n"+
                    "[%]は x%y と入力すると x/yの余りとなり、[^]は x^y と入力するとxに対するyの累乗を計算します。"+
                    "[,]は関数で引数が２つ以上ある場合に区切りてして使用します。(pow(2,3) など)\n"+
                    "関数は上部の巻キーを押すと関数のメニューが表示されるのでその中から選択すると関数が入力"+
                    "されるので続けて数値と閉じ括弧を入力します。\n"+
                    "\n2.計算式の登録による方法\n"+
                    "　計算式は式を入力後に[編集]を押すと計算式の登録ダイヤログが表示されるのでタイトルを適当に"+
                    "編集して[OK}を押すと計算式が登録されます。登録された計算式はタイトルを押すとメニューの中に"+
                    "表示されます。特定のパラメータだけ随時変更かるには計算式の中に引数を設定すると引数が入力値と"+
                    "となります。\n"+
                    "例えば計算式に ([身長m]*[身長m])*22 とすると[身長m]が引数として入力できます。引数が複数"+
                    "ある時は引数分の入力項目ができます。引数は編集ダイヤログでわかりやすい名称にします。\n"+
                    "不要になれば表示して[削除]ボタンを押すとメニューから削除されます。";

    private int mDigitNumber = 14;                                  //  実数の丸め有効桁数

    //  データの保存
    private String mSaveDirectory;                                  //  データ保存ディレクトリ
    private String mSaveFilePath;                                   //  データ保存ファイルパス
    private final String mListFilename = "CalculateList.csv";       //  データ保存ファイル名

    //  データ編集ダイヤログのコントロール
    private TextView mTvTitle;
    private EditText mEdTitle;
    private TextView mTvComment;
    private EditText mEdComment;
    private TextView mTvExpression;
    private EditText mEdExpression;
    private LinearLayout mLinearLayout;

    public ExpressCalcView(Context context) {
        super(context);

        mC = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ygbutton = new YGButton(mSurfaceHolder);
        ycalc = new YCalc();
        ylib = new YLib();
        vib = (Vibrator)mC.getSystemService(VIBRATOR_SERVICE);
        cm = (ClipboardManager)mC.getSystemService(CLIPBOARD_SERVICE);    //システムのクリップボードを取得

        //  データ保存ディレクトリの設定
        setSaveDirectory();
        mSaveFilePath = mSaveDirectory + "/" + mListFilename;

        //  計算式データの取り込み
        mDataMap.clear();
        for (int i =0; i< mCalculateList.length; i++)
            mDataMap.put(mCalculateList[i][0],mCalculateList[i]);
        loadDataFile();

        //  キーコードの登録
        mKeyIdmap.put(mNumberId,"0");
        mKeyIdmap.put(mNumberId+1,"1");
        mKeyIdmap.put(mNumberId+2,"2");
        mKeyIdmap.put(mNumberId+3,"3");
        mKeyIdmap.put(mNumberId+4,"4");
        mKeyIdmap.put(mNumberId+5,"5");
        mKeyIdmap.put(mNumberId+6,"6");
        mKeyIdmap.put(mNumberId+7,"7");
        mKeyIdmap.put(mNumberId+8,"8");
        mKeyIdmap.put(mNumberId+9,"9");
        mKeyIdmap.put(mDotId,".");
        mKeyIdmap.put(mAddId,"+");
        mKeyIdmap.put(mSubId,"-");
        mKeyIdmap.put(mMulId,"*");
        mKeyIdmap.put(mDivId,"/");
        mKeyIdmap.put(mEqId,"=");
        mKeyIdmap.put(mDummy," ");
        mKeyIdmap.put(mCeId,"CE");
        mKeyIdmap.put(mAcId,"AC");
        mKeyIdmap.put(mBsId,"BS");
        mKeyIdmap.put(mPMId,"±");
        mKeyIdmap.put(mPrePareId,"(");
        mKeyIdmap.put(mSufPareId,")");
        mKeyIdmap.put(mModId,"% (余り)");
        mKeyIdmap.put(mPowId,"^ (累乗)");
        mKeyIdmap.put(mCommaId,",");
        mKeyIdmap.put(mTorigonoId,"三角関数");
        mKeyIdmap.put(mExpLogId,"指数/対数");
        mKeyIdmap.put(mIntegerId,"整数化");
        mKeyIdmap.put(mOrderId,"整列/順列");
        mKeyIdmap.put(mConvtId,"変換");
        mKeyIdmap.put(mConstId,"定数");
        mKeyIdmap.put(mEtcFuncId,"他の関数");
        mKeyIdmap.put(mEditListId,"編集/登録");
        mKeyIdmap.put(mAddListId,"登録");
        mKeyIdmap.put(mDelListId,"削除");
        mKeyIdmap.put(mArgPareId,"[引数]");
        mKeyIdmap.put(mFuncArgId,"[@]");
        mKeyIdmap.put(mHelpId,"Help");
        mKeyIdmap.put(mLeftMoveId,"<<");
        mKeyIdmap.put(mRightMoveId,">>");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mWidth = getWidth();
        mHeight = getHeight();
        mDispButtonHeight = mWidth / 13f;
        ygbutton.setOffScreen(mWidth, mHeight);
        mTextSize = ygbutton.getTextSize();
        //  画面の初期化
        initScreen();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        saveDataFile();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //  押されたボタン位置のマスを反転する
                int id = ygbutton.getButtonDownId(event.getX(), event.getY());
                if (0 <= id) {
                    vib.vibrate(10);
                    dispGButton(mTitleId, mTitleBuf);
                    inputKeyBoard(id);
                    if (mExpressinBottom != ygbutton.getGButtonSize(mExpressionId).bottom) {
                        Log.d(TAG,"onTouchEvent:"+ ygbutton.getGButtonSize(mExpressionId).bottom);
                        String[] bufs = mDataMap.get(mTitleBuf);    //  計算式の取得
                        setCustumScreen(bufs);
                        mExpressinBottom = ygbutton.getGButtonSize(mExpressionId).bottom;
                    }
                    ygbutton.unlockCanvasAndPost();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setViewHeight(int height) {
        mViewHeight = height;
    }


    /**
     * 入力キーを割り振る
     * @param id    入力キーのiD
     */
    private void inputKeyBoard(int id) {
        if (mCustumInputId <= id) {
            mInputBuf = "";
            mCurInputId = id;
            ygbutton.drawGButtonsDown(mCurInputId);
            ygbutton.unlockCanvasAndPost();
            return;
        }
        if (!mCustumMode){
            mCurInputId = mExpressionId;
        } else {
            if (mCurInputId < mCustumInputId)
                mCurInputId = mCustumInputId;
        }
        switch (id) {
            case mNumberId :            //  数値入力
            case mNumberId+1 :
            case mNumberId+2 :
            case mNumberId+3 :
            case mNumberId+4 :
            case mNumberId+5 :
            case mNumberId+6 :
            case mNumberId+7 :
            case mNumberId+8 :
            case mNumberId+9 :
            case mDotId :
            case mAddId :
            case mSubId :
            case mMulId :
            case mDivId :
            case mPrePareId:
            case mSufPareId:
            case mModId:
            case mPowId:
            case mCommaId :
                mInputBuf = addInputbuf(mKeyIdmap.get(id), mInputBuf);
                break;
            case mFuncArgId :           //  引数設定のダミー [@]
                setFunctionMenuDialog(mArgSampleMenu);
                break;
            case mArgPareId :           //  引数設定のダミー [引数]
                EditTextDialog(mC,"引数の編集入力", mKeyIdmap.get(id),iArgOperation);
                break;
            case mPMId :                //  符号の反転
                mInputBuf = editInputbuf(mPMId, mInputBuf);
                break;
            case mAcId :                //  入力バッファをクリア
                if (0 < mInputBuf.length())
                    mInputBuf = "";
                else
                    dispGButton(mCalcResultId, "");
                break;
            case mCeId :                //  最後尾から単語単位で削除
                mInputBuf = editInputbuf(mCeId, mInputBuf);
                break;
            case mBsId :                //  最後尾から１文字削除
                mInputBuf = editInputbuf(mBsId,mInputBuf);
                break;
            case mEqId :                //  計算処理の実施
                calculate();
                break;
            case mTorigonoId :          //  三角関数メニュー
                setFunctionMenuDialog(mTorigonoMenu);
                break;
            case mExpLogId :            //  指数対数メニュー
                setFunctionMenuDialog(mExpLogMenu);
                break;
            case mIntegerId :           //  桁調整処理メニュー
                setFunctionMenuDialog(mIntegerMenu);
                break;
            case mOrderId :             //  整列、順列、組合せ
                setFunctionMenuDialog(mOrderMenu);
                break;
            case mConvtId :             //  データ変換メニュー
                setFunctionMenuDialog(mConvtMenu);
                break;
            case mConstId :             //  定数メニュー
                setFunctionMenuDialog(mConstMenu);
                break;
            case mEtcFuncId :           //  その他の関数メニュー
                setFunctionMenuDialog(mEtcFuncMenu);
                break;
            case mEditListId:           //  計算式の編集・登録
                EditExpressDialog();
                break;
            case mAddListId :           //  計算式の登録
                addDataList();
                break;
            case mDelListId :           //  計算式の削除
                if (mTitleBuf.compareTo(mCalculateList[0][0])!=0)
                    ylib.messageDialog(mC, "計算式の削除",mTitleBuf, iDelListOperation);
                break;
            case mTitleId :             //  計算式の選択変更メニュー
                setExpressionListMenu();
                break;
            case mHelpId :              //  ヘルプダイヤログ表示
                HelpDialog();
                break;
            case mLeftMoveId:
                mInputBuf = editInputbuf(mLeftMoveId, mInputBuf);
                break;
            case mRightMoveId:
                mInputBuf = editInputbuf(mRightMoveId, mInputBuf);
                break;
            case mExpressionId :        //  計算式
            case mCommentId :
                mCurTempId = id;
                ylib.setMenuDialog(mC, "コピー/貼付け", mCopyPasteMenu, iCopyPasteOperation);
                break;
            case mCalcResultId :        //  計算結果
                setTextClipBoard(ygbutton.getGButtonTitle(mCalcResultId));
                break;
        }
        dispGButton(mCurInputId, mInputBuf);
    }

    /**
     * 計算式のコピーと貼付けの関数インターフェース
     */
    Consumer<String> iCopyPasteOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            if (s.compareTo("コピー")==0) {
                setTextClipBoard(ygbutton.getGButtonTitle(mCurTempId));
            } else if (s.compareTo("貼付け")==0) {
                mInputBuf = getTextClipBoard();
                if (mInputBuf!=null) {
                    dispGButton(mCurTempId, mInputBuf);
                    ygbutton.unlockCanvasAndPost();
                }
            }
        }
    };

    /**
     * クリップボードにテキストをコピーする
     * @param text      コピーするテキスト
     */
    private void setTextClipBoard(String text) {
        //クリップボードに格納するItemを作成
        ClipData.Item item = new ClipData.Item(text);
        //MIMETYPEの作成
        String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        //クリップボードに格納するClipDataオブジェクトの作成
        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        //クリップボードにデータを格納
        ClipboardManager cm = (ClipboardManager)mC.getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
    }

    /**
     * クリップボードからテキストを取り出す
     * @return      クリップボードのテキスト
     */
    private String getTextClipBoard() {
        ClipData cd = cm.getPrimaryClip();              //クリップボードからClipDataを取得
        if(cd != null) {
            ClipData.Item item = cd.getItemAt(0);   //クリップデータからItemを取得
            return item.getText().toString();
        }
        return null;
    }

    /***
     * 空白で区切られた文字列の前半部分に文字を追加する
     * @param key           追加挿入する文字列
     * @param inputBuf      編集する文字列
     * @return              編集後の文字列
     */
    private String addInputbuf(String key, String inputBuf) {
        String[] strBuf = inputBuf.split(" ");
        if (strBuf.length < 1)
            return "";
        String buf = "";
        if (0 <= key.indexOf(' '))
            buf = strBuf[0] + key.substring(0, key.indexOf(' '))+" ";
        else
            buf = strBuf[0] + key + " ";
        for (int i = 1; i < strBuf.length; i++)
            buf += strBuf[i];
        return buf;
    }

    /***
     * 空白で区切られた文字列の編集
     * @param funcId        ファンクションキーのID
     * @param inputBuf      編集する文字列
     * @return              編集後の文字列
     */
    private String editInputbuf(int funcId, String inputBuf) {
        String[] strBuf = inputBuf.split(" ");
        if (strBuf.length < 1)
            return "";
        String buf = "";
        if (funcId == mLeftMoveId) {        //  空白を左に移動
            List<String> listBuf = ylib.getToken(strBuf[0]);
            for (int i = 0; i < listBuf.size(); i++) {
                buf += listBuf.get(i)+" ";
            }
//            Log.d(TAG,"editInputbuf: "+buf);
            buf = "";
            for (int i = 0; i < listBuf.size(); i++) {
                if (i == listBuf.size() - 1)
                    buf += " " + listBuf.get(i);
                else
                    buf += listBuf.get(i);
            }
            for (int i = 1; i < strBuf.length; i++)
                buf += strBuf[i];
        } else if (funcId == mRightMoveId) {    //  空白を右に移動
            buf = strBuf[0];
            if (1 < strBuf.length) {
                List<String> listBuf = ylib.getToken(strBuf[1]);
                buf += listBuf.get(0) +" ";
                for (int i = 1; i < listBuf.size(); i++)
                    buf += listBuf.get(i);
                for (int i = 2; i < strBuf.length; i++)
                    buf += strBuf[i];
            }
        } else if (funcId == mPMId) {           //  空白のすぐ手前の数値の符号を変える
            buf = ycalc.setPMfunc(strBuf[0]) + " ";
            for (int i = 1; i < strBuf.length; i++)
                buf += strBuf[i];
        } else if (funcId == mCeId) {           //  空白のすぐ手前の単語を削除
            buf = ycalc.setCEfunc(strBuf[0]) + " ";
            for (int i = 1; i < strBuf.length; i++)
                buf += strBuf[i];
        } else if (funcId == mBsId) {           //  空白のすぐ手前の文字を削除
            if (0 < strBuf[0].length())
                buf = strBuf[0].substring(0, strBuf[0].length() - 1) + " ";
            for (int i = 1; i < strBuf.length; i++)
                buf += strBuf[i];
        }
        return buf;
    }


    /**
     * 計算式の計算処理と結果表示
     */
    private void calculate() {
        String resultTitle = ygbutton.getGButtonTitle(mCalcResultId).replace(",", "");
        if (!ylib.isFloat(resultTitle))
            resultTitle = "";
        Double result;
        if (mCustumMode) {
            //  引数付き計算式の処理
            result = custumCalculate();
        } else {
            //  通常の計算式
            String express = mInputBuf;
            while (0 <= express.indexOf("[#]")) {
                express = express.replace("[#]", resultTitle);
            }
            result = ycalc.getEfficientRound(ycalc.expression(express), mDigitNumber);
        }
//                    Log.d(TAG, "inputKeyBord expression:" + mInputBuf + " " + result);
        //  結果表示
        if (ycalc.mError) {
            dispGButton(mCalcResultId, "ERROR " + ycalc.mErrorMsg);
        } else {
            dispGButton(mCalcResultId, ylib.setDigitSeparator(result.toString()));
        }
    }

    /**
     * 引数付き計算式の計算処理
     */
    private double custumCalculate() {
//        ycalc.setExpression(ygbutton.getButtonTitle(mExpressionId));
        //  引数値の取得と計算式に設定
        for (int i = 0; i < ycalc.argKeySize(); i++) {
            if (!ygbutton.getGButtonTitle(mCustumTitleId+i).isEmpty()) {
                Log.d(TAG,"custumCalculate:"+i+" "+ ygbutton.getGButtonTitle(mCustumTitleId+i)+" "+ ygbutton.getGButtonTitle(mCustumInputId+i));
                ycalc.setArgvalue(ygbutton.getGButtonTitle(mCustumTitleId+i), ygbutton.getGButtonTitle(mCustumInputId+i));
            }
        }
        //  計算処理
        return ycalc.getEfficientRound(ycalc.calculate(), mDigitNumber);
    }

    /**
     * 画面上の計算式をデータリストから削除する
     */
    private void delDataList() {
        mTitleBuf = ygbutton.getGButtonTitle(mTitleId);
        mDataMap.remove(mTitleBuf);
        mTitleBuf = mCalculateList[0][0];       //  タイトル
        mInputBuf = mCalculateList[0][1];       //  計算式の取得
        mCommentBuf = mCalculateList[0][2];     //  コメント
        setCustumScreen(mCalculateList[0]);     //  計算式から引数を抽出
    }

    /**
     * 登録されている計算式をリストから削除する関数インターフェース
     */
    Consumer<String> iDelListOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            mDataMap.remove(s);                     //  ダイヤログで指定された文字列をリストから削除
            //  画面上のデータをデフォルトにする
            mTitleBuf = mCalculateList[0][0];       //  タイトル
            mInputBuf = mCalculateList[0][1];       //  計算式の取得
            mCommentBuf = mCalculateList[0][2];     //  コメント
            setCustumScreen(mCalculateList[0]);     //  計算式から引数を抽出
        }
    };

    /**
     * 計算式に引数を追加する関数インターフェース
     */
    Consumer<String> iArgOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            mInputBuf = addInputbuf(s, mInputBuf);
            dispGButton(mCurInputId, mInputBuf);
        }
    };


    /**
     * 画面上の計算式とタイトルをデータリストに登録する
     */
    private void addDataList() {
        mTitleBuf = ygbutton.getGButtonTitle(mTitleId);
        mCommentBuf = ygbutton.getGButtonTitle(mCommentId);
        mInputBuf = ygbutton.getGButtonTitle(mExpressionId);
        String[] bufs = {mTitleBuf, mInputBuf, mCommentBuf};
        mDataMap.put(mTitleBuf, bufs);
    }

    /**
     * 計算式のデータをファイルに保存する
     */
    public void saveDataFile() {
        String buffer = "";
        //  計算式リストをStringにバッファリングする
        for (HashMap.Entry<String,String[]> entry : mDataMap.entrySet()) {
            if (entry.getValue() == null)                   //  タイトルのみ
                buffer += "\"" + entry.getKey() + "\n";
            else if (entry.getValue().length == 2)          //  タイトルと計算式
                buffer += "\"" + entry.getKey() + "\",\"" + entry.getValue()[1] +  "\n";
            else if (entry.getValue().length == 3)          //  タイトルと計算式とコメント
                buffer += "\"" + entry.getKey() + "\",\"" + entry.getValue()[1] +  "\",\"" +
                        entry.getValue()[2] + "\"" + "\n";
            else                                            //  タイトルのみ
                buffer += "\"" + entry.getKey() + "\n";
        }
        //  ファイルに保存
        if (0 < buffer.length()) {
            ylib.writeFileData(mSaveFilePath, buffer);
//            if (mC != null)
//                Toast.makeText(mC, mSaveFilePath + "\n保存しました", Toast.LENGTH_LONG).show();
        } else {
            if (mC != null)
                Toast.makeText(mC, "データがありません", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 計算式のデータをファイルからロードする
     * @return
     */
    public boolean loadDataFile() {
        //	ファイルの存在確認
        if (!ylib.existsFile(mSaveFilePath)) {
            if (mC!=null)
                Toast.makeText(mC, "データが登録されていません\n"+mSaveFilePath, Toast.LENGTH_LONG).show();
            return false;
        }

        //	ファイルデータの取り込み
        List<String> fileData = new ArrayList<String>();
        fileData.clear();
        ylib.readTextFile(mSaveFilePath, fileData);
        if (fileData.size()<1)
            return false;

        //	データを複写する
        for (int i=0; i<fileData.size(); i++) {
            String[] text = ylib.splitCsvString(fileData.get(i));
            if (0 < text.length)
                mDataMap.put(text[0], text);
        }

        return true;
    }

    /**
     * タイトル領域(GButton)に文字列を右寄せで表示する
     * @param id        領域のID
     * @param str       表示文字列
     */
    private void dispGButton(int id, String str) {
        if (id == mTitleId)
            dispGButton(id, str, YDraw.TEXTALIGNMENT.CC);
        else if (mCommentId == id)
            dispGButton(id, str, YDraw.TEXTALIGNMENT.LC);
        else if (mExpressionId == id)
            dispGButton(id, str, YDraw.TEXTALIGNMENT.LC);
        else if (mCustumTitleId <= id)
            dispGButton(id, str, YDraw.TEXTALIGNMENT.LC);
        else
            dispGButton(id, str, YDraw.TEXTALIGNMENT.RC);
    }

    /**
     * GButtonに文字列を登録し表示する
     * @param id        GButtonのID
     * @param str       文字列
     * @param ta        文字列のアライメント
     */
    private void dispGButton(int id, String str, YDraw.TEXTALIGNMENT ta) {
        ygbutton.setGButtonTitle(id, str, ta);
        ygbutton.drawGButton(id);
        ygbutton.unlockCanvasAndPost();
    }
    /**
     * 画面を初期化する
     */
    private void initScreen() {
        ygbutton.clearButtons();

        mTitleBuf = mCalculateList[0][0];
        mCommentBuf = mCalculateList[0][2];
        float y = 0;
        //  タイトル、コメント、計算結果、計算式のエリア表示
        y = initDisplayButton(y);
        y += 10f;
        //  ファンクションキーの表示
        y = initFunctionButton(mFunctionsId,  y, mFuncButtonColCount, mFuncButtonRatio, 1f/2.4f);
        y += 10f;
        //  数値、演算キーの表示
        y = initFunctionButton(mNumbersId,  y, mNumberButtonColCount, mNumButtonRatio, 0f);

        ygbutton.lockCanvas();
        ygbutton.backColor(Color.WHITE);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     *  タイトル、計算結果、計算式(GButton)の設定
     * @param y     開始高さ位置
     * @return      終了高さ位置
     */
    private float initDisplayButton(Float y) {
        float x = 0;
        //  タイトル
        float dx = mWidth / 2f;
        float dy = mDispButtonHeight / 2f;
        ygbutton.addGButton(mTitleId, YGButton.BUTTONTYPE.RECT, x + dx , y + dy ,0, mWidth, dy * 2f);
        ygbutton.setGButtonBorderWidth(mTitleId, mBorderWidth);
        ygbutton.setGButtonTitle(mTitleId, mTitleBuf);
        y = ygbutton.getGButtonSize(mTitleId).bottom;
        //  コメント
        dy = mDispButtonHeight * 0.75f / 2f;
        ygbutton.addGButton(mCommentId, YGButton.BUTTONTYPE.RECT, x + dx , y + dy ,0, mWidth, dy * 2f);
        ygbutton.setGButtonBorderWidth(mCommentId, mBorderWidth);
        ygbutton.setGButtonExtension(mCommentId, true);
        ygbutton.setGButtonTitle(mCommentId, mCommentBuf, YGButton.TEXTALIGNMENT.LC);
        y = ygbutton.getGButtonSize(mCommentId).bottom;
        //  計算結果
        dy = mDispButtonHeight / 2f;
        ygbutton.addGButton(mCalcResultId, YGButton.BUTTONTYPE.RECT, x + dx , y + dy ,0, mWidth, dy * 2f);
        ygbutton.setGButtonBorderWidth(mCalcResultId, mBorderWidth);
        ygbutton.setGButtonTitle(mCalcResultId, "計算結果", YGButton.TEXTALIGNMENT.RC);
        y = ygbutton.getGButtonSize(mCalcResultId).bottom;
        //  計算式
        dy = mDispButtonHeight * 0.75f / 2f;
        ygbutton.addGButton(mExpressionId, YGButton.BUTTONTYPE.RECT, x + dx , y + dy ,0, mWidth, dy * 2f);
        ygbutton.setGButtonBorderWidth(mExpressionId, mBorderWidth);
        ygbutton.setGButtonExtension(mExpressionId, true);
        ygbutton.setGButtonTitle(mExpressionId, "計算式", YGButton.TEXTALIGNMENT.LC);
        y = ygbutton.getGButtonSize(mExpressionId).bottom;

        return y;
    }

    /**
     * 入力ボタンの設定
     * @param id                ボタンIDの配列
     * @param y                 開始高さ
     * @param divNo             横の分割数
     * @param ratio             ボタンの縦横比
     * @param titleSizeRatio    ボタンの高さに対する文字高さの比
     * @return                  終了高さ位置
     */
    private float initFunctionButton(int[] id, float y, int divNo, float ratio, float titleSizeRatio) {
        float dx = mWidth / (float)divNo;   //  ボタンの幅
        float dy = dx * ratio;              //  ボタンの高さ
        float x = 0f;
        int n = 0;
        y -= dy;
        while (n < id.length) {
            if ((n % divNo) == 0) {
                x = 0;
                y += dy;
            } else {
                x += dx;
            }
            ygbutton.addGButton(id[n], YGButton.BUTTONTYPE.RECT, x + dx / 2f, y + dy / 2f, 0, dx, dy);
            if (0 < titleSizeRatio)
                ygbutton.setGButtonTitleSize(id[n],dy * titleSizeRatio);
            ygbutton.setGButtonBorderWidth(id[n], mBorderWidth);
            ygbutton.setGButtonBackColor(id[n], Color.LTGRAY);
            ygbutton.setGButtonTitle(id[n], mKeyIdmap.get(id[n]));
            n++;
        }
        y += dy;
        return y;
    }

    /**
     * 引数入力画面を作る
     * @param y     表示開始高さ
     * @return      表示終了高さ
     */
    private float initArgumentButton(float y) {
        //  カスタム入力画面 計算式に引数がある場合
        //  引数のタイトルと入力表示エリアの作成
        String[] custumTitle = ycalc.getArgKey();
        Arrays.sort(custumTitle);
        for (int i = 0; i < custumTitle.length; i++) {
            if (custumTitle[i].compareTo("[@]")!=0 ||
                custumTitle[i].compareTo("[#]")!=0 ||
                custumTitle[i].compareTo("[%]")!=0) {
                float x = 0;
                float dx = mWidth / 2f;
                float dy = mDispButtonHeight * 0.75f;
                //  引数タイトル
                ygbutton.addGButton(mCustumTitleId + i, YGButton.BUTTONTYPE.RECT, x + dx / 2f, y + dy / 2f, 0, dx, dy);
                ygbutton.setGButtonBorderWidth(mCustumTitleId + i, mBorderWidth);
                ygbutton.setGButtonTitle(mCustumTitleId + i, custumTitle[i], YGButton.TEXTALIGNMENT.LC);
                x += dx;
                //  引数入力エリア
                ygbutton.addGButton(mCustumInputId + i, YGButton.BUTTONTYPE.RECT, x + dx / 2f, y + dy / 2f, 0, dx, dy);
                ygbutton.setGButtonBorderWidth(mCustumInputId + i, mBorderWidth);
                ygbutton.setGButtonTitle(mCustumInputId + i, "", YGButton.TEXTALIGNMENT.RC);
                y += dy;
            }
        }

        mCustumMode = true;
        mCurInputId = mCustumInputId;

        return y;
    }

    /**
     * 画面構成を作成し高さを求める
     * @param str   カスタム計算の文字列リスト
     * @return      カスタム画面の高さ
     */
    private float setCustumScreenData(String[] str) {
        float y = 0;
        //  タイトル、コメント、計算式、計算結果領域の表示
        y = initDisplayButton(y);
        dispGButton(mTitleId, mTitleBuf);
        dispGButton(mCommentId, mCommentBuf);
        dispGButton(mExpressionId, mInputBuf);
        y = ygbutton.getGButtonSize(mExpressionId).bottom + 10f;
        mCustumMode = false;
        mCurInputId = mExpressionId;

        //  計算式に引数処理がある場合
        if (1 < str.length) {
            if (0 < ycalc.setExpression(str[1])) {
                y = initArgumentButton(y);
                y += 10;
            }
        }
        Log.d(TAG,"setCustumScreen1: "+y+" "+str.length);
        //  関数メニューなどのファンクションキーの表示
        y = initFunctionButton(mFunctionsId, y, mFuncButtonColCount, mFuncButtonRatio, 1f/2.4f);
        y += 10f;
        //  数値、操作キーの表示
        y = initFunctionButton(mNumbersId, y, mNumberButtonColCount, mNumButtonRatio, 0f);
        return y;
    }

    /**
     * カスタム計算式に合わせて画面レイアウトを設定する
     * 計算式の[引数]入力項目を挿入
     * @param str       計算式
     */
    private void setCustumScreen(String[] str) {
        Log.d(TAG,"setCustumScreen:"+str[0]);
        //  事前に表示領域の大きさを求め、Viewサイズより大きければ縮小する
        float cy = setCustumScreenData(str)+20;
        Log.d(TAG,"setCustumScreen: Height: "+cy+" "+getHeight());
        float tmpDispButtonHeight = mDispButtonHeight;
        float tmpFuncButtonHeight = mFuncButtonRatio;
        float tmpNumButtonHeight = mNumButtonRatio;
        if (getHeight() < cy ) {
            mDispButtonHeight *= getHeight() / cy;
            mFuncButtonRatio *= getHeight() / cy;
            mNumButtonRatio *= getHeight() / cy;
        }

        float y = 0;
        ygbutton.clearButtons();

        y = setCustumScreenData(str);

        ygbutton.backColor(Color.WHITE);
        ygbutton.drawGButtons();
        if (mCustumMode)
            ygbutton.drawGButtonsDown(mCurInputId);
        ygbutton.unlockCanvasAndPost();

        mDispButtonHeight = tmpDispButtonHeight;
        mFuncButtonRatio = tmpFuncButtonHeight;
        mNumButtonRatio = tmpNumButtonHeight;
    }

    /**
     * メニューから計算式を選択して変更する
     */
    private void setExpressionListMenu(){
        String[] menu = new String[mDataMap.size()];
        int n = 0;
        for (String key : mDataMap.keySet())
            menu[n++] = key;
        setTitleMenuDialog(menu);
    }

    /**
     * 計算式のリストをメニューで表示し選択する
     * @param menu
     */
    private void setTitleMenuDialog(final String[] menu) {
        new AlertDialog.Builder(mC)
                .setTitle("計算式メニュー")
                .setItems(menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText( mC,which+" が選択", Toast.LENGTH_LONG).show();
                        mTitleBuf = menu[which];
                        String[] bufs = mDataMap.get(mTitleBuf);    //  計算式の取得
                        if (1 < bufs.length)
                            mInputBuf = bufs[1];
                        if (2 < bufs.length)
                            mCommentBuf = bufs[2];
                        setCustumScreen(bufs);                 //  計算式から引数を抽出
                        mExpressinBottom = ygbutton.getGButtonSize(mExpressionId).bottom;
                        if (mCustumMode)
                            mInputBuf = "";
                    }
                })
                .create()
                .show();
    }

    /**
     * 関数の選択メニュー表示ダイヤログ
     * @param menu          関数の項目リスト
     */
    private void setFunctionMenuDialog(final String[] menu) {
        new AlertDialog.Builder(mC)
                .setTitle("関数メニュー")
                .setItems(menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( mC,which+" が選択", Toast.LENGTH_LONG).show();
                        if (0 < menu[which].indexOf('(')) {             //  関数の場合
                            mInputBuf = addInputbuf(menu[which].substring(0, menu[which].indexOf('(') + 1), mInputBuf);
                        } else if (0 < menu[which].indexOf(' ')) {      //  定数の場合
                            mInputBuf = addInputbuf(menu[which].substring(0, menu[which].indexOf(' ')), mInputBuf);
                        }
                        dispGButton(mExpressionId, mInputBuf);
                        ygbutton.unlockCanvasAndPost();
                    }
                })
                .create()
                .show();
    }

    /**
     * 文字列編集ダイヤログ
     * @param c             コンテキスト
     * @param title         ダイヤログのタイトル
     * @param text          編集する文字列
     * @param operation     編集した文字列の操作関数(関数インターフェース)
     */
    private void EditTextDialog(final Context c, String title, String text, final Consumer operation) {
        mEdTitle = new EditText(c);
        mEdTitle.setText(text);
        mLinearLayout = new LinearLayout(c);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mEdTitle);

        new AlertDialog.Builder(c)
                .setTitle(title)
                .setView(mLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        operation.accept(mEdTitle.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    /**
     * 計算式と計算式タイトルの編集ダイヤログ
     */
    private void EditExpressDialog() {
        mTvTitle = new TextView(mC);
        mEdTitle = new EditText(mC);
        mTvComment = new TextView(mC);
        mEdComment = new EditText(mC);
        mTvExpression = new TextView(mC);
        mEdExpression = new EditText(mC);
        mLinearLayout = new LinearLayout(mC);
        mTvTitle.setText("タイトル");
        mEdTitle.setText(ygbutton.getGButtonTitle(mTitleId));
        mTvComment.setText("コメント");
        mEdComment.setText(ygbutton.getGButtonTitle(mCommentId));
        mTvExpression.setText("計算式");
        mEdExpression.setText(ygbutton.getGButtonTitle(mExpressionId));
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mTvTitle);
        mLinearLayout.addView(mEdTitle);
        mLinearLayout.addView(mTvComment);
        mLinearLayout.addView(mEdComment);
        mLinearLayout.addView(mTvExpression);
        mLinearLayout.addView(mEdExpression);

        new AlertDialog.Builder(mC)
                .setTitle("計算式の編集と登録")
                .setView(mLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ygbutton.setGButtonTitle(mTitleId, mEdTitle.getText().toString());
                        ygbutton.setGButtonTitle(mCommentId, mEdComment.getText().toString());
                        ygbutton.setGButtonTitle(mExpressionId, mEdExpression.getText().toString());
                        ygbutton.drawGButton(mTitleId);
                        ygbutton.drawGButton(mCommentId);
                        ygbutton.drawGButton(mExpressionId);
                        ygbutton.unlockCanvasAndPost();
                        addDataList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    /**
     * 	ヘルプダイヤログの表示
     */
    public void HelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mC);
        builder.setTitle("計算式ヘルプ");
        builder.setMessage(mHelpText);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.show();
        //  メッセージの文字の大きさを変更
        TextView textView = (TextView)dialog.findViewById(android.R.id.message);
        textView.setTextSize(12.0f);
    }

    /**
     * ワークディレクトリの作成
     * Manifestのパーミッションを設定が必要
     *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     *  スマホの設定でアプリ情報の権限でストレージを可にする
     */
    private void setSaveDirectory() {
        //	データ保存ディレクトリ
        mSaveDirectory = Environment.getExternalStorageDirectory().toString()+"/calc2";
        if (!ylib.existsFile(mSaveDirectory) && !ylib.isDirectory(mSaveDirectory)) {
            if (!ylib.mkdir(mSaveDirectory)) {
                Toast.makeText(mC, "ディレクトリが作成できません\nアプリ情報の権限を確認してください\n"+
                        mSaveDirectory, Toast.LENGTH_LONG).show();
                mSaveDirectory = Environment.getExternalStorageDirectory().toString();
            }
        }
    }

}
