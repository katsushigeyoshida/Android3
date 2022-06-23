package jp.co.yoshida.katsushige.tablegraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.Log
import android.view.View
import jp.co.yoshida.katsushige.mylib.KDraw
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD
import kotlin.math.max
import kotlin.math.min

class TableSheetView(context: Context): View(context) {
    private val TAG = "TableSheetView"

    var mSheetData = SheetData()
    var mTextSize = 40.0
    private var mCellHeight = mTextSize * 1.2
    private var mTextMargin = 5.0
    private var mCellSize = mutableListOf<Double>()
    var mCellTitleColor = mutableListOf<String>()
    var mColOffset = 0
    var mRowOffset = 0
    var mColMaxOffset = 0
    var mRowMaxOffset = 0

    private var kdraw = KDraw()

    /**
     * Windowが開かれた時に呼ばれ、Window Sizeが取得できる
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        //  Window Sizeの取得と設定
        kdraw.setInitScreen(width, height)
        kdraw.mView = RectD(10.0, 5.0, width - 10.0, height - mTextSize)
        kdraw.mWorld = RectD(100.0, 100.0, 1000.0, 1000.0)
        Log.d(TAG, "onWindowFocusChanged: "+kdraw.mView.left+" "+kdraw.mView.top+" "+kdraw.mView.right+" "+kdraw.mView.bottom)
    }

    /**
     * 描画処理
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //  外枠の表示
        kdraw.mCanvas = canvas
        kdraw.mColor = "Red"
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawRect(kdraw.mView)
        //  データの表表示
        drawTable()
    }

    /**
     * 再描画
     */
    fun reDraw(){
        invalidate()
    }

    /**
     * データの設定
     */
    fun setSheetData(sheetData: SheetData) {
        mSheetData = sheetData
        //  表示セルの最大オフセットの初期設定
        mColMaxOffset = mSheetData.mDataList.maxByOrNull { it.size }?.size?:0
        mRowMaxOffset = mSheetData.mDataList.size
        mColOffset = 0
        mRowOffset = 0
        initTitleColor()     //  列タイトルセル背景色の初期設定
        mSheetData.initRowSelectList(false)
    }

    /**
     * 表の描画
     */
    private fun drawTable() {
        if (mSheetData.mDataList.size == 0)
            return
        kdraw.setTextSize(mTextSize)
        mCellHeight = mTextSize * 1.2
        setCellWidth()
        setMaxOffset()
        Log.d(TAG,"drawTable: "+mColOffset+" "+mRowOffset)
        //  表の表示
        for (row in 0..mSheetData.mDataList.lastIndex) {
            if (mRowOffset + dispRowCount() < row)
                break
            if (row != 0 && row <= mRowOffset)
                continue
            //  行単位の表示
            for (col in 0..mSheetData.mDataList[row].lastIndex) {
                if (mColOffset + dispColCount() < col)
                    break
                if (col != 0 && col <= mColOffset)
                    continue
                drawCell(row, col)
            }
        }
        //  表の情報表示(最下部)
        kdraw.mColor = "Black"
        kdraw.setStyle(Paint.Style.FILL)
        kdraw.setTextSize(35.0)
        val info = "列 "+mColOffset+"/"+mSheetData.mDataList[0].size+" 行 "+mRowOffset+"/"+mSheetData.mDataList.size
        kdraw.drawText(info, PointD(10.0, height.toDouble()))
    }

    /**
     * 各セルの内容表示
     */
    private fun drawCell(row: Int, col: Int) {
//        Log.d(TAG,"drawCell: "+row+" "+col)
        //  列/行タイトルの時はoffsetなし
        val rowOffset = mRowOffset
        if (row == 0)
            mRowOffset = 0
        val colOffset = mColOffset
        if (col == 0)
            mColOffset = 0
        //  背景色設定
        if (row == 0 && 0 <= col)                               //  列タイトルの背景色
            kdraw.mColor = mCellTitleColor[col]
        else if (col == 0 && mSheetData.mRowSelectList[row])    //  行タイトル背景色
            kdraw.mColor = "Cyan"
        else
            kdraw.mColor = "White"
        //  セル背景表示
        kdraw.setStyle(Paint.Style.FILL)
        kdraw.drawRect(getCellRect(row, col))
        //  セル枠表示
        kdraw.mColor = "Black"
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawRect(getCellRect(row, col))
        //  セル内文字表示
        val sp = getCelLeftTop(row + 1, col)
        sp.offset(mTextMargin, 0.0)
        kdraw.drawText(mSheetData.mDataList[row][col], sp)

        mRowOffset = rowOffset
        mColOffset = colOffset
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
        var i = mSheetData.mDataList[0].lastIndex
        var cellWidth = 0.0
        while (0 <= i) {
            cellWidth += mCellSize[i]
            if (kdraw.mView.width() - mCellSize[0] < cellWidth) {
                mColMaxOffset = i
                break
            }
            i--
        }
        //  行の最大オフセット値
        mRowMaxOffset = mSheetData.mDataList.size - dispRowCount()

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
        for (col in 0..mSheetData.mDataList[0].lastIndex) {
            mCellSize.add(kdraw.measureText(mSheetData.mDataList[0][col]) + mTextMargin * 2.0)
        }
        //  表示範囲でのセル幅取得
        for (row in mRowOffset..min(mSheetData.mDataList.lastIndex, mRowOffset + dispRowCount())) {
            var buf = ""
            for (col in 0..mSheetData.mDataList[row].lastIndex) {
                if (col < mCellSize.size) {
                    mCellSize[col] = max(mCellSize[col], kdraw.measureText(mSheetData.mDataList[row][col]) + mTextMargin * 2.0)
                } else {
                    mCellSize.add(kdraw.measureText(mSheetData.mDataList[row][col]) + mTextMargin * 2.0)
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
        if (0 < mSheetData.mDataList.size) {
            mCellTitleColor.clear()
            for (i in 0..mSheetData.mDataList[0].lastIndex) {
                mCellTitleColor.add("White")
            }
        }
    }

    /**
     * 列番指定で列タイトルの背景色を設定
     * col      列番号
     * color    背景色の色
     */
    fun setTitleColor(col: Int, color: String) {
        if (col < mCellTitleColor.size)
            mCellTitleColor[col] = color
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
            sp.x = kdraw.mView.left
        else
            sp.x = kdraw.mView.left + mCellSize[0] + mCellSize.subList(mColOffset + 1, col).sum()
        sp.y = kdraw.mView.top + (row - mRowOffset) * mCellHeight
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
                if (kdraw.mView.width() < sum)
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
        return (kdraw.mView.height() / mCellHeight).toInt() - 1
    }

    /**
     * 指定位置がグラフ内かを確認
     * pos      指定位置
     * return   グラフ内
     */
    fun chkPosition(pos: PointD): Boolean {
        if (mCellSize.size == 0)
            return false
        var row = (pos.y / mCellHeight).toInt()
        if (row < 0 || mSheetData.mDataList.size < row)
            return false
        if (pos.x < mCellSize[0])
            return false
        if (dispColCount() + mColOffset < pos.x)
            return false
        return true
    }

    /**
     * 指定した2点のオフセット値を求める
     * pos1     指定点1
     * pos2     指定点2
     * return   列,行
     */
    fun offsetColRow(pos1: PointD, pos2: PointD): Point {
        var dis = Point()
        dis.x = getColPosition(pos2) - getColPosition(pos1)
        dis.y = getRowPosition(pos2) - getRowPosition(pos1)
        return dis
    }

    /**
     * 指定位置の行数を求める
     * pos      指定座標
     * return   行番号
     */
    fun getRowPosition(pos: PointD): Int {
        if (mCellSize.size == 0)
            return -1
        var row = (pos.y / mCellHeight).toInt()
        if (row == 0)
            return row
        row += mRowOffset
        if (0 <= row && row < mSheetData.mDataList.size)
            return row
        else
            return -1
    }

    /**
     * 指定位置の列数を求める
     * pos      指定座標
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
}