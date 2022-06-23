package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import kotlin.math.*

/**
 * 天体関連の処理関数
 * ----  ファイル処理  -----
 * meargeTitle(starData: List<List<String>>): List<List<String>>
 * meargeTitle(starData: List<List<String>>, convTitle: List<String>): List<List<String>>
 * convTitleData(title: List<String>, convCol: List<Int>): List<String>
 * convDataList(starData: List<List<String>>, convCol: List<Int>, titleData: List<String>): List<List<String>>
 * getConvCol(starDataTitle: List<String>, convTitle: List<String>, b: Boolean = false): List<Int>
 * getConvCol2(starDataTitle: List<String>): List<Int>
 * loadData(filePath: String, encode: Int=1): List<List<String>>
 * ---  日付・時間関連  ------
 * getJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int, nMin: Int, nSec: Int): Double
 * julianDay(y: Int, m: Int, d: Int): Int
 * getMJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int, nMin: Int, nSec: Int): Double
 * getGreenwichSiderealTime(nYear: Int, nMonth: Int, nDay: Int, nHour: Int, nMin: Int, nSec: Int): Double
 * getLocalSiderealTime(dSiderealTime: Double, dLongitude: Double): Double
 * ---  座標変換関連  ------
 * convHorizontalPoint(coordinate: PointD, lst: Double, localLatitude: Double, direction: Double, radius: Double, full: Boolean = false): PointD
 * cnvFullHorizontal(azimuth: Double, height: Double, direction:Double, radius:Double): PointD
 * cnvHorizontal(azimuth: Double, height: Double,direction: Double, radius: Double): PointD
 * equatorial2orthogonal(coordinate: PointD, direction: Double, radius: Double): PointD
 * declinationLength(declination: Double, radius: Double): Double
 * horizonHeight(hourAngle: Double, declination: Double, latitude:Double): Double
 * horizonAzimus(hourAngle: Double, declination: Double, latitude:Double): Double
 * equatorialDeclination(azimuth: Double, height: Double, latitude: Double): Double
 * equatorialHourAngle(azimuth: Double, height: Double, latitude: Double): Double
 * equatorialRightAscension(azimuth: Double, height: Double, latitude: Double, lst: Double): Double
 * ---  単位変換関連  ------
 * R2D(rad: Double): Double
 * D2R(deg: Double): Double
 * H2D(hour: Double): Double
 * D2H(deg: Double): Double
 * H2R(hour:Double): Double
 * R2H(rad:Double): Double
 * deg2DMS(deg: Double): String
 * DMS2deg(dms: String): Doubl
 * hour2HMS(hour: Double): String
 * HMS2hour(hms: String): Double
 * hhmmss2hour(hhmmss:String): Double
 * DMS2deg(d: Int, m: Int, s: Double):Double
 * HMS2hour(h: Int, m: Int, s: Double): Double
 * HM2hour(hm: String):Double
 * DM2deg(dm: String): Double
 * dm2deg(dm: String): Double
 * hm2hour(hm: String): Double
 *
 */
class AstroLib {
    val TAG = "AstroLib"

    val klib = KLib()

    //  ----  ファイル処理  -----

    val mCnvDataTitle = listOf<List<String>> (
        listOf<String> ( "HIP番号", "HIP", "HIP番号：1～120416" ),
        listOf<String> ( "赤経：時分秒", "赤経", "RA" ),
        listOf<String> ( "赤緯：±度分秒", "赤緯", "Dec" ),
        listOf<String> ( "視等級", "視等級(小数)", "Vmag", "V等級", "視等級：等級（小数）"  ),
        listOf<String> ( "Name/ASCII", "Name/Diacritics", "固有名", "固有名（英語）", "恒星名", "星名", "IAU Name" ),
        listOf<String> ( "カタカナ表記", "固有名（日本語）", "恒星名(日本語)" ),
        listOf<String> ( "Constellation", "Con", "星座","星座：略符", "星座名" )
    )
    val mConvTitle = listOf<String> (
        "赤経：時（整数）","赤経：分（整数）","赤経：秒（小数）",                       //  0 hip_100,hip_lite...
        "赤緯：符号（0：- 1：+）","赤緯：度（整数）","赤緯：分（整数）","赤緯：秒（小数）",//  3 hip_100,hip_lite...
        "HIP番号",                                                             //  7 hip_100,hip_lite...
        "赤経:時", "赤経:分（整数）", "赤経:分（小数第一位）",                        //  8 メシエカタログ
        "赤緯:符号 0:-、1:+", "赤緯:度 0°～90°", "赤緯:分 1分=1/60°",              //  11 メシエカタログ
        "天体種別 1:銀河、2:星団、3:星雲、0:その他(M40:二重星、M73:星群)",            //  14 メシエカタログ
        "RA(J2000)", "Dec(J2000)",                                           //  15 IAU Catalog
        "赤経(hhmmss)", "赤緯(±ddmm.m)",                                        //  17 天文年恒星表
        "赤経(hhmm.m)", "赤緯(±ddmm)",                                          //  19 理科年表恒星表
        "赤経(hhmm)", "赤緯(±dd)",                                              //  21 理科年表星座
        "赤経(h:m)", "赤緯(deg:m)"                                              //  23 メシエ天体の一覧
    )
    //  恒星表タイトル置換えタイトルデータ
    val mArrangeTitle = listOf<String>( "赤経：時分秒", "赤緯：±度分秒", "HIP番号", "天体種別"  )
    //  星雲や銀河などの種類
    private val mAstroType = listOf<String>("その他(M40:二重星、M73:星群)", "銀河", "星団", "星雲")

    /**
     * 分轄された赤経・赤緯のデータを統合したデータに変換する
     * starData     恒星データ
     * return       赤経・赤緯の統合データ
     */
    fun meargeTitle(starData: List<List<String>>): List<List<String>> {
        return meargeTitle(starData, mConvTitle)
    }

    /**
     *  分かれている時分秒,度分秒のデータをマージして一つの時分秒,度分秒のデータに変換する
     *  starData        恒星データ
     *  retun.rn        変換データ(時分秒,度分秒をマージ)
     */
    fun meargeTitle(starData: List<List<String>>, convTitle: List<String>): List<List<String>> {
        val convCol = getConvCol(starData[0], convTitle)            //  赤経、赤緯の変換リスト
        val titleData = convTitleData(starData[0], convCol)         //  赤経、赤緯のタイトル結合
        val destData = convDataList(starData, convCol, titleData)   //  赤経、赤緯のデータ結合

        return destData
    }

    /**
     * データタイトルをマージするタイトルら変換(赤経・赤緯の分割データをマージ)
     * title        タイトルリスト
     * convCol      タイトル変換列リスト
     * return       変換後のタイトルリスト
     */
    fun convTitleData(title: List<String>, convCol: List<Int>): List<String> {
        var titleList = mutableListOf<String>()
        for (i in 0..title.lastIndex) {
            if (i == convCol[0]) {                  //  赤経：時（整数）
                titleList.add(mArrangeTitle[0])     //  赤経：時分秒
            } else if (i == convCol[1] || i == convCol[2]) {
            } else if (i == convCol[3] || (convCol[3] < 0 && i == convCol[4])) { //  赤緯：符号（0：- 1：+）
                titleList.add(mArrangeTitle[1])     //  赤緯：±度分秒
            } else if ((0 <= convCol[3] && i == convCol[4]) || i == convCol[5] || i == convCol[6]) {
            } else if (i == convCol[7]) {           //  HIP番号
                titleList.add(mArrangeTitle[2])     //  HIP番号
            } else if (i == convCol[8]) {           //  赤経:時
                titleList.add(mArrangeTitle[0])     //  赤経：時分秒
            } else if (i == convCol[9]) {
            } else if (i == convCol[10]) {          //  赤緯:符号 0:-、1:+
                titleList.add(mArrangeTitle[1])     //  赤緯：±度分秒
            } else if (i == convCol[11] || i == convCol[12] || i == convCol[13]) {
            } else if (i == convCol[14]) {          //  天体種別 1:銀河、2:星団、3:星雲、0:その他(M40:二重星、M73:星群
                titleList.add(mArrangeTitle[3])     //  天体種別
            } else if (i == convCol[15] || i == convCol[17] || i == convCol[19] || i == convCol[21] || i == convCol[23]) {  //  赤経(xxx)
                titleList.add(mArrangeTitle[0])     //  赤経：時分秒
            } else if (i == convCol[16] || i == convCol[18] || i == convCol[20] || i == convCol[22] || i == convCol[24]) {  //  赤緯(xxx)
                titleList.add(mArrangeTitle[1])     //  赤緯：±度分秒
            } else {
                titleList.add(title[i]);
            }
        }
        return titleList
    }

    /**
     * 赤経・赤緯の分割データをマージしたデータに変換
     * starData     恒星データリスト
     * convCol      列対応データ
     * titleData    タイトルデータ
     * retrn        変換データ
     */
    fun convDataList(starData: List<List<String>>, convCol: List<Int>, titleData: List<String>): List<List<String>> {
        //  HIP番号の最大文字長さ(数値の桁を揃えるため))
        var maxHipStrLen = 0
        if (0 <= convCol[7]) {
            for (row in 1..starData.lastIndex) {
                maxHipStrLen = max(maxHipStrLen, starData[row][convCol[7]].length)
            }
        }
        //  データの登録
        var starStrData = mutableListOf<List<String>>()
        starStrData.add(titleData)
        for (row in 1..starData.lastIndex) {
            var buf = mutableListOf<String>()
            var ra = ""
            var dec = ""
            for (col in 0..starData[row].lastIndex) {
                if (convCol[0] == col) {            //  赤経：時（整数
                    ra = starData[row][col].padStart(2, '0') + "h"
                } else if (convCol[1] == col) {     //  赤経：分（整数）
                    ra += starData[row][col].padStart(2, '0') + "m"
                } else if (convCol[2] == col) {     //  赤経：秒（小数）
                    ra += starData[row][col].padStart(2, '0') + "s"
                    buf.add(ra);
                } else if (convCol[3] == col) {     //  赤緯：符号（0：- 1：+）
                    dec = if (starData[row][col].compareTo("0") == 0) "-" else "+"
                } else if (convCol[4] == col) {     //  赤緯：度（整数）
                    if (convCol[3] < 0) {
                        dec += (if (starData[row][col][0] == '-') "" else
                            (if (starData[row][col][0] == '+') "" else "+")) + starData[row][col] + "°"
                    } else {
                        dec += starData[row][col].padStart(2, '0') + "°"
                    }
                } else if (convCol[5] == col) {     //  赤緯：分（整数）
                    dec += starData[row][col].padStart(2, '0') + "′"
                } else if (convCol[6] == col) {     //  赤緯：秒（小数）
                    var l = 2
                    if (0 <= starData[row][col].indexOf('.'))
                        l += starData[row][col].length - starData[row][col].indexOf('.')
                    dec += starData[row][col].padStart(l, '0') + "″"
                    buf.add(dec)
                } else if (convCol[7] == col) {     //  HIP番号
//                 || 0 <= starData[0][col].indexOf(mArrangeTitle[2])) {      //  HIP番号
                    buf.add(starData[row][col].padStart(maxHipStrLen, '0'))
                } else if (convCol[8] == col) {     //  "赤経:時
                    ra = starData[row][col].padStart(2, '0') + "h"
                } else if (convCol[9] == col) {
                    ra += starData[row][col].padStart(2, '0') + "m"
                } else if (convCol[10] == col) {
                    ra += (klib.str2Double(starData[row][col]) * 6.0).toString().padStart(2, '0') + "s"
                    buf.add(ra)
                } else if (convCol[11] == col) {        //  赤緯:符号 0:-、1:+
                    dec = if (starData[row][col].compareTo("0") == 0) "-" else "+"
                } else if (convCol[12] == col) {
                    dec += starData[row][col].padStart(2, '0') + "°"
                } else if (convCol[13] == col) {
                    dec += starData[row][col].padStart(2, '0') + "′"
                    buf.add(dec)
                } else if (convCol[14] == col) {        //  天体種別
                    buf.add(mAstroType[(starData[row][col].toIntOrNull()?:0) % mAstroType.size])
                } else if (convCol[15] == col) {        //  RA(J2000)度(ddd.dddd)
                    buf.add(hour2HMS(D2H(starData[row][col].toDoubleOrNull()?:0.0)))
                } else if (convCol[16] == col) {        //  Dec(J2000)度(ddd.dddd)
                    buf.add(deg2DMS(starData[row][col].toDoubleOrNull()?:0.0))
                } else if (convCol[17] == col) {        //  赤経(hhmmss)
                    buf.add(hour2HMS(hhmmss2hour(starData[row][col])))
                } else if (convCol[18] == col) {        //  赤緯(±ddmm.m)
                    buf.add(deg2DMS(DM2deg(starData[row][col])))
                } else if (convCol[19] == col) {        //  赤経(hhmm.m)
                    buf.add(hour2HMS(HM2hour(starData[row][col])))
                } else if (convCol[20] == col) {        //  赤緯(±ddmm)
                    buf.add(deg2DMS(DM2deg(starData[row][col])))
                } else if (convCol[21] == col) {        //  赤経(hhmm)
                    buf.add(hour2HMS(HM2hour(starData[row][col])))
                } else if (convCol[22] == col) {        //  赤緯(±dd)
                    buf.add(deg2DMS(DM2deg(starData[row][col])))
                } else if (convCol[23] == col) {        //  赤経(h:m)
                    buf.add(hour2HMS(hm2hour(starData[row][col])));
                } else if (convCol[24] == col) {        //  赤緯(deg:m)
                    buf.add(deg2DMS(dm2deg(starData[row][col])));
                } else {
                    buf.add(starData[row][col])
                }
            }
            starStrData.add(buf)
        }
        return starStrData
    }

    /**
     * タイトル行から赤経、赤緯の分割データ位置の検索
     * starDataTitle    変換元データタイトル
     * convTitle        変換対象タイトル候補
     */
    fun getConvCol(starDataTitle: List<String>, convTitle: List<String>, b: Boolean = false): List<Int> {
        var convCol = mutableListOf<Int>()
        for (i in 0..convTitle.lastIndex)
            convCol.add(-1)
        for (i in 0..convTitle.lastIndex) {
            for (j in 0..starDataTitle.lastIndex) {
                var n = starDataTitle[j].indexOf(convTitle[i])
                if (0 <= n && !convCol.contains(j)) {
                    convCol[i] = j
                    break
                }
            }
        }
        return convCol
    }

    /**
     * タイトル行から赤経、赤緯の分割データ位置の検索
     * starDataTitle    変換元データタイトル
     * convTitle        変換対象タイトル候補(2次元リスト)
     * b                種類ごとに設定(defualt:しない)
     */
    fun getConvCol2(starDataTitle: List<String>): List<Int> {
        var convCol = mutableListOf<Int>()
        for (i in 0..mCnvDataTitle.lastIndex)
            convCol.add(-1)
        for (i in 0..mCnvDataTitle.lastIndex) {
            for (j in 0..mCnvDataTitle[i].lastIndex) {
                for (k in 0..starDataTitle.lastIndex) {
                    if (0 <= starDataTitle[k].indexOf(mCnvDataTitle[i][j])) {
                        convCol[i] = k
                        break
                    }
                }
                if (0 <= convCol[i])
                    break
            }
        }
        return convCol
    }

    /**
     * CSV形式のリストデータをファイルから読み込んでmDataListに格納する
     * filePath     ファイルのパス
     * encode       エンコード(0:UTF_8, 1:SJIS, 2:EUC-JP)
     * return       データリスト
     */
    fun loadData(filePath: String, encode: Int=1): List<List<String>> {
        var dataList = mutableListOf<List<String>>()
        if (klib.existsFile(filePath)) {
            klib.mFileEncode = encode
            val fileDataList = klib.loadCsvData(filePath)
            for (data in fileDataList) {
                if (0 < data.maxByOrNull { it.length }?.length?:0) {
                    if (!(0 < data[0].length && data[0][0] == '#')) {   //  先頭の'#'があればコメント行として除く
                        var buf = mutableListOf<String>()
                        for (i in data.indices)
                            buf.add(data[i].trim())
                        dataList.add(buf)
                    }
                }
            }
        }
        return dataList
    }

    //  ---  日付・時間関連  ------

    /**
     * ユリウス日(紀元前4713年(-4712年)1月1日が0日)の取得
     * (https://www.dinop.com/vc/getjd.html)
     * 年月日は西暦、時間はUTC
     */
    fun getJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int = 0, nMin: Int = 0, nSec: Int = 0): Double {
        var month = if (nMonth == 1 || nMonth ==2) nMonth +12 else nMonth
        var year = if (nMonth == 1 || nMonth ==2) nYear - 1 else nYear
        return ((year * 365.25).toInt() + (year / 400) - (year / 100) +
                (30.59 * (month - 2)).toInt() + nDay - 678912 + 2400000.5 +
                nHour.toDouble() / 24 + nMin.toDouble() / (24 * 60) + nSec.toDouble() / (24 * 60 * 60))
    }

    /**
     * 天文年鑑2020(346p)の数式でユリウス日を求める
     */
    fun julianDay(y: Int, m: Int, d: Int): Int {
        return (y + 4800 - (14 - m) / 12) * 1461 / 4
                + (m + (14 - m) / 12 * 12 - 2) * 367 / 12
                - (y + 4900 - (14 - m) / 12) / 100 * 3 / 4 + d - 32075
    }

    /**
     * 準ユリウス日(1582年10月15日が0日)の取得
     */
    fun getMJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int, nMin: Int, nSec: Int): Double {
        val dJD = getJD(nYear, nMonth, nDay, nHour, nMin, nSec)
        return if (dJD == 0.0) 0.0 else dJD - 2400000.5
    }

    /**
     * 恒星時(Wikipedia https://ja.wikipedia.org/wiki/恒星時)
     * 1平均太陽日 = 1.0027379094 (2020.5 天文年鑑2020)
     * return       グリニッジ恒星時(hh.hhhh)
     */
    fun getGreenwichSiderealTime(nYear: Int, nMonth: Int, nDay: Int, nHour: Int, nMin: Int, nSec: Int): Double {
        val JD = getJD(nYear, nMonth, nDay, nHour, nMin, nSec)  //  ユリウス日
        val TJD = JD - 2440000.5                                //  準ユリウス日
        return 24.0 * ((0.671262 + 1.0027379094 * TJD) % 1.0)
    }

    /**
     * 地方恒星時の取得
     * dSiderealTime    グリニッジ恒星時(hh.hhhh)
     * dLongitude       観測地の緯度(dd.dddd)
     * return           地方恒星時(hh.hhhh)
     */
    fun getLocalSiderealTime(dSiderealTime: Double, dLongitude: Double): Double {
        return dSiderealTime + dLongitude / 15.0
    }

    //  ---  座標変換関連  ------

    /**
     * 赤道座標系を地平座標系に変換
     * coordinate       赤道座標(rad)
     * lst              地方恒星時(時)
     * localLatitude    観測点緯度(rad)
     * direction        観測方向(時)
     * radius           天球の半径
     * full             全天表示
     * return           地平座標(rad) (isEmpty()の時は非表示
     */
    fun convHorizontalPoint(coordinate: PointD, lst: Double, localLatitude: Double, direction: Double, radius: Double, full: Boolean = false): PointD {
        val hourAngle = H2R(lst - R2H(coordinate.x))                   //  時角
        val height = horizonHeight(hourAngle, coordinate.y, localLatitude)  //  高さ
        val azimuth = horizonAzimus(hourAngle, coordinate.y, localLatitude) //  方位

        var sp = (direction - 6.0 + 24.0) % 24.0            //  方位の表示開始角度(常に正の値))
        var hazimuth = (R2H(azimuth) - sp + 24.0) % 24.0    //  表示開始位置と星の位置の方位差)
        var p = PointD()
        if (0 < height && (full || 0 < hazimuth && hazimuth < 12)) {
            if (full)
                p = cnvFullHorizontal(azimuth, height, direction, radius)
            else
                p = cnvHorizontal(azimuth, height, direction, radius)
        }
        return p
    }

    /**
     * 地平座標系の座標を全天表示の直交座標に変換
     * azimuth          方位(rad)
     * height           高度(rad)
     * return           表示位置(直交座標)
     */
    fun cnvFullHorizontal(azimuth: Double, height: Double, direction:Double, radius:Double): PointD {
        var p = PointD()
        p.fromPoler(radius * (1.0 - height * 2.0 / PI), azimuth)
        p.rotateOrg(H2R((direction - 6.0) % 24.0))
        return p
    }

    /**
     * 地平座標系の投影のための座標変換
     * azimuth      方位(rad)
     * height       高さ(rad)
     * direction    観測方向(時)
     * radius       天球の半径
     */
    fun cnvHorizontal(azimuth: Double, height: Double,direction: Double, radius: Double): PointD {
        return PointD(radius * sin(azimuth - H2R(direction)) * cos(height), radius * sin(height))
    }

    /**
     * 赤経・赤緯から直交座標点を求める
     * coordinate       赤道座標(赤経(rad),赤緯(rad))
     * direction        観測方向(時)
     * radius           天球の半径
     * return           直交座標
     */
    fun equatorial2orthogonal(coordinate: PointD, direction: Double, radius: Double): PointD {
        var p = PointD()
        p.fromPoler(declinationLength(coordinate.y, radius), PI * 2.0 - coordinate.x)
        p.rotateOrg(H2R(direction))
        return p
    }

    /**
     * 赤緯から天の天頂からの距離を求める
     * declination      赤緯(rad)
     * radius           天球面の半径
     * return           距離
     */
    fun declinationLength(declination: Double, radius: Double): Double {
        return radius * (0.5 - declination / PI)
    }

    /**
     * 赤道座標(equatorial coordinates)から地平座標(horizon coordinates)の高度(height)(0～90)を求める
     * hourAngle        時角
     * declination      赤緯δ(rad)
     * latitude         観測点緯度φ(rad)
     * return           高度h(rad)
     */
    fun horizonHeight(hourAngle: Double, declination: Double, latitude:Double): Double {
        val sinh = sin(declination) * sin(latitude) + cos(declination) * cos(latitude) * cos(hourAngle)
        return asin(sinh)
    }

    /**
     * 赤道座標(equatorial coordinates)から地平座標(horizon coordinates)の方位(azimuth)を求める
     * hourAngle        時角
     * declination      赤緯δ(rad)
     * latitude         観測点緯度φ(rad)
     * return           方位A(rad)
     */
    fun horizonAzimus(hourAngle: Double, declination: Double, latitude:Double): Double {
        val s = cos(declination) * sin(hourAngle)
        val c = cos(declination) * sin(latitude) * cos(hourAngle) - sin(declination) * cos(latitude)
        return if (s < 0) 2.0 * PI + atan2(s, c) else atan2(s, c)
    }

    /**
     * 地平座標(horizon coordinates)から赤道座標(equatorial coordinates)の赤緯(δ:declination)を求める
     * azimuth          方位 A(rad)
     * height           高度 h(rad)
     * latitude         観測点緯度 φ(rad)
     * return           赤緯δ (rad -π～π)
     */
    fun equatorialDeclination(azimuth: Double, height: Double, latitude: Double): Double {
        val sind = sin(height) * sin(latitude) - cos(height) * cos(latitude) * cos(azimuth)
        return asin(sind)
    }

    /**
     * 地平座標(horizon coordinates)から赤道座標(equatorial coordinates)の時角(t: hourangle)を求める
     * azimuth          方位 A(rad)
     * height           高度 h(rad)
     * latitude         観測点緯度 φ(rad)
     * return           時角　t(rad 0～2π)
     */
    fun equatorialHourAngle(azimuth: Double, height: Double, latitude: Double): Double {
        val x = cos(latitude) * sin(height) + sin(latitude) * cos(height) * cos(azimuth)
        val y = cos(height) * sin(azimuth)
        return if (y < 0) 2.0 * PI + atan2(y, x) else atan2(y, x) // 0 - 2π
    }

    /**
     * 地平座標(horizon coordinates)から赤道座標(equatorial coordinates)の赤経(ra: RightAscension)を求める
     * azimuth          方位 A(rad)
     * height           高度 h(rad)
     * latitude         観測点緯度 φ(rad)
     * lst              地方恒星時(rad)
     * return           赤経　α(rad 0～2π)
     */
    fun equatorialRightAscension(azimuth: Double, height: Double, latitude: Double, lst: Double): Double {
        val t = equatorialHourAngle(azimuth, height, latitude)
        return lst - t
    }

    //  ---  単位変換関連  ------

    /**
     * ラジアンから度に変換
     */
    fun R2D(rad: Double): Double {
        return rad * 180.0 / PI
    }

    /**
     * 度からラジアンに変換
     */
    fun D2R(deg: Double): Double {
        return deg * PI / 180.0
    }

    /**
     * 時(hh.hhhh)から度(ddd.dddd)
     */
    fun H2D(hour: Double): Double {
        return hour * 360.0 / 24.0
    }

    /**
     * 度(ddd.dddd)から時(hh.hhhh)に変換
     */
    fun D2H(deg: Double): Double {
        return deg * 24.0 / 360.0
    }

    /**
     * 時(hh.hhhh)からラジアンに変換
     */
    fun H2R(hour:Double): Double {
        return hour * PI / 12.0
    }

    /**
     * ラジアンから時(hh.hhhh)に変換
     */
    fun R2H(rad:Double): Double {
        return rad * 12.0 / PI
    }

    private val mDMSChar = listOf('°', '′', '″' )

    /**
     * 度(ddd.dddd)を度分秒(dd°mm′ss″)文字列に変換
     */
     fun deg2DMS(deg: Double): String {
        var deg2 = abs(deg)
        var dms = (if (deg < 0.0) "-" else "+") + ("%02d").format(deg2.toInt()) + mDMSChar[0]
        dms +=  ("%02d").format(floor((deg2 % 1.0) * 60).toInt()) + mDMSChar[1]
        dms +=  ("%02d").format(floor(((deg2 * 60.0) % 1.0) * 60).toInt()) + mDMSChar[2]
        return dms
    }

    /**
     * 度分秒(dd°mm′ss″)文字列を度(ddd.dddd)に変換
     */
    fun DMS2deg(dms: String): Double {
        val flag = if (dms[0] == '-') -1.0 else 1.0
        var dms2 = if (dms[0] == '-') dms.substring(1) else dms
        var d = dms2.substring(0, dms2.indexOf(mDMSChar[0])).toDouble()
        var m = dms2.substring(dms2.indexOf(mDMSChar[0])+1,dms2.indexOf(mDMSChar[1])).toDouble()
        var s = dms2.substring(dms2.indexOf(mDMSChar[1])+1,dms2.indexOf(mDMSChar[2])).toDouble()
        return (d + m / 60.0 + s / 3600.0) * flag
    }

    /**
     * 時(hh.hhhh)を時分秒(HHhMMmSSs)文字列に変換
     */
    fun hour2HMS(hour: Double): String {
        var hms2 = abs(hour)
        var hms = (if (hour < 0.0) "-" else "+") + ("%02d").format(hms2.toInt()) + "h"
        hms +=  ("%02d").format(floor((hms2 % 1.0) * 60).toInt()) + "m"
        hms +=  ("%02d").format(floor(((hms2 * 60.0) % 1.0) * 60).toInt()) + "s"
        return hms
    }

    /**
     * 時分秒(HHhMMmSSs)文字列を時(hh.hhhh)に変換
     */
    fun HMS2hour(hms: String): Double {
        val flag = if (hms[0] == '-') -1.0 else 1.0
        var hms2 = if (hms[0] == '-') hms.substring(1) else hms
        var h = hms2.substring(0, hms2.indexOf("h")).toDouble()
        var m = hms2.substring(hms2.indexOf("h")+1,hms2.indexOf("m")).toDouble()
        var s = hms2.substring(hms2.indexOf("m")+1,hms2.indexOf("s")).toDouble()
        return (h + m / 60.0 + s / 3600.0) * flag
    }

    /**
     * 時分秒(hhmmss)文字列を時(hh.hhhh)に変換
     */
    fun hhmmss2hour(hhmmss:String): Double {
        val h = hhmmss.substring(0, 2).toDouble()
        val m = hhmmss.substring(2, 4).toDouble()
        val s = hhmmss.substring(4, 6).toDouble()
        return h + m /60.0 + s /3600.0
    }

    /**
     * 度分秒(dd,mm,ss)を度(ddd.dddd)
     */
    fun DMS2deg(d: Int, m: Int, s: Double):Double {
        return d + m / 60.0 + s / 3600.0
    }

    /**
     * 時分秒(hh,mm,ss)を時(hh.hhhh)に変換
     */
    fun HMS2hour(h: Int, m: Int, s: Double): Double {
        return h + m / 60.0 + s / 3600.0
    }

    /**
     * 時分(hhmm.m)を時(hh.hhhh)に変換
     */
    fun HM2hour(hm: String):Double {
        return DM2deg(hm)
    }

    /**
     * 度分(±ddmm.m)を度(ddd.dddd)に変換
     */
    fun DM2deg(dm: String): Double {
        var n = 0
        var flag = 1.0
        if (dm[0] == '+') {
            n = 1
        } else if (dm[0] == '-') {
            n = 1
            flag = -1.0
        }
        val d = dm.substring(n, n + 2).toDouble()
        val m = if (n + 2 < dm.length) dm.substring(n + 2).toDouble() else 0.0
        return (d + m / 60.0) * flag
    }

    /**
     * 度分秒(±dd:mm:ss)を度(dd.ddddに変換
     * dm       度分秒(dd:mm:ss)
     * return   度(dd.dddd)<
     */
    fun dm2deg(dm: String): Double {
        return hm2hour(dm)
    }

    /**
     * 時分(hh:mm)を時(hh.hhhh)に変換
     * hm       時分(hh:mm)
     * return   時(hh.hhhh)
     */
    fun hm2hour(hm: String): Double {
        var sc = ":"
        if (0 <= hm.indexOf("："))
            sc = "："
        var text = hm.split(sc)
        if (0 < text.count()) {
            var n = 0
            var sign = 1.0
            if (text[0][0] == '+') {
                n = 1
            } else if (text[0][0] == '-') {
                n = 1
                sign = -1.0
            }
            var hour = text[0].substring(n).toDouble()
            for (i in 1..text.lastIndex) {
                hour += text[i].toDouble() / 60.0.pow(i)
            }
            return hour * sign
        }
        return 0.0
    }
}