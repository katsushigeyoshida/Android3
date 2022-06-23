package jp.co.yoshida.katsushige.mapapp

import android.util.Log
import jp.co.yoshida.katsushige.mylib.DownLoadWebFile
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.KParse
import java.io.File

class WikiData(
        var mTitle: String = "",        //  タイトル
        var mComment: String = "",      //  コメント
        var mUrl: String = "",          //  参照URL
        var mListName: String = "",     //  親リスト名
        var mSearchForm: String = "",   //  一覧抽出方法
        var mOutFolder:String = ""      //  ファイル出力先フォルダ
    ) {
    val TAG = "WikiData"

    var mCoordinate: String = ""    //  座標
    var mDetail: String = ""        //  詳細データ(基本情報) (タイトル:データ;タイトル:データ;....)
    var mIntroduction: String = ""  //  前書
    var mDistance: Double = 0.0     //  検索地点からの距離

    companion object {
        val mDataTitle = listOf(
            "タイトル", "コメント", "URLアドレス", "座標", "詳細情報", "親リスト", "一覧抽出方法", "前文")
    }

    var klib = KLib()
    var kparse = KParse()

    fun setData(coordinate: String, detail: String, introduction: String) {
        mCoordinate = coordinate
        mDetail = detail
        mIntroduction = introduction
    }

    fun getStringList(): List<String> {
        return listOf(mTitle, mComment, mUrl, mCoordinate, mDetail, mListName, mSearchForm, mIntroduction)
    }

    fun setStringList(data: List<String>) {
        mTitle = if (0 < data.count()) data[0] else ""
        mComment = if (1 < data.count()) data[1] else ""
        mUrl = if (2 < data.count()) data[2] else ""
        mCoordinate = if (3 < data.count()) data[3] else ""
        mDetail = if (4 < data.count()) data[4] else ""
        mListName = if (5 < data.count()) data[5] else ""
        mSearchForm = if (6 < data.count()) data[6] else ""
        mIntroduction = if (7 < data.count()) data[7] else ""
    }

     /**
     *  URLのページから基本情報を取得
     *  URL         基本情報を取得するページのURL
     *  return      基本情報
     */
    fun setBaseInfo() {
        Log.d(TAG,"setBaseInfo: " + mTitle + " " + mUrl + " " + mOutFolder)
        var downLoadFilePath = mOutFolder + "/" + "BaseInfoTemp.html"   //  一時保存ファイル名
        var downLoadFile = File(downLoadFilePath)
        if (downLoadFile.exists())
            downLoadFile.delete()
        var downLoad = DownLoadWebFile(mUrl, downLoadFile.path)          //  HTMLソースの取得と保存
        downLoad.start()                                                //  ダウンロード“開始
        while (downLoad.isAlive()) {
            Thread.sleep(100L)
        }
        //  ダウンロードしたファイルから基本情報のデータ取得
        if (downLoadFile.exists()) {
            getInfoData(downLoadFile.path)
        } else {
            Log.d(TAG,"setBaseInfo: download error " + downLoadFile.path)
        }
    }

    /**
     * HTMLソースファイルから基本情報を取得
     * filePath     HTMLソースファイルパス
     * return       基本情報
     */
    fun getInfoData(filePath: String) {
        Log.d(TAG,"getInfoData: " + filePath)
        var htmlSrc = kparse.loadHtmlData(filePath).toString()  //  HTMLファイルの読込
        var listData = kparse.getHTMLListData(htmlSrc)          //  HTMLソースのリスト化
        mIntroduction = getIntroduction(listData)               //  前文の取得
        mCoordinate = getCoordinateString(listData)
        var baseInfoList = getBaseInfo(listData)                //  基本情報取得
        mDetail = baseInfoList.joinToString(separator = ";")    //
        if (mCoordinate.length == 0)
            mCoordinate = klib.matchCoordinate(mDetail)         //  座標情報抽出
    }

    /**
     * 基本情報の取得
     * listData     HTMLソースのリスト化データ
     * return       取得情報
     */
    fun getBaseInfo(htmlListData: List<String>): List<String> {
        var listList = mutableListOf<String>()
        var tagName = "table"
        var st = 0
        var et = 0
        var listData = kparse.stripTagData(htmlListData, "style")
        //  table データの取得
        while (st < listData.count()) {
            var tagPos = kparse.getTagData(listData, tagName, st)
            st = tagPos.first
            et = tagPos.second
            if (st < 0)
                break
            var classData = kparse.getTagPara(listData[st], "class")
//            listList.add("==== " + (count++) + " [" + tagName + "] " + classData + " ====")
            //  基本情報の table を選択
            if (0 <= classData.indexOf("infobox")) {
                var tagPos2 = Pair(0, 0)
                var st2 = tagPos2.first
                //  表データからタイトルとデータを取得
                while (0 <= tagPos2.first) {
                    //  表のー行分(tr)のデータ抽出
                    tagPos2 = kparse.getTagData(listData, "tr", st2)
                    if (tagPos2.first < 0)
                        break
                    var trList = listData.slice(tagPos2.first..tagPos2.second)
                    var data = getTrData(trList).trim()
                    if (0 < data.length)
                        listList.add(data)
                    st2 = tagPos2.second + 1
                }
            }
            st = et + 1
        }
        return listList
    }

    /**
     * 表の一行分のデータからタイトルとデータを抽出
     * trList       表の一行分(tr)のHTMLリストデータ
     * return       タイトルとデータの文字列
     */
    fun getTrData(trList: List<String>): String {
        var buffer = ""
        var tagPos = kparse.getTagData(trList, "th")
        if (0 <= tagPos.first) {
            //  表のタイトルがある場合
            var thList = trList.slice(tagPos.first..tagPos.second).toList()
            var title = kparse.getDataAll(thList, 0, thList.count() - 1).joinToString(separator = "")
            if (0 < title.length) {
                //  表のデータ部
                tagPos = kparse.getTagData(trList, "td")
                if (0 <= tagPos.first) {
                    buffer += title + ": "
                    buffer += kparse.getDataAll(trList, tagPos.first, tagPos.second).joinToString(separator = "")
                }
            }
        }
        buffer = klib.cnvHtmlSpecialCode(buffer)
        return klib.stripBrackets(buffer)
    }

    /**
     * データの前書き部分を抽出
     * 最初の段落(<p> ～　</p>)部分から文字列デーを抽出する
     * htmlListData     List形式のHTMLソース
     * return           抽出文字列
     */
    fun getIntroduction(htmlListData: List<String>): String {
        var sp = 0
        var pos = 0
        var listData = kparse.stripTagData(htmlListData, "style")
        //  基本情報のテーブルを終わりを検索
        while (0 <= pos) {
            pos = kparse.findTagDataPos(listData, "table", sp)
            if (0 <= pos) {
                if (0 < listData[pos].indexOf("class=\"infobox")) {
                    var tagPos = kparse.getTagData(listData, "table", pos)
                    sp = tagPos.second
                    break
                } else {
                    sp = pos + 1
                }
            }
        }
        //  段落の検出
        var tagPos = kparse.getTagData(listData, "p", sp)
        if (tagPos.first < 0)
            return ""
        if (0 < kparse.findTagDataPos(listData, "span", tagPos.first, tagPos.second)) {
            tagPos = kparse.getTagData(listData, "p", tagPos.second)
            if (tagPos.first < 0)
                return ""
        }
        var buf = kparse.getDataAll(listData, tagPos.first, tagPos.second).joinToString(separator = "")
        buf = klib.cnvHtmlSpecialCode(buf)
        return klib.stripBrackets(buf)
    }

    /**
     * 基本情報とは別に座標情報の取得
     * ページ上部の段落の中に<span > ～ </span> で記述されている場合
     * lListData        List形式のHTMLソース
     * return           抽出文字列
     */
    fun getCoordinateString(listData: List<String>):String {
        var sp = 0
        var pos = 0
        var buf = ""
        while (0 <= pos) {
            pos = kparse.findTagDataPos(listData, "span", sp)
            if (0 <= pos) {
                if (0 < listData[pos].indexOf("class=\"geo-dms")) {
                    var tagPos = kparse.getTagData(listData, "span", pos)
                    buf = kparse.getDataAll(listData, tagPos.first, tagPos.second).joinToString(separator = "")
                    break
                } else {
                    sp = pos + 1
                }
            }
        }
        return buf
    }
}