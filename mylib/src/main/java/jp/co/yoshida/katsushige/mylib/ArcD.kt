package jp.co.yoshida.katsushige.mylib

import android.graphics.RectF
import kotlin.math.PI

class ArcD() {
    var cp = PointD()
    var r = 0.0
    var sa = 0.0
    var ea = PI * 2.0

    /**
     * Constructors
     */
    constructor(x: Double, y: Double, r: Double, sa: Double = 0.0, ea: Double = PI * 2.0): this() {
        cp = PointD(x, y)
        this.r = r
        this.sa = sa
        this.ea = ea
    }

    /**
     * Constructors
     */
    constructor(cp: PointD, r: Double, sa: Double = 0.0, ea: Double = PI * 2.0): this() {
        this.cp = PointD(cp.x, cp.y)
        this.r = r
        this.sa = sa
        this.ea = ea
    }

    override fun toString(): String {
        return "(" + cp.toString() + ")," + r + "," + sa + "," + ea
    }

    /**
     * RectDに変換
     */
    fun toRectD(): RectD {
        return RectD(toPeakPoint())
    }

    /**
     * RectFに変換
     */
    fun toRectF(): RectF {
        return RectD(toPeakPoint()).toRectF()
    }

    /**
     * 頂点をListに変換
     */
    fun toPeakPoint(): List<PointD> {
        var sp = PointD(cp.x + r, cp.y)
        sp.rotatePoint(cp,sa)
        var ep = PointD(cp.x + r, cp.y)
        ep.rotatePoint(cp, ea)
        var cir = CircleD(cp, r)
        var cpList = cir.toPeakPoint()          //  円としての頂点座標
        var plist = mutableListOf<PointD>()
        var saq = angleQuadrant(sa)             //  端点の象限
        var eaq = angleQuadrant(ea)
        if (eaq < saq || (saq == eaq && ea < sa)) {
            eaq += 4
        }
        plist.add(sp)
        for (i in (saq + 1)..eaq) {
            plist.add(cpList[i % 4])
        }
        if (plist[plist.lastIndex].x != ep.x || plist[plist.lastIndex].y != ep.y)
            plist.add(ep)
        return plist;
    }

    fun angleQuadrant(ang: Double): Int {
        var angq = (ang + PI * 2.0) % (PI * 2.0);
        return (angq / (PI / 2.0)).toInt()
    }
}