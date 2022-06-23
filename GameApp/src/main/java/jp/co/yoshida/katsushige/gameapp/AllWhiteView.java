package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import jp.co.yoshida.katsushige.mylib.YDraw;
import jp.co.yoshida.katsushige.mylib.YGButton;

/**
 *   パズルゲーム「白にしろ」 第一学習社
 *   http://www.daiichi-g.co.jp/osusume/forfun/05_white/05.html
 *   【遊び方】
 *       １つのマスをクリックすると，そのマスと上下左右のマスの色が反転する。
 *       その作業を繰り返し，すべてのマスを白にしたらクリア。
 *       別の問題に挑戦したいときは，「次の問題へ」ボタンを押す。
 */
public class AllWhiteView extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = "AllWhiteView";

    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private long waitTime = 200;            //  問題作瀬パターン表示間隔(ms)

    private int mBoardSize = 3;             //  盤の数
    private float mBoardSizeRatio = 0.7f;   //  画面に対する盤の大きさの比
    private float mHaba;                    //  盤のマスの大きさ
    private float mOx,mOy;                  //  盤の原点
    private float mTextSize;                //  文字の大きさ
    private int mCount = 0;                 //  実施回数

    private YGButton ygbutton;

    public AllWhiteView(Context context) {
        super(context);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ygbutton = new YGButton(mSurfaceHolder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mWidth = getWidth();
        mHeight = getHeight();
        ygbutton.setOffScreen(mWidth, mHeight);
        mTextSize = ygbutton.getTextSize();

        initScreen();                       //  盤の初期化
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //  押されたボタン位置のマスを反転する
                int id = ygbutton.getButtonDownId(event.getX(), event.getY());
                if (0 <= id) {
                    reversBoard(id);     //  盤の反転
                    countMessage(mCount++);
                    //  全部白になったか確認する
                    if (ygbutton.getButtonDownCount() == 0) {
                        completeMessage("完了");
                    } else {
                        completeMessage("　　");
                        axisTitle();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 盤の大きさの設定と画面のイニシャライズ
     * @param boardSize
     */
    public void setParameter(int boardSize) {
        mBoardSize = boardSize;
        initScreen();
    }

    /**
     * 盤の状態を取得する
     * @return
     */
    public int[][] getBoard() {
        int[][] board = new int[mBoardSize][mBoardSize];
        for (int x = 0; x < mBoardSize; x++) {
            for (int y = 0; y < mBoardSize; y++) {
                board[y][x] = ygbutton.getGButtonDownState(x * 100 + y)?1:0;
            }
        }
        return board;
    }

    /**
     * 初期画面の作成
     */
    public void initScreen() {
        //  盤の大きさと位置
        mHaba = mWidth * mBoardSizeRatio / mBoardSize;
        mOx = (mWidth * (1 - mBoardSizeRatio) + mHaba) / 2f;
        mOy = (mWidth * (1 - mBoardSizeRatio) + mHaba) / 2f;
        //  盤をグラフィックボタンで作成
        ygbutton.clearButtons();
        for (int x = 0; x < mBoardSize; x++) {
            for (int y = 0; y < mBoardSize; y++) {
                ygbutton.addGButton(x * 100 + y, YGButton.BUTTONTYPE.RECT,
                        (float)x * mHaba + mOx,(float)y * mHaba + mOy,0,mHaba,mHaba);
                ygbutton.setGButtonBorderWidth(x * 100 + y, 3f);

            }
        }
        mCount = 1;
        countMessage(mCount);
        //  盤の表示
        ygbutton.lockCanvas();
        ygbutton.backColor(Color.WHITE);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();

        axisTitle();
    }

    /**
     * 盤のボタンの反転
     * 指定ボタンとその周囲(上下左右)のボタンを反転する
     * @param id
     */
    private void reversBoard(int id) {
        int x, y;
        //  ボタンが押されたときの色表示
        ygbutton.lockCanvas();
        ygbutton.GButtonDownRevers(id);    //  反転表示
        x = id / 100 ;
        y = id % 100 - 1;
        if (0 <= x && x < mBoardSize &&0 <= y && y < mBoardSize)
            ygbutton.GButtonDownRevers(x*100+y);    //  反転表示
        x = id / 100 ;
        y = id % 100 + 1;
        if (0 <= x && x < mBoardSize &&0 <= y && y < mBoardSize)
            ygbutton.GButtonDownRevers(x*100+y);    //  反転表示
        x = id / 100 - 1;
        y = id % 100;
        if (0 <= x && x < mBoardSize &&0 <= y && y < mBoardSize)
            ygbutton.GButtonDownRevers(x*100+y);    //  反転表示
        x = id / 100 + 1;
        y = id % 100;
        if (0 <= x && x < mBoardSize &&0 <= y && y < mBoardSize)
            ygbutton.GButtonDownRevers(x*100+y);    //  反転表示
        ygbutton.unlockCanvasAndPost();

    }

    /**
     * 問題の作成
     * 全白状態からランダムにマスの反転を行って作る
     */
    public void createProblem(int n) {
        int x, y;
        initScreen();
        for ( int i = 0; i < n; i++) {
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
            }

            x = (int) Math.floor(Math.random() * mBoardSize);
            y = (int) Math.floor(Math.random() * mBoardSize);
            reversBoard(x * 100 + y);
        }
    }

    /**
     * 盤の行と列の番号を表示
     */
    private void axisTitle() {
        ygbutton.setTextSize(mTextSize * 4f);
        ygbutton.lockCanvas();
        for (int x = 0; x < mBoardSize; x++) {
            ygbutton.drawStringWithBack("" + x, (float)x * mHaba + mOx, mOy - mHaba/2 - (mTextSize * 6f),
                    YDraw.TEXTALIGNMENT.CT, Color.BLACK, Color.WHITE );
            ygbutton.drawStringWithBack("" + x, mOx - mHaba/2 - (mTextSize * 4f) , (float)x * mHaba + mOy ,
                    YDraw.TEXTALIGNMENT.CT, Color.BLACK, Color.WHITE );
        }
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 実施回数を表示
     * @param n
     */
    private void countMessage(int n) {
        float x = mWidth / 2;
        float y = mOy + mHaba * (mBoardSize - 0.5f) + 20f;
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mTextSize * 4f);
        ygbutton.drawStringWithBack("回数 : " + n, x, y, YDraw.TEXTALIGNMENT.CT, Color.BLACK, Color.WHITE );
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * メッセージの表示
     * @param text
     */
    private void completeMessage(String text) {
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mTextSize * 6f);
        ygbutton.drawStringCenterWithBack(text, mWidth / 2, ygbutton.getTextSize(),Color.RED, Color.WHITE);
        ygbutton.unlockCanvasAndPost();
    }
}
