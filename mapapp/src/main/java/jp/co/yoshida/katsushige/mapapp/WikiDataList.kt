package jp.co.yoshida.katsushige.mapapp

import android.util.Log
import android.widget.Toast
import jp.co.yoshida.katsushige.mylib.DownLoadWebFile
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.KParse
import jp.co.yoshida.katsushige.mylib.PointD
//import kotlinx.android.synthetic.main.activity_wiki_list.*
import java.io.File
import java.net.URLDecoder

class WikiDataList {
    val TAG = "WikiDataList"

    var mDataList = mutableListOf<WikiData>()                //  一覧リスト取得データ
    val mSearchFormTitle = listOf(
        "自動", "箇条書き,制限あり", "箇条書き", "表形式", "表形式2", "グループ形式", "参照", "表・箇条書き", "表2・箇条書き")
    enum class SEARCHFORM {
        NON, LISTLIMIT, LISTUNLIMIT, TABLE, TABLE2, GROUP, REFERENCE, TABLE_LIST, TABLE2_LIST;
        fun getOrder(v: Int): SEARCHFORM {      //  位置を取得
            return when (v) {
                0 -> SEARCHFORM.NON
                1 -> SEARCHFORM.LISTLIMIT
                2 -> SEARCHFORM.LISTUNLIMIT
                3 -> SEARCHFORM.TABLE
                4 -> SEARCHFORM.TABLE2
                5 -> SEARCHFORM.GROUP
                6 -> SEARCHFORM.REFERENCE
                7 -> SEARCHFORM.TABLE_LIST
                8 -> SEARCHFORM.TABLE2_LIST
                else -> SEARCHFORM.NON
            }
        }
    }
    var mSearchForm = SEARCHFORM.NON
    var mGetListDataStop = false            //  詳細データ読込中断フラグ
    var mOutFolder = ""                     //  一覧リスト出力先フォルダ
    lateinit var mWikiList: WikiList
    var mCount = -1                         //  検索数
    var mFilesSize = -1                     //  検索対象ファイル数
    var mFilesCount = -1                    //  検索したファイル数

    var klib = KLib()
    var kparse = KParse()


    /**
     * WikipediaのHTMLソースをダウンロードしてファイルに保存
     * 保存後は保存ファイルを使用する
     * url          HTMLを取得するためのURLアドレス
     * overWrite    取得したデータの更新有無
     */
    fun listDownLoad(url: String, overWrite: Boolean = false) {
        var baseUrl = url.substring(0, url.indexOf('/', 10))
        //  一時保存ファイル
        var downLoadFilePath = mOutFolder + "/" + "listDataTemp.html"
        var downLoadFile = File(downLoadFilePath)
        if (downLoadFile.exists())
            downLoadFile.delete()
        if (!downLoadFile.exists() || overWrite) {
            var downLoad = DownLoadWebFile(url, downLoadFile.path)
            downLoad.start()
            while (downLoad.isAlive()) {
                Thread.sleep(100L)
            }
        }
        if (downLoadFile.exists()) {
            var count = getListFile(downLoadFile.path, baseUrl, url.substring(url.lastIndexOf('/') + 1))
            mWikiList.tvCnvForm.text = mSearchFormTitle[mSearchForm.ordinal]
            mWikiList.tvListCount.text = "データ数: " + count
            Log.d(TAG,"listDownLoad: "+mSearchForm.ordinal+ " "+mSearchFormTitle[mSearchForm.ordinal]+ " "+mSearchForm)
        }
    }

    /**
     * HTMLファイルから一覧リストを取得しListViewに表示
     * filePath     HTMLファイルパス
     * baseUrl      参照URLのベースURL(WikipediaのURL)
     * return       データ数
     */
    fun getListFile(filePath:String, baseUrl: String, listName: String):Int {
        var htmlSrc = kparse.loadHtmlData(filePath).toString()  //  HTMLファイルの読込
        var listData = kparse.getHTMLListData(htmlSrc)          //  HTMLソースのリスト化
        var dataCount = 0
        var searchForm = SEARCHFORM.NON
        //  箇条書き・制限付き
        if (mSearchForm == SEARCHFORM.NON || mSearchForm == SEARCHFORM.LISTLIMIT) {
            searchForm = SEARCHFORM.LISTLIMIT
            mDataList.clear()
            dataCount = getList(listData, "li", true, baseUrl, listName, searchForm)
        }
        //  表形式
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.TABLE) {
            searchForm = SEARCHFORM.TABLE
            mDataList.clear()
            dataCount = getList(listData,"tr", false, baseUrl, listName, searchForm)
        }
        //  グループ形式
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.GROUP) {
            searchForm = SEARCHFORM.GROUP
            mDataList.clear()
            dataCount = getList(listData, "span", false, baseUrl, listName, searchForm)
        }
        //  箇条書き
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.LISTUNLIMIT) {
            searchForm = SEARCHFORM.LISTUNLIMIT
            mDataList.clear()
            dataCount = getList(listData, "li", false, baseUrl, listName, searchForm)
        }
        //  参照
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.REFERENCE) {
            searchForm = SEARCHFORM.REFERENCE
            mDataList.clear()
            dataCount = getList(listData, "a", false, baseUrl, listName, searchForm)
        }
        //  表形式2
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.TABLE2) {
            searchForm = SEARCHFORM.TABLE2
            mDataList.clear()
            dataCount = getList(listData,"tr", false, baseUrl, listName, searchForm, true)
        }
        //  表形式・箇条書き
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.TABLE_LIST) {
            searchForm = SEARCHFORM.TABLE_LIST
            mDataList.clear()
            dataCount = getList(listData,"tr", false, baseUrl, listName, searchForm)
            dataCount += getList(listData, "li", false, baseUrl, listName, searchForm)
        }
        //  表形式2・箇条書き
        if ((mSearchForm == SEARCHFORM.NON && dataCount < 25) || mSearchForm == SEARCHFORM.TABLE2_LIST) {
            searchForm = SEARCHFORM.TABLE2_LIST
            mDataList.clear()
            dataCount = getList(listData,"tr", false, baseUrl, listName, searchForm, true)
            dataCount += getList(listData, "li", false, baseUrl, listName, searchForm)
        }

        mSearchForm = searchForm
        return dataCount
    }

    /**
     * HTMLリストデータから一覧データを取得する
     *  listData     HTMLソースのリスト化データ
     *  tagName     フィルタリングするタグ名(省略時は[li]
     *  limit       キーワードによる検索終了位置を設定
     *  baseUrl     基準URL
     *  listName    一覧リスト名
     *  searchForm  抽出方法
     *  secondTitle 表の2列目をタイトルとする
     *  return      取得データリスト
     */
    fun getList(listData: List<String>, tagName:String = "li", limit: Boolean = false,
                baseUrl: String, listName: String, searchForm: SEARCHFORM = SEARCHFORM.NON, secondTitle: Boolean = false): Int {
        var endPos = -1
        if (limit)
            endPos = limitDataPosition(listData)
        return getListItem(listData, baseUrl, listName, tagName, 0, endPos, searchForm, secondTitle)
    }

    /**
     * HTMLリストデータから一覧データを取得する
     * listData     HTMLのリスト化データ
     * baseUrl      基準RL
     * listName     一覧リスト名
     * tagName      フィルタリングするタグ名(省略時は[li]
     * startPos     検索範囲の開始位置
     * endPos       検索範囲の終了位置
     * searchForm   抽出方法
     * secondTitle  表の2列目のタイトル
     * return       検索結果
     */
    fun getListItem(listData: List<String>, baseUrl: String, listName: String, tagName:String = "li", startPos: Int = 0,
                    endPos: Int = -1, searchForm: SEARCHFORM = SEARCHFORM.NON, secondTitle: Boolean = false): Int {
        var st = startPos
        var count = 0
        var titleCount = if (secondTitle) 1 else 0

        while (st < listData.count() && (endPos < 0 || st < endPos)) {
            var tagPos = kparse.getTagData(listData, tagName, st)
            if (tagPos.first < 0 || mGetListDataStop)
                break
            if (tagName.compareTo("tr") == 0 || tagName.compareTo("li") == 0) {
                var fp = kparse.findTagDataPos(listData, tagName, tagPos.first + 1, tagPos.second)
                if (0 < fp) {
                    //  入れ子処理
                    var cnt = getListData(listData, tagPos, tagName, titleCount)
                    if (0 < cnt) {
                        mDataList[mDataList.count() - 1].mUrl = baseUrl + mDataList[mDataList.count() - 1].mUrl
                        mDataList[mDataList.count() - 1].mListName = listName
                        mDataList[mDataList.count() - 1].mSearchForm = mSearchFormTitle[searchForm.ordinal]
                        count += cnt
                    }
                    //  再帰処理
                    count += getListItem(listData, baseUrl, listName, tagName, tagPos.first + 1, tagPos.second, searchForm, secondTitle)
                    st = tagPos.second + 1
                    continue
                }
            }
            var cnt = getListData(listData, tagPos, tagName, titleCount)
            if (0 < cnt) {
                mDataList[mDataList.count() - 1].mUrl = baseUrl + mDataList[mDataList.count() - 1].mUrl
                mDataList[mDataList.count() - 1].mListName = listName
                mDataList[mDataList.count() - 1].mSearchForm = mSearchFormTitle[searchForm.ordinal]
                count += cnt
            }
            st = tagPos.second + 1
        }

        return count
    }

    /**
     * HTMLリストデータから一覧データを登録する
     *  listData     HTMLソースのリスト化データ
     *  tagPos      対象範囲
     *  tagName     抽出対象タグ名
     *  titleCount  表形式でタイトルの対象列('a'タグが含まれている列でカウント)
     *  return      登録数(0/1)
     */
    fun getListData(listData: List<String>, tagPos: Pair<Int, Int>,
                    tagName:String = "li", titleCount: Int = 0): Int {
        Log.d(TAG,"getListData: " + tagName + " " + titleCount)
        var count = 0
        var title = ""
        var url = ""
        var comment = ""
        var commentPos = 0
        var tagList = listData.slice(tagPos.first..tagPos.second)
        tagList = kparse.stripTagData(tagList, "rb")  //  ルビ対象文字を除外(ルビ(rt)は残す)
        if (tagList.count() <= 0)
            return 0
        if (tagName.compareTo("tr") == 0) {
            //  表形式(td(列))
            var st = 0
            while (st < tagList.count()) {
                var tagPos = kparse.getTagData(tagList, "td", st)
                if (tagPos.first < 0)
                    break
                var paraDataPos = kparse.findParaDataPos(tagList, "a", "title", tagPos.first, tagPos.second, 0)
                if (0 <= paraDataPos) {
                    if (count == titleCount) {
                        title = kparse.getTagPara(tagList[paraDataPos], "title")
                        url = kparse.getTagPara(tagList[paraDataPos], "href")  //  参照URL
                        url = URLDecoder.decode(url, "UTF-8")                   //  URLの変換
                        if (kparse.getTagType(tagList[paraDataPos + 1]) == 7) {
                            title = tagList[paraDataPos + 1]
                            commentPos = paraDataPos + 2
                        }
                    }
                    count++
                }
                st = tagPos.second + 1
            }
        } else {
            //  表形式以外で箇条書き(li)や参照(a)
            var paraDataPos = kparse.findParaDataPos(tagList, "a", "title", 0, -1, titleCount)   //  タイトル位置
            if (0 <= paraDataPos) {
                Log.d(TAG,"getListData: " + paraDataPos + " " + tagList[paraDataPos])
                title = kparse.getTagPara(tagList[paraDataPos], "title")
                url = kparse.getTagPara(tagList[paraDataPos], "href")  //  参照URL
                url = URLDecoder.decode(url, "UTF-8")                   //  URLの変換
                Log.d(TAG,"getListData: " + title + " " + url)
                commentPos = 0
                if (paraDataPos + 1 < tagList.count() && kparse.getTagType(tagList[paraDataPos + 1]) == 7) {
                    title = tagList[paraDataPos + 1]
                    commentPos = paraDataPos + 2
                }
                count++
            }
        }
        if (0 < count) {
            comment = kparse.getDataAll(tagList, commentPos, tagList.count() - 1).joinToString(separator = "")
            mDataList.add(WikiData(title, comment, url, "", "", "mOutFolder"))
        }
        return count
    }

    /**
     * 検索の終了位置を求める
     * listData     HTMLソースリストデータ
     * return       終了位置
     */
    fun limitDataPosition(listData: List<String>): Int {
        val stopTagData = listOf("脚注", "References", "関連項目", "参考文献" )
        var stopPos = -1
        for (i in listData.indices) {
            for (stopWord in stopTagData) {
                stopPos = listData[i].indexOf("id=\"" + stopWord)
                if (0 <= stopPos) {
                    return i
                }
            }
            stopPos = listData[i].indexOf("<div class=\"printfooter")
            if (0 <= stopPos)
                return i
            stopPos = listData[i].indexOf("<div class=\"navbox")
            if (0 <= stopPos)
                return i
        }
        return -1
    }

    /**
     * 詳細情報の追加
     * 一覧リストのURLからWikipediaのページから基本情報を取得し座標位置があれば一覧リストに追加する
     */
    fun addBaseInfo() {
//        mWikiList.pbGetInfCount.max = mDataList.count()
        mCount = 1
        for (i in mDataList.indices) {
            Log.d(TAG,"addBaseInfo: "+mDataList[i].mTitle)
            mDataList[i].mOutFolder = mOutFolder
            var buf = mDataList[i].setBaseInfo()
            mCount++
//            mWikiList.tvListCount.text = "[ " + (count++) + " / " + mDataList.count() + " ]"
//            mWikiList.pbGetInfCount.progress = count
            if (mGetListDataStop)
                break
        }
        mCount = -1
//        mWikiList.pbGetInfCount.progress = 0
    }

    fun getSearchFileCount(): Int {
        val dir = File(mOutFolder)
        val files = dir.listFiles()
        return if (files == null) 0 else files.size
    }

    /**
     * 保存されたファイルから検索してリストデータに登録
     * word     検索文字列
     * url
     */
    fun searchFileWord(word: String, url: String = "") {
        if (word.length <= 0)
            return
        //  検索ファイル名
        var searchFile = ""
        if (0 <= url.indexOf("http"))
            searchFile = url.substring(url.lastIndexOf('/') + 1)
        Log.d(TAG,"searchFileWord: " + word + " " + searchFile)

        //  検索ワードが座標であれば座標値を求める
        var coord = PointD(0.0, 0.0)
        var dis = 20.0
        if (0 <= word.indexOf("北緯") && 0 <= word.indexOf("東経")) {
            Log.d(TAG, "searchFileWord: "+word)
            coord = klib.string2Coordinate(word)
            var d1 = word.indexOf("km以内")
            var d0 = word.lastIndexOf(" ", d1)
            if (0 <= d1 && 0 <= d0) {
                dis = word.substring(d0, d1).toDouble()
                Log.d(TAG,"searchFileWord: "+coord.x+" "+coord.y+" "+dis)
            }
        }
        //  ファイル名取得
        val dir = File(mOutFolder)
        val files = dir.listFiles()
        if (files != null) {
//            mWikiList.tvListCount.setText("データ数: ")
//            mWikiList.pbGetInfCount.max = files.size
            mFilesSize = files.size
            mFilesCount = 1
            mCount = 0
            //  ファイル内検索
            mDataList.clear()
            for (i in files.indices) {
                if (files[i].isFile) {
                    if (files[i].extension.compareTo("csv") == 0 &&
                        (searchFile.length == 0 || 0 <= files[i].name.indexOf(searchFile))) {
                        //  ファイル内を検索してリストに追加
                        try {
                            Log.d(TAG, "searchFileWord: " + files[i].nameWithoutExtension)
                            if (coord.x == 0.0 || coord.y == 0.0) {
                                loadData(files[i].nameWithoutExtension, word)
                            } else {
                                loadData(files[i].nameWithoutExtension, coord, dis)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                mWikiList,
                                e.message + "\n" + files[i].nameWithoutExtension,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                mFilesCount++
//                mWikiList.pbGetInfCount.progress = count++
            }
            //  検索地点からの距離でソート
            mDataList.sortBy { it.mDistance }
//            mWikiList.pbGetInfCount.progress = 0
            mFilesCount = -1
            //  検索結果を一覧リストに表示
//            setWikiDataList(mWikiDataList)
        }
    }

    /**
     * 表示中のデータから文字列を検索
     * 検索文字列がない時はリストの先頭または最後尾に移動
     * word         検索文字列
     * position     検索開始位置
     * order        検索方向
     */
    fun searchWord(word: String, position: Int, order:Boolean): Int {
        if (word.length <= 0) {
            //  表示位置の移動
            if (order) {
                mWikiList.lvListData.setSelection(mDataList.count() - 1)
            } else {
                mWikiList.lvListData.setSelection(0)
            }
            return position
        }
        //  文字列の検索
        var pos = Math.max(0, position)
        pos = Math.min(pos, mDataList.count() - 1)
        if (order) {
            //  順方向に検索
            for (i in pos..mDataList.count() - 1) {
                Log.d(TAG,"searchWord: "+i+" "+pos+" "+mDataList.count())
                var wikiData = mDataList[i]
                if (0 <= wikiData.mTitle.indexOf(word) ||
                    0 <= wikiData.mComment.indexOf(word) ||
                    0 <= wikiData.mCoordinate.indexOf(word)) {
                    mWikiList.lvListData.setSelection(i)
                    return i
                }
            }
        } else {
            //  逆方向に検索
            for (i in pos downTo 0) {
                var wikiData = mDataList[i]
                if (0 <= wikiData.mTitle.indexOf(word) ||
                    0 <= wikiData.mComment.indexOf(word) ||
                    0 <= wikiData.mCoordinate.indexOf(word)) {
                    mWikiList.lvListData.setSelection(i)
                    return i
                }
            }
        }
        return position
    }

    /**
     * 一覧表データをファイルに保存
     * fileName     ファイル名(拡張子なし)
     */
    fun saveData(fileName: String) {
        var filePath = mOutFolder + "/" + fileName + ".csv"
        filePath = filePath.replace(':', '_')   //  ':'がファイル名として使えない場合がある
        var dataList = mutableListOf<List<String>>()
        for (wikiData in mDataList) {
            dataList.add(wikiData.getStringList())
        }
        Log.d(TAG,"saveData: "+dataList.count()+" "+filePath)
        if (0 < dataList.count())
            klib.saveCsvData(filePath, WikiData.mDataTitle, dataList)
    }

    /**
     * 一覧表のデータをファイルから読み込む
     * fileName     ファイル名(拡張子なし)
     * searchWord   検索文字(省略可)
     */
    fun loadData(fileName: String, searchWord: String = ""): Boolean {
        var filePath = mOutFolder + "/" + fileName + ".csv"
        filePath = filePath.replace(':', '_')   //  ':'がファイル名として使えない場合がある
        if (klib.existsFile(filePath)) {
            var dataList = klib.loadCsvData(filePath, WikiData.mDataTitle)
            for (data in dataList) {
                if (0 < searchWord.length) {
                    //  検索文字でフィルタリングする
                    for (i in data.indices) {
                        if (0 < data[i].indexOf(searchWord)) {
                            var wikiData = WikiData()
                            wikiData.setStringList(data)
                            mDataList.add(wikiData)
                            mCount++
//                            mWikiList.tvListCount.setText("データ数: "+mDataList.size)
                            break
                        }
                    }
                } else {
                    //  すべて登録
                    var wikiData = WikiData()
                    wikiData.setStringList(data)
                    mDataList.add(wikiData)
                }
                if (mGetListDataStop)
                    break
            }
            Log.d(TAG,"loadData: "+mDataList.count()+" "+filePath)
            return true
        }
        Log.d(TAG,"loadData: not Exists "+mDataList.count()+" "+filePath)
        return  false
    }

    /**
     * 一覧表のデータを読み込んで検索地点からの距離が範囲内のデータだけを登録する
     * fileName         ファイル名(拡張子なし)
     * searchCoord      検索位置の座標
     * dis              検索地点からの距離の対象範囲(km)
     */
    fun loadData(fileName: String, searchCoord: PointD, searchDis: Double): Boolean {
        var filePath = mOutFolder + "/" + fileName + ".csv"
        filePath = filePath.replace(':', '_')   //  ':'がファイル名として使えない場合がある
        if (klib.existsFile(filePath)) {
            var dataList = klib.loadCsvData(filePath, WikiData.mDataTitle)
            var n = WikiData.mDataTitle.indexOf("座標")
            if (n < 0 || dataList.size < 1)
                return  false

            for (data in dataList) {
                if (6 < data.size && 0 <= data[n].indexOf("北緯")) {
                    var coord = klib.string2Coordinate(data[n])
                    if (coord.x != 0.0 && coord.y != 0.0) {
                        var dis = klib.cordinateDistance(searchCoord, coord)
                        if (dis < searchDis) {
                            var wikiData = WikiData()
                            wikiData.setStringList(data)
                            wikiData.mDistance = dis
                            mDataList.add(wikiData)
                            mCount++
//                            mWikiList.tvListCount.setText("データ数: "+mDataList.size)
                        }
                    }
                }
                if (mGetListDataStop)
                    break
            }
            return true
        }
        return  false
    }
}