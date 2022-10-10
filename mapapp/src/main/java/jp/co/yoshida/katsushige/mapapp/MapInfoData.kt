package jp.co.yoshida.katsushige.mapapp

import android.util.Log
import jp.co.yoshida.katsushige.mylib.KLib

/// 国土地理院の地図データに関するデータ
/// 参照: https://maps.gsi.go.jp/development/ichiran.html
/// 参考: インタフェース 2019年4月号 (CQ出版)
/// 　　　第1部国土地理院地図＆基礎知識 29P
class MapInfoData {
    val TAG = "MapInfoData"

    val mHelpUrl = "https://maps.gsi.go.jp/development/ichiran.html"    //  地理院タイルのヘルプ
    val mGsiUrl = "https://cyberjapandata.gsi.go.jp/xyz/"   //  国土地理院データ参照先
    val mHelpOsmUrl = "https://openstreetmap.jp/"           // オープンストリートマップの参照先

    val mMapDataFormat = listOf(
        "タイトル", "データＩＤ", "ファイル拡張子", "タイル名", "有効ズームレベル",
        "整備範囲", "概要", "地図データURL", "地図データ提供先名", "地図データ提供先URL",
        "標高データID","BaseMapID", "透過色", "BaseMap上位"
    )

    //  20万分の1日本シームレス地質図V2 Web API 凡例取得サービス
    //  https://gbank.gsj.jp/seamless/v2/api/1.2.1/     2020年6月9日
    var mLegendSeamless_V2 = "https://gbank.gsj.jp/seamless/v2/api/1.2.1/legend.csv"

    //  標高データ(https://maps.gsi.go.jp/development/ichiran.html#dem)
    //  256x256ピクセルの標高タイル画像に相当するデータが256行x256個の標高値を表すカンマ区切りの
    //  数値データとして格納されている。標高値が存在しない画素に相当するところはeが入っている
    val mMapElevatorData = listOf(
        listOf(
            "標高タイルデータ",         //  [0]タイトル
            "dem5a",                    //  [1]データID(DEM5A:Z15,5x5m,精度0.3m)
            "txt",                      //  [2]ファイル拡張子
            "標高数値データ",           //  [3]地図の種類
            "1-15",                       //  [4]有効な最大ズームレベル
            "",                         //  [5]データの整備範囲
            "データソース:基板地図情報数値標高モデル、測量方法:航空レーザー測量、標高点格子間隔:約5m四方", //  [6]データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/dem5a/{z}/{x}/{y}.txt",   //  [7]タイル座標URL
            "国土地理院",                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html#dem",  //  [9]参照先URL
        ),
        listOf(
            "標高タイルデータ(都市周辺など)",//  [0]タイトル
            "dem5b",                    //  [1]データID(DEM5B:Z15,5x5m,精度0.7m)
            "txt",                      //  [2]ファイル拡張子
            "標高数値データ",           //  [3]地図の種類
            "1-15",                       //  [4]有効な最大ズームレベル
            "",                         //  [5]データの整備範囲
            "データソース:基板地図情報数値標高モデル(都市周辺など)、測量方法:写真測量(地上画素寸法20cm)、標高点格子間隔:約5m四方", //  [6]データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/dem5b/{z}/{x}/{y}.txt",   //  [7]
            "国土地理院",                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html#dem",  //  [9]参照先URL
        ),
        listOf(
            "標高タイルデータ(一部の島嶼などなど)",//  タイトル
            "dem5c",                    //  データID(DEM5C:Z15,5x5m,精度1.4m)
            "txt",                      //  ファイル拡張子
            "標高数値データ",           //  地図の種類
            "1-15",                       //  有効な最大ズームレベル
            "",                         //  データの整備範囲
            "データソース:基板地図情報数値標高モデル(一部の島嶼など)、測量方法:写真測量(地上画素寸法40cm)、標高点格子間隔:約5m四方", //  データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/dem5c/{z}/{x}/{y}.txt",
            "国土地理院",                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html#dem",  //  [9]参照先URL
        ),
        listOf(
            "標高タイルデータ(1/2.5万地形等高線)", //  タイトル
            "dem",                      //  データID(DEM10B:Z14,10x10m,精度5m)
            "txt",                      //  ファイル拡張子
            "標高数値データ(DEM10B)",   //  地図の種類
            "1-14",                       //  有効な最大ズームレベル
            "",                         //  データの整備範囲
            "データソース:基板地図情報数値標高モデル、測量方法:1/2.5万地形等高線、標高点格子間隔:約10m四方", //  データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/dem/{z}/{x}/{y}.txt",
            "国土地理院",                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html#dem",  //  [9]参照先URL
        ),
        listOf(
            "標高タイルデータ地球地図全球版標高第2版)", //  [0]タイトル
            "demgm",                    //  [1]データID(DEM10B:Z14,10x10m,精度5m)
            "txt",                      //  [2]ファイル拡張子
            "標高数値データ(DEMGM)",   //  [3]地図の種類
            "0-8",                       //  [4]有効な最大ズームレベル
            "地球地図全球版",                         //  [5]データの整備範囲
            "データソース:地球地図全球版標高第2版を線形的に平滑化することによって得られた値", //  [6]データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/demgm/{z}/{x}/{y}.txt",   //  [7]
            "国土地理院",                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html#dem",  //  [9]参照先URL
        ),
    )
    //  地図データ(各種の国土地理院地図とOpenStreetMap)
    var mMapData = mutableListOf(
        listOf(
            "標準地図",             //  [0] タイトル
            "std",                  //  [1] データID(フォルダ名も兼ねる)
            "png",                  //  [2] タイル画像のファイル拡張子
            "1.標準地図(std)",      //  [3] 地図の種類
            "0-18",             //  [4] 利用できるズーム範囲
            "本全国および全世界",   //  [5] 利用できる範囲
            "道路、建物などの電子地図上の位置の基準である項目と植生、崖、岩、構造物などの土地の状況を" +  //  [6] 地図の概要
                    "表す項目を一つにまとめたデータをもとに作られた。国土地理院の提供している一般的な地図",
            "https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png", //  [7]参照先URL(地理院地図はデフォルトを使用)タイル座標順を含む(指定のない時は{z}/{x}/{y})
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "淡色地図",             //  [0] タイトル
            "pale",                 //  [1] データID(フォルダ名も兼ねる)
            "png",                  //  [2] タイル画像のファイル拡張子
            "2.淡色地図(pale)",     //  [3] 地図の種類
            "2-18",                    //  [4] 利用できるズーム範囲
            "日本全国",             //  [5] 利用できる範囲
            "標準地図を淡い色調で表したもの",  //  [6] 地図の概要
            "https://cyberjapandata.gsi.go.jp/xyz/pale/{z}/{x}/{y}.png",    //  [7] 参照先URL
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "数値地図25000",
            "lcm25k_2012",
            "png",
            "3.数値地図2500(土地条件)(lcm25k)",
            "4-16",
            "一部地域",
            "防災対策や土地利用/土地保全/地域開発などの計画の策定に必要な土地の自然条件などに関する" +
                    "基礎資料提供する目的で、昭和30年代から実施している土地条件調査の成果を基に地形分類" +
                    "(山地、台地・段丘、低地、水部、人口地形など)について可視化したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/lcm25k_2012/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "沿岸海域土地条件図",
            "ccm1",
            "png",
            "4.沿岸海域土地条件図(ccm1)",
            "14-16",
            "一部地域(瀬戸内海)",
            "陸部、解部の地形条件、標高、水深、底質、堆積層、沿岸関連施設、機関、区域などを可視化したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/ccm1/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "火山基本図",
            "vbm",
            "png",
            "5.火山基本図(vbm)",
            "16-18",
            "一部地域(雄阿寒/雌阿寒岳、十勝岳、有珠山、樽前山、北海道駒ヶ岳、" +
                    "八幡平、秋田駒ヶ岳、岩手山、鳥海山、栗駒山、磐梯山、安達太良山、妙高、草津白根山、" +
                    "御嶽山、富士山、大島、三宅島、久住山、阿蘇山、普賢岳、韓国岳、桜島、鬼界ヶ島、竹島)",
            "噴火の防災計画、緊急対策用のほか、火山の研究や火山噴火予知などの基礎資料として整備した" +
                    "火山の地形を精密に表す等高線や火山防災施設などを示した縮尺 1/2500-1/10000の地形図",
            "https://cyberjapandata.gsi.go.jp/xyz/vbm/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "火山土地条件図",
            "vlcd",
            "png",
            "6.火山土地条件図(vlcd)",
            "13-16",
            "一部地域(雄阿寒/雌阿寒岳、十勝岳、有珠山、樽前山、北海道駒ヶ岳、" +
                    "八幡平、秋田駒ヶ岳、岩手山、鳥海山、栗駒山、磐梯山、安達太良山、妙高、草津白根山、" +
                    "御嶽山、富士山、大島、三宅島、久住山、阿蘇山、普賢岳、韓国岳、桜島、鬼界ヶ島、竹島)",
            "火山災害の予測や防災対策立案に利用されている他、地震災害対策、土地保全/利用計画立案や" +
                    "各種の調査/研究、教育のための基礎資料としてあるいは地域や強度の理解を深めるための資料としても" +
                    "活用することを目的として整備した。火山の地形分類を示した縮尺 1/10000-1/50000の地図。" +
                    "過去の火山活動によって形成された地形や噴出物の分布(溶岩流、火砕流、スコリア丘、岩屑なだれなど)" +
                    "防災関連施設・機関、救護保安施設、河川工作物、観光施設などをわかりやすく表示したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/vlcd/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "白地図",
            "blank",
            "png",
            "9.白地図(blank)",
            "5-14",
            "日本全国",
            "全国の白地図",
            "https://cyberjapandata.gsi.go.jp/xyz/blank/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "湖沼図",
            "lake1",
            "png",
            "10.湖沼図(lake1)",
            "11-17",
            "主要な湖および沼(不明)",
            "湖及び沼とその周辺における、道路、主要施設、底質、推進、地形などを示したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/lake1/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "航空写真(全国最新撮影)",
            "seamlessphoto",
            "jpg",
            "11.航空写真全国最新写真(シームレス)(seamlessphoto)",
            "2-18",
            "日本全国",
            "電子国土基本図(オルソ画像)、東日本大震災後正射画像、森林(国有林)の空中写真、" +
                    "簡易空中写真、国土画像情報を組み合わせ、全国をシームレスに閲覧でるようにしたもの",
            "https://cyberjapandata.gsi.go.jp/xyz/seamlessphoto/{z}/{x}/{y}.jpg",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "色別標高図",
            "relief",
            "png",
            "12.色別標高図(relief)",
            "6-15",
            "日本全国",
            "基礎地図情報(数値標高モデル)および日本海洋データ・センタが提供する500mメッシュ" +
                    "海底地形データをもとに作成。標高の変化を色の変化を用いて視覚的に表現したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/relief/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "活断層図(都市圏活断層図)",
            "afm",
            "png",
            "13.活断層図(都市圏活断層)(afm)",
            "7-16",
            "日本全国の活断層",
            "地震被害の軽減に向けて整備された。地形図、活断層とその状態、地形分類を可視化したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/afm/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "宅地利用動向調査成果",
            "lum4bl_capital1994",
            "png",
            "14.宅地利用動向調査成果(lum4bl_capital1994)",
            "6-16",
            "首都圏、(中部圏、近畿圏)",
            "宅地利用動向調査の結果(山林・荒地、田、畑・その他の農地、造成中地、空地、工業用地" +
                    "一般低層住宅地、密集低層住宅地、中・高層住宅、商業・業務用地、道路用地、公園・緑地など、" +
                    "その他の公共施設用地、河川・湖沼など、その他、海、対象地域外)を可視化したもの" +
                    "首都圏は1994年、中部圏は1997年、近畿圏は1996年のデータが最新である",
            "https://cyberjapandata.gsi.go.jp/xyz/lum4bl_capital1994/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "全国植生指標データ",
            "ndvi_250m_2010_10",
            "png",
            "15.全国植生指標データ(ndvi_250m_2010_10)",
            "6-10",
            "日本とその周辺",
            "植生指標とは植物による光の反射の特徴を生かし衛星データを使って簡易な計算式で" +
                    "植生の状況を把握することを目的として考案された指標で植物の量や活力を表している",
            "https://cyberjapandata.gsi.go.jp/xyz/ndvi_250m_2010_10/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "磁気図(水平分力)(2015.0年値)",
            "jikizu2015_chijiki_h",
            "png",
            "17.磁気図(jikizu2015_chijiki_h)",
            "6-8",
            "日本全国",
            "時期の偏角、伏角、全磁力、水平分力、鉛直分力を示したもの",
            "https://cyberjapandata.gsi.go.jp/xyz/jikizu2015_chijiki_h/{z}/{x}/{y}.png",
            "国土地理院",                //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "dem",                  //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "オープンストリートマップ",             //  [0]タイトル
            "osm",                                  //  [1]データID
            "png",                                  //  [2]ファイル拡張子
            "世界地図",                             //  [3]地図の種類
            "0-18",                                 //  [4]有効なズームレベル
            "世界各国",                             //  [5]データの整備範囲
            "オープンストリートマップ（OpenStreetMap、OSM）は自由に利用でき、" +
                    "なおかつ編集機能のある世界地図を作る共同作業プロジェクトである",  //  [6]データの概要
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",   //  [7]タイルデータ参照先URL
            "オープンストリートマップ",             //  [8]参照先名
            "https://tile.openstreetmap.org/",      //  [9]参照先URL
            "demgm",                //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "20万分の1日本シームレス地質図V2",      //  [0]タイトル
            //  Web API  https://gbank.gsj.jp/seamless/v2/api/1.2.1/
            "seamless_v2",                          //  [1]データID
            "png",                                  //  [2]ファイル拡張子
            "日本地図",                             //  [3]地図の種類
            "3-13",                                 //  [4]有効なズームレベル
            "日本全国",                             //  [5]データの整備範囲
            "産業技術総合研究所地質調査総合センターが提供する日本全国統一の凡例を用いた地質図をタイル化したものです。" +
                    "産総研地質調査総合センターウェブサイト利用規約に従って利用できる", //  [6]データの概要
            "https://gbank.gsj.jp/seamless/v2/api/1.2.1/tiles/{z}/{y}/{x}.png", //  [7]タイルデータ参照先URL
            "地質調査総合センター",                 //  [8]参照先名
            "https://www.gsj.jp/HomePageJP.html",   //  [9]ヘルプ参照先URL
            "dem",                     //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "赤色立体地図",                             //  [0]タイトル
            "sekishoku",                                //  [1]データID
            "png",                                      //  [2]ファイル拡張子
            "標高・土地の凹凸 10ｍメッシュ（標高）DEM10B",    //  [3]地図の種類
            "2-14",                                     //  [4]有効なズームレベル
            "日本地図",                                 //  [5]データの整備範囲
            "赤色立体地図はアジア航測株式会社の特許（第3670274号等）を使用して作成したものです。",    //  [6]データの概要
            "https://cyberjapandata.gsi.go.jp/xyz/sekishoku/{z}/{x}/{y}.png",   //  [7]タイルデータ参照先URL
            "国土地理院",                                         //  [8]参照先名
            "https://maps.gsi.go.jp/development/ichiran.html",  //  [9]参照先URL
            "demgm",                     //  [10]標高データID
            "",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "地すべり地形分布図日本全国版",      //  [0]タイトル
            "landslide",                          //  [1]データID
            "png",                                  //  [2]ファイル拡張子
            "地すべり地形分布図日本全国版",                             //  [3]地図の種類
            "5-15",                                 //  [4]有効なズームレベル
            "日本全国（関東地方中央部、東京都及び鹿児島県の島嶼部並びに沖縄県を除く）", //  [5]データの整備範囲
            "防災科学技術研究所地震ハザードステーション（J-SHIS）が提供する地すべり地形分布図日本全国版を表示します。" +
                    "地すべり地形分布図は、地すべり変動によって形成された地形的痕跡である「地すべり地形」の外形と" +
                    "基本構造（滑落崖・移動体）を空中写真判読によりマッピングし、1:50,000地形図にその分布を示した図面です。", //  [6]データの概要
            "https://jmapweb3v.bosai.go.jp/map/xyz/landslide/{z}/{x}/{y}.png", //  [7]タイルデータ参照先URL
            "地震ハザードステーション",                 //  [8]参照先名
            "https://www.j-shis.bosai.go.jp/landslidemap",   //  [9]ヘルプ参照先URL
            "dem",                     //  [10]標高データID
            "pale",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
        listOf(
            "洪水浸水想定区域（想定最大規模）",      //  [0]タイトル
            "01_flood_l2_shinsuishin_data",                          //  [1]データID
            "png",                                  //  [2]ファイル拡張子
            "ハザードマップ",                             //  [3]地図の種類
            "2-17",                                 //  [4]有効なズームレベル
            "都道府県",                             //  [5]データの整備範囲
            "本データは、洪水浸水想定区域（想定最大規模）_国管理河川と洪水浸水想定区域（想定最大規模）_都道府県管理河川のデータを統合したものです。", //  [6]データの概要
            "https://disaportaldata.gsi.go.jp/raster/01_flood_l2_shinsuishin_data/{z}/{x}/{y}.png", //  [7]タイルデータ参照先URL
            "ハザードマップポータルサイト",                 //  [8]参照先名
            "https://disaportal.gsi.go.jp/hazardmap/copyright/opendata.html",   //  [9]ヘルプ参照先URL
            "dem",                     //  [10]標高データID
            "pale",                     //  [11]BaseMapID
            "",                     //  [12]重ねるデータの透過色
            "",                     //  [13]BaseMapが上の場合 (true)
        ),
    )

    val klib = KLib()

    /**
     * 標高データのIDからデータNoを取得
     * id       標高データのID名
     * return   標高データNo(該当なしは -1)
     */
    fun getMapDataNo(id: String): Int {
        return mMapData.indexOfFirst { p -> p[1].compareTo(id) == 0 }
    }

    /**
     * 地図データのWebアドレスを求める
     * mapTitleNum  地図データのNo
     * zoom         ズーム値
     * x            X座標
     * y            Y座標
     * return       Webアドレス
     */
    fun getMapWebAddress(mapTitleNum: Int, zoom:Int, x: Int, y: Int): String {
        var webUrl = mMapData[mapTitleNum][7]
        webUrl = webUrl.replace("{z}", zoom.toString())
        webUrl = webUrl.replace("{x}", x.toString())
        webUrl = webUrl.replace("{y}", y.toString())
        return webUrl
    }

    /**
     * 地図データのデータIDを求める
     * mapTitleNum  地図データのNo
     * return       データID
     */
    fun getMapDataId(mapTitleNum: Int): String {
        return mMapData[mapTitleNum][1]
    }

    /**
     * 地図データの拡張子を求める
     * mapTitleNum  地図データのNo
     * return       拡張子
     */
    fun getMapDataExt(mapTitleNum: Int): String {
        return mMapData[mapTitleNum][2]
    }

    /**
     * 地図データの重ね合わせ地図データのデータIDを求める
     * mapTitleNum  地図データのNo
     * return       データID
     */
    fun getMapMergeDataId(mapTitleNum: Int): String {
        return mMapData[mapTitleNum][11]
    }

    /**
     *  重ね合わせるデータの透過色を取得
     * mapTitleNum  地図データのNo
     * return       透過職(RGB 0xRRGGBB)
     */
    fun getMapOverlapTransparent(mapTitleNum: Int): Int {
        return klib.str2Integer("0x" + mMapData[mapTitleNum][12])
    }

    /**
     * 重ね合わせ地図データが上になるか
     * mapTitleNum  地図データのNo
     * return       上に重ねる(true)
     */
    fun getMapMergeOverlap(mapTitleNum: Int): Boolean {
        return mMapData[mapTitleNum][13].compareTo("true") == 0
    }

    /**
     * 標高データのIDからデータNoを取得
     * id       標高データのID名
     * return   標高データNo
     */
    fun getElevatorDataNo(id: String): Int {
        val no = mMapElevatorData.indexOfFirst { p -> p[1].compareTo(id) == 0 }
        return if (no < 0) 0 else no
    }

    /**
     *  標高データの最ID名を取得
     * elevatorDataNo   標高データのNo
     * return           標高データのID名
     */
    fun getElevatorDataId(elevatorDataNo: Int): String {
        return mMapElevatorData[elevatorDataNo][1]
    }

    /**
     *  標高データの拡張子を取得
     * elevatorDataNo   標高データのNo
     * return           拡張子
     */
    fun getElevatorDataExt(elevatorDataNo: Int): String {
        return mMapElevatorData[elevatorDataNo][2]
    }

    /**
     *  標高データのWebアドレスを取得
     * elevatorDataNo   標高データのNo
     * return           Webアドレス
     */
    fun getElevatorWebAddress(elevatorDataNo: Int, zoom:Int, x: Int, y: Int): String {
        var webUrl = mMapElevatorData[elevatorDataNo][7]
        webUrl = webUrl.replace("{z}", zoom.toString())
        webUrl = webUrl.replace("{x}", x.toString())
        webUrl = webUrl.replace("{y}", y.toString())
        return webUrl
    }

    /**
     * 標高データの最大ズーム値を取得
     * elevatorDataNo   標高データのNo
     * return           最大ズーム値
     */
    fun getElevatorMaxZoom(elevatorDataNo: Int): Int {
        return getMaxZoom(mMapElevatorData[elevatorDataNo][4], 14)
    }

    /**
     * ズーム値の文字列から最大ズーム値(最後の数値)を求める
     * zoom         ズーム値の文字列
     * default      数値がない時の値
     * return       ズーム値
     */
    fun getMaxZoom(zoom: String, default: Int = 0): Int {
        var buf = ""
        var buf2 = ""
        for (i in zoom.indices) {
            if (zoom[i].isDigit())
                buf += zoom[i]
            else {
                buf2 = buf
                buf = ""
            }
        }
        if (0 < buf.length)
            return buf.toInt()
        else if (0 < buf2.length)
            return buf2.toInt()
        else
            return default
    }

    /**
     * ファイルからMapDataを読み込む
     * path     ファイル名
     */
    fun loadMapInfoData(path: String) {
        if (klib.existsFile(path)) {
            val mapData = klib.loadCsvData(path, mMapDataFormat)
            if (0 < mapData.size) {
                mMapData.clear()
                mMapData.addAll(mapData)
            }
        }
    }

    /**
     * MapDataをファイルに保存
     * path     ファイル名
     */
    fun saveMaoInfoData(path: String) {
        if (0 < mMapData.size)
            klib.saveCsvData(path, mMapDataFormat, mMapData)
    }
}