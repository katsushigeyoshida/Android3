package jp.co.yoshida.katsushige.tablegraph

import android.util.Log
import jp.co.yoshida.katsushige.mylib.KCalc
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.mylib.RectD
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SheetData() {
    private val TAG = "SheetData"

    enum class DATATYPE { NON, STRING, NUMBER, DATE, WEEK, MONTH, YEAR, TIME, WEEKDAY }

    var mDataList = mutableListOf<List<String>>()       //  元データリスト(文字列)
    var mDoubleDataList = mutableListOf<List<Double>>() //  数値データリスト
    private var mDoubleDataStack = mutableListOf<List<List<Double>>>()  //  数値データリストのスタック
    var mDataType = mutableListOf<DATATYPE>()           //  列ごとのデータ種別リスト
    private var mColSelectList = mutableListOf<Boolean>()       //  列選択リスト
    var mRowSelectList = mutableListOf<Boolean>()       //  行選択リスト
    var mDispList = mutableListOf<Boolean>()            //  表示/非表示リスト
    var mColorList = mutableListOf<Int>()               //  カラー番号リスト
    var mDataArea = RectD()                             //  表示するデータ領域(最大値,最小値)
    var mStartRow = 0
    var mEndRow = -1

    private val klib = KLib()

    constructor(dataList: List<List<String>>): this() {
        mDataList = dataList.toMutableList()    //  コピー作成
    }

    constructor(dataList: List<List<String>>, rowSelectList: List<Boolean>): this() {
        mDataList = dataList.toMutableList()            //  コピー作成
        mRowSelectList = rowSelectList.toMutableList()
    }

    /**
     * 集計処理
     * パラメータ列なし(すべて-1)　左端の列の重複をなくして、他の数値データを合算する
     * パレメータ1の時(rowTitleのみ) 指定列の重複をなくして、他の数値データを合算する
     * パラメータ2の時(dataTitleのみなし) rowTitle列を縦軸にしてcolTitle列を集計していく
     * パラメータ3の時(すべてあり) rowTitle列を縦軸にしてcolTitle列ごとにdataTitle列を集計する
     * rowTitle     行タイトルの列番号
     * colTitle     列タイトルの列番号
     * dataTitle    データ列の列番号
     */
    fun pivotTable(rowTitle: Int, colTitle: Int, dataTitle: Int) {
        Log.d(TAG, "pivotTable: "+rowTitle+" "+colTitle+" "+dataTitle)
        if (rowTitle < 0) {
            return squeezeTable()
        } else if (colTitle < 0) {
            return squeezeTable(rowTitle)
        } else if (dataTitle < 0) {
            return pivotTableSub2(rowTitle, colTitle)
        } else {
            return pivotTableSub(rowTitle, colTitle, dataTitle)
        }
    }

    /**
     * 週計処理
     * 列タイトルの種別ごとにデータタイトル列の数値を集計する
     * rowTitle     行タイトルの列番号(表の左端のタイトル)
     * col Title    列タイトルの列番号(表の最上部のタイトル)
     * dataTitle    週計するデータの列番号(0> はcolTitle列をカウントする)
     */
    private fun pivotTableSub(rowTitle: Int, colTitle: Int, dataTitle: Int) {
        Log.d(TAG, "pivotTableSub: "+rowTitle+" "+colTitle+" "+dataTitle)
        setDataType()
        val dataMap = mutableMapOf<String, Map<String, Double>>()   //  一時保存データマップ
        val titleList = getTitleList(colTitle, rowTitle)
        for (dataList in mDataList) {
            if (dataList[0].compareTo(mDataList[0][0]) == 0)
                continue
//            Log.d(TAG, "pivotTableSub: "+dataList[rowTitle]+" "+dataList[colTitle]+" "+dataList[dataTitle])
            val valMap = mutableMapOf<String, Double>()
            if (dataMap[dataList[rowTitle]].isNullOrEmpty()) {
                //  Mapリストデータに新規追加
                valMap[dataList[colTitle]] = dataList[dataTitle].toDoubleOrNull()?:0.0
                dataMap[dataList[rowTitle]] = valMap
            } else {
                //  データの更新(コピーを作って更新し、Mapリストのデータと入れ替える)
                val valueMap = mapCopyAddData(dataMap[dataList[rowTitle]], dataList[colTitle],
                    if (dataTitle < 0) 1.0 else dataList[dataTitle].toDoubleOrNull()?:0.0)
                dataMap.remove(dataList[rowTitle])
                dataMap[dataList[rowTitle]] = valueMap
            }
        }

        //  抽出したデータをmDataListに戻す
        mDataList.clear()
        mDataList.add(titleList)
        for ((key,value) in dataMap) {
            val dataList = mutableListOf<String>()
            dataList.add(key)
            for (i in 1..titleList.lastIndex)
                dataList.add("")
            for ((k, v) in value) {
                dataList[titleList.indexOf(k)] = v.toString()
            }
            mDataList.add(dataList)
        }
    }

    /**
     * 週計処理2
     * 列タイトルのデータをカウントしてデータごとに集計する
     * rowTitle     行タイトルの列番号(表の左端のタイトル)
     * col Title    列タイトルの列番号(表の最上部のタイトル)
     */
    private fun pivotTableSub2(rowTitle: Int, colTitle: Int) {
        Log.d(TAG, "pivotTableSub2: " + rowTitle + " " + colTitle)
        setDataType()       //  列ごとのデータタイプを求める
        val dataMap = mutableMapOf<String, Map<String, Double>>()   //  一時保存データマップ
        val titleList = getTitleList(colTitle)
        for (dataList in mDataList) {
            if (dataList[0].compareTo(mDataList[0][0]) == 0)    //  タイトル行をスキップ
                continue
//            Log.d(TAG,"pivotTableSub2: " + dataList[rowTitle] + " " + dataList[colTitle])
            val valMap = mutableMapOf<String, Double>()
            if (dataMap[dataList[rowTitle]].isNullOrEmpty()) {
                //  Mapリストデータに新規追加
                valMap[dataList[colTitle]] = 1.0
                dataMap[dataList[rowTitle]] = valMap
            } else {
                //  データの更新(コピーを作って更新し、Mapリストのデータと入れ替える)
                val valueMap = mapCopyAddData(dataMap[dataList[rowTitle]], dataList[colTitle],1.0)
                dataMap.remove(dataList[rowTitle])
                dataMap[dataList[rowTitle]] = valueMap
            }
        }
        //  抽出したデータをmDataListに戻す
        mDataList.clear()
        mDataList.add(titleList)
        for ((key,value) in dataMap) {
            val dataList = mutableListOf<String>()
            dataList.add(key)
            for (i in 1..titleList.lastIndex)
                dataList.add("")
            for ((k, v) in value) {
                dataList[titleList.indexOf(k)] = v.toString()
            }
            mDataList.add(dataList)
        }
    }

    /**
     * Mapデータの更新(集計)データをコピーしてデータの追加または更新
     * Keyデータが存在すればValueデータを加算、なければ新規追加
     * data         Mapデータ(Mapリスト内のデータ)
     * key          加算または追加するKey名
     * value        加算または追加するデータ値
     * return       更新された新規データ
     */
    private fun mapCopyAddData(datas: Map<String, Double>?, key:String, value: Double): Map<String, Double> {
        val valueMap = mutableMapOf<String, Double>()
        if (!datas.isNullOrEmpty()) {
            for ((k,v) in datas) {
                if (k.compareTo(key) == 0)
                    valueMap[k] = v + value
                else
                    valueMap[k] = v
            }
            if (!valueMap.contains(key))
                valueMap[key] = value
        }
        return valueMap
    }

    /**
     * 指定列のデータリストの作成(重複なし)
     * titleCol     データリストの列番号
     * return       データリスト
     */
    fun getTitleList(titleCol: Int, rowTitle: Int = -1): List<String> {
        val titleList = mutableListOf<String>()
        for (i in 1..mDataList.lastIndex) {
            if (!titleList.contains(mDataList[i][titleCol]))
                titleList.add(mDataList[i][titleCol])
        }
        titleList.sort()
        if (rowTitle <0)
            titleList.add(0, mDataList[0][titleCol])
        else
            titleList.add(0, mDataList[0][rowTitle])
        return titleList
    }

    /**
     * 集計処理 指定列で重複している行同士を加算して一つの行にする
     * 指定列以外の数値以外の列は削除
     * rowTitle     重複削除する列番号
     */
    private fun squeezeTable(rowTitle: Int = 0) {
        Log.d(TAG, "squeezeTable: "+rowTitle)
        if (mDataList.size < 2)
            return
        setDataType()
        val dataMap = mutableMapOf<String, List<String>>()
        for (row in 0..mDataList.lastIndex) {
            //  数値データ列だけの行データを作成
            val data = mutableListOf<String>()
            for (col in mDataList[row].indices) {
                if (col != rowTitle && mDataType[col] == DATATYPE.NUMBER) {
                    data.add(mDataList[row][col])
                }
            }
            if (row == 0 || row == 1) {
                //  タイトル行と最初のデータ行はそのままコピー
                dataMap[mDataList[row][rowTitle]] = data
            } else {
                if (dataMap.containsKey(mDataList[row][rowTitle])) {
                    //  重複あり
                    dataMap[mDataList[row][rowTitle]] = addListData(dataMap[mDataList[row][rowTitle]], data)
                } else {
                    //  重複なし
                    dataMap[mDataList[row][rowTitle]] = data
                }
            }
        }
        mDataList.clear()
        for (data in dataMap) {
            val storData = mutableListOf<String>()
            storData.add(data.key)
            for (para in data.value)
                storData.add(para)
            mDataList.add(storData)
        }
    }

    /**
     * 数値文字列のリストデータ同士を加算して文字列リストとして返す
     *  data1           数値文字列リスト1
     *  data2           数値文字列リスト2
     *  return          加算した数値文字列リスト
     */
    private fun addListData(data1: List<String>?, data2: List<String>?): List<String> {
        val addData = mutableListOf<String>()
        if (data1!= null && data2 != null) {
            for (i in 0..data1.lastIndex) {
                val a = data1[i].toDoubleOrNull()?:0.0
                val b = data2[i].toDoubleOrNull()?:0.0
                addData.add((a + b).toString())
            }
        }
        return addData
    }

    /**
     * ([3:ALL:1]-[3:ALL])/[3:ALL]*100
     * sum(equals([@],0),[1:Aichi]:[48:Yamanashi])
     * repeat(min([@],[result]),[1:Aichi],[1:Aichi]:[48:Yamanashi])
     */
    fun calcExpressData(argExpress: String, title: String) {
        //  タイトル作成
        val titleList = mutableListOf<String>()
        for (colTitle in mDataList[0])
            titleList.add(colTitle)
        titleList.add(if (title.isNotEmpty()) title else "演算結果")

        //  範囲指定関数(sum,repeat..)の変数の抽出
        var express = argExpress
        val argMap = getAreaArgList(express)
        for (arg in argMap) {
            express = express.replace(arg.value, arg.key)
        }

        val calc = KCalc()
        calc.setExpression(express)        //  数式の登録
        val keyList = calc.getArgKey()     //  キーの取得
        val dataList = mutableListOf<List<String>>()
        for (row in 1..mDataList.lastIndex) {
            //  既存データのコピー
            val data = mutableListOf<String>()
            for (col in 0..mDataList[row].lastIndex) {
                data.add(mDataList[row][col])
            }

            //  パラメータ値の設定と数式処理
            for (j in keyList.indices) {
                //  範囲指定関数の処理
                var areaFunc = false
                for (arg in argMap) {
                  if (keyList[j].compareTo(arg.key) == 0) {
                      val v = areaFuncCalc(arg.value, row)
                      calc.setArgvalue(keyList[j], v.toString())
                      areaFunc = true
                  }
                }
                if (areaFunc)
                    continue
                // 引数キーから列データを数式処理に設定
                val (col, rrow) = getColRow(keyList[j])
                //  数式処理のパラメータに値を設定
                if (rrow + row < 0 || mDataList.size <= rrow + row || col < 0)
                    calc.setArgvalue(keyList[j], "0")
                else
                    calc.setArgvalue(keyList[j], mDataList[row + rrow][col].replace(",", ""))
            }
            val v = calc.calculate()
            data.add(v.toString())      //  演算結果追加
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.add(titleList)
        mDataList.addAll(dataList)
    }

    /**
     * 範囲指定関数の抽出しキーワードと式のリストを作成
     *  sum(数式,セルの範囲)             指定した範囲のセルの値を数式で処理して合計を出す
     *  sum(f[@], [col:title:relRow]:[10:title:relRow])
     *  repeat(数式,初期値,セルの範囲)
     *  repeat(min([@],[result]),[1:Aichi],[1:Aichi]:[48:Yamanashi])    指定範囲のセルの値を数式で繰り返し処理
     *      [result]:数式内で使用する変数で初期値と都度の演算結果が入る
     *      初期値:定数(演算結果も含む)またはセルの値
     *      セルの範囲:数式に代入[@]するセルの範囲
     * express      数式
     * argNo        変数Noの初期値 ([n]の初期値)
     * return       範囲指定関数
     */
    private fun getAreaArgList(express: String, argNo: Int=0):Map<String, String> {
        val argList = mutableMapOf<String, String>()
        val calc = KCalc()
        val expList = calc.expressList(express)
        var n = argNo
        for (i in expList.indices) {
            if (0 == expList[i].indexOf("sum(")) {
                //  sum関数をMapに登録く
                val argArray = calc.getFuncArgArray(expList[i])
                if (argArray.size == 2 && !argList.containsValue(expList[i])) {
                    argList["[sum" + n + "]"] = expList[i]
                    n++
                }
            } else if (0 == expList[i].indexOf("repeat(")) {
                //  repeat関数をMapに登録く
                val argArray = calc.getFuncArgArray(expList[i])
                if (argArray.size == 3 && !argList.containsValue(expList[i])) {
                    argList["[repeat" + n + "]"] = expList[i]
                    n++
                }
            } else if (0 <= expList[i].indexOf("(")) {
                //  その他の関数
                val str = calc.getBracketString(expList[i])
                if (str.isNotEmpty()) {
                    val argList2 = getAreaArgList(str, n + 1)
                    for (arg in argList2) {
                        if (!argList.containsKey(arg.key)) {
                            argList[arg.key] = arg.value
                        }
                    }
                    n += argList2.size
                }
            }
        }
        return argList
    }

    /**
     * sum,repeatなどの範囲指定パラメータの数式処理を行う
     *  sum(f[@], [col:title:relRow]:[10:title:relRow])
     *  repeat(f([@],[result]),initVal,[col:title:relRow]:[col:title:relRow])
     *  funcStr     数式
     *  row         対象行
     *  return      処理結果
     */
    private fun areaFuncCalc(funcStr: String, row: Int): Double {
        val calc = KCalc()
        val sp = funcStr.indexOf("(") + 1
        val func = funcStr.substring(0, sp - 1).trim()
        val strList = calc.getFuncArgArray(funcStr)
        val express = strList[0]
        //  関数内の範囲指定引数
        calc.setExpression(express)
        val keyList = calc.getArgKey()
        if (func.compareTo("sum") == 0) {
            //  sum関数の処理
            var sum = 0.0
            var sp = strList[1].indexOf("[")
            var ep = strList[1].indexOf("]", sp)
            val (fcol, frow) = getColRow(strList[1].substring(sp, ep + 1))
            sp = strList[1].indexOf("[", ep)
            ep = strList[1].indexOf("]", sp)
            val (scol, srow) = getColRow(strList[1].substring(sp, ep + 1))
            for (i in frow..srow) {
                for (j in fcol..scol) {
                    for (k in 0..keyList.lastIndex) {
                        if (row + i < 0 || mDataList.size <= row + i)
                            calc.setArgvalue(keyList[k], "0")
                        else
                            calc.setArgvalue(keyList[k], mDataList[row + i][j])
                    }
                    sum += calc.calculate()
                }
            }
            return sum
        } else if (func.compareTo("repeat") == 0) {
            //  repeat関数の処理
            var result = argCalc(strList[1], row)
            var sp = strList[2].indexOf("[")
            var ep = strList[2].indexOf("]", sp)
            val (fcol, frow) = getColRow(strList[2].substring(sp, ep + 1))
            sp = strList[2].indexOf("[", ep)
            ep = strList[2].indexOf("]", sp)
            val (scol, srow) = getColRow(strList[2].substring(sp, ep + 1))
            for (i in frow..srow) {
                for (j in fcol..scol) {
                    for (k in 0..keyList.lastIndex) {
                        if (keyList[k].compareTo("[result]") == 0) {
                            calc.setArgvalue(keyList[k], result.toString())
                        } else {
                            if (row + i < 0 || mDataList.size <= row + i)
                                calc.setArgvalue(keyList[k], "0")
                            else
                                calc.setArgvalue(keyList[k], mDataList[row + i][j])
                        }
                    }
                    result = calc.calculate()
                }
            }
            return result
        }

        return 0.0
    }

    /**
     * パラメータから列と相対行を求める
     * [col:columnTitle:relativeRow]
     * arg                  範囲指定の引数文字列([]を含める)
     * return(row, rrow)    (列,相対行)
     */
    private fun getColRow(arg: String): Pair<Int, Int> {
        val sepNo = arg.indexOf(":")
        val col = arg.substring(1, sepNo).toIntOrNull()?:0 	//	対象列
        val sepNo2 = arg.indexOf(":", sepNo + 1)
        var rrow = 0
        if (0 <= sepNo2)
            rrow = arg.substring(sepNo2 + 1, arg.length - 1).toIntOrNull()?:0	//	相対行
        return Pair(col, rrow)
    }

    /**
     * セル(行列指定)の値を数式処理をする
     * express      数式
     * row          対象行
     * return       計算結果
     */
    private fun argCalc(express: String, row: Int): Double {
        val calc = KCalc()
        calc.setExpression(express)
        val keyList = calc.getArgKey()
        for (k in 0..keyList.lastIndex) {
            if (0 <= keyList[k].indexOf("[")) {
                val (col, rrow) = getColRow(keyList[k])     //  引数のセルの位置を求める
                if (row + rrow < 0 || mDataList.size <= row + rrow)
                    calc.setArgvalue(keyList[k], "0")
                else
                    calc.setArgvalue(keyList[k], mDataList[row + rrow][col])
            }
        }
        return calc.calculate()
    }


    /**
     * 指定列のデータでフィルタリンクする
     * col      フィルタリングする列番号
     * chkList  フィルタリングリスト(trueのデータのみ残す)
     */
    fun filterDataList(col: Int, chkList: List<Boolean>) {
        val titleList = getTitleList(col)
        val chkTitleList = mutableListOf<String>()
        for (i in titleList.indices) {
            if (chkList[i]) {
                chkTitleList.add(titleList[i])
            }
        }
        val dataList = mutableListOf<List<String>>()
        dataList.add(mDataList[0])
        for (data in mDataList) {
            if (chkTitleList.contains(data[col])) {
                dataList.add(data)
                Log.d(TAG,"filterDataList: "+data)
            }
        }

        mDataList = dataList
    }

    /**
     * 縦横変換
     */
    fun transposeData() {
        val dataList = mutableListOf<List<String>>()
        for (col in mDataList[0].indices) {
            val data = mutableListOf<String>()
            for (row in mDataList.indices) {
                data.add(mDataList[row][col])
            }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 日付変換
     * icol     対象列
     * type     変換タイプ
     */
    fun dateConvert(icol: Int, type: Int) {
        val scol =  if (icol < 0) 0 else icol
        val dataList = mutableListOf<List<String>>()
        for (row in mDataList.indices) {
            val data = mutableListOf<String>()
            for (col in mDataList[0].indices) {
                if (scol == col) {
                    if (getDataType(mDataList[row][col]) == DATATYPE.DATE) {
                        data.add(klib.JulianDay2DateYear(
                            klib.dateString2JulianDay(mDataList[row][col]), type))
                    } else {
                        data.add(mDataList[row][col])
                    }
                } else {
                    data.add(mDataList[row][col])
                }
            }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 全角数値を反感変換
     */
    fun zenNum2HanConvert() {
        val dataList = mutableListOf<List<String>>()
        for (row in mDataList.indices) {
            val data = mutableListOf<String>()
            for (col in mDataList[row].indices) {
                data.add(klib.strZne2Han(mDataList[row][col]))
            }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 指定行の結合
     */
    fun rowCombaine() {
        Log.d(TAG,"rowCombaine: "+mDataList.size+" "+mRowSelectList.count { it })
        if (mRowSelectList.count { it } == 0)
            return
        val dataList = mutableListOf<List<String>>()
        var data = mutableListOf<String>()
        for (row in mDataList.indices) {
            if (mRowSelectList[row]) {
                //  選択行
                Log.d(TAG, "rowCombaine: "+row)
                if (0 < row && mRowSelectList[row - 1]) {
                    //  行結合
                    for (col in mDataList[row].indices) {
                        if (data[col].trim().isEmpty()) {
                            data[col] = mDataList[row][col]
                        } else if (mDataList[row][col].trim().isEmpty()) {
                            //  何もしない
                        } else if (getDataType(data[col])==DATATYPE.NUMBER && getDataType(mDataList[row][col])==DATATYPE.NUMBER) {
                            data[col] = (data[col].toDouble() + mDataList[row][col].toDouble()).toString()
                        } else {
                            data[col] += " " + mDataList[row][col].trim()
                        }
                    }
                } else {
                    //  行コピー
                    data = mutableListOf()
                    for (col in mDataList[row].indices) {
                        data.add(mDataList[row][col])
                    }
                }
                if (row < mDataList.lastIndex && !mRowSelectList[row + 1]) {
                    dataList.add(data)
                    data = mutableListOf()
                }
            } else {
                //  非選択行
                for (col in mDataList[row].indices) {
                    data.add(mDataList[row][col])
                }
                dataList.add(data)
                data = mutableListOf()
            }
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 列間削除
     * end < 0 の時はstart列のみ削除
     * end < start の時は 0列からemd列とstart列以降を削除
     * start        削除開始列
     * end          削除終了列
     */
    fun colRemove(istart: Int, iend: Int) {
        val start = if (istart < 0) 0 else istart
        val end = if (iend < 0) start else iend
        val dataList = mutableListOf<List<String>>()
        for (row in mDataList.indices) {
            val data = mutableListOf<String>()
            if (start <= end) {
                if (start == 0) {
                    data.addAll(mDataList[row].slice((end+1)..mDataList[row].lastIndex))
                } else if (end == mDataList[row].lastIndex) {
                    data.addAll(mDataList[row].slice(0 until start))
                } else {
                    data.addAll(mDataList[row].slice(0 until start))
                    data.addAll(mDataList[row].slice((end+1)..mDataList[row].lastIndex))
                }
            } else {
                data.addAll(mDataList[row].slice((end+1) until start))
            }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 列間移動
     * start列からend列をmove列に移動
     * move列 < 0 の時は start列だけを end列に移動
     * start        移動開始列
     * end          移動終了列
     * move         移動先列(0> の時はstartをendに1列だけ移動
     */
    fun colMove(start: Int, iend: Int, imove: Int) {
        val move = if (imove < 0) iend else imove
        val end = if (imove < 0) start else iend
        val dataList = mutableListOf<List<String>>()
        for (row in mDataList.indices) {
            val data = mutableListOf<String>()
                if (end < move) {
                    data.addAll(mDataList[row].slice(0 until start))
                    data.addAll(mDataList[row].slice((end+1) until move))
                    data.addAll(mDataList[row].slice(start..end))
                    data.addAll(mDataList[row].slice(move..mDataList[row].lastIndex))
                } else if (move in 1..start) {
                    data.addAll(mDataList[row].slice(0 until move))
                    data.addAll(mDataList[row].slice(start..end))
                    data.addAll(mDataList[row].slice(move until start))
                    data.addAll(mDataList[row].slice((end+1)..mDataList[row].lastIndex))
                } else if (move == 0) {
                    data.addAll(mDataList[row].slice(start..end))
                    data.addAll(mDataList[row].slice(0 until start))
                    data.addAll(mDataList[row].slice((end+1)..mDataList[row].lastIndex))
                } else {
                    data.addAll(mDataList[row])
                }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 列結合
     * styart列から end列までを結合して1列にする
     * データが文字の時は文字列を結語する
     * データが数値の時は、数値を足し合わせていく
     * start        開始列
     * end          囚虜列
     */
    fun colCombine(start: Int, end: Int) {
        val sp = min(start, end)
        val ep = max(start, end)
        val dataList = mutableListOf<List<String>>()
        for (row in mDataList.indices) {
            val data = mutableListOf<String>()
            for (col in mDataList[row].indices) {
                if (col in (sp + 1)..ep) {
                    if (getDataType(data[data.lastIndex])==DATATYPE.DATE) {
                        //  日付データの結合(/ 区切り)
                        data[data.lastIndex] += "/" + mDataList[row][col].trim()
                    } else if (data[data.lastIndex].trim().isEmpty()) {
                        //  結合元が空文字
                        data[data.lastIndex] = mDataList[row][col]
                    } else if (mDataList[row][col].trim().isEmpty()) {
                        //  結合先が空文字の時は何もしない
                    } else if (getDataType(data[data.lastIndex])==DATATYPE.NUMBER && getDataType(mDataList[row][col])==DATATYPE.NUMBER) {
                        //  数値データの結合は加算
                        data[data.lastIndex] = (data[data.lastIndex].toDouble() + mDataList[row][col].toDouble()).toString()
                    } else {
                        //  文字列は空白をはさんで接続
                        data[data.lastIndex] += " " + mDataList[row][col].trim()
                    }
                } else {
                    data.add(mDataList[row][col])
                }
            }
            dataList.add(data)
        }
        mDataList.clear()
        mDataList.addAll(dataList)
    }

    /**
     * 選択行削除
     */
    fun removeRowData() {
        Log.d(TAG,"removeRowData: "+mDataList.size)
        for (i in mRowSelectList.lastIndex downTo 0) {
            if (mRowSelectList[i]) {
                Log.d(TAG,"removeRowData: "+i)
                mDataList.removeAt(i)
            }
        }
        initRowSelectList(false)
        Log.d(TAG,"removeRowData: "+mDataList.size)
    }

    /**
     * 元データ(String)から実数データリストに変換する
     */
    fun toDoubleList() {
        setDataType()
        mDoubleDataList.clear()
        for (row in 1..mDataList.lastIndex) {
            val data = mutableListOf<Double>()
            for (col in 0..mDataList[row].lastIndex) {
                when (mDataType[col]) {
                    DATATYPE.NUMBER -> {
                        data.add(mDataList[row][col].toDoubleOrNull()?:0.0)
                    }
                    DATATYPE.DATE -> {
                        //  日付はユリウス日に変換
                        data.add(klib.dateString2JulianDay(mDataList[row][col]).toDouble())
                    }
                    DATATYPE.STRING -> {
                        //  文字列は行番号に変換
                        data.add(row.toDouble())
                    }
                    DATATYPE.WEEKDAY ->{
                        //  週番号にする
                        data.add(klib.getWeekNo(mDataList[row][col]).toDouble())
                    }
                    else -> {
                        data.add(0.0)
                    }
                }
            }
            mDoubleDataList.add(data)
        }

        mStartRow = 0
        mEndRow = mDoubleDataList.size - 1
    }

    //  ---------------  数値データ処理  ------------------
    /**
     * 数値データリストを増分値から累積値に変換
     */
    fun accumulateDoubleData() {
        val doubleDataList = stackDoubleDataList()

        mDoubleDataList.clear()
        val accumeData = mutableListOf<Double>()
        for (i in doubleDataList[0].indices)
            accumeData.add(doubleDataList[0][i])
        mDoubleDataList.add(accumeData)
        for (i in 1..doubleDataList.lastIndex) {
            val accumeData = mutableListOf<Double>()
            accumeData.add(doubleDataList[i][0])
            for (j in 1..doubleDataList[i].lastIndex) {
                accumeData.add(doubleDataList[i][j] + mDoubleDataList[i-1][j])
            }
            mDoubleDataList.add(accumeData)
        }
    }

    /**
     * 数値データリストを累積値から増分値に変換
     */
    fun differentialDoubleData() {
        val doubleDataList = stackDoubleDataList()

        mDoubleDataList.clear()
        val differentialData = mutableListOf<Double>()
        for (i in doubleDataList[0].indices)
            differentialData.add(doubleDataList[0][i])
        mDoubleDataList.add(differentialData)
        for (i in 1..doubleDataList.lastIndex) {
            val differentialData = mutableListOf<Double>()
            differentialData.add(doubleDataList[i][0])
            for (j in 1..doubleDataList[i].lastIndex) {
                differentialData.add(doubleDataList[i][j] - doubleDataList[i-1][j])
            }
            mDoubleDataList.add(differentialData)
        }
    }

    /**
     * 移動平均のデータに変換
     * averageDataSize  移動平均のデータの数
     */
    fun smoothingDoubleData(averageDataSize: Int=7) {
        val doubleDataList = stackDoubleDataList()

        val sp = -(averageDataSize / 2)
        val ep = averageDataSize + sp
        val dataList = klib.transpose(doubleDataList)
        val srcList = mutableListOf<List<Double>>()
        for (k in dataList.indices) {
            val aveData = mutableListOf<Double>()
            for (i in dataList[k].indices) {
                var sum = 0.0
                var count = 0
                if (k == 0) {
                    aveData.add(dataList[k][i])
                } else {
                    for (j in sp..ep) {
                        if ((0 <= i + j) && (i + j < dataList[k].size)) {
                            sum += dataList[k][i + j]
                            count++
                        }
                    }
                    aveData.add(sum / count)
                }
            }
            srcList.add(aveData)
        }
        val destList = klib.transpose(srcList)
        mDoubleDataList.clear()
        for (data in destList)
            mDoubleDataList.add(data)
    }

    /**
     * 列を指定してデータをscale倍する
     * scale        データの倍率
     * col          scale倍するデータ列(-1の時は選択列となる)
     */
    fun scaleDoubleData(scale: Double, column: Int = -1) {
        val colNo = if (column < 0) getSelectColNo() else column
        val doubleDataList = stackDoubleDataList()
        val destList = mutableListOf<List<Double>>()
        for (row in doubleDataList.indices) {
            val data = mutableListOf<Double>()
            for (col in doubleDataList[row].indices) {
                if (colNo == col)
                    data.add(doubleDataList[row][col] * scale)
                else
                    data.add(doubleDataList[row][col])
            }
            destList.add(data)
        }
        mDoubleDataList.clear()
        mDoubleDataList.addAll(destList)
    }


    /**
     * 数値データをスタックに格納する
     * return       格納した数値データリスト
     */
    private fun stackDoubleDataList(): List<List<Double>> {
        val doubleDataList = mutableListOf<List<Double>>()
        for (data in mDoubleDataList)
            doubleDataList.add(data)
        mDoubleDataStack.add(doubleDataList)
        return doubleDataList
    }

    /**
     * 数値データリストを一つ前の状態に戻す
     */
    fun recoveryDoubleList() {
        if (mDoubleDataStack.size == 0)
            return

        mDoubleDataList.clear()
        for (data in mDoubleDataStack.last())
            mDoubleDataList.add(data)
        mDoubleDataStack.removeLast()
    }

    /**
     * 行タイトルを取得
     * return       行タイトルリスト
     */
    fun getRowTitleList(): List<String> {
        val rowTitle = mutableListOf<String>()
        for (row in 0.. mDoubleDataList.lastIndex) {
            var title = ""
            val x = mDoubleDataList[row][0]
            when (mDataType[0]) {
                DATATYPE.DATE -> {
                    title = klib.JulianDay2DateString(x.toInt())
                }
                DATATYPE.STRING -> {
                    title = mDataList[x.toInt()][0]
                }
                DATATYPE.NUMBER -> {
                    title = "%,.0f".format(x)
                }
                else -> {
                    title = mDataList[x.toInt()][0]
                }
            }
            rowTitle.add(title)
        }
        return rowTitle
    }

    /**
     * 列タイトルリストの取得(行タイトル列は除く)
     * disp     表示データのみ(デフォルトは全データ)
     * return   列タイトルリスト
     */
    fun getColTitleList(disp: Boolean=false): List<String> {
        if (disp) {
            val colTitleList = mutableListOf<String>()
            for (i in 1..mDataList[0].lastIndex)
                if (mDispList[i])
                    colTitleList.add(mDataList[0][i])
            return colTitleList

        } else {
            return mDataList[0].slice(1..mDataList[0].lastIndex)
        }
    }

    /**
     * 表示データリストを取得する(行タイトル列は除く)
     * return   表示データリスト
     */
    fun getDispList(): List<Boolean> {
        return mDispList.slice(1..mDispList.lastIndex)
    }

    /**
     * 表示するデータの列数を求める
     * return   表示する列の数
     */
    fun getDispDataCount(): Int {
        return mDispList.slice(1..mDispList.lastIndex).count { it }
    }

    /**
     * タイトル名から選択カラムを設定
     * colTitle     選択対象列名
     * allDisp      表示データのみ
     */
    fun setSelectList(colTitle: String, allDisp: Boolean=false) {
        initColSelectList(false)
        for (i in mDataList[0].indices) {
            if ((allDisp || mDispList[i]) && mDataList[0][i].compareTo(colTitle) == 0 )
                mColSelectList[i] = true
        }
    }

    /**
     * 選択カラムの列番号を返す
     * returm       列番工(0> 選択なし)
     */
    fun getSelectColNo(): Int {
        for (i in mColSelectList.indices) {
            if (mColSelectList[i])
                return i
        }
        return -1
    }

    /**
     * 選択した列の色設定する
     * color        色番号
     */
    fun setSelectColor(color: Int) {
        for (i in mColSelectList.indices)
            if (mColSelectList[i])
                mColorList[i] = color
    }

    /**
     * 表示データ列リストを初期化する
     */
    fun initDispList() {
        mDispList.clear()
        for (col in mDataList[0].indices) {
            mDispList.add(true)
        }
    }

    /**
     * カラーリストの初期化(列番を設定)
     */
    fun initColorList() {
        mColorList.clear()
        for (col in mDataList[0].indices) {
            mColorList.add(col)
        }
    }

    /**
     * 列選択リストの初期化
     */
    fun initColSelectList(sel: Boolean=true) {
        mColSelectList.clear()
        for (col in mDataList[0].indices) {
            mColSelectList.add(sel)
        }
    }

    /**
     * 行選択リストを初期化する
     * set      初期化フラグ(初期値 true)
     */
    fun initRowSelectList(sel: Boolean=true) {
        mRowSelectList.clear()
        for (row in mDataList.indices) {
            mRowSelectList.add(sel)
        }
    }

    /**
     * データ領域を求める
     * startRow     開始行(省略時 0)
     * endRow       終了行(省略時　最終行(-1))
     */
    fun getArea(startRow: Int=0, endRow: Int=-1, stackType: Boolean = false): RectD {
        val area = RectD()
        mStartRow = max(0, startRow)
        mEndRow = if (endRow < 0) mDoubleDataList.lastIndex else min(endRow, mDoubleDataList.lastIndex)
//        Log.d(TAG,"getArea: "+mStartRow+" "+mEndRow)
        if (0 < mDoubleDataList.size) {
            val vc = max(0,mDispList.drop(1).indexOf(true)) + 1
            area.left   = mDoubleDataList[mStartRow][0]
            area.right  = mDoubleDataList[mStartRow][0]
            area.top    = mDoubleDataList[mStartRow][vc]
            area.bottom = mDoubleDataList[mStartRow][vc]
            for (row in mStartRow..mEndRow) {
                if (stackType ) {
                    var sum = 0.0
                    for (col in 1..mDoubleDataList[row].lastIndex) {
                        if (mDispList[col])
                            sum += mDoubleDataList[row][col]
                    }
                    area.extension(PointD(mDoubleDataList[row][0], sum))
                } else {
                    for (col in 1..mDoubleDataList[row].lastIndex) {
                        if (mDispList[col])
                            area.extension(PointD(mDoubleDataList[row][0], mDoubleDataList[row][col]))
                    }
                }
            }
        }
        Log.d(TAG,"setArea: "+area.left+" "+area.right)
        mDataArea = area

        return area
    }

    /**
     * 行間の差分(値の差)の最小値を求める
     * col      対象列
     * return   最小差分値
     */
    fun getMinmumRowDistance(col: Int): Double {
        var dis = abs(mDoubleDataList[1][col] - mDoubleDataList[0][col])
        for (i in 2..mDoubleDataList.lastIndex) {
            dis = min(dis, abs(mDoubleDataList[i][col] - mDoubleDataList[i - 1][col]))
        }
        return dis
    }

    /**
     * リストデータのデータの種類を求める
     * 結果はmDataTypeに格納
     */
    private fun setDataType() {
        mDataType.clear()
        for (i in 0..mDataList[0].lastIndex) {
            mDataType.add(DATATYPE.NON)
        }
        for (j in 1..mDataList.lastIndex) {
            var spcCellCOunt = 0
            for (i in 0..mDataList[j].lastIndex) {
                if (mDataList[j][i].isEmpty() && mDataType[i] == DATATYPE.NON) {
                    spcCellCOunt++
                } else {
                    if (mDataType[i] == DATATYPE.NON)
                        mDataType[i] = getDataType(mDataList[j][i])
                }
                if (mDataType[i] == DATATYPE.NON)
                    spcCellCOunt++
            }
            if (spcCellCOunt == 0)
                break
        }
    }

    /**
     * 文字列ののデータの種類を求める
     * data         データ文字列
     * return       文字列の種類
     */
    private fun getDataType(data: String): DATATYPE {
        if (data.isNotEmpty()) {
            if (0 <= klib.getTimeType(data)) {
                return DATATYPE.TIME
            } else if (0 <= klib.getDateStringType(data)) {
                return DATATYPE.DATE
            } else if (0 <= klib.getNumberStringType(data)) {
                return DATATYPE.NUMBER
            } else if (0 <= klib.getWeekDayType(data)) {
                return DATATYPE.WEEKDAY
            } else {
                return DATATYPE.STRING
            }
        }
        return DATATYPE.NON
    }

    /**
     * リストデータの列タイトルをリストデータ返す
     * return       タイトルのリストデータ
     */
    fun getTitleList(): List<String> {
        val titleList =  mutableListOf<String>()
        if (0 < mDataList.size) {
            for (title in mDataList[0])
                titleList.add(title)
        }
        return titleList
    }

    /**
     * CSV形式のリストデータをファイルから読み込んでmDataListに格納する
     * filePath     ファイルのパス
     * encode       エンコード(0:UTF_8, 1:SJIS, 2:EUC-JP)
     */
    fun loadData(filePath: String, encode: Int=0) {
        if (klib.existsFile(filePath)) {
            mDataList.clear()
            klib.mFileEncode = encode
            val dataList = klib.loadCsvData(filePath)
//            Log.d(TAG, "loadData: "+dataList.size+" "+filePath)
            for (data in dataList) {
                if (0 < data.maxByOrNull { it.length }?.length?:0) {
//                    Log.d(TAG, "loadData: "+data.size+" "+data[0])
                    mDataList.add(data)
                }
            }
        }
    }

    /**
     * mDataListをCSV形式でファイルに保存する
     * filePath     ファイルのパス
     */
    fun saveData(filePath:String) {
        Log.d(TAG, "saveData: "+filePath+" "+mDataList.size)
        if (klib.existsFile(filePath))
            klib.removeFile(filePath)
        klib.saveCsvData(filePath, mDataList)
    }
}