package jp.co.yoshida.katsushige.gameapp

import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.SizeF
import android.view.View
import kotlin.random.Random

val BLOCKXCOUNT = 10
val BLOCKYCOUNT = 20
val FIELDMARGIN = 0.1f

class TetrisView(context: Context): View(context) {
    private val TAG:String = "TetrisView"

    var mViewSize: SizeF = SizeF(0F, 0F)        //  グラフィック領域の大きさ
    val mFieldAreaRate = 1f - FIELDMARGIN * 2f               //  フィールド領域の割合
    var mBlockSize: Float = 0f                               //  ブロックの個々の大きさ
    var mStartPos: PointF = PointF(0f, 0f)             //  フィールドの左上座標
    var mBlockSleepCount = 0                                 //  ブロックが落下する待ち時間のカウント
    var mBlockDownCcount = 30

    var mBlock: Block = Block(0f, PointF(0f, 0f))   //  ブロッククラス
    lateinit var mField: Field                               //  フィールドクラス
    var mEraseCount: Int = 0
    var mEndMessage = ""

    override fun onDraw(canvas: Canvas?) {
        //  super.onDraw(canvas)
        //  初回のパラメータ設定
        if (mBlockSize == 0f) {
            setParameter()
            mField = Field(mBlockSize, mStartPos)
        }

        //  データの更新
        if (mBlock.IsEnabled && mBlockDownCcount < mBlockSleepCount) {
            mBlockSleepCount = 0
            //  落下可否の確認
            if (mField.chkBlockPos(mBlock)) {
                //  落下できない場合フィールドに四角を登録
                mField.setBlockSquare(mBlock)
                if (mBlock.position.y < 0) {
                    //  終了
                    mBlock.IsEnabled = false
                    mEndMessage = "ご苦労さん"
                } else {
                    //  新規ブロック
                   mBlock = Block(mBlockSize, mStartPos)
                }
            } else {
                mBlock.position.y++
            }
            if (mField.eraseBlock())
                mEraseCount++
        }

        mBlockSleepCount++
        //  終了でなければブロックを表示
        if (canvas != null){
            mBlock.draw(canvas)
            mField?.draw(canvas)
            downMessage(canvas, mEraseCount.toString() + " 点")
        }
        if (mBlock.IsEnabled)
            reDraw()
        else
            canvas?.let {centerMessage(canvas, mEndMessage)}
    }

    /**
     * パラメータ(Viewサイズ、ブロックの大きさ、開始位置)の設定
     */
    fun setParameter() {
        //  Viewサイズ onDraw後でないと取れない
        mViewSize = SizeF(this.width.toFloat(), this.height.toFloat())
        //  ブロックサイズ
        mBlockSize = mViewSize.width * mFieldAreaRate / BLOCKXCOUNT
        if (mBlockSize > mViewSize.height * mFieldAreaRate / BLOCKYCOUNT)
            mBlockSize = mViewSize.height * mFieldAreaRate / BLOCKYCOUNT
        //  表示開始座標
        mStartPos.x = (mViewSize.width - mBlockSize * BLOCKXCOUNT) / 2
        mStartPos.y = mViewSize.height * FIELDMARGIN
        Log.d(TAG, "setParameter: ${mViewSize.width},${mViewSize.height}, ${mBlockSize}")
    }

    /**
     * 画面の下にメッセージを表示する
     */
    fun downMessage(canvas: Canvas, msg: String) {
        if (0 < msg.length) {
            var paint = Paint()
            paint.textSize = 80F
            paint.color = Color.BLACK
            canvas.drawText(msg,
                    mStartPos.x + (mBlockSize * BLOCKXCOUNT - paint.measureText(msg)) / 2,
                    mViewSize.height - 60, paint)
        }
    }

    /**
     * 画面の中央にメッセージを表示
     */
    fun centerMessage(canvas: Canvas, msg: String) {
        if (0 < msg.length) {
            var paint = Paint()
            paint.textSize = 140F
            paint.color = Color.RED
            canvas.drawText(msg,
                    (mViewSize.width - paint.measureText(msg)) / 2,
                    mViewSize.height / 2, paint)
        }
    }

    /**
     * 再描画
     */
    fun reDraw(){
        invalidate()
    }

    /**
     * ブロックの落下を開始
     * ブロックが婿になっていれば新規画面にして開始
     */
    fun start() {
        if (!mBlock.IsEnabled) {
            mBlock = Block(mBlockSize, mStartPos)
            mField = Field(mBlockSize, mStartPos)
        }
        Log.d(TAG,"start: ${mBlock.IsEnabled}")
        reDraw()
    }

    /**
     * ブロックの落下を停止する
     */
    fun stop() {
        mBlock.IsEnabled = false
    }

    /**
     * ブロックが落下中か求める
     */
    fun isRunning(): Boolean {
        return mBlock.IsEnabled
    }

    /**
     * ブロックを左に移動
     */
    fun leftMove() {
        if (mBlock.IsEnabled){
            if (!mField.chkYokoBlockPos(mBlock, -1)) {
                mBlock.position.x--
            }
        }
    }

    /**
     * ブロックを右方向に移動
     */
    fun rightMove() {
        if (mBlock.IsEnabled){
            if (!mField.chkYokoBlockPos(mBlock, 1)) {
                mBlock.position.x++
            }
        }
    }

    /**
     * ブロックを時計回りに90度回転
     * 回転した結果はみだしがあれば元に戻す
     */
    fun rotate() {
        if (mBlock.IsEnabled) {
            mBlock.rotate()
            if (mField.chkYokoBlockPos(mBlock, 0))
                mBlock.rotate(-1)
        }
    }

    /**
     * ブロックを縦軸で反転
     */
    fun mirror() {
        if (mBlock.IsEnabled)
            mBlock.mirror()
    }
}

/**
 * フィールドの設定と表示
 */
class Field(val blockSize: Float, val startPos: PointF) {
    var paint: Paint = Paint()
    val blockOnField = mutableListOf<Square>()
    var fieldCount = IntArray(BLOCKYCOUNT) { 0 }

    init {
        paint.style = Paint.Style.STROKE
        paint.color = Color.argb(255, 130, 200, 255)
        paint.strokeWidth = 5f
    }

    /**
     * 一行揃っていればその行の四角を削除
     */
    fun eraseBlock(): Boolean {
        fieldCount = IntArray(BLOCKYCOUNT) { 0 }
        var eraseLine: Int = -1
        //  一行の四角の数を求める
        for (b in blockOnField) {
            fieldCount[b.pos.y]++
            if (BLOCKXCOUNT <= fieldCount[b.pos.y]) {
                eraseLine = b.pos.y
                break
            }
        }
        //  一行を四角で埋めている行の四角の削除とそれより上の四角を一つ落下させる
        for (i in blockOnField.size - 1 downTo 0) {
            if (blockOnField[i].pos.y == eraseLine)
                blockOnField.removeAt(i)
            else if (blockOnField[i].pos.y < eraseLine)
                blockOnField[i].pos.y++
        }
        if (eraseLine < 0)
            return false
        else
            return true
    }

    /**
     * ブロックの落下位置を確認(これ以上落下できなければ trueを返す)
     * block : Blockクラス
     * return : true = 下限値に到達
     */
    fun chkBlockPos(block: Block): Boolean {
        for (i in 0 until block.curBlock.size) {
            if (BLOCKYCOUNT <= block.getBlockPos(i).y + 1)
                return true
            for (square in blockOnField) {
                if (square.pos.x == block.getBlockPos(i).x && square.pos.y == block.getBlockPos(i).y + 1)
                    return true
            }
        }
        return false
    }

    /**
     * ブロックが横方向にはみだしていたり、四角と重なっていないかを確認
     * block : Blockクラス
     * offset : 左右方向への追加移動
     * return : true = はみだし、重なりあり
     */
    fun chkYokoBlockPos(block: Block, offset: Int): Boolean {
        for (i in 0 until block.curBlock.size) {
            if ((0 > block.getBlockPos(i).x + offset) ||
                 (block.getBlockPos(i).x + offset >= BLOCKXCOUNT))
                 return true
            for (square in blockOnField) {
                if (square.pos.x == (block.getBlockPos(i).x + offset) &&
                    square.pos.y == block.getBlockPos(i).y)
                    return true
            }
        }
        return false
    }

    /**
     * ブロックの形状をフィールドに追加
     */
    fun setBlockSquare(block: Block) {
        for (i in 0 until block.curBlock.size) {
            if (0 <= block.getBlockPos(i).y) {
                val square = Square(block.getBlockPos(i), blockSize, block.paint, startPos)
                blockOnField.add(square)
            }
        }
    }

    /**
     * フィールドの格子を表示
     */
    fun draw(canvas: Canvas) {
        //  落下したブロックの表示
        for (square in blockOnField)
            square.draw(canvas)
        //  フィールドの格子の表示
        for (i in 0..BLOCKYCOUNT) {
            canvas?.drawLine(startPos.x, startPos.y + i * blockSize,
                    startPos.x + BLOCKXCOUNT * blockSize, startPos.y + i * blockSize, paint)
        }
        for (j in 0..BLOCKXCOUNT) {
            canvas?.drawLine(startPos.x + j * blockSize, startPos.y,
                    startPos.x + j * blockSize, startPos.y + BLOCKYCOUNT * blockSize, paint)
        }
    }
}

//  フィールドに表示する四角
class Square(val pos: Point, val blockSize: Float, val paint: Paint, val startPos: PointF) {

    /**
     * 塗潰し四角を表示
     */
    fun draw(canvas: Canvas) {
        val sx = startPos.x + pos.x * blockSize
        val sy = startPos.y + pos.y * blockSize
        canvas.drawRect(Rect(sx.toInt(), sy.toInt(),
                (sx + blockSize).toInt(), (sy + blockSize).toInt()), paint)
    }
}

/**
 * ブロックの表示クラス
 */
class Block(val blockSize: Float, val startPos: PointF) {
    //  ブロックの形状データ
    private val blockA = listOf(Point(0, 0), Point(1, 0), Point(2, 0), Point(0, -1))
    private val blockB = listOf(Point(0, 0), Point(1, 0), Point(2, 0), Point(3, 0))
    private val blockC = listOf(Point(0, 0), Point(1, 0), Point(2, 0), Point(1, -1))
    private val blockD = listOf(Point(0, 0), Point(1, 0), Point(1, -1), Point(0, -1))
    var curBlock = when (Random.nextInt(4)){    //  設定されているブロックの形状
        0 -> blockA
        1 -> blockB
        2 -> blockC
        3 -> blockD
        else -> blockA
    }
    val paint = Paint()                 //  ブロックの属性
    var width = getBlockWidth()         //  ブロック全体の幅
    var height = getBlockHeight()       //  ブロック全体の高さ
    var position = Point((BLOCKXCOUNT - width) / 2, -1)
    var IsEnabled = (0f < blockSize)    //  ブロック有効/無効

    /**
     * 初期化
     */
    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(255,
                Random.nextInt(200),
                Random.nextInt(200),
                Random.nextInt(200))
    }

    /**
     * ブロックの表示
     */
    fun draw(canvas: Canvas){
        if (IsEnabled) {
            for (p in curBlock) {
                val sx = startPos.x + (p.x + position.x) * blockSize
                val sy = startPos.y + (p.y + position.y) * blockSize
                canvas.drawRect(Rect(sx.toInt(), sy.toInt(),
                        (sx + blockSize).toInt(), (sy + blockSize).toInt()), paint)
            }
        }
    }

    /**
     *  ブロックの個々の位置の取得(移動込み)
     */
    fun getBlockPos(n: Int): Point {
        return Point(curBlock[n].x + position.x, curBlock[n].y + position.y)
    }

    /**
     *  ブロックの個々の位置リストの取得
     */
    fun getBlockPos(): List<Point> {
        val pos = mutableListOf<Point>()
        for (p in curBlock) {
            pos.add(Point(p.x + position.x, p.y + position.y))
        }
        return pos
    }

    /**
     * 時計回りに90度回転
     * rotate 1:時計回り90度回転 -1:反時計回り90度回転
     */
    fun rotate(rotate: Int = 1) {
        var tmpBlock = mutableListOf<Point>()
        var minx:Int = 0
        var maxy:Int = 0
        for (p in curBlock) {
            tmpBlock.add(Point(-p.y * rotate, p.x * rotate))
            if (tmpBlock.size == 1) {
                minx = -p.y * rotate
                maxy = p.x * rotate
            } else {
                minx = Math.min(minx, -p.y * rotate)
                maxy = Math.max(maxy, p.x * rotate)
            }
        }
        for (p in tmpBlock) {
            p.x -= minx
            p.y -= maxy
        }
        curBlock = tmpBlock
        width = getBlockWidth()
        height = getBlockHeight()
    }

    /**
     * Y軸で反転
     */
    fun mirror() {
        var tmpBlock = mutableListOf<Point>()
        var minx:Int = 0
        var maxy:Int = 0
        for (p in curBlock) {
            tmpBlock.add(Point(-p.x, p.y))
            if (tmpBlock.size == 1) {
                minx = -p.x
                maxy = p.y
            } else {
                minx = Math.min(minx, -p.x)
                maxy = Math.max(maxy, p.y)
            }
        }
        for (p in tmpBlock) {
            p.x -= minx
            p.y -= maxy
        }
        curBlock = tmpBlock
        width = getBlockWidth()
        height = getBlockHeight()
    }

    /**
     * ブロック全体の幅
     */
    fun getBlockWidth(): Int {
        var width = 0
        for (p in curBlock) {
            width = Math.max(width, Math.abs(p.x))
        }
        return width
    }

    /**
     * ブロック全体の高さ
     */
    fun getBlockHeight(): Int {
        var height = 0
        for (p in curBlock) {
            height = Math.max(height, Math.abs(p.y))
        }
        return height
    }
}

