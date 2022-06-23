package jp.co.yoshida.katsushige.mylib

import android.graphics.Paint
import android.util.Log
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 表データのグラフィック表示
 *
 * コンストラクタ
 *      kdraw.initSurface(holder, this)
 *  surfaceCreated
 *      kdraw.setInitScreen(width, height)  //  スクリーンサイズの設定
 *      kdraw.mScreenInverted = false       //  表では倒立表示しない
 *      kdraw.mView = RectD(mLeftMargin, mTopMargin, width - mRightMargine, height - mBottomMargin)
 *
 *  fun setInit(width: Int, height: Int)            初期化と領域(View,World)の設定
 *  fun setSheetData(dataList: List<List<String>>)  表データ設定
 *  fun drawTable()                                 表の描画
 *
 *  mTextSize                                       文字サイズで表の大きさ変更
 *  setRowOffset(-(mTableView.dispRowCount() - 1))  上移動設定
 *  etRowOffset(mTableView.dispRowCount() - 1)      下移動設定
 *  setColOffset(mTableView.dispColCount() - 1)     右移動設定
 *  setColOffset(if (colLeft == 0) -1 else colLeft) 左移動設定
 *
 *      setColOffset(colRight: Int)                 列の表示オフセット設定
 *      setRowOffset(rowUp: Int)                    行の表示位置設定
 *      setTitleColor(col: Int, color: String)      列番指定で列タイトルの背景色を設定
 *      setSelectList(colTitle: String, allDisp: Boolean=false) タイトル名から選択カラムを設定
 *      setSelectColor(color: Int)                  選択した列の色設定する
 *
 *      dispColCount(): Int                         タイトル列を除いて表示できる列数
 *      dispRowCount(): Int                         タイトル行を除いて表示できる行数

 *      getRowPosition(pos: PointD): Int            指定位置の行数を求める
 *      getColPosition(pos: PointD): Int            指定位置の列数を求める
 *      getDispList(): List<Boolean>                表示データリストを取得する(行タイトル列は除く)
 *      getDispDataCount(): Int                     表示するデータの列数を求める
 *      getSelectColNo(): Int                       選択カラムの列番号を返す

 *      initTitleColor()                            列タイトルのセル背景色の初期設定
 *      initDispList()                              表示データ列リストを初期化する
 *      initColorList()                             カラーリストの初期化(列番を設定)
 *      initColSelectList(sel: Boolean=true)        列選択リストの初期化
 *      initRowSelectList(sel: Boolean=true)        行選択リストを初期化する
 *  private
 *      drawCell(row: Int, col: Int)                各セルの内容表示
 *      setMaxOffset()                              最大オフセット値を画面の表示数求める
 *      setCellWidth()                              表示範囲で各列のセルの最大幅を求める
 *      getCellRect(row: Int, col: Int): RectD      セルの大きさの取得
 *      getCelLeftTop(row: Int, col: Int): PointD   セルの大きさの取得
 *
 */
class TableDraw: KDraw() {

    var mDataList = listOf<List<String>>()
    var mColSelectList = mutableListOf<Boolean>()       //  列選択リスト
    var mRowSelectList = mutableListOf<Boolean>()       //  行選択リスト
    var mDispList = mutableListOf<Boolean>()            //  表示/非表示リスト
    var mColorList = mutableListOf<Int>()               //  カラー番号リスト
    var mTableTextSize = mTextSize                      //  表表示文字サイズ
    private var mSaveTextSize = mTextSize               //  TextSizeの一時保存
    private var mInfoTextSize = 25.0
    private var mCellHeight = mTableTextSize * 1.2
    private var mTextMargin = 5.0
    private var mCellSize = mutableListOf<Double>()
    private var mTitleRowBackColor = "Lavender"
    private var mTitleColBackColor = "Lavender"
    private var mSelectCellBackColor = "Cyan"
    var mTitleRowCellBackColor = mutableListOf<String>()
    var mColOffset = 0
    var mRowOffset = 0
    var mColMaxOffset = 0
    var mRowMaxOffset = 0
    var mSelectRow = -1                                 //  選択行
    var mSelectCol = -1                                 //  選択列

    /**
     * 初期化と領域(View,World)の設定
     * with         View幅
     * height       View高さ
     */
    fun setInit(width: Int, height: Int) {
        setInitScreen(width, height)
        mView = RectD(10.0, 5.0, width - 10.0, height - mInfoTextSize - 5.0)
        mWorld = RectD(100.0, 100.0, 1000.0, 1000.0)
    }


    /**
     * データの設定
     */
    fun setSheetData(dataList: List<List<String>>) {
        mDataList = dataList
        mColMaxOffset = mDataList.maxByOrNull { it.size }?.size?:0
        mRowMaxOffset = mDataList.size
        mColOffset = 0
        mRowOffset = 0
        selectClear()
        initTitleColor()     //  列タイトルセル背景色の初期設定
        initRowSelectList(false)
    }

    /**
     * 表の描画
     */
    fun drawTable() {
        if (mDataList.size == 0)
            return
        Log.d(TAG,"drawTable:0 "+mColOffset+" "+mRowOffset+" "+mColMaxOffset+" "+mRowMaxOffset)
        mSaveTextSize = mTextSize
        setTextSize(mTableTextSize.toDouble())
        mCellHeight = mTableTextSize * 1.2
        setCellWidth()
        setMaxOffset()
        Log.d(TAG,"drawTable: "+mColOffset+" "+mRowOffset+" "+mColMaxOffset+" "+mRowMaxOffset)
        //  表の表示
        for (row in 0..mDataList.lastIndex) {
            if (mRowOffset + dispRowCount() < row)
                break
            if (row != 0 && row <= mRowOffset)
                continue
            //  行単位の表示
            for (col in 0..mDataList[row].lastIndex) {
                if (mColOffset + dispColCount() < col)
                    break
                if (col != 0 && col <= mColOffset)
                    continue
                drawCell(row, col)
            }
        }
        //  表の情報表示(最下部)
        mColor = "Black"
        setStyle(Paint.Style.FILL)
        setTextSize(mInfoTextSize)
        val info = "列 "+mColOffset+"/"+mDataList[0].size+" 行 "+mRowOffset+"/"+mDataList.size
        drawText(info, PointD(10.0, mView.height() + mInfoTextSize))

        mTextSize = mSaveTextSize
    }

    /**
     * 各セルの内容表示
     */
    private fun drawCell(row: Int, col: Int, backColor: String = "White") {
//        Log.d(TAG,"drawCell: "+row+" "+col)
        //  列/行タイトルの時はoffsetなし
        val rowOffset = mRowOffset
        if (row == 0)
            mRowOffset = 0
        val colOffset = mColOffset
        if (col == 0)
            mColOffset = 0
        //  背景色設定
        if (row == 0 && 0 <= col)                   //  列タイトルの背景色
            mColor = mTitleRowCellBackColor[col]    //  セルの背景色
        else if (col == 0 && mRowSelectList[row])   //  行タイトル背景色
            mColor = mSelectCellBackColor
        else if (col == 0)
            mColor = mTitleColBackColor
        else if (row == 0)
            mColor = mTitleRowBackColor
        else if (row == mSelectRow || col == mSelectCol)
            mColor = mSelectCellBackColor
        else
            mColor = backColor
        //  セル背景表示
        setStyle(Paint.Style.FILL)
        drawRect(getCellRect(row, col))
        //  セル枠表示
        mColor = "Black"
        setStyle(Paint.Style.STROKE)
        drawRect(getCellRect(row, col))
        //  セル内文字表示
        val sp = getCelLeftTop(row + 1, col)
        sp.offset(mTextMargin, 0.0)
        drawText(mDataList[row][col], sp)

        mRowOffset = rowOffset
        mColOffset = colOffset
    }

    /**
     * 指定値の行を選択色にする
     * pos      スクリーン座標
     */
    fun selectRow(pos: PointD) {
        mSelectRow = getRowPosition(pos)
        mSelectCol = -1
    }

    /**
     * 指定値の行を選択色にする
     * pos      スクリーン座標
     */
    fun selectCol(pos: PointD) {
        mSelectCol = getColPosition(pos)
        mSelectRow = -1
    }

    /**
     * 選択行・列をクリア
     */
    fun selectClear() {
        mSelectRow = -1
        mSelectCol = -1
    }

    /**
     * 列の表示オフセット設定
     * colRight     表示列の移動列数
     */
    fun setColOffset(colRight: Int) {
        mColOffset += colRight
        if (mColOffset < 0) {
            mColOffset = 0
        } else if (mColMaxOffset <= mColOffset) {
            mColOffset = mColMaxOffset
        }
        Log.d(TAG,"setColOffset: "+colRight+" "+mColOffset+" "+mColMaxOffset)
    }

    /**
     * 行の表示位置設定
     * rowDown      表示行の移動行数
     */
    fun setRowOffset(rowUp: Int) {
        mRowOffset += rowUp
        if (mRowOffset < 0) {
            mRowOffset = 0
        } else if (mRowMaxOffset < mRowOffset)  {
            mRowOffset = mRowMaxOffset
        }
        Log.d(TAG,"setRowOffset: "+rowUp+" "+mRowOffset+" "+mRowMaxOffset)
    }

    /**
     * 最大オフセット値を画面の表示数求める
     * 最大オフセット = データの行数/列数 - 表示できる行数/列数
     */
    private fun setMaxOffset() {
        //  列の最大オフセット値
        var i = mDataList[0].lastIndex
        var cellWidth = 0.0
        while (0 <= i) {
            cellWidth += mCellSize[i]
            if (mView.width() - mCellSize[0] < cellWidth) {
                mColMaxOffset = i
                break
            }
            i--
        }
        //  行の最大オフセット値
        mRowMaxOffset = mDataList.size - dispRowCount()

        mRowMaxOffset = max(0, mRowMaxOffset)
        mColMaxOffset = max(0, mColMaxOffset)
        mColOffset = min(mColOffset, mColMaxOffset)
        mRowOffset = min(mRowOffset, mRowMaxOffset)
    }

    /**
     * 表示範囲で各列のセルの最大幅を求める
     */
    private fun setCellWidth() {
        mCellSize.clear()
        //  列タイトルのセル幅取得
        for (col in 0..mDataList[0].lastIndex) {
            mCellSize.add(measureText(mDataList[0][col]) + mTextMargin * 2.0)
        }
        //  表示範囲でのセル幅取得
        for (row in mRowOffset..min(mDataList.lastIndex, mRowOffset + dispRowCount())) {
            var buf = ""
            for (col in 0..mDataList[row].lastIndex) {
                if (col < mCellSize.size) {
                    mCellSize[col] = max(mCellSize[col], measureText(mDataList[row][col]) + mTextMargin * 2.0)
                } else {
                    mCellSize.add(measureText(mDataList[row][col]) + mTextMargin * 2.0)
                }
                buf += " " + mCellSize[col]
            }
//            Log.d(TAG, "setCellWidth: "+row+" "+mCellSize.size+" "+buf)
        }
    }

    /**
     * 列タイトルのセル背景色の初期設定
     */
    fun initTitleColor() {
        if (0 < mDataList.size) {
            mTitleRowCellBackColor.clear()
            for (i in 0..mDataList[0].lastIndex) {
                mTitleRowCellBackColor.add(mTitleRowBackColor)
            }
        }
    }

    /**
     * 列番指定で列タイトルの背景色を設定
     * col      列番号
     * color    背景色の色
     */
    fun setTitleColor(col: Int, color: String) {
        if (col < mTitleRowCellBackColor.size)
            mTitleRowCellBackColor[col] = color
    }

    /**
     * セルの大きさの取得
     * row      行番号
     * col      列番号
     */
    private fun getCellRect(row: Int, col: Int): RectD {
        return RectD(getCelLeftTop(row, col), getCelLeftTop(row+1, col+1))
    }

    /**
     * セルの左上の座標取得
     * row      行番号
     * col      列番号
     */
    private fun getCelLeftTop(row: Int, col: Int): PointD {
        val sp = PointD()
        if (col ==0)
            sp.x = mView.left
        else
            sp.x = mView.left + mCellSize[0] + mCellSize.subList(mColOffset + 1, col).sum()
        sp.y = mView.top + (row - mRowOffset) * mCellHeight
//        Log.d(TAG,"getCelLeftTop: "+sp.x+" "+sp.y)
        return sp
    }

    /**
     * タイトル列を除いて表示できる列数
     */
    fun dispColCount(): Int {
        if (0 < mCellSize.size) {
            var sum = mCellSize[0]
            for (i in (mColOffset+1)..mCellSize.lastIndex) {
                sum += mCellSize[i]
                if (mView.width() < sum)
                    return i - mColOffset
            }
            return mCellSize.size - mColOffset
        }
        return 0
    }

    /**
     * タイトル行を除いて表示できる行数
     */
    fun dispRowCount(): Int {
        return floor(mView.height() / mCellHeight).toInt() - 1
    }

    /**
     * 指定座標のセルの位置(行、列)を返す
     * pos          スクリーン座標
     * return       (row, col) 行と列
     */
    fun getCellPosition(pos: PointD): Pair<Int, Int> {
        val col = getColPosition(pos)
        val row = getRowPosition(pos)
        return Pair(row, col)
    }

    /**
     * 指定位置の行数を求める
     * pos      指定座標(スクリーン座標)
     * return   行番号
     */
    fun getRowPosition(pos: PointD): Int {
        if (mCellSize.size == 0)
            return -1
        var row = (pos.y / mCellHeight).toInt()
        if (row == 0)
            return row
        row += mRowOffset
        if (0 <= row && row < mDataList.size)
            return row
        else
            return -1
    }

    /**
     * 指定位置の列数を求める
     * pos      指定座標(スクリーン座標)
     * return   列番号
     */
    fun getColPosition(pos: PointD): Int {
        if (mCellSize.size == 0)
            return -1
        var colx = mCellSize[0]
        if (pos.x < colx)
            return 0
        val colCount = dispColCount() + mColOffset
        var buf = colx.toString()
        for (col in (mColOffset + 1)..colCount) {
            if (mCellSize.size <= col)
                return -1
            if (colx < pos.x && pos.x < colx + mCellSize[col]) {
                return col
            }
            colx += mCellSize[col]
            buf += " "+colx.toString()
        }
        return -1
    }


    /**
     * 表示データリストを取得する(行タイトル列は除く)
     * return   表示データリスト
     */
    fun getDispList(): List<Boolean> {
        return mDispList.slice(1..mDispList.lastIndex)
    }

    /**
     * 表示するデータの列数を求める
     * return   表示する列の数
     */
    fun getDispDataCount(): Int {
        return mDispList.slice(1..mDispList.lastIndex).count { it }
    }

    /**
     * タイトル名から選択カラムを設定
     * colTitle     選択対象列名
     * allDisp      表示データのみ
     */
    fun setSelectList(colTitle: String, allDisp: Boolean=false) {
        initColSelectList(false)
        for (i in mDataList[0].indices) {
            if ((allDisp || mDispList[i]) && mDataList[0][i].compareTo(colTitle) == 0 )
                mColSelectList[i] = true
        }
    }

    /**
     * 選択カラムの列番号を返す
     * returm       列番工(0> 選択なし)
     */
    fun getSelectColNo(): Int {
        for (i in mColSelectList.indices) {
            if (mColSelectList[i])
                return i
        }
        return -1
    }

    /**
     * 選択した列の色設定する
     * color        色番号
     */
    fun setSelectColor(color: Int) {
        for (i in mColSelectList.indices)
            if (mColSelectList[i])
                mColorList[i] = color
    }
    /**
     * 表示データ列リストを初期化する
     */
    fun initDispList() {
        mDispList.clear()
        for (col in mDataList[0].indices) {
            mDispList.add(true)
        }
    }

    /**
     * カラーリストの初期化(列番を設定)
     */
    fun initColorList() {
        mColorList.clear()
        for (col in mDataList[0].indices) {
            mColorList.add(col)
        }
    }

    /**
     * 列選択リストの初期化
     */
    fun initColSelectList(sel: Boolean=true) {
        mColSelectList.clear()
        for (col in mDataList[0].indices) {
            mColSelectList.add(sel)
        }
    }

    /**
     * 行選択リストを初期化する
     * set      初期化フラグ(初期値 true)
     */
    fun initRowSelectList(sel: Boolean=true) {
        mRowSelectList.clear()
        for (row in mDataList.indices) {
            mRowSelectList.add(sel)
        }
    }


}