package jp.co.yoshida.katsushige.mapapp

import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.SizeF
import android.view.View
import jp.co.yoshida.katsushige.mylib.*

//import kotlinx.android.synthetic.main.activity_main.*

class MapView(context: Context,var mMapData: MapData): View(context) {
    val TAG = "MapView"

    var mWidth = mMapData.mView.width
    var mHeight = mMapData.mView.height
    var mColCount = 1
    var mRowCount = 1
    var mCellSize = 0
    var mGCells = mutableListOf<GCell>()
    var mOffset = PointD(0.0, 0.0)
    var mMarkList = MarkList()              //  マークのデータ
    var mMeasure = Measure()                //  距離測定実行中のデータ
    var mGpsTrace = GpsTrace()              //  GPSトレース実行中のデータ
    var mGpsDataList = GpsDataList()        //  GPSデータ
    var mElevator = 0.0                     //  標高値(Mainから設定)
    var mColor = ""
    var mComment = ""

    var klib = KLib()
    val kdraw = KDraw()

    init {
        setCellBoard()
    }

    //  描画処理
    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
        //  セルの表示
        for (cell in mGCells) {
            cell.draw(canvas)
        }

        //  マークのの表示
        mMarkList.draw(canvas, mMapData)
        //  測定線の表示
        mMeasure.draw(canvas, mMapData)
        //  GPSトレースの表示
        mGpsTrace.draw(canvas, mMapData)
        //  GPSデータ表示
        mGpsDataList.draw(canvas, mMapData)
        //  中心線
        drawCross(canvas)
        //  座標と標高の表示
        drawCoordinates(canvas, mMapData)
    }

    //  再表示
    fun reDraw() {
        invalidate()
    }

    //  座標表示と標高、GPSトレースデータ、地質図凡例表示
    fun drawCoordinates(canvas: Canvas, mapData: MapData) {
        //  表示位置
        var x = 10f
        var y = 40f
        //  中心座標と標高
        var bp = mapData.screen2BaseMap(getCenter())
        var cp = mapData.baseMap2Coordinates(bp)
        var coordeMsg = " 座標 "+"%3.6f".format(cp.y)+","+"%3.6f".format(cp.x)
        var ele = mElevator
        var type = "MAP"
        if (mGpsTrace.mTraceOn && 0 < mGpsTrace.mGpsPointData.size) {
            //  GPS起動時の標高データ
            ele = mGpsTrace.mGpsLastElevator    //  標高(m)
            type = "GPS"
        }
        coordeMsg += " 標高 " + "%,4.1f m".format(ele) + "(" + type + ") 色 " + mColor

        kdraw.mCanvas = canvas
        kdraw.setColor("Blue")
        kdraw.setTextSize(32.0)
        kdraw.drawTextWithBox(coordeMsg, PointD(x.toDouble(), y.toDouble()))

        //  GPSトレース中の距離と歩数の表示
        if (type.compareTo("GPS") == 0 && 0 < mGpsTrace.mGpsPointData.size) {
            var moveMsg = " 移動距離 " + "%,.2f km".format(mGpsTrace.totalDistance())
            moveMsg += " 歩数 " + mGpsTrace.stepCount().toString()
            y += 40f
            kdraw.drawTextWithBox(moveMsg, PointD(x.toDouble(), y.toDouble()))
        }
        //  色凡例を表示
        if (0 < mComment.length) {
            y += 40f
            kdraw.drawTextWithBox(mComment, PointD(x.toDouble(), y.toDouble()))
        }
    }

    //  中心線表示
    fun drawCross(canvas: Canvas) {
        var paint = Paint()
        paint.color = Color.BLUE
        paint.strokeWidth = 4f
        var ctr = Point(mWidth / 2, mHeight / 2)
        canvas.drawLine((ctr.x - 50).toFloat(), ctr.y.toFloat(), (ctr.x + 50).toFloat(), ctr.y.toFloat(), paint)
        canvas.drawLine(ctr.x.toFloat(), (ctr.y - 50).toFloat(), ctr.x.toFloat(), (ctr.y + 50).toFloat(), paint)
    }

    //  セルにデータを設定
    fun setCellBoard() {
        if (mWidth <= 0 || mHeight <= 0 || mColCount == 0 || mRowCount == 0)
            return
        setCelSize()
        mGCells.clear()
        var sx = 0f
        var width = 0f
        var height = 0f
        for (x: Int in 0..mColCount) {
            var sy = 0f
            if (mWidth <= sx)
                break;
            for (y: Int in 0..mRowCount) {
                if (mHeight <= sy)
                    break;
                //  セルの設定
                val gcell = GCell()
                gcell.mId = getId(x, y)
                gcell.mGCellType = GCELLTYPE.RECT
                gcell.mBackColor = Color.RED
                gcell.mPosition = PointF(sx, sy)
                width = mCellSize.toFloat()
                height = mCellSize.toFloat()
                //  画像のトリミング
                if (mWidth < sx + width) {
                    width = mWidth - sx
                    gcell.mTrimmingRect.right = width / mCellSize
                } else if (x == 0 && mOffset.x != 0.0) {
                    gcell.mTrimmingRect.left = mOffset.x.toFloat()
                    width = mCellSize * (1.0 - mOffset.x).toFloat()
                } else {
                    gcell.mTrimmingRect.right = 1f
                }
                if (mHeight < sy + height) {
                    height = mHeight - sy
                    gcell.mTrimmingRect.bottom = height / mCellSize
                } else if (y == 0 && mOffset.y != 0.0) {
                    gcell.mTrimmingRect.top = mOffset.y.toFloat()
                    height = mCellSize * (1.0 - mOffset.y).toFloat()
                } else {
                    gcell.mTrimmingRect.bottom = 1f
                }
                //  画僧サイズの登録
                gcell.mSize = SizeF(width, height)
                mGCells.add(gcell)
                sy += height
            }
            sx += width
        }
    }

    //  列数からセルサイズと行数を求める
    fun setCelSize() {
        mCellSize = mWidth / mColCount
        mRowCount = mHeight / mCellSize + 1
    }

    //  Cellにイメージファイルのパスを設定
    fun setCellImage(col: Int, row: Int, imageFile: String) {
        var id = getId(col, row)
        for (cell in mGCells) {
            if (cell.mId == id) {
                cell.mImageFile = imageFile
                break
            }
        }
    }

    /**
     * セルのBitmapの取得
     *  col     列
     *  row     行
     *  return  Bitmap
     */
    fun getCellImage(col: Int, row: Int): Bitmap? {
        var id = getId(col, row)
        for (cell in mGCells) {
            if (cell.mId == id) {
                Log.d(TAG,"getCellImage: PixelId "+id)
                return cell.getBitmapImage()
            }
        }
        return null
    }

    //  行列からIDを求める
    fun getId(col: Int, row: Int): Int {
        return col + row * (mColCount + 1)
    }

    //  IDから列番を求める
    fun getIdCol(id: Int): Int {
        return id % (mColCount + 1)
    }

    //  IDから行番を求める
    fun getIdRow(id: Int): Int {
        return id / (mColCount + 1)
    }

    //  画面(スクリーン)の中心座標
    fun getCenter(): PointD {
        return PointD(mWidth.toDouble() / 2.0, mHeight.toDouble() / 2.0)
    }
}