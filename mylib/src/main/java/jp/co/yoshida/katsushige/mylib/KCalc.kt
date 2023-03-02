package jp.co.yoshida.katsushige.mylib

import android.util.Log

class KCalc {
    val TAG = "KCalc"

    /// 計算式を評価して計算をおこなう。計算式に対して引数を設定して計算を行うこともできる
    /// 引数なしの計算
    ///     expression(計算式) :　計算式(引数なし)の演算処理を行う
    /// 引数ありの計算
    ///     setExpression(計算式)  :　引数ありの計算式を設定する
    ///     getArgKey()             : 設定された計算式から引数名のリストを取得
    ///     setArgData(key,data)    : 引数のデータ値を設定
    ///     replaceArg()            : 引数ありの計算式にデータ値を設定する
    ///     calculate()             : 引数をデータ値に置き換えた計算式を計算する
    ///  計算後のエラー
    ///     エラーの有無  mError = true
    ///     エラーの内容  mErrorMsg

    var mExpression = ""                            //  計算式一時保管
    var mArgDic = mutableMapOf<String,String>()     //  計算式の引数リスト
    var mError = false                              //  エラーリ有無
    var mErrorMsg = ""                              //  エラーメッセージ

    val mFuncList = listOf(                         //  関数リスト
        "PI 円周率",
        "E 自然対数の底",
        "RAD(x) 度をラジアンに変換する",
        "DEG(x) ラジアンを度に変換する",
        "deg2hour(x) 度を時単位に変換する",
        "hour2deg(x) 時単位を度に変換する",
        "rad2hour(x) ラジアンを時単位に変換する",
        "hour2rad(x) 時単位をラジアンに変換する",
        "mod(x,y) 剰余(割算の余り",
        "pow(x,y) 累乗",
        "max(x,y) 大きい方",
        "min(x,y) 小さい方",
        "combi(n,r) 組合せの数(nCr)",
        "permu(n,r) 順列の数(nPr)",
        "sin(x) 正弦",
        "cos(x) 余弦",
        "tan(x) 正接",
        "asin(x) 逆正接",
        "acos(x) 逆余弦",
        "atan(x) 逆正接",
        "atan2(x,y) 逆正接",
        "sinh(x) 双曲線正弦",
        "cosh(x) 双曲線余弦",
        "tanh(x) 双曲線正接",
        "asinh(x) 逆双曲線正弦",
        "acosh(x) 逆双曲線余弦",
        "atanh(x) 逆双曲線正接",
        "exp(x) eの累乗",
        "ln(x) eを底とする自然対数",
        "log(x) 10を底とする対数",
        "log(x,y) xを底とするyの対数",
        "sqrt(x) 平方根",
        "abs(x) 絶対値",
        "ceil(x) 切上げ(x以上で最小の整数値)",
        "floor(x) 切捨て(小数点以下の数の内最大の整数値)",
        "round(x) 四捨五入(もっとも近い整数値)",
        "trunc(x) 浮動小数点の整数部",
        "sign(x) 符号示す値(1/0/-1)",
        "equals(x,y) 等価判定 x==y ⇒ 1,x!=y ⇒ 0",
        "lt(x,y) 大小判定(less than) x > y ⇒ 1,以外は0",
        "gt(x,y) 大小判定(greater than) x < y ⇒ 1,以外は0",
        "compare(x,y) 大小判定 x > y ⇒ 1,x==y ⇒ 0,x<y ⇒ -1",
        "deg2dms(x) 度(ddd.dddd) → 度分秒(ddd.mmss)",
        "dms2dig(x) 度分秒(ddd.mmss) → 度(ddd.dddd)",
        "hour2hms(x) 時(hh.hhhh) → 時分秒(hh.mmss)",
        "hms2hour(x) 時分秒(hh.mmss) → 時(hh.hhhh)",
        "fact(x) 階乗",
        "fib(x) フィボナッチ数列",
        "gcd(x,y) 最大公約数",
        "lcm(x,y) 最小公倍数",
        "JD(y,m,d) 西暦年月日からユリウス日を求める",
        "MJD(y,m,d) 西暦年月日から準ユリウス日を求める",
        "sum(f([@]),n,k) 級数の和 nからkまでの連続した値を計算式f([@])に入れた合計",
        "sum(f([@]),n1,n2,...nn) 級数の和(引数が4以上) n1からnnまで入れた値を計算式f([@])に入れた合計",
        "product(f([@]),n,k) 級数の積 nからkまでの連続した値を計算式f([@])に入れた積",
        "product(f([@]),n1,n2,...nn) 級数の積(引数が4以上) nからkまでの連続した値を計算式f([@])に入れた積",
        "repeat(f([@],[%]),i,n,k) 計算式の[@]にnからkまで入れて繰返す,[%]に計算結果が入る,iは[%]の初期値"
    )

    /**
     * 返り値を取得するための構造体
     */
    data class ExpressResult (
        var result: Boolean = false,    //  演算OK
        var n: Int = 0,                 //  計算リストの位置
        var x: Double = 0.0             //  演算結果
    )

    /**
     * 設定された計算式で計算する
     * 引数を数値に置き換えて計算式を実行
     * return       計算結果
     */
    fun calculate():Double {
        var express = replaceArg()
        return expression(express)
    }

    /**
     * 計算式を入れて引数を設定する
     * str          計算式
     * return       引数の数
     */
    fun setExpression(str: String): Int {
        mExpression = str
        return argKeySet(mExpression)
    }

    /**
     * 計算式から引数([]内)を取り出しHashTableに保存
     * str      計算式
     * return   引数の数
     */
    fun argKeySet(str: String): Int {
        var buf = ""
        var i = 0
        mArgDic.clear()
        while (i < str.length) {
            if (str[i] == '[') {
                buf += str[i]
            } else if (str[i] == ']') {
                buf += str[i]
                if (!mArgDic.containsKey(buf)) {
                    mArgDic[buf] = ""
                }
                buf = ""
            } else {
                if (0 < buf.length)
                    buf += str[i]
            }
            i++;
        }
        return mArgDic.size
    }

    /**
     * 引数を数値に置き換えた式を作成する
     * return      引数を置き換えた計算式
     */
    fun replaceArg(): String {
        var express = mExpression
        for (entry in mArgDic) {
            if (0 < entry.value.length)
                express = express.replace(entry.key, entry.value);
        }
        return express;
    }

    /**
     * 計算式の引数にデータを設定する
     * key       引数名
     * value     引数データ
     */
    fun setArgvalue(key: String, value: String) {
        mArgDic[key] = value
    }

    /**
     * 計算式の引数を文字列の配列で取得
     * return      引数の文字配列
     */
    fun getArgKey(): List<String> {
        var keys = mutableListOf<String>()
        for ((key, value) in mArgDic) {
            keys.add(key)
        }
        return  keys;
    }

    /**
     * 計算式の評価(四則演算)
     * 文字列を数値と演算子と括弧内文字列に分解してLISTを作る
     *  例: 1+23*4+sin(1.57)+(1+2)*5
     *       →  1,+,23,*,sin(1.57),+,(1+2),*,5
     * str          計算式
     * return       計算結果
     */
    fun expression(inStr: String): Double {
        mError = false
        var result = 0.0
        var str = inStr.replace(" ","")    //  空白除去
        try {
            //  文字列を数値と演算子、括弧内の分解リストを作成
            var expList = expressList(str)
            //  分解リストを順次計算していく
            var x = 0.0
            var ope = ""
            var i = 0
            while (i < expList.size) {
                var success = true
                if (expList[i][0] == '(') {
                    //  括弧内を計算
                    x = expression(expList[i].substring(1, expList[i].length - 1))
                } else if (expList[i][0].isLetter()) {
                    //  単項演算子の計算
                    x = monadicExpression(expList[i])
                } else {
                    //  数値の判定、数値であればxに返す
                    success = doubleParse(expList[i])
                    if (success)
                        x = expList[i].toDouble()
                }
//                Log.d(TAG,"expression:"+i+" "+expList.get(i)+" "+success+" "+x+" "+ope);
                //  数値の場合、前の演算子で計算する
                if (success) {
                    Log.d(TAG,"expression: "+i+" "+result+" "+ope+" "+x)
                    if (ope.compareTo("+") == 0) {          //  加算
                        var res = express2(i, x, expList)   //  剰余が先にあれば計算しておく
                        i = res.n
                        result += res.x
                    } else if (ope.compareTo("-") == 0) {   //  減算
                        var res = express2(i, x, expList)   //  剰余が先にあれば計算しておく
                        i = res.n
                        result -= res.x
                    } else if (ope.compareTo("*") == 0) {   //  乗算
                        var res = express3(i, x, expList)   //  べき乗が先にあれば計算しておく
                        i = res.n
                        result *= res.x
                    } else if (ope.compareTo("/") == 0) {   //  除算
                        var res = express3(i, x, expList)   //  べき乗が先にあれば計算しておく
                        i = res.n
                        if (x == 0.0) {
                            mError = true
                            mErrorMsg = "０割り"
                            return -1.0
                        }
                        result /= res.x
                    } else if (ope.compareTo("%") == 0) {   //  剰余
                        var res = express3(i, x, expList)   //  べき乗が先にあれば計算しておく
                        i = res.n
                        if (x == 0.0) {
                            mError = true
                            mErrorMsg = "０割り"
                            return -1.0
                        }
                        result %= res.x
                    } else if (ope.compareTo("^") == 0) {   //  累乗
                        result = Math.pow(result, x)
                    } else {
                        if (0 <i) {
                            mError = true
                            mErrorMsg = "未演算子"
                        } else
                            result = x
                    }
                    ope = ""
                } else {
                    ope = expList[i]
                }
                if (i < 0)
                    return -1.0
                i++
            }
        } catch (e: Exception) {
            mError = true
            mErrorMsg = e.toString()
        }
        return result
    }

    /**
     * 加減乗除の内優先度の高いものを先に計算する(乗除、剰余、)
     * i            計算式の位置(計算リストの位置)
     * x            式の第１項
     * expList      計算式リスト
     * return       計算結果(結果の値と数値の可否判定)
     */
    private fun express2(i: Int, x: Double, expList: List<String>): ExpressResult {
        var res = ExpressResult()
        res.x = x
        res.n = i
        if (res.n + 2 < expList.size) {
            var y = expression(expList[res.n])
            while (res.n + 2 < expList.size) {
                var ope = expList[res.n + 1]
                var z = expression(expList[res.n + 2])
                Log.d(TAG,"express2: "+res.n+" "+y+" "+ope+" "+z)
                if (ope.compareTo("*") == 0) {        //  掛け算
                    res = express3(res.n, z, expList)
                    res.x = y * res.x
                } else if (ope.compareTo("/") == 0) { // 割り算
                    res = express3(res.n, z, expList)
                    if (res.x == 0.0) {
                        mError = true
                        mErrorMsg = "０割り"
                        return res
                    }
                    res.x = y / res.x
                } else if (ope.compareTo("%") == 0) { //剰余
                    res = express3(res.n, z, expList)
                    if (res.x == 0.0) {
                        mError = true
                        mErrorMsg = "０割り"
                        return res;
                    }
                    res.x = y % res.x
                } else if (ope.compareTo("^") == 0) { //  累乗
                    res.x = Math.pow(y, z);
                } else {
                    break
                }
                y = res.x
                res.n += 2
            }
        }
        res.result = true

        return res
    }

    /***
     * 後ろのべき乗のみを優先して計算
     * 2^3^4 は 2^(3^4) と同じ
     * i            計算式の位置(計算リストの位置)
     * x            式の第１項
     * expList      計算式リスト
     * return       計算結果(結果の値と数値の可否判定)
     */
    private fun express3(i: Int, x: Double, expList: List<String>): ExpressResult {
        var res = ExpressResult()
        res.x = x
        res.n = i
        if (res.n + 2 < expList.size) {
            var y = expression(expList[res.n])
            while (res.n + 2 < expList.size) {
                var ope = expList[res.n + 1]
                var z = expression(expList[res.n + 2])
                Log.d(TAG,"express3: "+res.n+" "+y+" "+ope+" "+z)
                if (ope.compareTo("^") == 0) { //  累乗
                    res.x = Math.pow(y, z)
                } else {
                    break
                }
                y = res.x
                res.n += 2
            }
        }
        res.result = true

        return res
    }

    /**
     * 単項演算子の計算処理を行う
     * str          単項演算子の計算式
     * return       計算結果
     */
    private fun monadicExpression(str: String): Double {
        // System.out.println("monadic:"+str+" "+str.indexOf('(')+" "+str.length());
        var result = 0.0
        if (str.indexOf('(') < 0) {
            if (str.compareTo("PI")==0) {                   //  円周率
                result = Math.PI
            } else if (str.compareTo("E")==0) {             //  自然対数の底e
                result = Math.E
            } else {
                mError = true
                mErrorMsg = "未サポート定数 " + str
            }
            return result
        }
        var ope = str.substring(0,str.indexOf('('))
        var data = str.substring(str.indexOf('(')+1, str.length-1)
        // System.out.println(ope+" "+data);
        var datas = stringSeperate(data)
        if (1 == datas.size) {
            //  引数が1個の単項演算子
            var x = expression(datas[0])
            if (ope.compareTo("RAD") == 0) {                //  degree→radian
                result = x * Math.PI / 180.0
            } else if (ope.compareTo("DEG") == 0) {         //  radian→degree
                result = x * 180.0 / Math.PI
            } else if (ope.compareTo("deg2hour") == 0) {//  度 → 時
                result = deg2hour(x)
            } else if (ope.compareTo("hour2deg") == 0) {//  時 →度
                result = hour2deg(x)
            } else if (ope.compareTo("rad2hour") == 0) {//  ラジアン → 時
                result = rad2hour(x)
            } else if (ope.compareTo("hour2rad") == 0) {//  時 → ラジアン
                result = hour2rad(x)
            } else if (ope.compareTo("deg2dms") == 0) {     //  度 → 度分秒
                result = deg2dms(x)
            } else if (ope.compareTo("dms2deg") == 0) {     //  度分秒 → 度
                result = dms2deg(x)
            } else if (ope.compareTo("hour2hms") == 0) { //  時 → 時分秒
                result = hour2hms(x)
            } else if (ope.compareTo("hms2hour") == 0) { //  時分秒 → 時
                result = hms2hour(x)
            } else if (ope.compareTo("fact") == 0) {        //  階乗
                result = factorial(x.toInt())
            } else if (ope.compareTo("fib") == 0) {         //  フィボナッチ数列
                result = fibonacci(x.toInt())
            } else if (ope.compareTo("sin") == 0) {         //  正弦
                result = Math.sin(x)
            } else if (ope.compareTo("cos") == 0) {         //  余弦
                result = Math.cos(x)
            } else if (ope.compareTo("tan") == 0) {         //  正接
                result = Math.tan(x)
            } else if (ope.compareTo("asin") == 0) {        //  逆正弦
                result = Math.asin(x)
            } else if (ope.compareTo("acos") == 0) {        //  逆余弦
                result = Math.acos(x)
            } else if (ope.compareTo("atan") == 0) {        //  逆正接
                result = Math.atan(x)
            } else if (ope.compareTo("sinh") == 0) {        //  双曲線正弦
                result = Math.sinh(x)
            } else if (ope.compareTo("cosh") == 0) {        //  双曲線余弦
                result = Math.cosh(x)
            } else if (ope.compareTo("tanh") == 0) {        //  双曲線正接
                result = Math.tanh(x)
            } else if (ope.compareTo("asinh") == 0) {       //  逆双曲線正弦
                result = asinh(x)
            } else if (ope.compareTo("acosh") == 0) {       //  逆双曲線余弦
                result = acosh(x)
            } else if (ope.compareTo("atanh") == 0) {       //  逆双曲線正接
                result = atanh(x)
            } else if (ope.compareTo("exp") == 0) {         //  eの累乗値
                result = Math.exp(x)
            } else if (ope.compareTo("ln") == 0) {          //  eを底とする自然対数
                result = Math.log(x)
            } else if (ope.compareTo("log") == 0) {         //  10を底とする対数
                result = Math.log10(x)
            } else if (ope.compareTo("sqrt") == 0) {        //  平方根
                result = Math.sqrt(x)
            } else if (ope.compareTo("abs") == 0) {         //  絶対値
                result = Math.abs(x)
            } else if (ope.compareTo("ceil") == 0) {        //  (切上げ)最小の整数値
                result = Math.ceil(x)
            } else if (ope.compareTo("floor") == 0) {       //  (切捨て)小数点以下の数の内最大の整数値
                result = Math.floor(x)
            } else if (ope.compareTo("round") == 0) {       //  (四捨五入)最も近い整数値に丸める
                result = Math.round(x).toDouble()
            } else if (ope.compareTo("rint") == 0) {        //  浮動小数点の整数部を返す
                result = Math.rint(x)
            } else if (ope.compareTo("sign") == 0) {        //  符号を示す値を返す
                result = Math.signum(x)
            } else {
                mError = true
                mErrorMsg = "未サポート関数 " + ope
            }
        } else if (2 == datas.size) {
            //  引数が2個の単項演算子
            var x = expression(datas[0])
            var y = expression(datas[1])
            if (ope.compareTo("pow") == 0) {                //  累乗
                result = Math.pow(x, y)
            } else if (ope.compareTo("mod") == 0) {         //  剰余
                result = x % y
            } else if (ope.compareTo("atan2") == 0) {       //  逆正接
                result = Math.atan2(x, y)
            } else if (ope.compareTo("log") == 0) {         //  指定した底の対数
                result = Math.log(y) / Math.log(x)
            } else if (ope.compareTo("max") == 0) {         //  大きい方の値を返す
                result = Math.max(x, y)
            } else if (ope.compareTo("min") == 0) {         //  小さい方の値を返す
                result = Math.min(x, y)
            } else if (ope.compareTo("gcd") == 0) {         //  最大公約数
                result = Gcd(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("lcm") == 0) {         //  最小公倍数
                result = Lcm(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("combi") == 0) {       //  組合せの数
                result = combination(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("permu") == 0) {       //  順列の数
                result = permutation(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("equals") == 0) {      //  比較  x==y ⇒ 1 x!=y ⇒ 0
                result = if (x == y) 1.0 else 0.0
            } else if (ope.compareTo("gt") == 0) {          //  比較  x < y ⇒ 1, x >= y ⇒ 0
                result = if (x < y) 1.0 else 0.0
            } else if (ope.compareTo("lt") == 0) {          //  比較  x > y ⇒ 1, x <= y ⇒ 0
                result = if (x > y) 1.0 else 0.0
            } else if (ope.compareTo("compare") == 0) {     //  比較  x > y ⇒ 1 x == y ⇒ 0 x < y ⇒ -1
                result = if (x > y) 1.0 else if (x < y) -1.0 else 0.0
            } else {
                mError = true
                mErrorMsg = "未サポート関数 " + ope
            }
        } else if (3 == datas.size) {
            //  引数が3個の単項演算子
            var x = expression(datas[1])
            var y = expression(datas[2])
            if (ope.compareTo("sum")==0) {              //  級数の和
                result = sum(datas[0], x.toInt(), y.toInt())
            } else if (ope.compareTo("product")==0) {   //  級数の積
                result = product(datas[0], x.toInt(), y.toInt())
            } else if (ope.compareTo("JD") == 0) {      //  ユリウス日
                result = getJD(expression(datas[0]).toInt(), expression(datas[1]).toInt(), expression(datas[2]).toInt());
            } else if (ope.compareTo("MJD") == 0) {     //  準ユリウス日
                result = getMJD(expression(datas[0]).toInt(), expression(datas[1]).toInt(), expression(datas[2]).toInt());
            } else {
                mError = true
                mErrorMsg = "未サポート関数 " + ope
            }
        } else if (4 == datas.size) {
            //  引数が4個の単項演算子
            if (ope.compareTo("sum") == 0) {            //  級数の和
                result = sum(datas);
            } else if (ope.compareTo("product") == 0) { //  級数の積
                result = product(datas);
            } else if (ope.compareTo("repeat") == 0) {  //  繰り返し処理
                var x = expression(datas[1])
                var y = expression(datas[2])
                var z = expression(datas[3])
                result = repeat(datas[0], x, y.toInt(), z.toInt())
            }
        } else {
            if (ope.compareTo("sum") == 0) {            //  級数の和
                result = sum(datas);
            } else if (ope.compareTo("product") == 0) { //  級数の積
                result = product(datas);
            } else {
                mError = true
                mErrorMsg = "不正引数 "+ope
            }
        }
        return result
    }

    /**
     * 計算式の文字列を数値と演算子と括弧内文字列に分解してLISTを作る
     * str          計算式の文字列
     * return       計算式を分解したList
     */
    fun expressList(str: String): List<String>{
        var expList = mutableListOf<String>()
        var buf = ""
        var i = 0
        while (i < str.length) {
            if (str[i].isDigit() ||
                str[i] =='.' ||
                (i == 0 && str[i] == '-') ||
                (0 < i && str[i-1].isDigit() && (str[i] == 'E' || str[i] == 'e')) ||
                (0 < i && (str[i-1] == 'E' || str[i-1] == 'e') && str[i] == '-')) {
                //  数値(数値には指数表示(2E-5など)も含める
                buf += str[i]
            } else if (str[i] == ' ') {
                //  空白は読み飛ばす
            } else {
                if (0 < buf.length) {
                    //  バッファの文字列をリストに格納
                    expList.add(buf)
                    buf = "";
                }
                if (str[i] == '(') {
                    //  括弧内の文字列を格納(括弧を含む)
                    var n = getBracketSize(str,i)
                    buf = str.substring(i, i + n + 2)
                    expList.add(buf)
                    buf = ""
                    i += n + 1
                } else if (str[i] == '+' || str[i] == '-' || str[i] == '*' ||
                    str[i] == '/' || str[i] == '%' || str[i] =='^') {
                    //  2項演算子を格納
                    expList.add(""+str[i])
                } else if (str[i].isLetter()) {
                    //  定数または単項演算子を格納
                    var n = getMonadicString(str,i)
                    if (0 < n) {
                        expList.add(str.substring(i, i + n))
                        i += n - 1
                    }
                }
            }
            i++
        }
        if (0 < buf.length)
            expList.add(buf)
        return expList
    }

    /**
     *  関数の引数を配列にして取出す
     *  func(equal([@],0), [2:a:-1]:[13:b:0])
     *      ⇒  equal([@],0), [2:a:-1]:[13:b:0]
     * func         関数式
     * return       引数の配列
     */
    fun getFuncArgArray(func: String): List<String> {
        var argList = mutableListOf<String>()
        var sp = func.indexOf("(")
        if (0 <= sp) {
            var ss = getBracketSize(func, 1)
            var arg = func.substring(sp + 1, sp + ss + 1)
            return stringSeperate(arg)
        }
        return argList
    }


    /**
     * 括弧の対を検索しその中の文字数を求める(括弧は含めない)
     * str          括弧付きの計算式の文字列
     * start        括弧の対を求める開始位置
     * return       括弧内の文字数(括弧は含まない)
     */
    fun getBracketSize(str: String, start: Int = 0): Int {
        var bracketCount = 0
        var bracketStart = 0
        for (i in start..str.lastIndex) {
            if (str[i] == '(') {
                bracketCount++
                if (bracketCount == 1)
                    bracketStart = i
            } else if (str[i]== ')') {
                bracketCount--
                if (bracketCount == 0)
                    return i - bracketStart - 1
            }
        }
        if (0 < bracketCount) {
            mError = true
            mErrorMsg = "括弧があっていない"
        }
        return 0;
    }

    /**
     *  文字列の中の括弧内の文字列を抽出する(括弧は含まない)
     * str          文字列
     * start        抽出開始位置
     * return       抽出文字列
     */
    fun getBracketString(str: String, start: Int = 0): String {
        var m = str.indexOf("(", start)
        var n = getBracketSize(str, start)
        if (n == 0)
            return ""
        return str.substring(m + 1, m + n + 1);
    }

    /**
     * 定数または単項演算子の場合のサイズを返す(括弧を含む)
     * str      定数または単項演算子の計算式の文字列
     * start    文字数カウントの開始位置
     * return   文字数(括弧を含む)
     */
    fun getMonadicString(str: String, start: Int): Int {
        var i = start
        while (i < str.length) {
            if (str[i]=='(') {
                var n = getBracketSize(str,i)
                return i - start + n + 2
            } else if (str[i]=='+' || str[i]=='-' || str[i]=='*' ||
                str[i]=='/' || str[i]=='%' || str[i]=='^') {
                return i - start
            }
            i++
        }
        return i - start
    }

    /**
     * 文字列をカンマ(,)で分割する、括弧で囲まれている場合は分割しない(計算式用)
     * str          文字列
     * return       カンマで分解した文字列の配列
     */
    private fun stringSeperate(str: String): List<String> {
        var strList = mutableListOf<String>();
        var i = 0
        var bracketCount = 0
        var buf = ""
        while (i < str.length) {
            if (str[i] == '(') {
                bracketCount++
            } else if (str[i] == ')') {
                bracketCount--
            }
            if (bracketCount == 0 && str[i]== ',') {
                strList.add(buf)
                buf = ""
            } else {
                buf += str[i];
            }
            i++
        }
        if (0 < bracketCount) {
            mError = true
            mErrorMsg = "括弧があっていない"
        }
        if (0 < buf.length)
            strList.add(buf);
        return strList
    }

    /**
     * 文字が数値(実数)にできるかを判定する
     * val          数値の文字列
     * return       数値としての判定
     */
    private fun doubleParse(v: String): Boolean {
        if (v.toDoubleOrNull() == null)
            return false
        else
            return true
    }

    //  ======== 度分秒/時分秒　=======
    /**
     * 度(時)(ddd.dddd)を度分秒(時分秒)表記(ddd.mmss)にする
     * deg          ddd.dddddd
     * return       ddd.mmss
     */
    fun deg2dms(deg: Double):Double {
        var tmp = deg;
        var degree = Math.floor(tmp);
        tmp = (tmp - degree) * 60.0;
        var minutes = Math.floor(tmp);
        tmp = (tmp - minutes) * 60.0;
        return degree + minutes / 100.0 + tmp /10000.0;
    }

    /**
     * 度分秒(時分秒)表記(ddd.mmss)を度(時)(ddd.dddd)にする
     * dms          ddd.mmss
     * return       ddd.ddddd
     */
    fun dms2deg(dms: Double ):Double {
        var deg = Math.floor(dms);
        var tmp = (dms - deg) * 100.0;
        var min = Math.floor(tmp);
        var sec = (tmp - min) * 100.0;
        return deg + min / 60.0 + sec / 3600.0;
    }

    /**
     * 時(hh.hhhh)を時分秒(hh.mmss)に変換する
     * hour     時(hh.hhhh)
     * return   時分秒(hh.mmss)
     */
    fun hour2hms(hour: Double): Double {
        var tmp = hour
        val degree: Double = Math.floor(tmp)
        tmp = (tmp - degree) * 60.0
        val minutes: Double = Math.floor(tmp)
        tmp = (tmp - minutes) * 60.0
        return degree + minutes / 100.0 + tmp / 10000.0
    }

    /**
     * 時分秒(hh.mm.ss)を時(hh.hhhh)に変換する
     * hms      時分秒(hh.mmss)
     * return   時(hh.hhhh)
     */
    fun hms2hour(hms: Double): Double {
        val deg: Double = Math.floor(hms)
        val tmp = (hms - deg) * 100.0
        val min: Double = Math.floor(tmp)
        val sec = (tmp - min) * 100.0
        return deg + min / 60.0 + sec / 3600.0
    }

    /**
     * 度(ddd.dddd)から時(hh.hhhh)に変換
     * deg      度(dddd.dddd)
     * return   時(hh.hhhh)
     */
    fun deg2hour(deg: Double): Double {
        return deg * 24.0 / 360.0
    }

    /**
     * 時(hh.hhhh)から度(ddd.dddd)に変換
     * hour     時(hh.hhhh)
     * return   度(ddd.dddd)
     */
    fun hour2deg(hour: Double): Double {
        return hour * 360.0 / 24.0
    }

    /**
     * ラジアンからd時(hh.hhhhh)に変換
     * rad      ラジアン
     * return   時(hh.hhhh)
     */
    fun rad2hour(rad: Double): Double {
        return rad * 12.0 / Math.PI
    }

    /**
     * 時(hh.hhhh)からラジアンに変換
     * hour     時(hh.hhhh)
     * return   ラジアン
     */
    fun hour2rad(hour: Double): Double {
        return hour * Math.PI / 12.0
    }


    //  ======== 三角関数・指数関数　=======
    /**
     * 	逆双曲関数 sinh^-1 = log(x±√(x^2+1))
     *  x
     * return
     */
    fun asinh(x: Double): Double {
        return Math.log(x+Math.sqrt(x*x+1))
    }

    /**
     * 	逆双曲関数 cosh^-1 = log(x±√(x^2-1))
     * x
     * return
     */
    fun acosh(x: Double): Double {
        return Math.log(x+Math.sqrt(x*x-1))
    }

    /**
     * 	逆双曲関数 tanh^-1 = 1/2log((1+x)/(1-x))
     * x
     * return
     */
    fun atanh(x: Double): Double {
        return Math.log((1+x)/(1-x))/2.0;
    }


    //  ======== 組合せ　=======

    /**	階乗計算 (n!)
     * n
     * return
     */
    fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 1..n)
            result *= i.toDouble();
        return result
    }

    /**	順列(nPr)
     * 	異なる n個のものから r個を選んで並べる順列の総数 nPr を求めます。
     * n
     * r
     * return
     */
    fun permutation(n: Int, r: Int): Int {
        var result = 1;
        for (i in (n-r+1)..n)
            result *= i
        return result
    }

    /**	組合せ(nCr)=n!/(r!*(n-r)!)
     * 	異なる n個のものから r個を選ぶ組み合わせの総数 nCr を求めます。
     * 	nCr = n-1Cr-1 + n-1Cr
     * n
     * r
     * return
     */
    fun combination(n: Int, r: Int): Int {
        if (r==0 || r==n)
            return 1
        return combination(n - 1, r - 1) + combination(n-1, r)
    }

    /**
     * フィボナッチ数列を求める
     *  f(1) = f(2) =1, f(n+2) = f(n) + f(n+1)
     * n
     * return
     */
    fun fibonacci(n: Int): Double {
        if (n <= 2)
            return 1.0
        return fibonacci(n-2) + fibonacci(n-1)
    }

    /**
     * 最小公倍数
     * a
     * b
     * return
     */
    fun Lcm(a: Int, b:Int): Int {
        return a * b / Gcd(a, b)
    }

    /**
     * 最大公約数(ユークリッドの互除法)
     * a
     * b
     * return
     */
    fun Gcd(aa: Int, bb: Int): Int {
        var a = aa
        var b = bb
        if (a < b)
            return Gcd(b, a)
        while (b != 0) {
            var remainder = a % b
            a = b
            b = remainder
        }
        return a
    }

    //  ======== 級数、繰り返し計算　=======

    /**
     * 式(f(x)のxがnからkまでの合計を求める
     * 式は[@]を変数として記述し[@]にnからkまでの1づつ増加する値が入る
     * sum("2*[@]",3,5) ⇒  2*3+2*4+2*5 = 24
     * express      集計に使う式
     * n            開始の変数値
     * k            終了の変数値
     * return       計算結果
     */
    fun sum(express: String, n: Int, k: Int): Double {
        var result = 0.0
        var calc = KCalc()
        calc.setExpression(express)
        for (i in n..k) {
            calc.setArgvalue("[@]", "("+i+")")
            result += calc.calculate()
        }
        return result
    }

    /**
     * 引数の合計を求める
     *     sum(f([@],n1,n2,n3・・・nm)  n1からnmまでを[@]に代入してf([@])の合計を求める
     *     例: sum([@]^2,3,5,10,2) ⇒  3^2+5^2+10^2+2^2 = 138
     * arg      引数　arg[0] : f([@])の計算式
     * return   演算結果
     */
    fun sum(arg: List<String>): Double {
        var result = 0.0
        var calc = KCalc()
        calc.setExpression(arg[0])
        if (arg.count() < 4) {
            var n = expression(arg[1]).toInt()
            var k = expression(arg[2]).toInt()
            if (n > k) {
                var t = n
                n = k
                k = t
            }
            for (i in n..k) {
                calc.setArgvalue("[@]", "(" + i + ")")
                result += calc.calculate()
            }
        } else {
            for (i in 1..arg.lastIndex) {
                calc.setArgvalue("[@]", "(" + arg[i] + ")")
                result += calc.calculate()
            }
        }
        return result;
    }

    /**
     * 式(f(x)のxがnからkまでの積を求める
     * 式は[@]を変数として記述し[@]にnからkまでの1づつ増加する値が入る
     * product("[@]^2",3,5) ⇒  3^2+4^2+5^2 = 3600
     * express      集計に使う式
     * n            開始の変数値
     * k            終了の変数値
     * return       計算結果
     */
    fun product(express: String, n: Int, k: Int): Double {
        var result = 1.0
        var calc = KCalc()
        calc.setExpression(express)
        for (i in n..k) {
            calc.setArgvalue("[@]", "("+i+")")
            result *= calc.calculate()
        }
        return result
    }

    /**
     * 引数の積を求める
     *     product(f([@],n1,n2,n3・・・nm)  n1からnmまでを[@]に代入してf([@])の積を求める
     *     例: product([@]^2,3,5,10,2) ⇒  3^2*5^2*10^2*2^2 = 90,000
     * arg      引数 arg[0] : f([@])の計算式
     * return   計算結果
     */
    fun product(arg: List<String>):Double {
        var result = 1.0
        var calc = KCalc()
        calc.setExpression(arg[0])
        if (arg.count() < 4) {
            var n = expression(arg[1]).toInt()
            var k = expression(arg[2]).toInt()
            if (n > k) {
                var t = n
                n = k
                k = t
            }
            for (i in n..k) {
                calc.setArgvalue("[@]", "(" + i + ")")
                result *= calc.calculate()
            }
        } else {
            for (i in 1..arg.lastIndex) {
                calc.setArgvalue("[@]", "(" + arg[i] + ")")
                result *= calc.calculate()
            }
        }
        return result;
    }


    /**
     *  式 y = f(x,y) を nからkまで繰り返した結果を求める
     *  result = f([@],[%]),初期値,開始値,終了値)
     *  [@] : 初期値から終了値までの値(増分は1
     *  [%] : 前回の計算結果、初回は初期値が入る
     *  repeat([%]*1.02,10000,1,5) → ((((10000*1.02)*1.02)*1.02)*1.02))*1.02 = 11040.808
     * express      数式
     * init         初期値
     * n            開始値
     * k            終了値
     * return         計算結果
     */
    fun repeat(express: String, init: Double, n: Int, k: Int): Double {
        var result = init
        var calc = KCalc()
        calc.setExpression(express)
        for (i in n..k) {
            calc.setArgvalue("[@]", "(" + i + ")")
            calc.setArgvalue("[%]", "(" + result + ")")
            result = calc.calculate()
        }
        return result
    }

    //  ======== その他　=======

    /**
     * ユリウス日の取得 (https://www.dinop.com/vc/getjd.html)
     * 年月日は西暦、時間はUTC
     * nYear    年
     * nMonth   月
     * nDay     日
     * nHour    時
     * nMin     分
     * nSec     秒
     */
    fun getJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int = 0, nMin: Int = 0, nSec: Int = 0):Double {
        //  引数の妥当性はチェックしない
        //  ユリウス日の計算
        var year = nYear
        var month = nMonth
        var day = nDay
        if (month == 1 || month == 2) {
            month += 12
            year--;
        }
        return ((year * 365.25).toInt() + (year / 400).toInt() -
                (year / 100).toInt() + (30.59 * (month - 2)).toInt() + day - 678912 + 2400000.5 +
                nHour.toDouble() / 24 + nMin.toDouble() / (24 * 60) + nSec.toDouble() / (24 * 60 * 60)).toDouble()
    }

    /**
     * 準ユリウス日の取得
     * 年月日はグレゴリオ暦（普通の西暦）、時間はUTCで渡すこと
     * nYear    年
     * nMonth   月
     * nDay     日
     * nHour    時
     * nMin     分
     * nSec     秒
     */
    fun getMJD(nYear: Int, nMonth: Int, nDay: Int, nHour: Int = 0, nMin: Int = 0, nSec: Int = 0): Double {
        val dJD = getJD(nYear, nMonth, nDay, nHour, nMin, nSec)
        return if (dJD == 0.0) 0.0 else dJD - 2400000.5
    }

}