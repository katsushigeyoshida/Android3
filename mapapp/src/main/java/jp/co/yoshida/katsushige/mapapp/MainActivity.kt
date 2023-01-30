package jp.co.yoshida.katsushige.mapapp

//import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mapapp.GpsService.Companion.mGpsFileName
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityMainBinding
import jp.co.yoshida.katsushige.mylib.DownLoadWebFile
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import java.io.File

//  GPS位置情報取得
//  https://akira-watson.com/android/kotlin/gps-simple.html

class MainActivity : AppCompatActivity() {
//    , LocationListener {
    val TAG = "MapApp.MainActivity"

    var mBaseFolder = ""                            //  地図画像ファイル保存フォルダ
    var mImageFileSetPath = "ImageFileSet.csv"      //  ダウンロードしたファイルリストのファイル名
    val mImageFileSet = mutableSetOf<String>()      //  ダウンロードしたファイルリスト(Web上に存在しないファイルも登録)
    val mAreaDataListPath = "AreaDataList.csv"      //  画面登録リスト保存ファイル名
    val mMarkListPath = "MarkList.csv"              //  マークリスト保存ファイル名
    val mGpxDataListPath = "GpxDataList.csv"        //  GPXデータリストのファイル名
    var mGpsTraceFileFolder = "GpsTraceData"        //  GPSトレースデータの保存フォルダ名
    var mGpsTraceListPath = "GpsTraceList.csv"      //  GPSトレースリストのファイル名
    var mSelectMark = -1                            //  選択マークデータMo
    var mElevatorDataNo = 0                         //  標高データの種類(0: dem5a,1: dem5b,

    val MENU00 = 0
    val MENU01 = 1
    val MENU02 = 2
    val MENU03 = 3
    val MENU04 = 4
    val MENU05 = 5
    val MENU06 = 6
    val MENU07 = 7
    val MENU08 = 8
    val MENU09 = 9
    val MENU10 = 10
    val MENU11 = 11

    val REQUESTCODE_WIKI = 1
    val REQUESTCODE_MAPINFDATA = 2
    val REQUESTCODE_GPXEDIT = 3
    val REQUESTCODE_GPXFILELIST = 4
    val REQUESTCODE_GPSTRACELIST = 5

    //  オプションサブメニュー(画面登録)
    val mMapDispMenu = listOf(
        "登録画面呼び出し", "地図画面登録", "登録画面削除")
    //  オプションサブメニュー(マーク操作)
    val mMarkMenu = listOf(
        "中心位置を登録", "編集", "表示切替", "グループ表示切替", "ソート設定",
        "リストのインポート", "リストのエキスポート", "マークサイズ")

    //  長押し時のコンテキストメニュー項目
    var mLongTouchMenu = mutableListOf<String>(
        "マーク位置へ移動", "マーク登録", "マーク編集", "マーク参照", "マーク削除",
        "Wikiリスト検索", "距離測定開始")

    val mMarkSizeMenu = listOf(
        "0.3", "0.5", "0.7", "1.0", "1.2", "1.5", "1.7", "2.0", "2.5", "3.0", "4.0", "5.0")

    enum class WebFileDownLoad {                    //  WebFileのダウンロードモード
        NORMAL,                         //  地図ファイルなしでかつ登録データなしの時にダウンロード
        UPDATE,                         //  常にダウンロード
        ALLUPDATE,                      //  地図ファイルなしでダウンロード(登録データの有無に関係なく)
        OFFLINE                         //  地図ファイルの有無にかかわらずダウンロードなし
    }
    var mMapDataDownLoadMode = WebFileDownLoad.NORMAL

    var mGpsLocation = false                        //  GPSに追従して地図を動かす
    var mGpsEnable = false                          //  GPS使用可否
    var mElevatorDataList = mutableMapOf<String, List<List<String>>>()  //  標高データリスト


    lateinit var mMapView: MapView                  //  表示Viewクラス
    var mMapViewTop = 310                           //  ViewのTOP位置(マウス位置のオフセット)
    var mMapInfoData = MapInfoData()                //  地図のデータ情報
    var mMapData = MapData(this, mMapInfoData)      //  地図のパラメータクラス
    var mAreaData = AreaData()                      //  画面登録クラス
    var mMarkList = MarkList()                      //  マークリストクラス
    var mMeasure = Measure()                        //  距離測定クラス
    var mGpxDataList = GpsDataList()                //  GPXデータリスト
    var mGpsTraceList = GpsTraceList()              //  GPSトレースリスト
    var mGpsTrace = GpsTrace()                      //  GpsのLogをとる

    val handler = Handler(Looper.getMainLooper())   //  Handlerw@でUIを制御
    val mGpsInterval = 5000L                        //  GPSトレース表示のインターバル(ms)
    var mDataFolder = ""                            //  データ保存ディレクトリ
    var mMapInfoDataPath = "MapDataList.csv"        //  地図データリストファイル名

    val mAppTitle = "こんな地図"
    var klib = KLib()

    lateinit var binding: ActivityMainBinding
    lateinit var constraintLayout: ConstraintLayout
    lateinit var linearLayoutMap: LinearLayout
    lateinit var btMoveUp: Button
    lateinit var btMoveDown: Button
    lateinit var btMoveLeft: Button
    lateinit var btMoveRight: Button
    lateinit var btZoomUp: Button
    lateinit var btZoomDown: Button
    lateinit var btMark: Button
    lateinit var btGpsOn: Button
    lateinit var btRedraw: Button
    lateinit var btClockInc: Button
    lateinit var spMapType: Spinner
    lateinit var spZoomLevel: Spinner
    lateinit var spColCount:Spinner

    //  GPS取得のタイマースレッド
    val runnable = object : Runnable {
        override fun run() {
            mGpsTrace.loadGpsPointData()
            var bp = klib.coordinates2BaseMap(mGpsTrace.lastPosition())
            Log.d(TAG, "runnable: " + bp.toString())
            if (bp.x != 0.5 || bp.y != 0.5) {
                mMapData.setLocation(bp)        //  地図の中心に移動
                mapDisp(mMapDataDownLoadMode)
            }
            handler.postDelayed(this, mGpsInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG,"onCreate")

        chkFileAccessPermission()   //  ファイルアクセスのパーミッション設定

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constraintLayout
        linearLayoutMap = binding.linearLayoutMap
        btMoveUp = binding.btMoveUp
        btMoveDown = binding.btMoveDown
        btMoveLeft = binding.btMoveLeft
        btMoveRight = binding.btMoveRight
        btZoomUp = binding.btZoomUp
        btZoomDown = binding.btZoomDown
        btMark = binding.btMark
        btGpsOn = binding.btGpsOn
        btRedraw = binding.btRedraw
        btClockInc = binding.btClockInc
        spColCount = binding.spColCount
        spZoomLevel = binding.spZoomLevel
        spMapType = binding.spMapType

        mDataFolder = klib.getPackageNameDirectory(this)
        Log.d(TAG,"onCreate: " + mDataFolder)
        if (!klib.mkdir(mDataFolder)) {
            Log.d(TAG, "onCreate: NG "+mDataFolder)
            Toast.makeText(this, "フォルダ作成不可:" + mDataFolder, Toast.LENGTH_LONG).show()
        }
        mMapInfoDataPath = mDataFolder + "/" + mMapInfoDataPath
        mMapInfoData.loadMapInfoData(mMapInfoDataPath)  //  地図データリストの読込
        mGpsTraceListPath = mDataFolder + "/" + mGpsTraceListPath

        init()                                  //  コントロールなどの初期化

        //  地図データの初期化
        mMapView = MapView(this, mMapData)
        mMapView.mMarkList = mMarkList          //  マークリストデータの設定
        mMapView.mMeasure = mMeasure            //  距離測定データの設定
        mMapView.mGpsDataList = mGpxDataList    //  GPSデータの表示設定
        mMapView.mGpsTraceList = mGpsTraceList  //  GPSトレースの表示設定
        mMapData.mDataFolder = mDataFolder      //  データファイルフォルダ設定
        mMapData.loadParameter()                //  パラメータの読み込み
        setParameter()
        setViewParameter()
        mapInit()

        //  ダウンロードのモードを引き継ぐ
        var downloadMode = klib.getStrPreferences("WebFileDownLoadMode", this).toString()
        if (downloadMode.compareTo("###")!=0)
            mMapDataDownLoadMode = WebFileDownLoad.valueOf(downloadMode)
        mapDataSet(mMapDataDownLoadMode)
        linearLayoutMap.addView(mMapView)

        //	位置情報の初期化
        initGps()
    }

    /**
     *  ファイルアクセスのパーミッションチェック
     */
    fun chkFileAccessPermission(){
        if (30<= Build.VERSION.SDK_INT)
            chkManageAllFilesAccess()
        else
            klib.checkStragePermission(this)
    }

    /**
     *  MANAGE_ALL_FILES_ACCESS_PERMISSIONの確認(Android11 API30以上)
     */
    fun chkManageAllFilesAccess() {
        var file = File("/storage/emulated/0/chkManageAllFilesAccess.txt")
        try {
            if (file.createNewFile()) {
                Log.d(TAG,"chkManageAllFilesAccess: create " + "OK")
            }
        } catch (e: Exception) {
            Log.d(TAG,"chkManageAllFilesAccess: create " + "NG")
            val intent = Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION")
            startActivity(intent)
        }
    }

    /**
     *  LinearLayoutMap(MapTestView)の大きさを取得する
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mMapView.mWidth = linearLayoutMap.width
        mMapView.mHeight = linearLayoutMap.height
        mMapData.mView = Size(linearLayoutMap.width, linearLayoutMap.height)
        //  マウス位置をMapViewに合わせるためのオフセット値
        mMapViewTop = linearLayoutMap.top + getWindowHeight() - constraintLayout.height
    }

    /**
     * 再表示でのデータの更新
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        //  GPXデータリストの読込
        mGpxDataList.mSaveFilePath = mDataFolder + "/" + mGpxDataListPath
        klib.setStrPreferences(mGpxDataList.mSaveFilePath, "GpxDataListPath", this)
        mGpxDataList.loadDataFile(this)
        //  GPSトレースデータ
        mGpsTraceList.mGpsTraceListPath = mGpsTraceListPath
        mGpsTraceList.mGpsTraceFileFolder = mGpsTraceFileFolder
        mGpsTraceList.loadListFile()
        klib.setStrPreferences(mGpsTraceListPath, "GpsTraceListPath", this)
    }

    /**
     *  終了処理(Stop)
     */
    override fun onStop() {
        Log.d(TAG,"onStop")
        //  データの保存
        mMapInfoData.saveMaoInfoData(mMapInfoDataPath)  //  地図データリストの保存
        mMapData.saveParameter()                        //  地図パラメータの保存
        klib.setStrPreferences(mMapDataDownLoadMode.name, "WebFileDownLoadMode", this)
        mMapData.saveImageFileSet(mImageFileSetPath)    //  ダウンロードファイルリストの保存
        mAreaData.saveAreaDataList()                    //  登録画面リストの保存
        mMarkList.saveMarkFile(this)            //  マークリストの保存
        mGpxDataList.saveDataFile(this)             //  GPSデータリストの保存
        mGpsTraceList.saveListFile()                    //  GPSトレースリストの保存

        //  GPX変換が終わっていなければ処理待ちをおこなう
        if (GpsTraceList.mGpxConvertOn) {
            Log.d(TAG, "onStop : " + "GPX変換エキスポート中")
            klib.messageDialog(this, "確認", "GPX変換中ですが終了しますか", iStopOperation)
        } else {
            Log.d(TAG, "onStop : " + "GPX変換終了")
            super.onStop()
        }
    }

    /**
     *  終了処理(Destroy)
     */
    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        //  データの保存
        super.onDestroy()
    }

    var iStopOperation = Consumer<String> { s ->
        if (s.compareTo("OK") == 0) {
            GpsTraceList.mGpxConvertOn = false
            finish()
        }
    }


    //  タッチ位置の前回値
    var mPreTouchPosition = PointD(0.0, 0.0)
    var mPreTouchDistance = 0.0
    var mZoomOn = false
    //長押しのTouchEvemtの取得のための時間計測用
    var startTime:Long = 0
    var endTime:Long = 0

    /**
     *  マルチタッチ処理
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var pos = PointD(event.x.toDouble(), (event.y - mMapViewTop).toDouble())
        var pointCount = event.pointerCount
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {    //  (0)
                startTime = event.eventTime
                //  画面移動の起点
                mPreTouchPosition = pos
                mZoomOn = false
                //  距離測定
                if (mMeasure.mMeasureMode) {
                    mMeasure.add(mMapData.screen2BaseMap(pos))
                    if (0 < mMeasure.mPositionList.size)
                        mapDisp(mMapDataDownLoadMode)
                }
            }
            MotionEvent.ACTION_UP -> {      //  (1)
                if (!mZoomOn) {
                    endTime = event.eventTime
                    if ((endTime - startTime) > 500 && pos.distance(mPreTouchPosition) < 50) {
                        //  長押し処理
                        if (mMeasure.mMeasureMode) {
                            mMeasure.decriment()
                        }
                        klib.setMenuDialog(this, "コマンド選択", mLongTouchMenu, iLongTouchMenu)
                        startTime = 0
                        endTime = 0
                    } else {
                        //  画面移動
                        var dx = (mPreTouchPosition.x - pos.x) / mMapData.mCellSize
                        var dy = (mPreTouchPosition.y - pos.y) / mMapData.mCellSize
                        mMapData.setMove(dx, dy)
                        mapDisp(mMapDataDownLoadMode)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {    //  (2)

            }
            MotionEvent.ACTION_POINTER_2_DOWN -> {  //  (261)
                if (1 < pointCount) {
                    //  マルチタッチによる拡大縮小の起点取得
                    var pos1 = PointD(event.getX(0).toDouble(), event.getY(0).toDouble())
                    var pos2 = PointD(event.getX(1).toDouble(), event.getY(1).toDouble())
                    mPreTouchDistance = pos1.distance(pos2) / mMapData.mCellSize
                    mZoomOn = true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {  //  (5)
                if (1 < pointCount) {
                    var pos1 = PointD(event.getX(0).toDouble(), event.getY(0).toDouble())
                    var pos2 = PointD(event.getX(1).toDouble(), event.getY(1).toDouble())
                    mPreTouchDistance = pos1.distance(pos2) / mMapData.mCellSize
                    mZoomOn = true
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {  //  (6)
                if (1 < pointCount) {
                    //  マルチタッチによる拡大縮小
                    var pos1 = PointD(event.getX(0).toDouble(), event.getY(0).toDouble())
                    var pos2 = PointD(event.getX(1).toDouble(), event.getY(1).toDouble())
                    var dis = pos1.distance(pos2) / mMapData.mCellSize
                    var ctr = mMapData.screen2Map(pos1.center(pos2))
                    mMapData.setZoom(((dis - mPreTouchDistance) * 2.0).toInt(), ctr)
                }
                mapDisp(mMapDataDownLoadMode)
            }
        }
        return true;
//        return super.onTouchEvent(event)
    }

    /**
     * インテント終了結果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTCODE_WIKI -> {
                //  WikiList
                mMarkList.loadMarkFile(this)    //  マークデータ再読み込み
                if (resultCode == RESULT_OK) {
                    //  座標指定移動
                    val coordinate = data?.getStringExtra("座標") //  [北緯35度28分27秒東経139度09分46秒]
                    Log.d(TAG,"onActivityResult: " + coordinate)
                    val ctr = klib.string2Coordinate(coordinate.toString())
                    if (ctr.x != 0.0 && ctr.y != 0.0) {
                        mMapData.setLocation(mMapData.coordinates2BaseMap(ctr))
                        mapDisp(mMapDataDownLoadMode)     //  座標移動
                    }
                }
            }
            REQUESTCODE_MAPINFDATA -> {
                //  地図データ情報編集画面
                Log.d(TAG, "onActivityResult: MAPINFODATA " + resultCode)
                if (resultCode == RESULT_OK) {
                    //  地図データリストの再読込
                    mMapInfoData.loadMapInfoData(mMapInfoDataPath)
                    spSetMapInfoData()
                }
            }
            REQUESTCODE_GPSTRACELIST -> {
                //  GPSトレースリスト画面
                if (resultCode == RESULT_OK) {
                    mGpsTraceList.loadListFile()
                    //  座標指定移動
                    val coordinate = data?.getStringExtra("座標") //  [139.68142458, 35.56965361]
                    Log.d(TAG,"onActivityResult:　GPSTRACELIST " + coordinate)
                    if (coordinate != null && 0 < coordinate.length) {
                        val pos = coordinate.split(',')
                        Log.d(TAG,"onActivityResult: " + pos.toString())
                        if (pos.size == 2) {
                            val ctr = PointD(pos[0].toDouble(), pos[1].toDouble())
                            if (ctr.x != 0.0 && ctr.y != 0.0) {
                                mMapData.setLocation(mMapData.coordinates2BaseMap(ctr))
                            }
                        }
                    }
                    mapDisp(mMapDataDownLoadMode)     //  再表示
                }
            }
        }
    }

    /**
     *  オプションメニューの追加
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val item0 = menu.add(Menu.NONE, MENU00, Menu.NONE, "地図情報")
        item0.setIcon(android.R.drawable.ic_menu_set_as)
        val item1 = menu.add(Menu.NONE, MENU01, Menu.NONE, "登録画面...")
        item1.setIcon(android.R.drawable.ic_menu_set_as)
        val item4 = menu.add(Menu.NONE, MENU04, Menu.NONE, "GPSトレースリスト...")
        item4.setIcon(android.R.drawable.ic_menu_set_as)
        val item6 = menu.add(Menu.NONE, MENU06, Menu.NONE, "マーク操作...")
        item6.setIcon(android.R.drawable.ic_menu_set_as)
        val item7 = menu.add(Menu.NONE, MENU07, Menu.NONE, "Wikiリスト")
        item7.setIcon(android.R.drawable.ic_menu_set_as)
        val item8 = menu.add(Menu.NONE, MENU08, Menu.NONE, "地図データ一括取込")
        item8.setIcon(android.R.drawable.ic_menu_set_as)
        val item9 = menu.add(Menu.NONE, MENU09, Menu.NONE, "オンラインの切替")
        item9.setIcon(android.R.drawable.ic_menu_set_as)
        val item2 = menu.add(Menu.NONE, MENU02, Menu.NONE, "地図データ編集")
        item2.setIcon(android.R.drawable.ic_menu_set_as)
        val item10 = menu.add(Menu.NONE, MENU10, Menu.NONE, "アプリ情報")
        item10.setIcon(android.R.drawable.ic_menu_set_as)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * オプションメニューの動的切替
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        return super.onPrepareOptionsMenu(menu)
        super.onPrepareOptionsMenu(menu)
        val item9 = menu!!.findItem(MENU09)
        if (mMapDataDownLoadMode == WebFileDownLoad.OFFLINE) {
            item9.setTitle("オンラインにする")
        } else {
            item9.setTitle("オフラインにする")
        }
        return true;
    }

    /**
     *  オプションメニューの実行
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU00 -> {     //  地図情報の表示
                var msg = "■タイトル\n" + mMapInfoData.mMapData[mMapData.mMapTitleNum][3] + "\n" +
                        "■有効ズームレベル : " + mMapInfoData.mMapData[mMapData.mMapTitleNum][4] + "\n" +
                        "■整備範囲\n" + mMapInfoData.mMapData[mMapData.mMapTitleNum][5] + "\n" +
                        "■概要\n" + mMapInfoData.mMapData[mMapData.mMapTitleNum][6]
                klib.messageDialog(this, "地図情報", msg)
            }
            MENU01 -> {     //  登録画面
                areaDataOptionMenu()
            }
            MENU02 -> {      //  地図データ編集
                setMapInfoData()
            }
            MENU04 -> {     //  GpsTraceList
                gpGpsTraceList()
            }
            MENU06 -> {     //  マーク操作サブメニュー
                markOperationMenu()
            }
            MENU07 -> {     //  Wikiリスト
                goWikiList()
            }
            MENU08 -> {     //  地図データ一括ダウンロード
                mapDataDownLoadAll()
            }
            MENU09 -> {     //  オンラインとオフラインのモード切替
                mMapDataDownLoadMode = if (mMapDataDownLoadMode == WebFileDownLoad.OFFLINE) WebFileDownLoad.NORMAL
                else WebFileDownLoad.OFFLINE
            }
            MENU10 -> {      //  アプリ情報
                dispAplInf()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * GPS Serviceの開始
     * cont     前回値を引き継ぐ
     */
    fun GpsServiceStart(cont: Boolean = false) {
        Log.d(TAG,"GpsServiceStartContinue: "+cont)
        if (!cont) {
            val intent = Intent(this, GpsService::class.java)
            startService(intent)
        }
        handler.post(runnable)
    }

    /**
     * GPS Srviceの終了
     */
    fun GpsServiceEnd(save: Boolean = true) {
        Log.d(TAG,"GpsServiceEnd")
        val intent = Intent(this, GpsService::class.java)
        stopService(intent)
        handler.removeCallbacks(runnable)
        if (save) {
            mGpsTrace.moveGpsFile(mGpsTraceFileFolder)
        }
    }

    /**
     *  位置情報(GPS除法)の初期化
     */
    fun initGps() {
        mGpsTraceFileFolder = mDataFolder + "/" + mGpsTraceFileFolder
        if (!klib.mkdir(mGpsTraceFileFolder))
            Toast.makeText(this, "GPS Data フォルダが作成できません/n" + mGpsTraceFileFolder, Toast.LENGTH_LONG).show()
        //  GpsTraceの初期設定
        mGpsTrace.init(this, mGpsTraceFileFolder, mDataFolder + "/" + mGpsFileName)

        mMapView.mGpsTrace = mGpsTrace
        //  GPSのミッションチェック
        if (klib.checkGpsPermission(this)) {
            //  Serviceの設定
            val fromNotification = intent.getBooleanExtra("fromNotification", false)
            if (fromNotification) {
                Log.d(TAG,"onCreate: fromNotification")
            }
            //  Serviceの起動状態を確認
            if (klib.isServiceRunning(this, GpsService::class.java)) {
                //  Service起動中
                mGpsLocation = true
                setGpsButton(true)
            } else {
                mGpsLocation = false
                setGpsButton(false)
            }
            mGpsEnable = true
        } else {
            //  GPS使用不可
            mGpsEnable = false
        }

        //  GPS使用の有無
        if (mGpsEnable)
            btGpsOn.isEnabled = true
        else
            btGpsOn.isEnabled = false
    }

    /**
     *  初期化処理
     */
    fun init() {
        //  画像データ保存先フォルダ
        val extStrageDir = Environment.getExternalStorageDirectory()
        mBaseFolder = extStrageDir.absolutePath + "/" + Environment.DIRECTORY_DCIM + "/gsiMap/"
        mImageFileSetPath = mBaseFolder + mImageFileSetPath
//        loadImageFileSet(mImageFileSetPath)
        mMapData.mBaseFolder = mBaseFolder
        mMapData.loadImageFileSet(mImageFileSetPath)

        //  位置画面データの読み込みと保存先フォルダ設定
        mAreaData.setSavePath(mDataFolder + "/" + mAreaDataListPath)
        mAreaData.loadAreaDataList()

        //  マークリストデータの読み込み
        mMarkList.mSaveFilePath = mDataFolder + "/" + mMarkListPath
        mMarkList.loadMarkFile(this)
        mMarkList.setGroupDispList()
        setMarkButton()

        //  MapViewサイズ仮設定
        mMapData.mView = Size(getWindowWidth(), getWindowHeight() - 406)

        //  地図タイトルのspinner設定
        spSetMapInfoData()
        spMapType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (mMapData.mMapTitleNum != position) {
                    mMapData.mMapTitleNum = position
                    mapInit()
                    mapDisp(mMapDataDownLoadMode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //  ズームレベルのspinner設定
        var zoomLevelAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mMapData.mZoomName)
        spZoomLevel.adapter = zoomLevelAdapter
        spZoomLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mMapData.mZoom != position) {
                    mMapData.setZoomUpPos(mMapData.mZoom, position)
                    mapDisp(mMapDataDownLoadMode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //  列数のspinner設定
        var colCountAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mMapData.mColCountName)
        spColCount.adapter = colCountAdapter
        spColCount.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mMapData.mColCount != position + 1) {
                    mMapData.setColCountUpPos(mMapData.mColCount, position + 1)
                    mapDisp(mMapDataDownLoadMode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //  再表示ボタン
        btRedraw.setOnClickListener {
            getParameter()
            mapDisp(WebFileDownLoad.UPDATE)
        }
        //  再表示ボタン長押し(オンラインデータ更新)
        btRedraw.setOnLongClickListener {
            getParameter()
            mapDisp(WebFileDownLoad.ALLUPDATE)
            true
        }
        //  上に移動
        btMoveUp.setOnClickListener {
            getParameter()
            mMapData.setMove(0.0, -0.5)
            mapDisp(mMapDataDownLoadMode)
        }
        //  下に移動
        btMoveDown.setOnClickListener {
            getParameter()
            mMapData.setMove(0.0, 0.5)
            mapDisp(mMapDataDownLoadMode)
        }
        //  左に移動
        btMoveLeft.setOnClickListener {
            getParameter()
            mMapData.setMove(-0.5, 0.0)
            mapDisp(mMapDataDownLoadMode)
        }
        //  右に移動
        btMoveRight.setOnClickListener {
            getParameter()
            mMapData.setMove(0.5, 0.0)
            mapDisp(mMapDataDownLoadMode)
        }
        //  拡大
        btZoomUp.setOnClickListener {
            if (mMapData.mZoom < 20) {
                mMapData.setZoom(1)
                mapDisp(mMapDataDownLoadMode)
            }
        }
        //  縮小
        btZoomDown.setOnClickListener {
            if (0 < mMapData.mZoom) {
                mMapData.setZoom(-1)
                mapDisp(mMapDataDownLoadMode)
            }
        }
        //  GPSデータ取得の開始と停止
        btGpsOn.setOnClickListener {
            mGpsLocation = !mGpsLocation
            setGpsButton()
        }

        //  マーク表示切替
        btMark.setOnClickListener {
            mMarkList.mMarkDisp = !mMarkList.mMarkDisp
            setMarkButton()
            mapDisp(mMapDataDownLoadMode)
        }

        //  時刻設定の地図データの予測時間切り替え
        btClockInc.setOnClickListener {
            var timeString = mutableListOf<String>()
            var countMax = if (30 <= mMapData.mMapInfoData.mDateTimeInterval) 16
                            else 60 / mMapData.mMapInfoData.mDateTimeInterval
            for (i in -1..countMax) {
                var addtime = i * mMapData.mMapInfoData.mDateTimeInterval
                var timeStr = if (30 <= mMapData.mMapInfoData.mDateTimeInterval) (addtime / 60).toString() + "時間後"
                                else addtime.toString() + "分後"
                timeString.add(timeStr)
            }
            klib.setMenuDialog(this, "予測時間", timeString, iTimeIncSelectOperation)
        }
    }

    //  時刻設定の地図データの予測時間設定
    var iTimeIncSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        var addtime = klib.str2Integer(s) * if (0 <= s.indexOf("時間")) 60 else 1
        mMapData.mMapInfoData.mDateTimeInc = addtime / mMapData.mMapInfoData.mDateTimeInterval
        mapInit()
        mapDisp(mMapDataDownLoadMode)
    }


    /**
     * 地図データのタイトルをSpnnerに設定
     */
    fun spSetMapInfoData() {
        var mapTitle = mutableListOf<String>()
        for (i in mMapInfoData.mMapData.indices)
            mapTitle.add(mMapInfoData.mMapData[i][0])
        var mapTitleAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mapTitle)
        spMapType.adapter = mapTitleAdapter
    }

    /**
     * GPSの状態をボタンに設定する
     * cont         継続フラグ (true:継続　false:新規設定 null:ダイヤログ確認)
     */
    fun setGpsButton(cont: Boolean? = null) {
        if (cont == null) {
            //  ダイヤログ確認あり
            if (mGpsLocation) {
                AlertDialog.Builder(this)
                    .setTitle("開始確認")
                    .setMessage("GPSトレースを記録します")
                    .setPositiveButton("開始", {
                            dialog, which ->
                        //  GPS ON
                        btGpsOn.setBackgroundColor(Color.rgb(200, 50, 100))   //  赤(on)
                        mGpsTrace.start()
                        GpsServiceStart()
                    })
                    .setNegativeButton("キャンセル", {
                            dialog, which ->
                        mGpsLocation = !mGpsLocation
                    })
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("終了確認")
                    .setMessage("GPSトレースを保存しますか?")
                    .setPositiveButton("保存なし終了",{
                            dialog, which ->
                        //  GPSデータを保存せずに終了
                        btGpsOn.setBackgroundColor(Color.rgb(100, 50, 200))   //  紫(off)
                        mGpsTrace.end()
                        GpsServiceEnd(false)
                    })
                    .setNeutralButton("保存終了", {
                            dialog, which ->
                        //  GPS OFF
                        btGpsOn.setBackgroundColor(Color.rgb(100, 50, 200))   //  紫(off)
                        mGpsTrace.end()
                        GpsServiceEnd()
                    })
                    .setNegativeButton("キャンセル", {
                            dialog, which ->
                        mGpsLocation = !mGpsLocation
                    })
                    .show()
            }
        } else {
            //  ダイヤログ確認なし
            if (mGpsLocation) {
                //  GPS ON
                btGpsOn.setBackgroundColor(Color.rgb(200, 50, 100))   //  赤(on)
                mGpsTrace.start(cont)
                GpsServiceStart()
            } else {
                //  GPS OFF
                btGpsOn.setBackgroundColor(Color.rgb(100, 50, 200))   //  紫(off)
                mGpsTrace.end()
                GpsServiceEnd()
            }
        }
    }

    /**
     * Mark表示の状態をボタンに設定
     *  fileUpdate      Webファイルの更新方法
     */
    fun setMarkButton() {
        if (mMarkList.mMarkDisp) {
            //  マーク表示
            btMark.setBackgroundColor(Color.rgb(200, 50, 100))   //  赤
        } else {
            //  マーク非表示
            btMark.setBackgroundColor(Color.rgb(100, 50, 200))   //  紫
        }
    }

    /**
     * 地図表示前の設定(初期化)
     */
    fun mapInit() {
        mMapData.normarized()
        mMapData.setDateTime()
        mMapView.mDispDateTime = mMapData.mMapInfoData.mDispDateTime
        if (mMapData.mMapInfoData.isDateTimeData())
            btClockInc.visibility = View.VISIBLE
        else
            btClockInc.visibility = View.INVISIBLE
        title = mAppTitle + " [" + mMapInfoData.getMapDataTitle() + "]"
    }

    /**
     *  地図を表示する
     *  fileUpdate      Webファイルの更新方法
     */
    fun mapDisp(fileUpdate: WebFileDownLoad) {
        //  地図イメージの表示
        mapDataSet(fileUpdate)
        mMapView.reDraw()
        setParameter()
    }

    /**
     *  地図データの取り込み
     *  fileUpdate      Webファイルの更新方法
     */
    fun mapDataSet(fileUpdate: WebFileDownLoad) {
        if (mMapData.mView.width <= 0 || mMapData.mView.height <= 0 || mMapData.mColCount <= 0)
            return
        //  パラメータをMapViewに設定
        setViewParameter()
        //  表示セルの初期化
        mMapView.setCellBoard()
        //  画面に合った行数の取得
        mMapData.mRowCount = mMapView.mRowCount
        //  Webから地図イメージデータを取得
        webFileLoad(mMapData, fileUpdate)
        mMapView.mElevator = getMapElevator(mMapData, mMapData.getMapCenter())

        //  中心のカラーデータ
        var rgb = getMapPixelColor(mMapData, mMapData.getMapCenter())
        mMapView.mCenterColor = rgb.uppercase()
        mMapView.mComment = ""
        if (0 < mMapData.mColorLegend.size) {
            //  色凡例
            var data = mMapData.getColorLegend(rgb)
            if (0 < data.length)
                mMapView.mComment = data
        }
    }

    /**
     *  MapViewにパラメータを設定
     */
    fun setViewParameter() {
        mMapView.mColCount = mMapData.mColCount
        mMapView.mOffset = mMapData.getStartOffset()
    }

    /**
     *  コントロールからパラメータを取得
     */
    fun getParameter() {
        mMapData.mMapTitleNum = spMapType.selectedItemPosition
        mMapData.mZoom = spZoomLevel.selectedItemPosition
        mMapData.mColCount = spColCount.selectedItemPosition + 1
        mMapData.normarized()
    }

    /**
     *  コントロールにパラメータを設定
     */
    fun setParameter() {
        spMapType.setSelection(mMapData.mMapTitleNum)
        spZoomLevel.setSelection(mMapData.mZoom)
        spColCount.setSelection(mMapData.mColCount - 1)
    }

    /**
     * アプリ情報の表示
     */
    fun dispAplInf() {
        //  地図ファイルデータの数とサイズを取得
        var fileList = klib.getFileList(mBaseFolder, true)
        var filesSize = 0L
        for (file in fileList) {
            filesSize += file.length()
        }
        var mes = ""
        mes += "地図ファイル数　　: " + "%,d".format(fileList.count()) + "\n"
        mes += "ファイルサイズ合計: " + "%,d".format(filesSize)
        klib.messageDialog(this, "アプリ情報", mes)
    }

    /**
     *  Webファィルのダウンロード
     *  mapData     地図状態データ
     *  fileUpdate  地図データダウンロード状態(NORMAL: 地図データなし、登録なしの時にダウンロード
     *                                    ALLUPDATE: 地図データなしでダウンロード
     *                                    UPDATE: 常にダウンロード
     *                                    OFFLINE: ファイルの有無にかかわらずダウンロードなし
     */
    fun webFileLoad(mapData: MapData, fileUdate: WebFileDownLoad) {
        mElevatorDataNo = mapData.mElevatorDataNo
        val eleZoomMax = mapData.mMapInfoData.getElevatorMaxZoom(mElevatorDataNo)
        for (i in mapData.mStart.x.toInt()..mapData.mStart.x.toInt()+mapData.mColCount) {
            for (j in mapData.mStart.y.toInt()..mapData.mStart.y.toInt()+mapData.mRowCount) {
                if (i <= Math.pow(2.0, mapData.mZoom.toDouble()) &&
                        j <= Math.pow(2.0, mapData.mZoom.toDouble())) {
                    //  標高データのダウンロード
                    mMapData.getElevatorDataFile(i, j, fileUdate)
                    //  地図ファイルのダウンロード
                    var downLoadFile = mMapData.getMapData(i, j, fileUdate)
                    if (klib.existsFile((downLoadFile)))
                        mMapView.setCellImage(i - mapData.mStart.x.toInt(), j - mapData.mStart.y.toInt(), downLoadFile)
                    else
                        mMapView.setCellImage(i - mapData.mStart.x.toInt(), j - mapData.mStart.y.toInt(), "")   //  ダミー
                }
            }
        }
    }

    /**
     * 表示されている領域でそれ以上のズームレベルの地図データを一括でダウンロードする
     * ズームレベルが14以下の場合は時間がかかるので行わない
     */
    fun mapDataDownLoadAll(){
        if (mMapData.mZoom < 10) {
            klib.messageDialog(this, "注意", "ズームレベル１０以上で実行してください")
            return
        }
        var mapData = mMapData.copyTo()
        for (zoom in (mMapData.mZoom + 1)..(Math.min(18, mMapData.mZoom + 3))) {
            Toast.makeText(this, "ズームレベル "+zoom+" をダウンロード中", Toast.LENGTH_LONG).show()
            var ctr = mapData.getCenter()
            mapData.mColCount *= 2
            mapData.setZoom(1, mapData.baseMap2Map(ctr))
            mapDataDownLoad(mapData)
        }
        Toast.makeText(this, "ダウンロード完了", Toast.LENGTH_LONG).show()
    }

    /**
     * MapDataで示された領域の地図デーをダウンロードする(一括ダウンロード用)
     * mapData      MapDataを使用した領域データ
     */
    fun mapDataDownLoad(mapData: MapData) {
        for (i in mapData.mStart.x.toInt()..mapData.mStart.x.toInt()+mapData.mColCount) {
            for (j in mapData.mStart.y.toInt()..mapData.mStart.y.toInt() + mapData.mRowCount) {
                if (i <= Math.pow(2.0, mapData.mZoom.toDouble()) &&
                    j <= Math.pow(2.0, mapData.mZoom.toDouble())) {
                    mapFileDownLoad(mapData.mMapUrl, mapData.mMapTitle, mapData.mZoom, i, j, mapData.mExt, WebFileDownLoad.NORMAL)
                }
            }
        }
    }

    /**
     * Web上の地図データをタイルを指定してダウンロードする
     * {z}{x}{y}を数値に入れ換えてURLとダウンロードファイル名を作成
     * mapOrgUrl    地図データのWebアドレス
     * mapTitle     地図データのID名
     * zoom         ズームレベル
     * x            タイルのX位置
     * y            タイルのY位置
     * ext          地図データの拡張子
     * fileUpdate   ダウンロードモード(NORMAL/UPDATE/ALLUPDATE/OFFLINE
     * return       ダウンロードファイル(File)
     */
    fun mapFileDownLoad(mapOrgUrl: String, mapTitle: String, zoom: Int, x: Int, y: Int, ext: String, fileUdate: WebFileDownLoad): File {
        //  保存ファイル名
        var dataUrl = mapTitle + "/" + zoom.toString() + "/" + x + "/" + y + "." + ext
        //  ダウンロード先URL
        var mapUrl = mapOrgUrl.replace("{z}", zoom.toString())
        mapUrl = mapUrl.replace("{x}", x.toString())
        mapUrl = mapUrl.replace("{y}", y.toString())
        return mapFileDownLoad(mapUrl, dataUrl, fileUdate)
    }

    /**
     * URLとダウンロードファイル名からダウンロードパスを作成し、ファイルをダウンロードする
     * mapUrl           データのURL
     * dataUrl          データのファイル名
     * fileUpdate       ファイルの更新条件
     * return           Fileデータ
     */
    fun mapFileDownLoad(mapUrl: String, dataUrl: String, fileUpdate: WebFileDownLoad): File {
        //  保存フォルダ
        var downLoadFile = File(mBaseFolder + dataUrl)
        if ((fileUpdate == WebFileDownLoad.ALLUPDATE || !downLoadFile.exists()) && fileUpdate != WebFileDownLoad.OFFLINE) {
            if (fileUpdate != WebFileDownLoad.NORMAL || !mImageFileSet.contains(dataUrl)) {
                var downLoad = DownLoadWebFile(mapUrl, downLoadFile.path)
                downLoad.start()
                while (downLoad.isAlive()) {
                    Thread.sleep(100L)
                }
                mImageFileSet.add(dataUrl)  //  ダウンロードしたファイルをリスト登録
            }
        }
        return downLoadFile
    }

    /**
     * Pixelの色を取得しRGBの文字列で返す
     * mapData      地図情報
     * mp           位置座標(Map座標)
     * return       色文字列(rrggbb)
     */
    fun getMapPixelColor(mapData: MapData, mp: PointD): String {
        var zoom = mapData.mZoom
        var cmp = mp;
        var x = (256.0 * (cmp.x % 1.0)).toInt()
        var y = (256.0 * (cmp.y % 1.0)).toInt()
        var rgb = mMapView.getCellImage(cmp.x.toInt() - mapData.mStart.x.toInt(),
            cmp.y.toInt() - mapData.mStart.y.toInt())?.getPixel(x, y)
        if (rgb != null) {
            var r = rgb.shr(16).and(0xff)
            var g = rgb.shr(8).and(0xff)
            var b = rgb.shr(0).and(0xff)
            return "%02x%02x%02x".format(r,g,b)
        }
        return ""
    }

    /**
     * 標高データの取得
     * mapData      地図情報
     * mp           位置座標(Map座標)
     * return       標高(m)
     */
    fun getMapElevator(mapData: MapData, mp: PointD):Double {
        var zoom = mapData.mZoom
        var cmp = mp;
        var eleZoomMax = mapData.mMapInfoData.getElevatorMaxZoom(mElevatorDataNo)
        if (eleZoomMax < mapData.mZoom) {
            //  標高データはズームレベル15(DEM5)までなのでそれ以上は15のデータを取得
            cmp = mapData.cnvMapPositionZoom(eleZoomMax, mp);
            zoom = eleZoomMax
        }
        var downloadPath = mapFileDownLoad(mapData.mMapInfoData.mMapElevatorData[mElevatorDataNo][7],
            mapData.mMapInfoData.mMapElevatorData[mElevatorDataNo][1], zoom, cmp.x.toInt(), cmp.y.toInt(),
            mapData.mMapInfoData.mMapElevatorData[mElevatorDataNo][2], WebFileDownLoad.NORMAL)
        return  getMapElevatorFile(downloadPath.path, (256.0 * (cmp.x % 1.0)).toInt(), (256.0 * (cmp.y % 1.0)).toInt())
    }

    /**
     * 地図データの標高ファイルから標高値を取得
     * 標高ファイルにはタイル画像のピクセルと同じく,256x256の配列で標高値が入っている
     * データがない部分は'e'が記載されている
     * path         地図データの標高ファイルののパス
     * x            X位置(256x256の配列のX位置)
     * y            y位置(256x256の配列のY位置)
     * return       標高値(m)
     */
    fun getMapElevatorFile(path: String, x: Int, y: Int): Double {
        var eleList = listOf<List<String>>()
        if (mElevatorDataList.containsKey(path)) {
            //  一度読み込んだデータはMapに登録されているのでそこから取得
            eleList = mElevatorDataList.getValue(path)
        } else {
            //  初めての読み込むデータはMapに登録する
            eleList = klib.loadCsvData(path)
            mElevatorDataList.put(path, eleList)
        }
        if (0 < eleList.size && y <= eleList.size)
            return if (eleList[y][x].indexOf("e") < 0) eleList[y][x].toDouble() else 0.0
        else
            return 0.0
    }

    /**
     *  ダウンロードした画像ファイルリストの保存
     *  データのない画像名も登録する
     *  path        保存ファイルパス
     */
    fun saveImageFileSet(path: String) {
        if (mImageFileSet.count() == 0)
            return
        var dataList = mutableListOf<String>()
        for (data in mImageFileSet) {
            dataList.add(data)
        }
        klib.saveTextData(path, dataList)
    }

    /**
     *  ダウンロードした画像ファイルリストの取り込み
     *  path        ファイルパス
     */
    fun loadImageFileSet(path: String) {
        if (klib.existsFile(path)) {
            var dataList = klib.loadTextData(path)
            mImageFileSet.clear()
            for (data in dataList) {
                mImageFileSet.add(data)
            }
        }
    }

    /**
     *  ステータスバーの高さ
     */
    fun getStatusBarHeight(activity: Activity): Int {
        val rect = Rect()
        val window = activity.window
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
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

    /**
     *  長押しのコンテキストメニュー処理
     */
    var iLongTouchMenu = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        val n = mLongTouchMenu.indexOf(s)
        when (n) {
            0 -> {                      //  マーク位置への移動
                gotoMark()
            }
            1 -> {                      //  指定値のマークの登録
                mSelectMark = -1        //  新規データ
                var bp = mMapData.screen2BaseMap(mPreTouchPosition)
                newMark(bp)
            }
            2 -> {                      //  指定位置近傍のマークの編集
                editMark(mPreTouchPosition)
            }
            3 -> {                      //  指定位置近傍のマークの林間参照
                referenceMark(mPreTouchPosition)
            }
            4 -> {                      //  指定位置近傍のマークの削除
                removeMark(mPreTouchPosition)
            }
            5 -> {                      //
                var coord = mMapData.baseMap2Coordinates(mMapData.getCenter())
                var coordString = "北緯" + "%.8f".format(coord.y) + "度東経" + "%.8f".format(coord.x) + "度 20km以内"
                goWikiList(coordString)
            }
            6 -> {                      //  距離測定の開始・終了
                if (mLongTouchMenu[n].compareTo("距離測定開始") == 0) {
                    mLongTouchMenu[n] = "距離測定終了"
                    mLongTouchMenu.add("測定点を一つ戻す")
                    mMeasure.start()
                } else if (mLongTouchMenu[n].compareTo("距離測定終了") == 0) {
                    mLongTouchMenu[n] = "距離測定開始"
                    mLongTouchMenu.removeAt(n + 1)
                    var dis = mMeasure.measure(mMapData)
                    klib.messageDialog(this, "測定距離", "%.3f km".format(dis))
                }
            }
            7 -> {                      //  距離測定で「一つ戻る」
                mMeasure.decriment()
                mapDisp(mMapDataDownLoadMode)
            }
        }
    }

    /**
     * 地図データ情報の設定
     */
    fun setMapInfoData() {
        val intent = Intent(this, MapInfoDataActivity::class.java)
        startActivityForResult(intent, REQUESTCODE_MAPINFDATA)
    }

    /**
     * 画面登録メニュー
     */
    fun areaDataOptionMenu() {
        klib.setMenuDialog(this, "画面登録メニュー", mMapDispMenu, iAreaMapOperation)
    }

    //  登録画面処理
    var iAreaMapOperation = Consumer<String> { s ->
        if (s.compareTo(mMapDispMenu[0]) == 0) {
            //  登録画面読出し
            if (0 < mAreaData.mAreaDataList.size) {
                var areaDataList = mutableListOf<String>()
                for (title in mAreaData.mAreaDataList.keys)
                    areaDataList.add(title)
                klib.setMenuDialog(this, "登録画面の選択", areaDataList, iPostionSelectOperation)
            }
        } else if (s.compareTo(mMapDispMenu[1]) == 0) {
            //  画面の登録
            klib.setInputDialog(this, "画面登録", "", iInputOperation)
        } else if (s.compareTo(mMapDispMenu[2]) == 0) {
            //  登録画面の削除
            if (0 < mAreaData.mAreaDataList.size) {
                var areaDataList = mutableListOf<String>()
                for (title in mAreaData.mAreaDataList.keys)
                    areaDataList.add(title)
                klib.setMenuDialog(this, "登録画面の削除", areaDataList, iRemoveOperation)
            }
        }
    }

    /**
     *  画面位置の選択表示
     */
    var iPostionSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        val parameter = mAreaData.mAreaDataList[s]
        if (parameter != null) {
            mMapData.setStringData(parameter)
            mapInit()
            mapDisp(mMapDataDownLoadMode)
        }
    }

    /**
     *  画面位置の登録
     */
    var iInputOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        mAreaData.mAreaDataList.put(s, mMapData.getStringData())
    }

    /**
     *  画面位置の削除
     */
    var iRemoveOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        mAreaData.mAreaDataList.remove(s)
    }
    /**
     * マーク操作のメニュー表示
     */
    fun markOperationMenu() {
        klib.setMenuDialog(this, "マーク操作", mMarkMenu, iMarkOperation)
    }

    /**
     *  マーク操作のメニュー選択実行
     */
    var iMarkOperation = Consumer<String> { s ->
        if (s.compareTo(mMarkMenu[0]) ==0) {
            //  中心位置をマーク登録
            var bp = mMapData.screen2BaseMap(mMapView.getCenter())  //  中心座標
            newMark(bp)
        } else if (s.compareTo(mMarkMenu[1]) == 0) {
            //  編集
            selectEditMark()
        } else if (s.compareTo(mMarkMenu[2]) == 0) {
            //  マーク表示切替
            mMarkList.mMarkDisp = !mMarkList.mMarkDisp
            setMarkButton()
            mapDisp(mMapDataDownLoadMode)
        } else if (s.compareTo(mMarkMenu[3]) == 0) {
            //  グループ表示切替
            markGroupDisp()
        } else if (s.compareTo(mMarkMenu[4]) == 0) {
            //  マークリストのソート設定
            markSort()
        } else if (s.compareTo(mMarkMenu[5]) == 0) {
            //  マークリストのインポート
            markImport()
        } else if (s.compareTo(mMarkMenu[6]) == 0) {
            //  マークリストのエクスポート
            markExport()
        } else if (s.compareTo(mMarkMenu[7]) == 0) {
            //  マークサイズ
            markSize()
        }
    }

    /**
     * 新規にダイヤログを出してマークデータを追加する
     * Wikiリストでコピーしたデータがあればそれを表示は手登録
     */
    fun newMark(bp: PointD = PointD()) {
        mSelectMark = -1                //  新規データ
        var buffer = klib.getTextClipBoard(this)
        var mark = MarkData()
        mark.mLocation = bp
        if (0 < buffer.length) {
            //  クリップボードのデータを設定
            var datas = buffer.split('\n')
            for (data in datas) {
                if (0 <= data.indexOf("タイトル: ")) {
                    mark.mTitle = data.substring(data.indexOf(' ') + 1)
                } else if (0 <= data.indexOf("コメント: ")) {
                    mark.mComment = data.substring(data.indexOf(' ') + 1)
                } else if (0 <= data.indexOf("座標: ")) {
                    var cp = klib.string2Coordinate(data.substring(data.indexOf(' ') + 1))
                    mark.mLocation = mMapData.coordinates2BaseMap(cp)
                } else if (0 <= data.indexOf("URL: ")) {
                    mark.mLink = data.substring(data.indexOf(' ') + 1)
                }
            }
            klib.setTextClipBoard(this, "")
        }
        var buf = mark.getDataString()
        mMarkList.setMarkInputDialog(this, "マークの編集", buf, mMarkList.iMarkSetOperation)
    }

    /**
     *  中心位置をマークの追加
     */
    fun addMark() {
        mSelectMark = -1                //  新規データ
        mPreTouchPosition = mMapView.getCenter()
        mMarkList.setMarkInputDialog(this, "マーク登録", "", mMarkList.iMarkSetOperation)
    }

    /**
     *  指定位置近傍のマークデータの編集
     *  sp      マウス位置(スクリーン座標)
     */
    fun editMark(sp: PointD) {
        mSelectMark = mMarkList.getMarkNum(sp, mMapData)
        if (0 <= mSelectMark) {
            var buf = mMarkList.mMarkList[mSelectMark].getDataString()
            mMarkList.setMarkInputDialog(this, "マークの編集", buf, mMarkList.mMarkList[mSelectMark].iMarkEditOperation)
        }
    }

    /**
     * 指定位置近傍のマークデータのリンクを表示
     *  sp      マウス位置(スクリーン座標)
     */
    fun referenceMark(sp: PointD) {
        mSelectMark = mMarkList.getMarkNum(sp, mMapData)
        if (0 <= mSelectMark) {
            if (0 < mMarkList.mMarkList[mSelectMark].mLink.length) {
                if (0 <= mMarkList.mMarkList[mSelectMark].mLink.indexOf("http"))
                    klib.webDisp(this, mMarkList.mMarkList[mSelectMark].mLink)
                else
                    klib.executeFile(this, mMarkList.mMarkList[mSelectMark].mLink)
            }
        }
    }

    /**
     *  指定位置近傍のマークデータ削除
     *  sp      マウス位置(スクリーン座標)
     */
    fun removeMark(sp: PointD) {
        mSelectMark = mMarkList.getMarkNum(sp, mMapData)
        if (0 <= mSelectMark) {
            mMarkList.mMarkList.removeAt(mSelectMark)
            mMarkList.setGroupDispList()
            mapDisp(mMapDataDownLoadMode)
        }
    }

    /**
     *  マークリストのマークデータの編集1
     */
    fun selectEditMark() {
        klib.setMenuDialog(this, "マークの編集", mMarkList.getGroupList("すべて"), iMarkGroupSelectEditOperation)
    }

    //  マークリストのマークデータの編集2 (グループリストの選択でタイトルの選択実行の関数インターフェース)
    var iMarkGroupSelectEditOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            val group = if (s.compareTo("すべて")==0) "" else s
            mMarkList.mCenter = klib.baseMap2Coordinates(mMapData.getCenter())
            klib.setMenuDialog(this, "マークの編集", mMarkList.getTitleList(group), iMarkTitleSelectEditOperation)
        }
    }

    /**
     *  マークリストのマークデータの編集3 (タイトルストの選択で編集ダイヤログ実行の関数インターフェース)
     */
    var iMarkTitleSelectEditOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        mSelectMark = mMarkList.getMarkNum(s, mMarkList.mPreSelectGroup)
        if (0 <= mSelectMark) {
            Log.d(TAG, "iMarkTitleSelectEditOperation: "+mSelectMark.toString()+" "+mMarkList.mMarkList[mSelectMark].mTitle)
            var buf = mMarkList.mMarkList[mSelectMark].getDataString()
            mMarkList.setMarkInputDialog(this, "マークの編集", buf, mMarkList.mMarkList[mSelectMark].iMarkEditOperation)
        }
    }

    /**
     *  マークリスト選択による画面移動1
     */
    fun gotoMark() {
        klib.setMenuDialog(this, "マーク位置に移動", mMarkList.getGroupList("すべて"), iMarkGroupSelectOperation)
    }

    /**
     *  マークリスト選択による画面移動2 グループリストの選択でタイトルの選択実行
     */
    var iMarkGroupSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            val group = if (s.compareTo("すべて")==0) "" else s
            mMarkList.mCenter = klib.baseMap2Coordinates(mMapData.getCenter())
            klib.setMenuDialog(this, "マーク位置に移動", mMarkList.getTitleList(group), iMarkTitleSelectOperation)
        }
    }

    /**
     *  マークリスト選択による画面移動3 タイトル選択で画面の移動
     */
    var iMarkTitleSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        val mark = mMarkList.getMark(s, mMarkList.mPreSelectGroup)
        if (0 < mark.mTitle.length) {
            mMapData.setLocation(mark.mLocation)
            mapDisp(mMapDataDownLoadMode)
        }
    }

    /**
     *  マークリストのマークデータの削除1
     */
    fun selectRemoveMark() {
        klib.setMenuDialog(this, "マークの削除", mMarkList.getGroupList("すべて"), iMarkGroupSelectRemoveOperation)
    }

    /**
     *  マークリストのマークデータの削除2
     */
    var iMarkGroupSelectRemoveOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            val group = if (s.compareTo("すべて")==0) "" else s
            mMarkList.mCenter = klib.baseMap2Coordinates(mMapData.getCenter())
            klib.setMenuDialog(this, "マークの削除", mMarkList.getTitleList(group), iMarkTitleSelectRemoveOperation)
        }
    }

    /**
     *  マークリストのマークデータの削除3
     */
    var iMarkTitleSelectRemoveOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        mSelectMark = mMarkList.getMarkNum(s, mMarkList.mPreSelectGroup)
        if (0 <= mSelectMark) {
            mMarkList.mMarkList.removeAt(mSelectMark)
            mMarkList.setGroupDispList()
            mapDisp(mMapDataDownLoadMode)
        }
    }

    /**
     * マークをグループ単位で表示/非表示を設定
     */
    fun markGroupDisp() {
        var groupList = mMarkList.getMarkGroupArray()
        var chkList = mMarkList.getMarkGroupBooleanArray()
        klib.setChkMenuDialog(this, "表示設定", groupList, chkList, iMarkGroupDispOperation)
    }

    /**
     * マークをグループ単位で表示/非表示を設定の関数インターフェース
     */
    var iMarkGroupDispOperation = Consumer<BooleanArray> { s ->
        var groupList = mMarkList.getMarkGroupArray()
        for (i in 0..s.size - 1) {
              Log.d(TAG, "iMarkGroupDispOperation: " + i + " " + s[i])
              mMarkList.mGroupDispList[groupList[i]] = s[i]
        }
        mapDisp(mMapDataDownLoadMode)
    }


    /**
     * マークソート設定ダイヤログ
     */
    fun markSort(){
        klib.setMenuDialog(this, "ソート設定", mMarkList.mSortName, iListSort)
    }

    /**
     * マークソート設定関数インタフェース
     */
    var iListSort = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            mMarkList.mListSort = when (s) {
                "ソートなし" -> MarkList.SORTTYPE.Non
                "昇順" -> MarkList.SORTTYPE.Normal
                "降順" -> MarkList.SORTTYPE.Reverse
                "距離順" -> MarkList.SORTTYPE.Distance
                else -> MarkList.SORTTYPE.Non
            }
        }
    }

    /**
     * マークリストデータのインポート
     * ファイル一覧第ログを表示して行う
     */
    fun markImport() {
//        klib.fileSelectDialog(this, klib.getPackageNameDirectory(this), "*.csv", true, iFilePath)
        klib.fileSelectDialog(this, mDataFolder, "*.csv", true, iFilePath)
    }

    /**
     * マークリストデータのインポートの関数インターフェース
     */
    var  iFilePath = Consumer<String>() { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            mMarkList.loadMarkFile(s, true)
        }
    }

    /**
     * マークリストデータのエクスポート
     * ファイル保存ダイヤログを表示して行う
     */
    fun markExport() {
//        klib.saveFileSelectDialog(this, klib.getPackageNameDirectory(this), "*.csv", true, iDirPath)
        klib.saveFileSelectDialog(this, mDataFolder, "*.csv", true, iDirPath)
    }

    /**
     * マークリストデータのエクスポートの関数インターフェース
     */
    var  iDirPath = Consumer<String>() { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            mMarkList.saveMarkFile(s)
        }
    }

    /**
     * マークシンボルのサイズを設定
     */
    fun markSize() {
        klib.setMenuDialog(this, "マークの大きさ(" + "%.1f".format(mMarkList.mMarkScale) + ")", mMarkSizeMenu, iMarkSizeOperation)
    }

    /**
     * マークシンボルのサイズを設定
     */
    var iMarkSizeOperation = Consumer<String> { s ->
        mMarkList.mMarkScale = s.toFloat()
        mapDisp(mMapDataDownLoadMode)
    }

    /**
     * Wikiリストの起動
     * coordstr     検索座標文字列
     */
    fun goWikiList(coordStr: String = "") {
        mMarkList.saveMarkFile(this)
        val intent = Intent(this, WikiList::class.java)
        intent.putExtra("COORDINATE", coordStr)
        startActivityForResult(intent, REQUESTCODE_WIKI)
    }


    /**
     * GPSトレースリスト
     */
    fun gpGpsTraceList() {
        val intent = Intent(this, GpsTraceListActivity::class.java)
        intent.putExtra("GPSTRACEFOLDER", mGpsTraceFileFolder)
        intent.putExtra("GPSTRACELISTPATH", mGpsTraceListPath)
        startActivityForResult(intent, REQUESTCODE_GPSTRACELIST)
    }
}