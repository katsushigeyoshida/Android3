package jp.co.yoshida.katsushige.calc2;

import android.icu.util.Calendar;
import android.util.Log;

import java.util.Locale;

public class YDate {
    private static final String TAG = "YDate";

    private double[] mKaigen = {2019.0501,1989.0108,1926.1225,1912.0730,1868.0908,1865.0407,1864,1861,1860,1854,1848,1844,1830,1818};
    private String[] mGengou = {"令和","平成","昭和","大正","明治","慶応","元治","文久","万延","安政","嘉永","弘化",	"天保","文政"};
    private String[] mYoubi = {"日","月","火","水","木","金","土"};

    public String arrayLocation[] = {              //  地名と緯度経度
            "網走　 44.019722,144.258611",
            "札幌　 43.055248,141.345505",
            "仙台　 38.254162,140.891403",
            "東京　 35.680909,139.767372",
            "名古屋 35.154919,136.920593",
            "大阪　 34.702509,135.496505",
            "広島　 34.377560,132.444794",
            "福岡　 33.579788,130.402405",
            "鹿児島 31.570539,130.552505",
            "那覇　 26.204830,127.692398"};

    QReki qreki;        //  旧歴の計算
    SunTime sunTime;    //  日の出、日の入りの計算

    public YDate() {
        qreki = new QReki();
    }

    /**
     * 地名と緯度経度のリストで地名と緯度経度に文字列を分解する
     * @param str       地名と緯度経度の文字列
     * @return          配列に分解した文字列
     */
    public String[] getLocationSplit(String str) {
        String[] loc = new String[3];
        int n = 0;
        int i = 0;
        loc[n] = "";
        while (i < str.length() && n < 3) {
            if (str.charAt(i)!=' ' && str.charAt(i)!='　' && str.charAt(i)!=',')
                loc[n] += str.charAt(i);
            else if (str.charAt(i-1)!=' ' && str.charAt(i)==' ' || str.charAt(i)==',') {
                n++;
                loc[n] = "";
            }
            i++;
        }
        return loc;
    }

    /**
     * 日の出の時間を取得する
     * @param dateYear      西暦の日付(yyyy.mmdd)
     * @param latitude      緯度
     * @param longitude     経度
     * @return              日の出の時間(HH.mmss)
     */
    public double getSunRise(double dateYear, double latitude, double longitude) {
        Calendar cal = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        cal = calNumber(cal, dateYear);
        SunTime suntime = new SunTime(latitude, longitude, 9, cal);
        String[] result = suntime.getStrRiseTime().split(":");
        return Double.valueOf(result[0])+Double.valueOf(result[1])/100d+Double.valueOf(result[2])/10000d;
    }

    /**
     * 日の入りの時間を取得する
     * @param dateYear      西暦の日付(yyyy.mmdd)
     * @param latitude      緯度
     * @param longitude     経度
     * @return              日の出の時間(HH.mmss)
     */
    public double getSunSet(double dateYear, double latitude, double longitude) {
        Calendar cal = Calendar.getInstance(new Locale("ja", "JP", "JP"));
        cal = calNumber(cal, dateYear);
        SunTime suntime = new SunTime(latitude, longitude, 9, cal);
        String[] result = suntime.getStrSetTime().split(":");
        return Double.valueOf(result[0])+Double.valueOf(result[1])/100d+Double.valueOf(result[2])/10000d;
    }

    /**
     *	年月文字列をカレンダクラスに変換する
     * @param cal		カレンダークラス
     * @param dateYear　年月日(yyyy.mmdd)
     * @return			カレンダークラス
     */
    public Calendar calNumber(Calendar cal, double dateYear) {
        double year = Math.floor(dateYear);
        double tmp = (dateYear - year) * 100d;
        double month = Math.floor(tmp);
        double day = (tmp - month) * 100d;
        cal.set(Calendar.YEAR, (int)year);
        cal.set(Calendar.MONTH, (int)month - 1);
        cal.set(Calendar.DATE, (int)day);
        return cal;
    }

    /**
     * 	月齢を求める
     * 	Calendarの時間値をミリ秒をもとに計算
     * @param dateYear  西暦(yyyy.mmdd)
     * @return          月齢
     */
    public int moonAge(double dateYear) {
        double year = Math.floor(dateYear);
        double tmp = (dateYear - year) * 100d;
        double month = Math.floor(tmp);
        double day = (tmp - month) * 100d;
//        int year = (int)dateYear;
//        int month = (int)((dateYear % 1)*100);
//        int day = (int)((dateYear % 0.01) * 10000); << ここで誤差が出る
        Log.d(TAG,"moonAge:"+dateYear+" "+year+" "+month+" "+day);
        Calendar cal = Calendar.getInstance();
        cal.set((int)year, (int)month, (int)day, 12, 0);
        Log.d(TAG,"moonAge:"+cal.get(Calendar.YEAR)+"/"+
                (cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DATE));
        double mage = (((cal.getTimeInMillis()/1E3-919179540)%2551442)/86400)+0.4;
        return (int)mage;
        //return (((year - 11) % 19) * 11 + moonConst[month-1] + day) % 30;
    }


    /**
     * 西暦から旧歴を求める
     * @param date      西暦(yyyy.mmdd)
     * @return          旧歴 yyyy年mm月dd日
     */
    public String date2Kyureki(double date) {
        return qreki.Kyureki((int)date, (int)((date % 1) * 100), (int)((date % 0.01) * 10000));
    }

    /**
     * 西暦から六曜を求める
     * @param date      西暦(yyyy.mmdd)
     * @return          六曜
     */
    public String date2Rokuyo(double date) {
        return qreki.RokuYo((int)date, (int)((date % 1) * 100), (int)((date % 0.01) * 10000));
    }

    /**
     * 西暦から十干十二支を求める
     * @param date      西暦(yyyy.mmdd)
     * @return          十干十二支
     */
    public String date2JyukanJyunishi(double date) {
        return qreki.JyukanJyunishi((int)date, (int)((date % 1) * 100), (int)((date % 0.01) * 10000));
    }

    /**
     * 西暦(yyyy.mmdd)を和暦(GGyy年mm月dd日)にする
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              和暦(GGyy年mm月dd日)
     */
    public String date2JpnCal(double dateYear) {
        String result="";
        for (int i = 0; i < mKaigen.length; i++) {
            if (mKaigen[i] <= dateYear) {
                result = mGengou[i] + ((int)dateYear - (int)mKaigen[i] + 1) + "年" +
                        ((int)((dateYear % 1) * 100)) + "月" + ((int)((dateYear % 0.01) * 10000) +"日");
                break;
            }
        }
        return result;
    }

    public double reiwa2Date(double reiwa) {
        double dyear = (int) mKaigen[0] + (int)reiwa - 1;
        return dyear + (reiwa % 1);
    }

    /**
     * 平成(yy.mmdd)を西暦(yyyy.mmdd)に変換する
     * @param heisei    平成の年(yy.mmdd)
     * @return          西暦(yyyy.mmdd)
     */
    public double heisei2Date(double heisei) {
        double dyear = (int) mKaigen[1] + (int)heisei - 1;
        return dyear + (heisei % 1);
    }

    /**
     * 昭和(yy.mmdd)を西暦(yyyy.mmdd)に変換する
     * @param shyouwa    平成の年(yy.mmdd)
     * @return          西暦(yyyy.mmdd)
     */
    public double shyouwa2Date(double shyouwa) {
        double dyear = (int) mKaigen[2] + (int)shyouwa - 1;
        return dyear + (shyouwa % 1);
    }

    /**
     * 大正(yy.mmdd)を西暦(yyyy.mmdd)に変換する
     * @param taisyou    平成の年(yy.mmdd)
     * @return          西暦(yyyy.mmdd)
     */
    public double taisyou2Date(double taisyou) {
        double dyear = (int) mKaigen[3] + (int)taisyou -1;
        return dyear + (taisyou % 1);
    }

    /**
     * 明治(yy.mmdd)を西暦(yyyy.mmdd)に変換する
     * @param meiji    平成の年(yy.mmdd)
     * @return          西暦(yyyy.mmdd)
     */
    public double meiji2Date(double meiji) {
        double dyear = (int) mKaigen[4] + (int)meiji - 1;
        return dyear + (meiji % 1);
    }

    public double date2Reiwa(double dateYear) {
        double dyear = (int)dateYear - (int) mKaigen[0] + 1;
        return dyear + (dateYear % 1);
    }

    /**
     * 西暦(yyyy.mmdd)から平成の年を求める
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              平成(yy.mmdd)
     */
    public double date2Heisei(double dateYear) {
        double dyear = (int)dateYear - (int) mKaigen[1] + 1;
        return dyear + (dateYear % 1);
    }

    /**
     * 西暦(yyyy.mmdd)から昭和の年を求める
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              昭和(yy.mmdd)
     */
    public double date2Shyouwa(double dateYear) {
        double dyear = (int)dateYear - (int) mKaigen[2] + 1;
        return dyear + (dateYear % 1);
    }

    /**
     * 西暦(yyyy.mmdd)から大正の年を求める
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              大正(yy.mmdd)
     */
    public double date2Taishou(double dateYear) {
        double dyear = (int)dateYear - (int) mKaigen[3] + 1;
        return dyear + (dateYear % 1);
    }

    /**
     * 西暦(yyyy.mmdd)から明治の年を求める
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              明治(yy.mmdd)
     */
    public double date2Meiji(double dateYear) {
        double dyear = (int)dateYear - (int) mKaigen[4] + 1;
        return dyear + (dateYear % 1);
    }

    /**
     * 日付の表示を変換する
     * yyyymmdd → yyyy年mm月dd日
     * @param date
     * @return
     */
    public String strCnvDate(String date) {
        if (7<date.length())
            return date.substring(0,4)+"年"+date.substring(4,6)+"月"+date.substring(6,8)+"日";
        else
            return date;
    }

    /**
     * 日付の表示に逆変換する
     * yyyy年mm月dd日 → yyyymmdd
     * @param date
     * @return
     */
    public String strRevDate(String date) {
        if (7<date.length())
            return date.substring(0,4)+date.substring(5,7)+date.substring(8,10);
        else
            return date;
    }

    /**
     * 時間の表示を変換する
     * HH:mm:ss → HH時mm分ss秒
     * @param time
     * @return
     */
    public String strCnvTime(String time) {
        if (7<time.length())
            return time.substring(0,2)+"時"+time.substring(3,5)+"分"+time.substring(6,8)+"秒";
        else
            return time;
    }

    /**
     * 時間の表示を逆変換する
     * HH時mm分ss秒 → HH:mm:ss
     * @param time
     * @return
     */
    public String strRevTime(String time) {
        time = time.trim();
        if (7<time.length())
            return time.substring(0,2)+":"+time.substring(3,5)+":"+time.substring(6,8);
        else
            return time;
    }

    /**
     * 日付の差を年数で返す
     * @param date1     日付(yyyy.mmdd)
     * @param date2     日付(yyyy.mmdd)
     * @return          年数差(yyyy)
     */
    public double diffYears(double date1, double date2) {
        double dyear = date1 - date2;
        return (int)(dyear);        //  整数部
    }

    /**
     * 日付の差を日数で返す
     * @param date1     日付(yyyy.mmdd)
     * @param date2     日付(yyyy.mmdd)
     * @return          日数差(dddd)
     */
    public double diffDays(double date1, double date2) {
        return date2JulianDay(date1) - date2JulianDay(date2);
    }


    /**
     * ひと月の日数を求める
     * @param date      yyyy/mm/dd or yyyymmdd
     * @return
     */
    public int getMonthLength(String date) {
        int year,month,day;
        if (0 <= date.indexOf('/')) {
            String[] str = date.split("/", 0);
            year = Integer.valueOf(str[0]);
            month = Integer.valueOf(str[1]);
            day = Integer.valueOf(str[2]);
        } else {
            year =  Integer.valueOf(date.substring(0,4));
            month =  Integer.valueOf(date.substring(4,6));
            day =  Integer.valueOf(date.substring(6,8));
        }
        int MonthDay = date2JulianDay(year, month,1);
        if (12 <= month) {
            year++;
            month = 1;
        }
        int nextMonthDay = date2JulianDay(year, month + 1,1);
        return nextMonthDay - MonthDay;
    }

    /**
     * 年と週目を指定して週の最初の日付(日曜日)をユリウス日で返す
     * @param year
     * @param weekNo
     * @return
     */
    public String getStartWeekDay(int year, int weekNo) {
        return getStartWeekDay(year, weekNo, true);
    }

    /**
     * 年と週目を指定して週の最初の日付(日曜日)をユリウス日で返す
     * @param year
     * @param weekNo
     * @param sp        セパレータの有無
     * @return          yyyy/mm/dd or yyyymmdd
     */
    public String getStartWeekDay(int year, int weekNo, boolean sp) {
        int startDay = date2JulianDay(year, 1, 1);	//	年初１月１日のユリウス日
        int startWeekNo = getDayWeek(year, 1, 1);	//	年初１月１日の曜日
        int day = (weekNo - 1) * 7 - startWeekNo;
        int jd = startDay + day;
        return JulianDay2DateYear(jd, sp);
    }

    /**
     * 日付から何週目かを算出する １月１日が第１週目
     * @param date      yyyy/mm/dd or yyyymmdd
     * @return          週目
     */
    public int getWeekNo(String date) {
        int year,month,day;
        if (0 <= date.indexOf('/')) {
            String[] str = date.split("/", 0);
            year = Integer.valueOf(str[0]);
            month = Integer.valueOf(str[1]);
            day = Integer.valueOf(str[2]);
        } else {
            year =  Integer.valueOf(date.substring(0,4));
            month =  Integer.valueOf(date.substring(4,6));
            day =  Integer.valueOf(date.substring(6,8));
        }
        return  getWeekNo(year, month, day);
    }

    /**
     * 日付から何週目かを算出する １月１日が第１週目
     * @param year
     * @param month
     * @param day
     * @return          週目
     */
    public int getWeekNo(int year, int month, int day) {
        int startDay = date2JulianDay(year, 1, 1);	//	年初１月１日のユリウス日
        int startWeekNo = getDayWeek(year, 1, 1);	//	年初１月１日の曜日
        int dayY = date2JulianDay(year, month, day) - startDay;
        return (dayY + startWeekNo) / 7 + 1;
    }

    /**
     * 西暦(yyyy.mmdd)から曜日を返す
     * @param dateYear      西暦(yyyy.mmdd)
     * @return              曜日
     */
    public String getDayWeek(double dateYear) {
        int year = (int)dateYear;
        int month = (int)((dateYear % 1)*100);
        int day = (int)((dateYear % 0.01) * 10000);
        return mYoubi[getDayWeek(year, month, day)]+"曜日";
    }

    /**
     * 西暦から曜日を求める
     * @param year
     * @param month
     * @param day
     * @return          0=日,1=月,?....6=土
     */
    public int getDayWeek(int year, int month, int day) {
        if (month < 3) {
            year--;
            month += 12;
        }
        return (year + year /4 - year / 100 + year / 400 + (13 * month + 8) / 5 + day) % 7;
    }

    /**
     * 実数の年月日(yyyy.mmdd)からユリウス日を求める
     * @param dateYear
     * @return
     */
    public int date2JulianDay(double dateYear) {
        double year = Math.floor(dateYear);
        double tmp = (dateYear - year) * 100d;
        double month = Math.floor(tmp);
        double day = (tmp - month) * 100d;
//        int year = (int)dateYear;
//        int month = (int)((dateYear % 1)*100);
//        int day = (int)((dateYear % 0.01) * 10000);
        return date2JulianDay((int)year, (int)month, (int)day);
    }

    /**
     * 年月日からユリウス日を求める
     * @param date      yyyy/mm/dd or yyyymmdd
     * @return
     */
    public int date2JulianDay(String date) {
        int year,month,day;
        if (0 <= date.indexOf('/')) {
            String[] str = date.split("/", 0);
            year = Integer.valueOf(str[0]);
            month = Integer.valueOf(str[1]);
            day = Integer.valueOf(str[2]);
        } else {
            year =  Integer.valueOf(date.substring(0,4));
            month =  Integer.valueOf(date.substring(4,6));
            day =  Integer.valueOf(date.substring(6,8));
        }
        return  date2JulianDay(year, month, day);
    }

    /**
     * 歴日からユリウス日に変換
     * @param year      2018年
     * @param month     10月
     * @param day       5日
     * @return          日
     */
    public int date2JulianDay(int year, int month, int day) {
        if (month <= 2) {
            month += 12;
            year--;
        }
        if ((year * 12 + month) * 31 + day >= (1582 * 12 + 10) * 31 + 15) {
            //  1582/10/15以降はグレゴリオ暦
            day += 2 - year / 100 + year / 400;
        }
        return (int)Math.floor(365.25 * (year + 4716))+ (int)(30.6001 * (month + 1)) + day  - 1524;
    }

    /**
     * ユリウス日から西暦(yyyy.mmdd)を実数で返す
     * @param jd        ユリウス日
     * @return          西暦(yyyy.mmdd)
     */
    public double julianDay2DateYear(double jd) {
        String jdstr = julianDay2DateYear((int) jd);
        String[] text = jdstr.split("/");
        if (text.length==3)
            return Double.valueOf(text[0])+Double.valueOf(text[1])/100+Double.valueOf(text[2])/10000;
        else
            return 0;
    }

    /**
     * ユリウス日から歴日の文字列に変換(セパレータ付き)
     * @param jd
     * @return      yyyy/mm/dd
     */
    public String julianDay2DateYear(int jd) {
        return JulianDay2DateYear(jd, true);
    }

    /**
     * ユリウス日から歴日の文字列に変換
     * @param jd        ユリウス日
     * @param sp        セパレータの有無
     * @return          yyyy/mm/dd or yyyymmdd
     */
    public String JulianDay2DateYear(int jd, boolean sp) {
        if (jd >= 2299161) {
            //  1582/10+15以降はグレゴリオ暦
            int t = (int)((jd - 1867216.25) /365.25);
            jd += 1 + t / 100 - t /400;
        }
        jd += 1524;
        int y = (int)Math.floor(((jd - 122.1) / 365.25));
        jd -= (int)Math.floor(365.25 * y);
        int m = (int)(jd / 30.6001);
        jd -= (int)(30.6001 * m);
        int day = jd;
        int month = m - 1;
        int year = y - 4716;
        if (month > 12) {
            month -= 12;
            year++;
        }
        if (sp)
            return String.format("%04d/%02d/%02d",year,month,day);
        else
            return String.format("%04d%02d%02d",year,month,day);
    }
}
