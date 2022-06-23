package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YGButton;

public class BlockGameView extends SurfaceView
        implements SurfaceHolder.Callback,Runnable {

    private static final String TAG = "BlockGameView";

    private float WIDTH = 640f;             //  描画領域
    private float HEIGHT = 480f;
    private int[] colortable = {Color.RED, Color.YELLOW, Color.GREEN};

    private float br = 10;                  //  ボールの半径
    private float paddlew = 96;             //  パドルの幅
    private float paddleh = 16;             //  パドルの高さ
    private float blockw = 54;              //  ブロックの幅
    private float blockh = 24;              //  ブロックの高さ
    private RectF mBlocksBack;              //  ブロックの全体領域
    private boolean endflag = false;
    private boolean gameover = false;
    private int mWinCount = 0;

    private float ballx;                    //  ボールのX座標
    private float bally;                    //  ボールのY座標
    private float reBallx;                  //  ボールの前X座標
    private float reBally;                  //  ボールの前Y座標
    private float bx1;                      //  ボールの速度
    private float by1;                      //  ボールの速度
    private RectF paddle;                   //  パドルの座標
    private RectF prePaddle;                //  パドルの前回位置
    private ArrayList<RectF> blocks;        //  ブロックの座標
    private float mBullAccell = 1.015f;     //  ボールがブロックにあたった時の速度アップの割合

    private float mButtonRad;               //  グラフィックボタンの半径
    private float mLButtonX;                //  左ボタンのx座標
    private float mLButtonY;                //  左ボタンのy座標
    private float mRButtonX;                //  右ボタンのx座標
    private float mRButtonY;                //  右ボタンのy座標
    private float mRetryButtonX;
    private float mRetryButtonY;
    enum BUTTONTOUCH {LEFT,NON,RIGHT,RETRY};
    private BUTTONTOUCH mButtonTouch;       //  タッチしたボタンの位置
    private int mBackColor = Color.BLUE;    //  背景色

    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private float mAspect = 0.75f;          //  描画領域縦横比
    private float mAreaRate;                //  画面サイズに対する描画比率
    private float mTextSize;                //  文字サイズ

    private Thread thread;
    private boolean initflag = true;
    private boolean doing = false;
    private long waitTime = 0;
    private Context c;
    private YGButton ygbutton;

    public BlockGameView(Context context) {
        super(context);
        c = context;

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ygbutton = new YGButton(mSurfaceHolder);
        mTextSize = ygbutton.getTextSize();
        mButtonTouch = BUTTONTOUCH.NON;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mWidth = getWidth();
        mHeight = getHeight();

        initParameter();

        ygbutton.setOffScreen(mWidth, mHeight);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //  グラフィックボタンが押されたときに設定
                int id = ygbutton.getButtonDownId(event.getX(), event.getY());
                if (id < 0)
                    mButtonTouch = BUTTONTOUCH.NON;
                else {
                    BUTTONTOUCH[] values = BUTTONTOUCH.values();
                    mButtonTouch = values[id];
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                if (initflag) {
                    initParameter();
                    screenClear();
                    messageOut("左右に傾けるとパドルが移動します");
                    drawGame();
                    initflag = false;
                    doing = true;
                }
                if (doing) {
                    accelCheck();
                    drawGame();
                    if (gameover) {
                        doing = false;
                        GameOver("Game Over !! うひひ!!");
                    }
                    if (blocks.size()==0) {
                        doing = false;
                        mWinCount++;
                        GameOver("Your Win !! まあまあかな");
                    }
                }
                keyCheck();
                if (waitTime > 0) {
                    //  表示速度(静止時間)
                    Thread.sleep(waitTime);
                }
            } catch (Exception e) {
                Log.d(TAG,"Exception: "+e.getMessage());
                initflag = false;
            }
        }
    }

    /**
     * 各種パラメータの設定
     */
    private void initParameter() {
        Log.d(TAG,"initParameter");
        mAreaRate = mWidth / WIDTH;
        //  画面枠
        WIDTH = mWidth;
        HEIGHT = WIDTH * mAspect;

        //  パドルの大きさ
        paddlew *= mAreaRate;
        paddleh *= mAreaRate;
        //  パドルの位置
        float x = WIDTH / 2f;
        float y = HEIGHT * 9f / 10f - paddleh * 3f * mWinCount;
        paddle = new RectF(x - (paddlew/2), y - (paddleh/2), x + (paddlew/2), y + (paddleh/2));
        prePaddle = new RectF(paddle);

        //  ブロックの大きさ
        blockw *= mAreaRate;
        blockh *= mAreaRate;
        //  ブロックと壁との隙間
        float blockMargin = (WIDTH - (blockw + 4) * 10) / 2f;
        //  ブロックの位置
        blocks = new ArrayList();
        for (int i = 0; i < 50; i++) {
            x = (i % 10) * (blockw + 4) + blockMargin;
            y = (int)(i / 10) * (blockh + 4) + blockMargin;
            blocks.add(new RectF(x, y, x + blockw, y + blockh));
        }
        //  ブロック全体の領域
        mBlocksBack = new RectF(blocks.get(0).left, blocks.get(0).top,
                blocks.get(blocks.size()-1).right, blocks.get(blocks.size()-1).bottom);

        //  ボールの大きさ
        br *= mAreaRate;

        //  ボタンの位置と大きさ
        mButtonRad = mWidth / 12f;
        mLButtonX = mButtonRad*1.5f;
        mLButtonY = mHeight - mButtonRad*1.5f;
        mRButtonX = mWidth - mButtonRad*1.5f;
        mRButtonY = mHeight - mButtonRad*1.5f;
        mRetryButtonX = mWidth / 2f;
        mRetryButtonY= mHeight - mButtonRad*1.5f;

        ygbutton.clearButtons();
        ygbutton.addGButton(BUTTONTOUCH.LEFT.ordinal(),YGButton.BUTTONTYPE.CIRCLE, mLButtonX, mLButtonY, mButtonRad,0,0);
        ygbutton.addGButton(BUTTONTOUCH.RIGHT.ordinal(),YGButton.BUTTONTYPE.CIRCLE, mRButtonX, mRButtonY, mButtonRad,0,0);
        ygbutton.addGButton(BUTTONTOUCH.RETRY.ordinal(),YGButton.BUTTONTYPE.CIRCLE, mRetryButtonX, mRetryButtonY, mButtonRad,0,0);
        ygbutton.setGButtonTitleColor(BUTTONTOUCH.LEFT.ordinal(), Color.BLACK);
        ygbutton.setGButtonTitleColor(BUTTONTOUCH.RIGHT.ordinal(), Color.BLACK);
        ygbutton.setGButtonTitleColor(BUTTONTOUCH.RETRY.ordinal(), Color.BLACK);
        ygbutton.setGButtonTitle(BUTTONTOUCH.LEFT.ordinal(), "左");
        ygbutton.setGButtonTitle(BUTTONTOUCH.RIGHT.ordinal(), "右");
        ygbutton.setGButtonTitle(BUTTONTOUCH.RETRY.ordinal(), "再開");

    }

    /**
     * 画面のクリア
     */
    private void screenClear() {
        Log.d(TAG,"screenClear");
        //  パドルの位置
        float x = WIDTH / 2f;
        float y = HEIGHT * 9f / 10f - paddleh * 3f * mWinCount;
        paddle.set(x - (paddlew/2), y - (paddleh/2), x + (paddlew/2), y + (paddleh/2));
        prePaddle.left = paddle.left;
        prePaddle.right = paddle.right;

        //  ボールの位置
        ballx = WIDTH / 2f;
        bally = (paddle.top + blocks.get(blocks.size()-1).bottom) / 2f;
        reBallx = ballx;
        reBally = bally;
        //  ボールの移動量
        bx1 = 4f * mAreaRate;
        by1 = -5f * mAreaRate;

        waitTime = 15;

        //  初期描画
        ygbutton.lockCanvas();
        ygbutton.setColor(Color.LTGRAY);
        ygbutton.fillRect(0, 0, mWidth, mHeight);
        ygbutton.setColor(mBackColor);
        ygbutton.fillRect(0, 0, WIDTH, HEIGHT);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * ゲーム画面の表示と更新
     */
    private void drawGame() {
        ballMoveCalc();
        ygbutton.lockCanvas();
        drawBlocks();
        drawPaddle();
        drawBall();
        drawGButton();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * ブロックの表示
     */
    private void drawBlocks() {
        ygbutton.setColor(mBackColor);
        ygbutton.fillRect(mBlocksBack);
        for (int i = 0; i < blocks.size(); i++) {
            ygbutton.setColor(colortable[(int)(blocks.get(i).top / 28) % 3]);
            ygbutton.fillRect(blocks.get(i));
        }
    }

    /**
     * パドルの表示
     */
    private void drawPaddle() {
        ygbutton.setColor(mBackColor);
        ygbutton.fillRect(prePaddle);
        ygbutton.setColor(Color.WHITE);
        ygbutton.fillRect(paddle);
        prePaddle.left = paddle.left;
        prePaddle.right = paddle.right;
    }

    /**
     * ボールの表示
     */
    private void drawBall() {
        ygbutton.setColor(mBackColor);
        ygbutton.fillCircle(reBallx,reBally, br);
        ygbutton.setColor(Color.WHITE);
        ygbutton.fillCircle(ballx,bally, br);
        reBallx = ballx;
        reBally = bally;
    }

    /**
     *  操作によるによるパドルの移動
     */
    private void keyCheck() {
        if (mButtonTouch == BUTTONTOUCH.LEFT) {
            paddle.left -= paddlew / 2f;
            paddle.right -= paddlew / 2f;
        } else if (mButtonTouch == BUTTONTOUCH.RIGHT) {
            paddle.left += paddlew / 2f;
            paddle.right += paddlew / 2f;
        } else if (mButtonTouch == BUTTONTOUCH.RETRY) {
            initflag = true;
            gameover = false;
        } else {
            return;
        }
        //  パドルが領域を越えた時に元に戻す
        if (paddle.left <= 0) {
            paddle.left = 0;
            paddle.right = paddle.left + paddlew;
        } else if (WIDTH <= paddle.right) {
            paddle.right = WIDTH;
            paddle.left = paddle.right - paddlew;
        }
        mButtonTouch = BUTTONTOUCH.NON;
    }

    /**
     * グラフィックボタンの押下表示
     */
    private void drawGButton() {
        if (mButtonTouch == BUTTONTOUCH.NON)
            return;
        ygbutton.drawGButtonsDown(mButtonTouch.ordinal());
    }

    /**
     * ボールの移動計算
     */
    private void ballMoveCalc() {
        float x = ballx + bx1;
        float y = bally + by1;
        //  画面枠に対してのボールの反射
        if (x < br || x > (WIDTH- br))
            bx1 = -bx1;
        if (y < br)
            by1 = -by1;
        if (y > HEIGHT)
            gameover = true;
        //  パドルの中心とのボールとの距離
        float dx = paddle.centerX() - x;
        float dy = paddle.centerY() - y;
        //  パドルに対してのボールの反射
        if (dy == 0)
            dy = 1f;
        if (Math.abs(dx) < (paddlew / 2f + br) && Math.abs(dy) < (paddleh / 2f + br)) {
            //  パドルの上下か左右の判定
            if (Math.abs(dx / dy) > (paddlew / paddleh)) {
                //  左右にあたる
                bx1 = -bx1;
                ballx = paddle.centerX() * Math.signum(dx) * (paddlew / 2f + br);
            } else {
                //  上下にあたる
                by1 = -by1;
                bally = paddle.centerY()  - Math.signum(dy) * (paddleh / 2f + br);
            }
        }
        //  ブロックに対してのボールの反射
        for (int i = 0; i < blocks.size(); i++) {
            dx = blocks.get(i).centerX() - x;
            dy = blocks.get(i).centerY() - y;
            if (dy == 0)
                dy = 1;
            //  ボールがブロックにあたった時
            if (Math.abs(dx) < (blockw / 2f + br) && Math.abs(dy) < (blockh / 2f + br)) {
                //  ブロックの上下か左右化の判定
                if (Math.abs(dx / dy) > (blockw / blockh)) {
                    //  左右にあたる
                    bx1 = -bx1;
                    ballx = blocks.get(i).centerX() - Math.signum(dx) * (blockw / 2f + br);
                } else {
                    //  上下にあたる
                    by1 = -by1;
                    bally = blocks.get(i).centerY() - Math.signum(dy) * (blockh / 2f + br);
                }
                blocks.remove(i);           //  ブロックを減らす
                bx1 *= mBullAccell;         //  ボールの速度アップ
                by1 *= mBullAccell;
                if (1 < waitTime)
                    waitTime -= 1;
                break;
            }
        }

        ballx += bx1;
        bally += by1;
    }

    /**
     * 下部にメッセージを表示
     * @param msg
     */
    private void messageOut(String msg) {
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mTextSize * 3f);
        float textLength = ygbutton.measureText(msg);
        ygbutton.setColor(Color.RED);
        ygbutton.drawString(msg, (WIDTH - textLength) / 2f, HEIGHT * 1.1f);
        ygbutton.unlockCanvasAndPost();
    }


    /**
     * ゲームし終了処理
     * @param msg
     */
    private void GameOver(String msg) {
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mTextSize * 4f);
        float textLength = ygbutton.measureText(msg);
        ygbutton.setColor(Color.RED);
        ygbutton.drawString(msg, (WIDTH - textLength) / 2f, HEIGHT / 2f);
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 傾きでパドルを動かす(加速度センサーを使う)
     */
    private void accelCheck() {
        float accelPara = Math.abs(BlockGame.accel_x) / 9.81f;
        if (BlockGame.mAccelCount == 1) {
            paddle.left -= paddlew / 2f * accelPara;
            paddle.right -= paddlew / 2f * accelPara;
        } else if (BlockGame.mAccelCount == -1){
            paddle.left += paddlew / 2f  * accelPara;
            paddle.right += paddlew / 2f  * accelPara;
        } else {
            return;
        }
        BlockGame.mAccelCount = 0;

        //  パドルが領域を越えた時に元に戻す
        if (paddle.left <= 0) {
            paddle.left = 0;
            paddle.right = paddle.left + paddlew;
        } else if (WIDTH <= paddle.right) {
            paddle.right = WIDTH;
            paddle.left = paddle.right - paddlew;
        }
    }
}
