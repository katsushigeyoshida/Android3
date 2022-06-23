package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD

class NebulaData {
    /**
     * 星雲・銀河などの構造体
     */
    data class NEBULADATA (
        var messieNo: Int,
        var coordinate: PointD,
        var magnitude: Double,
        var NGCNo: String,
        var name: String
    )
    var mNebulaData = listOf<NEBULADATA>()
    var mNebulaStrData = listOf<List<String>>()   //  星雲・銀河ファイルデータ
    var mNebulaFile = "メシエ天体の一覧.csv"
    val mNebulaTitle = mutableListOf<String>(
        "メシエ番号", "NGC番号", "名称", "タイプ", "距離(千光年)", "星座" ,
        "赤経：時分秒", "赤緯：±度分秒", "等級",
        "赤経(h:m)", "赤緯(deg:m)"
    )
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
        mNebulaFile = mDataFolder + "/" + mNebulaFile
    }

    /**
     * データの読込
     */
    fun loadData() {
        val encode = 1
        var dataList = alib.loadData(mNebulaFile, encode)
        if (dataList.count() <= 1 )
            return
        mNebulaStrData = alib.meargeTitle(dataList)
        mNebulaData = convNebulaData(mNebulaStrData)
    }

    /**
     * データを構造体に変換
     * dataList     テキスト形式のデータリスト
     * return       データ(構造体)
     */
    fun convNebulaData(dataList: List<List<String>>): List<NEBULADATA> {
        var ntbulaList = mutableListOf<NEBULADATA>()
        if (0 < dataList.count()) {
            var convCol = alib.getConvCol(dataList[0], mNebulaTitle)
            for (row in 1..dataList.lastIndex) {
                var buf = NEBULADATA(
                    dataList[row][convCol[0]].substring(1).toInt(),
                    PointD(
                        alib.H2R(alib.HMS2hour(dataList[row][convCol[6]])),
                        alib.D2R(alib.DMS2deg(dataList[row][convCol[7]]))
                    ),
                    dataList[row][convCol[8]].toDouble(),
                    dataList[row][convCol[1]],
                    dataList[row][convCol[2]]
                )
                ntbulaList.add(buf)
            }
        }
        return ntbulaList
    }
}