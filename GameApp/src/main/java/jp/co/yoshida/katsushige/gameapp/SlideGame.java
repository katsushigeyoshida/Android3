package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SlideGame extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "SlideGame";

    private Spinner mSpBoardSize;                //  盤の大きさ
    private Button mBtNextProblem;              //  問題作成ボタン
    private TextView mTvSolerResult;            //  解法結果
    private Button mBtSolver;                   //  解答ボタン
    private LinearLayout mLinearLayout;         //  Viewのコンテナ
    private SlideGameView mSlideGameView;
    private ArrayAdapter<String> mBoardSizeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_game);

        this.setTitle("スライドゲーム");
        init();


        /**
         * ボードサイズの変更を設定する
         */
        mSpBoardSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                //showToast(Integer.toString(spinner.getSelectedItemPosition()));
                Log.d(TAG, "onCreate:mSpBoardSize" + spinner.getSelectedItemPosition());
                mSlideGameView.setParameter(getBoardSize());
                if (4 < getBoardSize()) {
                    mBtSolver.setEnabled(false);
                    mTvSolerResult.setText("解法は盤の大きさが4以下のみ");
                } else {
                    mBtSolver.setEnabled(true);
                    mTvSolerResult.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtNextProblem.getId()) {
            //  問題の作成
            int n = (int)(Math.random()*Math.pow(getBoardSize(), 3.0)+3); //  操作回数
            mTvSolerResult.setText("問題作成操作数: "+n);
            mSlideGameView.initScreen(n);
        } else if (view.getId() == mBtSolver.getId()) {
            mTvSolerResult.setText("解法手順");
            if (4 < getBoardSize())
                solver2();  //  最良優先探索
            else
                solver();   //  幅優先探索
        }
    }

    /**
     * 盤の問題の解法と手順の表示
     */
    private void solver() {
        byte[] board = mSlideGameView.getBoard();
        int boardSize = (int)Math.sqrt(board.length);
        String txt = "盤の状態\n";
        for (int row=0; row<boardSize; row++) {
            for(int col=0; col<boardSize; col++) {
                txt += board[row*boardSize+col]+" ";
            }
            txt += "\n";
        }
        mTvSolerResult.setText(txt);
        txt = "解法\n";
        ArrayList<SlideBoard> boards = EightPuzzleSolver(board, 23); //  22手ぐらいまでがメモリの限界
        if (boards==null) {
            txt += "計算不可";
        } else {
            txt += "探索数: " + boards.size() + "\n手順数: " + boards.get(boards.size() - 1).mLevel + "\n";
            if (boards.get(boards.size() - 1).mStat == SlideBoard.Status.COMPLETE) {
                txt += "回答手順\n";
                ArrayList<Integer> result = getSolverResult(boards);
                for (int i = result.size() - 2; 0 <= i; i--) {
                    SlideBoard tempBoard = boards.get(result.get(i));
                    txt += "" + (result.size() - i - 1) + ": [" + tempBoard.mTitle + "] \n";
                }
            } else if (boards.get(boards.size() - 1).mStat == SlideBoard.Status.UNCOMPLETE) {
                txt += "解答不可";
            } else {
                txt += "解答未完成";
            }
        }
        mTvSolerResult.setText(txt);
    }

    private void solver2() {
        byte[] board = mSlideGameView.getBoard();
        int boardSize = (int)Math.sqrt(board.length);
        String txt = "解法(非最適解)\n";
        SlideGameSolver solver = new SlideGameSolver(board);
        boolean result = solver.Solver();
        if (!result) {
            txt +="計算不可\n" + solver.mErrorMsg;
        } else {
            if (solver.mStat == SlideGameSolver.Status.COMPLETE) {
                txt += "探索数: \n " + solver.getCount() + "\n";
                //txt += "手順数: " + boardSolver.getLevel() + "\n";
                txt += "回答手順\n";
                List<Integer> resultNo = solver.getResult();
                for (int i = resultNo.size() - 2; 0 <= i; i--) {
                    txt += "" + (resultNo.size() - i - 1) + ": [" + resultNo.get(i) + "] \n";
                }
            } else if (solver.mStat == SlideGameSolver.Status.UNCOMPLETE) {
                txt += "解答不可\n";
            } else {
                txt += "解答未完成(打ち切り)\n" + solver.mErrorMsg;
            }
        }
        mTvSolerResult.setText(txt);
    }

    /**
     * ボードの大きさを取得
     * @return
     */
    private int getBoardSize() {
        int l = mSpBoardSize.getSelectedItemPosition();
        return Integer.valueOf(mBoardSizeAdapter.getItem(l));
    }

    /**
     * 初期化
     */
    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//	画面を縦に固定する

        mSpBoardSize = (Spinner)findViewById(R.id.spinner2);
        mBtNextProblem = (Button)findViewById(R.id.button7);
        mBtSolver = (Button)findViewById(R.id.button8);
        mTvSolerResult = (TextView)findViewById(R.id.textView5);
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout1);
        mSlideGameView = new SlideGameView(this);
        mLinearLayout.addView(mSlideGameView);

        mTvSolerResult.setText("解法手順");
        mBtNextProblem.setOnClickListener(this);
        mBtSolver.setOnClickListener(this);

        String[] boardSizeItems = {"2","3","4","5"};
        mBoardSizeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, boardSizeItems);
        mSpBoardSize.setAdapter(mBoardSizeAdapter);
        mSpBoardSize.setSelection(mBoardSizeAdapter.getPosition("4"));
    }


    /**
     *  「15パズル」(スライディングパズル)の解法関数 幅優先探索
     * 探索した結果、最終盤が完成であればそこから逆に探索すると解法手順が求められる
     * @param tboard        盤の状態(問題の盤)2次元配列を1次元にしたものの
     * @param maxLevel      最大手順数(3x3で程度、それ以上だとメモリ不足になる可能性大)
     * @return              探索結果の盤ののリスト
     */
    private static ArrayList EightPuzzleSolver(byte[] tboard, int maxLevel) {
        ArrayList<SlideBoard> boards = new ArrayList<SlideBoard>();
        int boardSize = (int)Math.sqrt(tboard.length);
        int level = 0;                                      //  検索レベル
        int prevNo = 0;
        boolean complete = false;                           //  完了の有無

        SlideBoard board = new SlideBoard(boardSize);
        board.setBoadData(tboard);                          //  パターンコピー
        boards.add(board);
        int n = 0;
        level = board.mLevel;
//        while (!complete && n < boards.size() && level < maxLevel) {
        try {
            while (!complete && n < boards.size()) {
                ArrayList<SlideBoard> tempBoards = boards.get(n).nextPatterns();
                if (tempBoards == null) {
                    complete = false;
                    break;
                }
                for (int i = 0; i < tempBoards.size(); i++) {
                    level = tempBoards.get(i).mLevel;
                    tempBoards.get(i).mCurPos = boards.size();
                    boards.add(tempBoards.get(i));
                    if (tempBoards.get(i).mStat != SlideBoard.Status.UNNON) {
                        complete = true;
                        break;
                    }
                }
                n++;
            }
        } catch (Exception e) {
            boards = null;
            Log.d(TAG,"EightPuzzleSolver: "+e.getMessage()+" Size: "+n+" level: "+level);
        }
        return boards;
    }

    //  盤のリストから解法手順(盤の番号リストを抽出)
    private static ArrayList getSolverResult(ArrayList<SlideBoard> boards) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int n = boards.size() - 1;
        while (0 <= n) {
            result.add(n);
            n = boards.get(n).mPrevPos;
        }
        return result;
    }
}


//  パズル「15パズル(スライディンクゲーム)」の解法クラス
class SlideBoard {
    private final String TAG = "SlideBoard";

    int mBoardSize;     //  盤の大きさ
    byte[] mBoard;      //  盤(2次元を1次元で保管)(row*borsSize + col)
    byte mTitle;        //  移動したブロックの番号
    byte mLoc;          //  座標(1次元での位置)移動したブロック移動前の位置(ブランクの位置)
    byte mPrevLoc;      //  前回のブロック伊藤前の位置(1次元にした値)
    int mPrevPos;       //  変更前のデータ保管位置
    int mCurPos;        //  現在のデータ位置
    int mLevel;         //  検索レベル(手順数)
    enum Status {COMPLETE,UNCOMPLETE,UNNON};    //  盤の状態
    Status mStat = Status.UNNON;

    //  初期化
    public SlideBoard(int size) {
        mBoardSize = size;
        mBoard = new byte[mBoardSize*mBoardSize];
        initBoard();        //  盤面を0にする
        mTitle = -1;        //  ブロックの番号
        mLoc = -1;          //  ブランクの位置
        mPrevLoc = -1;      //  一つ前のブランクの位置
        mPrevPos = -1;
        mCurPos = 0;
        mLevel = 0;
    }

    //  次のパターンを作る
    public ArrayList<SlideBoard> nextPatterns() {
        ArrayList<SlideBoard> boards = new ArrayList<SlideBoard>();
        int curPos = mCurPos;
        byte[] locs = getNextPosition();
        try {
            for (int i = 0; i < locs.length; i++) {
                if (mPrevLoc != locs[i]) {    //  もとの状態を除く
                    SlideBoard tempBoard = copyBoard();
                    tempBoard.swapBlank(locs[i]);
                    tempBoard.mTitle = mBoard[locs[i]];
                    tempBoard.mPrevLoc = tempBoard.mLoc;
                    tempBoard.mLoc = locs[i];
                    tempBoard.mCurPos = ++curPos;
                    tempBoard.setStat();
                    boards.add(tempBoard);
                }
            }
        } catch(Exception e) {
            Log.d(TAG,"nextPatterns: "+e.getMessage());
            boards = null;
        }
        return boards;
    }

    //  盤の状態を設定する(完成/完成不可/不明)
    public void setStat() {
        if (completeChk())
            mStat = Status.COMPLETE;
        else if (unCompleteChk())
            mStat = Status.UNCOMPLETE;
        else
            mStat = Status.UNNON;
    }

    //  盤が完成していればtrueを返す
    public boolean completeChk() {
        for (int i=0; i < mBoard.length-1; i++) {
            if (mBoard[i] != i+1)
                return false;
        }
        if (mBoard[mBoard.length-1]!=0)
            return false;
        return true;
    }

    //  盤が完成しないパターンの確認
    public boolean unCompleteChk() {
        for (int i=0; i < mBoard.length-3; i++)
            if (mBoard[i] != i+1)
                return false;
        if (mBoard[mBoard.length-3]!=mBoard.length-1)
            return false;
        if (mBoard[mBoard.length-2]!=mBoard.length-2)
            return false;
        if (mBoard[mBoard.length-1]!=0)
            return false;
        return true;
    }

    //  盤を比較してすべて同じであればtrueを返す
    private boolean compareChk(byte[] board) {
        for (int i=0; i < mBoard.length; i++)
            if (mBoard[i] != board[i])
                return false;
        return true;
    }

    //  盤のコピーを作成する
    public SlideBoard copyBoard() {
        SlideBoard board = new SlideBoard(mBoardSize);
        board.mBoardSize = mBoardSize;
        for (int i=0; i < mBoard.length; i++)
            board.mBoard[i] = mBoard[i];
        board.mTitle = mTitle;
        board.mPrevLoc = mPrevLoc;
        board.mLoc = mLoc;
        board.mPrevPos = mCurPos;
        board.mCurPos = mCurPos + 1;
        board.mLevel = mLevel + 1;

        return board;
    }

    //  ブランク位置とデータを交換する
    public void swapBlank(byte loc) {
        byte n = mBoard[loc];
        mBoard[getBlankLoc()] = n;
        mBoard[loc] = 0;
    }

    //  ブランクと交換できる位置のリストを求める
    public byte[] getNextPosition() {
        byte loc = getBlankLoc();
        return getNextPosition(loc);
    }

    //  指定した位置と前後左右の位置のリストを作る
    public byte[] getNextPosition(byte loc) {
        int row = loc / mBoardSize;
        int col = loc % mBoardSize;
        int size = 4;
        if (row==0 || row==mBoardSize-1)
            size--;
        if (col==0 || col==mBoardSize-1)
            size--;
        byte[] nextId = new byte[size];
        int i=0;
        if (0 < col)
            nextId[i++] = (byte)(row * mBoardSize + col - 1);
        if (col < mBoardSize - 1)
            nextId[i++] = (byte)(row * mBoardSize + col + 1);
        if (0 < row)
            nextId[i++] = (byte)((row-1) * mBoardSize + col);
        if (row < mBoardSize - 1)
            nextId[i++] = (byte)((row+1) * mBoardSize + col);
        return nextId;
    }

    //  ブランクデータの位置を取得する
    public byte getBlankLoc() {
        for (byte i=0; i<mBoard.length; i++)
            if (mBoard[i]==0)
                return i;
        return -1;
    }

    //  指定座標が盤内にあるかを確認する
    private boolean chkPoint(byte loc) {
        if (loc < 0 || mBoard.length <= loc)
            return false;
        return true;
    }

    //  現在の盤のデータ位置を設定
    public void setCurPos(int n) {
        mCurPos = n;
    }

    //  もととなった盤のNoを設定する
    public void setPrevPos(int n) {
        mPrevPos = n;
    }

    //  盤のパターンだけを設定する
    public void setBoadData(byte[] board) {
        for (int i=0; i < mBoardSize*mBoardSize; i++)
            mBoard[i] = board[i];
    }

    //  盤の状態を出力する
    public void printStatus() {
        System.out.println("CurNo: "+mCurPos+" prev: "+mPrevPos+" level: "+mLevel+
                " loc: "+mLoc+" PrevLoc:"+mPrevLoc+" raw:"+mLoc/mBoardSize+" col: "+mLoc%mBoardSize+
                " Title: "+mTitle+" Stat: "+mStat);
    }

    //  盤ののパターンを出力する
    public void printBoard() {
        int i=0;
        for (int row = 0; row < mBoardSize; row++) {
            String txt = "";
            for (int col = 0; col < mBoardSize; col++) {
                txt += String.valueOf(mBoard[i++]) + " ";
            }
            System.out.println(txt);
        }
    }

    //  盤を初期化(1からインクリメントしながら数値を入れる、最後のマスは0にする)
    private void initBoard() {
        for (int i=0; i < mBoard.length; i++)
            mBoard[i] = (byte)(i+1);
        mBoard[mBoard.length - 1] =0;
    }

}
