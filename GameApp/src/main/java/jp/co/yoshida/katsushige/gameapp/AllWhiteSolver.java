package jp.co.yoshida.katsushige.gameapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import jp.co.yoshida.katsushige.mylib.YLib;

/**
 * A*探索(最良優先探索)による「白にしろ」の解法プログラム
 * 幅優先探索に評価点による優先順位をつけて探索
 * 評価点は反転したセルの数で少ないほど優先順位を上げる
 * 最短の解法を見つけるものではないが早めに解答を見つけることができる
 * 盤サイズが大きい場合(4x4以上)に有効となる
 */
public class AllWhiteSolver {

    private static final String TAG = "AllWhiteSolver";

    class Board {
        public long mPreBoard;
        public int mLoc;

        public Board(long board, int loc) {
            mPreBoard = board;
            mLoc = loc;
        }
    }

    class Score {
        public int mScore;
        public long mBoard;

        public Score(int score, long board) {
            mScore = score;
            mBoard = board;
        }
    }

    /**
     * PriorityQueue 比較関数
     */
    class ScoreComparator implements Comparator {
        @Override
        public int compare(Object a, Object b) {
            Score x = (Score)a;
            Score y = (Score)b;
            if (x.mScore < y.mScore)
                return -1;
            else if (x.mScore > y.mScore)
                return 1;
            else
                return 0;
        }
    }

    private int mBoardSize;                 //  ボードサイズ
    private long mBoardPattern;             //  64bit ボードパターン
    private Map<Long, Board> mBoards;       //  探索したボード(重複なし)
    private PriorityQueue<Score> mScores;   //  評価点優先順位キュー
    private String mErrorMsg;

    private YLib ylib = new YLib();

    /**
     * コンストラクタ (初期登録)
     * @param boardPattern      問題パターン
     */
    public AllWhiteSolver(int[][] boardPattern) {
        mBoardSize = boardPattern.length;           //  盤の大きさ(boardSize x boardSize)
        mBoardPattern = cnvBoardData(boardPattern); //  盤の状態を2元配列からビットアドレスに変換
        mBoards = new HashMap<Long, Board>();       //  探索した盤状態の登録リスト
        mScores = new PriorityQueue<Score>(1012, new ScoreComparator());//  評価点を優先順位キューに登録
    }

    /**
     * 解法の実装
     * @return  解法の成否
     */
    public boolean Solver() {
        Score scoreBoard = new Score(ylib.bitsCount(mBoardPattern), mBoardPattern);
        mScores.add(scoreBoard);
        Board board = new Board(mBoardPattern, -1);
        Log.d(TAG,"Solver: "+String.format("%x",mBoardPattern)+" "+scoreBoard.mScore);

        try {
            while (true) {
                //  評価点のもっとも高いものから探索する
                if (getNextPattern(mScores.poll().mBoard))
                    break;              //  解法できた場合
                if (0 == mScores.size())
                    break;
            }
        } catch (Exception e) {
            mErrorMsg = e.getMessage();
            return false;
        }
        return true;
    }

    /**
     * 指定のパターンから派生するパターンを作成し登録
     * パターンの中に完成型(0)があれば終了
     * 既に登録されているパターンは登録しない
     * @param boardPattern      派生元のパターン
     * @return                  完成の有無
     */
    private boolean getNextPattern(long boardPattern) {
//        Log.d(TAG,"getNextPattern: "+String.format("%x",boardPattern)+" "+bitsCount(boardPattern));
        for (int row = 0; row < mBoardSize; row++) {
            for (int col = 0; col < mBoardSize; col++) {
                long nextPattern = reverseBoard(boardPattern, bitLoc(row, col), mBoardSize);
                //  既に登録されているパターンを除いてBoardデータとScoreデータを登録
                if (!mBoards.containsKey(nextPattern)) {
                    //  Boardリストにデータを登録
                    mBoards.put(nextPattern, new Board(boardPattern, bitLoc(row, col)));
                    //  Scoreキューにデータを登録
                    Score scoreBoard = new Score(ylib.bitsCount(nextPattern), nextPattern);
                    mScores.add(scoreBoard);
//                    Log.d(TAG,"getNextPattern: "+String.format("%x",nextPattern)
//                            +" "+String.format("%x",boardPattern)+" "+scoreBoard.mScore);
                    if (scoreBoard.mScore == 0)        //  反転データがなければ完了
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 探索結果のリストを出力
     * すべて白の状態から逆順で問題パターンにいたる反転位置のリスト
     * @return      反転位置リスト
     */
    public List<int[]> getResult() {
        List<int[]> result = new ArrayList<int[]>();
        long board = 0;
        int loc = 0;
//        Log.d(TAG,"getResult: "+String.format("%x",board));
        do {
            if (mBoards.containsKey(board)) {
                long tboard = board;
                loc = mBoards.get(board).mLoc;
                board = mBoards.get(board).mPreBoard;
                mBoards.remove(tboard);
                int[] locs = new int[2];
                locs[0] = bitLoc2Row(loc);
                locs[1] = bitLoc2Col(loc);
                result.add(locs);
//                Log.d(TAG,"getResult: "+String.format("%x",board)+" "+locs[0]+","+locs[1]);
            } else {
                mErrorMsg = "データが存在しない "+board;
                break;
            }
        } while (0 <= loc);
        return result;
    }

    /**
     * エラーメッセージの取得
     * @return      エラーメッセージ
     */
    public String getErrorMsg() {
        return mErrorMsg;
    }

    /**
     * 探索数の取得
     * @return      探索数
     */
    public int getCount() {
        return mBoards.size();
    }

    /**
     * 指定した位置とその上下左右のセルを反転
     * @param board         反転の盤の状態
     * @param loc           反転の起点アドレス
     * @param boardSize     盤のサイズ
     * @return              反転後の盤の状態
     */
    private long reverseBoard(long board, int loc, int boardSize) {
        board = ylib.bitRevers(board, loc);                      //  指定位置を反転
        if (0 < (loc / boardSize))
            board = ylib.bitRevers(board, loc - boardSize);  //  指定値の上を反転
        if ((loc / boardSize) < (boardSize - 1))
            board = ylib.bitRevers(board, loc + boardSize);  //  指定値の下を反転
        if (0 < (loc % boardSize))
            board = ylib.bitRevers(board, loc - 1);          //  指定値の左を反転
        if ((loc % boardSize) < (boardSize - 1))
            board = ylib.bitRevers(board, loc + 1);          //  指定値の右を反転
        return board;
    }

    /**
     * 2次元配列の盤のパターンからビット配列に変換
     * @param board     2次元配列の盤状態
     * @return          ビット配列の盤状態
     */
    private long cnvBoardData(int[][] board) {
        long cboard = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 0)
                    cboard = ylib.bitOn(cboard, bitLoc(row, col));
            }
        }
        return cboard;
    }

    /**
     * bitアドレスから行を求める
     * @param bitloc    bitアドレス
     * @return          行
     */
    private int bitLoc2Row(int bitloc) {
        return bitloc / mBoardSize;
    }

    /**
     * bitアドレスから列を求める
     * @param bitLoc    bitアドレス
     * @return          列
     */
    private int bitLoc2Col(int bitLoc) {
        return bitLoc % mBoardSize;
    }

    /**
     * ボードサイズに合わせて行列をbit位置に変換
     * @param row       行
     * @param col       列
     * @return          bit位置
     */
    private int bitLoc(int row, int col) {
        return row * mBoardSize + col;
    }

}
