package jp.co.yoshida.katsushige.calc2;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.util.Locale;

/**
 * Created by katsushige on 2015/02/14.
 */

//	日の出日没の時間を求める

//	http://www.codeproject.com/Articles/33724/Calculate-Sunset-and-Sunrise-Time/using System;
// Calculates sunset / sunrise time.
//
// Implementation of algorithm found in Almanac for Computers, 1990
// published by Nautical Almanac Office
// Implemented by Huysentruit Wouter, Fastload-Media.be

// 日の出/日の入り時刻を計算します。
// アルゴリズムの実装では、航海年鑑局によって発行されコンピュータ、1990年年鑑で発見
// Huysentruitワウテル、高速読み込み - Media.beによって実装された

//	2012/1/10 C#からJavaに移植
//Calculate Sunset and Sunrise Time

//	・使い方
//	Calendar date = Calendar.getInstance(new Locale("ja", "JP", "JP"));
//	東京駅（東京） の座標(WGS84)　緯度：35.681382 経度：139.766084
//	緯度 35度40分52.975秒(35.681382), 経度 139度45分57.902秒(139.766084)
//	SunTime suntime = new SunTime(SunTime.DegreesToAngle(35, 40, 53),
//    							SunTime.DegreesToAngle(139, 45, 58), 9, date);
//	SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//	System.out.println("Requested sunrise/sunset times for " + f.format(date.getTime()));
//	System.out.println("The sun will rise at " + f.format(suntime.getRiseTime().getTime()));
//	System.out.println("The sun will set at " + f.format(suntime.getSetTime().getTime()));

public class SunTime {

    public class ZenithValue {
        public static final int Official = 90833; 	//	Official zenith 公式天頂 (90 degrees and 50' = 90.833 degrees)
        public static final int Civil = 96000;			//	Civil zenith 市民の天頂 (96)
        public static final int Astronomical = 108000;//	Astronomical zenith 天文天頂 (108)
    }

    private enum Direction {
        Sunrise, Sunset
    }

    private int zenith = ZenithValue.Official;
    private double longitude;		//	経度
    private double latitude;		//	緯度
    private double utcOffset;		//	協定世界時からのオフセット
    private Calendar date;			//	指定の日時
    private int sunriseTime;		//	日の出時間(秒)
    private int sunsetTime;			//	日没時間(秒)


    /**
     * 	Create a new SunTime object with default settings.
     *	デフォルトの設定で新しいサンタイムオブジェクトを作成します。
     */
    public SunTime() {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.utcOffset = 1.0;
        this.date = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        Update();
    }

    /**	Create a new SunTime object for the current date.
     * 	現在の日付の新しいサンタイムオブジェクトを作成します。
     * @param latitude	Global position latitude in degrees. Latitude is positive for North and negative for South.
     * @param longitude	Global position longitude in degrees. Longitude is positive for East and negative for West.
     * @param utcOffset	The local UTC offset (f.e. +1 for Brussel,Kopenhagen, Madrid, Paris).
     */
    public SunTime(double latitude, double longitude, double utcOffset) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.utcOffset = utcOffset;
        this.date = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        Update();
    }

    /**	Create a new SunTime object for the given date.
     * 	指定された日付の新しいサンタイムオブジェクトを作成します。
     * @param latitude	Global position latitude in degrees. Latitude is positive for North and negative for South.
     * @param longitude	Global position longitude in degrees. Longitude is positive for East and negative for West.
     * @param utcOffset	The local UTC offset (f.e. +1 for Brussel,Kopenhagen, Madrid, Paris).
     * @param date		The date to calculate the set- and risetime for.
     */
    public SunTime(double latitude, double longitude, double utcOffset,Calendar date) {
        this.latitude = latitude; 	// 緯度
        this.longitude = longitude; // 経度
        this.utcOffset = utcOffset;
        this.date = date;
        Update();
    }

    private static double Deg2Rad(double angle) {
        return Math.PI * angle / 180.0;
    }

    private static double Rad2Deg(double angle) {
        return 180.0 * angle / Math.PI;
    }

    private static double FixValue(double value, double min, double max) {
        while (value < min)
            value += (max - min);

        while (value >= max)
            value -= (max - min);

        return value;
    }

    /**
     *
     * @param direction	日出か日没を指定
     * @return				日出または日没時間(秒)
     */
    private int Calculate(Direction direction) {
        /* doy (N) */
        // int N = date.DayOfYear;
        int N = date.get(Calendar.DAY_OF_YEAR);	//	その年の日数

        /* appr. time (t) */							//	経度(秒→時間)　世界標準時に対するズレ
        double lngHour = longitude / 15.0;

        double t;										//	春分/秋分の日の日出/日没
        if (direction == Direction.Sunrise)
            t = N + ((6.0 - lngHour) / 24.0);
        else
            t = N + ((18.0 - lngHour) / 24.0);

        /* mean anomaly (M) */						//	平均近点角(軌道要素)
        double M = (0.9856 * t) - 3.289;

        /* true longitude (L) */						//	地球の観測点の経度
        double L = M + (1.916 * Math.sin(Deg2Rad(M)))
                + (0.020 * Math.sin(Deg2Rad(2 * M))) + 282.634;
        L = FixValue(L, 0, 360);

        /* right asc (RA) */							//	赤経
        double RA = Rad2Deg(Math.atan(0.91764 * Math.tan(Deg2Rad(L))));
        RA = FixValue(RA, 0, 360);

        /* adjust quadrant of RA */
        double Lquadrant = (Math.floor(L / 90.0)) * 90.0;
        double RAquadrant = (Math.floor(RA / 90.0)) * 90.0;
        RA = RA + (Lquadrant - RAquadrant);

        RA = RA / 15.0;

        /* sin cos DEC (sinDec / cosDec) */
        double sinDec = 0.39782 * Math.sin(Deg2Rad(L));
        double cosDec = Math.cos(Math.asin(sinDec));

        /* local hour angle (cosH) */
        double cosH = (Math.cos(Deg2Rad((double) zenith / 1000.0f)) - (sinDec * Math.sin(Deg2Rad(latitude))))	/ (cosDec * Math.cos(Deg2Rad(latitude)));

        /* local hour (H) */
        double H;

        if (direction == Direction.Sunrise)
            H = 360.0 - Rad2Deg(Math.acos(cosH));
        else
            H = Rad2Deg(Math.acos(cosH));

        H = H / 15.0;

        /* time (T) */
        double T = H + RA - (0.06571 * t) - 6.622;

        /* universal time (T) */
        double UT = T - lngHour;

        UT += utcOffset; // local UTC offset

        //	夏時間対応
        // if (daylightChanges != null)
        // if ((date > daylightChanges.Start) && (date < daylightChanges.End))
        // UT += daylightChanges.Delta.TotalHours;

        UT = FixValue(UT, 0, 24);

        return (int) Math.round(UT * 3600); // Convert to seconds
    }

    private void Update() {
        sunriseTime = Calculate(Direction.Sunrise);
        sunsetTime = Calculate(Direction.Sunset);
    }

    /**	Combine a degrees/minutes/seconds value to an angle in degrees.
     * 	度/分/度単位の角度の秒の値を組み合わせたものです。
     * @param degrees	The degrees part of the value.
     * @param minutes	The minutes part of the value.
     * @param seconds	The seconds part of the value.
     * @return			The combined angle in degrees.
     */
    public static double DegreesToAngle(double degrees, double minutes,double seconds) {
        if (degrees < 0)
            return degrees - (minutes / 60.0) - (seconds / 3600.0);
        else
            return degrees + (minutes / 60.0) + (seconds / 3600.0);
    }

    /**	Gets or sets the global position longitude in degrees.
     * 	Longitude is positive for East and negative for West.
     * 	取得または度でグローバルな位置の経度を設定します。
     * 	経度は西の東の正と負になります。
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double value) {
        longitude = value;
        Update();
    }

    /**	Gets or sets the global position latitude in degrees.
     * 	Latitude is positive for North and negative for South.
     * 	取得または度でグローバルな位置の緯度を設定します。
     * 	緯度は北のために、正と南緯は負のです。
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double value) {
        latitude = value;
        Update();
    }

    /**	Gets or sets the date where the RiseTime and SetTime apply to.
     * 	取得または立ち上がりとsetTimeに適用日付を設定します。
     * @return
     */
    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar value) {
        date = value;
        Update();
    }

    /**	Gets or sets the local UTC offset in hours.
     *	F.e.: +1 for Brussel, Kopenhagen, Madrid, Paris.
     *	See Windows Time settings for a list of offsets.
     *	取得または時間でローカルオフセットUTCを設定します。
     *	F.e.：ブリュッセル、Kopenhagen、マドリード、パリの1。
     *	オフセットの一覧については、Windowsの時刻設定を参照してください。
     *
     * @return
     */
    public double getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(double value) {
        utcOffset = value;
        Update();
    }

    /**	The time (in seconds starting from midnight) the sun will rise on the
     *	given location at the given date.
     *	時間（真夜中からの秒単位）太陽は、特定の日付で指定された位置に上昇します。
     *
     * @return	日の出時間(秒)
     */
    public int getRiseTimeSec() {
        return sunriseTime;
    }

    /**	The time (in seconds starting from midnight) the sun will set on the
     *	given location at the given date.
     *	時間（真夜中からの秒単位）太陽は、特定の日付で指定された場所に設定されます。
     * @return	日没時間(秒)
     */
    public int getSetTimeSec() {
        return sunsetTime;
    }

    /**	The time the sun will rise on the given location at the given date.
     *	時間は太陽が与えられた日付に、指定された位置に上昇します。
     *
     * @return	日の出時間(Calender Class)
     */
    public Calendar getRiseTime() {
        Calendar cal = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        cal.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.add(Calendar.SECOND, sunriseTime);
        return cal;
    }

    /**	The time the sun will set on the given location at the given date.
     * 	時間は太陽が与えられた日付に、指定された位置に設定されます。
     * @return	日没時間(Calender Class)
     */
    public Calendar getSetTime() {
        Calendar cal = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        cal.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.add(Calendar.SECOND, sunsetTime);
        return cal;
    }

    public String getStrRiseTime() {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        return f.format(getRiseTime().getTime());
    }

    public String getStrSetTime() {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        return f.format(getSetTime().getTime());
    }

    /**	Gets or sets the zenith used in the sunrise / sunset time calculation.
     *	取得または日の出/日没時間の計算に使用される天頂を設定します。
     *
     * @return
     */
    public int getZenith() {
        return zenith;
    }

    public void setZenith(int value) {
        zenith = value;
        Update();
    }
}