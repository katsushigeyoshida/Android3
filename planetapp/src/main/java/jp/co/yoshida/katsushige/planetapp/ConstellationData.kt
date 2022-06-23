package jp.co.yoshida.katsushige.planetapp

import android.util.Log
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD


class ConstellationData {
    val TAG = "ConstellationData"

    /**
     * 星座の腺データ HIP番号(構造体)
     */
    data class CONSTELLATIONLINE (
        var sHip: Int,
        var eHip: Int,
        var constrationName: String
    )

    /**
     * 星座線の起点座標(赤経赤緯)の構造体
     */
    data class CONSTELLATIONSTAR (
        var hip: Int,
        var coordinate: PointD
    )

    /**
     * 星座名
     */
    data class CONSTELLATIONNAME (
        var constrationName: String,
        var constrationNameJpn: String,
        var constrationNameMono: String,
        var coordinate: PointD
    )

    var mConstellaLineLineList = listOf<CONSTELLATIONLINE>() //  星座線(HIP番号)
    var mConstellaStarList = mapOf<Int, CONSTELLATIONSTAR>() //  恒星位置
    var mConstellaNameList = listOf<CONSTELLATIONNAME>()     //  星座名

    var mConstellationLineFile = "hip_constellation_line(星座線データ).csv"
    var mConstellationStarFile = "hip_constellation_line_star(星座線恒星データ).csv"
    var mConstellationNameFile = "理科年表2021星座.csv"
    val mConstellaLineTitle = mutableListOf<String>( "星座", "HIP", "HIP" )
    val mConstellaStarTitle = mutableListOf<String>( "HIP", "赤経", "赤緯" )
    val mConstellaNameTitle = mutableListOf<String>( "星座名", "学名", "略符", "赤経", "赤緯" )

    var mDataFolder = ""                        //  データフォルダ

    val klib = KLib()
    var alib = AstroLib()

    /**
     * コンストラクタ
     * datafolder       データフォルダ
     * starDataPath     恒星データパス
     */
    constructor(dataFolder: String) {
        mDataFolder = dataFolder
        mConstellationLineFile = mDataFolder + "/" + mConstellationLineFile
        mConstellationStarFile = mDataFolder + "/" + mConstellationStarFile
        mConstellationNameFile = mDataFolder + "/" + mConstellationNameFile
    }

    /**
     * 星座データの読込
     */
    fun loadData() {
        mConstellaLineLineList = loadConstellationLine(mConstellationLineFile)
//        ConstellationLine()       //  debug用
        mConstellaStarList = loadConstellationStar(mConstellationStarFile)
//        ConstellaStar()           //  debug用
        mConstellaNameList = loadConstellaName(mConstellationNameFile)
//        ConstellaNameList()       //  debug用
    }

    //  Debug用
    fun ConstellationLine() {
        var n = 0
        for (data in mConstellaLineLineList) {
            Log.d(TAG, "ConstellationLine: "+"${(n++)} ${data.sHip} ${data.eHip} ${data.constrationName}")
        }
    }

    //  Debug用
    fun ConstellaStar() {
        var n = 0
        for (data in mConstellaStarList) {
            Log.d(TAG, "ConstellaStar: "+"${(n++)} ${data.key} ${data.value.hip} ${data.value.coordinate}")
        }
    }

    //  Debug用
    fun ConstellaNameList() {
        var n = 0
        for (data in mConstellaNameList) {
            Log.d(TAG, "ConstellaNameList: "+"${(n++)} ${data.constrationName} ${data.constrationNameJpn} ${data.constrationNameMono} ${data.coordinate}")
        }
    }

    /**
     * 星座線リストの読込
     * filePath         ファイル名
     * encode           エンコード
     * return           星座線リスト(CONSTELLATIONLINE)
     */
    fun loadConstellationLine(filePath:String, encode: Int=1): List<CONSTELLATIONLINE> {
        var constrationLineList = mutableListOf<CONSTELLATIONLINE>()
        val dataList = alib.loadData(filePath, encode)
        if (dataList.count() <= 1 )
            return constrationLineList
        if (0 < dataList.count()) {
            var convCol = alib.getConvCol(dataList[0], mConstellaLineTitle)
            for (row in 1..dataList.lastIndex) {
                var buf = CONSTELLATIONLINE(
                    dataList[row][convCol[1]].toInt(),
                    dataList[row][convCol[2]].toInt(),
                    dataList[row][convCol[0]]
                )
                constrationLineList.add(buf)
            }
        }
        return constrationLineList
    }

    /**
     * 星座線恒星リストの読込
     * filePath         ファイル名
     * encode           エンコード
     * return           星座線恒星リスト(CONSTELLATIONSTAR)
     */
    fun loadConstellationStar(filePath: String, encode: Int=1): Map<Int, CONSTELLATIONSTAR> {
        var constrationStarList = mutableMapOf<Int, CONSTELLATIONSTAR>()
        var dataList = alib.loadData(filePath, encode)
        if (dataList.count() <= 1 )
            return constrationStarList
        dataList = alib.meargeTitle(dataList)
        if (0 < dataList.count()) {
            var convCol = alib.getConvCol(dataList[0], mConstellaStarTitle)
            for (row in 1..dataList.lastIndex) {
                var buf = CONSTELLATIONSTAR(
                    dataList[row][convCol[0]].toInt(),
                    PointD(
                        alib.H2R(alib.HMS2hour(dataList[row][convCol[1]])),
                        alib.D2R(alib.DMS2deg(dataList[row][convCol[2]]))
                    )
                )
                constrationStarList.put(dataList[row][convCol[0]].toInt(), buf)
            }
        }
        return constrationStarList
    }

    /**
     * 星座名の読込
     * filePath         ファイル名
     * encode           エンコード
     * return           星座名リスト(CONSTELLATIONNAME)
     */
    fun loadConstellaName(filePath: String, encode: Int=1): List<CONSTELLATIONNAME> {
        var constellaNameList = mutableListOf<CONSTELLATIONNAME>()
        val dataList = alib.loadData(filePath, encode)
        if (dataList.count() <= 1 )
            return constellaNameList
        var convCol = alib.getConvCol(dataList[0], mConstellaNameTitle)
        for (row in 1..dataList.lastIndex) {
            var buf = CONSTELLATIONNAME(
                dataList[row][convCol[1]],
                dataList[row][convCol[0]],
                dataList[row][convCol[2]],
                PointD(
                    alib.H2R(alib.HM2hour(dataList[row][convCol[3]])),
                    alib.D2R(alib.DM2deg(dataList[row][convCol[4]]))
                )
            )
            constellaNameList.add(buf)
        }
        return constellaNameList
    }

}