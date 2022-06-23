package jp.co.yoshida.katsushige.mylib

class CircleD() {
    var cp = PointD()
    var r = 0.0

    /**
     * Constructors
     */
    constructor(x: Double, y: Double, r: Double): this() {
        cp = PointD(x, y)
        this.r = r
    }

    /**
     * Constructors
     */
    constructor(cp: PointD, r: Double): this() {
        this.cp = PointD(cp.x, cp.y)
        this.r = r
    }

    override fun toString(): String {
        return "(" + cp.toString() + ")," + r
    }

    /**
     * 4頂点をListに変換
     */
    fun toPeakPoint(): List<PointD>{
        var cpList = mutableListOf<PointD>()
        cpList.add(PointD(cp.x + r, cp.y))
        cpList.add(PointD(cp.x, cp.y + r))
        cpList.add(PointD(cp.x - r, cp.y))
        cpList.add(PointD(cp.x, cp.y - r))
        return cpList
    }

    /**
     * RectDに変換
     */
    fun toRectD(): RectD {
        return RectD(PointD(cp.x + r, cp.y + r), PointD(cp.x - r, cp.y - r))
    }
}