package jp.co.yoshida.katsushige.mylib

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class LineD() {

    var ps = PointD()
    var pe = PointD()
    private var mEps = 1E-8;

    /**
     *  コンストラクタ
     */
    constructor(psx: Double, psy: Double, pex: Double, pey: Double): this() {
        ps.x = psx
        ps.y = psy
        pe.x = pex
        pe.y = pey
    }

    /**
     *  コンストラクタ(2点 PointD)
     */
    constructor(ps: PointD, pe: PointD): this() {
        this.ps.x = ps.x
        this.ps.y = ps.y
        this.pe.x = pe.x
        this.pe.y = pe.y
    }

    /**
     *  コンストラクタ(線分 LineD)
     */
    constructor(l: LineD): this() {
        this.ps.x = l.ps.x
        this.ps.y = l.ps.y
        this.pe.x = l.pe.x
        this.pe.y = l.pe.y
    }

    /**
     *  コンストラクタ(端点とベクトル)
     */
    constructor(ps: PointD,l: Double, th: Double): this() {
        this.ps = ps
        pe.x = ps.x + l * Math.cos(th);
        pe.y = ps.x + l * Math.sin(th);
    }

    /**
     * ベクトル値を返す
     */
    fun vector(): PointD {
        return PointD(pe.x - ps.x, pe.y - ps.y)
    }

    /**
     * RectDに変換する
     */
    fun toRectD(): RectD {
        return RectD(ps, pe)
    }

    /**
     * 線分の長さ
     */
    fun length(): Double {
        var v = vector()
        return sqrt(v.x * v.x + v.y *v.y)
    }

    /**
     * 線分の角度(-π ～ π)
     */
    fun angle(): Double {
        var v = vector()
        return atan2(v.y, v.x)
    }

    /**
     * 線分と点との角度
     */
    fun angle(p: PointD): Double {
        val l = LineD(ps, p)
        return angle(l)
    }

    /**
     * 線分との角度(0 ～ π)
     */
    fun angle(l: LineD): Double {
        return abs(angle() - l.angle())
    }

    /**
     * 平行線の判定
     */
    fun isParalell(l: LineD): Boolean {
        val v1 = vector()
        val v2 = l.vector()
        return abs(v1.x * v2.y - v2.x * v1.y) < mEps
    }

    /**
     * 点との垂線の距離
     */
    fun pointDistance(p: PointD): Double {
        val l = LineD(ps, p)
        return l.length() * Math.sin(angle(p))
    }

    /**
     * 垂点の座標
     */
    fun intersectPoint(p: PointD): PointD {
        val l = ps.distance(p)
        val ll = l * Math.cos(angle(p))
        val a = angle()
        return PointD(ps.x + ll * Math.cos(a), ps.y + ll * Math.sin(a))
    }

}