package jp.co.yoshida.katsushige.mapapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityGpxEditBinding
import jp.co.yoshida.katsushige.mylib.KLib

/**
 * 他のアプリで作成されたGPXファイルを登録・編集する
 * 1.新規に開いた時はGPXファイルを選択して登録
 * 2.既存データの登録項目の変種
 * 3.共有で外部アプリからの呼び出しに対応
 */
class GpxEditActivity : AppCompatActivity() {
    val TAG = "GpxEditActivity"

    lateinit var binding: ActivityGpxEditBinding
    lateinit var constraintLayout: ConstraintLayout
    lateinit var edTitle: EditText
    lateinit var edGroup: EditText
    lateinit var edGpxPath: EditText
    lateinit var edComment: EditText
    lateinit var tvYear: TextView
    lateinit var tvGpxInfo: TextView
    lateinit var btGruopRef: Button
    lateinit var btGpxPathRef: Button
    lateinit var btGraph: Button
    lateinit var btOK: Button
    lateinit var btCancel: Button
    lateinit var spColor: Spinner
    lateinit var spCategory: Spinner

    var mGpxDataList = GpsDataList()                    //  GPXファイルリスト
    var mGpxDataListPath = ""                           //  GPXファイルリストパス
    var mGpxFilePath = ""                               //  GPXファイルパス
    var mGpxFilePos = -1                                //  選択されたGPXファイル位置
    val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpx_edit)
        this.title = "GPXファイル登録"

        mGpxDataListPath = klib.getStrPreferences("GpxDataListPath", this).toString()
        mGpxDataList.mSaveFilePath = mGpxDataListPath
        mGpxDataList.loadDataFile(this)

        initControl()

        val intent = getIntent()
        val action = intent.getAction();
        if(Intent.ACTION_VIEW.equals(action)){
            //  外部アプリから共有で起動された場合(共有 FileProvider使用)
            val type = intent.type      //  mime type [application/gpx]
            val data = intent.data      //  URI
            if (data != null){
                Log.d(TAG, "onCreate: "+data.toString()+" ["+data.path+"] "+type.toString()+" "+action.toString())
                mGpxFilePath = getPath(data)
            }
        }else{
            //  内部からの呼び出し
            mGpxFilePath = intent.getStringExtra("GPXFILEPATH").toString()
        }
        Log.d(TAG, "onCreate: "+mGpxFilePath+" "+action.toString())

        setDataGpxFile(mGpxFilePath)
    }

    /**
     * URIからパスを取得する
     * uri      ファイルのURI
     * return   フルパス
     */
    fun getPath(uri: Uri): String {
        val uriPath = uri.path.toString()
        var path = klib.getInternalStrage() + uriPath.substring(klib.indexOf(uriPath, "/",1))
        if (!klib.existsFile(path))
            path = klib.getExternalStrage(this) + uriPath.substring(klib.indexOf(uriPath, "/",1))
        return path
    }

    /**
     * コントロールの初期化
     */
    fun initControl() {
        binding = ActivityGpxEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.gpxConstraintLayout
        edTitle = binding.editTextTextPersonName13
        edGroup = binding.editTextTextPersonName14
        edGpxPath = binding.editTextTextPersonName15
        edComment = binding.editTextTextPersonName16
        tvGpxInfo = binding.textView31
        tvYear = binding.textView32
        btGruopRef = binding.button7
        btGpxPathRef = binding.button8
        btGraph = binding.button15
        btOK = binding.button9
        btCancel = binding.button10
        spColor = binding.spinner2
        spCategory = binding.spinner3

        edTitle.setText("")
        edGroup.setText("")
        edGpxPath.setText("")
        edComment.setText("")
        tvGpxInfo.setText("")

        //  線分の色のSpinner
        var colorAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mGpxDataList.mColorMenu)
        spColor.adapter = colorAdapter

        //  分類のSpinner
        var categoryAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mGpxDataList.mCategoryMenu)
        spCategory.adapter = categoryAdapter

        //  グループ設定
        btGruopRef.setOnClickListener {
            var groupList = mGpxDataList.getGroupList("")
            klib.setMenuDialog(this, "グループ", groupList, iGpxGroup)
        }

        //  GPXファイルをパスの選択と設定
        btGpxPathRef.setOnClickListener {
            var gpxFileFolder = klib.getStrPreferences("GpxFileFolder", this)
            if (gpxFileFolder == null || !klib.isDirectory(gpxFileFolder))
                gpxFileFolder = klib.getPackageNameDirectory(this)
            //  ファイル選択
            klib.fileSelectDialog(this, gpxFileFolder, "*.gpx", true, iGpxFilePath)
        }

        //  GPXファイルをグラフ表示する
        btGraph.setOnClickListener {
            val gpxPath = edGpxPath.text.toString()
            val title = edTitle.text.toString()
            goGpsGraph(gpxPath, title)
        }

        //  登録処理
        btOK.setOnClickListener {
            val gpsFileData = GpsFileData()
            gpsFileData.getFileData(edGpxPath.text.toString())
            gpsFileData.mTitle = edTitle.text.toString()
            gpsFileData.mGroup = edGroup.text.toString()
            gpsFileData.mCategory = mGpxDataList.mCategoryMenu[spCategory.selectedItemPosition]
            gpsFileData.mLineColor = mGpxDataList.mColorMenu[spColor.selectedItemPosition]
            gpsFileData.mComment = edComment.text.toString()
            if (0 <= mGpxFilePos) {
                mGpxDataList.mDataList[mGpxFilePos] = gpsFileData
            } else {
                mGpxDataList.mDataList.add(gpsFileData)
            }
            mGpxDataList.saveDataFile(this)

            setResult(RESULT_OK)
            finish()
        }

        //  キャンセル
        btCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    //  グループ名をコントロールに設定する関数インターフェース
    var iGpxGroup = Consumer<String> { s ->
        edGroup.setText(s)
    }

    //  GPXファイルパスをコントロールに設定する関数インターフェース
    var  iGpxFilePath = Consumer<String>() { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 < s.length) {
            edGpxPath.setText(s)
            //  選択ファイルのフォルダを保存
            klib.setStrPreferences(klib.getFolder(s), "GpxFileFolder", this)
            if (edTitle.text.length == 0)
                edTitle.setText(klib.getFileNameWithoutExtension(s))
            //  GPSデータの取得と登録
            setGpxFileInfo(s)
        }
    }

    /**
     * GPXファイルのデータをコントロールに7設定する
     * gpxFilePath      GPXファイルのパス名
     */
    fun setDataGpxFile(gpxFilePath: String) {
        mGpxFilePos = mGpxDataList.findGpxFile(gpxFilePath)
        if (0 <= mGpxFilePos) {
            Log.d(TAG, "setDataGpxFile: "+mGpxFilePos+" "+gpxFilePath)
            edTitle.setText(mGpxDataList.mDataList[mGpxFilePos].mTitle)
            edGroup.setText(mGpxDataList.mDataList[mGpxFilePos].mGroup)
            edGpxPath.setText(mGpxDataList.mDataList[mGpxFilePos].mFilePath)
            edComment.setText(mGpxDataList.mDataList[mGpxFilePos].mComment)
            setGpxFileInfo(mGpxDataList.mDataList[mGpxFilePos].mFilePath)
            spColor.setSelection(mGpxDataList.mColorMenu.indexOf(mGpxDataList.mDataList[mGpxFilePos].mLineColor))
            spCategory.setSelection(mGpxDataList.mCategoryMenu.indexOf(mGpxDataList.mDataList[mGpxFilePos].mCategory))
        } else {
            edGpxPath.setText(gpxFilePath)
            setGpxFileInfo(gpxFilePath)
        }
    }

    /**
     * GPXファイルからGPX情報と測定年をコントロールに登録
     */
    fun setGpxFileInfo(gpxFilePath: String) {
        val gpsFileData = GpsFileData()
        gpsFileData.getFileData(gpxFilePath)
        tvGpxInfo.setText(gpsFileData.getInfoData())
        tvYear.setText(klib.date2String( gpsFileData.mFirstTime, "yyyy年"))
        if(spCategory.selectedItemPosition < 0) {
            var v = gpsFileData.getSpeed()
            if (20 < v)
                spCategory.setSelection(mGpxDataList.mCategoryMenu.indexOf("車"))
            else if (12 < v)
                spCategory.setSelection(mGpxDataList.mCategoryMenu.indexOf("自転車"))
            else if (6 < v)
                spCategory.setSelection(mGpxDataList.mCategoryMenu.indexOf("ジョギング"))
            else
                spCategory.setSelection(mGpxDataList.mCategoryMenu.indexOf("散歩"))
        }
    }


    /**
     * GPXファイルのグラフ表示画面に移行する
     * gpxPath          GPXファイルのパス
     * title            グラフのタイトル
     */
    fun goGpsGraph(gpxPath: String, title: String) {
        if (klib.existsFile(gpxPath)) {
            val intent = Intent(this, GpsGraph::class.java)
            intent.putExtra("FILE", gpxPath)
            intent.putExtra("TITLE", title)
            startActivity(intent)
        }
    }
}