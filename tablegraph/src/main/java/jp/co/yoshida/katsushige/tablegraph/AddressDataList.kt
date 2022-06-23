package jp.co.yoshida.katsushige.tablegraph

import jp.co.yoshida.katsushige.mylib.KLib

enum class ENCODE {
    UTF8, SJIS, EUC;
    companion object {
        //  インデックスからインスタンスを取得
        fun fromOrdinal(ordinal: Int): ENCODE {
            return values().first { it.ordinal == ordinal }
        }
    }
}

/**
 * オープンデータアドレス管理クラス
 */
class DataAddress(
    var mTitle: String = "",            //  タイトル
    var mAddress: String = "",          //  データのアドレス(URL.ファイルパス)
    var mEncode: ENCODE = ENCODE.SJIS,  //  エンコード
    var mComment: String = "",          //  コメント
    var mReference: String = ""         //  参照先(URL/ファイルパス)
) {
    val TAG = "DataAddress"

    //  データタイトルリスト
    companion object {
        val mDataTitle = listOf(
            "Title", "Address", "Encode", "Comment", "Reference"
        )
    }

    /**
     * セカンドコンストラクタ
     * data     オープンデータアドレス
     */
    constructor(data: DataAddress):
        this(data.mTitle, data.mAddress, data.mEncode, data.mComment, data.mReference) {

    }

    /**
     * データの取得
     * return       文字列リスト
     */
    fun getDataList(): List<String> {
        val dataList = listOf(mTitle, mAddress, mEncode.toString(), mComment, mReference)
        return dataList
    }

    /**
     * データの設定
     * data     データリスト
     */
    fun setDataList(data: List<String>) {
        if (data.size < 5)
            return
        mTitle = data[0]
        mAddress = data[1]
        mEncode = ENCODE.valueOf(data[2])
        mComment = data[3]
        mReference = data[4]
    }
}

/**
 * オープンデータアドレス管理リストクラス
 */
class AddressDataList {
    val TAG = "AddressDataList"

    //  オープンデータアドレスリスト
    var mDataList = mutableListOf<DataAddress>(
        DataAddress(
            "厚生労働省新型コロナ__新規陽性者数の推移（日別）",
            "https://covid19.mhlw.go.jp/public/opendata/newly_confirmed_cases_daily.csv",
            ENCODE.UTF8,
            "厚生労働省データ",
            "https://www.mhlw.go.jp/stf/covid-19/open-data.html"
        ),
        DataAddress(
            "厚生労働省新型コロナ__死亡者数（累積）",
            "https://covid19.mhlw.go.jp/public/opendata/deaths_cumulative_daily.csv",
            ENCODE.UTF8,
            "厚生労働省データ",
            "https://www.mhlw.go.jp/stf/covid-19/open-data.html"
        ),
        DataAddress(
            "新型コロナワクチン接種_接種日別接種回数サマリ",
            "https://vrs-data.cio.go.jp/vaccination/opendata/latest/summary_by_date.csv",
            ENCODE.UTF8,
            "全国のワクチン接種時系列データ",
            "https://cio.go.jp/c19vaccine_dashboard"
        ),
        DataAddress(
            "Data on COVID-19 (coronavirus) by Our World in Data",
            "https://covid.ourworldindata.org/data/owid-covid-data.csv",
            ENCODE.UTF8,
            "オックスフォード大学のOur World Data(新型コロナ(COVID-19)の感染者・死者・検査・ワクチン接種数)",
            "https://github.com/owid/covid-19-data/tree/master/public/data"
        ),
        DataAddress(
            "日本の超過および仮称死亡数ダッシュボード",
            "https://exdeaths-japan.org/data/Observed.csv",
            ENCODE.UTF8,
            "",
            "https://exdeaths-japan.org/graph/weekly"
        ),
        DataAddress(
            "人口動態統計 年次推移（東京都全体）死因別の死亡数・死亡率（平成14年～令和元年）",
            "https://www.fukushihoken.metro.tokyo.lg.jp/kiban/chosa_tokei/jinkodotaitokei/tokyotozentai.files/shiin.csv",
            ENCODE.SJIS,
            "東人口動態統計 年次推移（東京都全体）",
            "https://www.fukushihoken.metro.tokyo.lg.jp/kiban/chosa_tokei/jinkodotaitokei/tokyotozentai.html"
        ),
        DataAddress(
            "人口動態統計 年次推移（東京都全体）出生数・死亡数・死産数・婚姻数・離婚数・合計特殊出生率・平均初婚年齢",
            "https://www.fukushihoken.metro.tokyo.lg.jp/kiban/chosa_tokei/jinkodotaitokei/tokyotozentai.files/tokyo_suii.csv",
            ENCODE.SJIS,
            "人口動態統計 年次推移（東京都全体）",
            "https://www.fukushihoken.metro.tokyo.lg.jp/kiban/chosa_tokei/jinkodotaitokei/tokyotozentai.html"
        ),
        DataAddress(
            "過去の気象データ・ダウンロード",
            "",
            ENCODE.UTF8,
            "参照先でデータをダウンロードし、そのデータを読み込む",
            "https://www.data.jma.go.jp/gmd/risk/obsdl/index.php"
        ),
        DataAddress(
            "東京都新型コロナ",
            "https://stopcovid19.metro.tokyo.lg.jp/data/130001_tokyo_covid19_patients.csv",
            ENCODE.UTF8,
            "都内の最新感染動向",
            "https://stopcovid19.metro.tokyo.lg.jp/"
        ),
        DataAddress(
            "北海道新型コロナ",
            "https://www.harp.lg.jp/opendata/dataset/1369/resource/2853/covid19_data.csv",
            ENCODE.SJIS,
            "新型コロナウイルス感染症対策に関するオープンデータ項目定義書（Code for Japan）」に沿った形で情報",
            "https://www.harp.lg.jp/opendata/dataset/1369.html"
        )
    )

    var klib = KLib()

    /**
     * データの追加
     * data     アドレスデータ
     */
    fun add(data: DataAddress) {
        val n = searchTitle(data)
        if (0 <= n)
            mDataList.removeAt(n)
        mDataList.add(data)
    }

    /**
     * データの取得
     * return       タイトルデータリスト
     */
    fun getTitleList(): List<String> {
        var titleList = mutableListOf<String>()
        for (data in mDataList)
            titleList.add(data.mTitle)
        return titleList
    }

    /**
     * タイトルからアドレスデータを検索取得
     * title        タイトル
     * return       アドレスデータ
     */
    fun getTitleData(title: String): DataAddress {
        val n = searchTitle(title)
        if (0 <= n)
            return mDataList[n]
        else
            return DataAddress()
    }

    /**
     * アドレスデータのタイトルを検索
     * data     アドレスデータ
     * return   検索位置
     */
    fun searchTitle(data: DataAddress): Int {
        return searchTitle(data.mTitle)
    }

    /**
     * データリストからタイトルを検索
     * data     タイトル
     * return   検索位置
     */
    fun searchTitle(title: String): Int {
        for (i in mDataList.indices) {
            if (mDataList[i].mTitle.compareTo(title) == 0)
                return i
        }
        return -1
    }

    /**
     * アドレスデータの存在確認
     * dataAddress      アドレスデータ
     * return           true/false
     */
    fun continus(dataAddress: DataAddress): Boolean {
        for (data in mDataList) {
            if (data.mTitle.compareTo(dataAddress.mTitle) == 0)
                return true
        }
        return false
    }

    /**
     * アドレスデータリストの保存
     * filePath     保存ファイル名
     */
    fun saveData(filePath: String) {
        var dataList = mutableListOf<List<String>>()
        for (data in mDataList) {
            dataList.add(data.getDataList())
        }
        klib.mFileEncode = 0
        klib.saveCsvData(filePath, DataAddress.mDataTitle, dataList)
    }

    /**
     * アドレスデータリストの読込
     * filePath     保存ファイル名
     */
    fun loadData(filePath: String) {
        klib.mFileEncode = 0
        var dataList = klib.loadCsvData(filePath, DataAddress.mDataTitle)
        for (data in dataList) {
            if (4 < data.size) {
                var addresData = DataAddress()
                addresData.setDataList(data)
                add(addresData)
            }
        }
    }
}