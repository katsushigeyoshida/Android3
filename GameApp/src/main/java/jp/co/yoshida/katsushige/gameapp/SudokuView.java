package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashSet;
import java.util.Set;

import jp.co.yoshida.katsushige.mylib.YGButton;
import jp.co.yoshida.katsushige.mylib.YLib;

public class SudokuView extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = "SudokuView";

    private SurfaceHolder mSurfaceHolder;
    private int mWidth;                     //  画面幅
    private int mHeight;                    //  画面高さ
    private YGButton ygbutton;
    private YLib ylib;

    private float mTextSize;                //  文字の大きさ
    private int mBoardSize = 9;             //  盤の数
    private float mBoardSizeRatio = 0.85f;  //  画面に対する盤の大きさの比
    private float mHaba;                    //  盤のマスの大きさ
    private float mOx,mOy;                  //  盤の原点
    private Set<Integer> mFixPos = new HashSet<Integer>();
    private boolean mFixPosMode = false;    //  問題パターンが設定されている状態
    private int mCurId=-1;                  //  変更されたセルのID
    private int mSupportBtId = 200;         //  解法の補助ボタン
    private boolean mSupportMode = false;   //  解法補助ボタン表示モード
    private String mTempBoard;
    private boolean mNumberInBtDisp = false;    //  数値入力ボード表示
    private int mNumberInBtId = 300;        //  数値入力ボードID
    private int mBlockId = 100;             //  3x3ブロックの境界線用

    public SudokuView(Context context) {
        super(context);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ygbutton = new YGButton(mSurfaceHolder);
        ylib = new YLib();
        mTempBoard = "";
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mWidth = getWidth();
        mHeight = getHeight();
        ygbutton.setOffScreen(mWidth, mHeight);
        mTextSize = ygbutton.getTextSize();
        initScreen();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mTempBoard = getCurBoard();             //  スリープなどで画面が変わった時にのために盤データを保存
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //  押されたボタン位置のマスを反転する
                int id = ygbutton.getButtonDownId(event.getX(), event.getY());
                if (mSupportBtId <= id && id < mNumberInBtId) {
                    drawBoard();                //  盤を表示する
                    supportDraw(id);
                } else if (0 <= id && !mFixPos.contains(id)) {
                    Log.d(TAG,"onTouchEvent: "+id);
                    if (mNumberInBtId <= id) {
                        setTitleIdNo(mCurId, id);   //  選択した入力値をセルにセットする
                    } else {
                        mCurId = id;
                    }
                    numberInputBord(event.getX(), event.getY());    //  入力ボードの表示/非表示
                    boradColorClear();          //  背景色の設定をクリア
                    boolean b = boardCheck();   //  完成チェック(行、列、ブロックの完成は背景色を変える)
                    setFixPosColor();           //  問題の入っている数値ボタンの色を設定
                    duplicateCheck(mCurId);     //  重複チェック(数字が重なっていれば赤くする)
                    drawBoard();                //  盤を表示する
                    if (b)
                        completeMessage("完成");
                    else if (mFixPosMode)
                        passCheckMessage(id);
                }
        }
        return super.onTouchEvent(event);
    }


    /**
     * Solverで求めた解を盤に反映する
     * @param board     解答
     * @param count     回答に要した操作回数
     */
    public void setSolveData(int[] board, int count) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                if (!mFixPos.contains(getId(x,y)))
                    ygbutton.setGButtonTitle(getId(x, y), board[y*9+x]==0?"":String.valueOf(board[y*9+x]));
            }
        }
        if (boardCheck()) {
            setFixPosColor();
            drawBoard();
            completeMessage("解法　操作回数："+count);
        }
    }

    /**
     * 解法補助機能の表示切替
     */
    public void setSupportModeSW(boolean mode) {
        setSupportBordVisible(mode);
        drawBoard();
    }

    /**
     * 文字列による盤データを盤面に表示させる(データの復元)
     * セルの確定があるため、同じ問題パターンに適用
     * @param board     盤データ
     */
    public void setCurBoard(String board) {
        boradColorClear();
        setBoardData(board, true);
        setFixPosColor();
        drawBoard();
    }

    /**
     * 盤の入力値をすべてクリアする
     * 入力値がない時は盤を初期化する
     */
    public void boardClear() {
        if (0 < getInputdataCount()) {
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 9; y++) {
                    int id = getId(x, y);
                    if (!mFixPos.contains(id)) {
                        ygbutton.setGButtonTitle(id, "");
                        ygbutton.setGButtonBackColor(id, Color.WHITE);
                    }
                }
            }
            drawBoard();                //  盤を再表示
        } else {
            initBoard();
            setFixPosClear();
        }
    }

    /**
     * 現在選択されているセルをクリアする
     */
    public void curCellClear() {
        if (0 <= mCurId && !mFixPos.contains(mCurId))
            ygbutton.setGButtonTitle(mCurId, "");  //  選択セルのタイトルクリア
        mCurId = -1;
        ygbutton.removeGButton(mNumberInBtId); //  入力ボードのデータ削除
        mNumberInBtDisp = false;            //  入力ボードの非表示
        boradColorClear();                  //  背景色の設定をクリア
        boardCheck();                       //  完成チェック(行、列、ブロックの完成は背景色を変える)
        setFixPosColor();                   //  問題の入っている数値ボタンの色を設定
        drawBoard();                        //  盤を再表示
    }

    /**
     * 値を設定したセルの数を求める
     * @return      セルの数
     */
    private int getInputdataCount() {
        int n=0;
        for (int x=0; x<9; x++) {
            for (int y=0; y<9; y++) {
                int id = getId(x, y);
                if (!mFixPos.contains(id))
                    if (0 < getButtonNo(id))
                        n++;
            }
        }
        return n;
    }

    /**
     * 盤の完成を確認する
     * 列単位、行単位、ブロック単位で完成したところは背景をシアンに設定する
     * @return          成否
     */
    private boolean boardCheck() {
        boolean result = true;
        for (int x = 0; x < 9; x++) {
            if (rowCheck(x))
                rowColorSet(x);
            else
                result = false;
        }
        for (int y = 0; y < 9; y++) {
            if (columnCheck(y))
                columnColorSet(y);
            else
                result = false;
        }
        for (int x = 0; x < 9; x += 3) {
            for (int y = 0; y < 9; y += 3) {
                if (blockCheck(x, y))
                    blockColorSet(x, y);
                else
                    result = false;
            }
        }
        return result;
    }

    /**
     * 列単位で背景色をシアンにする
     * @param x         列位置
     */
    private void rowColorSet(int x) {
        for (int y = 0; y < 9; y++)
            setColorCyan(getId(x, y));
    }

    /**
     * 行単位で背景色をシアンにする
     * @param y         行位置
     */
    private void columnColorSet(int y) {
        for (int x = 0; x < 9; x++)
            setColorCyan(getId(x, y));
    }

    /**
     * 3x3ブロック単位で背景色をシアンにする
     * @param x         列位置
     * @param y         行位置
     */
    private void blockColorSet(int x, int y) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                setColorCyan(getId(x + i, y + j));
    }

    /**
     * 列範囲で完成しているかを確認
     * @param x         列位置
     * @return          成否
     */
    private boolean rowCheck(int x) {
        boolean[] data = new boolean[10];
        for (int y=0; y < 9; y++) {
            int n = getButtonNo(x, y);
            if (n==0)
                return false;
            if (data[n])
                return false;
            else
                data[n]= true;
        }
        return true;
    }

    /**
     * 行単位で完成しているかを確認
     * @param y         行位置
     * @return          成否
     */
    private boolean columnCheck(int y) {
        boolean[] data = new boolean[10];
        for (int x=0; x < 9; x++) {
            int n = getButtonNo(x, y);
            if (n==0)
                return false;
            if (data[n])
                return false;
            else
                data[n]= true;
        }
        return true;
    }

    /**
     * 3x3 のブロック範囲で完成しているかを確認
     * @param x         列位置
     * @param y         行位置
     * @return          成否
     */
    private boolean blockCheck(int x, int y) {
        boolean[] data = new boolean[10];
        x = x / 3 * 3;
        y = y / 3 * 3;
        for (int i=0; i < 3; i++) {
            for (int j=0; j < 3; j++) {
                int n = getButtonNo(x+i, y+j);
                if (n==0)
                    return false;
                if (data[n])
                    return false;
                else
                    data[n]= true;
            }
        }
        return true;
    }

    /**
     * 盤文字列データを盤面に表示させる(問題のパターン設定)
     * 盤文字列データは"0123456...789"などの数値文字列で表す
     * @param board         盤データ
     */
    public void setPatternBoard(String board) {
        boradColorClear();
        setBoardData(board, false);
        setFixPos();
        setFixPosColor();
        drawBoard();
    }

    /**
     * 盤の表示データを盤文字列データに変換する
     * 空白は'0'にする
     * @return              盤の文字列データ
     */
    public String getCurBoard() {
        String board = "";
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                board += 0< ygbutton.getGButtonTitle(getId(x,y)).length()? ygbutton.getGButtonTitle(getId(x,y)):"0";
            }
        }
        return board;
    }

    /**
     * セルに表示数値を設定する
     * @param id        ID
     * @param numId     表示数値
     */
    private void setTitleIdNo(int id, int numId) {
        if (mBlockId <= id)
            return;
        int no = getButtonNo(numId);
        ygbutton.setGButtonTitle(id, 0<no?String.valueOf(no):"");
    }

    /**
     * 問題パターンは変更できないように固定するIDを登録する
     */
    public void setFixPos() {
        mFixPos.clear();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                int n = getButtonNo(x, y);
                if (0 < n) {
                    mFixPos.add(getId(x, y));
                }
            }
        }
        mFixPosMode = true;
    }

    /**
     * 盤の背景色をクリア(白)する
     */
    private void boradColorClear() {
        for (int x = 0; x < 9; x++)
            for (int y = 0; y < 9; y++)
                ygbutton.setGButtonBackColor(getId(x, y), Color.WHITE);
    }


    /**
     * x,yで指定された位置のセルの数値を取得(空白は0)
     * @param x         列位置
     * @param y         行位置
     * @return          数値
     */
    private int getButtonNo(int x, int y) {
        return  getButtonNo(getId(x,y));
    }

    /**
     * ID で指定された位置のセルの数値を取得
     * @param id       セルのID
     * @return          数値
     */
    private int getButtonNo(int id) {
        return  ylib.str2Integer(ygbutton.getGButtonTitle(id));
    }

    /**
     * 行(y)列(x)からボタンのIDを求める
     * @param x         列番豪
     * @param y         行番号
     * @return          ID
     */
    private int getId(int x, int y) {
        return x * 10 + y;
    }


    /**
     * IDから列番号を取得
     * @param id        ID
     * @return          列番号
     */
    private int getXId(int id) {
        return id / 10;
    }

    /**
     * IDから行番号を取得
     * @param id        ID
     * @return          行番号
     */
    private int getYId(int id) {
        return id % 10;
    }


    /**
     * 固定セル(問題パターン)を解除する
     */
    private void setFixPosClear() {
        mFixPos.clear();
        mFixPosMode = false;
    }

    /**
     * 固定位置の背景色をライトグレイに設定する
     */
    private void setFixPosColor() {
        for (int id : mFixPos) {
            setColorLightGray(id);
        }
    }

    /**
     * 盤に数値データを設定する
     * @param board         盤データ(文字列: 01230056...90)
     * @param fixpos        固定数値セルにデータを書き込まない
     */
    private void setBoardData(String board,boolean fixpos) {
        int n= 0;
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                if (!fixpos || !mFixPos.contains(getId(x,y)))
                    ygbutton.setGButtonTitle(getId(x,y), board.charAt(n)=='0'?"":String.valueOf(board.charAt(n)));
                n++;
            }
        }
    }

    /**
     * タッチで指定した位置にセルに選択入力する数値を表示する
     * @param x     タッチ位置座標
     * @param y     タッチ位置座標
     */
    private void numberInputBord(float x, float y){
        if (mNumberInBtDisp) {
            ygbutton.removeGButton(mNumberInBtId);
        } else {
            String[] titles  = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
            float width = mHaba * 1.3f;
            //  円形配列
//            RectF rect = new RectF(x - width, y - width, x + width, y + width);
//            ygbutton.addGroupGButton(mNumberInBtId, YDraw.BUTTONTYPE.GROUPCIRCLE, rect, titles.length, 0, titles);
            //  矩形配列
            RectF rect = new RectF(x - width, y - width, x + width, y + width);
            ygbutton.addGroupGButton(mNumberInBtId, YGButton.BUTTONTYPE.GROUPRECT, rect, 3, 3, titles);
        }
        mNumberInBtDisp = !mNumberInBtDisp;
    }

    /**
     * 解法のための補助ボタンの表示
     */
    private void supportBord() {
        float ox = mOx - mHaba / 2f;
        float oy = mOy + mHaba * mBoardSize + mHaba * 0.7f ;
        for (int i = 1; i <= 9; i++) {
            ygbutton.addGButton(mSupportBtId + i, YGButton.BUTTONTYPE.RECT,
                    (float)ox + mHaba * i ,(float)oy,0, mHaba, mHaba);
            ygbutton.setGButtonBorderWidth(mSupportBtId + i, 3f);
            ygbutton.setGButtonTitleColor(mSupportBtId + i, Color.BLACK);
            ygbutton.setGButtonTitle(mSupportBtId + i, String.valueOf(i));
        }
    }


    /**
     * 解法の補助ボタンが押されたときに指定された数値で設定できない場所に取り消し線を入れて
     * 設定できる場所をわかるようにする。ブロックまたは行、列で一か所しかない場合は確定となる
     * @param id        セルのID
     */
    private void supportDraw(int id ) {
        float startx = mOx + mHaba / 3f;
        float endx = mOx + mHaba * mBoardSize - mHaba / 3f;
        float starty = mOy + mHaba / 3f;
        float endy = mOy + mHaba * mBoardSize - mHaba / 3f;
        int n = id - mSupportBtId;

        ygbutton.lockCanvas();
        ygbutton.setColor(Color.BLUE);
        ygbutton.setStrokeWidth(2f);

        for (int x=0; x<9;x++) {
            for (int y=0; y<9; y++) {
                if (n==getButtonNo(x, y)) {
                    ygbutton.drawLine(startx, mOy + mHaba / 2f + mHaba * y, endx, mOy + mHaba / 2f + mHaba * y);
                    ygbutton.drawLine(mOx + mHaba / 2f + mHaba * x, starty, mOx + mHaba / 2f + mHaba * x, endy);
                    int blockX = x / 3 * 3;
                    int blockY = y / 3 * 3;
                    for (int i=0; i<3; i++)
                        ygbutton.drawLine(mOx + mHaba / 3f + mHaba * blockX, mOy + mHaba / 2f + mHaba * (blockY+i),
                                mOx - mHaba / 3f + mHaba * (blockX+3), mOy + mHaba / 2f + mHaba * (blockY+i));
                }
            }
        }
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 解法補助の数値ボタンの表示/非表示設定
     * @param visible       表示/非表示
     */
    private void setSupportBordVisible(boolean visible) {
        for (int i = 1; i <= 9; i++) {
            ygbutton.setGButtonVisible(mSupportBtId + i, visible);
        }
    }

    /**
     * 指定値のセルに入れられる数値の候補を表示する
     * 指定値の行、列、ブロックで使用されていない数値の一覧
     * @param id        指定位置のボタンのID
     */
    private void passCheckMessage(int id) {
        boolean[] check = new boolean[10];
        int ox = getXId(id);
        int oy = getYId(id);
        if (9 < ox || 9 < oy)
            return;
        for (int x=0; x<9; x++)
            if (x!=ox)
                check[getButtonNo(x, oy)] = true;
        for (int y=0; y<9; y++)
            if (y!=oy)
                check[getButtonNo(ox, y)] = true;
        int x = ox / 3 * 3;
        int y = oy / 3 * 3;
        for (int i=0; i<3; i++)
            for (int j=0; j<3; j++)
                if ((i+x)!=ox && (j+y)!=oy)
                    check[getButtonNo(i+x, j+y)] = true;
        String msg = "候補値";
        for (int i=1; i<10; i++)
            if (!check[i])
                msg += " " + String.valueOf(i);
        completeMessage(msg);
    }

    /**
     * 入力した数値が他と重複していないかチェックする
     * 重複している場合はセルの背景を赤くする
     * @param id       指定位置のボタンのID
     */
    private void duplicateCheck(int id) {
        int count = 0;
        int n = getButtonNo(id);
        if (n == 0)
            return;
        int ox = getXId(id);
        int oy = getYId(id);
        for (int x=0; x<9; x++)
            if (x!=ox)
                if (n == getButtonNo(x, oy)) {
                    setColorLightRed(getId(x, oy));
                    count++;
                }
        for (int y=0; y<9; y++)
            if (y!=oy)
                if (n == getButtonNo(ox, y)) {
                    setColorLightRed(getId(ox, y));
                    count++;
                }
        int x = ox / 3 * 3;
        int y = oy / 3 * 3;
        for (int i=0; i<3; i++)
            for (int j=0; j<3; j++)
                if ((i+x)!=ox && (j+y)!=oy)
                    if (n == getButtonNo(i+x, j+y)) {
                        setColorLightRed(getId(i+x, j+y));
                        count++;
                    }
        if (0 < count)
            setColorLightRed(id);
    }

    /**
     * IDのセルの背景をシアンに設定する(1～9が揃ったところ)
     * @param id    セルのID
     */
    private void setColorCyan(int id) {
        ygbutton.setGButtonBackColor(id, Color.argb(30, 0, 255, 255));
    }

    /**
     * IDのセルの背景をライトグレイに設定する(固定セル)
     * @param id    セルのID
     */
    private void setColorLightGray(int id) {
        ygbutton.setGButtonBackColor(id, Color.argb(50,125,125,125));
    }

    /**
     * IDのセルの背景を薄い赤に設定する(重複チェック)
     * @param id    セルのID
     */
    private void setColorLightRed(int id) {
        ygbutton.setGButtonBackColor(id, Color.argb(80,250,0,0));
    }

    /**
     * メッセージの表示
     * @param text      表示文字列
     */
    public void completeMessage(String text) {
        ygbutton.lockCanvas();
        ygbutton.setTextSize(mOy * 0.7f);
        ygbutton.drawStringCenterWithBack(text, mWidth / 2, mOy / 2f, Color.RED, Color.WHITE);
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 盤面を表示する
     */
    private void drawBoard() {
        ygbutton.lockCanvas();
        ygbutton.backColor(Color.WHITE);
        ygbutton.drawGButtons();
        ygbutton.unlockCanvasAndPost();
    }

    /**
     * 盤の初期化を行う
     */
    private void initBoard() {
        //  盤をグラフィックボタンで作成
        ygbutton.clearButtons();
        //  盤の完成状態を作成
        float ox = mOx + mHaba / 2f;
        float oy = mOy + mHaba / 2f;
        for (int y = 0; y < mBoardSize; y++) {
            for (int x = 0; x < mBoardSize; x++) {
                ygbutton.addGButton(getId(x,y), YGButton.BUTTONTYPE.RECT,
                        (float)x * mHaba + ox,(float)y * mHaba + oy,0, mHaba, mHaba);
                ygbutton.setGButtonBorderWidth(getId(x,y), 3f);
                ygbutton.setGButtonTitleColor(getId(x,y), Color.BLACK);
                ygbutton.setGButtonTitle(getId(x,y), "");
            }
        }
        //  ブロック境界線
        ox = mOx + mHaba * 3 / 2f;
        oy = mOy + mHaba * 3 / 2f;
        for (int y = 0; y < mBoardSize; y+=3) {
            for (int x = 0; x < mBoardSize; x+=3) {
                int id = mBlockId + getId(x, y);
                ygbutton.addGButton(id, YGButton.BUTTONTYPE.RECT,
                        (float)x * mHaba + ox,(float)y * mHaba + oy,0, mHaba * 3, mHaba *3);
                ygbutton.setGButtonBorderWidth(id, 6f);
                ygbutton.setTransparent(id, true);
                ygbutton.setGButtonEnabled(id, false);
            }
        }

        //  補助ボタンむの設定
        supportBord();
        setSupportBordVisible(mSupportMode);

        drawBoard();
    }

    /**
     * 背景色で画面をクリアする
     */
    private void backClear() {
        ygbutton.lockCanvas();
        ygbutton.backColor(Color.WHITE);
        ygbutton.unlockCanvasAndPost();
    }
    /**
     * 画面の初期化
     */
    private void initScreen() {
        //  盤の大きさと位置
        float habaX = mWidth * mBoardSizeRatio / mBoardSize;
        float habaY = mHeight * mBoardSizeRatio / (mBoardSize+1.5f);
        mHaba = habaX < habaY?habaX:habaY;

        mOx = ((float)mWidth - (float)mBoardSize * mHaba) / 2f;
        mOy = mHaba / 1f;

        backClear();
        initBoard();

        if (0 < mTempBoard.length()) {
            setBoardData(mTempBoard, false);
            supportBord();
            setSupportBordVisible(mSupportMode);
            setFixPosColor();
            drawBoard();
        }
    }
}
