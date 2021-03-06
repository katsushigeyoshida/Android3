package jp.co.yoshida.katsushige.mapapp

import jp.co.yoshida.katsushige.mylib.KLib

class WikiUrlList {

    var mUrlTitle = mutableListOf<String>()

    var mUrlList = mutableListOf<String>(
        "Wikipediaの一覧リスト",
//        "https://ja.wikipedia.org/wiki/日本の山一覧",
//        "https://ja.wikipedia.org/wiki/日本百名山",
//        "https://ja.wikipedia.org/wiki/日本二百名山",
//        "https://ja.wikipedia.org/wiki/日本三百名山",
//        "https://ja.wikipedia.org/wiki/花の百名山",
//        "https://ja.wikipedia.org/wiki/新・花の百名山",
//        "https://ja.wikipedia.org/wiki/北海道の百名山",
//        "https://ja.wikipedia.org/wiki/北海道百名山",
//        "https://ja.wikipedia.org/wiki/東北百名山",
//        "https://ja.wikipedia.org/wiki/関東百名山",
//        "https://ja.wikipedia.org/wiki/関西百名山#近畿百名山",
//        "https://ja.wikipedia.org/wiki/中国百名山",
//        "https://ja.wikipedia.org/wiki/四国百名山",
//        "https://ja.wikipedia.org/wiki/九州百名山",
//        "https://ja.wikipedia.org/wiki/しま山100選",
//        "https://ja.wikipedia.org/wiki/うつくしま百名山",
//        "https://ja.wikipedia.org/wiki/ぐんま百名山",
//        "https://ja.wikipedia.org/wiki/山梨百名山",
//        "https://ja.wikipedia.org/wiki/信州百名山",
//        "https://ja.wikipedia.org/wiki/ぎふ百山",
//        "https://ja.wikipedia.org/wiki/神社一覧",
//        "https://ja.wikipedia.org/wiki/日本の寺院一覧",
//        "https://ja.wikipedia.org/wiki/日本百景",
//        "https://ja.wikipedia.org/wiki/平成百景",
//        "https://ja.wikipedia.org/wiki/日本の夜景100選",
//        "https://ja.wikipedia.org/wiki/新東京百景",
//        "https://ja.wikipedia.org/wiki/森林浴の森100選",
//        "https://ja.wikipedia.org/wiki/日本さくら名所100選",
//        "https://ja.wikipedia.org/wiki/日本二十五勝",
//        "https://ja.wikipedia.org/wiki/新日本観光地100選",
//        "https://ja.wikipedia.org/wiki/新日本旅行地100選",
//        "https://ja.wikipedia.org/wiki/日本の特別名勝一覧",
//        "https://ja.wikipedia.org/wiki/日本国指定名勝の一覧",
//        "https://ja.wikipedia.org/wiki/日本の観光地一覧",
//        "https://ja.wikipedia.org/wiki/日本の秘境100選",
//        "https://ja.wikipedia.org/wiki/日本百名峠",
//        "https://ja.wikipedia.org/wiki/日本の渚百選",
//        "https://ja.wikipedia.org/wiki/日本の地質百選",
//        "https://ja.wikipedia.org/wiki/日本の滝百選",
//        "https://ja.wikipedia.org/wiki/日本の湖沼一覧",
//        "https://ja.wikipedia.org/wiki/日本の島の一覧",
//        "https://ja.wikipedia.org/wiki/日本の貝塚一覧",
//        "https://ja.wikipedia.org/wiki/縄文時代の遺跡一覧",
//        "https://ja.wikipedia.org/wiki/弥生時代の遺跡一覧",
//        "https://ja.wikipedia.org/wiki/日本の古墳一覧",
//        "https://ja.wikipedia.org/wiki/日本の大規模古墳一覧",
//        "https://ja.wikipedia.org/wiki/日本の特別史跡一覧",
//        "https://ja.wikipedia.org/wiki/北海道・東北地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/関東地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/近畿地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/中国地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/四国地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/九州・沖縄地方の史跡一覧",
//        "https://ja.wikipedia.org/wiki/日本の歴史公園100選",
//        "https://ja.wikipedia.org/wiki/日本庭園",
//        "https://ja.wikipedia.org/wiki/日本の城一覧",
//        "https://ja.wikipedia.org/wiki/日本の博物館の一覧",
//        "https://ja.wikipedia.org/wiki/日本の植物園一覧",
//        "https://ja.wikipedia.org/wiki/日本の水族館",
//        "https://ja.wikipedia.org/wiki/Category:日本の動物園",
//        "https://ja.wikipedia.org/wiki/美術館の一覧",
//        "https://ja.wikipedia.org/wiki/公開天文台一覧",    //  tr
//        "https://ja.wikipedia.org/wiki/都道府県立自然公園",
//        "https://ja.wikipedia.org/wiki/国営公園",
//        "https://ja.wikipedia.org/wiki/日本の国立公園",
//        "https://ja.wikipedia.org/wiki/国定公園",
//        "https://ja.wikipedia.org/wiki/日本の都市公園100選",
//        "https://ja.wikipedia.org/wiki/Category:札幌市の公園",
//        "https://ja.wikipedia.org/wiki/東京都立公園",
//        "https://ja.wikipedia.org/wiki/Category:大田区の公園",
//        "https://ja.wikipedia.org/wiki/Category:世田谷区の公園",
//        "https://ja.wikipedia.org/wiki/Category:品川区の公園",
//        "https://ja.wikipedia.org/wiki/Category:目黒区の公園",
//        "https://ja.wikipedia.org/wiki/Category:東京都港区の公園",
//        "https://ja.wikipedia.org/wiki/Category:東京都の公園",
//        "https://ja.wikipedia.org/wiki/Category:川崎市の公園",
//        "https://ja.wikipedia.org/wiki/Category:横浜市の公園",
//        "https://ja.wikipedia.org/wiki/Category:神奈川県の公園",
//        "https://ja.wikipedia.org/wiki/Category:北海道の自然景勝地",
//        "https://ja.wikipedia.org/wiki/Category:東京都の自然景勝地",
//        "https://ja.wikipedia.org/wiki/Category:埼玉県の自然景勝地",
//        "https://ja.wikipedia.org/wiki/Category:山梨県の自然景勝地",
//        "https://ja.wikipedia.org/wiki/Category:長野県の自然景勝地",
//        "https://ja.wikipedia.org/wiki/日本の市の面積一覧",
//        "https://ja.wikipedia.org/wiki/日本の温泉地一覧",
//        "https://ja.wikipedia.org/wiki/遊園地",
//        "https://ja.wikipedia.org/wiki/日本の橋一覧",
//        "https://ja.wikipedia.org/wiki/Category:石狩川の橋",
//        "https://ja.wikipedia.org/wiki/Category:多摩川の橋",
//        "https://ja.wikipedia.org/wiki/日本百名橋",
//        "https://ja.wikipedia.org/wiki/ダム湖百選",
//        "https://ja.wikipedia.org/wiki/延長別日本の道路トンネルの一覧",
//        "https://ja.wikipedia.org/wiki/日本の野球場一覧",
//        "https://ja.wikipedia.org/wiki/日本のサッカー競技場一覧",
//        "https://ja.wikipedia.org/wiki/日本のスキー場一覧",
//        "https://ja.wikipedia.org/wiki/優良ホール100選",
//        "https://ja.wikipedia.org/wiki/日本の大規模ホテル一覧",
//        "https://ja.wikipedia.org/wiki/日本の港湾一覧",
//        "https://ja.wikipedia.org/wiki/日本の空港",
//        "https://ja.wikipedia.org/wiki/日本の原子力発電所",
//        "https://ja.wikipedia.org/wiki/日本の地方公共団体一覧",
//        "https://ja.wikipedia.org/wiki/日本の大学一覧",
//        "https://ja.wikipedia.org/wiki/日本の銀行一覧",
//        "https://ja.wikipedia.org/wiki/都道府県#一覧",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_北海道地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_東北地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_関東地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_中部地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_北陸地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_近畿地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_中国地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_四国地方",
//        "https://ja.wikipedia.org/wiki/道の駅一覧_九州地方",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_あ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_い",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_う-え",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_お",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_か",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_き",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_く-け",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_こ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_さ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_し-しも",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_しや-しん",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_す-そ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_た",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_ち-て",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_と",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_な",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_に",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_ぬ-の",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_は",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_ひ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_ふ-ほ",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_ま",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_み",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_む-も",
//        "https://ja.wikipedia.org/wiki/日本の鉄道駅一覧_や-わ行",
    )

    var mUrlListPath = ""                   //  URL一覧リストファイル名
    var klib = KLib()

    /**
     * URLリストをファイルに保存
     */
    fun saveWikiUrlList() {
        klib.saveTextData(mUrlListPath, mUrlList)
    }

    /**
     * URLリストをファイルから読み込む
     */
    fun loadWikiUrlList() {
        var urlList = klib.loadTextData(mUrlListPath)
        if (0 < urlList.count()) {
//            mUrlList.clear()
//            mUrlList.add("Wikipediaの一覧リスト")
            for (url in urlList) {
                if (!mUrlList.contains(url))
                    mUrlList.add(url)
            }
        }
    }

}