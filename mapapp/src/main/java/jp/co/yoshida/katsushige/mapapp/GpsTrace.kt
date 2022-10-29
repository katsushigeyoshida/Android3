package jp.co.yoshida.katsushige.mapapp

import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mylib.GpxWriter
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 *  GPSでの取得位置情報をCSV形式でデータ保存/読込、トレースを地図上に表示する
 *  GPSトレースファイルのファイル操作(Rename/Move/Remove/Export/Info)
 */
class GpsTrace {
    val TAG = "GpsTrace"
    var mTraceOn = false                            //  GPSトレース中のフラグ
    var mGpxConvertOn = false                       //  GPXデータ変換フラグ
    lateinit var mGpxConvertJob: Job                //  GPXファイル変換Job
    var mGpsTraceFileFolder = ""                    //  GPSトレースデータを保存するフォルダ
    var mGpsPath = ""                               //  GPSトレースデータファイルパス
    var mGpsData = mutableListOf<Location>()        //  GPSトレースのデータリスト(フルデータ)
    var mGpsPointData = mutableListOf<PointD>()     //  GPSトレースのデータリスト(座標のみの簡易形式)
    var mGpsLastElevator = 0.0                      //  GPSトレースの標高最新値
    var mStepCount = mutableListOf<Int>()           //  歩数のレスとデータ
    var mGpsLap = mutableListOf<Long>()             //  GPS経過時間
    var mGpsPointDatas = mutableListOf<List<PointD>>()  //  GPSトレースデータリスト(簡易形式)のリスト
    var mLineColor = Color.GREEN                    //  トレース中の線の色
    var mLineColors = listOf(                       //  色リスト
        Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.YELLOW, Color.GREEN)
    var mTempFileNameList = mutableListOf<String>() //  データ受け渡し用の一時ファイルリスト
    lateinit var mC: Context
    val klib = KLib()

    /**
     * データの初期化
     * c            コンテキスト
     * filefolder   トレースデータ保存先フォルダ
     * gpsPath      GPSトレース時のファイル名
     */
    fun init(c: Context, filefolder: String, gpsPath: String) {
        mC = c
        mGpsTraceFileFolder = filefolder
        mGpsPath = gpsPath
        //  トレースが継続中でなければトレースファイルを削除
        if (!klib.getBoolPreferences("GpsTraceContinue", mC)) {
            Log.d(TAG, "init: remove: " + mGpsPath)
            removeGpxFile(mGpsPath)
        }
    }

    /**
     * GPSトレース開始
     * count        継続保存(前回の値に追加)
     */
    fun start(cont: Boolean = false) {
        Log.d(TAG,"start:" + mGpsData.size )
        if (!cont) {
            removeGpxFile(mGpsPath)
            mGpsData.clear()
        }
        mTraceOn = true
        klib.setBoolPreferences(true, "GpsTraceContinue", mC)
        val ldt = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss")
        klib.setStrPreferences(ldt.format(formatter), "GpsTraceStartTime", mC)
    }

    /**
     * GPSトレース終了
     */
    fun end() {
        Log.d(TAG,"end:" + mGpsData.size + " " + mGpsPointData.size)
        mTraceOn = false
        klib.setBoolPreferences(false, "GpsTraceContinue", mC)
    }

    /**
     *  GPS位置情報をトレースす表示する
     *  canvas      描画キャンバス
     *  mapData     地図データクラス
     */
    fun draw(canvas: Canvas, mapData: MapData) {
        if (mTraceOn) {
            //  測定中のGPSデータの表示
            draw(canvas, mGpsPointData, mLineColor, mapData)
        }
        //  既存GPSデータの表示
        var i = 0
        for (gpsData in mGpsPointDatas)
            draw(canvas, gpsData, mLineColors[i++ % mLineColors.size], mapData)
    }

    /**
     * GPS位置情報トレースを地図上に表示(Locationデータ)
     * canvas           地図キャンバス
     * mapData          地図のMapData
     * traceData        GPSトレースの位置情報リスト
     * color            線分の色
     */
    fun draw(canvas: Canvas, mapData: MapData, traceData: List<Location>, color: Int) {
        if (1 < traceData.size) {
            var paint = Paint()
            paint.color = color
            paint.strokeWidth = 6f

            var sbp = PointD(traceData[0].latitude, traceData[0].longitude)
            var sp = mapData.baseMap2Screen(klib.coordinates2BaseMap(sbp))
            for (i in 1..traceData.size - 1) {
                var ebp = PointD(traceData[i].latitude, traceData[i].longitude)
                var ep = mapData.baseMap2Screen(klib.coordinates2BaseMap(ebp))
                canvas.drawLine(sp.x.toFloat(), sp.y.toFloat(), ep.x.toFloat(), ep.y.toFloat(), paint)
                sp = ep
            }
        }
    }

    /**
     * GPS位置情報トレースを地図上に表示(PointDデータ)
     * canvas           地図キャンバス
     * traceData        GPSトレースの位置情報リスト
     * color            線分の色
     * mapData          地図のMapData
     */
    fun draw(canvas: Canvas, traceData: List<PointD>, color: Int, mapData: MapData) {
        if (1 < traceData.size) {
            var paint = Paint()
            paint.color = color
            paint.strokeWidth = 6f

            var sbp = traceData[0]
            var sp = mapData.baseMap2Screen(klib.coordinates2BaseMap(sbp))
            for (i in 1..traceData.size - 1) {
                var ebp = traceData[i]
                var ep = mapData.baseMap2Screen(klib.coordinates2BaseMap(ebp))
                canvas.drawLine(sp.x.toFloat(), sp.y.toFloat(), ep.x.toFloat(), ep.y.toFloat(), paint)
                sp = ep
            }
        }
    }

    /**
     * GPS記録ファイルを削除
     * path     ファイル名(デフォルト:mGpxPath)
     */
    fun removeGpxFile(path: String = mGpsPath) {
        klib.removeFile(path)
    }

    /**
     * GPS記録ファイルに日時ファイル名を付けて指定フォルダに移動
     */
    fun moveGpsFile(moveFolder: String, orgPath: String = mGpsPath) {
        if (klib.existsFile(orgPath)) {
            val traceStarTime = klib.getStrPreferences("GpsTraceStartTime", mC)
            var destPath = moveFolder + "/" + "GPS_" + traceStarTime + ".csv"
            Log.d(TAG, "moveGpsFile: " + orgPath + " " + destPath)
            if (!klib.renameFile(orgPath, destPath)) {
                Log.d(TAG, "moveGpsFile: renameError")
                Toast.makeText(mC, "ファイル保存エラー", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * GPS記録データの読込(GPS Serviceで出力されたCSVファイルの読込)、Locationデータとして取り込む
     * path     ファイル名(デフォルト:mGpxPath)
     */
    fun loadGpsData(path: String = mGpsPath) {
        mGpsData.clear()
        mStepCount.clear()
        var listData = klib.loadCsvData(path)
        for (data in listData) {
            if (data[0].compareTo("DateTime") != 0) {
                var location = Location(LocationManager.GPS_PROVIDER)
                location.time = data[1].toLong()            //  時間(ms)
                location.latitude = data[2].toDouble()      //  緯度
                location.longitude = data[3].toDouble()     //  経度
                location.altitude = data[4].toDouble()      //  高度(m)
                location.speed = data[5].toFloat()          //  速度(m/s)
                location.bearing = data[6].toFloat()        //  方位(度)
                location.accuracy = data[7].toFloat()       //  精度(半径 m)
                mGpsData.add(location)
                if (8 < data.size)
                    mStepCount.add(data[8].toInt())         //  歩数
                else
                    mStepCount.add(0)
            }
        }
    }

    /**
     * GPS記録データを読込、座標データ(PointD))のみ取得
     * path     GPSデータのCSVファイル名
     */
    fun loadGpsPointData(path: String = mGpsPath) {
        mGpsPointData.clear()
        mGpsLap.clear()
        mStepCount.clear()
        var listData = klib.loadCsvData(path)
        for (data in listData) {
            if (data[0].compareTo("DateTime") != 0) {
                var location = PointD(data[3].toDouble(), data[2].toDouble())   //  経度,緯度
                mGpsPointData.add(location)
                mGpsLap.add(data[1].toLong())
                if (8 < data.size)
                    mStepCount.add(data[8].toInt())         //  歩数
                else
                    mStepCount.add(0)
            }
        }
        if (0 < listData.size) {
            mGpsLastElevator = listData.last()[4].toDouble()
        }
    }

    /**
     * 既存データファイルの追加
     * path     ファイルパス(GPSのCSVデータ)
     */
    fun addGpsData(path: String) {
        Log.d(TAG, "addGpsData: "+path)
        mGpsPointDatas.add(loadGpxPointData(path))
//        mGpsDatas.add(loadGpxData(path))
    }

    /**
     * CSV形式の位置情報ファイルの読込Locationデータに変換
     * path     CSVファイルパス
     * return   位置情報リスト(List<Location>)
     */
    fun loadGpxData(path: String): List<Location> {
        var gpxData = mutableListOf<Location>()
        var listData = klib.loadCsvData(path)
        for (data in listData) {
            if (data[0].compareTo("DateTime") != 0) {
                var location = Location(LocationManager.GPS_PROVIDER)
                location.time = data[1].toLong()            //  時間(ms)
                location.latitude = data[2].toDouble()      //  緯度
                location.longitude = data[3].toDouble()     //  経度
                location.altitude = data[4].toDouble()      //  高度(m)
                location.speed = data[5].toFloat()          //  速度(m/s)
                location.bearing = data[6].toFloat()        //  方位(度)
                location.accuracy = data[7].toFloat()       //  精度(半径 m)
                gpxData.add(location)
            }
        }
        return gpxData
    }

    /**
     * CSV形式の位置情報ファイルの読込PointDデータに変換
     * path     CSVファイルパス
     * return   位置情報リスト(List<PointD>)
     */
    fun loadGpxPointData(path: String = mGpsPath): List<PointD> {
        var gpxData = mutableListOf<PointD>()
        var listData = klib.loadCsvData(path)
        for (data in listData) {
            if (data[0].compareTo("DateTime") != 0) {
                var location = PointD(data[3].toDouble(), data[2].toDouble())   //  経度,緯度
                gpxData.add(location)
            }
        }
        return gpxData;
    }

    /**
     * GPSの最新の位置を取得
     */
    fun lastPosition(): PointD {
        Log.d(TAG,"lastPosition: "+mGpsPointData.size)
        if (0 < mGpsPointData.size)
            return mGpsPointData.last()
        else if (0 < mGpsData.size)
            return PointD(mGpsData[mGpsData.size - 1].longitude, mGpsData[mGpsData.size - 1].latitude)
        else
            return PointD()
    }

    /**
     * GPSデータをCSV形式でフォルダに保存
     * ファイル名は日時で設定
     * folder       フォルダパス
     */
    fun saveGpsDataFolder(folder: String) {
        Log.d(TAG, "saveGpsDataFolder: " + mGpsData.size)
        if (0 < mGpsData.size) {
            var path = folder + "/" + "GPS_" +
                LocalDateTime.ofEpochSecond(mGpsData[0].time / 1000 + 9 * 3600, 0, ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +".csv"
            saveGpsData(path)
        }
    }

    /**
     * GPSデータをCSV形式でファイルに保存
     * path     ファイルパス
     */
    fun saveGpsData(path: String) {
        val listData = mutableListOf<String>()
        var title = "DateTime,Time,Latitude,Longtude,Altitude,Speed,Bearing,Accuracy,StepCount"
        listData.add(title)
        var count = 0
        for (location in mGpsData) {
            location.let {
                var buffer =
                    LocalDateTime.ofEpochSecond(it.time / 1000 + 9 * 3600, 0, ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                buffer += "," + it.time.toString()          //  時間(UTC Time)(ms)
                buffer += "," + it.latitude.toString()      //  緯度(度)
                buffer += "," + it.longitude.toString()     //  経度(度)
                buffer += "," + it.altitude.toString()      //  高度(m)
                buffer += "," + it.speed.toString()         //  速度(m/s)
                buffer += "," + it.bearing.toString()       //  方位(度)
                buffer += "," + it.accuracy.toString()      //  精度(m半径)
                if (count < mStepCount.size)                //  歩数(カウンタ値)
                    buffer += "," + mStepCount[count]
                else
                    buffer += ",0"
                listData.add(buffer)
            }
            count++
        }
        klib.saveTextData(path, listData)
    }

    /**
     * GPSデータをGPX形式でファイル保存
     * folder       保存先フォルダー名
     * gpxFileName  変換先ファイル名(省略時は開始時刻によるファイル名)
     */
    fun saveGps2GpxFolder(folder: String, gpxFileName: String = "") {
        Log.d(TAG, "saveGps2GpxFolder: " + mGpsData.size)
        if (0 < mGpsData.size) {
            var path = if (0 < gpxFileName.length) folder + "/" + gpxFileName
                        else folder + "/" + "GPS_" +
                LocalDateTime.ofEpochSecond(mGpsData[0].time / 1000 + 9 * 3600, 0, ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +".gpx"
            Log.d(TAG, "saveGps2GpxFolder: " + path)
            var gpxWriter = GpxWriter()
            gpxWriter.mGpxHeaderCreater = "MapApp GPS Logger for Android"
            gpxWriter.writeDataAll(path, mGpsData)
        }
    }

    /**
     * GPSトレースデータの情報取得
     */
    fun getInfoData(): String {
        if (0 < mGpsData.size) {
            var lapTime = mGpsData[mGpsData.size - 1].time - mGpsData[0].time
            var distance = totalDistance()
            var maxEle = maxElevator()
            var minEle = minElevator()
            var buffer = "開始日時: " +
                LocalDateTime.ofEpochSecond(mGpsData[0].time / 1000 + 9 * 3600, 0, ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            buffer += "\n終了日時: " +
                LocalDateTime.ofEpochSecond(mGpsData[mGpsData.size - 1].time / 1000 + 9 * 3600, 0, ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            buffer += "\n経過時間: " + klib.lap2String(lapTime)
            buffer += "\n移動距離: " + "%,.2f km".format(distance)
            buffer += "\n平均速度: " + "%,.1f km/h".format((distance / (lapTime / 1000.0 / 3600.0)))
            buffer += "\n平均ペース: " + "%,.2f min/km".format(((lapTime / 1000.0 / 60.0) / distance))
            buffer += "\n最大高度: " + "%,.0f m".format(maxEle)
            buffer += "\n最小高度: " + "%,.0f m".format(minEle)
            buffer += "\n標高差: " + "%,.0f m".format((maxEle - minEle))
            buffer += "\n歩数: " + "%,d".format(stepCount())
            buffer += "\nデータ数: " + mGpsData.size
            return buffer
        }
        return ""
    }

    /**
     * 経過時間(sec)
     */
    fun lastLap(): Double {
        return (mGpsLap[mGpsPointData.lastIndex] - mGpsLap[0]) / 1000.0
    }

    /**
     * 最新速度(km/h)
     * aveSize      移動平均のデータサイズ(1以上)
     * return       速度(km/h)
     */
    fun lastSpeed(aveSize: Int = 1): Double {
        val last = mGpsPointData.lastIndex
        if (1 < last) {
            val st = max(last - aveSize + 1, 2)
            var sum = 0.0
            for (i in st..last) {
                val distance = klib.cordinateDistance(mGpsPointData[last - 1], mGpsPointData[last])   //  (km)
                val lap = (mGpsLap[last] - mGpsLap[last - 1]) / 1000.0 / 3600.0                       //  (h)
                sum += if (lap <= 0) 0.0 else distance / lap
            }
            return sum / (last - st + 1)
        } else {
            return 0.0
        }
    }

    /**
     * 歩数(step count)
     */
    fun stepCount(): Int {
        return mStepCount[mStepCount.size - 1] -  mStepCount[0]
    }

    /**
     * トレースの領域を求める
     */
    fun traceArea(): RectD {
        var area = RectD()
        if (0 < mGpsPointData.size) {
            area = RectD(mGpsPointData[0], mGpsPointData[0])
            for (i in 1..mGpsPointData.lastIndex) {
                area.extension(mGpsPointData[i])
            }
        } else if (0 < mGpsData.size) {
            var p0 = PointD(mGpsData[0].longitude, mGpsData[0].latitude)
            area = RectD(p0, p0)
            for (i in 1..mGpsData.lastIndex) {
                var p = PointD(mGpsData[i].longitude, mGpsData[i].latitude)
                area.extension(p)
            }
        }
        return area;
    }


    /**
     * 累積距離(km)
     */
    fun totalDistance(): Double {
        var distance = 0.0
        if (0 < mGpsPointData.size) {
            for (i in 1..mGpsPointData.lastIndex) {
                distance += klib.cordinateDistance(mGpsPointData[i - 1], mGpsPointData[i])
            }
        } else if (0 < mGpsData.size) {
            for (i in 1..mGpsData.lastIndex) {
                distance += klib.cordinateDistance(mGpsData[i - 1].longitude, mGpsData[i - 1].latitude,
                    mGpsData[i].longitude, mGpsData[i].latitude)
            }
        }
        return distance
    }

    /**
     * 最大高度(m)
     */
    fun maxElevator(): Double {
        var maxEle = mGpsData[0].altitude
        for (i in 1..(mGpsData.size - 1)) {
            maxEle = Math.max(maxEle, mGpsData[i].altitude)
        }
        return maxEle
    }

    /**
     * 最小高度(m)
     */
    fun minElevator(): Double {
        var minEle = mGpsData[0].altitude
        for (i in 1..(mGpsData.size - 1)) {
            minEle = Math.min(minEle, mGpsData[i].altitude)
        }
        return minEle
    }

    /**
     * 累積歩数
     */
    fun totalStep(): Int {
        var count = 0
        for (stepCount in mStepCount)
            count += stepCount
        return count
    }

    /**
     * GPSトレースデータの情報表示
     */
    fun gpsTraceInfo() {
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        var fileNameList = mutableListOf<String>()
        if (mTraceOn && klib.existsFile(klib.getPackageNameDirectory(mC) + "/" + GpsService.mGpsFileName))
            fileNameList.add("トレース中データ")
        for (file in listFile)
            fileNameList.add(file.nameWithoutExtension)
        klib.setMenuDialog(mC, "情報表示ファイルリスト", fileNameList, iGpsFileInfo)
    }

    //  選択ファイルのの情報表示
    //  s       選択ファイル名
    var iGpsFileInfo = Consumer<String> { s ->
        var gpsTrace = GpsTrace()
        if (s.compareTo("トレース中データ") == 0) {
            gpsTrace.loadGpsData(klib.getPackageNameDirectory(mC) + "/" + GpsService.mGpsFileName)
        } else {
            gpsTrace.loadGpsData(mGpsTraceFileFolder + "/" + s + ".csv")
        }
        klib.messageDialog(mC, s, gpsTrace.getInfoData())
    }

    /**
     * GPSトレースファイルのファイル名変更
     */
    fun gpsTraceFileRename() {
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        var fileNameList = mutableListOf<String>()
        for (file in listFile)
            fileNameList.add(file.nameWithoutExtension)
        klib.setMenuDialog(mC, "ファイル名変更ファイルリスト", fileNameList, iGpsTraceFileRename)
    }

    //  ファイル名変更処理(ファイル名変更ダイヤログ表示)
    //  s       選択ファイル名
    var iGpsTraceFileRename = Consumer<String> { s ->
        mTempFileNameList.clear()
        mTempFileNameList.add(s)
        klib.setInputDialog(mC, "データ名変更", s, iGpsTraceFileRenameOperation)
    }

    //  ファイル名の変更処理
    //  s       変更後のファイル名
    var iGpsTraceFileRenameOperation = Consumer<String> { s ->
        if (0 < mTempFileNameList.size && 0 < s.length) {
            var srcFileName = mGpsTraceFileFolder + "/" + mTempFileNameList[0] + ".csv"
            var destFileName = mGpsTraceFileFolder + "/" + s + ".csv"
            klib.renameFile(srcFileName, destFileName)
        }
    }
    /**
     * GPSトレースファイルの移動
     */
    fun gpsTraceFileMove() {
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        var fileNameList = Array<String>(listFile.size, { i -> listFile[i].nameWithoutExtension })
        var chkList = BooleanArray(listFile.size)
        klib.setChkMenuDialog(mC, "移動ファイルリスト", fileNameList, chkList, iGpsTraceFileMove)
    }

    //  GPSトレースファイル選択表示
    //  s       選択ファイルのチェックリスト
    var iGpsTraceFileMove = Consumer<BooleanArray> { s ->
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        mTempFileNameList.clear()
        for (i in s.indices) {
            if (s[i])
                mTempFileNameList.add(listFile[i].nameWithoutExtension)
        }
        setDataMoveDialog(mC, "移動先フォルダ", mGpsTraceFileFolder, iGpsTraceFileMoveOperation)
    }

    //  GPSトレースファイルの移動処理
    //  s       移動先フォルダ名
    var iGpsTraceFileMoveOperation = Consumer<String> { s ->
        var targetFolder =  mGpsTraceFileFolder + "/" + s
        if (klib.mkdir((targetFolder))) {
            for (fileName in mTempFileNameList) {
                var sfileName = mGpsTraceFileFolder + "/" + fileName + ".csv"
                klib.moveFile(sfileName, targetFolder)
            }
        }
    }

    /**
     * GPSトレースデータファイルの削除
     * チェックボックスで複数削除可
     */
    fun gpsTraceRemove() {
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        var fileNameList = Array<String>(listFile.size, { i -> "" })
        var chkList = BooleanArray(listFile.size)
        //  リスト作成
        for (i in 0..(listFile.size - 1)) {
            fileNameList[i] = listFile[i].nameWithoutExtension +
                    "  [" + klib.size2String(listFile[i].length().toDouble(),1024.0) + "B]"
            chkList[i] = false
        }
        //  削除ファイル選択
        klib.setChkMenuDialog(mC, "削除ファイルリスト", fileNameList, chkList, iGpsFilesRemove)
    }

    //  GPSトレースデータファイルの削除(csvとgpx同時削除)
    //  s       選択ファイルのチェックリスト
    var iGpsFilesRemove = Consumer<BooleanArray> { s ->
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.lastModified() }
        //  ファイル削除
        for (i in 0..(s.size - 1)) {
            if (s[i]) {
                var gpxfile = listFile[i].path.replace(".csv", ".gpx")
                klib.removeFile(gpxfile)
                listFile[i].delete()
            }
        }
    }

    /**
     * データ移動ダイオログ
     * フォルダ名を選択または入力してデータを移動する
     * c            コンテキスト
     * title        ダイヤログのタイトル
     * curFoilde    カレントフォルダ
     * operation    フォルダ選択後の処理関数インターフェース
     */
    fun setDataMoveDialog(c: Context, title: String, curFolder: String, operation: Consumer<String>) {
        val linearLayout = LinearLayout(c)
        val folderLabel = TextView(c)
        var etFolderTitle = EditText(c)
        var spFolder = Spinner(c)

        linearLayout.orientation = LinearLayout.VERTICAL
        folderLabel.setText("フォルダ名")

        var folderList = klib.getDirList(curFolder)
        var folderAdapter = ArrayAdapter(c, R.layout.support_simple_spinner_dropdown_item, folderList)
        spFolder.adapter = folderAdapter
        spFolder.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etFolderTitle.setText(folderList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        linearLayout.addView(folderLabel)
        linearLayout.addView(etFolderTitle)
        linearLayout.addView(spFolder)

        val dialog = AlertDialog.Builder(c)
        dialog.setTitle(title)
        dialog.setView(linearLayout)
        dialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            operation.accept(etFolderTitle.text.toString())
        })
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    /**
     * GPSトレースのデータをGPXに変換してエキスポートする
     * チェックリストでファイルを選択
     */
    fun gpsTraceExport() {
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.nameWithoutExtension }
        var fileNameList = Array<String>(listFile.size, { i -> listFile[i].nameWithoutExtension })
        var chkList = BooleanArray(listFile.size)
        klib.setChkMenuDialog(mC, "ファイルリスト", fileNameList, chkList, iGpsTraceExport)
    }

    //  出力先フォルダを選択または入力
    //  s       ファイルのチェックリスト
    var iGpsTraceExport = Consumer<BooleanArray> { s ->
        var listFile = klib.getFileList(mGpsTraceFileFolder, false, "*.csv")
        listFile = listFile.sortedByDescending { it.nameWithoutExtension }
        //  選択ファイルリスト作成
        mTempFileNameList.clear()
        for (i in s.indices) {
            if (s[i])
                mTempFileNameList.add(listFile[i].nameWithoutExtension)
        }
        //  出力先フォルダ選択
        klib.folderSelectDialog(mC, mGpsTraceFileFolder, iGpsTraceExportOperation)
    }

    //  選択されたトレースデータをGPXファイルに変換
    //  s       出力先フォルダ
    var iGpsTraceExportOperation = Consumer<String> { s ->
//        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        Log.d(TAG, "iGpsTraceExportOperation: " + s)
        var targetFolder = s
        if (klib.mkdir((targetFolder))) {
            Toast.makeText(mC, "ファイルの変換を開始します。", Toast.LENGTH_LONG).show()
            mGpxConvertOn = true
            //  ファイル変換を非同期処理
            mGpxConvertJob = GlobalScope.launch {
                for (fileName in mTempFileNameList) {
                    Log.d(TAG,"iGpsTraceFileMoveOperation: " + s + " " + fileName)
                    var sfileName = mGpsTraceFileFolder + "/" + fileName + ".csv"
                    loadGpsData(sfileName)
                    saveGps2GpxFolder(targetFolder, fileName + ".gpx")
                }
                mGpxConvertOn = false
//                Toast.makeText(mC, "ファイルの変換が完了しました。", Toast.LENGTH_LONG).show()
            }
        }
    }
}