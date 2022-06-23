package jp.co.yoshida.katsushige.gameapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.gameapp.databinding.ActivityBombBinding
import jp.co.yoshida.katsushige.gameapp.databinding.ActivityTetrisBinding
import jp.co.yoshida.katsushige.mylib.YLib
//import kotlinx.android.synthetic.main.activity_tetris.*


/**
 * KotlinのAndroid拡張を利用するには
 * 1.build.gradle(GameApp)に「apply plugin: 'kotlin-android-extensions'」を追加
 * 2.本ファイルに「import kotlinx.android.synthetic.main.activity_tetris.*」を追加
 * 上記を追加した後に必ず「Make Project」でbuildすること
 */
class Tetris : AppCompatActivity() {

    private val downCount = arrayOf( "5", "10", "20", "30", "40", "50")
    private val MENU00 = 0
    private var ylib = YLib(this)
    private lateinit var tetrisView: TetrisView

    lateinit var binding: ActivityTetrisBinding
    lateinit var tetrisLinearLayout: LinearLayout
    lateinit var BtStart: Button
    lateinit var BtLeftMove: Button
    lateinit var BtRightMove: Button
    lateinit var BtRotate: Button
    lateinit var BtMirror:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tetris)
        this.title = "テトリス"

        binding = ActivityTetrisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tetrisLinearLayout = binding.tetrisLinearLayout
        BtStart = binding.BtStart
        BtLeftMove = binding.BtLeftMove
        BtRightMove = binding.BtRightMove
        BtRotate = binding.BtRotate
        BtMirror = binding.BtMirror

        tetrisView = TetrisView(this)
        tetrisLinearLayout.addView(tetrisView)

        BtStart.setOnClickListener(){
            if (!tetrisView.isRunning()) {
                tetrisView.start()
            } else {
                tetrisView.stop()
            }
        }

        BtLeftMove.setOnClickListener() {
            tetrisView.leftMove()
        }

        BtRightMove.setOnClickListener() {
            tetrisView.rightMove()
        }

        BtRotate.setOnClickListener() {
            tetrisView.rotate()
        }

        BtMirror.setOnClickListener() {
            tetrisView.mirror()
        }
    }

    //  オプションメニューの作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val item1 = menu!!.add(Menu.NONE, MENU00, Menu.NONE, "落下速度")
        item1.setIcon(android.R.drawable.ic_menu_set_as)
        return super.onCreateOptionsMenu(menu)
    }

    //  オプションメニューの実行
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU00 -> {
                Toast.makeText(this, "落下速度が選択されました", Toast.LENGTH_SHORT).show()
                ylib.setMenuDialog(this, "落下速度の設定", downCount, iCopyPasteOperation)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //  setMenuDialogの実行関数(落下速度設定)
    var iCopyPasteOperation = Consumer<String> { s ->
        when (s) {
            "5" ->  { tetrisView.mBlockDownCcount = 5 }
            "10" -> { tetrisView.mBlockDownCcount = 10 }
            "20" -> { tetrisView.mBlockDownCcount = 20 }
            "30" -> { tetrisView.mBlockDownCcount = 30 }
            "40" -> { tetrisView.mBlockDownCcount = 40 }
            "50" -> { tetrisView.mBlockDownCcount = 50 }
        }
    }
}