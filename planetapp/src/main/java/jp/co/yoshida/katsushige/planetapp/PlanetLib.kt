package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import kotlin.math.*


class PlanetLib {

    val mPlanetData = listOf<PLANETDATA>(
        //  惑星名, 軌道長半径a, 離心率e, 黄道軌道傾斜i, 近日点黄経ω, 昇交点黄経Ω, 元期平均近点角M0, 公転周期, 元期(JD)
        PLANETDATA("水星",    0.3871, 0.2056, 7.004,  77.490,  48.304, 282.128,  0.24085, 2459400.5),
        PLANETDATA("金星",    0.7233, 0.0068, 3.694, 131.565,  76.620,  35.951,  0.61520, 2459400.5),
        PLANETDATA("地球",    1.0000, 0.0167, 0.003, 103.007, 174.821, 179.912,  1.00002, 2459400.5),
        PLANETDATA("火星",    1.5237, 0.0934, 1.848, 336.156,  49.495, 175.817,  1.88085, 2459400.5),
        PLANETDATA("木星",    5.2026, 0.0485, 1.303,  14.378, 100.502, 312.697,  11.8620, 2459400.5),
        PLANETDATA("土星",    9.5549, 0.0555, 2.489,  93.179, 113.610, 219.741,  29.4572, 2459400.5),
        PLANETDATA("天王星", 19.2184, 0.0464, 0.773, 173.024,  74.022, 233.182,  84.0205, 2459400.5),
        PLANETDATA("海王星", 30.1104, 0.0095, 1.770,  48.127, 131.783, 303.212, 164.7701, 2459400.5)
    )

    val klib = KLib()
    /**
     * 惑星データの取得
     * name     惑星名
     * return   惑星データ
     */
    fun getPlanetData(name:String): PLANETDATA {
        val planetData = mPlanetData.find { it -> it.name.compareTo(name) == 0 }
        if (planetData == null)
            return mPlanetData[2]
        else
            return planetData
    }

    /**
     * 惑星の赤経・赤緯を求める
     * name     惑星名
     * jd       ユリウス日
     * return   赤道座標(赤経(ra),赤緯(dec))
     */
    fun equatorialCoordenate(name: String, jd: Double): PointD {
        val T = (jd - 2451545.0) / 36525        //  ユリウス世紀
        //  黄道傾斜角
        var epsilon = (84381.406 - 46.836769 * T - 0.00059 * T.pow(2.0) + 0.001813 * T.pow(3.0)) / 3600.0
        epsilon = klib.D2R(epsilon);    //  歳差を考慮しない時は 84381.406 / 3600 (deg) = 0.409092600600583(rad)

        //  地球の黄道座標
        val earthData = getPlanetData("地球")
        val earthPos = earthData.getPlanetPos(jd)

        //  対象惑星の黄道座標
        val planetData = getPlanetData(name)
        val planetPos = planetData.getPlanetPos(jd)

        //  赤道座標に変換
        val x = planetPos.x - earthPos.x
        val y = (planetPos.y - earthPos.y) * cos(epsilon) - (planetPos.z - earthPos.z) * sin(epsilon)
        val z = (planetPos.y - earthPos.y) * sin(epsilon) + (planetPos.z - earthPos.z) * cos(epsilon)
        //  赤経・赤緯に変換
        val ra: Double = if (y < 0) 2.0 * Math.PI + atan2(y, x) else atan2(y, x)
        val dec: Double = atan2(z, sqrt(x * x + y * y))

        return PointD(ra, dec)      //  赤経(ra),赤緯(dec)
    }
}