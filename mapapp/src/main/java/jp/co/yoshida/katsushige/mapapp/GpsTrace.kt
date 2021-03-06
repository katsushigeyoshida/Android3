package jp.co.yoshida.katsushige.mapapp

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationManager
import android.util.Log
import jp.co.yoshida.katsushige.mylib.GpxWriter
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 *  GPSでの取得位置をデータ保存し、トレースを表示する
 */
class GpsTrace {
    val TAG = "GpsTrace"
    var mTraceOn = false
    var mSaveOn = true;
    var mGpsxPath = ""
    var mGpsData = mutableListOf<Location>()
    var mStepCount = mutableListOf<Int>()
    var mGpsPaths = mutableListOf<String>()
    var mGpsDatas = mutableListOf<List<Location>>()
    var mLineColor = Color.GREEN
    var mLineColors = listOf(
        Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.YELLOW, Color.GREEN)

    val klib = KLib()

    /**
     * GPSトレース開始
     * count        前回の値を使う
     */
    fun start(cont: Boolean = false, saveOn: Boolean = true) {
        Log.d(TAG,"start:" + mGpsData.size + " save: " + saveOn)
        if (!cont) {
            removeGpxFile(mGpsxPath)
            mGpsData.clear()
        }
        mTraceOn = true
        mSaveOn = saveOn;
    }

    /**
     * GPSトレース終了
     */
    fun end() {
        Log.d(TAG,"end:" + mGpsData.size)
        mTraceOn = false
    }

    /**
     *  GPS位置情報をトレースす表示する
     *  canvas      描画キャンバス
     *  mapData     地図データクラス
     */
    fun draw(canvas: Canvas, mapData: MapData) {
        if (mTraceOn && mSaveOn) {
            //  測定中のGPSデータの表示
            draw(canvas, mapData, mGpsData, mLineColor)
        }
        //  既存GPSデータの表示
        var i = 0
        for (gpsData in mGpsDatas)
            draw(canvas, mapData, gpsData, mLineColors[i++ % mLineColors.size])
    }

    /**
     * GPS位置情報トレースを地図上に表示
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

            var sbp = PointD(traceData[0].longitude, traceData[0].latitude)
            var sp = mapData.baseMap2Screen(klib.coordinates2BaseMap(sbp))
            for (i in 1..traceData.size - 1) {
                var ebp = PointD(traceData[i].longitude, traceData[i].latitude)
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
    fun removeGpxFile(path: String = mGpsxPath) {
        klib.removeFile(path)
    }

    /**
     * GPS記録データの読込(GPS Serviceで出力されたCSVファイルの読込)
     * path     ファイル名(デフォルト:mGpxPath)
     */
    fun loadGpsData(path: String = mGpsxPath) {
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
     * 既存データの追加
     * path     ファイルパス(GPSのCSVデータ)
     */
    fun addGpsData(path: String) {
        Log.d(TAG, "addGpsData: "+path)
        mGpsDatas.add(loadGpxData(path))
    }

    /**
     * CSV形式の位置情報ファイルの読込
     * path     CSVファイルパス
     * return   位置情報リスト
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
     * GPSの最新の位置を取得
     */
    fun lastPosition(): PointD {
        Log.d(TAG,"lastPosition: "+mGpsData.size)
        if (0 < mGpsData.size)
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
        if (mSaveOn && 0 < mGpsData.size) {
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
     * folder   保存先フォルダー名
     */
    fun saveGps2GpxFolder(folder: String) {
        Log.d(TAG, "saveGps2GpxFolder: " + mGpsData.size)
        if (mSaveOn && 0 < mGpsData.size) {
            var path = folder + "/" + "GPS_" +
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
     * 歩数(step count)
     */
    fun stepCount(): Int {
        return mStepCount[mStepCount.size - 1] -  mStepCount[0]
    }

    /**
     * 累積距離(km)
     */
    fun totalDistance(): Double {
        var distance = 0.0
        for (i in 1..(mGpsData.size - 1)) {
            distance += klib.cordinateDistance(mGpsData[i - 1].longitude, mGpsData[i - 1].latitude,
                mGpsData[i].longitude, mGpsData[i].latitude)
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
}