package jp.co.yoshida.katsushige.gameapp;

import java.util.Random;

public class CreatSudokuProblem {

    //  http://www.net.c.dendai.ac.jp/~ynakajima/index.htm
    //  http://www.net.c.dendai.ac.jp/~ynakajima/furoku.html#7

    private int mBordSize = 9;          //  盤サイズ
    private int mBlockSize = 3;         //  ブロックサイズ
    private int mRepeat = 1000;         //  反復回数
    private Random mRand = new Random();

    public int[] mBoardDef;
    public int[] mBoardMax;
    public int mMaxCountSp;

    public CreatSudokuProblem()
    {
        mBoardDef = new int[mBordSize * mBordSize];
        mBoardMax = new int[mBordSize * mBordSize];
    }

    public int[] getCreatProblem(int n, int repeat)
    {
        int[] board = new int[mBordSize * mBordSize];
        int c = 0;  //  dummy
        int countSp;
        mMaxCountSp = 0;

        mRepeat = repeat;

        for (int j = 0; j < mRepeat; j++) {
            c++;        //  ループごとのランダムシート変更用
            //  盤を0で初期化
            for (int i = 0; i < mBordSize * mBordSize; i++)
                board[i] = 0;

            check(board, 0, c);
            cloneBoard(board, mBoardDef);
            maker2(board, 0);
            countSp = count0(board);

            if (mMaxCountSp < countSp) {
                mMaxCountSp = countSp;
                cloneBoard(board, mBoardMax);
            }

            if (n < countSp) {
                return board;
            }
        }
        return mBoardMax;
    }

    /***
     * 完成形の作成
     * 左上から順に数を入れていくバックトラックのアルゴリズムを使用
     * 数を入れてcanBePlaced()で正当性をチェック
     * @param board
     * @param pos
     * @param n
     * @return
     */
    private boolean check(int[] board, int pos, int n)
    {
        //  完成形を作成
        int i;
        int x;
        int newPos;
        int j = 0;

        //  Solution is found
        if (pos >= mBordSize * mBordSize)
            return false;
        //throw new Exception();      //  例外処理で抜ける

        //  Find a blank
        for (newPos = pos; newPos < mBordSize * mBordSize; ++newPos) {
            if (board[newPos] == 0)
                break;
        }

        //  Check recursively
        for (x = 0; x < mBordSize; ++x) {
            int[] randBoard9 = new int[mBordSize];
            //  randBoard9 = 1-9 の値がランダムに入った配列、 n は　randomのシード値を変える
            rand9(randBoard9, n);
            int y = randBoard9[x];
            if (canBePlaced(board, newPos, y)) {
                //  if （boardのnewPosにrand9(x)が入れられるなら）
                board[newPos] = y;
                if (!check(board, newPos + 1, n + 1))
                    return false;
                board[newPos] = 0;  //  backtracking
            }
        }
        return true;
    }

    /**
     * １～９までの数字がランダム順に格納された配列を作成する関数
     * １～９までの数字が順に入った配列を作成し、その中身をシャッフルする
     * シャッフルは１番目とランダム番目をスワップ、２番目とランダム番目をスワップ…という行為を９番目まで行う
     * @param randBoard
     * @param n             乱数のシードを変えるための値(dummy)
     */
    private void rand9(int[] randBoard, int n)
    {
        //  1-9の値をランダムに並び替える
        for (int x = 0; x < mBordSize; x++)
            randBoard[x] = x + 1;
        //  numbersをランダムに並び替える
        for (int i = 0; i < mBordSize; i++) {
            int j = mRand.nextInt(mBordSize);
            if (i != j) {
                int tmp = randBoard[i];
                randBoard[i] = randBoard[j];
                randBoard[j] = tmp;
            }
        }
    }

    /**
     * 0～80までの数字がランダム順に格納された配列を作成
     * maker1でランダム順に穴を空けていくために使用
     * @param randBoard
     * @param n
     */
    private void rand81(int[] randBoard, int n)
    {
        //  0-80の値をランダムに並び替える
        for (int x = 0; x < mBordSize * mBordSize; x++)
            randBoard[x] = x;
        //  numbersをランダムに並び替える
        for (int i = 0; i < mBordSize * mBordSize; i++) {
            int j = mRand.nextInt(mBordSize * mBordSize);
            if (i != j) {
                int tmp = randBoard[i];
                randBoard[i] = randBoard[j];
                randBoard[j] = tmp;
            }
        }
    }

    /**
     * 仮に入れた値が縦・横・その値の所属3x3マス(大枠)にあれば0返す。なければ1を返す
     * @param board     盤データ
     * @param pos       指定位置
     * @param x         指定位置の値
     * @return
     */
    private boolean canBePlaced(int[] board, int pos, int x)
    {
        //  check関数で仮に入れた値が縦・横・その値の所属3x3マス(大枠)にあれば0返す。なければ1を返す
        int row = pos / mBordSize;      //  行
        int col = pos % mBordSize;      //  列
        int i, j, topLeft;

        //  縦と横の重複チェック
        for (i = 0; i < mBordSize; ++i) {
            if (board[row * mBordSize + i] == x)
                return false;
            if (board[col + i * mBordSize] == x)
                return false;
        }
        //  ブロック内の重複チェック()ブロックの左上の位置を求めて行う)
        topLeft = mBordSize * (row / mBlockSize) * mBlockSize + (col / mBlockSize) * mBlockSize;
        for (i = 0; i < mBlockSize; ++i) {
            for (j = 0; j < mBlockSize; ++j) {
                if (board[topLeft + i * mBordSize + j] == x)
                    return false;
            }
        }
        return true;
    }

    /**
     * 配列内の0の数を数える
     * 作成した問題の空欄の数を調べるのに使用
     * @param board
     * @return
     */
    public int count0(int[] board)
    {
        int c = 0;
        for (int i = 0; i < mBordSize * mBordSize; i++)
            if (board[i] == 0)
                c++;
        return c;
    }

    /**
     * 配列のコピーを行う
     * @param boardOriginal 複製元
     * @param boardCopy     複製先
     */
    private void cloneBoard(int[] boardOriginal, int[] boardCopy)
    {
        //  前の配列を後ろの配列にコピー
        for (int i = 0; i < mBordSize * mBordSize; i++)
            boardCopy[i] = boardOriginal[i];
    }

    /**
     * 穴を開けるプログラム(完全にランダム順に穴を開けていくプログラム)
     * ランダムに指定した場所を仮に0で置き換える
     * そこに１～９の数字を入れていき、canBePlaced関数を用いてそこに入れることができるかを調べ、
     * その結果そこに入る数字が元々入っていた数字のみならばそこは０で置き換えたままで、
     * そうでない場合は元の数字に戻す
     * @param board
     * @param n
     */
    private void maker1(int[] board, int n)
    {
        //  問題作成1 ランダム
        int[] randBoard81 = new int[mBordSize * mBordSize];
        rand81(randBoard81, n);

        for (int x = 0; x < mBordSize * mBordSize; x++) {
            int c = 0;
            int tmp = board[randBoard81[x]];
            board[randBoard81[x]] = 0;
            for (int i = 1; i <= mBordSize; i++) {
                if (canBePlaced(board, randBoard81[x], i))
                    c++;
            }
            if (c != 1) {
                board[randBoard81[x]] = tmp;
            }
        }
    }

    /**
     * 穴を開けるプログラム穴を開けるプログラム
     * 穴を開ける順所を完全に指定し、真ん中→上→左→右→下→四隅の順に穴を開けるようにしたもの
     * 81個の要素をもつ配列を全て手入力で入力することで順番の指定を行った
     * @param board
     * @param n
     */
    private void maker2(int[] board, int n)
    {
        //  問題作成 順番指定
        int[] cntBoard = new int[] {
                46,47,48,10,11,12,55,56,57,
                49,50,51,13,14,15,58,59,60,
                52,53,54,16,17,18,61,62,63,
                19,20,21, 1, 2, 3,28,29,30,
                22,23,24, 4, 5, 6,31,32,33,
                25,26,27, 7, 8, 9,34,35,36,
                64,65,66,37,38,39,73,74,75,
                67,68,69,40,41,42,76,77,78,
                70,71,72,43,44,45,79,80,81,
        };
        for (int x = 0; x < mBordSize * mBordSize; x++) {
            int c = 0;
            int tmp = board[cntBoard[x] - 1];
            board[cntBoard[x] - 1] = 0;
            for (int i = 1; i <= mBordSize; i++) {
                if (canBePlaced(board, cntBoard[x] - 1, i))
                    c++;
            }
            if (c != 1)
                board[cntBoard[x] - 1] = tmp;
        }
    }
}
