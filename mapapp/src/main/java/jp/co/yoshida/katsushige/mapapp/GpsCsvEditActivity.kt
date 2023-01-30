package jp.co.yoshida.katsushige.mapapp

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityGpsCsvEditBinding
import jp.co.yoshida.katsushige.mylib.KLib

/**
 * 現在未使用 GpxEditActivityに統一
 */
class GpsCsvEditActivity : AppCompatActivity() {
    val TAG = "GpsCsvEditActivity"

    lateinit var binding: ActivityGpsCsvEditBinding
    lateinit var edTitle: EditText
    lateinit var edGroup: EditText
    lateinit var edGpsFilePath: EditText
    lateinit var edComment: EditText
    lateinit var tvYear: TextView
    lateinit var tvGpsFileInfo: TextView
    lateinit var btGruopRef: Button
    lateinit var btGpsFilePathRef: Button
    lateinit var btGraph: Button
    lateinit var btOK: Button
    lateinit var btCancel: Button
    lateinit var spColor: Spinner
    lateinit var spCategory: Spinner

    var mGpsTraceListPath = ""
    var mGpsTraceFilePath = ""
    var mGpsCsvFilePos = -1                                //  選択されたGPXファイル位置

    val mGpsTraceList = GpsTraceList()
    val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_csv_edit)
        title = "GPSトレースデータ"

        mGpsTraceListPath = intent.getStringExtra("GPSTRACELISTPATH").toString()
        mGpsTraceFilePath = intent.getStringExtra("GPSTRACEFILEPATH").toString()
        mGpsTraceList.mGpsTraceListPath = mGpsTraceListPath
        mGpsTraceList.loadListFile()

        initControl()
        setDataGpsCsvFile(mGpsTraceFilePath)
    }

    fun initControl() {
        binding = ActivityGpsCsvEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edTitle = binding.editTextTextPersonName17
        edGroup = binding.editTextTextPersonName18
        edGpsFilePath =binding.editTextTextPersonName19
        edComment = binding.editTextTextPersonName20
        tvYear = binding.textView37
        tvGpsFileInfo = binding.textView45
        btGruopRef = binding.button16
        btGpsFilePathRef = binding.button21
        btOK = binding.button22
        btCancel = binding.button23
        btGraph = binding.button24
        spColor = binding.spinner10
        spCategory = binding.spinner11

        edTitle.setText("")
        edGroup.setText("")
        edGpsFilePath.setText("")
        edComment.setText("")
        tvGpsFileInfo.setText("")

        //  線分の色のSpinner
        var colorAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mGpsTraceList.mColorMenu)
        spColor.adapter = colorAdapter

        //  分類のSpinner
        var categoryAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mGpsTraceList.mCategoryMenu)
        spCategory.adapter = categoryAdapter

        //  グループ設定
        btGruopRef.setOnClickListener {
            var groupList = mGpsTraceList.getGroupList("")
            klib.setMenuDialog(this, "グループ", groupList, iGpxGroup)
        }


        //  登録処理
        btOK.setOnClickListener {
            val gpsTraceData = GpsTraceList.GpsTraceData()
            gpsTraceData.mFilePath = edGpsFilePath.text.toString()
            gpsTraceData.loadGpsData(false)
            gpsTraceData.mTitle = edTitle.text.toString()
            gpsTraceData.mGroup = edGroup.text.toString()
            gpsTraceData.mCategory = mGpsTraceList.mCategoryMenu[spCategory.selectedItemPosition]
            gpsTraceData.mLineColor = mGpsTraceList.mColorMenu[spColor.selectedItemPosition]
            gpsTraceData.mComment = edComment.text.toString()
            gpsTraceData.mVisible = mGpsTraceList.mDataList[mGpsCsvFilePos].mVisible
            if (0 <= mGpsCsvFilePos) {
                mGpsTraceList.mDataList[mGpsCsvFilePos] = gpsTraceData
            } else {
                mGpsTraceList.mDataList.add(gpsTraceData)
            }
            mGpsTraceList.saveListFile()

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
            edGpsFilePath.setText(s)
            //  選択ファイルのフォルダを保存
            klib.setStrPreferences(klib.getFolder(s), "GpxFileFolder", this)
            if (edTitle.text.length == 0)
                edTitle.setText(klib.getFileNameWithoutExtension(s))
            //  GPSデータの取得と登録
            setGpsCsvFileInfo(s)
        }
    }


    fun setDataGpsCsvFile(gpsCsvFilePath: String) {
        mGpsCsvFilePos = mGpsTraceList.findGpsFile(gpsCsvFilePath)
        if (0 <= mGpsCsvFilePos) {
            Log.d(TAG, "setDataGpsCsvFile: "+mGpsCsvFilePos+" "+gpsCsvFilePath)
            edTitle.setText(mGpsTraceList.mDataList[mGpsCsvFilePos].mTitle)
            edGroup.setText(mGpsTraceList.mDataList[mGpsCsvFilePos].mGroup)
            edGpsFilePath.setText(mGpsTraceList.mDataList[mGpsCsvFilePos].mFilePath)
            edComment.setText(mGpsTraceList.mDataList[mGpsCsvFilePos].mComment)
            setGpsCsvFileInfo(mGpsTraceList.mDataList[mGpsCsvFilePos].mFilePath)
            spColor.setSelection(mGpsTraceList.mColorMenu.indexOf(mGpsTraceList.mDataList[mGpsCsvFilePos].mLineColor))
            spCategory.setSelection(mGpsTraceList.mCategoryMenu.indexOf(mGpsTraceList.mDataList[mGpsCsvFilePos].mCategory))
        } else {
            edGpsFilePath.setText(gpsCsvFilePath)
            setGpsCsvFileInfo(gpsCsvFilePath)
        }
    }

    /**
     * GPXファイルからGPX情報と測定年をコントロールに登録
     */
    fun setGpsCsvFileInfo(gpsTraceFilePath: String) {
        val gpsTraceData = GpsTraceList.GpsTraceData()
        gpsTraceData.loadGpsData()
        tvGpsFileInfo.setText(gpsTraceData.getInfoData())
        tvYear.setText(klib.date2String( gpsTraceData.mFirstTime, "yyyy年"))
        if(spCategory.selectedItemPosition < 0) {
            var v = gpsTraceData.getSpeed()
            if (20 < v)
                spCategory.setSelection(mGpsTraceList.mCategoryMenu.indexOf("車"))
            else if (12 < v)
                spCategory.setSelection(mGpsTraceList.mCategoryMenu.indexOf("自転車"))
            else if (6 < v)
                spCategory.setSelection(mGpsTraceList.mCategoryMenu.indexOf("ジョギング"))
            else
                spCategory.setSelection(mGpsTraceList.mCategoryMenu.indexOf("散歩"))
        }
    }
}