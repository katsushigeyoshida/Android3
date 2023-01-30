
MainActivity.kt			MapAppのメインプログラムでUI制御
    MapView
    MapInfoData
    MapData
    AreaData
    MarkList
    Measure
    GpsDataList
    GpsTraceList
    GpsTrace
    KLib
    MapInfoDataActivity
    WikiList
    GpsTraceListActivity
    GpsService
MapView.kt				地図表示のViewクラス
MapData.kt				地図データを管理クラス
MapInfoData.kt			地図情報データ(国土地理院地図などのデータ)
AreaData.kt				地図登録情報の管理クラス
Measure.kt				地図の距離を測定するクラス
GpsService.kt			GPSのトレースを行うサービスクラス
GpsTrace.kt				GPSトレースデータクラス
GpsTraceListActivity.kt	GPSトレースデータおよび外部GPXファイルの一覧表示クラス
    GpsTraceList
    GpxEditActivity
    GpsGraph
GpsTraceList.kt			GPSトレースデータ一覧管理クラス
GpxEditActivity.kt		GPSトレースデーの属性編集
    GpsTraceList
    GpsGraph
GpsGraph.kt				GPSデータをグラフ表示するUIクラス
GpsGraphView.kt			GPSデータをグラフ表示する描画クラス
MarkData.kt				Markデータクラス
MarkList.kt				Markデータの一覧管理クラス
WikiList.kt				Wikipediaから抽出したデータを管理するUIクラス
WikiDataList.kt			Wikipediaから抽出したデータを一覧管理するクラス
WikiData.kt				Wikipediaから抽出したデータクラス
WikiUrlList.kt			抽出するWikipediaのアドレスの一覧管理クラス

GpxListActivity			外部GPXファイル一覧管理 → GpsTraceListActivity に移行
GpsDataList.kt			GPSトレースデータの管理 → GpsTraceList に移行
GpsCSvEditActivity.kt	GPSトレースデータの属性編集クラス → GpxEditActivity に移行
