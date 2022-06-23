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

/**
 *   パズルゲーム「白にしろ」 第一学習社
 *   http://www.daiichi-g.co.jp/osusume/forfun/05_white/05.html
 *   【遊び方】
 *       １つのマスをクリックすると，そのマスと上下左右のマスの色が反転する。
 *       その作業を繰り返し，すべてのマスを白にしたらクリア。
 *       別の問題に挑戦したいときは，「次の問題へ」ボタンを押す。
 */
public class AllWhite extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "AllWhite";

    private Spinner mSpBoardSize;               //  盤の大きさ
    private Button mBtNextProblem;              //  問題作成ボタン
    private TextView mTvSolerResult;            //  解法結果
    private Button mBtSolver;                   //  解答ボタン
    private LinearLayout mLinearLayout;         //  Viewのコンテナ
    private AllWhiteView mAllWhiteView;
    private ArrayAdapter<String> mBoardSizeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_white);
        this.setTitle("白にしろ");

        init();

        //  盤のサイズの切り替え
        mSpBoardSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                //showToast(Integer.toString(spinner.getSelectedItemPosition()));
                Log.d(TAG, "onCreate:mSpBoardSize" + spinner.getSelectedItemPosition());
                mAllWhiteView.setParameter(getBoardSize());
                mTvSolerResult.setText("");
                //  盤のサイズが4以上だと能力的に解放できないので使用不可とする
//                if (4 < getBoardSize()) {
//                    mBtSolver.setEnabled(false);
//                    mTvSolerResult.setText("この盤の大きさで求められた解は最適解ではない可能性がありあり"
//                        +"場合によっては求められない場合もあります");
//                } else {
//                    mBtSolver.setEnabled(true);
//                    mTvSolerResult.setText("");
//                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtNextProblem.getId()) {
            int n = (int)Math.floor(Math.random() * 10) + 3;
            mTvSolerResult.setText("問題作成操作数: "+n);
            mAllWhiteView.createProblem(n);
        } else if (view.getId() == mBtSolver.getId()) {
            if (4 < getBoardSize())
                solver2();  //  最良優先探索
            else
                solver();   //  幅優先探索
        }
    }


    /**
     * 盤の問題の解法と手順の表示(幅優先探索)
     */
    private void solver() {
        int[][] board;
        board = mAllWhiteView.getBoard();
        String msg = "";
        mTvSolerResult.setText("探索中・・・");

        msg += "解法手順\n";
        ArrayList<AllWhiteBoard> boards = AllWhiteSolver(board);
        if (boards == null) {
            msg += "解法できず";
        } else {
            msg += "探索数: "+boards.size()+"\n";
            msg += "No[行,列]\n";
            //  手順の表示
            ArrayList<int[]> result = getSolverResult(boards);
            for (int i=0; i<result.size()-1; i++) {
                int[] loc = result.get(i);
                msg += (i+1)+" ["+loc[0]+" , "+loc[1]+"]\n";
            }
        }

        mTvSolerResult.setText(msg);
    }

    /**
     * 盤の問題の解法と手順の表示(最良優先探索)
     */
    private void solver2() {
        int[][]board;
        board = mAllWhiteView.getBoard();
        String msg = "";
        msg += "解法手順\n" + "(非最適化)\n";

        AllWhiteSolver solver = new AllWhiteSolver(board);
        if (solver.Solver()) {
            msg += "探索数\n" + solver.getCount() + "\n";
            //  手順の表示
            List<int[]> result = solver.getResult();
            for (int i = result.size() - 2; 0 <= i; i--) {
                msg += (result.size() -  i - 1) + ":[" +
                        result.get(i)[0] + "," + result.get(i)[1] + "]\n";
            }
            mTvSolerResult.setText(msg);
        } else {
            msg += "解法できず\n" +
                    solver.getCount() + "\n" +
                    solver.getErrorMsg();
        }
    }

    /**
     * spinnerで設定されている盤のサイズを取得
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

        mSpBoardSize = (Spinner)findViewById(R.id.spinner3);
        mBtNextProblem = (Button)findViewById(R.id.button9);
        mTvSolerResult = (TextView)findViewById(R.id.textView10);
        mBtSolver = (Button)findViewById(R.id.button10);
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout2);
        mAllWhiteView= new AllWhiteView(this);
        mLinearLayout.addView(mAllWhiteView);

        mTvSolerResult.setText("解法手順");
        mBtNextProblem.setOnClickListener(this);
        mBtSolver.setOnClickListener(this);

        String[] boardSizeItems = {"2","3","4","5"};
        mBoardSizeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, boardSizeItems);
        mSpBoardSize.setAdapter(mBoardSizeAdapter);
        mSpBoardSize.setSelection(mBoardSizeAdapter.getPosition("3"));
    }


    /**
     *  「白にしろ」の解法関数 幅優先探索で完成手順を求める
     * @param tboard    盤のデータ(問題)
     * @return          探索結果リスト(null時は解答が求められなかったとき)
     */
    private ArrayList AllWhiteSolver(int[][] tboard) {
        ArrayList<AllWhiteBoard> boards = new ArrayList<AllWhiteBoard>();   //  検索パターンの盤リスト
        int boardSize = tboard[0].length;                       //  盤の大きさ(boardSize x boardSize)
        int level = 0;                                          //  検索レベル
        int prevNo = 0;
        boolean complete = false;                               //  完了の有無

        AllWhiteBoard board = new AllWhiteBoard(boardSize);     //  問題の盤のパターン
        board.setBoadData(tboard);                              //  パターンコピー
        boards.add(board);                                      //  盤の状態をリストに追加
        int n = 0;
        level = board.mLevel;                                   //  解法手順(操作数)
        //  パターン探索処理(操作数Levelは10手、登録盤数20000までとする)
        try {
            while (!complete && n < boards.size()) {
                if (boards.get(n).mPrevNo == prevNo - 1) {
                    //  現盤の状態から次の盤のパターンを求める(反転や回転で同じになるものは除く)
                    ArrayList<AllWhiteBoard> tempBoards = boards.get(n).nextPatterns();
                    if (tempBoards == null) {
                        complete = false;
                        break;
                    }
                    for (int i = 0; i < tempBoards.size(); i++) {
                        level = tempBoards.get(i).mLevel;
                        tempBoards.get(i).mCurNo = boards.size();
                        boards.add(tempBoards.get(i));
                        //  盤がすべて白になったか確認
                        if (tempBoards.get(i).completeChk()) {
                            complete = true;
                            break;
                        }
                    }
                    n++;
                } else {
                    prevNo++;
                }
            }
        } catch (Exception e) {
            complete = false;
            Log.d(TAG,"AllWhiteSolver: "+e.getMessage()+" Size: "+n+" level: "+level);
        }
        if (complete)
            return boards;
        else
            return null;
    }

    /**
     * 探索結果リストから解法手順を取得
     * @param boards    探索リスト
     * @return          操作手順リスト([row][col]リスト)
     */
    private ArrayList getSolverResult(ArrayList<AllWhiteBoard> boards) {
        ArrayList<int[]> result = new ArrayList<int[]>();
        int n = boards.size() - 1;
        while (0 <= n) {
            int[] loc = new int[2];
            loc[0] = boards.get(n).mLoc[0];
            loc[1] = boards.get(n).mLoc[1];
            result.add(loc);
            n = boards.get(n).mPrevNo;
        }
        return result;
    }
}


/**
 *  パズル「白にしろ」の解法クラス
 *  幅優先探索に解法
 */
class AllWhiteBoard {

    private final String TAG = "AllWhiteBoard";

    int mBoardSize;     //  盤の大きさ
    int[][] mBoard;     //  盤
    int[] mLoc;         //  座標
    int mPrevNo;        //  変更前のNo
    int mCurNo;         //  現在のNo
    int mLevel;         //  検索レベル

    public AllWhiteBoard(int size) {
        mBoardSize = size;
        mBoard = new int[mBoardSize][mBoardSize];
        initBoard();        //  盤面を0にする
        mLoc = new int[2] ;
        mLoc[0] = mLoc[1] = -1;
        mPrevNo = -1;
        mCurNo = 0;
        mLevel = 0;
    }

    //  現在の盤から次の盤のパターンを検索する
    //  検索したパターンの内ミラーや回転したときに同じパターンは除く
    public ArrayList<AllWhiteBoard> nextPatterns() {
        ArrayList<AllWhiteBoard> boards = new ArrayList<AllWhiteBoard>();
        int curNo = mCurNo;
        //  盤上のすべての位置に指定してできたパターンから反転や回転で重複するものを除いて登録する
        try {
            for (int row = 0; row < mBoardSize; row++) {
                for (int col = 0; col < mBoardSize; col++) {
                    //  前回の指定位置を除いて対象パターンを取得する
                    if (mLoc[0] != row || mLoc[1] != col) {
                        AllWhiteBoard tempBoard = copyBoard();
                        tempBoard.reverseBoard(row, col);
                        int i;
                        for (i = 0; i < boards.size(); i++) {
                            //  反転または回転したパターンが今までにあれば登録から除外
                            if (boards.get(i).mirrorRotateChk(tempBoard))
                                break;
                        }
                        if (boards.size() <= i) {
                            //  パターンを登録
                            tempBoard.setCurNo(++curNo);
                            boards.add(tempBoard);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"nextPatterns: "+e.getMessage());
            boards = null;
        }
        return boards;
    }

    public void incCurNo() {
        mCurNo++;
    }

    public void setCurNo(int n) {
        mCurNo = n;
    }

    //  盤がすべて[0]であればtrueを返す
    public boolean completeChk() {
        for (int row = 0; row < mBoardSize; row++)
            for (int col = 0; col < mBoardSize; col++)
                if (mBoard[row][col] != 0)
                    return false;
        return true;
    }

    //  盤を比較してすべて同じであればtrueを返す
    private boolean compareChk(int[][] board) {
        for (int row = 0; row < mBoardSize; row++)
            for (int col = 0; col < mBoardSize; col++)
                if (mBoard[row][col] != board[row][col])
                    return false;
        return true;
    }

    //  盤のコピーを作成する
    public AllWhiteBoard copyBoard() {
        AllWhiteBoard board = new AllWhiteBoard(mBoardSize);
        for (int row = 0; row < mBoardSize; row++)
            for (int col = 0; col < mBoardSize; col++)
                board.mBoard[row][col] = mBoard[row][col];
        board.mBoardSize = mBoardSize;
        board.mLoc[0] = mLoc[0];
        board.mLoc[1] = mLoc[1];

        board.mPrevNo = mCurNo;
        board.mCurNo = mCurNo + 1;
        board.mLevel = mLevel + 1;

        return board;
    }

    //  指定した位置と上下左右を反転する
    public boolean reverseBoard(int row, int col) {
        if (!chkPoint(row, col))
            return false;
        mLoc[0] = row;
        mLoc[1] = col;
        reversePoint( row, col, mBoard);
        reversePoint(row, col-1, mBoard);
        reversePoint(row, col+1, mBoard);
        reversePoint(row-1, col, mBoard);
        reversePoint(row+1, col, mBoard);
        return true;
    }

    //  指定位置の符号を反転
    private void reversePoint(int row, int col, int[][] board)  {
        if (!chkPoint(row, col))
            return ;
        if (board[row][col] == 0)
            board[row][col] = 1;
        else
            board[row][col] = 0;
    }

    //  同じパターンが一つでもあればtrueを返す
    //  反転や回転でできたパタンとの比較
    private boolean mirrorRotateChk(AllWhiteBoard board) {
        boolean[] chk = {true, true, true, true, true, true, true, true};
        for (int row = 0; row < mBoardSize; row++) {
            for (int col = 0; col < mBoardSize; col++) {
                // System.out.println("mirrorChk: "+row+" "+col+" "+mBoardSize+" : ");
                //+src[row][col]+" "+dest[row][mBoardSize - col - 1]);
                if (mBoard[row][col] != board.mBoard[row][col])                    //  そのまま比較
                    chk[0] =  false;
                if (mBoard[row][col] != board.mBoard[row][mBoardSize - col - 1])   //  Y軸ミラー比較
                    chk[1] =  false;
                if (mBoard[row][col] != board.mBoard[mBoardSize - row - 1][col])   //  X軸ミラー比較
                    chk[2] =  false;
                if (mBoard[row][col] != board.mBoard[mBoardSize - row - 1][mBoardSize - col - 1])  //  XY軸ミラー
                    chk[3] =  false;
                if (mBoard[row][col] != board.mBoard[col][mBoardSize - row - 1])   //  90°回転比較
                    chk[4] =  false;
                if (mBoard[row][col] != board.mBoard[mBoardSize - col - 1][row])   //  270°回転比較
                    chk[5] =  false;
                if (mBoard[row][col] != board.mBoard[col][row])                    //  対角線ミラー比較
                    chk[6] =  false;
                if (mBoard[row][col] != board.mBoard[mBoardSize - col - 1][mBoardSize - row - 1])  //  逆対角線ミラー比較
                    chk[7] =  false;
            }
        }
        for (int i=0; i<chk.length; i++) {
            if (chk[i]==true)
                return true;
        }
        return false;
    }

    //  指定座標が盤内にあるかを確認する
    private boolean chkPoint(int row, int col) {
        if (row < 0 || mBoardSize <= row)
            return false;
        if (col < 0 || mBoardSize <= col)
            return false;
        return true;
    }

    //  もととなった盤のNoを設定する
    public void setPrevNo(int n) {
        mPrevNo = n;
    }

    //  盤のパターンだけを設定する
    public void setBoadData(int[][] board) {
        for (int row=0; row < mBoardSize; row++)
            for (int col=0; col < mBoardSize; col++)
                mBoard[row][col] = board[row][col];
    }

    //  盤ののパターンを出力する
    public void printBoard() {
        System.out.println("CurNo: "+mCurNo+" prev: "+mPrevNo+" raw:"+mLoc[0]+" col: "+mLoc[1]);
        for (int row = 0; row < mBoardSize; row++) {
            String txt = "";
            for (int col = 0; col < mBoardSize; col++) {
                txt += String.valueOf(mBoard[row][col]) + " ";
            }
            System.out.println(txt);
        }
    }

    //  盤を初期化(すべて0にする)
    private void initBoard() {
        for (int row=0; row < mBoardSize; row++)
            for (int col=0; col < mBoardSize; col++)
                mBoard[row][col] = 0;
    }
}
