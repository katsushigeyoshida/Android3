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
    enum class DATALISTSORTTYPE {
        Non, DATE, TITLE, DISTANCE, ELEVATOR
    }
    var mDataListSortCending = false                //  ソート方向降順
    var mDataListSortType = DATALISTSORTTYPE.DATE   //  ソート対象

    enum class SORTTYPE {
        Non, Normal, Reverse
    }
    var mSortName = listOf<String>( "ソートなし", "昇順", "降順")
    var mListSort = SORTTYPE.Non

    val mAllListName = "すべて"

    //  カラーメニュー
    val mColorMenu = listOf("Black", "Red", "Blue", "Green", "Yellow", "White",
        "Cyan", "Gray", "LightGray", "Magenta", "DarkGray", "Transparent")
    //  分類メニュー
    val mCategoryMenu = mutableListOf<String>(
        "散歩", "ウォーキング", "ジョギング", "ランニング", "山歩き", "自転車", "車", "旅行")

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
     * 年リストの取得
     * firstItem        リストの最初に追加するアイテム
     */
    fun getYearList(firstItem:String = ""): List<String> {
        var yearList = mutableListOf<String>()
        if (0 < firstItem.length)
            yearList.add(firstItem)
        for (i in mDataList.indices) {
            val year = mDataList[i].getYearStr()
            if (!yearList.contains(year))
                yearList.add(year)
        }
        return yearList
    }

    /**
     * 分類リストの取得
     * firstItem        リストの最初に追加するアイテム
     */
    fun getCategoryList(firstItem:String = ""): List<String> {
        var categoryList = mutableListOf<String>()
        if (0 < firstItem.length)
            categoryList.add(firstItem)
        for (i in mDataList.indices) {
            if (!categoryList.contains(mDataList[i].mCategory))
                categoryList.add(mDataList[i].mCategory)
        }
        return categoryList
    }

    /**
     *  グループリストの取得
     *  firstTitle  リストの最初に追加するタイトル
     */
    fun getGroupList(firstTitle: String = ""): List<String> {
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
     *  リストビューに表示するタイトルリストをつくる
     *  return          タイトルリスト
     */
    fun getListTitleData(year: String, category: String, group: String): List<String> {
        Log.d(TAG,"getListTitleData: "+mDataListSortCending+" "+mDataListSortType)
        var titleList = mutableListOf<String>()
        //  ソート処理
        if (mDataListSortCending) {
            if (mDataListSortType == DATALISTSORTTYPE.DATE) {
                mDataList.sortWith({ a, b -> (a.mFirstTime.time / 1000 - b.mFirstTime.time / 1000).toInt() })
            } else if (mDataListSortType == DATALISTSORTTYPE.TITLE) {
                mDataList.sortWith({ a, b -> a.mTitle.compareTo(b.mTitle) })
            } else if (mDataListSortType == DATALISTSORTTYPE.DISTANCE) {
                mDataList.sortWith({ a, b -> (a.mDistance * 1000 - b.mDistance * 1000).toInt() })
            } else if (mDataListSortType == DATALISTSORTTYPE.ELEVATOR) {
                mDataList.sortWith({ a, b -> (a.mMaxElevation - b.mMaxElevation).toInt() })
            }
        } else {
            if (mDataListSortType == DATALISTSORTTYPE.DATE) {
                mDataList.sortWith({ b, a -> (a.mFirstTime.time / 1000 - b.mFirstTime.time / 1000).toInt() })
            } else if (mDataListSortType == DATALISTSORTTYPE.TITLE) {
                mDataList.sortWith({ b, a -> a.mTitle.compareTo(b.mTitle) })
            } else if (mDataListSortType == DATALISTSORTTYPE.DISTANCE) {
                mDataList.sortWith({ b, a -> (a.mDistance * 1000 - b.mDistance * 1000).toInt() })
            } else if (mDataListSortType == DATALISTSORTTYPE.ELEVATOR) {
                mDataList.sortWith({ b, a -> (a.mMaxElevation - b.mMaxElevation).toInt() })
            }
        }
        //  表示タイトル設定
        for (gpsFileData in mDataList) {
            if ((year.compareTo(mAllListName) == 0 || gpsFileData.getYearStr().compareTo(year) == 0) &&
                (category.compareTo(mAllListName) == 0 || gpsFileData.mCategory.compareTo(category) == 0) &&
                (group.compareTo(mAllListName) == 0 || gpsFileData.mGroup.compareTo(group) == 0)) {
                titleList.add(gpsFileData.getListTitle())
                Log.d(TAG,"getListTitleData: "+gpsFileData.getListTitle())
            }
        }
        return titleList
    }

    /**
     * ソートタイプの設定をおこなう
     * 現ソートタイプと同じであればソート方向を反転する
     * sortType         ソートタイプ
     */
    fun setDataListSortType(sortType: DATALISTSORTTYPE) {
        if (mDataListSortType == sortType) {
            mDataListSortCending = !mDataListSortCending
        } else {
            mDataListSortType = sortType
        }
        Log.d(TAG,"setDataListSortType: "+mDataListSortCending+" "+mDataListSortType)
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
        for (i in mDataList.indices) {
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
     * GPXファイルで登録位置をファイルパスで検索する(大文字小文字無視)
     * gpxFilePath      GPXファイルパス
     * return           GPXファイルの登録位置
     */
    fun findGpxFile(gpxFilePath: String): Int {
        for (i in mDataList.indices) {
            if (mDataList[i].mFilePath.compareTo(gpxFilePath, true) == 0)
                return i
        }
        return -1
    }

    /**
     * GPXファイルで登録位置を開始時間で検索する(大文字小文字無視)
     * firstTimeStr     開始時間(yyyy/MM/dd HH:mm:ss)
     * return           GPXファイルの登録位置
     */
    fun findListTitleFirstTime(firstTimeStr: String): Int {
        for (i in mDataList.indices) {
            if (mDataList[i].getFirstTimeStr().compareTo(firstTimeStr, true) == 0)
                return i
        }
        return -1
    }

    /**
     * 開始時刻で削除する
     * firstTimeStr     開始時刻(yyyy/MM/dd HH:mm:ss)
     */
    fun removeListData(firstTimeStr: String) {
        val n = findListTitleFirstTime(firstTimeStr)
        if (0 <= n)
            mDataList.removeAt(n)
    }

    /**
     * 指定項目を削除する
     * n        データ位置
     */
    fun removeListData(n: Int) {
        if (0 <= n)
            mDataList.removeAt(n)
    }

    /**
     * 全データの表示フラグをクリア(非表示)にする
     */
    fun clearVisible() {
        for (i in mDataList.indices) {
            mDataList[i].mVisible = false
        }
    }

    /**
     * 全データの表示フラグを表示にする
     */
    fun setAllVisible() {
        for (i in mDataList.indices) {
            mDataList[i].mVisible = true
        }
    }

    /**
     * 表示フラグを反転する
     */
    fun reverseVisible() {
        for (i in mDataList.indices) {
            mDataList[i].mVisible = !mDataList[i].mVisible
        }
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
     *  リストデータの保存と表示設定をプリファレンスに登録
     */
    fun saveDataFile(c: Context) {
        Log.d(TAG, "saveDataFile: "+mDataList.size+" "+mSaveFilePath)
        saveDataFile(mSaveFilePath)
        saveParameter(c)
    }

    /**
     *  リストデータの読込と表示設定をプリファレンスから取得
     */
    fun loadDataFile(c: Context) {
        loadDataFile(mSaveFilePath, false)
        loadParameter(c)
        Log.d(TAG, "loadDataFile: "+mDataList.size+" "+mSaveFilePath)
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
}

/**
 * GPSファイルデータ情報
 */
class GpsFileData {
    val TAG = "GpsFileData"
    var mLocData = mutableListOf<PointD>()  //  位置座標データ
    var mTitle = ""                         //  タイトル
    var mGroup = ""                         //  グループ名
    var mCategory = ""                      //  分類
    var mComment = ""                       //  コメント
    var mFilePath = ""                      //  gpxファイルパス
    var mVisible = true                     //  表示の可否
    var mLineColor = "Green"                //  表示線分の色
    var mThickness = 4f;                    //  表示線分の太さ
    var mLocArea = RectD()                  //  位置領域(緯度経度座標)
    var mDistance = 0.0                     //  移動距離(km)
    var mMinElevation = 0.0                 //  最小標高(m)
    var mMaxElevation = 0.0                 //  最高標高(m)
    var mFirstTime = Date()                 //  開始時間
    var mLastTime = Date()                  //  終了時間

    companion object {
        var mDataFormat = listOf<String>(   //  ファイル保存時の項目タイトル
            "Title", "Group", "Category", "Comment", "FilePath", "Visible", "Color", "Thickness",
            "Left", "Top", "Right", "Bottom", "Distance", "MinElevator", "MaxElevator",
            "FirstTime", "LastTime"
        )
    }

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
        dataList.add(mCategory)
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
     *  data    パラメータの文字配列
     */
    fun setStringData(data: List<String>) {
        mTitle          = data[0]
        mGroup          = data[1]
        mCategory       = data[2]
        mComment        = data[3]
        mFilePath       = data[4]
        mVisible        = data[5].toBoolean()
        mLineColor      = data[6]
        mThickness      = data[7].toFloat()
        mLocArea.left   = data[8].toDouble()
        mLocArea.top    = data[9].toDouble()
        mLocArea.right  = data[10].toDouble()
        mLocArea.bottom = data[11].toDouble()
        mDistance       = data[12].toDouble()
        mMinElevation   = data[13].toDouble()
        mMaxElevation   = data[14].toDouble()
        mFirstTime      = Date(data[15].toLong())
        mLastTime       = Date(data[16].toLong())
    }

    /**
     * GPS情報を文字列で取得
     */
    fun getInfoData(): String {
        var buffer = ""
        buffer += "開始時間 " + klib.date2String( mFirstTime, "yyyy/MM/dd HH:mm:ss") + "\n"
        buffer += "終了時間 " + klib.date2String( mLastTime, "yyyy/MM/dd HH:mm:ss") + "\n"
        buffer += "経過時間 " + klib.lap2String(mLastTime.time - mFirstTime.time) + "\n"
        buffer += "移動距離 " + "%.2f km  ".format(mDistance)
        buffer += "速度　%.1f km/h".format(mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000) + "\n"
        buffer += "最大標高 %.0f m".format(mMaxElevation) + " 最小標高 %.0f m".format(mMinElevation)
        return buffer
    }

    /**
     * 一覧リスト用タイトル
     */
    fun getListTitle(): String {
        var title = if (mVisible) "*" else " "
        title += getFirstTimeStr() + " "
        title += mTitle +"\n"
        title += "[" + mCategory + "]"
        title += "[" + mGroup + "] "
        title += "%.2f km".format(mDistance)
        title += "(" + klib.lap2String(mLastTime.time - mFirstTime.time) + ") "
        title += "%.1f km/h".format(mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000) + " "
        title += "%.0f m".format(mMinElevation) + "-" + "%.0f m".format(mMaxElevation)
        return title
    }

    /**
     * データの開始日時の年を取出す(xxxx年)
     * return           xxxx年
     */
    fun getYearStr(): String {
        return klib.date2String(mFirstTime, "yyyy年")
    }

    /**
     * 開始時間を文字列で取得
     * return       開始時間の文字列
     */
    fun getFirstTimeStr(): String {
        return klib.date2String(mFirstTime, "yyyy/MM/dd HH:mm:ss")
    }

    /**
     * 平均速度の取得(km/h)
     * return       速度(km/h)
     */
    fun getSpeed():Double {
        return mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000
    }
}