package jp.co.yoshida.katsushige.mapapp

import android.content.Context
import android.util.Log
import android.util.Size
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD

//  地図の位置情報
//  国土地理院のタイル画像を画面上に配置するためのクラス
//  ZoomLevel : 0-20 (使われているのは20まで) 世界全体を表すためのレベルで数字が大きいほど詳細が見れる
//              0だと一枚のタイル画像で世界全体を表示, 1だと2x2の4枚で表し,2だと3x3の9枚で表示する
//  MapTitle : 地図の種類、標準図や写真図などいろいろあり、MapInfoDataに記載
//  Ext : タイル画像の拡張子、主にpngで写真データはjpgになっている(MapInfoDataに記載)
//  CellSize : タイル画像の表示の大きさ、元データは256x256pixel
//  ColCount : 描画領域に表示するタイル画像の列数
//  RowCount : 描画領域に表示するタイル画像の行数、描画領域とColCountで決まる
//  Start : 描画領域に表示する左上のタイル画像の開始番号
//  View : 美容が領域のサイズ
//  座標系
//  Screen : 描画領域のスクリーン座標
//  BaseMap : ZoomLevel= 0 の時の座標、赤道の周長を1に換算して表す
//  Map : ZoomLevel ごとの座標、 BaseMap * 2^ZoomLevel となる
//  Coordinates : メルカトル図法による緯度経度座標(度)
//  メルカトル図法の緯度変換
//  幾何学的な円筒投影法だと f(Φ) = R x tan(Φ) であるが (Φ : 緯度(rad))
//  メルカトル図法では f(Φ) = R x ln(tan(π/4 + Φ/2)) で表される
//  逆変換は Φ = 2 x arcTan(exp(y/R)) - π/2
//
class MapData(var context: Context, var mMapInfoData: MapInfoData) {
    val TAG = "MapData"

    val mZoomName = listOf(                            //  ズームレベル(spinner表示用)
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20")
    val mColCountName = listOf(                       //  列数(spinner表示用)
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    var mMapTitle = "std"                             //  地図の名称
    var mMapTitleNum = 0                              //  地図の種類(No)
    var mExt = "png"                                  //  タイル画像の拡張子
    var mCellSize = 256f                              //  タイル画像の大きさ(辺の長さ)
    var mZoom = 0                                     //  ズームレベル
    var mColCount = 1                                 //  表示列数
    var mRowCount = 1                                 //  表示行数
    var mStart = PointD(0.0, 0.0)               //  表示開始座標(Map座標)
    var mView = Size(1000, 1000)          //  表示するViewの大きさ
    var mUrl = ""

    val klib = KLib()

    //  MapDataをCSVファイルに保存する時のデータ配列のタイトル
    companion object {
        val mMapDataFormat = listOf("地図名", "地図ID", "拡張子", "セルサイズ", "ズームレベル",
                "列数", "行数", "X座標", "Y座標")
    }

    /**
     * データのコピーを作成
     */
    fun copyTo():MapData {
        var mapData = MapData(context, mMapInfoData)
        mapData.mMapTitle = mMapTitle
        mapData.mMapTitleNum = mMapTitleNum
        mapData.mExt = mExt
        mapData.mCellSize =mCellSize
        mapData.mZoom = mZoom
        mapData.mColCount = mColCount
        mapData.mRowCount = mRowCount
        mapData.mStart = PointD(mStart.x, mStart.y)
        mapData.mView = Size(mView.width,  mView.height)
        mapData.mUrl = mUrl
        return mapData
    }

    /**
     *  クラスデータをテキストのリストで取得
     */
    fun getStringData(): List<String> {
        var dataList = mutableListOf<String>()
        dataList.add(mMapTitle)
        dataList.add(mMapTitleNum.toString())
        dataList.add(mExt)
        dataList.add(mCellSize.toString())
        dataList.add(mZoom.toString())
        dataList.add(mColCount.toString())
        dataList.add(mRowCount.toString())
        dataList.add(mStart.x.toString())
        dataList.add(mStart.y.toString())
        return dataList
    }

    /**
     *  テキストデータをクラスデータに設定
     */
    fun setStringData(data: List<String>) {
        mMapTitle    = data[0]
        mMapTitleNum = data[1].toInt()
        mExt         = data[2]
        mCellSize    = data[3].toFloat()
        mZoom        = data[4].toInt()
        mColCount    = data[5].toInt()
        mRowCount    = data[6].toInt()
        mStart.x     = data[7].toDouble()
        mStart.y     = data[8].toDouble()
    }

    /**
     *  クラスデータのチェック(正規化)
     */
    fun normarized(){
        mMapTitle = mMapInfoData.mMapData[mMapTitleNum][1]
        mExt = mMapInfoData.mMapData[mMapTitleNum][2]
        mZoom = Math.min(Math.max(mZoom, 0), 20)
        val maxColCount = getMaxColCount()
        mStart.x  = Math.min(Math.max(mStart.x, 0.0), maxColCount.toDouble())
        mStart.y  = Math.min(Math.max(mStart.y, 0.0), maxColCount.toDouble())
        mColCount = Math.min(Math.max(mColCount, 1), 20)
        mColCount = Math.min(mColCount, maxColCount)
        mCellSize = getCellSize().toFloat()
        mRowCount = getRowCount().toInt()
        mUrl = mMapInfoData.mMapData[mMapTitleNum][7]
    }

    /**
     *  画面の移動
     *  dx,dy : MAP座標の増分
     */
    fun setMove(dx: Double, dy: Double) {
        mStart.x = mStart.x + dx
        mStart.y = mStart.y + dy
        normarized()
    }

    /**
     *  ズームアップした時の表示位置を更新
     *  curZoom     変更前のzoom値
     *  nextZoom    変更後のzoom値
     */
    fun setZoomUpPos(curZoom: Int, nextZoom: Int) {
        setZoom(nextZoom - curZoom)
    }

    /**
     *  地図の中心で拡大縮小
     *  dZoom : Zoomの増分
     */
    fun setZoom(dZoom: Int) {
        var ctr = PointD(mStart.x + mColCount / 2.0, mStart.y + getRowCountF() / 2.0)
        setZoom(dZoom, ctr)
    }

    /**
     *  指定店を中心に拡大縮小
     *  dZoom   Zoom増分
     *  ctr     拡大縮小の中心点(Map座標)
     */
    fun setZoom(dZoom: Int, ctr: PointD) {
        mStart.x = ctr.x * Math.pow(2.0, dZoom.toDouble()) - mColCount / 2.0
        mStart.y = ctr.y * Math.pow(2.0, dZoom.toDouble()) - getRowCountF() / 2.0
        mZoom += dZoom
        normarized()
    }

    /**
     * ズームレベルを変更したときのMap座標を求める
     * nextZoom     変更するズームレベル
     * mp           変換元のMap座標
     * return       変換後のMap座標
     */
    fun cnvMapPositionZoom(nextZoom: Int, mp: PointD): PointD {
        var cmp = PointD()
        cmp.x = mp.x * Math.pow(2.0, nextZoom.toDouble() - mZoom.toDouble())
        cmp.y = mp.y * Math.pow(2.0, nextZoom.toDouble() - mZoom.toDouble())
        return cmp
    }

    /**
     *  列数を変更したときの表示位置を更新
     *  curColCount     変更前の列数
     *  nextColCount    変更後の列数
     */
    fun setColCountUpPos(curColCount: Int, nextColCount: Int) {
        mStart.x += (nextColCount - curColCount) / 2
        mStart.y += (nextColCount - curColCount) / 2
        mColCount = nextColCount
    }

    /**
     *  BaseMap座標で指定した位置を中心に移動する
     *  location    移動座標(BaseMap座標)
     */
    fun setLocation(location: PointD) {
        var ctr = baseMap2Map(location)
        mStart.x = ctr.x - mColCount / 2.0
        mStart.y = ctr.y - getRowCountF() / 2.0
        Log.d(TAG, "setLocation: " + mStart.x + "," + mStart.y)
    }

    /**
     *  パラメータをプリファレンスに保存
     */
    fun saveParameter() {
        klib.setIntPreferences(mMapTitleNum, "MapDataID", context)
        klib.setIntPreferences(mZoom, "MapZoomLevel", context)
        klib.setIntPreferences(mColCount, "MapColCount", context)
        klib.setFloatPreferences(mStart.x.toFloat(), "MapStartX", context)
        klib.setFloatPreferences(mStart.y.toFloat(), "MapStartY", context)
    }

    /**
     *  プリファレンスから保存パラメートを取得
     */
    fun loadParameter() {
        mMapTitleNum = klib.getIntPreferences("MapDataID", context)
        mZoom = klib.getIntPreferences("MapZoomLevel", context)
        mColCount = klib.getIntPreferences("MapColCount", context)
        mStart.x = klib.getFloatPreferences("MapStartX", context).toDouble()
        mStart.y = klib.getFloatPreferences("MapStartY", context).toDouble()
        normarized()
    }

    /**
     *  Map座標の端数の取得
     */
    fun getStartOffset(): PointD {
        return PointD(mStart.x - Math.floor(mStart.x), mStart.y - Math.floor(mStart.y))
    }

    /**
     *  タイル画像のサイズを求める(一辺のながさ)
     *  return      タイル画像の1篇の長さ(スクリーン座標)
     */
    fun getCellSize(): Double {
        return if (mColCount === 1) {
            return if (mView.width < mView.height) mView.width.toDouble() else mView.height.toDouble()
        } else {
            return mView.width.toDouble() / mColCount.toDouble()
        }
    }

    /**
     *  画面縦方向のタイル数を求める(整数単位に切上げ)
     *  return  表示列数(Int)
     */
    fun getRowCount(): Double {
        return Math.ceil(mColCount.toDouble() * mView.height / mView.width)
    }

    /**
     *  画面縦方向のタイル数を求める(Double)
     *  return  表示列数(Double)
     */
    fun getRowCountF(): Double {
        return mColCount.toDouble() * mView.height / mView.width
    }

    /**
     *  ズームレベルでの最大列数(一周分)
     *  return  列数
     */
    fun getMaxColCount(): Int {
        return Math.pow(2.0, mZoom.toDouble()).toInt()
    }

    /**
     *  表示地図の中心座標(BaseMap座標)の取得
     *  return  中心座標(BaseMap座標)
     */
    fun getCenter(): PointD {
        return map2BaseMap(getMapCenter())
    }

    fun getMapCenter(): PointD {
        return PointD(mStart.x + mColCount.toDouble() / 2.0, mStart.y + getRowCountF() / 2.0)
    }

    /**
     * 描画領域をBaseMap座標で取得
     * return   描画領域
     */
    fun getArea(): RectD {
        var bsp = map2BaseMap(mStart)
        var bep = map2BaseMap(PointD(mStart.x + mColCount, mStart.y + getRowCountF()))
        return RectD(bsp, bep)
    }

    /**
     * 描画領域を緯度経度座標で取得
     * return   描画領域
     */
    fun getAreaCoordinates(): RectD {
        var bsp = klib.baseMap2Coordinates(map2BaseMap(mStart))
        var bep = klib.baseMap2Coordinates(map2BaseMap(PointD(mStart.x + mColCount, mStart.y + getRowCountF())))
        return RectD(bsp, bep)
    }

    /**
     *  BaseMap座標をスクリーン座標に変換
     *  bp      BaseMap座標
     *  return  スクリーン座標
     */
    fun baseMap2Screen(bp: PointD): PointD {
        var mp = baseMap2Map(bp)
        return map2Screen(mp)
    }

    /**
     *  BaseMap座標をMap座標に変換
     *  bp      BaseMap座標
     *  retrurn Map座標
     */
    fun baseMap2Map(bp: PointD): PointD {
        var mp = PointD(0.0, 0.0)
        mp.x = bp.x * Math.pow(2.0, mZoom.toDouble())
        mp.y = bp.y * Math.pow(2.0, mZoom.toDouble())
        return mp
    }

    /**
     *  Map座標をスクリーン座標に変換
     *  mp      Map座標
     *  return  スクリーン座標
     */
    fun map2Screen(mp: PointD): PointD {
        var sp = PointD(0.0, 0.0)
        sp.x = (mp.x - mStart.x) * mCellSize
        sp.y = (mp.y - mStart.y) * mCellSize
        return sp
    }

    //  スクリーン座標をBaseMap座標に変換
    fun screen2BaseMap(sp: PointD): PointD {
        var mp = screen2Map(sp)
        return map2BaseMap(mp)
    }

    /**
     *  スクリーン座標をMap座標に変換
     *  sp      スクリーン座標
     *  return  Map座標
     */
    fun screen2Map(sp: PointD): PointD{
        var x = mStart.x + sp.x / mCellSize
        var y = mStart.y + sp.y / mCellSize
        return PointD(x, y)
    }

    /**
     *  Map座標をBaseMap座標に変換
     *  mp      Map座標
     *  return  BaseMap座標
     */
    fun map2BaseMap(mp: PointD): PointD {
        var x = mp.x / Math.pow(2.0, mZoom.toDouble())
        var y = mp.y / Math.pow(2.0, mZoom.toDouble())
        return PointD(x, y)
    }

    /**
     *  緯度経度座標(度)からメルカトル図法での距離を求める
     *  cp      cp.x : 経度、 cp.y : 緯度
     *  return  BaseMap座標
     */
    fun coordinates2BaseMap(cp: PointD): PointD {
        //  座標変換
        return PointD(
                cp.x / 360.0 + 0.5,
                0.5 - 0.5 / Math.PI * Math.log(Math.tan(Math.PI * (1 / 4.0 + cp.y / 360.0))))
    }

    /**
     *  メルカトル図法での距離から緯度経度座標(度)を求める
     *  bp.X : 経度方向の距離、 bp.Y : 緯度方向の距離
     *  return : BaseMap上の位置
     */
    fun baseMap2Coordinates(bp: PointD): PointD {
        var cp = PointD(Math.PI * (2.0 * bp.x - 1),
                2.0 * Math.atan(Math.exp((0.5 - bp.y) * 2.0 * Math.PI)) - Math.PI / 2.0)
        //  rad → deg
        cp.x *= 180.0 / Math.PI
        cp.y *= 180.0 / Math.PI
        return cp
    }
}