package jp.co.yoshida.katsushige.gameapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.SizeF
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.gameapp.databinding.ActivityBombBinding
import jp.co.yoshida.katsushige.mylib.GCELLTYPE
import jp.co.yoshida.katsushige.mylib.GCell
import jp.co.yoshida.katsushige.mylib.YLib
//import kotlinx.android.synthetic.main.activity_bomb.*
import kotlin.random.Random


class Bomb : AppCompatActivity() {
    val TAG = "Bomb"
    var mBombViewWidth = 0
    var mBombViewHeight = 0

    private val MENU00 = 0
    private val MENU01 = 1
    private val MENU02 = 2
    private val MENU03 = 3
    val colCount = arrayOf("4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15")
    val rowCount = arrayOf("4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15")
    val bombCount = arrayOf("4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15")

    var bombGraphicView: BombGraphicView? = null
    var ylib = YLib(this)

    lateinit var binding: ActivityBombBinding
    lateinit var bombLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bomb)
        title = "マインスィーパ"

        binding = ActivityBombBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bombLinearLayout = binding.bombLinearLayout

        Log.d(TAG, "onCreate: " + "Width: " + getCellWidth(1, 16 + 16) + " Height: " + getCellHeight(1, 16 + 192))
        bombGraphicView = BombGraphicView(this, getCellWidth(1, 16 + 16), getCellHeight(1, 16 + 192))
        bombLinearLayout.addView(bombGraphicView)
        title = "マインスイーパ (爆弾の数: " + bombGraphicView?.mBombCount + ")"
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "onWindowFocusChanged: " + "Width: " + bombLinearLayout.width + " Height: " + bombLinearLayout.height)
        mBombViewWidth = bombLinearLayout.width
        mBombViewHeight = bombLinearLayout.height
    }

    //  オプションメニューの設定
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val item0 = menu!!.add(Menu.NONE, MENU00, Menu.NONE, "爆弾の数")
        item0.setIcon(android.R.drawable.ic_menu_set_as)
        val item1 = menu!!.add(Menu.NONE, MENU01, Menu.NONE, "列数")
        item1.setIcon(android.R.drawable.ic_menu_set_as)
        val item2 = menu!!.add(Menu.NONE, MENU02, Menu.NONE, "行数")
        item2.setIcon(android.R.drawable.ic_menu_set_as)
        val item3 = menu!!.add(Menu.NONE, MENU03, Menu.NONE, "リセット")
        item3.setIcon(android.R.drawable.ic_menu_set_as)
        return super.onCreateOptionsMenu(menu)
    }

    //  オプションメニューの処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU00 -> {
                Toast.makeText(this, "爆弾の数の設定が選択", Toast.LENGTH_SHORT).show()
                ylib.setMenuDialog(this, "爆弾の数の設定", bombCount, iBombNumOperation)
            }
            MENU01 -> {
                Toast.makeText(this, "列数の設定が選択", Toast.LENGTH_SHORT).show()
                ylib.setMenuDialog(this, "列数の設定", colCount, iColOperation)
            }
            MENU02 -> {
                Toast.makeText(this, "行数の設定が選択", Toast.LENGTH_SHORT).show()
                ylib.setMenuDialog(this, "行数の設定", rowCount, iRowOperation)
            }
            MENU03 -> {
                bombGraphicView?.initBoard(mBombViewWidth, mBombViewHeight)
                bombGraphicView?.reDraw()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //  爆弾数の設定
    var iBombNumOperation = Consumer<String> { s ->
        var bombNum = s.toInt()
        bombGraphicView?.mBombCount = bombNum
        ylib.setIntPreferences(bombNum, "bombNum", this)
        bombGraphicView?.initBoard(mBombViewWidth, mBombViewHeight)
        bombGraphicView?.reDraw()
        title = "マインスイーパ (爆弾の数: " + bombGraphicView?.mBombCount + ")"
    }

    //  列数の設定
    var iColOperation = Consumer<String> { s ->
        var col = s.toInt()
        bombGraphicView?.mColCount = col
        ylib.setIntPreferences(col, "colCount", this)
        bombGraphicView?.initBoard(mBombViewWidth, mBombViewHeight)
        bombGraphicView?.reDraw()
    }

    //  行数の設定
    var iRowOperation = Consumer<String> { s ->
        var row = s.toInt()
        bombGraphicView?.mRowCount = row
        ylib.setIntPreferences(row, "rowCount", this)
        bombGraphicView?.initBoard(mBombViewWidth, mBombViewHeight)
        bombGraphicView?.reDraw()
    }

    // 行と画面の幅からマス目の高さを算出する
    private fun getCellHeight(row: Int, margin: Int): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return (display.height - margin) / row
    }

    // 行と画面の幅からマス目の幅を算出する
    private fun getCellWidth(col: Int, margin: Int): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return (display.width - margin) / col
    }
}

class BombGraphicView(context: Context, mWidth: Int, mHeight: Int): View(context) {
    val TAG = "BombGraphicView"
    //  長押しのTouchEvemtの取得のための時間計測用
    var startTime: Long = 0
    var endTime: Long = 0
    lateinit var chronometer: Chronometer    //  タイマー(GUIコンポーネント)
    var ylib = YLib(context)

    //  盤の大きさ
    val mGCells = mutableListOf<GCell>()
    var mColCount = 9
    var mRowCount = 9
    var mBombCount = 5
    var bombPos = IntArray(mColCount * mRowCount) { 0 }

    init {
        //  プリファレンスから取得
        var row = ylib.getIntPreferences("rowCount", context)
        var col = ylib.getIntPreferences("colCount", context)
        var bombNum = ylib.getIntPreferences("bombNum", context)
        mRowCount = if (row < 1) 5 else row
        mColCount = if (col < 1) 5 else col
        mBombCount = if (bombNum < 1) 5 else bombNum
        //  盤の初期化
        initBoard(mWidth, mHeight)
        // クロノメーター取得(時刻設定)
        chronometer = Chronometer(context)
   }

    //  描画処理
    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
        //  セルの表示
        for (cell in mGCells)
            cell.draw(canvas)
    }

    //  描画の更新
    fun reDraw(){
        invalidate()
    }

    //  タッチ操作処理
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouchEvent: " + event?.x + "," + event?.y + " action: " + event?.action.toString())
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = event.eventTime
            }
            MotionEvent.ACTION_UP -> {
                endTime = event.eventTime
                if ((endTime - startTime) > 500) {
                    //  長押し処理(旗を立てる)
                    setCellFlagTile(event.x, event.y)
                    startTime = 0
                    endTime = 0
                } else {
                    //  通常の処理(セルを開く)
                    setCellTile(event.x, event.y)
                }
            }
        }

        return true;       //  falseだとACTION_UPが取れない
//        return super.onTouchEvent(event)
    }

    //  セルに旗イメージを設定する
    fun setCellFlagTile(x: Float, y: Float) {
        val id: Int = getTouchCellId(PointF(x, y))
        setCellImage(id, R.drawable.flagtile)
        reDraw()
    }

    //  セルを開いてイメージを設定する
    fun setCellTile(x: Float, y: Float) {
        val id: Int = getTouchCellId(PointF(x, y))
        if (0 <= id) {
            if (bombPos[id] == 1) {
                setCellImage(id, R.drawable.bombtile)
                gameOverDialog("残念")
            } else {
                var nearBombCount = getNearBombCount(id)
                when (nearBombCount) {
                    0 -> setCellImage(id, R.drawable.ontitle)
                    1 -> setCellImage(id, R.drawable.on1title)
                    2 -> setCellImage(id, R.drawable.on2title)
                    3 -> setCellImage(id, R.drawable.on3title)
                    4 -> setCellImage(id, R.drawable.on4title)
                    5 -> setCellImage(id, R.drawable.on5title)
                    6 -> setCellImage(id, R.drawable.on6title)
                    7 -> setCellImage(id, R.drawable.on7title)
                    8 -> setCellImage(id, R.drawable.on8title)
                }
            }
            reDraw()
            bombPos[id] = 2
            if (!bombPos.contains(0)) {
                gameOverDialog("無事終了")
            }
        }

    }

    //  指定セルの周りの爆弾数を求める
    fun getNearBombCount(id: Int): Int {
        //  クリック位置周囲の爆弾を検索
        var nearBombNum = 0
        for (i in -1 until 2) {
            for (j in -1 until 2) {
                val r = getIdRow(id) + i
                val c = getIdCol(id) + j
                if (0 <= c && c < mColCount && 0 <= r && r < mRowCount) {
                    val p = getId(c, r)
                    if (p != id && bombPos[p] == 1)
                        nearBombNum++
                }
            }
        }
        return nearBombNum
    }

    // 爆弾の位置をランダムにフィールドに設定する
    fun setBomb(bombNum: Int) {
        bombPos = IntArray(mColCount * mRowCount) { 0 }
        var i = 0
        while (i < bombNum) {
            val pos: Int = Random.nextInt(mColCount * mRowCount)
            if (bombPos[pos] != 1) {
                bombPos[pos] = 1
            } else {
                i--
            }
            i++
        }
    }

    //  盤の初期化(セルの設定)
    fun initBoard(width: Int, height: Int) {
        var size = Math.min(width.toFloat() / mColCount, height.toFloat() / mRowCount)
        var sx = (width - size * mColCount) / 2f
        var sy = 0f

        mGCells.clear()
        for (x: Int in 0..mColCount-1) {
            for (y: Int in 0..mRowCount-1) {
                val gcell = GCell()
                gcell.mId = getId(x, y)
                gcell.mGCellType = GCELLTYPE.RECT
                gcell.mBackColor = Color.RED
                gcell.mPosition = PointF(sx + x * size, sy + y * size)
                gcell.mSize = SizeF(size, size)
                gcell.mResources = resources
                gcell.mImageResource = R.drawable.offtile
//                gcell.mTitle = x.toString() + y.toString()
//                gcell.mTitleSize = 50f
                mGCells.add(gcell)
            }
        }
        setBomb(mBombCount)
    }

    //  リソース画像データの登録
    fun setCellImage(id: Int, resource: Int) {
        for (cell in mGCells) {
            if (cell.mId == id) {
                cell.mImageResource = resource
                break
            }
        }
    }

    //  タッチイベントでタッチしたセルのIDを取得
    fun getTouchCellId(pos: PointF): Int {
        for (cell in mGCells)
            if (cell.getCellIn(pos))
                return cell.mId
        return -1
    }

    //  行列からIDを求める
    fun getId(col: Int, row: Int): Int {
        return col + row * mColCount
    }

    //  IDから列番を求める
    fun getIdCol(id: Int): Int {
        return id % mColCount
    }

    //  IDから行番を求める
    fun getIdRow(id: Int): Int {
        return id / mColCount
    }

    // ゲームオーバー時のダイヤログ表示
    fun gameOverDialog(title: String) {
        val seconds = (SystemClock.elapsedRealtime() - chronometer.base.toInt()) / 1000
        val secStr = (String.format("%2d", seconds / 60) + ":"
                + String.format("%2d", seconds % 60))
        //  アラート・ダイヤログ
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage("経過時間：$secStr")
        builder.show()
        //openCount = 0;
    }
}