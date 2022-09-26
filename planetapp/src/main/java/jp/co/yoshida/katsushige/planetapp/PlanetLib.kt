package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.Point3D
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
    fun getPlanetData(name:String, jd: Double = 0.0): PLANETDATA {
        val planetData = mPlanetData.find { it -> it.name.compareTo(name) == 0 }
        if (planetData == null)
            return mPlanetData[2]
        else
            return planetData.getPlanetData(jd)
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
        return ecliptic2equatorial(planetPos.sub(earthPos), epsilon)
    }

    /**
     * 黄道座標を赤道座標に変換
     * ecliptic     黄道座標(xyz)
     * epsilon      黄道傾斜角(rad)
     * return       赤道座標(赤経ra、赤緯dec)(rad)
     */
    fun ecliptic2equatorial(ecliptic: Point3D, epsilon: Double): PointD {
        //  赤道座標に変換
        val x = ecliptic.x
        val y = ecliptic.y * cos(epsilon) - ecliptic.z * sin(epsilon)
        val z = ecliptic.y * sin(epsilon) + ecliptic.z * cos(epsilon)
        //  赤経・赤緯に変換
        val ra  = if (y < 0) 2.0 * PI + atan2(y, x) else atan2(y, x)
        val dec = atan2(z, sqrt(x * x + y * y))

        return PointD(ra, dec)      //  赤経(ra),赤緯(dec)
    }

    /**
     * 地球の自転軸の傾き(黄道傾斜角)
     * jd           ユリウス日
     * return       傾き(rad)
     */
    fun getEpslion(jd: Double): Double {
        //  黄道傾斜角(IAU基準) https://ja.wikipedia.org/wiki/黄道傾斜角
        val T = (jd - 2451545.0) / 36525 //  ユリウス世紀(2000年1月1日元期)
        return klib.D2R((84381.406 - 46.836769 * T - 0.00059 * T * T + 0.001813 * T * T * T) / 3600.0)
        //  歳差を考慮しない時は 84381.406 / 3600 (deg) = 0.409092600600583(rad)

        //  黄道傾斜角 「天体の位置計算増補版」141p
        //double T = (jd - ylib.getJD(1899, 12, 31, 9)) / 365.25;       //  元期 1900
        //return ylib.D2R(23.452294 - 0.0130125 * T - 0.00000164 * T * T + 0.000000503 * T * T * T);
    }

    /**
     * 黄道座標を赤道座標に変換
     * ecliptic     黄道座標(λ,β)(rad)
     * epslion      黄道傾斜角(rad)
     * return       赤道座標(ra,dec)(rad)
     */
    fun ecliptic2equatorial(ecliptic: PointD, epslion: Double): PointD {
        val U = cos(ecliptic.y) * cos(ecliptic.x)
        val V = cos(ecliptic.y) * sin(ecliptic.x)
        val W = sin(ecliptic.y)

        val M = V * cos(epslion) - W * sin(epslion)
        val N = V * sin(epslion) + W * cos(epslion)

        var ra  = atan2(M, U)
        val dec = asin(N)

        if (ra < 0.0) ra += 2.0 * PI

        return PointD(ra, dec)
    }

    /**
     * 月の黄経・黄緯を求める
     * Tは1975年1月0日9時0分(世界時0時)とした日数を365.25日で割った値
     * 「天体の位置計算」長沢工緒201pより「月の位置の略算法」
     * jd       ユリウス美
     * return   黄経・黄緯(λ,β)(rad)
     */
    fun moonEclipticCoordinate(jd: Double): PointD {
        val T = (jd - klib.getJD(1974, 12, 31, 0)) / 365.25
        var A = 0.0040 * sin(klib.D2R(93.8 - 1.33 * T))
        A += 0.0020 * sin(klib.D2R(248.6 - 19.34 * T))
        A += 0.0006 * sin(klib.D2R(66.0 + 0.2 * T))
        A += 0.0006 * sin(klib.D2R(249.0 - 19.3 * T))

        var ramuda = 124.8754 + 4812.67881 * T
        ramuda += 6.2887 * sin(klib.D2R(338.915 + 4771.9886 * T + A))
        ramuda += 1.2740 * sin(klib.D2R(107.248 - 4133.3536 * T))
        ramuda += 0.6583 * sin(klib.D2R(51.668 + 8905.3422 * T))
        ramuda += 0.2136 * sin(klib.D2R(317.831 + 9543.9773 * T))
        ramuda += 0.1856 * sin(klib.D2R(176.531 + 359.9905 * T))

        ramuda += 0.1143 * sin(klib.D2R(292.463 +  9664.0404 * T));
        ramuda += 0.0588 * sin(klib.D2R( 86.161 +   638.635  * T));
        ramuda += 0.0572 * sin(klib.D2R(103.781 -  3773.363  * T));
        ramuda += 0.0533 * sin(klib.D2R( 30.581 + 13677.331  * T));
        ramuda += 0.0459 * sin(klib.D2R(124.861 -  8545.352  * T));

        ramuda += 0.0410 * sin(klib.D2R(342.38  +  4411.998 * T));
        ramuda += 0.0348 * sin(klib.D2R( 25.83  +  4452.671 * T));
        ramuda += 0.0305 * sin(klib.D2R(155.45  +  5131.979 * T));
        ramuda += 0.0153 * sin(klib.D2R(240.79  +   758.698 * T));
        ramuda += 0.0125 * sin(klib.D2R(271.38  + 14436.029 * T));

        ramuda += 0.0110 * sin(klib.D2R(226.45 -  4892.052 * T));
        ramuda += 0.0107 * sin(klib.D2R( 55.58 - 13038.696 * T));
        ramuda += 0.0100 * sin(klib.D2R(296.75 + 14315.966 * T));
        ramuda += 0.0085 * sin(klib.D2R( 34.5  -  8266.71  * T));
        ramuda += 0.0079 * sin(klib.D2R(290.7  -  4493.34  * T));

        ramuda += 0.0068 * sin(klib.D2R(228.2  +  9265.33  * T));
        ramuda += 0.0052 * sin(klib.D2R(133.1  +   319.32  * T));
        ramuda += 0.0050 * sin(klib.D2R(202.4  +  4812.66  * T));
        ramuda += 0.0048 * sin(klib.D2R( 68.6  -    19.34  * T));
        ramuda += 0.0040 * sin(klib.D2R( 34.1  + 13317.34  * T));

        ramuda += 0.0040 * sin(klib.D2R(  9.5 + 18449.32 * T));
        ramuda += 0.0040 * sin(klib.D2R( 93.8 -     1.33 * T));
        ramuda += 0.0039 * sin(klib.D2R(103.3 + 17810.68 * T));
        ramuda += 0.0037 * sin(klib.D2R( 65.1 +  5410.62 * T));
        ramuda += 0.0027 * sin(klib.D2R(321.3 +  9183.99 * T));

        ramuda += 0.0026 * sin(klib.D2R(174.8 - 13797.39 * T));
        ramuda += 0.0024 * sin(klib.D2R( 82.7 +   998.63 * T));
        ramuda += 0.0024 * sin(klib.D2R(  4.7 +  9224.66 * T));
        ramuda += 0.0022 * sin(klib.D2R(121.4 -  8185.36 * T));
        ramuda += 0.0021 * sin(klib.D2R(134.4 +  9903.97 * T));

        ramuda += 0.0021 * sin(klib.D2R(173.1 +   719.98 * T));
        ramuda += 0.0021 * sin(klib.D2R(100.3 -  3413.37 * T));
        ramuda += 0.0020 * sin(klib.D2R(248.6 -    19.34 * T));
        ramuda += 0.0018 * sin(klib.D2R( 98.1 +  4013.29 * T));
        ramuda += 0.0016 * sin(klib.D2R(344.1 + 18569.38 * T));

        ramuda += 0.0012 * sin(klib.D2R( 52.1 - 12678.71 * T));
        ramuda += 0.0011 * sin(klib.D2R(250.3 + 19208.02 * T));
        ramuda += 0.0009 * sin(klib.D2R( 81.0 -  8586.0  * T));
        ramuda += 0.0008 * sin(klib.D2R(207.0 + 14037.3  * T));
        ramuda += 0.0008 * sin(klib.D2R( 31.0 -  7906.7  * T));

        ramuda += 0.0007 * sin(klib.D2R(346.0 +  4052.0  * T));
        ramuda += 0.0007 * sin(klib.D2R(294.0 -  4853.3  * T));
        ramuda += 0.0007 * sin(klib.D2R( 90.0 +   278.6  * T));
        ramuda += 0.0006 * sin(klib.D2R(237.0 +  1118.7  * T));
        ramuda += 0.0005 * sin(klib.D2R( 82.0 + 22582.7  * T));

        ramuda += 0.0005 * sin(klib.D2R(276.0 + 19088.0 * T));
        ramuda += 0.0005 * sin(klib.D2R( 73.0 - 17450.7 * T));
        ramuda += 0.0005 * sin(klib.D2R(112.0 +  5091.3 * T));
        ramuda += 0.0004 * sin(klib.D2R(116.0 -   398.7 * T));
        ramuda += 0.0004 * sin(klib.D2R( 25.0 -   120.1 * T));

        ramuda += 0.0004 * sin(klib.D2R(181.0 +  9584.7 * T));
        ramuda += 0.0004 * sin(klib.D2R( 18.0 +   720.0 * T));
        ramuda += 0.0003 * sin(klib.D2R( 60.0 -  3814.0 * T));
        ramuda += 0.0003 * sin(klib.D2R( 13.0 -  3494.7 * T));
        ramuda += 0.0003 * sin(klib.D2R( 13.0 + 18089.3 * T));

        ramuda += 0.0003 * sin(klib.D2R(152.0 +  5492.0 * T));
        ramuda += 0.0003 * sin(klib.D2R(317.0 -    40.7 * T));
        ramuda += 0.0003 * sin(klib.D2R(348.0 + 23221.3 * T));

        var B: Double = 0.0267 * sin(klib.D2R(68.64 - 19.341 * T))
        B += 0.0043 * sin(klib.D2R(342.0 - 19.36 * T))
        B += 0.0040 * sin(klib.D2R(93.8 - 1.33 * T))
        B += 0.0020 * sin(klib.D2R(248.6 - 19.34 * T))
        B += 0.0005 * sin(klib.D2R(358.0 - 19.40 * T))

        var beta: Double = 5.1282 * sin(klib.D2R(236.231 + 4832.0202 * T + B))
        beta += 0.2806 * sin(klib.D2R(215.147 + 9604.0088 * T))
        beta += 0.2777 * sin(klib.D2R(77.316 + 60.0316 * T))
        beta += 0.1732 * sin(klib.D2R(4.563 - 4073.3220 * T))
        beta += 0.0554 * sin(klib.D2R(308.98 + 8965.374 * T))

        beta += 0.0463 * sin(klib.D2R(343.48 + 698.667 * T))
        beta += 0.0326 * sin(klib.D2R(287.90 + 13737.362 * T))
        beta += 0.0172 * sin(klib.D2R(194.06 + 14375.997 * T))
        beta += 0.0093 * sin(klib.D2R(25.6 - 8845.31 * T))
        beta += 0.0088 * sin(klib.D2R(98.4 - 4711.96 * T))

        beta += 0.0082 * sin(klib.D2R(1.1 - 3713.33 * T))
        beta += 0.0043 * sin(klib.D2R(322.4 + 5470.66 * T))
        beta += 0.0042 * sin(klib.D2R(266.8 + 18509.35 * T))
        beta += 0.0034 * sin(klib.D2R(188.0 - 4433.31 * T))
        beta += 0.0025 * sin(klib.D2R(312.5 + 8605.38 * T))

        beta += 0.0022 * sin(klib.D2R(291.4 + 13377.37 * T))
        beta += 0.0021 * sin(klib.D2R(340.0 + 1058.66 * T))
        beta += 0.0019 * sin(klib.D2R(218.6 + 9244.02 * T))
        beta += 0.0018 * sin(klib.D2R(291.8 - 8206.68 * T))
        beta += 0.0018 * sin(klib.D2R(52.8 + 5192.01 * T))

        beta += 0.0017 * sin(klib.D2R(168.7 + 14496.06 * T))
        beta += 0.0016 * sin(klib.D2R(73.8 + 420.02 * T))
        beta += 0.0015 * sin(klib.D2R(262.1 + 9284.69 * T))
        beta += 0.0015 * sin(klib.D2R(31.7 + 9964.00 * T))
        beta += 0.0014 * sin(klib.D2R(260.8 - 299.96 * T))

        beta += 0.0013 * sin(klib.D2R(239.7 + 4472.03 * T))
        beta += 0.0013 * sin(klib.D2R(30.4 + 379.35 * T))
        beta += 0.0012 * sin(klib.D2R(304.9 + 4812.68 * T))
        beta += 0.0012 * sin(klib.D2R(12.4 - 4851.36 * T))
        beta += 0.0011 * sin(klib.D2R(173.0 + 19147.99 * T))

        beta += 0.0010 * sin(klib.D2R(312.9 - 12978.66 * T))
        beta += 0.0008 * sin(klib.D2R(1.0 + 17870.70 * T))
        beta += 0.0008 * sin(klib.D2R(190.0 + 9724.10 * T))
        beta += 0.0007 * sin(klib.D2R(22.0 + 13098.70 * T))
        beta += 0.0006 * sin(klib.D2R(117.0 + 5590.70 * T))

        beta += 0.0006 * sin(klib.D2R(47.0 - 13617.30 * T))
        beta += 0.0005 * sin(klib.D2R(22.0 - 8465.30 * T))
        beta += 0.0005 * sin(klib.D2R(150.0 + 4193.40 * T))
        beta += 0.0004 * sin(klib.D2R(119.0 - 9483.90 * T))
        beta += 0.0004 * sin(klib.D2R(246.0 + 23281.30 * T))

        beta += 0.0004 * sin(klib.D2R(301.0 + 10242.60 * T))
        beta += 0.0004 * sin(klib.D2R(126.0 + 9325.40 * T))
        beta += 0.0004 * sin(klib.D2R(104.0 + 14097.40 * T))
        beta += 0.0003 * sin(klib.D2R(340.0 + 22642.70 * T))
        beta += 0.0003 * sin(klib.D2R(270.0 + 18149.40 * T))

        beta += 0.0003 * sin(klib.D2R(358.0 - 3353.30 * T))
        beta += 0.0003 * sin(klib.D2R(148.0 + 19268.00 * T))

        return PointD(klib.D2R(ramuda), klib.D2R(beta))
    }
}