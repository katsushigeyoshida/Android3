package jp.co.yoshida.katsushige.mapapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationManager
import android.util.Log
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD
import java.util.*
import kotlin.math.max
import kotlin.math.min

class GpsTraceList {
    val TAG = "GpsTraceList"

    var mDataList = mutableListOf<GpsTraceData>()   //  GPSリストデータ
    var mGpsTraceListPath = ""                      //  リストデータのファイル保存パス
    var mGpsTraceFileFolder = ""                    //  トレースデータフォルダ
    var mDisp = true                                //  表示フラグ
    lateinit var mC: Context

    enum class DATALISTSORTTYPE {
        Non, DATE, TITLE, DISTANCE, ELEVATOR
    }
    var mDataListSortCending = false                //  ソート方向降順
    var mDataListSortType = DATALISTSORTTYPE.DATE   //  ソート対象

    val mAllListName = "すべて"
    val mTrashGroup = "ゴミ箱"

    //  カラーメニュー
    val mColorMenu = listOf("Black", "Red", "Blue", "Green", "Yellow", "White",
        "Cyan", "Gray", "LightGray", "Magenta", "DarkGray", "Transparent")
    //  分類メニュー
    val mCategoryMenu = mutableListOf<String>(
        "散歩", "ウォーキング", "ジョギング", "ランニング", "山歩き", "自転車", "車・バス・鉄道", "飛行機", "旅行")

    val klib = KLib()

    /**
     * 再帰的にファイルを検索してリストに等追加する
     */
    fun getFileData() {
        var fileList = klib.getFileList(mGpsTraceFileFolder, true, "*.csv")
        for (i in fileList.indices) {
            var gpsTraceData = GpsTraceData()
            try {
                gpsTraceData.loadGpsData(fileList[i].absolutePath, false)
//                if (null == mDataList.find { it.mFilePath.compareTo(gpsTraceData.mFilePath) == 0 })
                if (!duplicateDataPath(gpsTraceData.mFilePath))
                    mDataList.add(gpsTraceData)
            } catch (e: Exception) {
//                    Toast.makeText(mC, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 年リストの取得
     * firstItem        リストの最初に追加するアイテム
     */
    fun getYearList(firstItem:String = ""): List<String> {
        var yearList = mutableListOf<String>()
        for (i in mDataList.indices) {
            val year = mDataList[i].getYearStr()
            if (!yearList.contains(year))
                yearList.add(year)
        }
        yearList.sortDescending()
        if (0 < firstItem.length)
            yearList.add(0, firstItem)
        return yearList
    }

    /**
     * 分類リストの取得
     * firstItem        リストの最初に追加するアイテム
     */
    fun getCategoryList(firstItem:String = ""): List<String> {
        var categoryList = mutableListOf<String>()
        for (i in mDataList.indices) {
            if (!categoryList.contains(mDataList[i].mCategory))
                categoryList.add(mDataList[i].mCategory)
        }
        categoryList.sortDescending()
        if (0 < firstItem.length)
            categoryList.add(0, firstItem)
        return categoryList
    }

    /**
     *  グループリストの取得
     *  firstTitle  リストの最初に追加するタイトル
     */
    fun getGroupList(firstTitle: String = ""): List<String> {
        var groupList = mutableListOf<String>()
        for (markData in mDataList) {
            if (!groupList.contains(markData.mGroup))
                groupList.add(markData.mGroup)
        }
        groupList.sortDescending()
        if (0 < firstTitle.length)
            groupList.add(0, firstTitle)
        return groupList
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
     * リストデータから表示フラグを設定
     * selectList       選択リスト
     */
    fun setVisible(selectList: List<Int>) {
        clearVisible()
        for (i in selectList.indices)
            mDataList[selectList[i]].mVisible = true
    }

    /**
     * 指定項目を[ゴミ箱]に設定
     */
    fun setTrashData(n: Int) {
        if (0 <= n && n < mDataList.size)
            mDataList[n].mGroup = mTrashGroup
    }

    /**
     * リストデータからグループを[ゴミ箱]に設定
     */
    fun setTrashData(selectList: List<Int>) {
        for (i in selectList.indices)
            mDataList[selectList[i]].mGroup = mTrashGroup
    }

    /**
     * リストデータからグループのゴミ箱を解除
     * selectList       選択リスト
     */
    fun setUnTrashData(selectList: List<Int>) {
        for (i in selectList.indices)
            if (mDataList[selectList[i]].mGroup.compareTo(mTrashGroup) == 0)
                mDataList[selectList[i]].mGroup = ""
    }

    /**
     * すべてのデータのゴミ箱を解除
     */
    fun setAllUnTrashData() {
        for (i in mDataList.indices) {
            if (mDataList[i].mGroup.compareTo(mTrashGroup) == 0)
                mDataList[i].mGroup = ""
        }
    }

    /**
     * リストデータからグループを設定
     * selectList       選択リスト
     */
    fun setGroupData(selectList: List<Int>, group: String) {
        for (i in selectList.indices)
            mDataList[selectList[i]].mGroup = group
    }

    /**
     * リストデータからデータファイルを含めて削除
     * firstTime        開始時間リスト
     */
    fun removeDataFile(firstTimeList: List<String>) {
        for (i in firstTimeList.indices) {
            val n = findListTitleFirstTime(firstTimeList[i])
            Log.d(TAG,"removeDataFile: "+firstTimeList[i]+" "+n+" "+mDataList[n].mFilePath)
            klib.removeFile(mDataList[n].mFilePath)
            mDataList.removeAt(n)
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
                        gpsData.loadGpsData(gpsData.mFilePath)
                    gpsData.draw(canvas, mapData)
                }
            }
        }
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
                ((group.compareTo(mAllListName) == 0 && gpsFileData.mGroup.compareTo(mTrashGroup) != 0)
                        || gpsFileData.mGroup.compareTo(group) == 0)) {
                titleList.add(gpsFileData.getListTitle())
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
     * 対象項目のデータをデータファイルから更新する
     * データファイルがない場合は項目を削除
     * return           更新の可否
     */
    fun reloadDataFile(n: Int): Boolean {
        var gpsData = mDataList[n]
        if (klib.existsFile(gpsData.mFilePath)) {
            val title = gpsData.mTitle
            val group = gpsData.mGroup
//            val category = gpsData.mCategory
            val color = gpsData.mLineColor
            val comment = gpsData.mComment
            gpsData.loadGpsData(gpsData.mFilePath)
            gpsData.mTitle = title
            gpsData.mGroup = group
//            gpsData.mCategory = category
            gpsData.mComment = comment
            gpsData.mLineColor = color
            return true
        } else {
            mDataList.removeAt(n)
            return false
        }
    }

    /**
     * データファイルの存在の有無を確認しなければリストから削除
     */
    fun existDataFileAll(): Int {
        var count = 0
        for (i in mDataList.lastIndex downTo 0) {
            if (!klib.existsFile(mDataList[i].mFilePath)) {
                mDataList.removeAt(i)
                count++
            }
        }
        return count
    }

    /**
     * データファイル名からデータ位置を求める
     * return           データ登録位置
     */
    fun findGpsCsvFile(gpsCsvFilePath: String): Int {
        for (i in mDataList.indices) {
            if (mDataList[i].mFilePath.compareTo(gpsCsvFilePath, true) == 0)
                return i
        }
        return -1
    }

    /**
     * GPSファイルで登録位置を開始時間で検索する(大文字小文字無視)
     * firstTimeStr     開始時間(yyyy/MM/dd HH:mm:ss)
     * return           GPSファイルの登録位置
     */
    fun findListTitleFirstTime(firstTimeStr: String): Int {
        for (i in mDataList.indices) {
            if (mDataList[i].getFirstTimeStr().compareTo(firstTimeStr, true) == 0)
                return i
        }
        return -1
    }

    /**
     * リストデータを取得する
     */
    fun loadListFile(exist: Boolean = false){
        mDataList.clear()
        var gpsDataList = klib.loadCsvData(mGpsTraceListPath, GpsTraceData.mDataFormat)
        for (i in gpsDataList.indices) {
            val gpsTraceData = GpsTraceData()
            gpsTraceData.getStringData(gpsDataList[i])
            if (exist && !klib.existsFile(gpsTraceData.mFilePath))  //  ファイルの存在チェック
                continue
//            if (null == mDataList.find { it.mFilePath.compareTo(gpsTraceData.mFilePath) == 0 })
            if (!duplicateDataPath(gpsTraceData.mFilePath))         //  ファイルの重複チェック
                mDataList.add(gpsTraceData)
        }
    }

    /**
     * リストデータでファイルの重複をチェック
     */
    fun duplicateDataPath(path: String): Boolean {
        for(data in mDataList) {
            if (data.mFilePath.compareTo(path, true) == 0)
                return true
        }
        return false
    }

    /**
     * リストデータを保存
     */
    fun saveListFile() {
        var gpsDataList = mutableListOf<List<String>>()
        for (i in mDataList.indices) {
            gpsDataList.add(mDataList[i].setStringData())
        }
        klib.saveCsvData(mGpsTraceListPath, GpsTraceData.mDataFormat, gpsDataList)
    }

    class GpsTraceData {
        val TAG = "GpsTraceData"

        var mLocData = mutableListOf<PointD>()  //  位置座標データ
        var mStepCountList = mutableListOf<Int>()   //  歩数データ
        var mTitle = ""                         //  タイトル
        var mGroup = ""                         //  グループ名
        var mCategory = ""                      //  分類
        var mComment = ""                       //  コメント
        var mFilePath = ""                      //  gpxファイルパス
        var mVisible = false                    //  表示の可否
        var mLineColor = "Green"                //  表示線分の色
        var mThickness = 4f;                    //  表示線分の太さ
        var mLocArea = RectD()                  //  位置領域(緯度経度座標)
        var mDistance = 0.0                     //  移動距離(km)
        var mMinElevation = 0.0                 //  最小標高(m)
        var mMaxElevation = 0.0                 //  最高標高(m)
        var mFirstTime = Date()                 //  開始時間
        var mLastTime = Date()                  //  終了時間
        var mStepCount = 0                      //  歩数

        val klib = KLib()

        companion object {
            var mDataFormat = listOf<String>(   //  ファイル保存時の項目タイトル
                "Title", "Group", "Category", "Comment", "FilePath", "Visible", "Color", "Thickness",
                "Left", "Top", "Right", "Bottom", "Distance", "MinElevator", "MaxElevator",
                "FirstTime", "LastTime", "StepCOunt"
            )
            var mGpsFormat = listOf<String>(
                "DateTime","Time","Latitude","Longtude","Altitude","Speed","Bearing","Accuracy","StepCount"
            )
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
            val tz = Date().getTimezoneOffset() / 60 + 9
            Log.d(TAG, "getFirstTimeStr: TimeZoneOffset "+tz)
            return klib.date2String(mFirstTime, "yyyy/MM/dd HH:mm:ss", tz)
        }

        /**
         * 平均速度の取得(km/h)
         * return       速度(km/h)
         */
        fun getSpeed():Double {
            return mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000
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

        fun getInfoData(): String {
            var buffer = ""
            val tz = Date().getTimezoneOffset() / 60 + 9
            Log.d(TAG, "getInfoData: TimeZoneOffset "+tz)
            buffer += "開始時間 " + klib.date2String( mFirstTime, "yyyy/MM/dd HH:mm:ss", tz) + "\n"
            buffer += "終了時間 " + klib.date2String( mLastTime, "yyyy/MM/dd HH:mm:ss", tz) + "\n"
            buffer += "経過時間 " + klib.lap2String(mLastTime.time - mFirstTime.time) + "\n"
            buffer += "移動距離 " + "%.2f km  ".format(mDistance)
            buffer += "速度　%.1f km/h  ".format(mDistance/(mLastTime.time - mFirstTime.time)*60*60*1000)
            buffer += "歩数 " + mStepCount + "\n"
            buffer += "最大標高 %.0f m".format(mMaxElevation) + " 最小標高 %.0f m".format(mMinElevation)
            return buffer
        }

        /**
         * 一覧リストのリストデータからデータを取得する
         */
        fun getStringData(data: List<String>) {
            mLocData.clear()
            mStepCountList.clear()
            mTitle = data[0]
            mGroup = data[1]
            mCategory = data[2]
            mComment = data[3]
            mFilePath = data[4]
            mVisible = data[5].toBoolean()
            mLineColor = data[6]
            mThickness = data[7].toFloat()
            mLocArea.left = data[8].toDouble()
            mLocArea.top = data[9].toDouble()
            mLocArea.right = data[10].toDouble()
            mLocArea.bottom = data[11].toDouble()
            mDistance = data[12].toDouble()
            mMinElevation = data[13].toDouble()
            mMaxElevation = data[14].toDouble()
            mFirstTime = Date(data[15].toLong())
            mLastTime = Date(data[16].toLong())
            mStepCount = data[17].toInt()
        }

        /**
         * 一覧リストのリストデータにデータを設定する
         */
        fun setStringData(): List<String> {
            val data = mutableListOf<String>()
            data.add(mTitle)
            data.add(mGroup)
            data.add(mCategory)
            data.add(mComment)
            data.add(mFilePath)
            data.add(mVisible.toString())
            data.add(mLineColor)
            data.add(mThickness.toString())
            data.add(mLocArea.left.toString())
            data.add(mLocArea.top.toString())
            data.add(mLocArea.right.toString())
            data.add(mLocArea.bottom.toString())
            data.add(mDistance.toString())
            data.add(mMinElevation.toString())
            data.add(mMaxElevation.toString())
            data.add(mFirstTime.time.toString())
            data.add(mLastTime.time.toString())
            data.add(mStepCount.toString())
            return data
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
                for (i in 1..mLocData.lastIndex) {
                    var ebp = mLocData[i]
                    var ep = mapData.baseMap2Screen(klib.coordinates2BaseMap(ebp))
                    canvas.drawLine(sp.x.toFloat(), sp.y.toFloat(), ep.x.toFloat(), ep.y.toFloat(), paint)
                    sp = ep
                }
            }
        }

        /**
         * GPS記録データの読込(GPS Serviceで出力されたCSVファイルの読込)、Locationデータとして取り込む
         * path         ファイル名(デフォルト:mGpxPath)
         * locsave      位置データを保存する
         */
        fun loadGpsData(path: String, locsave: Boolean = true) {
            mFilePath = path
            mLocData.clear()
            mStepCountList.clear()
            var listData = klib.loadCsvData(path, mGpsFormat)
            mTitle = klib.getFileNameWithoutExtension(mFilePath)
            mFirstTime = Date(listData[0][1].toLong())
            mLastTime = Date(listData[listData.lastIndex][1].toLong())
            mStepCount = listData[listData.lastIndex][8].toInt() - listData[0][8].toInt()
            mDistance = 0.0
            mMinElevation = Double.MAX_VALUE
            mMaxElevation = Double.MIN_VALUE
            mLocArea.setInitExtension()
            var preLoc = PointD()
            for (data in listData) {
                if (data[0].compareTo("DateTime") != 0) {
                    var location = Location(LocationManager.GPS_PROVIDER)
                    location.time = data[1].toLong()            //  Time      時間(ms)
                    location.latitude = data[2].toDouble()      //  Latitude  緯度
                    location.longitude = data[3].toDouble()     //  Longitude 経度
                    location.altitude = data[4].toDouble()      //  Altitude  高度(m)
                    location.speed = data[5].toFloat()          //  Speed     速度(m/s)
                    location.bearing = data[6].toFloat()        //  Bearing   方位(度)
                    location.accuracy = data[7].toFloat()       //  Accuracy  精度(半径 m)
                    val loc = PointD(location.longitude, location.latitude)
                    if (!preLoc.isEmpty())
                        mDistance += klib.cordinateDistance(preLoc, loc)
                    preLoc = loc
                    if (locsave)
                        mLocData.add(loc)
                    mLocArea.extension(loc)
                    if (8 < data.size)
                        mStepCountList.add(data[8].toInt())         //  StepCount 歩数
                    else
                        mStepCountList.add(0)
                    mMinElevation = min(mMinElevation, location.altitude)
                    mMaxElevation = max(mMaxElevation, location.altitude)
                }
            }
            val lap = mLastTime.time - mFirstTime.time
            mCategory = speed2Category(lap, mDistance, mStepCount, mMaxElevation - mMinElevation)
        }

        /**
         * 経過時間、距離、歩数、標高差から分類を求める
         */
        fun speed2Category(lap: Long, distance: Double, stepCount: Int, elevator: Double): String {
            Log.d(TAG,"speed2Category: "+lap + " " + distance + "  " + stepCount)
            val speed = distance / (lap.toDouble() / 3600.0 / 1000.0)   //  速度(km/h)
            val stepDis = if (0 < stepCount) distance * 1000.0 / stepCount else -1.0  //  歩幅(m)
            Log.d(TAG,"speed2Category: "+speed + " " + stepDis + "  " + elevator)
            if (stepDis < 10.0) {
                if (speed < 6.0) {                      //  速度6km/h以下
                    return if (elevator < 300.0)        //  標高差 300m以下
                        "散歩" else "山歩き"
                } else if (speed < 12.0) {              //  速度 6-12のもめく
                    return "ジョギング"
                } else if (speed < 30.0) {              //  速度 12-30km/h
                    return "ランニング"
                } else {
                    return "自転車"
                }
            } else {
                if (speed < 40.0) {                     //  速度 12-30km/h
                    return "自転車"
                } else if (speed < 200.0)  {
                    return "車・バス・鉄道"
                } else {
                    return "飛行機"
                }
            }
            return "散歩"
        }

    }
}