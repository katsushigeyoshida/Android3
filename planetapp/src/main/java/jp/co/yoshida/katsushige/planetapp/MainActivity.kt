package jp.co.yoshida.katsushige.planetapp

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.planetapp.databinding.ActivityMainBinding
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    val TAG ="MainActivity"

    lateinit var mMainSurfaceView: MainSurfaceView
    private lateinit var binding: ActivityMainBinding
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var llPlanetView: LinearLayout
    private lateinit var spMenu: Spinner
    private lateinit var spDataFile: Spinner
    private lateinit var btZoomUp: ImageButton
    private lateinit var btZoomDown: ImageButton
    private lateinit var btMoveLeft: ImageButton
    private lateinit var btMoveRight: ImageButton
    private lateinit var btMoveUp: ImageButton
    private lateinit var btMoveDown: ImageButton
    private lateinit var btSetDate: ImageButton
    private lateinit var btSetTime: ImageButton
    private lateinit var btBack: ImageButton
    private lateinit var btNow: ImageButton
    private lateinit var btForward: ImageButton
    private lateinit var btSettingMenu: ImageButton
    private lateinit var cbConstella: CheckBox
    private lateinit var cbConstellaName: CheckBox
    private lateinit var cbNebula: CheckBox
    private lateinit var cbPlanet: CheckBox

    private var mDataFolder = "StarData"
    private var mAppFolder = ""
    private var mCurFolder = ""
    private var mStarDataPath = "hip_100.csv"
    private var mDataFiles = mutableListOf<String>()

    private val mMenu = listOf(
        "北面表示", "東面表示", "南面表示", "西面表示", "全天表示", "星座早見盤",
        "恒星データ", "表示データ", "データフォルダ", "太陽系", "太陽系地球中心"
    )
    private val mSettingMenu = listOf(
        "恒星表示等級", "恒星名表示等級", "観測地点"
    )
    private val mDispMagnitude = listOf(
        "なし", "1等級", "2等級", "3等級", "4等級", "5等級", "6等級"
    )
    enum class DispType { Celestial, Horizontal, FullHorizontal, SolarSystem, Table }
    private var mDispType = DispType.Horizontal         //  表示形式
    private var mGeoCenter = false                      //  地球中心太陽系システム
    private var mDateTime = LocalDateTime.now()         //  表示日時
    private var mMoveTimeStep = 30L                     //  時間変更ステップサイズ
    private var mMoveSolarTimeStep = 5L                 //  太陽系の日変更ステップサイズ
    private var mMoveDirectionStep = 0.5                //  方位移動ステップサイズ
    private var mPlanetViewTop = 310                    //  ViewのTOP位置(マウス位置のオフセット)

    lateinit var mStarData:StarData                     //  恒星データ
    lateinit var mConstellationData:ConstellationData   //  星座データ
    lateinit var mNebulaData: NebulaData                //  星雲・銀河などのデータ
    lateinit var mStarInfoData: StarInfoData            //  恒星、星雲・銀河の情報データ

    private val mLocationTitle = mutableListOf<String>( //  観測点位置タイトル
        "札幌", "東京", "大阪", "福岡", "那覇"
    )
    private val mLocationData = mutableListOf<PointD>(
        PointD(141.35439, 43.06208 ),              //  札幌
        PointD(139.69172, 35.68956 ),              //  東京
        PointD(135.50211, 34.69375 ),              //  大阪
        PointD(130.40172, 33.59014 ),              //  福岡
        PointD(127.67806, 26.21308 ),              //  那覇
    )

    private val alib = AstroLib()
    private val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constraintLayout
        llPlanetView = binding.llPlanetView
        spMenu       = binding.spMenu
        spDataFile   = binding.spDataFile
        btZoomUp     = binding.btZoomUp
        btZoomDown   = binding.btZoomDown
        btMoveLeft   = binding.btMoveLeft
        btMoveRight  = binding.btMoveRight
        btMoveUp     = binding.btMoveUp
        btMoveDown   = binding.btMoveDown
        btBack       = binding.btBack
        btNow        = binding.btNow
        btForward    = binding.btForward
        btSetDate    = binding.btSetDate
        btSetTime    = binding.btSetTime
        btSettingMenu = binding.btSettingMenu
        cbConstella     = binding.cbConstella
        cbConstellaName = binding.cbConstellaName
        cbNebula        = binding.cbNebula
        cbPlanet        = binding.cbPlanet

        mMainSurfaceView = MainSurfaceView(this)
        llPlanetView.addView(mMainSurfaceView)

        //  パーミッションチェック(ストレージ)
        if (!klib.checkStragePermission(this))
            return
        //  フォルダの設定
        mDataFolder = klib.getPackageNameDirectory(this) + "/" + mDataFolder
        if (!klib.mkdir(mDataFolder)) {
            Toast.makeText(this, "データ保存フォルダが作成できません", Toast.LENGTH_LONG).show()
            return
        }
        mAppFolder = klib.getPackageNameDirectory(this)
        //  恒星データファイルリストの設定
        getDataFile(mAppFolder)
        //  恒星データの設定
        mStarDataPath = mAppFolder + "/" + mStarDataPath
        mStarData = StarData(mDataFolder, mStarDataPath)
        mStarData.loadData()
        mStarData.convStarData()
        //  星座データの設定
        mConstellationData = ConstellationData(mDataFolder)
        mConstellationData.loadData()
        //  星雲・銀河などのデータ
        mNebulaData = NebulaData(mDataFolder)
        mNebulaData.loadData()
        //  情報表示データ
        mStarInfoData = StarInfoData()

        setViewData()

        //  現在時の設定
        mDateTime = LocalDateTime.now()
        setDispButton()

        val location = klib.getStrPreferences("LocationPoint", this)
        var locationNo = 0
        if (location != null && 0 < location.length)
            locationNo = mLocationTitle.indexOf(location)
        if (0 <= locationNo) {
            mMainSurfaceView.mLocalLatitude = mLocationData[locationNo].y
            mMainSurfaceView.mLocalLongitude = mLocationData[locationNo].x
        }

        init()
    }

    /**
     * データをSurfaceViewに設定する
     */
    fun setViewData() {
        mMainSurfaceView.mStarData = mStarData.mStarData
        mMainSurfaceView.mConstellaData = mConstellationData
        mMainSurfaceView.mNebulaData = mNebulaData.mNebulaData
        mMainSurfaceView.mStarStrData = mStarData.mStarStrData
        mMainSurfaceView.mNebulaStrData = mNebulaData.mNebulaStrData

        mStarInfoData.mStarStrData = mStarData.mStarStrData
        mStarInfoData.mStarData = mStarData.mStarData
        mStarInfoData.mNebulaStrData = mNebulaData.mNebulaStrData
        mStarInfoData.mNebulaData = mNebulaData.mNebulaData
        mMainSurfaceView.mStarInfoData = mStarInfoData
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        //  マウス位置をMapViewに合わせるためのオフセット値
        mPlanetViewTop = llPlanetView.top + getWindowHeight() - constraintLayout.height
        Log.d(TAG,"onWindowFocusChanged: ${mPlanetViewTop} ${llPlanetView.top} ${getWindowHeight()} ${constraintLayout.height}")
    }

    //  タッチ位置の前回値
    private var mPreTouchPosition = PointD(0.0, 0.0)
    private var mPreTouchDistance = 0.0
    private var mZoomOn = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false
        var pos = PointD(event.x.toDouble(), (event.y - mPlanetViewTop).toDouble())
        var pointCount = event.pointerCount
        Log.d(TAG,"onTouchEvent: ${event.action} ${pos} ${pointCount}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {            //  (0)
                mPreTouchPosition = pos
                mZoomOn = false
            }
            MotionEvent.ACTION_UP -> {              //  (1)
                if (10 < pos.distance(mPreTouchPosition)) {
                    //  移動処理
                    if (!mZoomOn) {
                        if (mDispType == DispType.Table) {
                            //  表スクロール
                            var colOffset = mMainSurfaceView.kdraw.getColPosition(mPreTouchPosition) -
                                    mMainSurfaceView.kdraw.getColPosition(pos)
                            var rowOffset = mMainSurfaceView.kdraw.getRowPosition(mPreTouchPosition) -
                                    mMainSurfaceView.kdraw.getRowPosition(pos)
                            mMainSurfaceView.kdraw.setColOffset(if (colOffset == 0) -1 else colOffset)
                            mMainSurfaceView.kdraw.setRowOffset(rowOffset)
                            mMainSurfaceView.drawTable()
                        } else {
                            //  画面移動
                            var dx = mMainSurfaceView.kdraw.cnvScreen2WorldX(mPreTouchPosition.x - pos.x)
                            var dy = mMainSurfaceView.kdraw.cnvScreen2WorldY(mPreTouchPosition.y - pos.y)
                            mMainSurfaceView.mWindowOffset.offset(dx, -dy)
                            reDisp()
                        }
                    }
                } else {
                    if (mDispType == DispType.Table) {
                        mMainSurfaceView.kdraw.selectRow(pos)
                        mMainSurfaceView.drawTable()
                    } else {
                        //  情報表示
                        var seachStar = mStarInfoData.searchPos(pos, 30.0)
                        if (!seachStar.coordinate.isEmpty()) {
                            var infoData = mStarInfoData.searchData(seachStar)
                            reDisp()
                            mMainSurfaceView.drawInfoData(infoData)
                        }
                    }
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
                    if (mDispType == DispType.Table) {
                        mMainSurfaceView.kdraw.mTableTextSize *= scale.toFloat()
                        mMainSurfaceView.drawTable()
                    } else if (mDispType == DispType.Horizontal) {
                        mMainSurfaceView.mWindowScale *= scale
                        mMainSurfaceView.drawHorizontal()
                    } else if (mDispType == DispType.FullHorizontal) {
                        mMainSurfaceView.mWindowScale *= scale
                        mMainSurfaceView.drawFullHorizontal()
                    } else if (mDispType == DispType.Celestial) {
                        mMainSurfaceView.mWindowScale *= scale
                        mMainSurfaceView.drawCelestial()
                    }
                }
            }
        }
        return true;
//        return super.onTouchEvent(event)
    }

    /**
     * 初期化設定
     */
    fun init() {
        //  [操作メニュー]コンボボックス
        val menuListAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mMenu)
        spMenu.adapter = menuListAdapter
        spMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 6) {
                    if (getDataFile(mAppFolder))
                        setDataFileList()
                }
                when (position) {
                    0 -> {              //  北面表示
                        horizontalDisp(12.0)
                    }
                    1 -> {              //  東面表示
                        horizontalDisp(18.0)
                    }
                    2 -> {              //  南面表示
                        horizontalDisp(0.0)
                    }
                    3 -> {              //  西面表示
                        horizontalDisp(6.0)
                    }
                    4 -> {              //  全天表示
                        fullHorizontalDisp()
                    }
                    5 -> {              //  天球面表示(星座早見盤)
                        celestialDisp()
                    }
                    6 -> {              //  恒星データ
                        mStarData.mStarStrData = mStarData.convStrStarData(mStarData.mStarStrData)
                        tableDisp(mStarData.mStarStrData)
                    }
                    7 -> {              //  表示データ(地平データ)
                        val starStrData = mStarData.getHorizonStarData(mMainSurfaceView.mLst, mMainSurfaceView.mLocalLatitude)
                        tableDisp(starStrData)
                    }
                    8 -> {              //  データフォルダデータ表示
                        if (getDataFile(mDataFolder)) {
                            setDataFileList()
                            val dataPath = mCurFolder + "/" + mDataFiles[0]
                            loadTableDataDisp(dataPath)
                        }
                    }
                    9 -> {              //  太陽系日心シミュレーション
                        solarSystemDisp()
                    }
                    10 -> {             //  太陽系地心シミュレーション
                        solarSystemDisp(true)
                    }
                }
                setDispButton()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }
        //  [データファイル選択]コンボボックス
        setDataFileList()
        spDataFile.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                if (0 <= position) {
                    if (mCurFolder.compareTo(mAppFolder) == 0) {
                        //  アプリケーションフォルダの選択
                        mStarDataPath = mAppFolder + "/" + mDataFiles[position]
                        mStarData.mStarDataPath = mStarDataPath
                        mStarData.loadData()
                        mStarData.convStarData()
                        setViewData()
                        if (mDispType == DispType.Horizontal) {
                            horizontalDisp(mMainSurfaceView.mDirection)
                        } else if (mDispType == DispType.Celestial) {
                            celestialDisp()
                        } else if (mDispType == DispType.FullHorizontal) {
                            fullHorizontalDisp()
                        } else if (mDispType == DispType.Table) {
                            mStarData.mStarStrData = mStarData.convStrStarData(mStarData.mStarStrData)
                            tableDisp(mStarData.mStarStrData)
                        }
                    } else {
                        //  データフォルダのファイル選択
                        val dataPath = mCurFolder + "/" + mDataFiles[position]
                        var starData = StarData(mDataFolder, dataPath)
                        var dataList = alib.loadData(dataPath, 1)
                        var meargeData = alib.meargeTitle(dataList)
                        tableDisp(meargeData)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        /**
         * [星座表示線]チェックボックス
         */
        cbConstella.setOnClickListener {
            if (cbConstella.isChecked) {
                mMainSurfaceView.mConstallaLineDisp = true
            } else {
                mMainSurfaceView.mConstallaLineDisp = false
            }
            reDisp()
        }

        /**
         * [星座名表示]チェックボックス
         */
        cbConstellaName.setOnClickListener {
            if (cbConstellaName.isChecked) {
                mMainSurfaceView.mConstallaNameDisp = true
            } else {
                mMainSurfaceView.mConstallaNameDisp = false
            }
            reDisp()
        }

        /**
         * [星座名表示]チェックボックス
         */
        cbNebula.setOnClickListener {
            if (cbNebula.isChecked) {
                mMainSurfaceView.mNebulaDisp = true
            } else {
                mMainSurfaceView.mNebulaDisp = false
            }
            reDisp()
        }

        /**
         * [惑星表示]チェックボックス
         */
        cbPlanet.setOnClickListener {
            if (cbPlanet.isChecked) {
                mMainSurfaceView.mPlanetDisp = true
            } else {
                mMainSurfaceView.mPlanetDisp = false
            }
            reDisp()
        }

        /**
         *  [拡大]ボタン
         */
        btZoomUp.setOnClickListener {
            if (mDispType == DispType.Table) {
                mMainSurfaceView.kdraw.mTableTextSize *= 1.2f
                mMainSurfaceView.drawTable()
            } else if (mDispType == DispType.Horizontal) {
                mMainSurfaceView.mWindowScale *= 1.2
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                mMainSurfaceView.mWindowScale *= 1.2
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                mMainSurfaceView.mWindowScale *= 1.2
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                mMainSurfaceView.mWindowScale *= 1.2
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *  [縮小]ボタン
         */
        btZoomDown.setOnClickListener {
            if (mDispType == DispType.Table) {
                mMainSurfaceView.kdraw.mTableTextSize /= 1.2f
                mMainSurfaceView.drawTable()
            } else if (mDispType == DispType.Horizontal) {
                mMainSurfaceView.mWindowScale /= 1.2
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                mMainSurfaceView.mWindowScale /= 1.2
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                mMainSurfaceView.mWindowScale /= 1.2
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                mMainSurfaceView.mWindowScale /= 1.2
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *  [左移動]ボタン
         */
        btMoveLeft.setOnClickListener {
            if (mDispType == DispType.Table) {
                val colLeft = -mMainSurfaceView.kdraw.dispColCount() + 1
                mMainSurfaceView.kdraw.setColOffset(if (colLeft == 0) -1 else colLeft)
                mMainSurfaceView.drawTable()
            } else if (mDispType == DispType.Horizontal) {
                mMainSurfaceView.mDirection -= mMoveDirectionStep
                if (mMainSurfaceView.mDirection < 0.0)
                    mMainSurfaceView.mDirection = 24.0
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                mMainSurfaceView.mDirection -= mMoveDirectionStep
                if (mMainSurfaceView.mDirection < 0.0)
                    mMainSurfaceView.mDirection = 24.0
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                mMainSurfaceView.mDirection -= mMoveDirectionStep
                if (mMainSurfaceView.mDirection < 0.0)
                    mMainSurfaceView.mDirection = 24.0
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                //  ここでは上向きボタンのイメージ
                mMainSurfaceView.mRollX += 5
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *  [右移動]ボタン
         */
        btMoveRight.setOnClickListener {
            if (mDispType == DispType.Table) {
                mMainSurfaceView.kdraw.setColOffset(mMainSurfaceView.kdraw.dispColCount() - 1)
                mMainSurfaceView.drawTable()
            } else if (mDispType == DispType.Horizontal) {
                mMainSurfaceView.mDirection += mMoveDirectionStep
                mMainSurfaceView.mDirection = mMainSurfaceView.mDirection % 24.0
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                mMainSurfaceView.mDirection += mMoveDirectionStep
                mMainSurfaceView.mDirection = mMainSurfaceView.mDirection % 24.0
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                mMainSurfaceView.mDirection += mMoveDirectionStep
                mMainSurfaceView.mDirection = mMainSurfaceView.mDirection % 24.0
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                //  ここでは下向きボタンのイメージ
                mMainSurfaceView.mRollX -= 5
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *  [上移動]ボタン
         */
        btMoveUp.setOnClickListener {
            if (mDispType == DispType.Table) {
                mMainSurfaceView.kdraw.setRowOffset(-(mMainSurfaceView.kdraw.dispRowCount() - 1))
                mMainSurfaceView.drawTable()
            }
        }

        /**
         *   [下移動]ボタン
         */
        btMoveDown.setOnClickListener {
            if (mDispType == DispType.Table) {
                mMainSurfaceView.kdraw.setRowOffset(mMainSurfaceView.kdraw.dispRowCount() - 1)
                mMainSurfaceView.drawTable()
            }
        }

        /**
         *  [戻り]ボタン
         */
        btBack.setOnClickListener {
            if (mDispType == DispType.Table) {
            } else if (mDispType == DispType.Horizontal) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.minusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.minusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.minusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.minusDays(mMoveSolarTimeStep)
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *   [現在時]ボタン
         */
        btNow.setOnClickListener {
            if (mDispType == DispType.Table) {
            } else if (mDispType == DispType.Horizontal) {
                mMainSurfaceView.mLocalDateTime = LocalDateTime.now()
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                mMainSurfaceView.mLocalDateTime = LocalDateTime.now()
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                mMainSurfaceView.mLocalDateTime = LocalDateTime.now()
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                mMainSurfaceView.mLocalDateTime = LocalDateTime.now()
                mMainSurfaceView.ptolemaicInit()
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *   [進む]ボタン
         */
        btForward.setOnClickListener {
            if (mDispType == DispType.Table) {
            } else if (mDispType == DispType.Horizontal) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.plusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawHorizontal()
            } else if (mDispType == DispType.FullHorizontal) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.plusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawFullHorizontal()
            } else if (mDispType == DispType.Celestial) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.plusMinutes(mMoveTimeStep)
                mMainSurfaceView.drawCelestial()
            } else if (mDispType == DispType.SolarSystem) {
                var dateTime = mMainSurfaceView.mLocalDateTime
                mMainSurfaceView.mLocalDateTime = dateTime.plusDays(mMoveSolarTimeStep)
                mMainSurfaceView.drawSolarSystem(mGeoCenter)
            }
        }

        /**
         *   [日付設定]ボタン
         */
        btSetDate.setOnClickListener {
            var dateTime = mMainSurfaceView.mLocalDateTime
            klib.showDatePicker(this, dateTime, iLocalDateTime)
        }

        /**
         *   [時間設定]ボタン
         */
        btSetTime.setOnClickListener {
            var dateTime = mMainSurfaceView.mLocalDateTime
            klib.showTimePicker(this, dateTime, iLocalDateTime)
        }

        /**
         *   [設定メニュー]ボタン
         */
        btSettingMenu.setOnClickListener {
            klib.setMenuIntDialog(this, "設定操作", mSettingMenu, iSettingOperation)
        }
    }

    /**
     * [メニュー]ボタン関数インターフェース
     */
    var iSettingOperation = Consumer<Int> { s ->
        when (s) {
            0 -> {       //  恒星表示等級
                klib.setMenuIntDialog(this, "恒星の表示等級の選択", mDispMagnitude, iStarDispMagnitude)
            }
            1 -> {       //  恒星名表示等級
                klib.setMenuIntDialog(this, "恒星名の表示等級の選択", mDispMagnitude, iStarNameDispMagnitude)
            }
            2 -> {       //  観測地点
                klib.setMenuIntDialog(this, "観測地点の選択", mLocationTitle, iSetLocation)
            }
            3 -> {       //  星座線表示切替

            }
        }
    }

    /**
     * 観測点位置の変更
     */
    var iSetLocation = Consumer<Int> { s ->
        mMainSurfaceView.mLocalLatitude = mLocationData[s].y
        mMainSurfaceView.mLocalLongitude = mLocationData[s].x
        klib.setStrPreferences(mLocationTitle[s], "LocationPoint", this)
        reDisp()
    }

    /**
     * 恒星の表示等級設定関数インターフェース
     */
    var iStarDispMagnitude = Consumer<Int> { s ->
        mMainSurfaceView.mStarDispMagnitude = s.toDouble()
        reDisp()
    }

    /**
     * 恒星名表示等級設定関数インターフェース
     */
    var iStarNameDispMagnitude = Consumer<Int> { s ->
        mMainSurfaceView.mStarNameDispMagnitude = s.toDouble()
        reDisp()
    }

    /**
     * 日付・時間設定ダイヤログの関数インターフェース
     */
    var iLocalDateTime = Consumer<LocalDateTime> { s ->
        mMainSurfaceView.mLocalDateTime = s
        if (mDispType == DispType.Horizontal)
            mMainSurfaceView.drawHorizontal()
        else if (mDispType == DispType.SolarSystem) {
            mMainSurfaceView.ptolemaicInit()
            mMainSurfaceView.drawSolarSystem(mGeoCenter)
        }
    }

    /**
     * 表データのファイル選択
     */
    fun dataFileSelect() {
        klib.fileSelectDialog(this, klib.getPackageNameDirectory(this), "*.csv", true, iFilePath)
    }

    /**
     * 表データのファイルデータ表示関数インターフェース
     */
    var  iFilePath = Consumer<String>() { s ->
        if (0 < s.length) {
            Log.d(TAG, "dataFileSelect: "+s )
            var starData = StarData(mDataFolder, s)
            var dataList = alib.loadData(s, 1)
            var meargeData = alib.meargeTitle(dataList)
            tableDisp(meargeData)
        }
    }

    /**
     * データファイルリストの作成
     * folder       検索フォルダ
     * return       データの更新有無
     */
    fun getDataFile(folder: String): Boolean {
        if (mCurFolder.compareTo(folder) == 0)
            return false
        var defualtFiles = klib.getFileList(folder, false, mStarDataPath)
        var files = klib.getFileList(folder, false, "*.csv")
        mDataFiles.clear()
        for (file in files) {
            mDataFiles.add(file.name)
        }
        if (0 < defualtFiles.count()) {
            if (mDataFiles.contains(defualtFiles[0].name))
                mDataFiles.remove(defualtFiles[0].name)
            mDataFiles.add(0, defualtFiles[0].name)
        }
        mCurFolder = folder
        return true
    }

    /**
     * データファイルリストをコンボボックスに設定
     */
    fun setDataFileList() {
        val fileListAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mDataFiles)
        spDataFile.adapter = fileListAdapter
    }

    /**
     * 表データを読み込んで表示する
     * fileName     データファイル名
     */
    fun loadTableDataDisp(fileName: String) {
        var starData = StarData(mCurFolder, fileName)
        var dataList = alib.loadData(fileName, 1)
        var meargeData = alib.meargeTitle(dataList)
        tableDisp(meargeData)
    }

    /**
     * 再表示(表示更新)
     */
    fun reDisp() {
        if (mDispType == DispType.Horizontal) {
            mMainSurfaceView.drawHorizontal()
        } else if (mDispType == DispType.Celestial) {
            celestialDisp()
        } else if (mDispType == DispType.FullHorizontal) {
            fullHorizontalDisp()
        } else if (mDispType == DispType.SolarSystem) {
            solarSystemDisp()
        } else if (mDispType == DispType.Table) {
            mMainSurfaceView.drawTable()
        }
        setDispButton()
    }

    /**
     * 地平座標表示
     * direction        方位
     */
    fun horizontalDisp(direction: Double) {
        mDispType = DispType.Horizontal
        mMainSurfaceView.mDirection = direction
        mMainSurfaceView.drawHorizontal()
        setDispButton()
    }

    /**
     * 全天表示
     */
    fun fullHorizontalDisp() {
        mDispType = DispType.FullHorizontal
        mMainSurfaceView.drawFullHorizontal()
        setDispButton()
    }

    /**
     * 天球面表示
     */
    fun celestialDisp() {
        mDispType = DispType.Celestial
        mMainSurfaceView.drawCelestial()
        setDispButton()
    }

    /**
     * 票データ表示
     * tableData        表データ(リスト)
     */
    fun tableDisp(tableData: List<List<String>>) {
        mDispType = DispType.Table
        mMainSurfaceView.initTable(tableData)
        mMainSurfaceView.drawTable()
        setDispButton()
    }

    /**
     * 太陽系の表示
     * geo          地球中心
     */
    fun solarSystemDisp(geo: Boolean = false) {
        mDispType = DispType.SolarSystem
        mGeoCenter = geo
        mMainSurfaceView.ptolemaicInit()
        mMainSurfaceView.drawSolarSystem(mGeoCenter)
        setDispButton()
    }

    /**
     * ボタンの表示非表示設定
     */
    fun setDispButton(){
        if (mDispType == DispType.Table) {
            btMoveLeft.isVisible  = true
            btMoveLeft.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
            btMoveRight.isVisible = true
            btMoveRight.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
            btMoveUp.isVisible    = true
            btMoveDown.isVisible  = true
            btBack.isVisible      = false
            btNow.isVisible       = false
            btForward.isVisible   = false
            cbNebula.isVisible    = false
            cbConstella.isVisible = false
            cbConstellaName.isVisible = false
            cbPlanet.isVisible    = false
            btSetDate.isEnabled   = false
            btSetTime.isEnabled   = false
        } else if (mDispType == DispType.SolarSystem) {
            btMoveLeft.isVisible  = true
            btMoveLeft.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            btMoveRight.isVisible = true
            btMoveRight.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            btMoveUp.isVisible    = false
            btMoveDown.isVisible  = false
            btBack.isVisible      = true
            btNow.isVisible       = true
            btForward.isVisible   = true
            cbNebula.isVisible    = false
            cbConstella.isVisible = false
            cbConstellaName.isVisible = false
            cbPlanet.isVisible    = false
            btSetDate.isEnabled   = true
            btSetTime.isEnabled   = true
        } else {
            btMoveLeft.isVisible  = true
            btMoveLeft.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
            btMoveRight.isVisible = true
            btMoveRight.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
            btMoveUp.isVisible    = false
            btMoveDown.isVisible  = false
            btBack.isVisible      = true
            btNow.isVisible       = true
            btForward.isVisible   = true
            cbNebula.isVisible    = true
            cbConstella.isVisible = true
            cbConstellaName.isVisible = true
            cbPlanet.isVisible    = true
            btSetDate.isEnabled   = true
            btSetTime.isEnabled   = true
        }
    }


    /**
     * 画面の高さ(下記ボタンを含まない)
     */
    private fun getWindowHeight(): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.height
    }

    /**
     * 画面の幅(下記ボタンを含まない)
     */
    private fun getWindowWidth(): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.width
    }
}