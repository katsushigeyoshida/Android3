package jp.co.yoshida.katsushige.mylib

import java.io.File

class KParse {
    val TAG = "KParse"

    /**
     *  HTMLファイルからのデータの読込
     *  path    HTMLファイル名
     *  return  ファイルデータ
     */
    fun loadHtmlData(path: String): StringBuilder {
        var buffer = StringBuilder()

        val file = File(path)
        if (file.exists()) {
            val bufferReader = file.bufferedReader()    //  UTF8 (SJISの時はCharset.forName("MS932")を追加)
            var str = bufferReader.readLine()
            //  BOM付きの場合、BOM削除
            if (2 < str.length && str.startsWith("\uFEFF"))
                str = str.substring(1)
            while (str != null) {
                buffer.append(str)
                buffer.append('\r')
                str = bufferReader.readLine()
            }
            bufferReader.close()
        }
        return buffer
    }

    /**
     *  HTMLデータをアイテム単位でリスト化
     *  fileData    HTMLテキストデータ
     *  return
     */
    fun getHTMLListData(fileData: String): List<String> {
        var listData = mutableListOf<String>()  //  HTML,XMLのデータをタグとデータにしたリスト
        listData.clear()
        var buffer = ""
        var itemOn = false
        for (i in 0..fileData.length - 1) {
            when (fileData[i]) {
                '<' -> {
                    if (0 < buffer.length) {
                        listData.add(buffer)
                        buffer = ""
                        itemOn = true
                    }
                    buffer += fileData[i]
                }
                '>' -> {
                    buffer += fileData[i]
                    buffer = buffer.trim(' ', '\n', '\r', '\t')
                    if (0 < buffer.length)
                        listData.add(buffer)
                    buffer = ""
                    itemOn = false
                }
                '\r' -> {
                    if (!itemOn) {
                        buffer = buffer.trim(' ', '\n', '\r', '\t')
                        if (0 < buffer.length) {
                            listData.add(buffer)
                            buffer = ""
                        }
                    }
                }
                ' ' -> {
                    if (buffer.length == 0)
                        continue
                    else if (1 < (fileData.length - i) && fileData[i + 1] == ' ')
                        continue
                    else
                        buffer += fileData[i]
                }
                '\t' -> {
                    continue
                }
                else -> {
                    buffer += fileData[i]
                }
            }
        }
        return listData
    }

    /**
     * HTMLリストデータからデータのみを抽出する
     * atgList      HTMLリストデータ
     * startPos     開始位置
     * endPos       終了位置
     * return       データ抽出リスト
     */
    fun getDataAll(tagList: List<String>, startPos: Int, endPos: Int): List<String> {
        var dataList = mutableListOf<String>()
        for (pos in startPos..endPos) {
            if (getTagType(tagList[pos]) == 7) {
                dataList.add(tagList[pos])
            }
        }
        return dataList
    }

    /**
     * タグデータの抽出(入れ子構造に対応, タグも含む)
     * tagList      HTMLソースのリスト
     * tagName      抽出するタグ名
     * startPos     検索開始位置(リストの位置)
     * return       (開始位置,終了位置) (タグが存在しない場合　開始位置 < 0)
     */
    fun getTagData(tagList: List<String>, tagName: String, startPos: Int = 0, endPos: Int = -1): Pair<Int, Int> {
        var nestCount = 0
        var start = -1
        var pos = startPos
        while (pos < tagList.count() && (endPos < 0 || pos < endPos)) {
            var tagType = getTagType(tagList[pos], tagName)
            when (tagType) {
                1 -> {      //  完結タグ
                    if (start < 0) {
                        start = pos
                        break
                    }
                }
                2 -> {      //  開始タグ
                    if (start < 0)
                        start = pos
                    nestCount++
                }
                3 -> {      //  終了タグ
                    nestCount--
                }
            }
            pos++
            if (0 <= start && nestCount <= 0)
                break
        }
        return Pair(start, pos - 1)
    }

    /**
     * リストデータから指定のタグデータを除く
     * tagList      HTMLソースリストデータ
     * tagName      除外するタグ名
     * return       指定タグを除外したリストデータ
     */
    fun stripTagData(tagList: List<String>, tagName: String): List<String> {
        var outList = mutableListOf<String>()
        var count = 0
        for (data in tagList) {
            var tagType = getTagType(data, tagName)
            when (tagType) {
                1 -> continue
                2 -> count++
                3 -> {
                    count--
                    continue
                }
            }
            if (count == 0)
                outList.add(data)
        }
        return outList
    }

    /**
     * リストデータからタグ名とタグパラメータを検索して検索したタグデータを返す
     * liatData     HTMLリストデータ
     * tagName      検索するタグ名
     * paraTitle    パラメータタイトル名(省略可)
     * count        出現回数
     * return       タグデータ(<TAG ... >)
     */
    fun findParaData(listData: List<String>, tagName: String, paraTitle: String = "", count: Int = 0): String {
        var findCount = 0
        for (i in listData.indices) {
            var tagType = getTagType(listData[i], tagName)
            if (tagType == 1 || tagType == 2) {     //  完結タグか開始タグ
                findCount++
                if (count < findCount) {
                    if (paraTitle.length == 0)
                        return listData[i]
                    var para = getTagPara(listData[i], paraTitle)
                    if (0 < para.length)
                        return listData[i]
                }
            }
        }
        return ""
    }

    /**
     * リストデータから開始タグを検索してタグデータ(<TAG ...>)を返す
     * 見つからなかった場合、空文字を返す
     * liatData     HTMLリストデータ
     * tagName      検索するタグ名
     * startPos     開始開始位置
     * endPos       終了位置
     * return       検索したタグデータ
     */
    fun findTagData(listData: List<String>, tagName: String, startPos: Int = 0, endPos: Int = -1): String {
        var starTag = "<" + tagName
        var ePos = if (endPos < 0) listData.count() - 1 else endPos
        for (i in startPos..ePos) {
            if (0 <= listData[i].indexOf(starTag)) {
                return listData[i]
            }
        }
        return ""
    }

    /**
     * リストデータから開始タグを検索して検出下位置を返す
     * liatData     HTMLリストデータ
     * tagName      検索するタグ名
     * paraTitle    パラメータのタイトルID
     * sp           開始開始位置
     * ep           終了位置(省略可、-1で最後)
     * count        検出回数
     * return       検索したタグデータ位置
     */
    fun findParaDataPos(listData: List<String>, tagName: String, paraTitle: String = "", sp: Int = 0, ep: Int =  -1, count: Int = 0): Int {
        var findCount = 0
        var eep = if (ep < 0) listData.count() - 1 else ep
        for (i in sp..eep) {
            var tagType = getTagType(listData[i], tagName)
            if (tagType == 1 || tagType == 2) {     //  完結タグか開始タグ
                findCount++
                if (count < findCount) {
                    if (paraTitle.length == 0)
                        return i
                    var para = getTagPara(listData[i], paraTitle)
                    if (0 < para.length)
                        return i
                }
            }
        }
        return -1
    }

    /**
     * リストデータから開始タグを検索してタグデータ(<TAG ...>)を返す
     * mNextPosに検索位置の次の位置が入る
     * liatData     HTMLリストデータ
     * tagName      検索するタグ名
     * startPos     開始開始位置(省略可)
     * endPos       終了位置(省略可)
     * return       検索したタグデータ位置
     */
    fun findTagDataPos(listData: List<String>, tagName: String, startPos: Int = 0, endPos: Int = -1): Int {
        var starTag = "<" + tagName
        var ePos = if (endPos < 0) listData.count() - 1 else endPos
        for (i in startPos..ePos) {
            if (0 <= listData[i].indexOf(starTag)) {
                return i
            }
        }
        return -1
    }

    /**
     * タグデータのパラメータから指定タイトルのデータを抽出
     * tagData      タグデータ(<TAG title="abcd" >)
     * title        パラメータのタイトル
     * return       指定したタイトルのパラメータ
     */
    fun getTagPara(tagData: String, title: String): String {
        var pp = tagData.indexOf(" " + title + "=")
        if (0 <= pp) {
            var sp = tagData.indexOf('\"', pp)
            if (0 < sp) {
                var ep = tagData.indexOf('\"', sp + 1)
                if (sp + 1 < ep)
                    return tagData.substring(sp + 1, ep)
            }
        }
        return ""
    }

    /**
     * タグの種別を取得する
     * タグの種類  0: 不明, 1: 完結タグ, 2: 開始タグ, 3: 終了タグ,
     *           4: コメントタグ, 5:コメント開始タグ, 6:コメント終了タグ, 7:データ
     * tagData      タグ文字列
     * tagName      対象タグ名(省略可)
     * return       タグの種類
     */
    fun getTagType(tagData: String, tagName: String = ""): Int{
        if (tagData[0] != '<')
            return 7					//	データ
        if (0 < tagName.length)
            if (tagData.indexOf("<" + tagName + " ") < 0 &&
                    tagData.indexOf("<" + tagName + ">") < 0 &&
                    tagData.indexOf("</" + tagName + ">") < 0)
                return 0					//	タグ名がない
        if (tagData[1] == '/')
            return 3					//	終了タグ
        if (tagData[1] == '!') {
            if (tagData[tagData.length - 1] =='>')
                return 4				//	コメント
            else
                return 5				//	コメント開始
        }
        if (0 < tagData.indexOf("/>"))
            return 1					//	完結タグ
        if (tagData[tagData.length - 1] =='>')
            return 2					//	開始タグ
        return 0						//	不明/TAGNAME以外
    }

    /**
     *  タグの種類を文字列で取得
     *  type        タグ蛮行
     *  return      タグ名
     */
    fun getTagTypeName(type: Int): String{
        var tagName = listOf("?", "完結タグ", "開始タグ", "終了タグ",
                "コメントタグ", "コメント開始タグ", "コメント終了タグ", "データ")
        return tagName[type]
    }

    /**
     * HTMLソースをスペースの挿入でインデントをつける
     */
    fun layeredHtmlDisp(htmlList: List<String>): String {
        var buffer = ""
        var indent = 0
        var nextIndent = 0
        var lineCount = 0
        for (tagData in htmlList) {
            var tagType = getHtmlTagType(tagData)
            nextIndent = 0
            when (tagType) {
                2 -> { nextIndent = 1 }     //  <TAG ....>
                3 -> { indent -= 1 }        //  </TAG>
                5 -> { nextIndent = 1 }     //  <!-- ....
                6 -> { indent -= 1 }        //  ...-->
            }
            if (0 <= indent)
                buffer += "  ".repeat(indent) + tagData + "\n"
            indent += nextIndent
            lineCount++
        }

        return buffer
    }

    /**
     *  タグデータの種類を判別する
     *  タグの種類
     *   1: <TAG ..../>
     *   2: <TAG ....>
     *   3: </TAG>
     *   4: <!....>
     *   5: <!-- ....
     *   6: ...-->
     *   7: ...DATA...
     *   0: ?
     *
     *   tagData    タグデータ
     *   pos        タグ検索開始位置(=0)
     *   return     タグの種類
     */
    fun getHtmlTagType(tagData: String, pos: Int = 0): Int {
        val st: Int = tagData.indexOf('<', pos)
        val et: Int = tagData.indexOf('>', pos)
        return if (st < 0 && et < 0) {
            7                                               //  データ(タグがない)
        } else if (st < 0 && 0 <= et) {
            if (0 <= tagData.indexOf("-->", pos)) 6      //  コメント終端(-->)
            else 0                                          //  不明(...>)
        } else if (0 <= st && et < 0) {
            if (0 <= tagData.indexOf("<!--", pos)) 5     //  コメント開始(<!--)
            else 0                                          //  不明(<...)
        } else if (0 <= st && 0 <= et) {
            if (st == tagData.indexOf("</", pos)) 3  	//  終端タグ(</TAG>
            else if (st == tagData.indexOf("<!", pos)) 4	//  コメント(<!...>)
            else if (tagData[et - 1] == '/') 1			    //  完結タグ(<TAG .../>)
            else 2                                          //  開始タグ(<TAG ...>)
        } else 0                                            //  不明
    }

    /**
     * タグの位置を検索する
     * html     HTMLソース
     * tagName  タグ名(<>は含まない)
     * pos      検索開始位置
     * return   検出した位置(<0 の時は見つからなかった時　
     */
    fun findHtmlTag(html: String, tagName: String, pos: Int = 0): Int {
        var startTagPos = html.indexOf("<" + tagName, pos)
        var endTagPos = html.indexOf("</" + tagName+ ">", pos)
        return if (startTagPos < 0 && endTagPos < 0) -1
        else if (0 <= startTagPos && endTagPos < 0) startTagPos
        else if (startTagPos < 0 && 0 <= endTagPos) endTagPos
        else Math.min(startTagPos, endTagPos)
    }

    /**
     * 開始タグから終了タグまでのデータ位置を取得(ネスト構造に対応)
     * html                     HTMLソース
     * tagName                  抽出するタグ名
     * pos                      開始位置(defualt = 0)
     * return(first, second)    (開始位置,終了位置)
     */
    fun getHtmlTagData(html: String, tagName: String, pos: Int = 0): Pair<Int, Int> {
        var startPos = findHtmlTag(html, tagName, pos)
        var endPos = -1
        if (startPos < 0)
            return Pair(startPos, endPos)
        var tagType = getHtmlTagType(html, startPos)
        if (tagType != 1 && tagType != 2) {
            startPos = -1
            return Pair(startPos, endPos)
        }
        endPos = html.indexOf('>', startPos) + 1
        if (tagType == 1)					//	完結タグ
            return Pair(startPos, endPos)
        var nestCount = 0
        var nextPos = endPos
        while (0 <= nestCount) {
            var sp = findHtmlTag(html, tagName, nextPos + 1)
            if (sp < 0)
                break
            tagType = getHtmlTagType(html, sp)
            endPos = html.indexOf('>', sp) + 1
            when (tagType) {
                2 -> nestCount++					//	開始タグ
                3 -> nestCount--					//	終了タグ
                0 -> return Pair(startPos, endPos)	//	不明
            }
            nextPos = sp
        }
        return Pair(startPos, endPos)
    }
}