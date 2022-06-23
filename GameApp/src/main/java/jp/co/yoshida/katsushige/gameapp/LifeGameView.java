package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YGButton;

public class LifeGameView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "LifeGameView";

    private int mWidth;                 //	描画領域の幅
    private int mHeight;                //	描画領域の高さ
    private float mTextSize;

    private float mOx = 0f;             //  盤の原点
    private float mOy = 0f;
    private int mYoko = 50;             //  盤の大きさ
    private int mTate = 72;
    private float mHaba;                //  １個の大きさ
    private int mGenerateMax = 1000;    //  最大世代数
    private boolean mDisp[][] = new boolean[mYoko+2][mTate+2];  //  表示
    private int mGen[][] = new int[mYoko+2][mTate+2];           //  世代数
    private ArrayList<int[]> mPatterns; //  初期パターン
    ArrayList<String> mPatternName;
    int mPatternNo = 0;

    private int mPatternCellX = 11;     //  パターン作成の一辺のセルの数
    private int mPatternCellY = 11;     //  パターン作成の一辺のセルの数
    private float mPatternHaba;         //  パターン作成のセルの幅

    private SurfaceHolder surfaceHolder;
    private Thread thread = null;
    private boolean initflag = true;
    private boolean doing = false;
    private boolean stopping = true;
    private long waitTime = 300;
    YGButton ygbutton;

    public LifeGameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        ygbutton = new YGButton(surfaceHolder);
        //  初期パターンの登録
        mPatternName = new ArrayList<String>();
        int[] pattern00 = {0,0,-1,0,1,0,0,-1,-1,1};
        int[] pattern01 = {0,-5,0,-4,0,-3,0,-1,0,0,0,1,0,2,0,3,0,4,0,5};
        int[] pattern02 = {0,-5,0,-4,0,-3,0,-2,0,-1,0,0,0,1,0,2,1,2,2,2};
        int[] pattern03 = {-3,1,-2,-1,-2,1,0,0,1,1,2,1,3,1};
        int[] pattern04 = {-4,0,-3,0,-3,1,1,1,2,-1,2,1,3,1};
        mPatterns = new ArrayList<int[]>();
        mPatterns.add(pattern00);
        mPatternName.add("ペントミノ");
        mPatterns.add(pattern01);
        mPatternName.add("棒状");
        mPatterns.add(pattern02);
        mPatternName.add("Ｌ形");
        mPatterns.add(pattern03);
        mPatternName.add("どんぐり");
        mPatterns.add(pattern04);
        mPatternName.add("ダイハード");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        //  パラメータの初期設定
        mWidth = width;
        mHeight = height;
        ygbutton.setOffScreen(mWidth, mHeight);
        Paint paint = new Paint();
        mTextSize = paint.getTextSize() * 2f;
        ygbutton.setTextSize(mTextSize);
        //  盤のメッシュの幅
        if (mWidth / (float)mYoko < mHeight / (float)mTate)
            mHaba = mWidth / (float)mYoko;
        else
            mHaba = (mHeight - mTextSize*2f) / (float)mTate;
        //  盤の原点
        mOx = (mWidth - mHaba * mYoko) / 2f;
        mOy = (mHeight - mHaba * mTate) / 2f;
        //  パターン作成のメッシュの幅
        mPatternHaba = mWidth / mPatternCellX;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread = null;
    }

    @Override
    public void run() {
        int loopCount = 1;      //  試験のン回数

        while (thread != null) {
            try {
                if (initflag) {
                    //  初期描画
                    ygbutton.lockCanvas();
                    loopCount = 1;
                    screenInit();
                    ygbutton.unlockCanvasAndPost();
                    initflag = false;
                }
                if (doing) {
                    //  シミュレーションの実行
                    do {
                        if (doing) {
                            ygbutton.lockCanvas();
                            if (0 < drawSlowly())
                                drawGeneration(loopCount++);
                            else
                                stopping = true;
                            ygbutton.unlockCanvasAndPost();
//                            if (waitTime > 0 && (loopCount % 10) == 0) {
                            //  表示速度(静止時間)
                            Thread.sleep(waitTime);
//                            }
                        }
                    } while (loopCount <= mGenerateMax && !stopping);
                    ygbutton.unlockCanvasAndPost();
                    doing = false;
                }
                if (waitTime > 0) {
                    //  表示速度(静止時間)
                    Thread.sleep(waitTime);
                }
            } catch (Exception e) {
            }
        }
    }
    /**
     * シミュレーション開始設定
     */
    public void start() {
        initflag = true;
        doing = true;
        stopping = false;
    }

    /**
     * シミュレーション終了
     */
    public void end() {
        stopping = true;
        doing = false;
    }

    /**
     * シミュレーション再開設定
     */
    public void restart() {
        doing = true;
    }

    /**
     * シミュレーション中断設定
     */
    public void stop() {
        doing = false;
    }

    /**
     * アニメーション表紙速度設定
     *
     * @param wait 待ち時間゛(ms)
     */
    public void setWaitTime(int wait) {
        waitTime = wait;
    }


    /**
     * 初期パターンを設定する
     * @param pattern
     */
    public void setParameter(int pattern) {
        mPatternNo= pattern;
        initflag = true;
    }

    /**
     * 初期画面の作成
     */
    private void screenInit() {
        //  盤の初期化
        for (int i = 0; i < mYoko; i++) {
            for (int j = 0; j < mTate; j++) {
                mDisp[i][j] = false;
                mGen[i][j] = 0;
            }
        }
        //  初期パターンの取得
        int[] pattern = mPatterns.get(mPatternNo);
        for (int i = 0; i < pattern.length; i+=2) {
            mDisp[mYoko/2+pattern[i]][mTate/2+pattern[i+1]] = true;
        }
        //  表示
        ygbutton.backColor(Color.LTGRAY);
        drawCells();
    }

    /**
     * ライフゲームの実行
     */
    private int drawSlowly() {
        //  生命体のあるセルの周りのセルの世代を一つ上げる
        for (int i = 1; i <= mYoko; i++) {
            for (int j = 1; j <= mTate; j++) {
                if (mDisp[i][j]) {
                    mGen[i-1][j-1]++; mGen[i-1][j]++; mGen[i-1][j+1]++;
                    mGen[i][j-1]++;                   mGen[i][j+1]++;
                    mGen[i+1][j-1]++; mGen[i+1][j]++; mGen[i+1][j+1]++;
                }
            }
        }
        //  世代が３だったら表示し、世代をクリアする
        for (int i = 0; i <= mYoko+1; i++) {
            for (int j = 0; j <= mTate+1; j++) {
                if (mGen[i][j] != 2)
                    mDisp[i][j] = (mGen[i][j]==3);
                mGen[i][j] = 0;
            }
        }

        return drawCells();
    }

    /**
     * 一画面の表示
     */
    private int drawCells() {
        int count = 0;
        ygbutton.setColor(Color.WHITE);
        ygbutton.fillRect(mOx, mOy , mHaba*mYoko,mHaba*mTate);
        for (int i = 0; i < mYoko; i++) {
            for (int j = 0; j < mTate; j++) {
                if (mDisp[i][j]) {
                    ygbutton.setColor(Color.RED);
                    ygbutton.fillRect(mOx + mHaba * i, mOy + mHaba * j, mHaba, mHaba);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 世代を表示
     * @param i
     */
    private void drawGeneration(int i) {
        ygbutton.drawStringWithBack(String.format("%5d",i),mWidth/2, mHeight, Color.BLACK, Color.CYAN);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int id = ygbutton.getButtonDownId(event.getX(), event.getY());
                if (0 <= id) {
                    //  ボタンが押されたときの色表示
                    ygbutton.lockCanvas();
                    ygbutton.GButtonDownRevers(id);    //  反転表示
                    ygbutton.unlockCanvasAndPost();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初期パターンの作成
     */
    public void creatPattern() {
        ygbutton.clearButtons();
        for (int x = 0; x < mPatternCellX; x++) {
            for (int y = 0; y < mPatternCellY; y++) {
                ygbutton.addGButton(x*100+y, YGButton.BUTTONTYPE.RECT,
                        x*mPatternHaba+mPatternHaba/2f,y*mPatternHaba+mPatternHaba/2,0,mPatternHaba,mPatternHaba);
            }
        }
        ygbutton.lockCanvas();
        ygbutton.backColor(Color.LTGRAY);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 初期パターンの作成完了と登録
     */
    public void completePattern() {
        //  ボタンダウン設定値の取り出し
        ArrayList<Integer> patternList = new ArrayList<Integer>();
        for (int x = 0; x < mPatternCellX; x++) {
            for (int y = 0; y < mPatternCellY; y++) {
                if (ygbutton.getGButtonDownState(x*100+y)) {
                    patternList.add(x - mPatternCellX/2);
                    patternList.add(y - mPatternCellY/2);
                }
            }
        }
        if (patternList.size() <= 0)
            return;
        //  パターンの登録
        int[] pattern = new int[patternList.size()];
        for (int i = 0; i < pattern.length; i++)
            pattern[i] = patternList.get(i);
        mPatterns.add(pattern);
        mPatternName.add("ユーザー "+mPatternName.size());
        mPatternNo = mPatterns.size()-1;
    }
}
