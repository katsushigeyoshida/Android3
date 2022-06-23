package jp.co.yoshida.katsushige.gameapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import jp.co.yoshida.katsushige.mylib.YLib;

public class SlideGameSolver {

    private static final String TAG = "SlideGameSolver";

    class SlideGameBoard {
        public long mPreBoard;  //  ボードの状態(移動前状態)
        public int mLoc;        //  移動セル位置アドレス(row * BoardSize + col)

        public SlideGameBoard(long board, int loc) {
            mPreBoard = board;
            mLoc = loc;
        }
    }

    class SlideGameScore {
        public int mScore;  //  ボードの評価点(完成と不一致のセルの数)
        public long mBoard; //  ボードの状態

        public SlideGameScore(int score, long board) {
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
            SlideGameScore x = (SlideGameScore)a;
            SlideGameScore y = (SlideGameScore)b;
            if (x.mScore < y.mScore)
                return -1;
            else if (x.mScore > y.mScore)
                return 1;
            else
                return 0;
        }
    }

    private int mBoardSize;                             //  ボードのサイズ
    private long mBoardPattern;                         //  問題パターン
    private long mCompletePattern;                      //  完成形
    private long mUnComletePattern;                     //  非完成形
    private Map<Long, SlideGameBoard> mBoards;          //  探索したボードの登録8重複不可)
    private PriorityQueue<SlideGameScore> mScoreBoards; //  評価点の優先順位キュー
    public String mErrorMsg;                            //  エラーメッセージ
    enum Status { COMPLETE, UNCOMPLETE, UNNON };        //  盤の状態
    public Status mStat = Status.UNNON;

    private YLib ylib;

    /**
     * A*探索(最良優先探索)による解法を行う(評価関数は一致している駒の数)
     * 盤データをlong(64bit=4bitx16)で扱っているため4x4の15ゲームまでしか扱えません
     * @param tboard        問題パターン
     */
    public SlideGameSolver(byte[] tboard) {
        mBoardSize = (int)Math.sqrt(tboard.length);     //  盤の大きさ(boardSize x boardSize)
        mBoardPattern = cnvArray2Long(tboard);          //  盤の状態を2元配列からビットアドレスに変換する
        mBoards = new HashMap<Long, SlideGameBoard>();  //  探索した盤状態の登録リスト
        mScoreBoards = new PriorityQueue<SlideGameScore>(1024, new ScoreComparator());  //  優先順位キューに評価点を登録
        mCompletePattern = getCompletePattern();        //  完成形の4bit配列パターン
        mUnComletePattern = getUnCompletePattern();     //  完成しない4bit配列パターン
    }

    /**
     * 解法の実行
     * @return
     */
    public boolean Solver() {
        //  初期化
        SlideGameBoard board = new SlideGameBoard(mBoardPattern, -1);   //  問題の盤の状態を登録
        mBoards.put(mBoardPattern, board);
        SlideGameScore scoreBoad = new SlideGameScore(getPriority(mBoardPattern), mBoardPattern);   //  評価点の登録
        mScoreBoards.add(scoreBoad);

        try {
            while (0 < mScoreBoards.size()) {
                if (mScoreBoards.peek().mScore == 0) {
                    mStat = Status.COMPLETE;
                    break;
                }
                //  評価点の最も高い(不一致数が少ない)物から探索する
                if (getNextPattern(mScoreBoards.poll().mBoard)) //  現状の盤の状態から次の盤状態を求める
                    break;
                if (0 == mScoreBoards.size())                   //  登録データがなくなったら完了
                    break;
            }
        } catch (Exception e) {
            mErrorMsg = e.getMessage();
            return false;
        }
        return true;
    }

    /**
     * 現在の盤状態から次の盤の状態を求める
     * 登録された盤のパターンを覗いて盤の状態と優先度を別々に登録する
     * @param boardPattern      盤の状態
     * @return                  完成形または非完成形であればtrue
     */
    private boolean getNextPattern(long boardPattern) {
        int spaceLoc = getSpaceLoc(boardPattern);
        List<Integer> locList = getSwapLocList(spaceLoc);   //  空白位置から移動できるセルの位置を求める
        for (int i = 0; i < locList.size(); i++) {
            long nextPattern = swapLocData(boardPattern, spaceLoc, locList.get(i)); //  位置リストから盤状態を求める
            if (!mBoards.containsKey(nextPattern)) {
                //  Boardリストにデータを登録
                mBoards.put(nextPattern, new SlideGameBoard(boardPattern, locList.get(i)));
                //  Scoreリストにデータを登録
                SlideGameScore scoreBoard = new SlideGameScore(getPriority(nextPattern), nextPattern);
                mScoreBoards.add(scoreBoard);
                //  完成チェック
                if (scoreBoard.mScore== 0 || nextPattern == mCompletePattern) {
                    mStat = Status.COMPLETE;                //  解法完了
                    return true;
                } else if (nextPattern == mUnComletePattern) {
                    mStat = Status.UNCOMPLETE;              //  解法不可パターン
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 探索結果の盤の移動駒の数値をリストにする
     * 出力の順番は完成から問題パターンへ移動する駒の数値
     * 逆順にすると解法順となる
     * @return      数値リスト
     */
    public List<Integer> getResult() {
        List<Integer> result = new ArrayList<Integer>();
        long board = getCompletePattern();
        int loc = 0;
        do {
            if (mBoards.containsKey(board)) {
                loc = mBoards.get(board).mLoc;
                board= mBoards.get(board).mPreBoard;
                result.add(get4BitData(board, loc));
            } else {
                break;
            }
        } while (0 <= loc);
        return result;
    }


    /**
     * 探索数の取得
     * @return      探索数
     */
    public int getCount() {
        return mBoards.size();
    }

    /**
     *  盤の指定位置同士の値を入れ替える
     * @param board     盤の状態(4bit配列)
     * @param loc1      位置アドレス1
     * @param loc2      位置アドレス2
     * @return          変更後の盤の状態
     */
    private long swapLocData(long board, int loc1, int loc2) {
        int t1 = get4BitData(board, loc1);
        int t2 = get4BitData(board, loc2);
        board = set4BitData(board, loc2, t1);
        board = set4BitData(board, loc1, t2);
        return board;
    }


    /**
     * 指定位置から上下左右の位置アドレスリストを求める
     * @param loc   指定位置
     * @return      位置アドレスリスト
     */
    private List<Integer> getSwapLocList(int loc) {
        List<Integer> locList = new ArrayList<Integer>();
        if (0 < (loc / mBoardSize))                 //  上
            locList.add(loc - mBoardSize);
        if ((loc / mBoardSize) < (mBoardSize - 1))  //  下
            locList.add(loc + mBoardSize);
        if (0 < (loc % mBoardSize))                 //  左
            locList.add(loc - 1);
        if ((loc % mBoardSize) < (mBoardSize - 1))  //  右
            locList.add(loc + 1);
        return locList;
    }


    /**
     * 盤の中の空白(0)の位置を求める
     * @param board     盤の4bit配列
     * @return          空白の位置アドレス
     */
    private int getSpaceLoc(long board) {
        for (int i = 0; i < mBoardSize * mBoardSize; i++) {
            if (get4BitData(board, i) == 0)
                return i;
        }
        return -1;
    }

    /**
     * 不一致数を盤の評価点として求める
     * @param board     盤の状態
     * @return          評価点(不一致数)
     */
    private int getPriority(long board) {
        int count = 0;
        int i = 0;
        for (i = 0; i < mBoardSize * mBoardSize - 1; i++) {
            if (get4BitData(board, i) != i+1)
                count++;
        }
        if (get4BitData(board, i) != 0)
            count++;
        return count;
    }

    /**
     * 完成形の4bit配列を求める
     * @return      4bit配列
     */
    private long getCompletePattern() {
        byte[] pat = new byte[mBoardSize * mBoardSize];
        for (int i=0; i < pat.length -1; i++) {
            pat[i] = (byte)(i + 1);
        }
        pat[pat.length - 1] = 0;
        return cnvArray2Long(pat);
    }

    /**
     * 非完成形の4bit配列を求める(解法できないパターン)
     * @return      4bit配列
     */
    private long getUnCompletePattern() {
        byte[] pat = new byte[mBoardSize * mBoardSize];
        for (int i = 0; i < pat.length ; i++) {
            if (pat.length - 3 == i) {
                pat[i] = (byte)(pat.length - 1);
            } else if (pat.length - 2 == i) {
                pat[i] = (byte)(pat.length - 2);
            } else if (pat.length - 1 == i) {
                pat[i] = 0;
            } else {
                pat[i] = (byte)(i + 1);
            }
        }
        return cnvArray2Long(pat);
    }

    /**
     * byte配列をlong(4bitx16)配列に変換する
     * @param byteArray byte配列
     * @return          longの4bit配列
     */
    private long cnvArray2Long(byte[] byteArray) {
        long board = 0;
        for (int i = 0; i < byteArray.length; i++) {
            board |= (long)byteArray[i] << (i * 4);
        }
        return board;
    }

    /**
     * long(4Bitx16)配列をbyte配列に変換
     * @param bitVal    longのbit配列
     * @return          byte配列
     */
    private byte[] cnvLong2Array(long bitVal) {
        byte[] array = new byte[mBoardSize * mBoardSize];
        for (int i = 0; i < mBoardSize * mBoardSize; i++) {
            array[i] = (byte)get4BitData(bitVal, i);
        }
        return array;
    }

    /**
     * 4bit配列のn番目に値を設定する
     * @param bitVal    4bit配列データ
     * @param n         設定位置
     * @param data      設定する値
     * @return          4bit配列データ
     */
    private long set4BitData(long bitVal, int n, int data) {
        long t = (long)0x0f << (n * 4);
        bitVal &= ~t;
        bitVal |= (long)data << (n * 4);
        return bitVal;
    }

    /**
     * 4bit配列からn番目の値を取り出す
     * @param bitVal    4bit配列
     * @param n         取出し位置
     * @return          値
     */
    private int get4BitData(long bitVal, int n) {
        return (int)(bitVal >> (n * 4)) & 0xf;
    }
}
