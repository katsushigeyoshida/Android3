package jp.co.yoshida.katsushige.mylib

import android.location.Location

class GpxWriter {

    var mFilePath = ""
    var mGpxHeaderCreater = "GPS Logger for Android"

    val klib = KLib()

    /**
     * 保存ファイル名の設定
     */
    fun Open(path: String) {
        mFilePath = path
    }

    /**
     * GPXファイルのヘッダ部作成保存
     * creater      Creater名(default: GPS Logger for Android)
     * name         track名(default: なし)
     */
    fun init(creater: String = "",name: String = "") {
        // GPXヘッダ作成
        mGpxHeaderCreater = if (0 < creater.length) creater else mGpxHeaderCreater
        var buffer = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
//        buffer += "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\">\n"
        buffer += "<gpx version=\"1.0\" creator=\"" + mGpxHeaderCreater + "\">\n"
        buffer += "<trk>\n"
        if (0 < name.length)
            buffer += "<name>" + name + "</name>\n"
        buffer += "<trkseg>\n"
        klib.writeFileData(mFilePath, buffer)
    }

    /**
     * Loactionデータの追加
     * location     位置情報(latitude,longtude,altitude,time)
     */
    fun appendData(location: Location) {
        // 位置データ
        var buffer = "<trkpt lat=\"" + location.latitude.toString() +
                        "\" lon=\"" + location.longitude.toString() + "\">"
        buffer += "<ele>" + location.altitude.toString() + "</ele>"
        buffer += "<time>" + klib.getLocationTime(location) + "</time>"
        buffer += "</trkpt>"
        buffer += "\n"
        klib.writeFileDataAppend(mFilePath, buffer)
    }

    /**
     * GPXファイルの囚虜処理
     */
    fun close() {
        // 終了コード出力
        var buffer = "</trkseg>\n";
        buffer += "</trk>\n";
        buffer += "</gpx>";
        buffer += "\n";
        klib.writeFileDataAppend(mFilePath, buffer)
    }

    /**
     * GPSデータの一括書き込み
     * path         GPXファイルパス
     * locations    位置情報リスト(List<Location>)
     */
    fun writeDataAll(path: String, locations: List<Location>) {
        mFilePath = path
        init()
        for (location in locations) {
            appendData(location)
        }
        close()
    }
}