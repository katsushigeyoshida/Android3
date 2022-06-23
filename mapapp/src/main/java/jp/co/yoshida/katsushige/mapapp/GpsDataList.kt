package jp.co.yoshida.katsushige.mapapp

import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mylib.GpxReader
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD
import java.io.File
import java.util.*

/**
 * GPSデータのリストデータ管理
 */
class GpsDataList {
    val TAG = "GpsDataList"
    var mDataList = mutableListOf<GpsFileData>()    //  GPSリストデータ
    var mSaveFilePath = ""                          //  リストデータのファイル保存パス
    var mDisp = true                                //  表示フラグ
    var mPreSelectGroup = ""                        //  タイトルリストで指定したグループ名
    enum class SORTTYPE {
        Non, Normal, Reverse
    }
    var mSortName = listOf<String>( "ソートなし", "昇順", "降順")
    var mListSort = SORTTYPE.Non
    val mAllListName = "すべて"

    //  カラーメニュー
    val mColorMenu = listOf("Black", "Red", "Blue", "Green", "Yellow", "White",
        "Cyan", "Gray", "LightGray", "Magenta", "DarkGray", "Transparent")

    val klib = KLib()

    /**
     *  GPXファイルデータをリストに登録
     *  path        ファイルパス
     */
    fun addFile(path: String) {
        var gpsFileData = GpsFileData()
        gpsFileData.getFileData(path)
        mDataList.add(gpsFileData)
    }

    /**
     *  グループリストの取得
     *  firstTitle  リストの最初に追加するタイトル
     */
    fun getGroupList(firstTitle: String): List<String> {
        var groupList = mutableListOf<String>()
        if (0 < firstTitle.length)
            groupList.add(firstTitle)
        for (markData in mDataList) {
            if (!groupList.contains(markData.mGroup))
                groupList.add(markData.mGroup)
        }
        return groupList
    }

    /**
     *  タイトルリストの取得
     *  group   検索する対象グループ名
     */
    fun getTitleList(group: String):List<String> {
        mPreSelectGroup = group
        var titleList = mutableListOf<String>()
        for (gpsFileData in mDataList) {
            if (group.compareTo(mAllListName) == 0 || gpsFileData.mGroup.compareTo(group) == 0 &&
                    !titleList.contains(gpsFileData.mTitle))
                titleList.add(gpsFileData.mTitle)
        }
        if (mListSort == SORTTYPE.Normal) {
            titleList.sortWith(Comparator{ a,b -> a.compareTo(b)})
        } else if (mListSort == SORTTYPE.Reverse) {
            titleList.sortWith(Comparator{ a,b -> b.compareTo(a) })
        }

        return titleList
    }

    /**
     *  グループ単位でのgpsFileDataリストの取得
     *  group   グループ名
     *  return  GPSのリストデータ
     */
    fun getDataList(group: String):List<GpsFileData> {
        mPreSelectGroup = group
        var dataList = mutableListOf<GpsFileData>()
        for (gpsFileData in mDataList) {
            if (group.compareTo(mAllListName) == 0 || gpsFileData.mGroup.compareTo(group) == 0)
                dataList.add(gpsFileData)
        }
        if (mListSort == SORTTYPE.Normal) {
            dataList.sortBy { it.mTitle }
        } else if (mListSort == SORTTYPE.Reverse) {
            dataList.sortByDescending { it.mTitle }
        }
        return dataList
    }

    /**
     *  タイトルとグループからデータのインデックスを返す
     *  title   検索タイトル名
     *  group   検索対象グループ名
     */
    fun getDataNum(title: String, group: String): Int {
        for (i in 0..mDataList.size - 1) {
            if (mDataList[i].mTitle.compareTo(title) == 0 &&
                    (group.compareTo(mAllListName) == 0 || mDataList[i].mGroup.compareTo(group) == 0))
                return i
        }
        return -1
    }

    /**
     *  タイトルとグループから検索してGPSデータを取得する
     *  title   検索タイトル名
     *  group   検索対象グループ名
     */
    fun getData(title: String, group: String): GpsFileData {
        for (data in mDataList) {
            if (data.mTitle.compareTo(title) == 0 &&
                    (0 == group.length || data.mGroup.compareTo(group) == 0))
                return data
        }
        return GpsFileData()
    }

    /**
     *  GPSトレースの表示
     *  canvas      描画canvas
     *  mapData     地図位置情報
     */
    fun draw(canvas: Canvas, mapData: MapData) {
        if (mDisp) {
            for (gpsData in mDataList) {
                if (gpsData.mVisible &&
                    (gpsData.mLocArea.isEmpty() || !mapData.getAreaCoordinates().outside(gpsData.mLocArea))) {
                    if (gpsData.mLocData.size < 1)
                        gpsData.getFileData(gpsData.mFilePath)
                    gpsData.draw(canvas, mapData)
                }
            }
        }
    }

    /**
     *  GPXトレースの編集ダイヤログ
     *  c           context
     *  title       ダイヤログのタイトル
     *  data        表示データの文字列リスト
     *  operation   操作関数インタフェース
     */
    fun setGpxInputDialog(c: Context, title: String, data: List<String>, operation: Consumer<String>) {
        val linearLayout = LinearLayout(c)
        val titleLabel = TextView(c)
        val etTitle = EditText(c)
        val groupLabel = TextView(c)
        var etGroup = EditText(c)
        var spGroup = Spinner(c)
        val colorLabel = TextView(c)
        var etColor = EditText(c)
        var spColor = Spinner(c)
        val commanetLabel = TextView(c)
        val etComment = EditText(c)
        val filePathLabel = TextView(c)
        val tvFilePath = TextView(c)
        val gpsInfoLabel = TextView(c)
        val tvGpsInfo = TextView(c)

        linearLayout.orientation = LinearLayout.VERTICAL
        titleLabel.setText("タイトル")
        groupLabel.setText("グループ")
        colorLabel.setText("カラー")
        commanetLabel.setText("コメント")
        filePathLabel.setText("ファイルパス")
        gpsInfoLabel.setText("GPXデータ情報")

        var groupList = getGroupList("")
        var groupAdapter = ArrayAdapter(c, R.layout.support_simple_spinner_dropdown_item, groupList)
        spGroup.adapter = groupAdapter
        spGroup.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etGroup.setText(groupList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        var colorAdapter = ArrayAdapter(c, R.layout.support_simple_spinner_dropdown_item, mColorMenu)
        spColor.adapter = colorAdapter
        spColor.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etColor.setText(mColorMenu[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        if (6 < data.size) {
            //  編集時のデータ設定
            etTitle.setText(data[1])
            etGroup.setText(data[2])
            spGroup.setSelection(groupList.indexOf(etGroup.text.toString()))
            etComment.setText(data[3])
            tvFilePath.setText(data[4])
            etColor.setText(data[6])
            spColor.setSelection(mColorMenu.indexOf(etColor.text.toString()))
            tvGpsInfo.setText(data[data.size - 1])
        }

        linearLayout.addView(titleLabel)
        linearLayout.addView(etTitle)
        linearLayout.addView(groupLabel)
        linearLayout.addView(etGroup)
        linearLayout.addView(spGroup)
        linearLayout.addView(colorLabel)
        linearLayout.addView(etColor)
        linearLayout.addView(spColor)
        linearLayout.addView(commanetLabel)
        linearLayout.addView(etComment)
        linearLayout.addView(filePathLabel)
        linearLayout.addView(tvFilePath)
        linearLayout.addView(gpsInfoLabel)
        linearLayout.addView(tvGpsInfo)

        val dialog = AlertDialog.Builder(c)
        dialog.setTitle(title + " [" + data[0] + "]")
        dialog.setView(linearLayout)
        dialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            operation.accept(data[0] + "," + etTitle.text.toString() + "," + etGroup.text.toString() + "," +
                    etComment.text.toString() + "," + tvFilePath.text.toString() + "," + etColor.text.toString())
        })
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    /**
     *  リストデータの保存と表示設定をプリファレンスに登録
     */
    fun saveDataFile(c: Context) {
        saveDataFile(mSaveFilePath)
        saveParameter(c)
    }

    /**
     *  リストデータの読込と表示設定をプリファレンスから取得
     */
    fun loadDataFile(c: Context) {
        loadDataFile(mSaveFilePath, false)
        loadParameter(c)
    }

    /**
     *  マークリストを指定パスでファイルに保存
     *  path        保存ファイル目
     */
    fun saveDataFile(path: String) {
        if (mDataList.size == 0)
            return
        var dataList = mutableListOf<List<String>>()
        for (gpsData in mDataList) {
            dataList.add(gpsData.getStringData())
        }
        klib.saveCsvData(path, GpsFileData.mDataFormat, dataList)
    }

    /**
     *  マークリストを指定ファイルから取得
     *  path        ファイル名
     *  add         追加読込の時
     */
    fun loadDataFile(path: String, add: Boolean) {
        var file = File(path)
        if (file.exists()) {
            var dataList = klib.loadCsvData(path, GpsFileData.mDataFormat)
            if (!add)
                mDataList.clear()
            for (data in dataList) {
                var gpsData = GpsFileData()
                gpsData.setStringData(data)
                if (0 == getData(gpsData.mTitle, gpsData.mGroup).mTitle.length)
                    mDataList.add(gpsData)
            }
        }
    }

    /**
     *  プリファレンスにパラメータを保存
     */
    fun saveParameter(context: Context) {
        klib.setBoolPreferences(mDisp, "GpsDataDisp", context)
    }

    /**
     *  プリファレンスからパラメータを取得
     */
    fun loadParameter(context: Context) {
        mDisp = klib.getBoolPreferences("GpsDataDisp", context)
    }
}

/**
 * GPSファイルデータ情報
 */
class GpsFileData {
    val TAG = "GpsFileData"
    var mLocData = mutableListOf<PointD>()  //  位置座標データ
    var mTitle = ""                         //  タイトル
    var mGroup = ""                         //  グループ名
    var mLineColor = "Green"                //  表示線分の色
    var mThickness = 4f;                    //  表示線分の太さ
    var mFilePath = ""                      //  gpxファイルパス
    var mComment = ""                       //  コメント
    var mVisible = true                     //  表示の可否
    companion object {
        var mDataFormat = listOf<String>(   //  ファイル保存時の項目タイトル
                "Title", "Group", "Comment", "FilePath", "Visible", "Color", "Thickness",
                "Left", "Top", "Right", "Bottom", "Distance", "MinElevator", "MaxElevator",
                "FirstTime", "LastTime"
        )
    }
    var mLocArea = RectD()                  //  位置領域(緯度経度座標9
    var mDistance = 0.0                     //  移動距離8km)
    var mMinElevation = 0.0                 //  最小標高8m)
    var mMaxElevation = 0.0                 //  再考標高8m)
    var mFirstTime = Date()                 //  開始時間
    var mLastTime = Date()                  //  終了時間

    val klib = KLib()

    /**
     * GPXファイルデータの読込と情報設定
     * path     GPXファイルパス
     */
    fun getFileData(path: String){
        mFilePath = path
        //  gpxファイルからGPSデータの取得
        var gpsReader = GpxReader(GpxReader.DATATYPE.gpxData)
        if (mTitle.length == 0)
            mTitle = klib.getFileNameWithoutExtension(path)
        if (0 < gpsReader.getGpxRead(path)) {
            //  GPSデータから位置リストを取得
            gpsReader.setGpsInfoData()
            mLocData  = gpsReader.mListGpsPointData
            mLocArea  = gpsReader.mGpsInfoData.mArea
            mDistance = gpsReader.mGpsInfoData.mDistance
            mMinElevation = gpsReader.mGpsInfoData.mMinElevator
            mMaxElevation = gpsReader.mGpsInfoData.mMaxElevator
            mFirstTime    = gpsReader.mGpsInfoData.mFirstTime
            mLastTime     = gpsReader.mGpsInfoData.mLastTime
        }
    }

    /**
     *  GPS位置情報をトレースす表示する
     *  canvas      描画canvas
     *  mapData     地図座標データ
     */
    fun draw(canvas: Canvas, mapData: MapData) {
        if (1 < mLocData.size) {
            var paint = Paint()
            paint.color = if (klib.mColorMap[mLineColor] == null) Color.BLACK else klib.mColorMap[mLineColor]!!
            paint.strokeWidth = mThickness

            var sbp = mLocData[0]
            var sp = mapData.baseMap2Screen(klib.coordinates2BaseMap(sbp))
            for (i in 1..mLocData.size - 1) {
                var ebp = mLocData[i]
                var ep = mapData.baseMap2Screen(klib.coordinates2BaseMap(ebp))
                canvas.drawLine(sp.x.toFloat(), sp.y.toFloat(), ep.x.toFloat(), ep.y.toFloat(), paint)
                sp = ep
            }
        }
    }

    /**
     *  パラメータを文字リストデータで取得
     */
    fun getStringData(): List<String> {
        var dataList = mutableListOf<String>()
        dataList.add(mTitle)
        dataList.add(mGroup)
        dataList.add(mComment)
        dataList.add(mFilePath)
        dataList.add(mVisible.toString())
        dataList.add(mLineColor)
        dataList.add(mThickness.toString())
        dataList.add(mLocArea.left.toString())
        dataList.add(mLocArea.top.toString())
        dataList.add(mLocArea.right.toString())
        dataList.add(mLocArea.bottom.toString())
        dataList.add(mDistance.toString())
        dataList.add(mMinElevation.toString())
        dataList.add(mMaxElevation.toString())
        dataList.add(mFirstTime.time.toString())
        dataList.add(mLastTime.time.toString())

        return dataList
    }

    //
    /**
     *  文字データ配列でパラメータに設定
     *  data    パラメータの文字配列]
     */
    fun setStringData(data: List<String>) {
        if (data.size < 7)
            return
        mTitle     = data[0]
        mGroup     = data[1]
        mComment   = data[2]
        mFilePath  = data[3]
        mVisible   = data[4].toBoolean()
        mLineColor = data[5]
        mThickness = data[6].toFloat()
        if (data.size < 11)
            return
        mLocArea.left   = data[7].toDouble()
        mLocArea.top    = data[8].toDouble()
        mLocArea.right  = data[9].toDouble()
        mLocArea.bottom = data[10].toDouble()
        if (data.size < 16)
            return
        mDistance     = data[11].toDouble()
        mMinElevation = data[12].toDouble()
        mMaxElevation = data[13].toDouble()
        mFirstTime    = Date(data[14].toLong())
        mLastTime     = Date(data[15].toLong())
    }

    /**
     * GPS情報を文字列で取得
     */
    fun getInfoData(): String {
        var buffer = ""
        buffer += "開始時間 " + klib.date2String( mFirstTime, "yyyy/MM/dd HH:mm:ss") + " "
        buffer += "終了時間 " + klib.date2String( mLastTime, "yyyy/MM/dd HH:mm:ss") + "\n"
        buffer += "経過時間 " + klib.lap2String(mLastTime.time - mFirstTime.time) + "\n"
        buffer += "移動距離 " + "%.2f km".format(mDistance)
        buffer += " 速度　%.1f km/h".format(mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000) + "\n"
        buffer += "最大標高 %.0f m".format(mMaxElevation) + " 最小標高 %.0f m".format(mMinElevation)
        return buffer
    }
}