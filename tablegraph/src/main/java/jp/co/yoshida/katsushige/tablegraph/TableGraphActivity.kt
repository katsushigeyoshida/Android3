package jp.co.yoshida.katsushige.tablegraph

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.tablegraph.databinding.ActivityTableGraphBinding

//import kotlinx.android.synthetic.main.activity_table_graph.*

class TableGraphActivity : AppCompatActivity() {
    val TAG = "TableGraphActivity"

//    lateinit var mTableGraphView: TableGraphView
    lateinit var mTableGraphView: TableGraphSurfaceView
    var mSheetData = SheetData()
    val mGraphType = listOf("折線", "棒線", "積上げ式折線", "積上げ式棒グラフ")
    val mGraphMenu = listOf(
                "表示要素の設定","表示を反転", "すべてを表示", "データ色設定", "スケール設定",
                "増分→累積", "累積→増分", "スムージング", "元に戻す"
    )
    private var mTableGraphViewTop = 616                           //  ViewのTOP位置(マウス位置のオフセット)
    var mDrawFlag = true

    val klib = KLib()

    lateinit var binding: ActivityTableGraphBinding
     lateinit var constraintLayout: ConstraintLayout
    lateinit var llTableGraph: LinearLayout
    lateinit var spStartPosition: Spinner
    lateinit var spEndPosition: Spinner
    lateinit var spGraphType: Spinner
    lateinit var btGraphZoomUp: ImageButton
    lateinit var btGraphZoomDown: ImageButton
    lateinit var btGraphMoveLeft: ImageButton
    lateinit var btGraphMoveRight: ImageButton
    lateinit var btGraphMenu: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_graph)

        Log.d(TAG, "onCreate")
        val filePath = intent.getStringExtra("FILE").toString()
        val graphTitle = intent.getStringExtra("TITLE")
        title = graphTitle

        binding = ActivityTableGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constraintLayout
        llTableGraph = binding.llTableGraph
        spStartPosition = binding.spStartPosition
        spEndPosition = binding.spEndPosition
        spGraphType = binding.spGraphType
        btGraphZoomUp = binding.btGraphZoomUp
        btGraphZoomDown = binding.btGraphZoomDown
        btGraphMoveLeft = binding.btGraphMoveLeft
        btGraphMoveRight = binding.btGraphMoveRight
        btGraphMenu = binding.btGraphMenu

        mSheetData.loadData(filePath)
        if (0 < mSheetData.mDataList.size) {
            mSheetData.toDoubleList()           //  元データを数値データに変換
            mSheetData.initDispList()           //  データ表示設定の初期化
            mSheetData.initColorList()          //  カラーリストを初期化
            mSheetData.getArea()                //  データ標示領域を求める
//        mTableGraphView = TableGraphView(this, mSheetData)
            mTableGraphView = TableGraphSurfaceView(this, mSheetData)

            llTableGraph.addView(mTableGraphView)
            init()
//        mTableGraphView.drawGraph()
        } else {
            Log.d(TAG,"onCreate: File Load Error: " + filePath)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //  TableViewのタッチ位置の縦座標の起点を求める
        mTableGraphViewTop = getWindowHeight() - constraintLayout.height + llTableGraph.top
    }

    //  タッチ位置の前回値
    private var mPreTouchPosition = PointD(0.0, 0.0)
    private var mPreTouchDistance = 0.0
    private var mZoomOn = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false
        var pos = PointD(event.x.toDouble(), (event.y - mTableGraphViewTop).toDouble())
        var pointCount = event.pointerCount
        Log.d(TAG,"onTouchEvent: ${event.action} ${pos} ${pointCount}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {            //  (0)
                mPreTouchPosition = pos
                mZoomOn = false
            }
            MotionEvent.ACTION_UP -> {              //  (1)
                if (!mZoomOn) {
                    var offset = pos.getOffset(mPreTouchPosition)
                    moveDisp(offset.x / mTableGraphView.kdraw.mView.width())
                }
            }
            MotionEvent.ACTION_MOVE -> {            //  (2)

            }
            MotionEvent.ACTION_POINTER_2_DOWN -> {  //  (261)
                if (1 < pointCount) {
                    //  マルチタッチによる拡大縮小の起点取得
                    var pos1 = PointD(event.getX(0).toDouble(), event.getY(0).toDouble())
                    var pos2 = PointD(event.getX(1).toDouble(), event.getY(1).toDouble())
                    mPreTouchDistance = pos1.distance(pos2)
                    mZoomOn = true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {    //  (5)

            }
            MotionEvent.ACTION_POINTER_UP -> {      //  (6)
                if (1 < pointCount) {
                    //  マルチタッチによる拡大縮小
                    var pos1 = PointD(event.getX(0).toDouble(), event.getY(0).toDouble())
                    var pos2 = PointD(event.getX(1).toDouble(), event.getY(1).toDouble())
                    val scale = pos1.distance(pos2) / mPreTouchDistance
                    zoomDisp(scale)
                }
            }
        }
        return true;
//        return super.onTouchEvent(event)
    }

    fun init() {
        Log.d(TAG,"init: ")
        //  [表示開始位置]設定
        var startPositionAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mSheetData.getRowTitleList())
        spStartPosition.adapter = startPositionAdapter
        spStartPosition.setSelection(0)
        spStartPosition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mTableGraphView.mStartPosition != position) {
                    mTableGraphView.mStartPosition = position
//                mTableGraphView.reDraw()
                    mTableGraphView.drawGraph()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }
        //  [表示終了位置]設定
        var endPositionAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mSheetData.getRowTitleList())
        spEndPosition.adapter = endPositionAdapter
        spEndPosition.setSelection(mSheetData.getRowTitleList().size - 1)
        spEndPosition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mTableGraphView.mEndPosition != position) {
                    mTableGraphView.mEndPosition = position
//                mTableGraphView.reDraw()
                    mTableGraphView.drawGraph()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }
        //  [グラフタイプ]選択
        var graphTypeAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mGraphType)
        spGraphType.adapter = graphTypeAdapter
        spGraphType.setSelection(0)
        spGraphType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mTableGraphView.mGraphType.ordinal != position) {
                    mTableGraphView.mGraphType = when (position) {
                        0 -> TableGraphSurfaceView.GRAPHTYPE.LINE
                        1 -> TableGraphSurfaceView.GRAPHTYPE.BAR
                        2 -> TableGraphSurfaceView.GRAPHTYPE.STACKELINE
                        3 -> TableGraphSurfaceView.GRAPHTYPE.STACKEDBAR
                        else -> TableGraphSurfaceView.GRAPHTYPE.LINE
                    }
//                    mTableGraphView.reDraw()
                    mTableGraphView.drawGraph()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        //  [拡大]ボタン
        btGraphZoomUp.setOnClickListener {
            zoomDisp(2.0)
        }
        //  [縮小]ボタン
        btGraphZoomDown.setOnClickListener {
            zoomDisp(0.5)
        }
        //  [左移動]ボタン
        btGraphMoveLeft.setOnClickListener {
            moveDisp(-0.5)
        }
        //  [右移動]ボタン
        btGraphMoveRight.setOnClickListener {
            moveDisp(0.5)
        }
        //  [グラフメニュー]ボタン
        btGraphMenu.setOnClickListener {
            klib.setMenuDialog(this, "メニュー選択", mGraphMenu, iMenuSelectOperation)
        }
    }

    fun zoomDisp(zoom: Double) {
        mTableGraphView.zoomDisp(zoom)
        mTableGraphView.drawGraph()
        spStartPosition.setSelection(mTableGraphView.mStartPosition)
        spEndPosition.setSelection(mTableGraphView.mEndPosition)
    }

    fun moveDisp(move: Double) {
        mTableGraphView.moveDisp(move)
        mTableGraphView.drawGraph()
        spStartPosition.setSelection(mTableGraphView.mStartPosition)
        spEndPosition.setSelection(mTableGraphView.mEndPosition)
    }


    var iMenuSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        Log.d(TAG, "iMenuSelectOperation: "+s)
        when (mGraphMenu.indexOf(s)) {
            0 -> {      //  指定以外を非表示
                dispSelectData()
            }
            1 -> {      //  表示を反転
                for (i in 1..(mSheetData.mDispList.size - 1)) {
                    mSheetData.mDispList[i] = !mSheetData.mDispList[i]
                }
                mTableGraphView.drawGraph()
            }
            2 -> {      //  すべてを表示
                mSheetData.initDispList()
                mTableGraphView.drawGraph()
            }
            3 -> {      //  データ色の設定
                dispDataColor()
            }
            4 ->{       //  スケール設定
                dispDataScale()
            }
            5 -> {      //  増分→累積
                mSheetData.accumulateDoubleData()
                mTableGraphView.drawGraph()
            }
            6 -> {      //  累積→増分
                mSheetData.differentialDoubleData()
                mTableGraphView.drawGraph()
            }
            7 -> {      //  スムージング
                klib.setInputDialog(this, "移動平均のデータ数", "7", iMoveAveCount)
            }
            8 -> {      //  一つ前に戻る
                mSheetData.recoveryDoubleList()
                mTableGraphView.drawGraph()
            }
        }
    }

    /**
     * データの表示/非表示設定
     */
    fun dispSelectData() {
        var titleList = mSheetData.getColTitleList()
        var chkList = mSheetData.getDispList()
        klib.setChkMenuDialog(this, "表示データリスト", titleList.toTypedArray(), chkList.toBooleanArray(), iSelectDataDisp)
    }

    //  データの表示/非表示設定
    var iSelectDataDisp = Consumer<BooleanArray> { s ->
        for (i in 0..(s.size - 1)) {
            mSheetData.mDispList[i + 1] = s[i]
        }
        mTableGraphView.drawGraph()
    }

    /**
     * 選択データの色を再設定する
     */
    fun dispDataColor() {
        //  データの選択
        var titleList = mSheetData.getColTitleList(true)
        klib.setMenuDialog(this, "表示色設定対象データ", titleList, iDispDataColor)
    }

    //  選択データの色を再設定で色の選択
    var iDispDataColor = Consumer<String> { s ->
        mSheetData.setSelectList(s, false)
        klib.setMenuDialog(this, "表示色選択", mTableGraphView.mColors, iSelectDataColor)
    }

    //  選択データの色を再設定を実施
    var iSelectDataColor = Consumer<String> { s ->
        mSheetData.setSelectColor(mTableGraphView.mColors.indexOf(s))
        mTableGraphView.drawGraph()
    }

    //  スムージングの移動平均データサイズ設定
    var iMoveAveCount = Consumer<String> { s ->
        mSheetData.smoothingDoubleData(s.toIntOrNull()?:0)
        Log.d(TAG,"iMoveAveCount: "+mSheetData.mDoubleDataList.size+" "+mSheetData.mDoubleDataList[0].size)
        mTableGraphView.drawGraph()
    }

    /**
     * 選択データをスケール倍する
     */
    fun dispDataScale() {
        //  データの選択
        var titleList = mSheetData.getColTitleList(true)
        klib.setMenuDialog(this, "スケール設定対象データ", titleList, iDispDataScale)
    }

    var iDispDataScale = Consumer<String> { s ->
        mSheetData.setSelectList(s, false)
        klib.setInputDialog(this, "スケール値入力", "1", iSetScale)
    }

    var iSetScale = Consumer<String> { s ->
        var scale = s.toDoubleOrNull()?:1.0
        mSheetData.scaleDoubleData(scale)
        mTableGraphView.drawGraph()
    }

    /**
     * 画面の高さ(下記ボタンを含まない)
     */
    private fun getWindowHeight(): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.height
    }
}