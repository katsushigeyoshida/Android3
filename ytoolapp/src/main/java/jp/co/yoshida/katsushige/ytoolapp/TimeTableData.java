package jp.co.yoshida.katsushige.ytoolapp;

import android.util.Log;

import java.util.ArrayList;

import jp.co.yoshida.katsushige.mylib.YLib;

public class TimeTableData {

    private static final String TAG = "TimeTableData";

    String mArea = "";                  //  地域
    String mLocation = "";             //  場所
    String mURL = "";                   //  掲載しているURL
    String mDateIsuue = "";            //  発行日
    String mRoot = "";                  //  ルート
    String[] mStartDate;                //  開始日
    String[] mEndDate;                  //  終了日
    String[] mDayWeek;                  //  運行曜日(毎日、平日、土日、土日祝、祝..)
    String[] mAtention;                 //  注意書き
    ArrayList<String[]> mBusTime;       //  バス停と時刻表
    int mBusCount = 0;                  //  バスの本数

    YLib ylib;

    public TimeTableData() {
        ylib = new YLib();
        mBusTime = new ArrayList<String[]>();
    }

    public boolean setData(String data) {
        String[] datas = ylib.splitCsvString(data);
        Log.d(TAG,"setData:"+datas.length+" "+datas[0]);
        if (1 < datas.length && 0 < datas[0].length()) {
            if (datas[0].compareTo("地域")==0) {
                mArea = datas[1];
            }else if (datas[0].compareTo("場所")==0) {
                mLocation = datas[1];
            }else if (datas[0].compareTo("URL")==0) {
                mURL = datas[1];
            }else if (datas[0].compareTo("発行年")==0) {
                mDateIsuue = datas[1];
            }else if (datas[0].compareTo("ルート")==0) {
                mRoot = datas[1];
            }else if (datas[0].compareTo("開始日")==0) {
                mBusCount = datas.length - 1;
                mStartDate = new String[mBusCount];
                for (int i = 0; i < mBusCount; i++)
                    if (i < datas.length-1)
                        mStartDate[i] = datas[i+1];
            }else if (datas[0].compareTo("終了日")==0) {
                mEndDate = new String[mBusCount];
                for (int i = 0; i < mBusCount; i++)
                    if (i < datas.length-1)
                        mEndDate[i] = datas[i+1];
            }else if (datas[0].compareTo("運行日")==0) {
                mDayWeek = new String[mBusCount];
                for (int i = 0; i < mBusCount; i++)
                    if (i < datas.length-1)
                        mDayWeek[i] = datas[i+1];
            }else if (datas[0].compareTo("注意")==0) {
                mAtention = new String[mBusCount];
                for (int i = 0; i < mBusCount; i++)
                    if (i < datas.length-1)
                        mAtention[i] = datas[i+1];
            }else{
                mBusTime.add(datas);
            }
            return true;
        } else {
            return false;
        }
    }

    public void dataTrace() {
        Log.d(TAG,"dataTrace: "+mArea+" "+ mLocation +" "+mDateIsuue+" "+mRoot+" "+mBusCount);
        String text="";
        for (String date : mStartDate)
            text += date+" ";
        Log.d(TAG,"StartDate: "+text);
        text = "";
        for (String date : mEndDate)
            text += date+" ";
        Log.d(TAG,"EndDate: "+text);
        text = "";
        for (String date : mDayWeek)
            text += date+" ";
        Log.d(TAG,"DayWeek: "+text);
        text = "";
        for (String date : mAtention)
            text += date+" ";
        Log.d(TAG,"Atention: "+text);
        for (String[] datas : mBusTime) {
            text = "";
            for (String data : datas)
                text += data+" ";
            Log.d(TAG,"BusTime: "+text);
        }
    }
}
