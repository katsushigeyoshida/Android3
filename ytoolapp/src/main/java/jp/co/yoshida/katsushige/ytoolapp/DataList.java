package jp.co.yoshida.katsushige.ytoolapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.co.yoshida.katsushige.mylib.YLib;

public class DataList {

    private static final String TAG = "DataList";

    Context c =null;
    YLib ylib;

    private String mSaveDirectory;
    public  ArrayList<TimeTableData> mTimeTableList;
    private List<String> mFileList = null;
    private Set<String> mAreaList;
    private Set<String> mLocalAreaList;
    private Set<String> mRootList;

    public DataList() {
        ylib = new YLib();
    }

    public DataList(Context context) {
        this.c = c;
        ylib = new YLib(c);
    }

    public DataList(Context context, String saveDirectoryPath) {
        this.c = c;
        ylib = new YLib(c);
        mSaveDirectory = saveDirectoryPath;
        mTimeTableList = new ArrayList<TimeTableData>();
        mFileList = ylib.getFileList(mSaveDirectory, "*.csv", false);
    }


    /**
     * 検索したファイルのリストを取得
     * @return      ファイルリスト
     */
    public List<String> getFileList() {
        if (mFileList == null)
            mFileList = ylib.getFileList(mSaveDirectory, "*.csv", false);
        return mFileList;
    }

    /**
     * ファイルからデータの取得
     * @return      取得したファイル数
     */
    public int getDataList() {
        mTimeTableList.clear();;
        for (String path : mFileList) {
            loadData(path);
        }
        return mTimeTableList.size();
    }

    /**
     * 地域リストの取得
     * @return      地域のリスト
     */
    public String[] getAreaList() {
        mAreaList = new LinkedHashSet<String>();
        for (TimeTableData data : mTimeTableList) {
            mAreaList.add(data.mArea);
        }
        String[] areaArray = new String[mAreaList.size()];
        mAreaList.toArray(areaArray);
        return areaArray;
    }

    /**
     * 場所リストの取得
     * @param area      地域名
     * @return          場所リスト
     */
    public String[] getLocalAreaList(String area) {
        Set<String> localAreaList = new LinkedHashSet<String>();
        for (TimeTableData data : mTimeTableList) {
            if (data.mArea.compareTo(area)==0)
                localAreaList.add(data.mLocation);
        }
        String[] localAreaArray = new String[localAreaList.size()];
        localAreaList.toArray(localAreaArray);
        return localAreaArray;
    }

    /**
     * ルートリストの取得
     * @param area          地域名
     * @param location      場所名
     * @return              ルートリスト
     */
    public String[] getRootList(String area, String location) {
        Set<String> rootList = new LinkedHashSet<String>();
        for (TimeTableData data : mTimeTableList) {
            if (data.mArea.compareTo(area)==0 && data.mLocation.compareTo(location)==0)
                rootList.add(data.mRoot);
        }
        String[] rootArray = new String[rootList.size()];
        rootList.toArray(rootArray);
        return rootArray;
    }

    /**
     * 時刻表のデータを取得する
     * @param area          地域名
     * @param location      場所名
     * @param root          ルート
     * @return              時刻表データ
     */
    public TimeTableData getTimeTableData(String area, String location, String root) {
        for (TimeTableData data : mTimeTableList) {
            if (data.mArea.compareTo(area)==0 && data.mLocation.compareTo(location)==0 && data.mRoot.compareTo(root)==0)
                return data;
        }
        return null;
    }

    /**
     * 指定したファイルのデータを取得
     * @param path      ファイル名
     * @return          取得の成否
     */
    private boolean loadData(String path) {
        Log.d(TAG,"loadData: "+path);
        //	ファイルの存在確認
        if (!ylib.existsFile(path)) {
            if (c!= null)
                Toast.makeText(c, "データが登録されていません\n"+path, Toast.LENGTH_LONG).show();
            return false;
        }

        Log.d(TAG,"loadData: FileRead");
        //	ファイルデータの取り込み
        ArrayList<String> fileData = new ArrayList<String>();
        fileData.clear();
        ylib.readTextFile(path, fileData);
        if (fileData.size()<1)
            return false;

        Log.d(TAG,"loadData: DataSize: "+fileData.size());
        //  データの登録
        TimeTableData timeTableData = new TimeTableData();
        boolean preEmpty = true;
        for (String data : fileData) {
            if (!timeTableData.setData(data) && !preEmpty) {
                String area = timeTableData.mArea;
                String location = timeTableData.mLocation;
                String dateIsuue = timeTableData.mDateIsuue;
                String url = timeTableData.mURL;
                mTimeTableList.add(timeTableData);
                timeTableData = new TimeTableData();
                timeTableData.mArea = area;
                timeTableData.mLocation = location;
                timeTableData.mDateIsuue = dateIsuue;
                timeTableData.mURL = url;
                preEmpty = true;
            } else {
                preEmpty = false;
            }
        }
        if (!preEmpty) {
            mTimeTableList.add(timeTableData);
        }
        return true;
    }
}
