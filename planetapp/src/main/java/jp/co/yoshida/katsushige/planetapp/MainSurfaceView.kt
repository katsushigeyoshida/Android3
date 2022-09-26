package jp.co.yoshida.katsushige.planetapp

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import jp.co.yoshida.katsushige.mylib.*
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sqrt

class MainSurfaceView: SurfaceView, SurfaceHolder.Callback {
    val TAG = "MainSurfaceView"

    var mLeftMargin   = 10.0                //  左マージン(スクリーンサイズ)
    var mTopMargin    = 10.0                //  下部(倒立時)マージン(スクリーンサイズ)
    var mRightMargine = 10.0                //  右マージン(スクリーンサイズ)
    var mBottomMargin = 10.0                //  上部(倒立時)マージン(スクリーンサイズ)
    var mWindowScale  = 1.0                 //  表示倍率
    var mWindowOffset = PointD()            //  移動量
    var mRadius       = 100.0               //  天球面の半径
    var mRadiusMargin = 1.2                 //  天球面の半径のマージン
    var mDirection    = 0.0                 //  表示方向 0: 北　1: 東 2: 南 3: 西
    val mAzimuthName = listOf("南", "西", "北", "東")   //  方位表示名
    var mScaleTextSize = 35.0               //  目盛りの文字の大きさ
    var mScaleLargeTextSize = 45.0          //  目盛りの文字の大きさ(大)
    var mStarNameTextSize = 25.0            //  恒星名の文字の大きさ
    val mInfoTextSize = 25.0                //  滋養法表示の文字の大きさ
    val mStarRadiusRate = 6.0               //  恒星の表示系の比率
    var mStarNameColor = "Black"            //  恒星名の色
    var mConstallaLineColor = "LightGray"   //  星座線の色
    var mConstallaNameColor = "Blue"        //  星座名の色
    var mNebulaNameColor = "Gray"           //  星雲・銀河名などの名の色
    var mStarDispMagnitude = 6.0            //  恒星を表示する視等級
    var mStarNameDispMagnitude = 3.0        //  恒星名を表示する視等級
    var mConstallaLineThickness = 2.0       //  星座線の幅
    var mConstallaLineDisp = false          //  星座線の表示フラグ
    var mConstallaNameDisp = false          //  星座名の表示フラグ
    var mNebulaDisp = false                 //  星雲・銀河などの表示フラグ
    var mPlanetDisp = false                 //  惑星表示

    var mLocalDateTime = LocalDateTime.now()
    var mLocalLatitude = 35.6581            //  観測点の緯度(東京)
    var mLocalLongitude = 139.7414          //  観測点の経度(東京)
    var mLst = 0.0                          //  地方恒星時
    val mSolarSystemSize = 1.2              //  太陽系のサイズ(AU)
    val mPlanetName = listOf(               //  惑星名
        "水星", "金星", "地球", "火星", "木星", "土星", "天王星", "海王星"
    )
    val mPlanetColor = listOf(              //  惑星の色
        "Silver", "Gold", "Blue", "Magenta", "Chocolate", "Yellow", "Aqua", "DarkBlue"
    )
    var mPtlemaicList = mutableListOf<MutableList<Point3D>>()   //  惑星の軌跡データ
    var mRollX = 0.0                        //  X軸回転角(deg)

    lateinit var mStarData: List<StarData.STARDATA>
    lateinit var mConstellaData: ConstellationData
    lateinit var mNebulaData: List<NebulaData.NEBULADATA>
    lateinit var mStarStrData: List<List<String>>    //  恒星ファイルデータ
    lateinit var mNebulaStrData: List<List<String>>  //  星雲・銀河などファイルデータ
    lateinit var mStarInfoData: StarInfoData

    val klib = KLib()
    val kdraw = TableDraw()
    val alib = AstroLib()
    val plib = PlanetLib()

    constructor(context: Context) : super(context) {
        Log.d(TAG,"MainSurfaceView: constractor: "+width + " " + height)

        kdraw.initSurface(holder, this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: "+ width + " " + height)

        kdraw.setInitScreen(width, height)  //  スクリーンサイズの設定
        kdraw.mScreenInverted = true        //  倒立表示
        kdraw.mView = RectD(mLeftMargin, mTopMargin, width - mRightMargine, height - mBottomMargin) //  ビュー領域を設定
        kdraw.mWorld = RectD(-mRadius, mRadius, mRadius, -mRadius)  //  ワールド領域を初期設定
        kdraw.mTextSize = 30f               //  文字サイズを初期設定
        kdraw.mTextStrokeWidth = 2f         //  文字太さを初期設定
        kdraw.initSurfaceScreen(Color.LTGRAY, width, height)    //  urfaceView画面の初期化
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }

    fun initTable(listData: List<List<String>>) {
        kdraw.setSheetData(listData)        //  データ設定
        kdraw.mScreenInverted = false       //  倒立表示なし
        kdraw.mTableTextSize = 30f
    }

    /**
     * 恒星データを表で表示
     */
    fun drawTable() {
        kdraw.lockCanvas()
        kdraw.backColor(Color.WHITE)
        kdraw.drawTable()
        kdraw.unlockCanvasAndPost()
    }

    /**
     * Planet天球面表示
     */
    fun drawCelestial() {
        //  表示データ情報
        val gst = getGst(mLocalDateTime)
        mLst = getLst(gst)
        var info = getInfo(mLocalDateTime, gst, mLst)

        //  恒星、星座データの表示
        mRadius = 100.0
        initArea(false)
        drawCelestialBackGround(mLst)

        var count = drawCelestialStar(mStarData)
        if (mConstallaLineDisp)
            drawCelestialConstellaLine(mConstellaData)
        if (mConstallaNameDisp)
            drawCelestialConstellaName(mConstellaData)
        if (mNebulaDisp)
            drawCelestialNebula(mNebulaData)

        info += " 星数: %d".format(count)
        drawInfo(info)
    }

    /**
     * Planet地平座標表示
     */
    fun drawHorizontal() {
        //  表示データ情報
        val gst = getGst(mLocalDateTime)
        mLst = getLst(gst)
        var info = getInfo(mLocalDateTime, gst, mLst)

        //  恒星、星座データの表示
        mRadius = 100.0
        initArea(true)
        mStarInfoData.mSearchData.clear()
        drawHorizontalBackGround()

        var count = drawHorizontalStar(mStarData, mLst, alib.D2R(mLocalLatitude))
        if (mConstallaLineDisp)
            drawHorizontalConstellaLine(mConstellaData, mLst, alib.D2R(mLocalLatitude))
        if (mConstallaNameDisp)
            drawHorizontalConstellaName(mConstellaData, mLst, alib.D2R(mLocalLatitude))
        if (mNebulaDisp)
            count += drawHorizontalNebula(mNebulaData, mLst, alib.D2R(mLocalLatitude))
        if (mPlanetDisp)
            drawHorizontalPlanet(mLst, alib.D2R(mLocalLatitude))

        info += " 星数: %d".format(count)
        drawInfo(info)
    }

    /**
     * 全天表示
     */
    fun drawFullHorizontal() {
        //  表示データ情報
        val gst = getGst(mLocalDateTime)
        mLst = getLst(gst)
        var info = getInfo(mLocalDateTime, gst, mLst)

        //  恒星、星座データの表示
        mRadius = 100.0
        initArea(false)
        mStarInfoData.mSearchData.clear()
        drawFullHorizontalBackGround()

        var count = drawHorizontalStar(mStarData, mLst, alib.D2R(mLocalLatitude), true)
        if (mConstallaLineDisp)
            drawHorizontalConstellaLine(mConstellaData, mLst, alib.D2R(mLocalLatitude), true)
        if (mConstallaNameDisp)
            drawHorizontalConstellaName(mConstellaData, mLst, alib.D2R(mLocalLatitude), true)
        if (mNebulaDisp)
            count += drawHorizontalNebula(mNebulaData, mLst, alib.D2R(mLocalLatitude), true)
        if (mPlanetDisp)
            drawHorizontalPlanet(mLst, alib.D2R(mLocalLatitude), true)

        info += " 星数: %d".format(count)
        drawInfo(info)
    }

    /**
     * 太陽系の表示
     * geo      地球中心表示
     */
    fun drawSolarSystem(geo: Boolean = false) {
        mRadius = mSolarSystemSize
        var info = getInfo(mLocalDateTime, 0.0, 0.0) + "　回転角 " + "%.1f".format(mRollX)

        initArea(false,false)

        drawSolarBackGround(geo)
        drawPlanet(geo)
        drawSolarLegend()

        drawInfo(info)
    }

    /**
     * グリニッジ恒星時の取得
     * nt       ローカルタイム
     * return   恒星時(hour)
     */
    fun getGst(nt: LocalDateTime): Double {
        return alib.getGreenwichSiderealTime(nt.year, nt.monthValue, nt.dayOfMonth, nt.hour, nt.minute, nt.second)
    }

    /**
     * 地方恒星時の取得
     * gst      恒星時(hour)
     * return   地方恒星時(hour)
     */
    fun getLst(gst:Double): Double {
        return alib.getLocalSiderealTime(gst, mLocalLongitude - 135.0)
    }

    /**
     * 情報データの文字列(日時,GST,LST,観測点緯度)
     * nt       ローカルタイム
     * get      恒星時(hour)
     * lst      地方恒星時(hour)
     * return   情報文字列
     */
    fun getInfo(nt: LocalDateTime, gst: Double, lst: Double): String {
        var info = "" + nt.year+"/" + nt.monthValue + "/" + nt.dayOfMonth + " " +
                "%02d".format(nt.hour) + ":" + "%02d".format(nt.minute) + ":" + "%02d".format(nt.second)
        if (0 < gst) {
            info += " GST: " + "%.4f".format(gst)
        }
        if (0 < lst) {
            info += " LST: " + "%.4f".format(lst)
        }
        info += " 緯度:"+"%.4f".format(mLocalLatitude)

        return info
    }

    /**
     * 領域を初期化
     * halfCircle       true: 地平座標系, false:天球面
     * limit            WindowsSizeの下限設定
     */
    fun initArea(halfCircle: Boolean, limit: Boolean = true) {
        //  ビュー領域とワールド領域を設定
        kdraw.mView = RectD(mLeftMargin, mTopMargin, width - mRightMargine, height - mBottomMargin)
        kdraw.mScreenInverted = true        //  倒立表示
        //  ワールド領域を初期設定
        if (halfCircle)
            kdraw.mWorld = RectD(-mRadius * mRadiusMargin, mRadius * mRadiusMargin,
                mRadius * mRadiusMargin, -mRadius * (mRadiusMargin - 1.0))
        else
            kdraw.mWorld = RectD(-mRadius * mRadiusMargin, mRadius * mRadiusMargin,
                mRadius * mRadiusMargin, -mRadius * mRadiusMargin)
        kdraw.setAspectFix()
        if (limit && mWindowScale < 1.0) {
            mWindowScale = 1.0
            mWindowOffset = PointD()
        }
        kdraw.mWorld.zoom(mWindowScale)
        kdraw.mWorld.offset(mWindowOffset)
        kdraw.mTextSize = 30f
    }

    /**
     * 天球面の背景表示
     */
    fun drawCelestialBackGround(lst: Double) {
        kdraw.lockCanvas()

        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setTextSize(30.0)
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))

        var cir = CircleD(PointD(0.0, 0.0), mRadius)
        kdraw.setProperty("Aqua", 2.0, Paint.Style.FILL)
        kdraw.drawWCircle(cir)
        kdraw.setProperty("Black", 2.0, Paint.Style.STROKE)
        kdraw.drawWCircle(cir)

        //  赤緯の補助線(度)
        kdraw.setTextSize(mScaleTextSize)
        for (i in -60 until 90 step 30) {
            val l = alib.declinationLength(alib.D2R(i.toDouble()), mRadius)
            kdraw.setProperty("LightGray", 2.0, Paint.Style.STROKE)
            kdraw.drawWCircle(cir.cp, l)
            kdraw.setColor("Black")
            kdraw.drawWText("%d°".format(i), PointD(l, 0.0))
        }
        //  赤経の補助線(時)
        for (ra in 0 until 24) {
            kdraw.setColor("Black")
            var ps = alib.equatorial2orthogonal(PointD(alib.H2R(ra.toDouble()), alib.D2R(-90.0)), mDirection, mRadius)
            var pe = PointD(0.0, 0.0)
            var line = LineD(ps, pe)
            if (ra % 2 == 0) {
                val p = alib.equatorial2orthogonal(PointD(alib.H2R(ra.toDouble()), alib.D2R(-90.0 * 1.1 )), mDirection, mRadius)
                kdraw.drawWText("%02dh".format(ra % 24), p, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Center)
                kdraw.setColor("LightGray")
                kdraw.drawWLine(line)
            }
        }

        //  地平線表示
        var hour = 0.0                      //  方位(hour)
        var height = 0.0                    //  高度(rad)
        var azimuth = alib.H2R(hour)        //  方位(rad)
        var hps = alib.equatorial2orthogonal(PointD(
                alib.equatorialRightAscension(azimuth, height, alib.D2R(mLocalLatitude), alib.H2R(lst)),
                alib.equatorialDeclination(azimuth, height, alib.D2R(mLocalLatitude))), mDirection, mRadius)
        var hpe = PointD()
        while (hour < 24.0) {
            if (hour % 6.0 == 0.0) {
                //  東西南北表示
                kdraw.setTextProperty(mScaleLargeTextSize, 1.0, "Yellow")
                kdraw.drawWText(mAzimuthName[(hour / 6).toInt() % 4], hps, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Center)
            }
            //  地平境界線
            hour += 0.5
            azimuth = alib.H2R(hour)
            hpe = alib.equatorial2orthogonal(PointD(
                alib.equatorialRightAscension(azimuth, height, alib.D2R(mLocalLatitude), alib.H2R(lst)),
                alib.equatorialDeclination(azimuth, height, alib.D2R(mLocalLatitude))), mDirection, mRadius)
            kdraw.setColor("Red")
            kdraw.drawWLine(hps, hpe)
            hps = hpe
        }

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 天球面での恒星表示
     */
    fun drawCelestialStar(starData: List<StarData.STARDATA>): Int {
        var count = 0
        if (starData.count() <= 1)
            return count
        kdraw.lockCanvas()
        for (star in starData) {
            var p = alib.equatorial2orthogonal(star.coordinate, mDirection, mRadius)
            if (p.length() < mRadius && (star.magnitude <= mStarDispMagnitude || mStarDispMagnitude <= 0.0)) {
                drawStar(p, star)
                count++
            }
        }
        kdraw.unlockCanvasAndPost()
        return count
    }

    /**
     * 天球面での星座線の表示
     * constellaData    星座データ
     */
    fun drawCelestialConstellaLine(constellaData: ConstellationData) {
        if (constellaData.mConstellaLineLineList.count() <= 1 ||
            constellaData.mConstellaStarList.count() <= 1)
            return
        kdraw.lockCanvas()
        kdraw.setProperty(mConstallaLineColor, mConstallaLineThickness, Paint.Style.STROKE)
        val starData = constellaData.mConstellaStarList
        for (lineData in constellaData.mConstellaLineLineList) {
            if (starData.containsKey(lineData.sHip) && starData.containsKey(lineData.eHip)) {
                val sStar = starData[lineData.sHip]
                val eStar = starData[lineData.eHip]
                if (alib.D2R(-80.0) < sStar!!.coordinate.y && alib.D2R(-80.0) < eStar!!.coordinate.y ) {
                    val sp = alib.equatorial2orthogonal(sStar.coordinate, mDirection, mRadius)
                    val ep = alib.equatorial2orthogonal(eStar.coordinate, mDirection, mRadius)
                    kdraw.drawWLine(sp, ep)
                }
            }
        }
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 天球面での星座名の表示
     * constellaData    星座データ
     */
    fun drawCelestialConstellaName(constellaData: ConstellationData) {
        if (constellaData.mConstellaNameList.count() <= 1)
            return
        kdraw.lockCanvas()
        kdraw.setTextProperty(mStarNameTextSize, 1.0, "Blue")
        for (constellaName in constellaData.mConstellaNameList) {
            var p = alib.equatorial2orthogonal(constellaName.coordinate, mDirection, mRadius)
            if (p.length() < mRadius) {
                drawConstellaName(p, constellaName)
            }
        }
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 天球面での星雲・銀河などの表示
     * nebulaData       星雲・銀河などのデータ
     * return           表示数
     */
    fun drawCelestialNebula(nebulaData: List<NebulaData.NEBULADATA>): Int {
        var count = 0
        if (nebulaData.count() <= 1)
            return count
        kdraw.lockCanvas()
        for (nebula in nebulaData) {
            var p = alib.equatorial2orthogonal(nebula.coordinate, mDirection, mRadius)
            if (p.length() < mRadius && (nebula.magnitude <= mStarDispMagnitude || mStarDispMagnitude <= 0.0)) {
                drawNebula(p,nebula)
                count++
            }
        }
        kdraw.unlockCanvasAndPost()
        return count
    }

    /**
     * 全天表示の背景設定
     */
    fun drawFullHorizontalBackGround() {
        kdraw.lockCanvas()

        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setTextSize(30.0)
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))
        //  背景の設定
        var cir = CircleD(PointD(0.0, 0.0), mRadius)
        kdraw.setProperty("Aqua", 2.0, Paint.Style.FILL)
        kdraw.drawWCircle(cir)
        kdraw.setProperty("Black", 2.0, Paint.Style.STROKE)
        kdraw.drawWCircle(cir)
        //  高度補助線
        var height = PI / 9.0
        kdraw.setColor("LightGray")
        while (height < PI / 2.0) {
            cir = CircleD(PointD(0.0, 0.0), mRadius * (1.0 - height / (PI / 2.0)))
            kdraw.drawWCircle(cir)
            height += PI / 9.0
        }
        //  方位補助線
        var scaleOffset = PI * kdraw.cnvScreen2WorldX(mScaleTextSize) / mRadius / 2.0 *1.5
        var largeScaleOffset = PI * kdraw.cnvScreen2WorldX(mScaleLargeTextSize) / mRadius / 2.0
        for (hour in 0..24) {
            if (hour % 2 == 0) {
                kdraw.setColor("LightGray")
                kdraw.drawWLine(PointD(), alib.cnvFullHorizontal(alib.H2R(hour.toDouble()),0.0, mDirection, mRadius))
                kdraw.setColor("Black")
                kdraw.setTextSize(mScaleTextSize)
                var p = alib.cnvFullHorizontal(alib.H2R(hour.toDouble()),-scaleOffset / 2.0, mDirection,mRadius)
                kdraw.drawWText("%02dh".format(hour % 24), p, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Center)
            }
            if (hour % 6 == 0) {
                kdraw.setColor("Black")
                kdraw.setTextSize(mScaleLargeTextSize)
                var p = alib.cnvFullHorizontal(alib.H2R(hour.toDouble()),-scaleOffset - largeScaleOffset / 2.0, mDirection,mRadius)
                kdraw.drawWText(mAzimuthName[klib.mod((hour / 6), 4)], p, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Center)
            }
            kdraw.setColor("Black")
            kdraw.drawWLine(alib.cnvFullHorizontal(alib.H2R(hour.toDouble()), PI / 36.0, mDirection, mRadius),
                alib.cnvFullHorizontal(alib.H2R(hour.toDouble()), 0.0, mDirection, mRadius))
        }

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 地平座標系での背景表示
     */
    fun drawHorizontalBackGround() {
        kdraw.lockCanvas()

        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setTextSize(30.0)
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))

        var arc = ArcD(PointD(0.0, 0.0), mRadius, klib.D2R(0.0), klib.D2R(180.0))
        kdraw.setProperty("Aqua", 2.0, Paint.Style.FILL)
        kdraw.drawWArc(arc)
        kdraw.setProperty("Black", 2.0, Paint.Style.STROKE)
        kdraw.mUseCenter = true
        kdraw.drawWArc(arc)

        //  目盛り表示
        //  経度線
        var azStep = 2.0
        var sp = mDirection - 6.0
        var ep = sp +12.0
        sp = ceil(sp / azStep) * azStep
        var az = sp
        while ( az <= ep) {
            var ps = alib.cnvHorizontal(alib.H2R(az.toDouble()), 0.0, mDirection, mRadius)
            var pe = alib.cnvHorizontal(alib.H2R(az.toDouble()), 0.05, mDirection, mRadius)
            var line = LineD(ps, pe)
            if ((az % 2.0) == 0.0) {
                kdraw.setColor("Black")
                kdraw.setTextSize(mScaleTextSize)
                kdraw.drawWText("%02dh".format(az.toInt() % 24), ps, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Top)
                kdraw.setColor("LightGray")
                var sp = ps
                for (h in 10..90 step 10) {
                    var tp = alib.cnvHorizontal(alib.H2R(az.toDouble()), alib.D2R(h.toDouble()), mDirection,mRadius)
                    kdraw.drawWLine(sp, tp)
                    sp = tp
                }
            }
            if ((az % 6.0) == 0.0) {
                kdraw.setColor("Black")
                kdraw.setTextSize(mScaleLargeTextSize)
                val p = PointD(ps.x,ps.y - kdraw.cnvScreen2WorldX(kdraw.mTextSize.toDouble()))
                kdraw.drawWText(mAzimuthName[klib.mod((az.toInt() / 6), 4)], p, 0.0, KDraw.HALIGNMENT.Center, KDraw.VALIGNMENT.Top)
            }
            kdraw.setColor("Black")
            kdraw.drawWLine(line)
            az += azStep
        }
        //  緯度線
        val latiStep = 20
        azStep=1.0
        for (lati in latiStep until 90 step latiStep) {
            az = sp
            while (az < ep) {
                var ps = alib.cnvHorizontal(alib.H2R(az), alib.D2R(lati.toDouble()), mDirection, mRadius)
                var pe = alib.cnvHorizontal(alib.H2R(((az + azStep) % 24.0).toDouble()), alib.D2R(lati.toDouble()), mDirection,mRadius)
                kdraw.setColor("LightGray")
                kdraw.drawWLine(ps, pe)
                az += azStep
            }
            //  緯度値表示
            kdraw.setColor("Black")
            kdraw.setTextSize(mScaleTextSize)
            var ps = alib.cnvHorizontal(alib.H2R(sp), alib.D2R(lati.toDouble()),mDirection, mRadius)
            kdraw.drawWText("%02d°".format(lati), ps, 0.0, KDraw.HALIGNMENT.Right, KDraw.VALIGNMENT.Bottom)
        }

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 惑星の軌道データの背景表示
     */
    fun drawSolarBackGround(geo: Boolean = false) {
        kdraw.lockCanvas()

        kdraw.matrixClear()
        kdraw.setRotateX(klib.D2R(mRollX))

        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setTextSize(30.0)
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))
        //  補助線
        kdraw.mColor = "LightGray"
        kdraw.draw3DWline(Point3D(-mSolarSystemSize, 0.0, 0.0), Point3D(mSolarSystemSize, 0.0, 0.0))
        kdraw.draw3DWline(Point3D(0.0, -mSolarSystemSize,  0.0), Point3D(0.0, mSolarSystemSize, 0.0))
        //  中心太陽表示
        kdraw.setProperty(if (geo) mPlanetColor[2] else "Red", 1.0, Paint.Style.FILL)
        kdraw.draw3DWCircle(Point3D(), kdraw.cnvScreen2WorldX(10.0))

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 惑星の軌道表示
     * geo      地球中心表示
     */
    fun drawPlanet(geo: Boolean = false) {
        kdraw.lockCanvas()

        val jd = alib.getJD(mLocalDateTime.year, mLocalDateTime.monthValue, mLocalDateTime.dayOfMonth)
        val earthData = plib.getPlanetData(mPlanetName[2])

        for (j in mPlanetName.indices) {
            val pData = plib.getPlanetData(mPlanetName[j])
//            Log.d(TAG, "drawPlanet: ${pData.name} ${pData.a} ${kdraw.mWorld.width()}")
            if (existPlanet(pData.a))
                continue
            kdraw.mColor = "Blue"
            var sp = pData.getPlanetPos(jd)
            if (geo) {
                if (j == 2) {
                    sp.inverse()
                } else {
                    val op = earthData.getPlanetPos(jd)
                    op.inverse()
                    sp.offset(op)
                }
            }

            var ssp = sp
            //  惑星の周回軌道
            kdraw.mColor = "Black"
            kdraw.mStrokWidth = 1.0.toFloat()
            var period = pData.periodDays()
            var i = 0;
            while(i < period) {
                var jd2 = jd + i
                val ep = pData.getPlanetPos(jd2)
                if (geo) {
                    if (j == 2) {
                        ep.inverse()
                    } else {
                        val op = earthData.getPlanetPos(jd)
                        op.inverse()
                        ep.offset(op)
                    }
                }
//                Log.d(TAG, "drawPlanet: "+sp.toString()+" "+ep.toString())
                kdraw.draw3DWline(sp,ep)
                sp = ep
                i += (period / 48).toInt()
            }
            //  惑星の位置表示
            kdraw.setProperty(if (geo && j ==2) "Red" else mPlanetColor[j], 1.0, Paint.Style.FILL_AND_STROKE)
            kdraw.draw3DWCircle(ssp, kdraw.cnvScreen2WorldX(8.0))

            mPtlemaicList[j].add(ssp)
            //  惑星の軌跡表示
            if (geo && 1 < mPtlemaicList[j].count()) {
                kdraw.mColor = if (j == 2) "Red" else mPlanetColor[j]
                kdraw.mStrokWidth = 2.0.toFloat()
                for (i in 0..(mPtlemaicList[j].lastIndex - 1)) {
                    kdraw.draw3DWline(mPtlemaicList[j][i], mPtlemaicList[j][i + 1])
                }
            }
        }

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 惑星の表示範囲チェック
     */
    fun existPlanet(a: Double): Boolean {
        return if (kdraw.mWorld.width() < a || a < kdraw.mWorld.width() * 0.03) true else false
    }


    /**
     * 惑星の軌跡データの初期化
     */
    fun ptolemaicInit() {
        mPtlemaicList.clear()
        for (i in mPlanetName.indices) {
            var buf = mutableListOf<Point3D>()
            mPtlemaicList.add(buf)
        }
    }

    /**
     * 太陽系の惑星凡例表示
     */
    fun drawSolarLegend() {
        kdraw.mTextSize = 30.0F
        var x = 30.0
        var y = kdraw.mView.bottom - kdraw.mTextSize * 1.2
        kdraw.setProperty("Red", 1.0, Paint.Style.FILL_AND_STROKE)
        var p = PointD(x, y)
        kdraw.drawCircle(p, 8.0)
        kdraw.setProperty("Black", 1.0, Paint.Style.FILL_AND_STROKE)
        p.offset(15.0, -10.0)
        kdraw.drawText("太陽", p)
        y -= kdraw.mTextSize * 1.2
        for (i in mPlanetName.indices) {
            val pData = plib.getPlanetData(mPlanetName[i])
            if (existPlanet(pData.a))
                continue
            var p = PointD(x, y)
            kdraw.drawCircle(p, 8.0)
            kdraw.setProperty(mPlanetColor[i], 1.0, Paint.Style.FILL_AND_STROKE)
            p.offset(15.0, -10.0)
            kdraw.drawText(mPlanetName[i], p)
            y -= kdraw.mTextSize * 1.2
        }
    }

    /**
     * 地平座標系での恒星の表示
     * starData         恒星データ
     * lst              地方恒星時(hh.hhh)
     * localLatitude    観測点緯度(rad)
     */
    fun drawHorizontalStar(starData: List<StarData.STARDATA>, lst: Double, localLatitude: Double, full: Boolean = false): Int {
        var count = 0
        if (starData.count() <= 1)
            return count
        kdraw.lockCanvas()
        for (star in starData) {
            var p = alib.convHorizontalPoint(star.coordinate, lst, localLatitude, mDirection, mRadius, full)
            if (!p.isEmpty() && p.length() < mRadius && (star.magnitude <= mStarDispMagnitude || mStarDispMagnitude <= 0.0)) {
                drawStar(p, star)
                count++
            }
        }
        kdraw.unlockCanvasAndPost()
        return count
    }

    /**
     * 地平座標系での星座線の表示
     * constellaData        星座データ
     * lst                  地方恒星時
     * localLatitude        観測点緯度(rad)
     */
    fun drawHorizontalConstellaLine(constellaData: ConstellationData, lst: Double, localLatitude: Double, full: Boolean = false) {
        if (constellaData.mConstellaLineLineList.count() <= 1 ||
            constellaData.mConstellaStarList.count() <= 1)
            return
        kdraw.lockCanvas()
        kdraw.setProperty(mConstallaLineColor, mConstallaLineThickness, Paint.Style.STROKE)
        val starData = constellaData.mConstellaStarList
        for (lineData in constellaData.mConstellaLineLineList) {
            if (starData.containsKey(lineData.sHip) && starData.containsKey(lineData.eHip)) {
                val sStar = starData[lineData.sHip]
                val eStar = starData[lineData.eHip]
                if (alib.D2R(-80.0) < sStar!!.coordinate.y && alib.D2R(-80.0) < eStar!!.coordinate.y ) {
                    val sp = alib.convHorizontalPoint(sStar.coordinate, lst, localLatitude, mDirection.toDouble(), mRadius, full)
                    val ep = alib.convHorizontalPoint(eStar.coordinate, lst, localLatitude, mDirection.toDouble(), mRadius, full)
                    if (!sp.isEmpty() && !ep.isEmpty())
                        kdraw.drawWLine(sp, ep)
                }
            }
        }
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 地平座標系での星座名の表示
     * constellaData        星座データ
     * lst                  地方恒星時
     * localLatitude        観測点緯度(rad)
     */
    fun drawHorizontalConstellaName(constellaData: ConstellationData, lst: Double, localLatitude: Double, full: Boolean = false) {
        if (constellaData.mConstellaNameList.count() <= 1)
            return
        kdraw.lockCanvas()
        kdraw.setTextProperty(mStarNameTextSize, 1.0, mConstallaNameColor)
        for (constellaName in constellaData.mConstellaNameList) {
            val p = alib.convHorizontalPoint(constellaName.coordinate, lst, localLatitude, mDirection.toDouble(), mRadius, full)
            if (!p.isEmpty()) {
                drawConstellaName(p, constellaName)
            }
        }
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 地平座標系での星雲・銀河などの表示
     * nebulaData       星雲・銀河などのデータ
     * lst              地方恒星時
     * localLatitude    観測点緯度(rad)
     * full             全天表示
     */
    fun drawHorizontalNebula(nebulaData: List<NebulaData.NEBULADATA>, lst: Double, localLatitude: Double, full: Boolean = false): Int {
        var count = 0
        if (nebulaData.count() <= 1)
            return count
        kdraw.lockCanvas()
        for (nebula in nebulaData) {
            var p = alib.convHorizontalPoint(nebula.coordinate, lst, localLatitude, mDirection, mRadius, full)
            if (!p.isEmpty() && p.length() < mRadius) {
                drawNebula(p, nebula)
                count++
            }
        }
        kdraw.unlockCanvasAndPost()
        return count
    }

    /**
     * 地平座標系で惑星の表示
     * lst              地方恒星時
     * localLatitude    観測点緯度(rad)
     * full             全天表示
     */
    fun drawHorizontalPlanet(lst: Double, localLatitude: Double, full: Boolean = false) {
        kdraw.lockCanvas()
        val jd = alib.getJD(mLocalDateTime.year, mLocalDateTime.monthValue, mLocalDateTime.dayOfMonth)
        for (i in mPlanetName.indices) {
            if (mPlanetName[i].compareTo("地球") != 0) {
                val planetPos = plib.equatorialCoordenate(mPlanetName[i], jd)
                if (planetPos.isEmpty())
                    continue
                val p = alib.convHorizontalPoint(planetPos, lst, localLatitude, mDirection, mRadius, full)
                if (p.isEmpty())
                    continue
                //  惑星位置表示
                kdraw.setProperty(mPlanetColor[i], 1.0, Paint.Style.FILL)
                kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(2.0)))
                kdraw.setProperty("Black", 1.0, Paint.Style.STROKE)
                kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(2.0)))
                //  惑星名表示
                kdraw.setTextProperty(mStarNameTextSize, 1.0, mStarNameColor)
                kdraw.drawWText(mPlanetName[i], p, 0.0,KDraw.HALIGNMENT.Left,KDraw.VALIGNMENT.Top)
            }
        }
        //  月の表示
        val moonEcliptic = plib.moonEclipticCoordinate(jd)      //  黄経・黄緯
        val moonEquatorialPos = plib.ecliptic2equatorial(moonEcliptic, plib.getEpslion(jd))
        val mp = alib.convHorizontalPoint(moonEquatorialPos, lst, localLatitude, mDirection, mRadius, full)
        if (!mp.isEmpty()) {
            kdraw.setProperty("Yellow", 1.0, Paint.Style.FILL)
            kdraw.drawWCircle(mp, kdraw.cnvScreen2WorldX(magnitude2radius(2.0)))
            kdraw.setProperty("Black", 1.0, Paint.Style.STROKE)
            kdraw.drawWCircle(mp, kdraw.cnvScreen2WorldX(magnitude2radius(2.0)))
            //  惑星名表示
            kdraw.setTextProperty(mStarNameTextSize, 1.0, mStarNameColor)
            kdraw.drawWText("月", mp, 0.0,KDraw.HALIGNMENT.Left,KDraw.VALIGNMENT.Top)
        }

        kdraw.unlockCanvasAndPost()
    }

    /**
     * 恒星データ表示
     * p            恒星の位置(直交座標
     * star         恒星データ
     */
    fun drawStar(p: PointD, star: StarData.STARDATA) {
        //  画面位置の登録
        mStarInfoData.mSearchData.add(StarInfoData.SEARCHDATA(kdraw.invertedPointD(kdraw.cnvWorld2View(p)), star.starName, -1, star.hipNo))
        //  恒星の円表示
        kdraw.setProperty("White", 1.0, Paint.Style.FILL)
        kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(star.magnitude)))
        kdraw.setProperty("Black", 1.0, Paint.Style.STROKE)
        kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(star.magnitude)))
        //  恒星名の表示
        if (star.magnitude <= mStarNameDispMagnitude || mStarNameDispMagnitude <= 0.0) {
            kdraw.setTextProperty(mStarNameTextSize, 1.0, mStarNameColor)
            if (0 < star.starNameJp.length) {
                kdraw.drawWText(star.starNameJp, p, 0.0,KDraw.HALIGNMENT.Left,KDraw.VALIGNMENT.Top)
            } else {
                kdraw.drawWText(star.starName, p, 0.0,KDraw.HALIGNMENT.Left,KDraw.VALIGNMENT.Top)
            }
        }
    }

    /**
     * 星雲・銀河などの表示
     * p            星雲・銀河などの位置(直交座標)
     * nebula       星雲・銀河などのデータ
     */
    fun drawNebula(p: PointD, nebula: NebulaData.NEBULADATA) {
        mStarInfoData.mSearchData.add(StarInfoData.SEARCHDATA(kdraw.invertedPointD(kdraw.cnvWorld2View(p)), nebula.name, nebula.messieNo, -1))
        kdraw.setProperty("White", 1.0, Paint.Style.FILL)
        kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(nebula.magnitude)))
        kdraw.setProperty("LightGray", 1.0, Paint.Style.STROKE)
        kdraw.drawWCircle(p, kdraw.cnvScreen2WorldX(magnitude2radius(nebula.magnitude)))
        val name = if (0 < nebula.NGCNo.length) "NGC" + nebula.NGCNo + " " + nebula.name else nebula.name
        if (0 < name.length) {
            kdraw.setTextProperty(mStarNameTextSize, 1.0, mNebulaNameColor)
            kdraw.drawWText(name, p, 0.0,KDraw.HALIGNMENT.Left,KDraw.VALIGNMENT.Top)
        }
    }

    /**
     * 星座名の表示
     * p                星座名の位置(直交座標)
     * constellaName    星座名データ
     */
    fun drawConstellaName(p: PointD, constellaName: ConstellationData.CONSTELLATIONNAME) {
        if (0 < constellaName.constrationNameJpn.length) {
            kdraw.drawWText(constellaName.constrationNameJpn, p)
        } else if (0 < constellaName.constrationName.length) {
            kdraw.drawWText(constellaName.constrationName, p)
        } else if (0 < constellaName.constrationNameMono.length) {
            kdraw.drawWText(constellaName.constrationNameMono, p)
        }
    }

    /**
     * 視等級を半径に変換する
     * magnitude        視等級
     * return           半径
     */
    fun magnitude2radius(magnitude: Double): Double {
        return sqrt(3.0 - (if (3.0 < magnitude) 2.8 else magnitude)) * mStarRadiusRate
    }

    /**
     * 最下にデータ譲歩亜表示
     * text     表示文字列
     */
    fun drawInfo(text: String) {
        kdraw.lockCanvas()
        kdraw.setTextProperty(mInfoTextSize, 1.0)
        kdraw.drawTextWithBox(text, PointD(15.0, mInfoTextSize))
        kdraw.unlockCanvasAndPost()
    }

    /**
     * 恒星や星雲・銀河などの情報表示(画面の左上に表示)
     * infoData     情報データリスト
     */
    fun drawInfoData(infoData: List<String>) {
        kdraw.lockCanvas()
        kdraw.setTextProperty(mInfoTextSize, 1.0)
        var y = kdraw.mView.bottom
        for (text in infoData) {
            y -= mInfoTextSize
            kdraw.drawTextWithBox(text, PointD(15.0, y))
        }
        kdraw.unlockCanvasAndPost()
    }

    fun drawTest(){
        kdraw.lockCanvas()
        kdraw.backColor(Color.WHITE)
        kdraw.mColor = "Black"
        kdraw.setStyle(Paint.Style.STROKE)
        kdraw.drawWRect(RectD(kdraw.mWorld))

        kdraw.drawText(""+width+"x"+height+" "+kdraw.mWorld.toString(), PointD(15, 10))

        var cir = CircleD(PointD(-20.0, 20.0), 20.0)
        kdraw.setProperty("Blue", 2.0, Paint.Style.FILL)
        kdraw.drawWCircle(cir)
        kdraw.setProperty("Black", 2.0, Paint.Style.STROKE)
        kdraw.drawWCircle(cir)
        kdraw.drawWRect(cir.toRectD())

        var arc = ArcD(PointD(30.0, 20.0), 20.0, klib.D2R(0.0), klib.D2R(180.0))
        kdraw.setProperty("Aqua", 2.0, Paint.Style.FILL)
        kdraw.drawWArc(arc)
        kdraw.setProperty("Black", 2.0, Paint.Style.STROKE)
        kdraw.drawWArc(arc)
        kdraw.drawWRect(arc.toRectD())

        val wplist = mutableListOf<PointD>(
            PointD(0.0, 50.0),PointD(20.0, 50.0),PointD(10.0, 70.0))
        kdraw.setProperty("Aqua", 2.0, Paint.Style.FILL)
        kdraw.drawWPath(wplist)

        kdraw.unlockCanvasAndPost()
    }
}
