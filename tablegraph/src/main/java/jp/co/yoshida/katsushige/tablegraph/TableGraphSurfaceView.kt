package jp.co.yoshida.katsushige.tablegraph

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import jp.co.yoshida.katsushige.mylib.KDraw
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD

class TableGraphSurfaceView: SurfaceView, SurfaceHolder.Callback {
    val TAG = "TableGraphSurfaceView"

    var mStepYSize = 1.0            //  縦軸目盛のステップサイズ
    var mStepXSize = 1.0            //  横軸目盛のステップサイズ
    var mLeftMargin = 150.0         //  左マージン(スクリーンサイズ)
    var mTopMargin = 230.0          //  下部(倒立時)マージン(スクリーンサイズ)
    var mRightMargine = 40.0        //  右マージン(スクリーンサイズ)
    var mBottomMargin = 20.0        //  上部(倒立時)マージン(スクリーンサイズ)
    var mColors = listOf(           //  グラフの色名(10色)
        "Black", "Red", "Blue", "Green", "Yellow", "Cyan", "Gray",
        "LightGray", "Magenta", "DarkGray"
    )
    val mScaleFormat = listOf("%,.0f", "%,.3f", "%,.5f")
    enum class GRAPHTYPE { LINE, BAR, STACKELINE, STACKEDBAR }
    var mGraphType = GRAPHTYPE.LINE
    var mStartPosition = 0
    var mEndPosition = -1
    var mSheetData = SheetData()

    var kdraw = KDraw()
    var klib = KLib()

    constructor(context: Context, sheetData: SheetData) : super(context) {
        Log.d(TAG,"TableGraphSurfaceView: constractor: "+width + " " + height)
        mSheetData = sheetData
        kdraw.initSurface(holder, this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: "+ width + " " + height)

        //  スクリーンサイズの設定
        kdraw.setInitScreen(width, height)
        //  倒立表示
        kdraw.mScreenInverted = true
        //  ビュー領域を設定
        kdraw.mView = RectD(mLeftMargin, mTopMargin, width - mRightMargine, height - mBottomMargin)
        //  ワールド領域を初期設定
        var area = mSheetData.mDataArea
        kdraw.mWorld = RectD(area.left, Math.min(0.0, area.top), area.right, area.bottom)
        //  文字サイズと文字太さを初期設定
        kdraw.mTextSize = 30f
        kdraw.mTextStrokeWidth = 2f

        kdraw.initSurfaceScreen(Color.LTGRAY, width, height)
        drawGraph()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceCreated: "+ width + " " + height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed: ")
        kdraw.termSurface()
    }

    /**
     * グラフの表示
     */
    fun drawGraph() {
        if (0 < mSheetData.mDoubleDataList.size && 1 < mSheetData.mDoubleDataList[0].size
            && 0 < mSheetData.mDataArea.width() && 0 < mSheetData.mDataArea.height()) {
            initGraphArea()
            drawGraphData()
        }
    }

    /**
     * グラフの画面表示
     */
    fun initGraphArea() {
        //  グラフの領域
        var area = mSheetData.getArea(mStartPosition, mEndPosition,
            mGraphType == GRAPHTYPE.STACKEDBAR || mGraphType == GRAPHTYPE.STACKELINE)
        kdraw.mWorld = RectD(area.left, Math.min(0.0, area.top), area.right, area.bottom)
        //  目盛のステップサイズを設定
        setStepSize()
        //  ステップサイズや上部下部マージンを補正してワールド座標を再設定
        setWorldArea()
        //  棒グラフ時に領域を拡張
        if (mGraphType == GRAPHTYPE.BAR || mGraphType == GRAPHTYPE.STACKEDBAR) {
            var barWidth = mSheetData.getMinmumRowDistance(0)        //  棒線の幅(棒グラフのみ)
            kdraw.mWorld.left -= barWidth / 2.0
            kdraw.mWorld.right += barWidth / 2.0
        }

        //  ビュー領域を再設定
        kdraw.mView = RectD(mLeftMargin, mTopMargin, width - mRightMargine, height - mBottomMargin)

        //  グラフ枠の表示
        kdraw.lockCanvas()

        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))
        //  補助線と目盛の表示
        setAxis()

        kdraw.unlockCanvasAndPost()
    }

    /**
     * グラフデータの表示
     */
    fun drawGraphData() {
        kdraw.lockCanvas()
        //  表示の前処理
        var sp = mStartPosition
        var ep = if (mEndPosition < 0) mSheetData.mDoubleDataList.size - 1 else Math.min(mEndPosition, mSheetData.mDoubleDataList.size - 1)
        var dispCount = mSheetData.getDispDataCount()                   //  表示するデータの数
        var barWidth = 0.8 * mSheetData.getMinmumRowDistance(0)        //  棒線の幅(棒グラフのみ)
        var barOffset = 0.0                                             //  棒グラフを中心位置からのオフセット
        if (mGraphType == GRAPHTYPE.BAR || mGraphType == GRAPHTYPE.STACKEDBAR) {
            //  棒グラフの幅
            kdraw.setStyle(Paint.Style.FILL)
            barOffset = barWidth / 2.0
            if (mGraphType == GRAPHTYPE.BAR) {
                barWidth = barWidth / dispCount
            }
        } else {
            kdraw.setStyle(Paint.Style.STROKE)
            ep--
        }

        //  各データの表示
        for (row in sp..ep) {
            drawChart(row, barWidth, barOffset)
        }

        //  凡例の表示
        setChartLegend()
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 個別データ表示
     * row          行位置
     * barWidth     棒グラフの棒線の幅
     * barOffset    棒グラフの中心からのオフセット値
     */
    fun drawChart(row: Int, barWidth: Double, barOffset: Double) {
        var count = 0
        var bsp = 0.0
        var bep = 0.0
        for (col in 1..mSheetData.mDoubleDataList[row].lastIndex) {
            if (mSheetData.mDispList[col]) {
                kdraw.mColor = mColors[mSheetData.mColorList[col] % mColors.size]      //  グラフの色設定
                if (mGraphType == GRAPHTYPE.LINE) {
                    //  折線
                    var ps = PointD(mSheetData.mDoubleDataList[row][0], mSheetData.mDoubleDataList[row][col])
                    var pe = PointD(mSheetData.mDoubleDataList[row+1][0], mSheetData.mDoubleDataList[row+1][col])
                    kdraw.drawWLine(ps, pe)
                } else if (mGraphType == GRAPHTYPE.BAR) {
                    //  棒グラフ
                    var ps = PointD(mSheetData.mDoubleDataList[row][0]-barOffset+barWidth*count, 0.0)
                    var pe = PointD(mSheetData.mDoubleDataList[row][0]-barOffset+barWidth*(count+1), mSheetData.mDoubleDataList[row][col])
                    count++
                    kdraw.drawWRect(RectD(ps, pe))
                } else if (mGraphType == GRAPHTYPE.STACKELINE) {
                    //  積上げ式折線
                    var ps = PointD(mSheetData.mDoubleDataList[row][0], bsp + mSheetData.mDoubleDataList[row][col])
                    var pe = PointD(mSheetData.mDoubleDataList[row+1][0], bep + mSheetData.mDoubleDataList[row+1][col])
                    kdraw.drawWLine(ps, pe)
                    bsp += mSheetData.mDoubleDataList[row][col]
                    bep += mSheetData.mDoubleDataList[row+1][col]
                } else if (mGraphType == GRAPHTYPE.STACKEDBAR) {
                    //  積上げ式棒グラフ
                    var ps = PointD(mSheetData.mDoubleDataList[row][0]-barOffset, bsp)
                    var pe = PointD(mSheetData.mDoubleDataList[row][0]+barOffset, bsp + mSheetData.mDoubleDataList[row][col])
                    bsp += mSheetData.mDoubleDataList[row][col]
                    kdraw.drawWRect(RectD(ps, pe))
                }
            }
        }
    }

    /**
     * グラフの拡大縮小
     * zoom     拡大率
     */
    fun zoomDisp(zoom: Double=2.0) {
        var dispWidth = mEndPosition - mStartPosition
        var dispCenter = (mStartPosition + mEndPosition) / 2
        mStartPosition = (dispCenter - dispWidth / (zoom * 2.0)).toInt()
        mEndPosition = (dispCenter + dispWidth / (zoom * 2.0)).toInt()
        mStartPosition = Math.max(0, mStartPosition)
        mEndPosition = Math.min(mSheetData.mDoubleDataList.size - 1, mEndPosition)
    }

    /**
     * グラフを左右に移動する
     * move     移動量(表示画面に対する割合、+は右、-は左)
     */
    fun moveDisp(move: Double=0.5) {
        var dispWidth = mEndPosition - mStartPosition
        var moveWidth = (dispWidth * move).toInt()
        if (0 < moveWidth) {
            if (mSheetData.mDoubleDataList.size - 1 <= mEndPosition + moveWidth)
                moveWidth = mSheetData.mDoubleDataList.size - 1 - mEndPosition
        } else {
            if (mStartPosition + moveWidth < 0)
                moveWidth = -mStartPosition
        }
        mStartPosition += moveWidth
        mEndPosition += moveWidth
        mStartPosition = Math.max(0, mStartPosition)
        mEndPosition = Math.min(mSheetData.mDoubleDataList.size - 1, mEndPosition)
    }

    /**
     * グラフ補助線の間隔を設定
     */
    fun setStepSize() {
        //  縦軸目盛り線の間隔
        mStepYSize = klib.graphStepSize(kdraw.mWorld.height(), 5.0)
        //  横軸軸目盛り線の間隔
        when (mSheetData.mDataType[0]) {
            SheetData.DATATYPE.STRING ->{
                //  横軸目盛り文字列
                mStepXSize = 1.0
            }
            else -> {
                //  横軸目盛り数値,日付など
                mStepXSize = klib.graphStepSize(kdraw.mWorld.width(), 8.0)
                var rowdis = mSheetData.mDataArea.width() / (mSheetData.mDoubleDataList.count() - 1)
                mStepXSize = if (mStepXSize < rowdis * 2.0) rowdis else mStepXSize
            }
        }
    }

    /**
     * ワールド座標領域をステップサイズや上下マージンを加えて設定
     */
    fun setWorldArea() {
        //  グラフエリアの領域(Stepサイズ込み)
        var xOffset = if (mGraphType == GRAPHTYPE.BAR || mGraphType == GRAPHTYPE.STACKEDBAR) 0.5 else 0.0
        kdraw.mWorld.bottom = klib.graphHeightSize(mSheetData.mDataArea.bottom, mStepYSize) //  上端
        kdraw.mWorld.top = if (mSheetData.mDataArea.top < 0.0) ((mSheetData.mDataArea.top / mStepYSize).toInt() - 1) * mStepYSize else 0.0
        kdraw.mWorld.left = mSheetData.mDataArea.left - xOffset
        kdraw.mWorld.right = mSheetData.mDataArea.right + xOffset

        //  縦軸の目盛り文字列の最大幅を求める
        var titleSize = 0.0
        var y = kdraw.mWorld.top
        while (y <= kdraw.mWorld.bottom) {
            titleSize = Math.max(titleSize, kdraw.measureText("%,.0f".format(y)))
            y += mStepYSize
        }
        mLeftMargin = titleSize + kdraw.mTextSize

        //  横軸の目盛り文字列の最大幅を求める
        titleSize = 0.0
        var x = kdraw.mWorld.left
        while (x <= kdraw.mWorld.right) {
            titleSize = Math.max(titleSize, kdraw.measureText(scaleXTitle(x)))
            x += mStepXSize
        }
        mTopMargin = titleSize + kdraw.mTextSize
    }


    /**
     * 補助線と目盛を設定
     */
    fun setAxis() {
        //  横軸の目盛と補助線、タイトル
        var x = mSheetData.mDoubleDataList[mSheetData.mStartRow][0]
        var ex = mSheetData.mDoubleDataList[mSheetData.mEndRow][0]
        xScaleDraw(x, PointD(x, kdraw.mWorld.top))
        while (x < (ex - mStepXSize)) {
            x += mStepXSize
            kdraw.drawWLine(PointD(x, kdraw.mWorld.top), PointD(x, kdraw.mWorld.bottom))
            if (kdraw.mWorld.left + kdraw.mWorld.width() * 0.05 < x &&
                x < kdraw.mWorld.right - kdraw.mWorld.width() * 0.05)
                xScaleDraw(x, PointD(x, kdraw.mWorld.top))
        }
        xScaleDraw(ex, PointD(ex, kdraw.mWorld.top))
//        getXTitleDraw()

        //  縦軸の目盛と補助線、タイトル
        var y = kdraw.mWorld.top
        yScaleDraw(y, PointD(kdraw.mWorld.left, y))
        while (y < kdraw.mWorld.bottom ) {
            y += mStepYSize
            kdraw.drawWLine(PointD(kdraw.mWorld.left, y), PointD(kdraw.mWorld.right, y))
            yScaleDraw(y, PointD(kdraw.mWorld.left, y))
        }
//        getYTitleDraw()
    }

    /**
     * 横軸目盛の表示
     * value        目盛の値
     * pos          表示位置
     */
    fun xScaleDraw(x: Double, pos: PointD) {
        pos.y -= kdraw.cnvScreen2WorldY(kdraw.mTextSize.toDouble()) / 2.0
        kdraw.drawWText(scaleXTitle(x), pos, 90.0, KDraw.HALIGNMENT.Left, KDraw.VALIGNMENT.Center)
    }

    /**
     * 縦軸目盛の表示
     * value        目盛の値
     * pos          表示位置
     */
    fun yScaleDraw(y: Double, pos: PointD) {
        pos.x -= kdraw.cnvScreen2WorldX(kdraw.mTextSize.toDouble()) / 2.0
        kdraw.drawWText(scaleYTitle(y), pos, 0.0, KDraw.HALIGNMENT.Right, KDraw.VALIGNMENT.Center)
    }

    /**
     * X軸目盛り文字列
     * X        X軸目盛値
     */
    fun scaleXTitle(x: Double): String {
        when (mSheetData.mDataType[0]) {
            SheetData.DATATYPE.DATE -> {
                return klib.JulianDay2DateString(x.toInt())
            }
            SheetData.DATATYPE.STRING -> {
                return mSheetData.mDataList[x.toInt()][0]
            }
            SheetData.DATATYPE.NUMBER -> {
                return "%,.0f".format(x)
            }
            else -> {
                return mSheetData.mDataList[x.toInt()][0]
            }
        }
    }

    /**
     * Y軸目盛り文字列
     * y        Y軸目盛値
     */
    fun scaleYTitle(y: Double):String {
        return "%,.0f".format(y)
    }

    /**
     * 凡例表示
     */
    fun setChartLegend() {
        var x = kdraw.mView.left + 30.0
        var y = kdraw.mView.bottom - 40.0
        for (col in mSheetData.mDataList[0].lastIndex downTo 1) {
            if (mSheetData.mDispList[col]) {
                kdraw.mColor = mColors[mSheetData.mColorList[col] % mColors.size]      //  色設定
                kdraw.setStyle(Paint.Style.FILL_AND_STROKE)
                kdraw.drawRect(RectD(PointD(x, y), PointD(x + 20f, y + 20f)))
                kdraw.mColor = "Black"
                kdraw.drawText(mSheetData.mDataList[0][col], PointD(x + 32.0, y + 30.0), 0.0, KDraw.HALIGNMENT.Left, KDraw.VALIGNMENT.Top)
                y -= kdraw.mTextSize * 1.3
            }
        }
    }
}