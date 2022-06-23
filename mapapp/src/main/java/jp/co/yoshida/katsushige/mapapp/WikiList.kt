package jp.co.yoshida.katsushige.mapapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityMainBinding
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityWikiListBinding
import jp.co.yoshida.katsushige.mylib.KLib
//import kotlinx.android.synthetic.main.activity_wiki_list.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*

class WikiList : AppCompatActivity() {
    val TAG = "WikiList"

    var mListDataItems = mutableListOf<Map<String, String>>()   //  一覧リスト表示データ
    lateinit var mListDataAdapter: SimpleAdapter                //  一覧リスト表示フォーマットアダプタ

    var mUrlAddressMenu = listOf("開く", "追加" , "削除")         //  一覧リストのメニュー
    val mListMenuTitle = listOf(                                //  リストの操作メニュー
        "地図の位置", "開く", "詳細情報", "マーク登録", "コピー", "URLコピー",
        "削除", "選択位置まで削除", "選択位置以降を削除")
    var mSelectListDataItem = -1                    //  一覧リスト選択位置

    var mWikiDataList = WikiDataList()              //  Wikiデータクラス
    var mWikiUrlList = WikiUrlList()                //  Wikiリスト
    var mMarkList = MarkList()                      //  マークリストクラス
    val mMarkListPath = "MarkList.csv"              //  マークリスト保存ファイル名

//    var mTimer: Timer? = null
    var klib = KLib()

    lateinit var binding: ActivityWikiListBinding
    lateinit var tvCnvForm: TextView
    lateinit var tvListCount: TextView
    lateinit var tvUrlAddress: TextView
    lateinit var lvListData: ListView
    lateinit var pbGetInfCount: ProgressBar
    lateinit var edSearchWord: EditText
    lateinit var spUrlTitle: Spinner
    lateinit var spSearchForm: Spinner
    lateinit var btListUpdate: Button
    lateinit var btGetDiscription: Button
    lateinit var btListRefresh: Button
    lateinit var btNextSearch: Button
    lateinit var btPrevSearch: Button
    lateinit var btSearch: Button


    val handler = Handler(Looper.getMainLooper())   //  HandlerでUIを制御
    //  詳細取得スレッド監視
    val runnableDetail = object : Runnable {
        override fun run() {
            tvListCount.text =
                "[ " + (mWikiDataList.mCount) + " / " + mWikiDataList.mDataList.count() + " ]"
            pbGetInfCount.progress = mWikiDataList.mCount
            if (0 < mWikiDataList.mCount) {
                handler.postDelayed(this, 500)
            } else {
                pbGetInfCount.progress = 0
                setWikiDataList(mWikiDataList.mDataList)              //  一覧リストを表示
                setButton(false)
            }
        }
    }

    //  ファイル検索スレッド監視
    val runnableSearch = object : Runnable {
        override fun run() {
//            tvListCount.setText("データ数: ")
            pbGetInfCount.max = mWikiDataList.mFilesSize
            tvListCount.text = mWikiDataList.mCount.toString() +
                " [ " + (mWikiDataList.mFilesCount) + " / " + mWikiDataList.mFilesSize + " ]"
            pbGetInfCount.progress = mWikiDataList.mFilesCount
            if (0 < mWikiDataList.mFilesCount) {
                handler.postDelayed(this, 500)
            } else {
                pbGetInfCount.progress = 0
                setWikiDataList(mWikiDataList.mDataList)              //  一覧リストを表示
                setButton(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wiki_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)   //  戻るメニュー追加
        title = "Wikipedia 一覧リスト"

        binding = ActivityWikiListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tvCnvForm = binding.tvCnvForm
        tvListCount = binding.tvListCount
        tvUrlAddress = binding.tvUrlAddress
        lvListData = binding.lvListData
        pbGetInfCount = binding.pbGetInfCount
        edSearchWord = binding.edSearchWord
        spUrlTitle = binding.spUrlTitle
        spSearchForm = binding.spSearchForm
        btListUpdate = binding.btListUpdate
        btGetDiscription = binding.btGetDiscription
        btListRefresh = binding.btListRefresh
        btNextSearch = binding.btNextSearch
        btPrevSearch = binding.btPrevSearch
        btSearch =binding.btSearch

        mWikiDataList.mWikiList = this
        var searchWord = intent.getStringExtra("COORDINATE")
        edSearchWord.setText(searchWord)

        init()                              //  初期化
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mWikiUrlList.saveWikiUrlList()         //  URL一覧リストファイル保存
        mMarkList.saveMarkFile(this)    //  マークリストの保存
    }

    /**
     * 初期化処理
     */
    fun init() {
        val outFolder = klib.getPackageNameDirectory(this) + "/WikiData"   //  データ保存フォルダ
        mWikiDataList.mOutFolder = outFolder
        mWikiUrlList.mUrlListPath = outFolder + "/WikiUrlList.csv"  //  URL一覧リスト保存ファイル名
        tvCnvForm.text = "自動"
        tvListCount.text = ""
        mWikiUrlList.loadWikiUrlList()      //  URL一覧リストの読込
        setUrlList()                        //  URL一覧リストをコンボボックスに設定

        //  マークリストデータの読み込み
        mMarkList.mSaveFilePath = klib.getPackageNameDirectory(this) + "/" + mMarkListPath
        mMarkList.loadMarkFile(this)

        //  URLリスト選択
        spUrlTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //  リストデータの読込表示
                tvUrlAddress.text = mWikiUrlList.mUrlList[position]
                mWikiDataList.mDataList.clear()
                if (!loadData()) {          //  ファイルから取得
                    listDownLoad()          //  Webから取得
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        //  一覧リスト取得の検索方式の選択
        var searchFormAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mWikiDataList.mSearchFormTitle)
        spSearchForm.adapter = searchFormAdapter
        spSearchForm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                mSearchForm = mSearchForm.getOrder(position)    //  一覧リスト抽出方法
                tvCnvForm.text = mWikiDataList.mSearchFormTitle[position]     //  一覧リスト抽出方法名
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        //  一覧リストのリストビュー表示
        lvListData.onItemClickListener = object : AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                TODO("Not yet implemented")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
            override fun onItemClick(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //  地図位置への移動
                mapMove(position)
            }
        }

        //  一覧リストデータの長押し処理でメニューを表示
        lvListData.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(p0: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                //  一覧リストのコンテキストメニュー選択
                mSelectListDataItem = position
                klib.setMenuDialog(this@WikiList, "コマンド選択", mListMenuTitle, iListDataMenu)
                return true
            }
        }

        //  URLリスト長押し選択
        tvUrlAddress.setOnLongClickListener {
            //  URLリストのコンテキストメニュー
            klib.setMenuDialog(this@WikiList, "コマンド選択", mUrlAddressMenu, iUrlAddressMenu)
            true
        }

        //  一覧更新ボタン
        btListUpdate.setOnClickListener {
            listDownLoad()
        }

        //  詳細取得ボタン
        btGetDiscription.setOnClickListener {
            if (btGetDiscription.text.contains("詳細取得")){
                setButton(true)
                mWikiDataList.mGetListDataStop = false
                listDataUpdate()
                handler.post(runnableDetail)
            } else {
                //  中断処理
                setButton(false)
                mWikiDataList.mGetListDataStop = true
                handler.removeCallbacks(runnableDetail)
            }
        }

        //  表示更新ボタン   リストデータの更新再表示
        btListRefresh.setOnClickListener {
            setWikiDataList(mWikiDataList.mDataList)              //  一覧リストを表示
            setButton(false)
        }

        //  次検索ボタン
        btNextSearch.setOnClickListener {
//            var pos = lvListData.selectedItemPosition + 1
            var pos = mSelectListDataItem + 1
            Log.d(TAG,"btNextSearch: "+pos)
            mSelectListDataItem = mWikiDataList.searchWord(edSearchWord.text.toString(), pos, true)
        }

        //  前検索ボタン
        btPrevSearch.setOnClickListener {
//            var pos = lvListData.selectedItemPosition - 1
            var pos = mSelectListDataItem - 1
            Log.d(TAG,"btPrevSearch: "+pos)
            mSelectListDataItem = mWikiDataList.searchWord(edSearchWord.text.toString(), pos, false)
        }

        //  ファイル検索ボタン
        btSearch.setOnClickListener {
            setButton(true)
            pbGetInfCount.max = mWikiDataList.getSearchFileCount()
            mWikiDataList.mGetListDataStop = false
            GlobalScope.launch {
                mWikiDataList.searchFileWord(edSearchWord.text.toString(), tvUrlAddress.text.toString())
//                setWikiDataList(mWikiDataList)
            }
            handler.post(runnableSearch)
        }
    }

    /**
     * 戻るメニューの処理
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var ret = true
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            ret = super.onOptionsItemSelected(item)
        }
        return ret
//        return super.onOptionsItemSelected(item)
    }

    /**
     * 詳細取得ボタンの設定
     * start        詳細取得開始
     */
    fun setButton(start: Boolean) {
        if (start) {
            btGetDiscription.text = "中断"
            spUrlTitle.isEnabled = false
            btListUpdate.isEnabled = false
            btSearch.isEnabled = false
            btNextSearch.isEnabled = false
            btPrevSearch.isEnabled = false
        } else {
            btGetDiscription.text = "詳細取得"
            spUrlTitle.isEnabled = true
            btListUpdate.isEnabled = true
            btSearch.isEnabled = true
            btNextSearch.isEnabled = true
            btPrevSearch.isEnabled = true
        }
    }

    /**
     *  URLアドレスのコンテキストメニューの処理
     */
    var iUrlAddressMenu = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mUrlAddressMenu.indexOf(s)) {
            0 -> {                  //  開く
                klib.webDisp(this, tvUrlAddress.text.toString())
            }
            1 -> {                  //  URLの追加
                klib.setInputDialog(this, "URLアドレスを入力",
                    "https://ja.wikipedia.org/wiki/", iUrlAddress)
            }
            2 -> {                  //  URLの削除
                mWikiUrlList.mUrlList.remove(tvUrlAddress.text.toString())
                setUrlList()
            }
        }
    }

    /**
     *  入力ダイヤログを出してURLを追加登録
     */
    var iUrlAddress = Consumer<String> { s->
        mWikiUrlList.mUrlList.add(s)
        setUrlList()
    }

    /**
     * リストデータのコンテキストメニューの処理(関数いたーフェース)
     */
    var iListDataMenu = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        if (0 <= mSelectListDataItem) {
            val n = mListMenuTitle.indexOf(s)
            when (n) {
                0 -> {          //  地図の位置移動
                    mapMove(mSelectListDataItem)
                }
                1 -> {          //  URLを開く
                    klib.webDisp(this, mWikiDataList.mDataList[mSelectListDataItem].mUrl)
                }
                2 -> {          //  詳細情報表示
                    var message = "タイトル: " + mWikiDataList.mDataList[mSelectListDataItem].mTitle + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mComment.length)
                        message += "コメント: " + mWikiDataList.mDataList[mSelectListDataItem].mComment + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mIntroduction.length)
                        message += "概要: " + mWikiDataList.mDataList[mSelectListDataItem].mIntroduction + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mCoordinate.length)
                        message += "座標: " + mWikiDataList.mDataList[mSelectListDataItem].mCoordinate + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mDetail.length)
                        message += "基本情報: \n" + mWikiDataList.mDataList[mSelectListDataItem].mDetail.replace(";", "\n")
                    message += "\n"
                    message += "リスト名: " + mWikiDataList.mDataList[mSelectListDataItem].mListName + "\n"
                    message += "一覧抽出方法: " + mWikiDataList.mDataList[mSelectListDataItem].mSearchForm + "\n"
                    klib.messageDialog(this, "詳細情報", message)
                }
                3 -> {          //  マークの追加
                    addMark()
                }
                4 -> {          //  コピー
                    var buffer = "タイトル: " + mWikiDataList.mDataList[mSelectListDataItem].mTitle + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mComment.length)
                        buffer += "コメント: " + mWikiDataList.mDataList[mSelectListDataItem].mComment + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mIntroduction.length)
                        buffer += "概要: " + mWikiDataList.mDataList[mSelectListDataItem].mIntroduction + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mCoordinate.length)
                        buffer += "座標: " + mWikiDataList.mDataList[mSelectListDataItem].mCoordinate + "\n"
                    if (0 < mWikiDataList.mDataList[mSelectListDataItem].mDetail.length)
                        buffer += "基本情報: \n" + mWikiDataList.mDataList[mSelectListDataItem].mDetail.replace(";", "\n")
                    buffer += "URL: " + mWikiDataList.mDataList[mSelectListDataItem].mUrl
                    klib.setTextClipBoard(this, buffer)
                }
                5 -> {          //  URLコピー
                    klib.setTextClipBoard(this, mWikiDataList.mDataList[mSelectListDataItem].mUrl)
                }
                6 -> {          //  削除
                    mWikiDataList.mDataList.removeAt(mSelectListDataItem)
                    saveData()
                    setWikiDataList(mWikiDataList.mDataList)
                }
                7 -> {          //  選択位置まで削除
                    var size = mWikiDataList.mDataList.count() - mSelectListDataItem
                    while (size <= mWikiDataList.mDataList.count()) {
                        mWikiDataList.mDataList.removeAt(0)
                    }
                    saveData()
                    setWikiDataList(mWikiDataList.mDataList)
                }
                8 ->{           //  選択位置以降を削除
                    while (mSelectListDataItem < mWikiDataList.mDataList.count()) {
                        mWikiDataList.mDataList.removeAt(mWikiDataList.mDataList.count() - 1)
                    }
                    saveData()
                    setWikiDataList(mWikiDataList.mDataList)
                }
            }
            mSelectListDataItem = -1
        }
    }

    /**
     * Wikiデータをもとにマークの追加
     */
    fun addMark() {
        var mark = MarkData()
        mark.mTitle = mWikiDataList.mDataList[mSelectListDataItem].mTitle
        mark.mComment = mWikiDataList.mDataList[mSelectListDataItem].mComment +
                mWikiDataList.mDataList[mSelectListDataItem].mDetail
        mark.mLink = mWikiDataList.mDataList[mSelectListDataItem].mUrl
        mark.mGroup = mWikiDataList.mDataList[mSelectListDataItem].mListName
        val coordinate = mWikiDataList.mDataList[mSelectListDataItem].mCoordinate
        val ctr = klib.string2Coordinate(coordinate.toString())
        if (ctr.x != 0.0 && ctr.x != 0.0) {
            mark.mLocation = klib.coordinates2BaseMap(ctr)
        }
        Log.d(TAG,"addMark: " + coordinate + " " + mark.mLocation.toString())
        var buf = mark.getDataString()
        Log.d(TAG,"addMark: " + buf)
        mMarkList.setMarkInputDialog(this, "マークの編集", buf, mMarkList.iMarkSetOperation)
    }

    /**
     * 詳細情報を取得して追加
     * コルーチンで実行
     */
    fun listDataUpdate() {
        pbGetInfCount.max = mWikiDataList.mDataList.count()
        GlobalScope.launch {
            mWikiDataList.addBaseInfo()         //  詳細情報追加
            saveData()                          //  ファイル保存
//            setWikiDataList(mWikiDataList)    //  一覧リストを表示(コルーチンの中では表示の更新ができない)
        }
    }

    /**
     * WikipediaのHTMLソースをダウンロードしてファイルに保存
     */
    fun listDownLoad() {
//        mSearchForm = mSearchForm.getOrder(spSearchForm.selectedItemPosition)   //  抽出方法
        var urlAddress = tvUrlAddress.text.toString()
        if (0 <= urlAddress.indexOf("http")) {
            mWikiDataList.mSearchForm = mWikiDataList.mSearchForm.getOrder(spSearchForm.selectedItemPosition)   //  抽出方法
            mWikiDataList.listDownLoad(urlAddress, true)    //  一覧リスト取得
            saveData()
        }
        setWikiDataList(mWikiDataList.mDataList)                     //  一覧リストを表示
    }


    /**
     * Wikipediaの一覧リストをListViewに反映
     * wikiList         Wikipediaの一覧リスト
     */
    fun setWikiDataList(wikiList: List<WikiData>) {
        mListDataItems.clear()
        if (1 < wikiList.count()) {
            //  UrlData を String[] のリストに変換
            for ( i in wikiList.indices) {
                var map = mutableMapOf(
                    "title" to "[" + wikiList[i].mTitle + "] [" + wikiList[i].mComment + "] [" + wikiList[i].mCoordinate + "]"
                    + if (0 < wikiList[i].mDistance) " [%.2fkm]".format(wikiList[i].mDistance) else "",
                    "detail" to wikiList[i].mUrl)
                mListDataItems.add(map)
            }
        }

        //  リストアダプターにリストデータを設定
        mListDataAdapter = SimpleAdapter(
            this,
            mListDataItems,
            android.R.layout.simple_list_item_2,
            arrayOf("title", "detail"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        Log.d(TAG,"setWikiDataList: "+mListDataAdapter.count)
        lvListData.adapter = mListDataAdapter
        tvListCount.setText("データ数: "+mListDataAdapter.count)
        if (0 < wikiList.count())
            tvCnvForm.setText(wikiList[0].mSearchForm)
        mSelectListDataItem = 0
    }

    /**
     * 地図の中心を設定して地図表示に戻る
     * position     リストデータの位置
     */
    fun mapMove(position: Int) {
        if (0 <= position) {
            val coordinate = mWikiDataList.mDataList[position].mCoordinate
            Log.d(TAG,"mapMove: " + coordinate + " " + position)
            if (0 < coordinate.length) {
                val intent = Intent()
                intent.putExtra("座標", coordinate)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    /**
     *  URLリストからタイトルを抽出してコンボボックスに登録
     */
    fun setUrlList(){
        //  URLリストのURLからタイトルを抽出して設定
        mWikiUrlList.mUrlTitle.clear()
        for (url in mWikiUrlList.mUrlList) {
            if (0 <= url.indexOf("http"))
                mWikiUrlList.mUrlTitle.add(url.substring(url.lastIndexOf(("/")) + 1))    //  タイトル抽出
            else
                mWikiUrlList.mUrlTitle.add(url)
        }
        //  URLリスト選択
        var urlTitleAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mWikiUrlList.mUrlTitle)
        spUrlTitle.adapter = urlTitleAdapter
    }

    /**
     * 一覧表データをファイルに保存
     */
    fun saveData() {
        mWikiDataList.saveData(spUrlTitle.selectedItem.toString())
    }

    /**
     * コンボボックスで表示されている一覧表のデータをファイルから読み込で表示
     */
    fun loadData(): Boolean {
        mWikiDataList.mDataList.clear()
        if (mWikiDataList.loadData(spUrlTitle.selectedItem.toString())) {
            setWikiDataList(mWikiDataList.mDataList)                          //  一覧リストを表示
            return true
        }
        return false
    }
}
