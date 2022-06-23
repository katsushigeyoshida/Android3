package jp.co.yoshida.katsushige.planetapp

import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.Point3D
import jp.co.yoshida.katsushige.mylib.PointD
import kotlin.math.*

class PLANETDATA {
    //  軌道要素 2000年月1月1.5日(JD=2451545.0) 理科年表2021
    var name = "地球"                     //  惑星名
    var a = 1.0                         //  軌道長半径 semi-major axis(au)
    var e = 0.0167                      //  離心率 eccentricity
    var i = 0.003 * PI / 180.0          //  軌道傾斜 orbital inclination (黄道面)(rad)
    var v = 103.007 * PI / 180.0        //  Π/varpi 近日点黄経　longitude of periheion (rad)
    var W = 174.821 * PI / 180.0        //  Ω 昇交点黄経 ascending node (rad)
    var M0 = 179.912 * PI / 180.0;      //  元期平均近点角(mean anomaly)(rad)
    var P = 1.0                         //  公転周期 orbital period (ユリウス年365.25日)
    var TT = 2459400.5                  //  元期(epoch) 2021年7月5日のユリウス日

    var M = Array(3, {arrayOf<Double>(0.0,0.0)})

    val klib = KLib()

    constructor(name: String, a: Double, e: Double, i: Double, v:Double, W: Double, m0: Double, p: Double, tt: Double) {
        this.name = name
        this.a = a
        this.e = e
        this.i = klib.D2R(i)
        this.v = klib.D2R(v)
        this.W = klib.D2R(W)
        this.M0 = klib.D2R(m0)
        this.P = p
        this.TT = tt
        setMatrixPara()
    }

    /**
     * 公転周期を日に変換
     * return   公転周期(日)
     */
    fun periodDays(): Double {
        return P * 365.25
    }

    /**
     * 平均近点角 M (rad)を求める
     * jd       ユリウス日
     * return   平均近点角(rad)
     */
    fun meanAnomary(jd: Double): Double {
        //val n = 0.9856076700000015 / a.pow(1.5);    //  deg
        val n =  0.01720209895 / a.pow(1.5);    //  rad
        return abs(M0 + n * (jd - TT))
    }

    /**
     * 離心近点角 E (rad)を求める
     * jd       ユリウス日
     * return   離心近点角 E (rad)
     */
    fun eccentricAnomary(jd: Double): Double {
        val M = meanAnomary(jd);
        return kepler(M, e)
    }

    /**
     * 日心軌道面座標を求める
     * E        離心近点角(rad)
     * return   日心軌道面座標
     */
    fun heriocentricOrbit(E: Double): PointD {
        val b = a * sqrt(1 - e * e)
        return PointD(a * (cos(E) - e), b * sin(E))
    }

    /**
     * 日心軌道面座標から日心黄道座標に変換する
     * op       日心軌道面座標
     * return   日心黄道座標
     */
    fun heriocentricOrbit2Ecliptic(op: PointD): Point3D {
        var ep = Point3D()
        ep.x = M[0][0] * op.x + M[0][1] * op.y
        ep.y = M[1][0] * op.x + M[1][1] * op.y;
        ep.z = M[2][0] * op.x + M[2][1] * op.y;
        return ep;
    }

    /**
     * 惑星の位置(日心黄道座標)
     * jd       ユリウス日
     * return   黄道座標
     */
    fun getPlanetPos(jd: Double): Point3D {
        val M = meanAnomary(jd)
        val E = eccentricAnomary(jd)
        val op = heriocentricOrbit(E)
        return heriocentricOrbit2Ecliptic(op)
    }

    /**
     * 軌道面座標を黄道面座標に変換するパラメータをセット
     */
    fun setMatrixPara() {
        val cos_i = cos(i)              //  軌道傾斜角
        val sin_i = sin(i)
        val cos_Omega = cos(W)          //  昇交点経度
        val sin_Omega = sin(W)
        val cos_omega = cos(v - W)  //  近日点経度(近日点黄経 verpi - 昇交点経度W)
        val sin_omega = sin(v - W)
        M[0][0] =  cos_omega * cos_Omega - sin_omega * sin_Omega * cos_i
        M[0][1] = -sin_omega * cos_Omega - cos_omega * sin_Omega * cos_i
        M[1][0] =  cos_omega * sin_Omega + sin_omega * cos_Omega * cos_i
        M[1][1] = -sin_omega * sin_Omega + cos_omega * cos_Omega * cos_i
        M[2][0] =  sin_omega * sin_i
        M[2][1] =  cos_omega * sin_i
    }

    /**
     * ケプラーの方程式で離心近点角を求める
     * M        平均近点角(rad)
     * e        離心率
     * return   離心近点角 E (rad)
     */
    fun kepler(M: Double, e: Double): Double {
        var E0 = M              //  M 平均近点角(rad)
        var delta_E: Double
        var E: Double           //  E 離心近点角(rad)
        do {
            delta_E = (M - E0 + e * sin(E0)) / (1 - e * cos(E0));
            E = E0 + delta_E;
            E0 = E;
        } while (abs(delta_E) > 0.00001)
        return  E
    }
}