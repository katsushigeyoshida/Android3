package jp.co.yoshida.katsushige.gameapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog.Builder
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.gameapp.databinding.ActivityMineSweeperBinding
import jp.co.yoshida.katsushige.mylib.YLib
//import kotlinx.android.synthetic.main.activity_mine_sweeper.*
import kotlin.random.Random

class MineSweeper : AppCompatActivity() {
    private val TAG = "MineSweeper "

    private var row = 9                     //	マス目の行数
    private var col = 9                     //	マス目の列数
    private var bombNum = 5                 //	爆弾の個数
    private var totalCellNum = row * col    //  マス目の数
    //    private var bombPos: IntArray ?= null         //	マス目の配列
    private var bombPos = IntArray(totalCellNum) { 0 }
    private var longClick = false           //  longClickとclickとの重複処理防止

    private val MENU00 = 0
    private val MENU01 = 1
    private val MENU02 = 2
    private val MENU03 = 3
    val colCount = arrayOf("4", "5", "6", "7", "8", "9", "10")
    val rowCount = arrayOf("4", "5", "6", "7", "8", "9", "10", "11", "12")
    val bombCount = arrayOf("4", "5", "6", "7", "8", "9", "10")

    lateinit var chronometer: Chronometer    //  タイマー(GUIコンポーネント)
    private var ylib = YLib(this)

    lateinit var binding: ActivityMineSweeperBinding
    lateinit var mineLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_sweeper)
        this.title = "マインスイーパ"

        binding = ActivityMineSweeperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mineLinearLayout = binding.mineLinearLayout
        init()
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
                init()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //  爆弾数の設定
    var iBombNumOperation = Consumer<String> { s ->
        bombNum = s.toInt()
        ylib.setIntPreferences(bombNum, "bombNum", this)
        init()
    }

    //  列数の設定
    var iColOperation = Consumer<String> { s ->
        col = s.toInt()
        ylib.setIntPreferences(col, "colCount", this)
        init()
    }

    //  行数の設定
    var iRowOperation = Consumer<String> { s ->
        row = s.toInt()
        ylib.setIntPreferences(row, "rowCount", this)
        init()
    }

    // 初期化処理
    private fun init() {
        row = ylib.getIntPreferences("rowCount", this)
        row = if (row < 1) 5 else row
        col = ylib.getIntPreferences("colCount", this)
        col = if (col < 1) 5 else col
        bombNum = ylib.getIntPreferences("bombNum", this)
        bombNum = if (bombNum < 1) 5 else bombNum
        totalCellNum = row * col
        Log.d(TAG + "-init-", "H:$row W:$col BombNum:$bombNum")

        this.title = "マインスイーパ (爆弾の数: " + bombNum + ")"
        // クロノメーター取得(時刻設定)
        chronometer = Chronometer(this)

        setBomb(bombNum)                    //  爆弾の位置をランダムに設定
        setLinerLayout(mineLinearLayout)    //  LinearLayoutでマス目を設定する時
//        val tableLayout: TableLayout = getTableLayout()   //  TableLayoutでマス目を設定する時
//        mineLinearLayout.addView(tableLayout)
//        setContentView(tableLayout)
    }

    // 爆弾の位置をランダムにフィールドに設定する
    private fun setBomb(bombNum: Int) {
        bombPos = IntArray(totalCellNum) { 0 }
        var i = 0
        while (i < bombNum) {
            val pos: Int = Random.nextInt(totalCellNum)
            if (bombPos[pos] != 1) {
                bombPos[pos] = 1
            } else {
                i--
            }
            i++
        }
    }

    //  LinearLayoutを使ってマス目を作る
    private fun setLinerLayout(mainLinearLayout: LinearLayout) {
        Log.d(TAG, "setLinerLayout ")
        var linerLayout: LinearLayout? = null
        mainLinearLayout.removeAllViews()
        for (i in 0 until totalCellNum) {
            if (i % col == 0) {
                //  行追加
                linerLayout = LinearLayout(this)
                linerLayout.orientation = LinearLayout.HORIZONTAL
                mainLinearLayout.addView(linerLayout)
            }
            val button = Button(this)
            button.setId(i)
//            button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            button.setPadding(0,0,0,0)
            button.height = getCellHeight(row + 2)
            button.width  = getCellWidth(col + 2)
            button.background = resources.getDrawable(R.drawable.offtile, null)
            //  buttonにイベントを設定
            setOnClick(button)
            setOnLongClick(button)
            linerLayout?.addView(button)
        }
    }

    // マス目を示すテーブルレイアウトを返す
    private fun getTableLayout(): TableLayout {
        val tableLayout = TableLayout(this)
        var tableRow: TableRow? = null
        // Console.out.println("Display Width" + getCellWidth(1));
        Log.d(TAG + "getTableLayout ", "Height:" + getCellHeight(1))
        Log.d(TAG + "getTableLayout ", "Width :" + getCellWidth(1))
        for (i in 0 until totalCellNum) {
            if (i % col == 0) {
                tableRow = TableRow(this)
                tableLayout.addView(tableRow)
            }
            val button = Button(this)
            button.setId(i)
            button.height = getCellHeight(row+2)
//            button.width  = getCellWidth(col+3)
            button.background = resources.getDrawable(R.drawable.offtile, null)
            //  buttonにイベントを設定
            setOnClick(button)
            setOnLongClick(button)
            tableRow?.addView(button)
        }
        return tableLayout
    }

    //  クリックイベント処理設定
    private fun setOnClick(button: Button) {
        button.setOnClickListener() {
            val pos = button.id
            if (!longClick) {       //  ロングクリックと重複処理を回避
                if (bombPos[pos] == 1) {
                    //  爆弾位置をあける
                    button.background = resources.getDrawable(R.drawable.bombtile, null)
                    gameOverDialog("残念")
                } else {
                    //  クリック位置周囲の爆弾を検索
                    var nearBombNum = 0
                    for (i in -1 until 2) {
                        for (j in -1 until 2) {
                            val r = pos / col + i
                            val c = pos % col + j
                            if (0 <= c && c < col && 0 <= r && r < row) {
                                val p = r * col + c
                                if (p != pos && bombPos[p] == 1)
                                    nearBombNum++
                            }
                        }
                    }
                    //  爆弾の数を表示
                    when (nearBombNum) {
                        0 -> button.background = resources.getDrawable(R.drawable.ontitle, null)
                        1 -> button.background = resources.getDrawable(R.drawable.on1title, null)
                        2 -> button.background = resources.getDrawable(R.drawable.on2title, null)
                        3 -> button.background = resources.getDrawable(R.drawable.on3title, null)
                        4 -> button.background = resources.getDrawable(R.drawable.on4title, null)
                        5 -> button.background = resources.getDrawable(R.drawable.on5title, null)
                        6 -> button.background = resources.getDrawable(R.drawable.on6title, null)
                        7 -> button.background = resources.getDrawable(R.drawable.on7title, null)
                        8 -> button.background = resources.getDrawable(R.drawable.on8title, null)
                    }
                    bombPos[pos] = 2
                    if (!bombPos.contains(0)) {
                        gameOverDialog("無事終了")
                    }
                }
            }
            longClick = false
        }
    }

    //  ロングクリックイベント処理設
    private fun setOnLongClick(button: Button) {
        button.setOnLongClickListener() {
            Log.d(TAG + "setOnLongClick ", "id: " + button.id)
            //  ロングクリックした位置に旗を立てる
            longClick = true
            if (button.isClickable) {
                button.background = resources.getDrawable(R.drawable.flagtile, null)
            }
            return@setOnLongClickListener false
        }
    }

    // 行と画面の幅からマス目の高さを算出する
    private fun getCellHeight(row: Int): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.height / row
    }

    // 行と画面の幅からマス目の幅を算出する
    private fun getCellWidth(col: Int): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.width / col
    }

    // ゲームオーバー時のダイヤログ表示
    private fun gameOverDialog(title: String) {
        val seconds = (SystemClock.elapsedRealtime() - chronometer.base.toInt()) / 1000
        val secStr = (String.format("%2d", seconds / 60) + ":"
                + String.format("%2d", seconds % 60))
        //  アラート・ダイヤログ
        val builder = Builder(this)
        builder.setTitle(title)
        builder.setMessage("経過時間：$secStr")
        builder.show()
        //openCount = 0;
    }
}