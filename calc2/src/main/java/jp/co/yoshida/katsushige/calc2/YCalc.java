package jp.co.yoshida.katsushige.calc2;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YCalc {
    private static final String TAG = "YCalc";

    private String mExpression;                 //  計算式一時保管
    private HashMap<String,String > mArgDic;    //  計算式の引数リスト
    public boolean mError = false;
    public String mErrorMsg;

    public YCalc() {
        mArgDic = new HashMap<String, String>();
    }

    /**
     * 計算式の文字列で最後尾から単語単位で削除する
     * 単語は数値化か数値以外で判定
     * @param str       計算式文字列
     * @return          削除後の文字列
     */
    public String setCEfunc(String str) {
        String buf = str;
        int n = str.length() - 1;
        if (n < 0)
            return buf;
        if (Character.isDigit(str.charAt(n)) || str.charAt(n)=='.') {
            //  数値だったら数値以外が出るまで戻る
            n--;
            while (0 <= n && (Character.isDigit(str.charAt(n)) || str.charAt(n) == '.'))
                n--;
            buf = str.substring(0, n + 1);
        } else if (Character.isLetter(str.charAt(n))) {
            //  アルファベットだったらアルファベット以外が出るまで戻る
            n--;
            while (0 <= n && (Character.isLetter(str.charAt(n))))
                n--;
            buf = str.substring(0, n + 1);
        } else {
            //  数値とアルファベット以外だっらそれになるまで戻る
            n--;
            while (0 <= n && !Character.isDigit(str.charAt(n)) && str.charAt(n)!='.'
                    && !Character.isLetter(str.charAt(n)))
                n--;
            buf = str.substring(0,n+1);
        }
        return buf;
    }

    /**
     * 計算式の文字列で最後尾の数値に対して符号を反転する
     * @param str       計算式の文字列
     * @return          符号反転後の計算式文字列
     */
    public String setPMfunc(String str) {
        String buf = str;
        int n = str.length() - 1;
        if (n < 0)
            return buf;
        //  最後尾が数値でない場合は処理しない
        if (!Character.isDigit(str.charAt(n)) && str.charAt(n)!='.')
            return buf;
        //  後ろから見て数字以外が出てきたら'-'に置き換えるか反転する
        while (0 <= n) {
            if(!Character.isDigit(str.charAt(n)) && str.charAt(n)!='.') {
                if (str.charAt(n)=='-') {
                    if (0 < n && str.charAt(n-1)!='(')
                        buf = str.substring(0, n) + "+" + str.substring(n + 1);
                    else
                        buf = str.substring(0, n) + str.substring(n + 1);
                } else if (str.charAt(n)=='+') {
                    buf = str.substring(0, n) + "-" + str.substring(n+1);
                } else {
                    buf = str.substring(0, n+1) + "-" + str.substring(n+1);
                }
                break;
            } else if (n==0) {
                if (Character.isDigit(str.charAt(n)))
                    buf = "-" + str;
            }
            n--;
        }
        return buf;
    }

    /**
     * 計算式を入れて引数を設定する
     * @param str   計算式
     * @return      引数の数
     */
    public int setExpression(String str) {
        mExpression = str;
        return argKeySet(mExpression);
    }

    /**
     * 設定された計算式で計算する
     * 引数を数値に置き換えて計算式を実行
     * @return      計算結果
     */
    public double calculate() {
        String express = replaceArg();
        return expression(express);
    }


    /**
     * 計算式から引数([]内)を取り出しHashTableに保存
     * @param str   計算式
     * @return      引数の数
     */
    private int argKeySet(String str) {
        String buf = "";
        int i = 0;
        if (mArgDic == null)
            mArgDic = new HashMap<String, String>();

        mArgDic.clear();
        while (i < str.length()) {
            if (str.charAt(i) == '[') {
                buf += str.charAt(i);
            } else if (str.charAt(i) == ']') {
                buf += str.charAt(i);
                if (!mArgDic.containsKey(buf)) {
                    mArgDic.put(buf, "");
                }
                buf = "";
            } else {
                if (0 < buf.length())
                    buf += str.charAt(i);
            }
            i++;
        }
        return mArgDic.size();
    }

    /**
     * 計算式の引数の数を取得
     * @return  引数の数
     */
    public int argKeySize() {
        return mArgDic.size();
    }

    /**
     * 計算式の引数を文字列の配列で取得
     * @return      引数の文字配列
     */
    public String[] getArgKey() {
        String[] keys = new String[mArgDic.size()];
        int i =0;
        for (String key : mArgDic.keySet()) {
            keys[i++] = key;
        }
        return  keys;
    }

    /**
     * 計算式の引数をリストに登録
     * @param key
     * @return
     */
    public String getArgValue(String key) {
        return mArgDic.get(key);
    }

    /**
     * 計算式の引数にデータを設定する
     * @param key       引数名
     * @param value     引数データ
     */
    public void setArgvalue(String key, String value) {
        mArgDic.put(key, value);
    }

    /**
     * 引数を数値に置き換えた式を作成する
     * @return      引数を置き換えた計算式
     */
    public String replaceArg() {
        String exprrss = mExpression;
        for (HashMap.Entry<String, String> entry  : mArgDic.entrySet()) {
            if (0 < entry.getValue().length())
                exprrss = exprrss.replace(entry.getKey(), entry.getValue());
        }
        return exprrss;
    }

    /**
     * 返り値を取得するための構造体
     */
    private class ExpressResult {
        public boolean result = false;
        public int n = 0;
        public double x = 0d;
    }

    /**
     * 計算式の文字列を数値と演算子と括弧内文字列に分解してLISTを作る
     *
     * @param str       計算式の文字列
     * @return          計算式を分解したList
     */
    public List<String> expressList(String str) {
        List<String> expList = new ArrayList<String>();
        expList.clear();
        String buf ="";
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i)) || str.charAt(i)=='.' ||
                    (i==0 && str.charAt(i)=='-') ||
                    (0 < i && Character.isDigit(str.charAt(i-1)) && (str.charAt(i)=='E' || str.charAt(i)=='e')) ||
                    (0 < i && (str.charAt(i-1)=='E' || str.charAt(i-1)=='e') && str.charAt(i)=='-')) {
                //  数値(数値には指数表示(2E-5など)も含める
                buf += str.charAt(i);
            } else if (str.charAt(i) == ' ') {
                //  空白は読み飛ばす
            } else {
                if (0 < buf.length()) {
                    //  バッファの文字列をリストに格納
                    expList.add(buf);
                    buf = "";
                }
                if (str.charAt(i) == '(') {
                    //  括弧内の文字列を格納(括弧を含む)
                    int n = getBracketSize(str,i);
                    buf = str.substring(i, i + n + 2);
                    expList.add(buf);
                    buf = "";
                    i += n + 1;
                } else if (str.charAt(i)=='+' || str.charAt(i)=='-' || str.charAt(i)=='*' ||
                        str.charAt(i)=='/' || str.charAt(i)=='%' || str.charAt(i)=='^') {
                    //  2項演算子を格納
                    expList.add(""+str.charAt(i));
                } else if (Character.isLetter(str.charAt(i))) {
                    //  定数または単項演算子を格納
                    int n = getMonadicString(str,i);
                    if (0 < n) {
                        expList.add(str.substring(i, i + n));
                        i += n - 1;
                    }
                }
            }
        }
        if (0 < buf.length())
            expList.add(buf);
        return expList;
    }

    /**
     * 括弧の対を検索しその中の文字数を求める(括弧は含めない)
     * @param str       括弧付きの計算式の文字列
     * @param start     括弧の対を求める開始位置
     * @return          括弧内の文字数(括弧は含まない)
     */
    private int getBracketSize(String str, int start) {
        int bracketCount = 0;
        int bracketStart = 0;
        for (int i = start; i < str.length(); i++) {
//            System.out.println(""+i+" "+str.charAt(i)+" "+bracketCount+" "+bracketStart);
            if (str.charAt(i) == '(') {
                bracketCount++;
                if (bracketCount == 1)
                    bracketStart = i;
            } else if (str.charAt(i)== ')') {
                bracketCount--;
                if (bracketCount == 0)
                    return i - bracketStart - 1;
            }
        }
        if (0 < bracketCount) {
            mError = true;
            mErrorMsg = "括弧があっていない";
        }
        return 0;
    }

    /**
     * 定数または単項演算子の場合のサイズを返す(括弧を含む)
     * @param str       定数または単項演算子の計算式の文字列
     * @param start     文字数カウントの開始位置
     * @return          文字数(括弧を含む)
     */
    private int getMonadicString(String str, int start) {
        int i = start;
        while (i < str.length()) {
            if (str.charAt(i)=='(') {
                int n = getBracketSize(str,i);
                return i - start + n + 2;
            } else if (str.charAt(i)=='+' || str.charAt(i)=='-' || str.charAt(i)=='*' ||
                    str.charAt(i)=='/' || str.charAt(i)=='%' || str.charAt(i)=='^') {
                return i - start;
            }
            i++;
        }
        return i - start;
    }

    /**
     * 文字列をカンマ(,)で分割する、括弧で囲まれている場合は分割しない(計算式用)
     * @param str       文字列
     * @return          カンマで分解した文字列の配列
     */
    private String[] stringSeperate(String str) {
        List<String> strList = new ArrayList<String>();
        int i = 0;
        int bracketCount = 0;
        String buf = "";
        while (i < str.length()) {
            if (str.charAt(i) == '(') {
                bracketCount++;
            } else if (str.charAt(i) == ')') {
                bracketCount--;
            }
            if (bracketCount == 0 && str.charAt(i)== ',') {
                strList.add(buf);
                buf = "";
            } else {
                buf += str.charAt(i);
            }
            i++;
        }
        if (0 < bracketCount) {
            mError = true;
            mErrorMsg = "括弧があっていない";
        }
        if (0 < buf.length())
            strList.add(buf);
        String[] strArray = strList.toArray(new String[strList.size()]);
        return strArray;
    }

    /**
     * 単項演算子の計算処理を行う
     * @param str       単項演算子の計算式
     * @return          計算結果
     */
    private double monadicExpression(String str) {
        // System.out.println("monadic:"+str+" "+str.indexOf('(')+" "+str.length());
        double result = 0;
        double x,y,z;
        if (str.indexOf('(') < 0) {
            if (str.compareTo("PI")==0) {                   //  円周率
                result = Math.PI;
            } else if (str.compareTo("E")==0) {             //  自然対数の底e
                result = Math.E;
            } else {
                mError = true;
                mErrorMsg = "未サポート定数 " + str;
            }
            return result;
        }
        String ope = str.substring(0,str.indexOf('('));
        String data = str.substring(str.indexOf('(')+1, str.length()-1);
        // System.out.println(ope+" "+data);
        String[] datas = stringSeperate(data);
        if (1 == datas.length) {
            //  引数が1個の単項演算子
            x = expression(datas[0]);
            if (ope.compareTo("RAD") == 0) {                //  degree→radian
                result = x * Math.PI / 180d;
            } else if (ope.compareTo("DEG") == 0) {         //  radian→degree
                result = x * 180d / Math.PI;
            } else if (ope.compareTo("deg2hour") == 0) {    //  度 → 時
                result = deg2hour(x);
            } else if (ope.compareTo("hour2deg") == 0) {    //  時 →度
                result = hour2deg(x);
            } else if (ope.compareTo("rad2hour") == 0) {    //  ラジアン → 時
                result = rad2hour(x);
            } else if (ope.compareTo("hour2rad") == 0) {    //  時 → ラジアン
                result = hour2rad(x);
            } else if (ope.compareTo("deg2dms") == 0) {     //  度 → 度分秒
                result = deg2dms(x);
            } else if (ope.compareTo("dms2deg") == 0) {     //  度分秒 → 度
                result = dms2deg(x);
            } else if (ope.compareTo("hour2hms") == 0) {    //  時 → 時分秒
                result = hour2hms(x);
            } else if (ope.compareTo("hms2hour") == 0) {    //  時分秒 → 時
                result = hms2hour(x);
            } else if (ope.compareTo("fact") == 0) {        //  階乗
                result = factorial((int) x);
            } else if (ope.compareTo("fib") == 0) {         //  フィボナッチ数列
                result = fibonacci((int) x);
            } else if (ope.compareTo("sin") == 0) {         //  正弦
                result = Math.sin(x);
            } else if (ope.compareTo("cos") == 0) {         //  余弦
                result = Math.cos(x);
            } else if (ope.compareTo("tan") == 0) {         //  正接
                result = Math.tan(x);
            } else if (ope.compareTo("asin") == 0) {        //  逆正弦
                result = Math.asin(x);
            } else if (ope.compareTo("acos") == 0) {        //  逆余弦
                result = Math.acos(x);
            } else if (ope.compareTo("atan") == 0) {        //  逆正接
                result = Math.atan(x);
            } else if (ope.compareTo("sinh") == 0) {        //  双曲線正弦
                result = Math.sinh(x);
            } else if (ope.compareTo("cosh") == 0) {        //  双曲線余弦
                result = Math.cosh(x);
            } else if (ope.compareTo("tanh") == 0) {        //  双曲線正接
                result = Math.tanh(x);
            } else if (ope.compareTo("asinh") == 0) {       //  逆双曲線正弦
                result = asinh(x);
            } else if (ope.compareTo("acosh") == 0) {       //  逆双曲線余弦
                result = acosh(x);
            } else if (ope.compareTo("atanh") == 0) {       //  逆双曲線正接
                result = atanh(x);
            } else if (ope.compareTo("exp") == 0) {         //  eの累乗値
                result = Math.exp(x);
            } else if (ope.compareTo("ln") == 0) {          //  eを底とする自然対数
                result = Math.log(x);
            } else if (ope.compareTo("log") == 0) {         //  10を底とする対数
                result = Math.log10(x);
            } else if (ope.compareTo("sqrt") == 0) {        //  平方根
                result = Math.sqrt(x);
            } else if (ope.compareTo("abs") == 0) {         //  絶対値
                result = Math.abs(x);
            } else if (ope.compareTo("ceil") == 0) {        //  (切上げ)最小の整数値
                result = Math.ceil(x);
            } else if (ope.compareTo("floor") == 0) {       //  (切捨て)小数点以下の数の内最大の整数値
                result = Math.floor(x);
            } else if (ope.compareTo("round") == 0) {       //  (四捨五入)最も近い整数値に丸める
                result = Math.round(x);
            } else if (ope.compareTo("rint") == 0) {        //  浮動小数点の整数部を返す
                result = Math.rint(x);
            } else if (ope.compareTo("sign") == 0) {        //  符号を示す値を返す
                result = Math.signum(x);
            } else {
                mError = true;
                mErrorMsg = "未サポート関数 " + ope;
            }
        } else if (2 == datas.length) {
            //  引数が2個の単項演算子
            x = expression(datas[0]);
            y = expression(datas[1]);
            if (ope.compareTo("pow") == 0) {                //  累乗
                result = Math.pow(x, y);
            } else if (ope.compareTo("mod") == 0) {         //  剰余
                result = x % y;
            } else if (ope.compareTo("atan2") == 0) {       //  逆正接
                result = Math.atan2(x, y);
            } else if (ope.compareTo("log") == 0) {         //  指定した底の対数
                result = Math.log(y) / Math.log(x);
            } else if (ope.compareTo("max") == 0) {         //  大きい方の値を返す
                result = Math.max(x, y);
            } else if (ope.compareTo("min") == 0) {         //  小さい方の値を返す
                result = Math.min(x, y);
            } else if (ope.compareTo("gcd") == 0) {         //  最大公約数
                result = Gcd((int)x, (int)y);
            } else if (ope.compareTo("lcm") == 0) {         //  最小公倍数
                result = Lcm((int)x, (int)y);
            } else if (ope.compareTo("combi") == 0) {       //  組合せの数
                result = combination((int) x, (int) y);
            } else if (ope.compareTo("permu") == 0) {       //  順列の数
                result = permutation((int) x, (int) y);
            } else if (ope.compareTo("equals") == 0) {      //  比較  x==y ⇒ 1 x!=y ⇒ 0
                result = x == y ? 1 : 0;
            } else if (ope.compareTo("gt") == 0) {          //  比較  x < y ⇒ 1, x >= y ⇒ 0
                result = x < y ? 1 : 0;
            } else if (ope.compareTo("lt") == 0) {          //  比較  x > y ⇒ 1, x <= y ⇒ 0
                result = x > y ? 1 : 0;
            } else if (ope.compareTo("compare") == 0) {     //  比較  x > y ⇒ 1 x == y ⇒ 0 x < y ⇒ -1
                result = x > y ? 1 : (x < y ? -1 : 0);
            } else {
                mError = true;
                mErrorMsg = "未サポート関数 " + ope;
            }
        } else if (3 == datas.length) {
            //  引数が3個の単項演算子
            x = expression(datas[1]);
            y = expression(datas[2]);
            if (ope.compareTo("sum")==0) {              //  級数の和
                result = sum(datas[0], (int)x, (int)y);
            } else if (ope.compareTo("product")==0) {   //  級数の積
                result = product(datas[0], (int)x, (int)y);
            } else if (ope.compareTo("JD") == 0) {      //  ユリウス日
                result = getJD((int)expression(datas[0]), (int)expression(datas[1]), (int)expression(datas[2]));
            } else if (ope.compareTo("MJD") == 0) {     //  準ユリウス日
                result = getMJD((int)expression(datas[0]), (int)expression(datas[1]), (int)expression(datas[2]));
            } else {
                mError = true;
                mErrorMsg = "未サポート関数 " + ope;
            }
        } else if (4 == datas.length) {
            //  引数が4個の単項演算子
            if (ope.compareTo("sum") == 0) {            //  級数の和
                result = sum(datas);
            } else if (ope.compareTo("product") == 0) { //  級数の積
                result = product(datas);
            } else if (ope.compareTo("repeat") == 0) {  //  繰り返し処理
                x = expression(datas[1]);
                y = expression(datas[2]);
                z = expression(datas[3]);
                result = repeat(datas[0], x, (int)y, (int)z);
            } else {
                mError = true;
                mErrorMsg = "未サポート関数 " + ope;
            }
        } else {
            if (ope.compareTo("sum") == 0) {            //  級数の和
                result = sum(datas);
            } else if (ope.compareTo("product") == 0) { //  級数の積
                result = product(datas);
            } else {
                mError = true;
                mErrorMsg = "不正引数 "+ope;
            }
        }
        return result;
    }

    /***
     * 後ろのべき乗のみを優先して計算
     * 2^3^4 は 2^(3^4) と同じ
     * @param i         計算式の位置(計算リストの位置)
     * @param x         式の第１項
     * @param expList   計算式リスト
     * @return          計算結果(結果の値と数値の可否判定)
     */
    private ExpressResult express3(int i, double x, List<String> expList) {
        ExpressResult res = new ExpressResult();
        double y = 0;
        double z = 0;
        String ope = "";
        res.n = i;
        if (i + 2 < expList.size()) {
            y = expression(expList.get(i));
            ope = expList.get(i+1);
            z = expression(expList.get(i+2));
            if (ope.compareTo("^")==0) { //  累乗
                res = express3(i+2, z, expList);
                x = Math.pow(y, res.x);
            }
        }
        res.x = x;
        res.result = true;

        return res;
    }

    /**
     * 加減乗除の内優先度の高いものを先に計算する(乗除、剰余、)
     * @param i         計算式の位置(計算リストの位置)
     * @param x         式の第１項
     * @param expList   計算式リスト
     * @return          計算結果(結果の値と数値の可否判定)
     */
    private ExpressResult express2(int i, double x, List<String> expList) {
        ExpressResult res = new ExpressResult();
        double y = 0;
        double z = 0;
        String ope = "";
        res.n = i;
        if (i + 2 < expList.size()) {
            y = expression(expList.get(i));
            while (i + 2 < expList.size()) {
                ope = expList.get(i+1);
                z = expression(expList.get(i+2));
                if (ope.compareTo("*")==0) {        //  掛け算
                    res = express2(i+2, z, expList);
                    x = y * res.x;
                } else if (ope.compareTo("/")==0) { // 割り算
                    res = express2(i+2, z, expList);
                    if (res.x == 0d) {
                        mError = true;
                        mErrorMsg = "０割り";
                        return res;
                    }
                    x = y / res.x;
                } else if (ope.compareTo("%")==0) { //剰余
                    res = express2(i+2, z, expList);
                    if (res.x == 0d) {
                        mError = true;
                        mErrorMsg = "０割り";
                        return res;
                    }
                    x = y % res.x;
                } else if (ope.compareTo("^")==0) { //  累乗
                    res = express3(i+2, z, expList);
                    x = Math.pow(y, res.x);
                } else {
                    break;
                }
                y = x;
                i = res.n;
            }
        }
        res.x = x;
        res.result = true;

        return res;
    }

    /**
     * 計算式の評価(四則演算)
     * @param str       計算式
     * @return          計算結果
     */
    public double expression(String str) {
        mError = false;
        double result = 0;
        str = str.replaceAll(" ","");   //  空白除去
        try {
            List<String> expList;
            //  文字列を数値と演算子、括弧内の分解リストを作成
            expList = expressList(str);
            //  分解リストを順次計算していく
            double x = 0;
            String ope = "";
            int i = 0;
            while (i < expList.size()) {
                ExpressResult res;
                boolean success = true;
                if (expList.get(i).charAt(0) == '(') {
                    //  括弧内を計算
                    x = expression(expList.get(i).substring(1, expList.get(i).length() - 1));
                } else if (Character.isLetter(expList.get(i).charAt(0))) {
                    //  単項演算子の計算
                    x = monadicExpression(expList.get(i));
                } else {
                    //  数値の判定、数値であればxに返す
                    success = doubleParse(expList.get(i));
                    if (success)
                        x = Double.parseDouble(expList.get(i));
                }
//                Log.d(TAG,"expression:"+i+" "+expList.get(i)+" "+success+" "+x+" "+ope);
                //  数値の場合、前の演算子で計算する
                if (success) {
                    if (ope.compareTo("+") == 0) {          //  加算
                        res = express2(i, x, expList);      //  剰余が先にあれば計算しておく
                        i = res.n;
                        result += res.x;
                    } else if (ope.compareTo("-") == 0) {   //  減算
                        res = express2(i, x, expList);      //  剰余が先にあれば計算しておく
                        i = res.n;
                        result -= res.x;
                    } else if (ope.compareTo("*") == 0) {   //  乗算
                        res = express3(i, x, expList);      //  べき乗が先にあれば計算しておく
                        i = res.n;
                        result *= res.x;
                    } else if (ope.compareTo("/") == 0) {   //  除算
                        res = express3(i, x, expList);      //  べき乗が先にあれば計算しておく
                        i = res.n;
                        if (x == 0d) {
                            mError = true;
                            mErrorMsg = "０割り";
                            return -1;
                        }
                        result /= res.x;
                    } else if (ope.compareTo("%") == 0) {   //  剰余
                        res = express3(i, x, expList);      //  べき乗が先にあれば計算しておく
                        i = res.n;
                        if (x == 0d) {
                            mError = true;
                            mErrorMsg = "０割り";
                            return -1;
                        }
                        result %= res.x;
                    } else if (ope.compareTo("^") == 0) {   //  累乗
                        res = express3(i, x, expList);      //  べき乗が先にあれば計算しておく
                        i = res.n;
                        result = Math.pow(result, res.x);
                    } else {
                        if (0 <i) {
                            mError = true;
                            mErrorMsg = "未演算子";
                        } else
                            result = x;
                    }
                    ope = "";
                } else {
                    ope = expList.get(i);
                }
                if (i < 0)
                    return -1;
                i++;
            }
        } catch (Exception e) {
            mError = true;
            mErrorMsg = e.getMessage();
        }
        return result;
    }

    /**
     * 文字が数値(実数)にできるかを判定する
     * @param val       数値の文字列
     * @return          数値としての判定
     */
    private boolean doubleParse(String val) {
        try {
            Double.parseDouble(val);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 度(時)(ddd.dddd)を度分秒(時分秒)表記(ddd.mmss)にする
     * @param deg       ddd.dddddd
     * @return          ddd.mmss
     */
    public double deg2dms(double deg) {
        double tmp = deg;
        double degree = Math.floor(tmp);
        tmp = (tmp - degree) * 60d;
        double minutes = Math.floor(tmp);
        tmp = (tmp - minutes) * 60d;
        return degree + minutes / 100d + tmp /10000d;
    }

    /**
     * 度分秒(時分秒)表記(ddd.mmss)を度(時)(ddd.dddd)にする
     * @param dms       ddd.mmss
     * @return          ddd.ddddd
     */
    public double dms2deg(double dms) {
        double deg = Math.floor(dms);
        double tmp = (dms - deg) * 100d;
        double min = Math.floor(tmp);
        double sec = (tmp - min) * 100d;
        return deg + min / 60d + sec / 3600d;
    }

    /**
     * 時(hh.hhhh)を時分秒表記(hh.mmss)にする
     * @param hour  時(hh.hhhh)
     * @return      時分秒表記(hh.mmss)
     */
    public double hour2hms(double hour) {
        double tmp = hour;
        double degree = Math.floor(tmp);
        tmp = (tmp - degree) * 60d;
        double minutes = Math.floor(tmp);
        tmp = (tmp - minutes) * 60d;
        return degree + minutes / 100d + tmp / 10000d;
    }

    /**
     * 時分秒表記(hh.mmss)を時(hh.hhhh)にする
     * @param hms   時分秒表記(hh.mmss)
     * @return      時(hh.hhhh)
     */
    public double hms2hour(double hms) {
        double deg = Math.floor(hms);
        double tmp = (hms - deg) * 100d;
        double min = Math.floor(tmp);
        double sec = (tmp - min) * 100d;
        return deg + min / 60d + sec / 3600d;
    }

    /**
     * 度(ddd.dddd)から時(hh.hhhh)に変換
     * @param deg   度(ddd.dddd)
     * @return      時(hh.hhhh)
     */
    public double deg2hour(double deg) {
        return deg * 24.0 / 360.0;
    }

    /**
     * 時(hh.hhhh)から度(ddd.dddd)に変換
     * @param hour  時(hh.hhhh)
     * @return      度(ddd.dddd)
     */
    public double hour2deg(double hour) {
        return hour * 360.0 /  24.0;
    }

    /**
     * ラジアンから時(hh.hhhh)に変換
     * @param rad   ラジアン
     * @return      時(hh.hhhh)
     */
    public double rad2hour(double rad) {
        return rad * 12.0 / Math.PI;
    }

    /**
     * 時(hh.hhhh)からラジアンに変換
     * @param hour  時(hh.hhhh)
     * @return      ラジアン
     */
    public double hour2rad(double hour) {
        return hour * Math.PI / 12.0;
    }

    //  ===========  リストデータ処理  ==============

    /***
     * 文字データのXYリストからXデータのリストを作る
     * @param list      文字データのXYリスト
     * @return          数値のXデータリスト
     */
    public ArrayList<Double> getXlist(ArrayList<String> list) {
        ArrayList<Double> xlist = new ArrayList<Double>();
        for (String key : list) {
            String[] text = key.split(",");
            if (0 < text.length) {
                try {
                    xlist.add(Double.valueOf(text[0]));
                } catch (Exception e) {
                    xlist.add(0d);
                }
            } else
                xlist.add(0d);
        }
        return xlist;
    }

    /**
     * 一次元データか二次元データを確認する
     * @param list      データリスト
     * @return          データの次元数
     */
    public int getXYListOrder(ArrayList<String> list) {
        int orderSum = 0;
        for (String key : list) {
            String[] text = key.split(",");
            orderSum += text.length;
        }
        return orderSum / list.size();
    }

    /***
     * 文字データのXYリストからYデータのリストを作る
     * @param list      文字データのXYリスト
     * @return          数値のYデータリスト
     */
    public ArrayList<Double> getYlist(ArrayList<String> list) {
        ArrayList<Double> ylist = new ArrayList<Double>();
        for (String key : list) {
            String[] text = key.split(",");
            if (1 < text.length)
                ylist.add(Double.valueOf(text[1]));
            else
                ylist.add(0d);
        }
        return ylist;
    }

    /**
     * リストデータから最小値を求める
     * @param list      リストデータ
     * @return          最小値
     */
    public double getMinList(ArrayList<Double> list) {
        double min = list.get(0);
        for (int i = 1; i <  list.size(); i++) {
            if (min > list.get(i))
                min = list.get(i);
        }
        return min;
    }

    /**
     * リストデーから最大値をもとめる
     * @param list      リストデータ
     * @return          最大値
     */
    public double getMaxList(ArrayList<Double> list) {
        double max = list.get(0);
        for (int i = 1; i <  list.size(); i++) {
            if (max < list.get(i))
                max = list.get(i);
        }
        return max;
    }

    public int[] getDataMap(ArrayList<Double> list, double min, double max, int divideCount) {
        int[] map = new int[divideCount];
        for (int val : map)
            val = 0;
        double range = max - min;
        double unit = range / divideCount;
        for (double val : list) {
            if (min <= val && val < max) {
                int n = (int)((val - min) / unit);
                map[n]++;
            }
        }
        return map;
    }

    //  ===========  統計処理  ==============

    /**	∑x リストの合計を求める
     *
     * @param list
     * @return
     */
    public double getSumList(ArrayList<Double> list) {
        double sum = 0.;
        for (int i=0; i<list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }

    /**	∑x^2 リストの自乗和(二乗の合計)を求める
     *
     * @param list
     * @return
     */
    public double getSqrSumList(ArrayList<Double> list) {
        double sum = 0.;
        for (int i=0; i<list.size(); i++)
            sum += list.get(i)*list.get(i);
        return sum;
    }

    /**	∑xy X*Yの合計
     *
     * @param Xlist
     * @param Ylist
     * @return
     */
    public double getXYSumList(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        double sum = 0.;
        for (int i=0; i<Xlist.size() && i<Ylist.size(); i++)
            sum += Xlist.get(i)*Ylist.get(i);
        return sum;
    }

    /**	平均
     *
     * @param list
     * @return
     */
    public double getListMean(ArrayList<Double> list) {
        return getSumList(list) / list.size();
    }

    /**	∑(x-xm) 偏差の和
     *
     * @param list
     * @return
     */
    public double getDevSum(ArrayList<Double> list) {
        double mean = getListMean(list);
        double sum = 0.;
        for (int i=0; i<list.size(); i++)
            sum += (list.get(i) - mean);
        return sum;
    }

    /**	∑(x-xm)^2 分散
     *
     * @param list
     * @return
     */
    public double getVarSum(ArrayList<Double> list) {
        double mean = getListMean(list);
        double sum = 0.;
        for (int i=0; i<list.size(); i++)
            sum += (list.get(i) - mean)*(list.get(i) - mean);
        return sum;
    }

    /**	∑(x-xm)(y-ym) 偏差積和
     *
     * @param Xlist
     * @return
     */
    public double getXYSum(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        double xmean = getListMean(Xlist);
        double ymean = getListMean(Ylist);
        double sum = 0.;
        for (int i=0; i<Xlist.size(); i++)
            sum += (Xlist.get(i) - xmean)*(Ylist.get(i) - ymean);
        return sum;
    }

    /***
     * 共分散(Covariance)
     * Cov(x,y) = 1/n * Σ(x-xm)(y-ym)
     * @param Xlist
     * @param Ylist
     * @return
     */
    public double getCovarince(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        return getXYSum(Xlist, Ylist) / Xlist.size();
    }

    /***
     * 相関係数  ρ = σxy / (σx * σy)
     * @param Xlist
     * @param Ylist
     * @return
     */
    public double getCorelation(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        return getCovarince(Xlist, Ylist) / (getStdDev(Xlist) * getStdDev(Ylist));
    }

    /**	標準偏差(standard deviation)
     * 	s^2 = 	∑(x-xm)^2 / n
     * @param list	データリスト
     * @return			標準偏差
     */
    public double getStdDev(ArrayList<Double> list) {
        return Math.sqrt(getVarSum(list)/list.size());
    }

    /**	標準偏差(standard deviation)
     * 	v^2 = 	∑(x-xm)^2 / (n-1)
     * @param list	データリスト
     * @return			標準偏差
     */
    public double getStdDev2(ArrayList<Double> list) {
        return Math.sqrt(getVarSum(list)/(list.size()-1));
    }

    /**	回帰分析の係数の取得  y = ax + b
     *
     * @param Xlist
     * @param Ylist
     * @return
     */
    public double getRegA(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        return getXYSum(Xlist, Ylist)/getVarSum(Xlist);
    }

    /**	回帰分析の係数の取得  y = ax + b
     *
     * @param Xlist
     * @param Ylist
     * @return
     */
    public double getRegB(ArrayList<Double> Xlist, ArrayList<Double> Ylist) {
        return getListMean(Ylist) - getRegA(Xlist, Ylist) * getListMean(Xlist);
    }

    //  ===========  順列組み合せ・級数処理・その他  ==============

    /**	順列(nPr)
     * 	異なる n個のものから r個を選んで並べる順列の総数 nPr を求めます。
     * @param n
     * @param r
     * @return
     */
    public int permutation(int n, int r) {
        Log.d("--AS--", "permutation:"+n+","+r);
        int result = 1;
        for (int i = n-r+1; i<=n; i++)
            result *= i;
        return result;
    }

    /**	組合せ(nCr)=n!/(r!*(n-r)!)
     * 	異なる n個のものから r個を選ぶ組み合わせの総数 nCr を求めます。
     * 	nCr = n-1Cr-1 + n-1Cr
     * @param n
     * @param r
     * @return
     */
    public int combination(int n, int r) {
        if (r==0 || r==n)
            return 1;
        return combination(n - 1, r - 1) + combination(n-1, r);
    }

    /**	階乗計算 (n!)
     *
     * @param n
     * @return
     */
    public double factorial(int n) {
        double result = 1.0;
        for (int i = 1; i <= n; i++)
            result *= (double) i;
        return result;
    }

    /**
     * フィボナッチ数列を求める
     *  f(1) = f(2) =1, f(n+2) = f(n) + f(n+1)
     * @param n
     * @return
     */
    public double fibonacci(int n) {
        if (n <= 2)
            return 1;
        return fibonacci(n-2) + fibonacci(n-1);
    }

    /**
     * 最小公倍数
     * @param a
     * @param b
     * @return
     */
    public int Lcm(int a, int b)
    {
        return a * b / Gcd(a, b);
    }

    /**
     * 最大公約数(ユークリッドの互除法)
     * @param a
     * @param b
     * @return
     */
    public int Gcd(int a, int b)
    {
        if (a < b)
            return Gcd(b, a);
        while (b != 0) {
            int remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    /**
     * xがnからkまで連続した数値の式(f(x)の演算結果の合計を求める
     * 式は[@]を変数として記述し[@]にnからkまでの1づつ増加する値が入る
     * sum("2*[@]",3,5) ⇒  2*3+2*4+2*5 = 24
     * @param express   集計に使う式
     * @param n         開始の変数値
     * @param k         終了の変数値
     * @return          計算結果
     */
    public double sum(String express,int n, int k) {
        double result = 0;
        YCalc calc = new YCalc();
        calc.setExpression(express);
        for (int i = n; i <= k; i++) {
            calc.setArgvalue("[@]", "("+i+")");
            result += calc.calculate();
        }
        return result;
    }

    /**
     * xがnからmまで式(f(x)の演算結果の合計を求める
     *     sum(f([@],n1,n2,n3・・・nm)  n1からnmまでを[@]に代入してf([@])の合計を求める
     *     例: sum([@]^2,3,5,10,2) ⇒  3^2+5^2+10^2+2^2 = 138
     * @param arg   引数 arg[0] : f([@])の計算式
     * @return      演算結果
     */
    public double sum(String[]  arg)
    {
        double result = 0;
        YCalc calc = new YCalc();
        calc.setExpression(arg[0]);
        if (arg.length < 4) {
            int n = (int)expression(arg[1]);
            int k = (int)expression(arg[2]);
            if (n > k) {
                int t = n;
                n = k;
                k = t;
            }
            for (int i = n; i <= k; i++) {
                calc.setArgvalue("[@]", "(" + i + ")");
                result += calc.calculate();
            }
        } else {
            for (int i = 1; i < arg.length; i++) {
                calc.setArgvalue("[@]", "(" + arg[i] + ")");
                result += calc.calculate();
            }
        }
        return result;
    }

    /**
     * 式(f(x)のxがnからkまでの積を求める
     * 式は[@]を変数として記述し[@]にnからkまでの1づつ増加する値が入る
     * product("[@]^2",3,5) ⇒  3^2+4^2+5^2 = 3600
     * @param express   集計に使う式
     * @param n         開始の変数値
     * @param k         終了の変数値
     * @return          計算結果
     */
    public double product(String express,int n, int k) {
        double result = 1;
        YCalc calc = new YCalc();
        calc.setExpression(express);
        for (int i = n; i <= k; i++) {
            calc.setArgvalue("[@]", "("+i+")");
            result *= calc.calculate();
        }
        return result;
    }

    /**
     * xがnからmまで式(f(x)の演算結果の積を求める
     *     product(f([@],n1,n2,n3・・・nm)  n1からnmまでを[@]に代入してf([@])の積を求める
     *     例: product([@]^2,3,5,10,2) ⇒  3^2*5^2*10^2*2^2 = 90,000
     * @param arg   引数 arg[0] : f([@])の計算式
     * @return      演算結果
     */
    public double product(String[] arg) {
        double result = 1;
        YCalc calc = new YCalc();
        calc.setExpression(arg[0]);
        if (arg.length < 4) {
            int n = (int)expression(arg[1]);
            int k = (int)expression(arg[2]);
            if (n > k) {
                int t = n;
                n = k;
                k = t;
            }
            for (int i = n; i <= k; i++) {
                calc.setArgvalue("[@]", "(" + i + ")");
                result *= calc.calculate();
            }
        } else {
            for (int i = 1; i < arg.length; i++) {
                calc.setArgvalue("[@]", "(" + arg[i] + ")");
                result *= calc.calculate();
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
     * @param express   数式
     * @param init      初期値
     * @param n         開始値
     * @param k         終了値
     * @return          計算結果
     */
    public double repeat(String express, double init, int n, int k)
    {
        double result = init;
        YCalc calc = new YCalc();
        calc.setExpression(express);
        for (int i = n; i <= k; i++) {
            calc.setArgvalue("[@]", "(" + i + ")");
            calc.setArgvalue("[%]", "(" + result + ")");
            result = calc.calculate();
        }
        return result;
    }

    /**
     * ユリウス日の取得 (https://www.dinop.com/vc/getjd.html)
     * 年月日は西暦、時間はUTC
     * @param nYear     年
     * @param nMonth    月
     * @param nDay      日
     * @return          ユリウス日
     */
    public double getJD(int nYear, int nMonth, int nDay) {
        return getJD(nYear, nMonth, nDay, 0, 0, 0);
    }

    /**
     * ユリウス日の取得 (https://www.dinop.com/vc/getjd.html)
     * 年月日は西暦、時間はUTC
     * @param nYear     年
     * @param nMonth    月
     * @param nDay      日
     * @param nHour     時
     * @param nMin      分
     * @param nSec      秒
     * @return          ユリウス日
     */
    public double getJD(int nYear, int nMonth, int nDay, int nHour, int nMin, int nSec) {
        //  引数の妥当性はチェックしない
        //  ユリウス日の計算
        if (nMonth == 1 || nMonth == 2) {
            nMonth += 12;
            nYear--;
        }
        return (double)((int)(nYear * 365.25) + (int)(nYear / 400) -
                (int)(nYear / 100) + (int)(30.59 * (nMonth - 2)) + nDay - 678912 + 2400000.5 +
                (double)nHour / 24 + (double)nMin / (24 * 60) + (double)nSec / (24 * 60 * 60));
    }

    /**
     * 準ユリウス日の取得
     * 年月日はグレゴリオ暦（普通の西暦）、時間はUTCで渡すこと
     * @param nYear     年
     * @param nMonth    月
     * @param nDay      日
     * @return          準ユリウス日
     */
    public double getMJD(int nYear, int nMonth, int nDay) {
        return getMJD(nYear, nMonth, nDay, 0, 0, 0);
    }

    /**
     * 準ユリウス日の取得
     * 年月日はグレゴリオ暦（普通の西暦）、時間はUTCで渡すこと
     * @param nYear     年
     * @param nMonth    月
     * @param nDay      日
     * @param nHour     時
     * @param nMin      分
     * @param nSec      秒
     * @return          準ユリウス日
     */
    public double getMJD(int nYear, int nMonth, int nDay, int nHour, int nMin, int nSec)
    {
        double dJD = getJD(nYear, nMonth, nDay, nHour, nMin, nSec);
        if (dJD == 0.0)
            return 0.0;
        else
            return dJD - 2400000.5;
    }


    //  ===========  三角関数・ベース関数その他  ==============

    /**
     * 	逆双曲関数 sinh^-1 = log(x±√(x^2+1))
     * @param x
     * @return
     */
    public double asinh(double x) {
        return Math.log(x+Math.sqrt(x*x+1));
    }

    /**
     * 	逆双曲関数 cosh^-1 = log(x±√(x^2-1))
     * @param x
     * @return
     */
    public double acosh(double x) {
        return Math.log(x+Math.sqrt(x*x-1));
    }

    /**
     * 	逆双曲関数 tanh^-1 = 1/2log((1+x)/(1-x))
     * @param x
     * @return
     */
    public double atanh(double x) {
        return Math.log((1+x)/(1-x))/2.0;
    }

    /**
     * 有効数字以下の数値を四捨五入します。
     * 実数の誤差を丸める場合は有効数字桁数を14とする
     * @param value　            数値
     * @param effectiveDigit　   有効数字桁数
     * @return                   四捨五入された数値
     */
    public double getEfficientRound(double value, int effectiveDigit) {
        if (value==0)
            return 0;
        int valueDigit = (int) Math.rint(Math.log10(Math.abs(value)));		//	桁数
        int roundDigit = effectiveDigit - valueDigit - 1;					//	四捨五入する桁
        double v;
        if (0 < value)
            v = Math.floor(value * Math.pow(10, roundDigit) + 0.5);		//
        else
            v = Math.ceil(value * Math.pow(10, roundDigit) - 0.5);		//
        // System.out.println("getEfficientRound:"+valueDigit+" "+roundDigit+" "+v);
        return v / Math.pow(10, roundDigit);
    }

}