package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD

class StarData {
    val TAG = "StarData"

    data class STARDATA (
        var hipNo: Int,                 //  HIP番号
        var coordinate: PointD,         //  赤道座標(鶺鴒(rad),赤緯(rad))
        var magnitude: Double,          //  視等級
        var starName: String,           //  恒星名
        var starNameJp: String,         //  恒星名(日本語)
        var constellation: String       //  星座名
    )
    var mStarData = listOf<STARDATA>()          //  恒星リスト
    var mStarStrData = listOf<List<String>>()   //  恒星ファイルデータ
    var mDataFolder = ""                        //  データフォルダ
    var mStarDataPath = ""                      //  恒星データパス

    val klib = KLib()
    var alib = AstroLib()

    /**
     * コンストラクタ
     * datafolder       データフォルダ
     * starDataPath     恒星データパス
     */
    constructor(dataFolder: String, starDataPath: String) {
        mDataFolder = dataFolder
        mStarDataPath = starDataPath
    }

    /**
     * 指定恒星データファイルのデータの読み込む
     * 文字列データのまま保持
     */
    fun loadData() {
        mStarStrData = alib.loadData(mStarDataPath, 1)
        if (0 < mStarStrData.count())
            mStarStrData = alib.meargeTitle(mStarStrData)
    }

    /**
     * 文字列データからSTARDATA形式に変換
     */
    fun convStarData() {
        mStarData = convStarData(mStarStrData)
    }

    /**
     *  文字列の恒星データをSTARDATA形式に変換
     *  starStrData     恒星データ(文字列)
     *  return          変換データ(STARDATA形式)
     */
    fun convStarData(starStrData: List<List<String>>): List<STARDATA> {
        var starData = mutableListOf<STARDATA>()
        if (0 < starStrData.count()) {
            var convCol = alib.getConvCol2(starStrData[0])
            for (row in 1..starStrData.lastIndex) {
                var buf = STARDATA(
                    if (convCol[0] < 0) 0 else starStrData[row][convCol[0]].toIntOrNull()?:0,
                    PointD(alib.H2R(alib.HMS2hour(starStrData[row][convCol[1]])),
                            alib.D2R(alib.DMS2deg(starStrData[row][convCol[2]]))),
                    klib.str2Double(starStrData[row][convCol[3]]),
                    if (0 <= convCol[4]) starStrData[row][convCol[4]] else "",
                    if (0 <= convCol[5]) starStrData[row][convCol[5]] else "",
                    if (0 <= convCol[6]) starStrData[row][convCol[6]] else ""
                )
                starData.add(buf)
            }
        }
        return starData
    }

    /**
     * 文字列の恒星データを標準形式の恒星データに変換
     * starStrDara      恒星データ(文字列)
     * return           変換データ(文字列)
     */
    fun convStrStarData(starStrData: List<List<String>>): List<List<String>> {
        var convCol = alib.getConvCol2(starStrData[0])
        var starData = mutableListOf<List<String>>()
        for (strData in starStrData) {
            var buf = mutableListOf<String>()
            for (i in 0..convCol.lastIndex) {
                if (0 <= convCol[i] && convCol[i] < strData.count())
                    buf.add(strData[convCol[i]])
                else
                    buf.add("")
            }
            starData.add(buf)
        }
        return starData
    }

    /**
     * 地平座標データを追加したテキストデータリストに変換
     * lst              地方恒星時(時)
     * localLatitude    観測点緯度(deg)
     */
    fun getHorizonStarData(lst: Double, localLatitude: Double):List<List<String>> {
        var starData = mutableListOf<List<String>>(
            listOf<String>("HIP番号", "赤経", "赤緯", "方位", "高度", "時角", "視等級", "恒星名", "恒星名(日本語)")
        )
        for (star in mStarData) {
            val hourAngle = alib.H2R(lst - alib.R2H(star.coordinate.x))                         //  時角
            val height = alib.horizonHeight(hourAngle, star.coordinate.y, alib.D2R(localLatitude))  //  高さ
            val azimuth = alib.horizonAzimus(hourAngle, star.coordinate.y, alib.D2R(localLatitude)) //  方位
            var data = mutableListOf<String>()
            data.add(star.hipNo.toString().padStart(6, '0'))
            data.add(alib.hour2HMS(alib.R2H(star.coordinate.x)))
            data.add(alib.deg2DMS(alib.R2D(star.coordinate.y)))
            data.add(alib.hour2HMS(alib.R2H(azimuth)))
            data.add(alib.deg2DMS(alib.R2D(height)))
            data.add(alib.hour2HMS((alib.R2H(hourAngle))))
            data.add(star.magnitude.toString())
            data.add(star.starName)
            data.add(star.starNameJp)
            starData.add(data)
        }

        return starData
    }
}