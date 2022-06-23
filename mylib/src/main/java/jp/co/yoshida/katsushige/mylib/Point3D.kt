package jp.co.yoshida.katsushige.mylib

import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Point3D() {

    var x = 0.0
    var y = 0.0
    var z = 0.0

    /**
     * Constructors
     */
    constructor(x: Int, y: Int, z: Int): this() {
        this.x = x.toDouble()
        this.y = y.toDouble()
        this.z = z.toDouble()
    }

    /**
     * Constructors
     */
    constructor(x: Double, y: Double, z:Double): this() {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(p: Point3D): this() {
        this.x = p.x
        this.y = p.y
        this.z = p.z
    }

    override fun toString(): String {
        return x.toString() + "," + y.toString() + "," + z.toString()
    }

    /**
     * XY座標を2次元変換
     * return   2D XY座標
     */
    fun toPointXY() : PointD {
        return PointD(x, y)
    }

    /**
     * XY座標を2次元変換
     * return   2D YZ座標
     */
    fun toPointYZ() : PointD {
        return PointD(y, z)
    }

    /**
     * XY座標を2次元変換
     * return   2D ZX座標
     */
    fun toPointZX() : PointD {
        return PointD(z, x)
    }

    /**
     * 座標を0クリアする
     */
    fun clear() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    /**
     * 座標値が0かを確認する
     */
    fun isEmpty(): Boolean {
        if (x == 0.0 && y == 0.0 && z == 0.0)
            return true
        else
            return false
    }

    /**
     * 長さ、原点からの距離
     * return       長さ
     */
    fun length(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * 座標データの符号を反転する
     */
    fun inverse() {
        this.x *= -1.0
        this.y *= -1.0
        this.z *= -1.0
    }

    /**
     * 座標をオフセット分移動させる
     */
    fun offset(offset: Point3D) {
        this.x += offset.x;
        this.y += offset.y;
        this.z += offset.z;
    }

    /**
     * この点で表されるベクトルと指定されたベクトルの間の角度(rad)を計算
     * 原点を中心とした2点の角度(内積から角度を求める)
     * p            対象ベクトル
     * return       角度(rad)
     */
    fun angle(p: Point3D): Double {
        val c = (x * p.x + y * p.y + z * p.z) /
                (sqrt(x * x + y * y + z * z) * sqrt(p.x * p.x + p.y * p.y + p.z * p.z))
        return acos(c)
    }

    /**
     * 原点を起点とした2つのベクトルの外積
     * p            対象ベクトル
     * return       外積
      */
    fun crossProduct(p: Point3D): Point3D {
        return Point3D(y * p.z - z * p.y, z * p.x - x * p.z, x * p.y - y * p.z)
    }

    /**
     * 指定点との距離
     * return       距離
     */
    fun distance(p: Point3D): Double{
        val dx = x - p.x
        val dy = y - p.y
        val dz = z - p.z
        return sqrt(dx * dx + dy * dy + dz *dz)
    }

    /**
     * 比較して大きい値を返す
     * p        比較座標
     * return   比較値
      */
    fun max(p: Point3D): Point3D {
        return Point3D(max(x, p.x), max(y, p.y), max(z, p.z))
    }

    /**
     * 比較して大きい値を返す
     * p        比較座標
     * return   比較値
     */
    fun min(p: Point3D): Point3D {
        return Point3D(min(x, p.x), min(y, p.y), min(z, p.z))
    }

}