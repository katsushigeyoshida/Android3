package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD

class StarInfoData {
    val TAG = "StarInfoData"

    //  恒星位置照合データ
    data class SEARCHDATA(
        var coordinate: PointD,     //  スクリーン座標の位置データ
        var starName: String,       //  恒星名
        var mesiaNo: Int,           //  メシア番号(星雲・銀河など、Mなし)(ない時は-1)
        var hipNo: Int              //  HIP番号(ない時は-1)
    )
    var mSearchData = mutableListOf<SEARCHDATA>()       //  恒星表示位置データ
    lateinit var mStarStrData: List<List<String>>       //  恒星ファイルデータ
    lateinit var mStarData: List<StarData.STARDATA>
    lateinit var mNebulaStrData: List<List<String>>     //  星雲・銀河などファイルデータ
    lateinit var mNebulaData: List<NebulaData.NEBULADATA>

    private val klib = KLib()

    /**
     * 指定位置(スクリーン座標)から恒星を特定する
     * pos          指定値(スクリーン座標)
     * dis          許容距離
     */
    fun searchPos(pos: PointD, dis: Double): SEARCHDATA {
        for (star in mSearchData) {
            if (pos.distance(star.coordinate) < dis)
                return star
        }
        return SEARCHDATA(PointD(), "", -1,-1)
    }

    /**
     * HIP番号または恒星名から恒星情報を取得する
     * searchData       検索データ
     * return           情報データリスト
     */
    fun searchData(searchData: SEARCHDATA):List<String> {
        var infoData = mutableListOf<String>()
        if (0 < searchData.hipNo) {
            val n = mStarData.indexOfFirst {it -> it.hipNo == searchData.hipNo}
            if (0 <= n) {
                for (i in mStarStrData[n + 1].indices) {
                    infoData.add(mStarStrData[0][i]+": "+mStarStrData[n + 1][i])
                }
            }
        } else if (searchData.hipNo <0 && 0 <= searchData.mesiaNo) {
            val n = mNebulaData.indexOfFirst { it.messieNo == searchData.mesiaNo }
            if (0 <= n) {
                for (i in mNebulaStrData[n + 1].indices) {
                    infoData.add(mNebulaStrData[0][i] + ": " + mNebulaStrData[n + 1][i])
                }
            }
        } else if (0 < searchData.starName.length) {
            val n = mStarData.indexOfFirst {it -> it.starName.compareTo(searchData.starName) == 0}
            if (0 <= n) {
                for (i in mStarStrData[n + 1].indices) {
                    infoData.add(mStarStrData[0][i]+": "+mStarStrData[n + 1][i])
                }
            }
        }
        return infoData
    }
}