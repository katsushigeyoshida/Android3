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

public class FuncGraphView extends SurfaceView
        implements SurfaceHolder.Callback{

    private static final String TAG = "FuncGraphView";

    private Context mC = null;              //
    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private float mWorldLeft = 0;
    private float mWorldTop = 100;
    private float mWorldRight = 100;
    private float mWorldBottom = 0;
    private boolean mAspextFix = false;

    private YDraw ydraw;
    private YLib ylib;
    private ArrayList<PointF[]> mPlotDatas; //  表示用座標データ

    public FuncGraphView(Context context) {
        super(context);

        mC = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ydraw = new YDraw(mSurfaceHolder);
        ylib = new YLib();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mWidth = getWidth();
        mHeight = getHeight();

        dispGraph();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * グラフの座標データと表示領域を設定する
     * @param datas         グラフの座標データ
     * @param minX          Xの最小値
     * @param minY          Yの最小値
     * @param maxX          Xの最大値
     * @param maxY          Yの最大値
     * @param aspectFix     アスペクト比固定
     */
    public void setDatas(ArrayList<PointF[]> datas, double minX, double minY, double maxX, double maxY, boolean aspectFix) {
        mPlotDatas = datas;
        setGraphArea((float)minX, (float)maxY, (float)maxX, (float)minY, aspectFix);
    }

    /**
     *  グラフのワールド領域を設定する
     * @param left      左端
     * @param top       上端
     * @param right     右端
     * @param bottom    下端
     * @param aspectFix アスペクト比固定
     */
    public void setGraphArea(float left, float top, float right, float bottom, boolean aspectFix) {
        mWorldLeft = left;
        mWorldTop = top;
        mWorldRight = right;
        mWorldBottom = bottom;
        mAspextFix = aspectFix;
    }

    /**
     * グラフを描画する
     */
    public void dispGraph() {
        initScreen(Color.WHITE);   //  グラフ領域の初期化
        setWorldArea(mWorldLeft, mWorldTop, mWorldRight, mWorldBottom, mAspextFix);
        drawGraphAxis();            //  グラフの目盛りの表示
        drawDatas();                //  データの表示
    }

    /**
     * データから折線を表示
     */
    private void drawDatas() {
        if (mPlotDatas==null)
            return;
        if (mPlotDatas.size() < 1)
            return;
        Log.d(TAG,"drawDatas: "+mPlotDatas.size());
        ydraw.lockCanvas();
        for (int i = 0; i < mPlotDatas.size(); i++) {
            Log.d(TAG,"drawDatas: "+i+" "+mPlotDatas.get(i).length);
            PointF[] plotData = mPlotDatas.get(i);
            ydraw.setColor(ydraw.getColor15(i));
            for (int j = 0; j < plotData.length-1; j++) {
                if (!Double.isNaN(plotData[j].x) && !Double.isNaN(plotData[j].y) &&
                        !Double.isInfinite(plotData[j].x) && !Double.isInfinite(plotData[j].y) &&
                        !Double.isNaN(plotData[j+1].x) && !Double.isNaN(plotData[j+1].y) &&
                        !Double.isInfinite(plotData[j+1].x) && !Double.isInfinite(plotData[j+1].y))
                    Log.d(TAG,("drawDatas: "+plotData[j].x+" "+plotData[j].y));
                drawWLine(plotData[j], plotData[j+1]);
            }
        }
        ydraw.unlockCanvasAndPost();
    }

    /**
     * 線分をクリッピングして表示する(上下方向のクリッピングのみ)
     * 横方向は必ず領域に入っいることを前提とする
     * @param ps    始点
     * @param pe    終点
     */
    private void drawWLine(PointF ps, PointF pe) {
        //  両端点が入っているデータはそのまま表示
        if (mWorldBottom <= ps.y && ps.y <= mWorldTop && mWorldBottom <= pe.y && pe.y <= mWorldTop) {
            ydraw.drawWLine(ps, pe);
            return;
        }
        //  領域を通らない線は表示しない
        if ((ps.y < mWorldBottom && pe.y < mWorldBottom) || (mWorldTop < ps.y && mWorldTop < pe.y))
            return;

        //  領域を跨る線を上下でクリッピングする
        //  元データを変更するとそれが残るので一時変数に移してクリッピングする
        PointF tps;
        PointF tpe;
        if (ps.y < pe.y) {
            tps = new PointF(ps.x, ps.y);
            tpe = new PointF(pe.x, pe.y);
        } else {
            tpe = new PointF(ps.x, ps.y);
            tps = new PointF(pe.x, pe.y);
        }
        float a = (tpe.y - tps.y) / (tpe.x - tps.x);
        float b = tps.y - a * tps.x;
        if (tps.y < mWorldBottom) {
            tps.x = (mWorldBottom - b) / a;
            tps.y = mWorldBottom;
        }
        if (mWorldTop < tpe.y) {
            tpe.x = (mWorldTop - b) / a;
            tpe.y = mWorldTop;
        }
        ydraw.drawWLine(tps, tpe);
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
        ydraw.setTextSize(35f);
        ydraw.setColor(Color.BLUE);
        ydraw.drawWString(String.format("%.1f",mWorldLeft), new PointF(mWorldLeft, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        ydraw.drawWString(String.format("%.1f",mWorldBottom), new PointF(mWorldLeft, mWorldBottom), YDraw.TEXTALIGNMENT.RC);
        ydraw.drawWString(String.format("%.1f",mWorldRight), new PointF(mWorldRight, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        ydraw.drawWString(String.format("%.1f",mWorldTop), new PointF(mWorldLeft, mWorldTop), YDraw.TEXTALIGNMENT.RC);

        float dx = ylib.graphStepSize(mWorldRight-mWorldLeft,5);
        float x = (float)Math.floor(mWorldLeft / dx) * dx + dx;
        for ( ; x< mWorldRight; x += dx) {
            if (x==0)
                ydraw.setColor(Color.RED);
            else
                ydraw.setColor(Color.BLUE);
            ydraw.drawWLine(new PointF(x, mWorldTop), new PointF(x, mWorldBottom));
            ydraw.drawWString(String.format("%.1f",x), new PointF(x, mWorldBottom), YDraw.TEXTALIGNMENT.CT);
        }
        float dy = ylib.graphStepSize(mWorldTop-mWorldBottom,5);
        float y = (float)Math.floor(mWorldBottom / dy) * dy + dy;
        for ( ; y< mWorldTop; y += dy) {
            if (y==0)
                ydraw.setColor(Color.RED);
            else
                ydraw.setColor(Color.BLUE);
            ydraw.drawWLine(new PointF(mWorldLeft, y), new PointF(mWorldRight, y));
            ydraw.drawWString(String.format("%.1f",y), new PointF(mWorldLeft, y), YDraw.TEXTALIGNMENT.RC);
        }

        ydraw.unlockCanvasAndPost();
    }

    /**
     * グラフ領域の設定
     * @param left      左座標
     * @param top       上座標
     * @param right     右座標
     * @param bottom    下座標
     * @param aspectFix アスペクト固定
     */
    private void setWorldArea(float left, float top, float right, float bottom, boolean aspectFix) {
        float xmargine = (right - left) / 6;
        float ymargine = (top - bottom) / 10;
        mWorldLeft = left;
        mWorldTop = top;
        mWorldRight = right;
        mWorldBottom = bottom;
        ydraw.setWorldArea(left - xmargine, top + ymargine/3,
                right + xmargine/3, bottom - ymargine, aspectFix);
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
