package jp.co.yoshida.katsushige.mapapp

import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityGpsGraphBinding
import jp.co.yoshida.katsushige.mylib.GpxReader
import jp.co.yoshida.katsushige.mylib.KLib


//import kotlinx.android.synthetic.main.activity_gps_graph.*

class GpsGraph : AppCompatActivity() {
    val TAG = "GpsGraph"

    val klib = KLib()

    lateinit var mGpsGraphView: GpsGraphView
    lateinit var binding: ActivityGpsGraphBinding
    lateinit var linearLayoutGraphview: LinearLayout
    lateinit var spVertical: Spinner
    lateinit var spHorizontal: Spinner
    lateinit var spAverageCount: Spinner
    lateinit var btZoomUp: Button
    lateinit var btZoomDown: Button
    lateinit var btMoveLeft: Button
    lateinit var btMoveRight: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_graph)

        val filePath = intent.getStringExtra("FILE")
        val gpsTitle = intent.getStringExtra("TITLE")
        title = klib.getFileNameWithoutExtension(gpsTitle.toString())

        //  csvファイルからGPSデータの取得
        var gpxReader = GpxReader(GpxReader.DATATYPE.gpxData)
        Log.d(TAG,"onCreate: "+filePath)
        if (0 < filePath.toString().indexOf(".csv")) {
            var gpsTrace = GpsTrace()
            var gpsData = gpsTrace.loadGpxData(filePath.toString()) //  CSV形式のファイルからGPSデータを読み込む
            if (0 < gpsData.size) {
                gpxReader.location2GpsData(gpsData, false)
            } else {
                Toast.makeText(this, "データがありません", Toast.LENGTH_LONG).show()
                return
            }
        } else if (0 < filePath.toString().indexOf(".gpx")) {
            if (0 > gpxReader.getGpxRead(filePath.toString())) {
                Toast.makeText(this, "データがありません", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            Toast.makeText(this, "データがありません", Toast.LENGTH_LONG).show()
            return
        }

        gpxReader.setGpsInfoData()
        mGpsGraphView = GpsGraphView(this, gpxReader)

        //  初期化
        initControl()

        //  API30以上でのScreenSizek取得
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = this.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val ScreenWidth = windowMetrics.bounds.width()
            val ScreenHeight = windowMetrics.bounds.height()
            val StatusBar = insets.top
            val NavigationBar = insets.bottom
            Log.d(TAG,"onCreate: ScreenSize "+ScreenWidth+" "+ScreenHeight+" "+StatusBar+" "+NavigationBar)
            mGpsGraphView.setInitGraphScreen(ScreenWidth, ScreenHeight - 600)    //  600はGraphエリア以外
        }
    }

    //  初期化処理
    fun initControl() {
        binding = ActivityGpsGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        linearLayoutGraphview = binding.linearLayoutGraphview
        spVertical = binding.spVertical
        spHorizontal = binding.spHorizontal
        spAverageCount = binding.spAverageCount
        btZoomUp = binding.button29
        btZoomDown = binding.button30
        btMoveLeft = binding.button31
        btMoveRight = binding.button32

        linearLayoutGraphview.addView(mGpsGraphView)

        //  縦軸のデータ種別の設定
        spVertical.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, mGpsGraphView.mGraphYType)
        spVertical.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mGpsGraphView.mYTypeTitle = mGpsGraphView.mGraphYType[position]
                mGpsGraphView.reDraw()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //  横軸のデータ種別の設定
        spHorizontal.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, mGpsGraphView.mGraphXType)
        spHorizontal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mGpsGraphView.mXTypeTitle = mGpsGraphView.mGraphXType[position]
                mGpsGraphView.reDraw()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //  移動平均のデータ数の設定
        spAverageCount.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, mGpsGraphView.mAverageCount)
        spAverageCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mGpsGraphView.mAverageCountTitle = mGpsGraphView.mAverageCount[position]
                mGpsGraphView.reDraw()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        /**
         * [⊕]ボタン 横方向拡大
         */
        btZoomUp.setOnClickListener {
            mGpsGraphView.zoomDisp(2.0)
            mGpsGraphView.reDraw()
        }

        /**
         * [⊖]ボタン 横方向縮小
         */
        btZoomDown.setOnClickListener {
            mGpsGraphView.zoomDisp(0.5)
            mGpsGraphView.reDraw()
        }

        /**
         * グラフを左に移動
         */
        btMoveLeft.setOnClickListener {
            mGpsGraphView.moveDisp(-0.5)
            mGpsGraphView.reDraw()
        }

        /**
         * グラフを右に移動
         */
        btMoveRight.setOnClickListener {
            mGpsGraphView.moveDisp(0.5)
            mGpsGraphView.reDraw()
        }
    }

}