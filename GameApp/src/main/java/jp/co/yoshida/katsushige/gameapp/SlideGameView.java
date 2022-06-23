package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import jp.co.yoshida.katsushige.mylib.YDraw;
import jp.co.yoshida.katsushige.mylib.YGButton;

public class SlideGameView extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = "SlideGameView";

    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private YGButton ygbutton;
    private long waitTime = 100;

    private int mBoardSize = 3;             //  盤の数
    private float mBoardSizeRatio = 0.8f;   //  画面に対する盤の大きさの比
    private float mHaba;                    //  盤のマスの大きさ
    private float mOx,mOy;                  //  盤の原点
    private float mTextSize;                //  文字の大きさ
    private int mCount = 0;                 //  実施回数
    String[] mNumber = {"*","1","2","3","4","5","6","7","8","9",
            "10","11","12","13","14","15","16","17","18","19",
            "20","21","22","23","24","25","26","27","28","29",
    };

    public SlideGameView(Context context) {
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

        initScreen(0);                       //  盤の初期化
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
                    if (!chkBlank(id)) {
                        //  ブランクボタンでなければ前後左右のブランクボタンをチェックする
                        int id2 = getBlankId(id);
                        //  前後左右にブランクボタンがあればブランクボタンと入れ替えをする
                        if (0 <= id2) {
                            swapTitle(id, id2);
                            countMessage(mCount++);
                            if (completeChk())
                                completeMessage("完成");
                            else
                                completeMessage("　　");
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 盤が完成したどうかを判定する
     * @return      true/false
     */
    private boolean completeChk() {
        int i=1;
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                int id = x * 100 + y;
                Log.d(TAG, "completeChk: " + mNumber[i] + "  id= " + id + " " + ygbutton.getGButtonTitle(id));
                if (i < mBoardSize * mBoardSize) {
                    if (ygbutton.getGButtonTitle(id).compareTo(mNumber[i++]) != 0)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * 前後左右のブランクボタンの有無をチェックする
     * @param id    指定のボタンのID
     * @return      ブランクボタンのID,-1のときはブランクボタンなし
     */
    private int getBlankId(int id) {
        int[] rel = {-1,1,-100,100};
        for (int i = 0; i < rel.length; i++) {
            int id2 = id + rel[i];
            int x = id2 / 100;
            int y = id2 % 100;
            if (0 <= x && x < mBoardSize)
                if (0 <= y && y < mBoardSize) {
                    Log.d(TAG,"getBlankId: id1= "+id2+"  id="+id);
                    if (chkBlank(id2)) {
                        return id2;
                    }
                }
        }
        return -1;
    }

    /**
     * ボタンのタイトルを入れ替える
     * @param id1
     * @param id2
     */
    private void swapTitle(int id1, int id2) {
        String txt1 = ygbutton.getGButtonTitle(id1);
        String txt2 = ygbutton.getGButtonTitle(id2);
        ygbutton.lockCanvas();
        ygbutton.setGButtonTitle(id1, txt2);
        ygbutton.setGButtonTitle(id2, txt1);
        ygbutton.drawGButton(id1);
        ygbutton.drawGButton(id2);
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 指定したIDに移動できるIDのリストを取得
     * @param id        IDの配列リスト(移動できない場所は-1を設定)
     * @return
     */
    private int[] getNextPosition(int id) {
        int x = id / 100;
        int y = id % 100;
        int[] nextId = {-1,-1,-1,-1};
        int i=0;
        if (0 < x)
            nextId[i++] = (x-1) * 100 + y;
        if (x < mBoardSize - 1)
            nextId[i++] = (x+1) * 100 + y;
        if (0 < y)
            nextId[i++] = x * 100 + y - 1;
        if (y < mBoardSize - 1)
            nextId[i++] = x * 100 + y + 1;
        return nextId;
    }

    /**
     * 指定のボタンがブランクボタンかどうかを確認する
     * @param id    ボタンのID
     * @return      ブランクボタンの時はtrue
     */
    private boolean chkBlank(int id) {
        if (ygbutton.getGButtonTitle(id).compareTo("*")==0)
            return true;
        else
            return false;
    }

    /**
     * 盤の大きさの設定と画面のイニシャライズ
     * @param boardSize
     */
    public void setParameter(int boardSize) {
        mBoardSize = boardSize;
        initScreen(0);
    }

    /**
     * 盤の状態を取得する(ブランク[*]は[0]に変換)
     * @return
     */
    public byte[] getBoard() {
        byte[] board = new byte[mBoardSize * mBoardSize];
        int i=0;
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                int id = x * 100 + y;
                String title = ygbutton.getGButtonTitle(id);
                if (title.compareTo("*")==0) {
                    board[i++] = (byte)0;
                } else {
                    board[i++] = Byte.valueOf(ygbutton.getGButtonTitle(id));
                }
            }
        }
        return board;
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
     * @param text      表示文字列
     */
    private void completeMessage(String text) {
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mTextSize * 6f);
        ygbutton.drawStringCenterWithBack(text, mWidth / 2, ygbutton.getTextSize()-5, Color.RED, Color.WHITE);
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 動作をシミュレーションして問題を作成する
     * @param count     移動回数
     */
    private void createProblem(int count) {
        int prevId=-1;
        for (int i=0; i<count; i++) {
            int id = ygbutton.getGbuttonTitleId("*");  //  ブランクの位置
            int[] nextIds = getNextPosition(id);    //  ブランクの位置から移動できるブロックの位置のリストを求める
            int nextId;
            do {//  移動できるブロックからランダムに一つ選択する
                nextId = nextIds[(int) (Math.random() * nextIds.length)];
            } while (nextId == prevId || nextId < 0);
            swapTitle(id, nextId);
            prevId = id;
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 盤を初期化する(完成形にする)
     */
    private void initBoard() {
        //  盤をグラフィックボタンで作成
        ygbutton.clearButtons();
        //  盤の完成状態を作成
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                ygbutton.addGButton(x * 100 + y, YGButton.BUTTONTYPE.RECT,
                        (float)x * mHaba + mOx,(float)y * mHaba + mOy,0,mHaba,mHaba);
                ygbutton.setGButtonBorderWidth(x * 100 + y, 3f);
                ygbutton.setGButtonTitleColor(x * 100 + y, Color.BLACK);
                if (x== mBoardSize -1 && y== mBoardSize -1)
                    ygbutton.setGButtonTitle(x * 100 + y, mNumber[0]);
                else
                    ygbutton.setGButtonTitle(x * 100 + y, mNumber[y* mBoardSize +x+1]);
            }
        }
        ygbutton.lockCanvas();
        ygbutton.backColor(Color.WHITE);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 初期画面の作成
     */
    public void initScreen(int count) {
        //  盤の大きさと位置
        mHaba = mWidth * mBoardSizeRatio / mBoardSize;
        mOx = (mWidth * (1 - mBoardSizeRatio) + mHaba) / 2f;
        mOy = (mWidth * (1 - mBoardSizeRatio) + mHaba) / 2f;

        initBoard();

        //  問題を作成する
        if (0 < count)
            createProblem(count);

        mCount = 1;
    }
}
