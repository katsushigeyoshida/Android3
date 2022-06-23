package jp.co.yoshida.katsushige.calc2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YDraw;
import jp.co.yoshida.katsushige.mylib.YLib;

public class GraphView extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = "Calc2 GraphView";

    private Context mC = null;              //
    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private float mWorldLeft;
    private float mWorldTop;
    private float mWorldRight;
    private float mWorldBottom;

    private YDraw ydraw;
    private YCalc ycalc;
    private YLib ylib;

    public String mSaveStatisticsDirectory;     //  データ保存ディレクトリ
    public String mDataFileName;                //  データファイル名
    enum GRAPHTYPE {SCATTERPLOTS, HISTGRAM};
    public GRAPHTYPE mGraphType;
    public boolean mRegressionCurve=false;      //  再帰曲線の描画
    public boolean mDataValue=false;            //  データ値の表示
    public ArrayList<String> mStatisticsData;   //  統計計算用データ
    public ArrayList<Double> mStatisticsDataX;
    public ArrayList<Double> mStatisticsDataY;
    public int mDivideCount;

    public GraphView(Context context) {
        super(context);

        mC = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ydraw = new YDraw(mSurfaceHolder);
        ycalc = new YCalc();
        ylib = new YLib();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mWidth = getWidth();
        mHeight = getHeight();

        dispGraph();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    public void dispGraph() {
        initScreen(Color.LTGRAY);   //  グラフ領域の初期化
        setWorldArea(mWorldLeft, mWorldTop, mWorldRight, mWorldBottom);
        drawGraphAxis();            //  グラフの目盛りの表示
        if (mGraphType==GRAPHTYPE.SCATTERPLOTS) {
            drawScatterData();
            if (mRegressionCurve)
                drawRegressionCurve();
        } else if (mGraphType==GRAPHTYPE.HISTGRAM) {
            drawHistData();
        }
//        drawWSample();
    }

    /**
     * 表示データの設定
     * @param fileName  データファイル名
     */
    public void setData(String fileName) {
        mDataFileName = fileName;
        String path = mSaveStatisticsDirectory + "/" + fileName + ".csv";
        if (!loadStatisticsData(path))
            return;
        mStatisticsDataX = ycalc.getXlist(mStatisticsData);
        mStatisticsDataY = ycalc.getYlist(mStatisticsData);
    }

    public void setGraphArea(float left, float top, float right, float bottom) {
        mWorldLeft = left;
        mWorldTop = top;
        mWorldRight = right;
        mWorldBottom = bottom;
    }

    /**
     * グラフ領域の設定
     * @param left      左座標
     * @param top       上座標
     * @param right     右座標
     * @param bottom    下座標
     */
    public void setWorldArea(float left, float top, float right, float bottom) {
        float xmargine = (right - left) / 10;
        float ymargine = (top - bottom) / 10;
        mWorldLeft = left;
        mWorldTop = top;
        mWorldRight = right;
        mWorldBottom = bottom;
        ydraw.setWorldArea(left - xmargine, top + ymargine,
                right + xmargine, bottom - ymargine, false);
    }

    /**
     * ファイルからデータを取り込む
     * @param path  データファイル名
     * @return
     */
    private boolean loadStatisticsData(String path) {
        if (!ylib.existsFile(path)) {
//            Toast.makeText(this, "ファイルが存在していません\n"+path, Toast.LENGTH_LONG).show();
            return false;
        }
        //	ファイルデータの取り込み
        mStatisticsData = new ArrayList<String>();
        ylib.readTextFile(path, mStatisticsData);
        return true;
    }

    /**
     * グラフの枠と目盛りの表示
     */
    private void drawGraphAxis() {
        ydraw.lockCanvas();
        //  グラフの枠
        ydraw.setColor(Color.BLACK);
        ydraw.drawWRect(new RectF(mWorldLeft, mWorldTop, mWorldRight, mWorldBottom));

        //  目盛りと補助線
        ydraw.setTextSize(40f);
        ydraw.setColor(Color.BLUE);
        ydraw.drawWString(String.format("%.1f",mWorldLeft), new PointF(mWorldLeft, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        ydraw.drawWString(String.format("%.1f",mWorldBottom), new PointF(mWorldLeft, mWorldBottom), YDraw.TEXTALIGNMENT.RC);
        ydraw.drawWString(String.format("%.1f",mWorldRight), new PointF(mWorldRight, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        ydraw.drawWString(String.format("%.1f",mWorldTop), new PointF(mWorldLeft, mWorldTop), YDraw.TEXTALIGNMENT.RC);

        float dx = ylib.graphStepSize(mWorldRight-mWorldLeft,5);
        float x = (float)Math.floor(mWorldLeft / dx) * dx + dx;
        for ( ; x< mWorldRight; x += dx) {
            ydraw.drawWLine(new PointF(x, mWorldTop), new PointF(x, mWorldBottom));
            ydraw.drawWString(String.format("%.1f",x), new PointF(x, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        }
        float dy = ylib.graphStepSize(mWorldTop-mWorldBottom,5);
        float y = (float)Math.floor(mWorldBottom / dy) * dy + dy;
        for ( ; y< mWorldTop; y += dy) {
            ydraw.drawWLine(new PointF(mWorldLeft, y), new PointF(mWorldRight, y));
            ydraw.drawWString(String.format("%.1f",y), new PointF(mWorldLeft, y), YDraw.TEXTALIGNMENT.RC);
        }

        ydraw.unlockCanvasAndPost();
    }

    /**
     * 散布図のデータの表示
     */
    private void drawScatterData() {

        ydraw.lockCanvas();
        //  データの表示
        ydraw.setColor(Color.RED);
        ydraw.setPointSize(10);
        for (int i = 0; i < mStatisticsDataX.size(); i++) {
            ydraw.drawWPoint(new PointF(mStatisticsDataX.get(i).floatValue(), mStatisticsDataY.get(i).floatValue()));
        }

        if (mDataValue) {
            //  データ値の表示
            int dataSize = mStatisticsDataX.size();                 //  データ数
            double varienceX = ycalc.getVarSum(mStatisticsDataX);   //  分散(Varience)
            double varienceY = ycalc.getVarSum(mStatisticsDataY);   //  分散(Varience)
            double sdX = ycalc.getStdDev(mStatisticsDataX);         //  標準偏差(Standard deviation)
            double sdY = ycalc.getStdDev(mStatisticsDataY);         //  標準偏差(Standard deviation)
            double cavariance = ycalc.getCovarince(mStatisticsDataX,mStatisticsDataY);  //  共分散(Covariance)
            double correction = ycalc.getCorelation(mStatisticsDataX,mStatisticsDataY); //  相関係数(correlation coefficient)
            double regressionA = ycalc.getRegA(mStatisticsDataX,mStatisticsDataY);      //  回帰直線 傾き (linear regression)
            double regressionB = ycalc.getRegB(mStatisticsDataX,mStatisticsDataY);      //  回帰直線 切片 (linear regression)

            ydraw.setColor(Color.BLACK);
            float x = mWidth/10;
            float y = ydraw.getTextSize();
            ydraw.drawString("データ数: "+dataSize, x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("分散　　: X: "+String.format("%.3f",varienceX)+"  Y: "+String.format("%.3f",varienceY), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("標準偏差: "+String.format("X: %.3f  Y: %.3f",sdX,sdY), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("共分散　: "+String.format("%.3f",cavariance), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("相関係数: "+String.format("%.3f",correction), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("回帰直線(y = ax + b) a: "+String.format("%.3f",regressionA)+"  b: "+String.format("%.3f",regressionB), x, y);
        }

        ydraw.unlockCanvasAndPost();
    }

    /**
     * ヒストグラムのデータを標示
     */
    private void drawHistData() {
        int[] map = ycalc.getDataMap(mStatisticsDataX, mWorldLeft, mWorldRight,
                Integer.valueOf(mDivideCount));
        int maxCount = 0;
        for (int key : map)
            maxCount = maxCount < key ? key : maxCount;
        float dx = (mWorldRight - mWorldLeft) / mDivideCount;
        float x = 0;
        ydraw.lockCanvas();
        for (int count : map) {
            ydraw.setColor(Color.CYAN);
            ydraw.fillWRect(new RectF(x,0f,x+dx,(float)count));
            ydraw.setColor(Color.RED);
            ydraw.drawWRect(new RectF(x,0f,x+dx,(float)count));
            x += dx;
        }

        if (mDataValue) {
            //  データ値の表示
            int dataSize = mStatisticsDataX.size();                 //  データ数
            double average = ycalc.getListMean(mStatisticsDataX);   //  平均　中央値(メシアン) 最頻値(モード)
            double distribute = ycalc.getVarSum(mStatisticsDataX);  //  分散
            double divation = ycalc.getStdDev(mStatisticsDataX);    //  標準偏差

            ydraw.setColor(Color.BLACK);
            x = mWidth/10;
            float y = ydraw.getTextSize();
            ydraw.drawString("データ数: "+dataSize, x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("平均　　: "+String.format("%.3f",average), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("分散　　: "+String.format("%.3f",distribute), x, y);
            y += ydraw.getTextSize();
            ydraw.drawString("標準偏差: "+String.format("%.3f",divation), x, y);
        }

        ydraw.unlockCanvasAndPost();
    }

    /**
     * 散布図に回帰直線を標示
     */
    private void drawRegressionCurve() {
        //  回帰直線の傾きと切片を求める
        float a = (float)ycalc.getRegA(mStatisticsDataX, mStatisticsDataY);
        float b = (float)ycalc.getRegB(mStatisticsDataX, mStatisticsDataY);
        PointF ps = new PointF(mWorldLeft, a*mWorldLeft+b);
        PointF pe = new PointF(mWorldRight, a*mWorldRight+b);
        Log.d(TAG,"drawRegressionCurve: "+a+","+b+"  "+ps.x+","+ps.y+"  "+pe.x+","+pe.y);
        ydraw.lockCanvas();
        ydraw.setColor(Color.BLACK);
        ydraw.drawWLine(ps, pe);
        ydraw.unlockCanvasAndPost();
    }

    private void drawWSample() {
        ydraw.setWorldArea(0,300, 500,0, false);
        ydraw.lockCanvas();
        ydraw.setColor(Color.RED);
        ydraw.drawWLine(new PointF(0,0), new PointF(500,300));
        ydraw.drawWRect(new RectF(1, 299, 499, 1));
        ydraw.drawWCircle(new PointF(250,150), 20f);
        ydraw.drawWCircle(new PointF(50,50), 20f);
        ydraw.setTextSize(40);
        ydraw.drawWString("座標位置", new PointF(250,150), YDraw.TEXTALIGNMENT.CT);
        ydraw.unlockCanvasAndPost();
    }

    private void drawSample() {
        ydraw.lockCanvas();
        ydraw.setColor(Color.RED);
        ydraw.drawLine(0,0, 400, 400);
        ydraw.drawRect(0,0,400,400);
        ydraw.drawLine(-50,-50, 500, 500);
        ydraw.drawRect(-50,-50,549,549);
        ydraw.drawRect(new RectF(-20,-10,300,300));
        ydraw.setColor(Color.BLUE);
        ydraw.drawCircle(20, 10, 2);
        ydraw.fillCircle(20, 30, 2);
        ydraw.unlockCanvasAndPost();
    }

    /**
     * グラフ領域の初期化
     * @param backColor     背景色
     */
    private void initScreen(int backColor) {
        ydraw.setViewArea(mWidth, mHeight);
        ydraw.setWorldArea(0,0, mWidth, mHeight, true);
        ydraw.lockCanvas();
        ydraw.backColor(backColor);
        ydraw.unlockCanvasAndPost();
    }
}
