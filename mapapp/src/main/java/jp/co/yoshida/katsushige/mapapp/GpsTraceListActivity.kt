package jp.co.yoshida.katsushige.mapapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.size
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityGpsTraceListBinding
import jp.co.yoshida.katsushige.mylib.KLib

/**
 * GPSトレースデータ(CSVファイル)の管理
 */
class GpsTraceListActivity : AppCompatActivity() {
    val TAG = "GpsTraceListActivity"

    lateinit var binding: ActivityGpsTraceListBinding
    lateinit var spYear: Spinner
    lateinit var spGroup: Spinner
    lateinit var spCategory: Spinner
    lateinit var btUpdate: Button
    lateinit var btTrash: Button
    lateinit var btExport: Button
    lateinit var btAdd: Button
    lateinit var btSort: Button
    lateinit var btRoute: Button
    lateinit var btSelect: Button
    lateinit var lvDataList: ListView

    val REQUESTCODE_GPXEDIT = 3
    val REQUESTCODE_GPXFILELIST = 4
    val REQUESTCODE_CSVEDIT = 5

    val mDispMenu = listOf<String>(
        "全表示", "全非表示", "反転表示", "選択表示"
    )
    val mRemoveMenu = listOf<String>(
        "表示分ゴミ箱", "全ゴミ箱解除", "ゴミ箱から削除"
    )
    val mSelectRemoveMenu = listOf<String>(
        "ゴミ箱", "ゴミ箱解除", "完全削除"
    )
    val mSortMenu = listOf<String>(
        "日付順", "タイトル順", "距離順", "標高順"
    )
    val mUpdateMenu = listOf<String>(
        "再表示", "データファイル確認", "初期化"
    )
    val mItemClickMenu = listOf<String>(
        "編集", "経路表示", "位置移動", "グラフ表示", "ゴミ箱", "GPXエクスポート"
    )

    var mSelectListPosition = -1
    var mSelectList = false
    var mGpsTraceList = GpsTraceList()                  //  GPXファイルリスト
    var mGpsTraceFileFolder = ""                        //  GPXファイルリストパス
    var mGpsTraceListPath = ""

    val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_trace_list)
        title = "GPSトレース"

        mGpsTraceFileFolder = intent.getStringExtra("GPSTRACEFOLDER").toString()
        mGpsTraceListPath = intent.getStringExtra("GPSTRACELISTPATH").toString()

        mGpsTraceList.mC = this
        mGpsTraceList.mGpsTraceFileFolder = mGpsTraceFileFolder
        mGpsTraceList.mGpsTraceListPath = mGpsTraceListPath
        mGpsTraceList.loadListFile()
        mGpsTraceList.getFileData()
        if (0 < mGpsTraceList.mErrorMessage.length) {
            klib.messageDialog(this, "エラー", mGpsTraceList.mErrorMessage)
            mGpsTraceList.mErrorMessage = ""
        }

        initControl()
    }

    override fun onPause() {
        super.onPause()
        mGpsTraceList.saveListFile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTCODE_CSVEDIT -> {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG,"onActivityResult: REQUESTCODE_CSVEDIT: ")
                    mGpsTraceList.loadListFile()
                    setSpinnerData()
                    setDataList()
                }
            }
        }
    }

    /**
     * コントロールの初期化とデータの設定
     */
    fun initControl() {
        binding = ActivityGpsTraceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spYear = binding.spinner7
        spGroup = binding.spinner8
        spCategory = binding.spinner9
        btUpdate = binding.button17
        btTrash = binding.button18
        btExport = binding.button28
        btAdd = binding.button27
        btSort = binding.button19
        btRoute = binding.button20
        btSelect = binding.button25
        lvDataList = binding.gpsTraceListView

        setSpinnerData()
        setDataList()

        //  [データ年]選択でのフィルタ処理
        spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //  [分類]選択でのフィルタ処理
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //  [グループ]選択でのフィルタ処理
        spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        /**
         * [選択モード]切替
         */
        btSelect.setOnClickListener {
            mSelectList = !mSelectList
            setDataList()
        }

        /**
         * [経路表示]
         */
        btRoute.setOnClickListener {
            if (mSelectList) {
                var visibleChecked = lvDataList.checkedItemPositions
                Log.d(TAG, "setOnClickListener: "+visibleChecked)
                mGpsTraceList.clearVisible()
                for (i in 0..visibleChecked.size()-1){
                    if (visibleChecked.valueAt(i)) {
                        val n = getItemPos2DataListPos(visibleChecked.keyAt(i))
                        Log.d(TAG, "setOnClickListener: "+i+" "+n)
                        if (0 <= n)
                            mGpsTraceList.mDataList[n].mVisible = true
                    }
                }
                mSelectList = !mSelectList
                setDataList()
            } else {
                klib.setMenuDialog(this, "経路表示設定メニュー", mDispMenu, iRouteDispOperatin)
            }
        }

        /**
         * [ソート]
         */
        btSort.setOnClickListener {
            klib.setMenuDialog(this, "ソートメニュー", mSortMenu, iSortOperatin)
        }

        /**
         * [追加]
         */
        btAdd.setOnClickListener {
            goGpsCsvEdit("")
        }

        /**
         * [エクスポート]
         */
        btExport.setOnClickListener {
            if (mSelectList) {
                klib.folderSelectDialog(this, mGpsTraceFileFolder, iGpsFilesExportOperation)
            }
        }

        /**
         * [ゴミ箱]
         */
        btTrash.setOnClickListener {
            if (mSelectList) {
                klib.setMenuDialog(this, "削除メニュー", mSelectRemoveMenu, iSelectRemoveOperatin)
            } else {
                klib.setMenuDialog(this, "削除メニュー", mRemoveMenu, iRemoveOperatin)
            }
        }

        /**
         * [データ更新]
         */
        btUpdate.setOnClickListener {
            if (mSelectList) {
                selectDataUpDate()
            } else {
                klib.setMenuDialog(this, "更新メニュー", mUpdateMenu, iUpdateOperatin)
            }
            setDataList()
        }

        //  一覧リストの項目クリックで項目編集
        lvDataList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectListPosition = position
                if (mSelectList)
                    return
                itemClickMenu()
            }
        }

        //  一覧リストの項目長押しで選択モード
        lvDataList.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                //  選択(選択モードの変更)
                mSelectList = !mSelectList
                setDataList()
                return true
            }
        }
    }

    //  GPXエクスポートで選択したフォルダへ変換
    var iGpsFilesExportOperation = Consumer<String> { s ->
        if (0 < s.length) {
            selectExport(s)
        }
    }

    /**
     * 選択された項目のデータを指定フォルダにGPX変換ファイルを出力
     * outFolder            出力先フォルダ
     */
    fun selectExport(outFolder: String) {
        var checked = lvDataList.checkedItemPositions
        var count = 0
        var gpsTraceList = mutableListOf<GpsTraceList.GpsTraceData>()
        for (i in 0..checked.size()-1){
            if (checked.valueAt(i)) {
                val n = getItemPos2DataListPos(checked.keyAt(i))
                Log.d(TAG, "setOnClickListener: "+i+" "+n+" "+mGpsTraceList.mDataList[n].mFilePath)
                if (0 <= n) {
                    gpsTraceList.add(mGpsTraceList.mDataList[n])
                    count++
                }
            }
        }
        GpsTraceList.gpxExport(gpsTraceList, outFolder)

        if (0 < count)
            Toast.makeText(this, count.toString() + " 個のデータをエクスポートします", Toast.LENGTH_LONG).show()

        mSelectList = false
        setDataList()
    }

    /**
     * 選択項目をファイルからデータを更新する
     */
    fun selectDataUpDate() {
        var visibleChecked = lvDataList.checkedItemPositions
        var count = 0
        for (i in 0..visibleChecked.size()-1){
            if (visibleChecked.valueAt(i)) {
                val n = getItemPos2DataListPos(visibleChecked.keyAt(i))
                Log.d(TAG, "setOnClickListener: "+i+" "+n)
                if (0 <= n) {
                    if (!mGpsTraceList.reloadDataFile(n))     //  データファイル読み直す
                        count++
                }
            }
        }
        if (0 < count)
            Toast.makeText(this, count.toString() + " 個のデータを更新しました", Toast.LENGTH_LONG).show()
        mSelectList = !mSelectList
        setDataList()
    }

    //  データ項目の更新
    var iUpdateOperatin = Consumer<String> { s ->
        if (s.compareTo(mUpdateMenu[0]) == 0) {
            //  再表示
            mSelectList = false
            setDataList()
        } else if (s.compareTo(mUpdateMenu[1]) == 0) {
            //  データファイル確認
            val n = mGpsTraceList.existDataFileAll()
            Toast.makeText(this, "データのない " + n.toString() + " 個の項目を削除しました", Toast.LENGTH_LONG).show()
            setDataList()
        } else if (s.compareTo(mUpdateMenu[2]) == 0) {
            //  初期化
            klib.messageDialog(this, "確認", "リストの全項目を初期化します", iAllItemInitOperation)
        }
    }

    //  すべての項目を初期化する(データファイル読み直し)
    var iAllItemInitOperation = Consumer<String> { s ->
        if (s.compareTo("OK") == 0) {
            mGpsTraceList.mDataList.clear()
            mGpsTraceList.getFileData()
            setDataList()
        }
    }

    //  ゴミ箱・削除処理
    var iRemoveOperatin = Consumer<String> { s ->
        if (s.compareTo(mRemoveMenu[0]) == 0) {
            //  表示全ゴミ箱
            mGpsTraceList.setTrashData(getDispDataPos())
            setDataList()
        } else if (s.compareTo(mRemoveMenu[1]) == 0) {
            //  全ゴミ箱解除
            mGpsTraceList.setUnTrashData(getSelectDataPos())
            setDataList()
        } else if (s.compareTo(mRemoveMenu[2]) == 0) {
            //  ゴミ箱から削除
            klib.messageDialog(
                this, "確認", "ゴミ箱の全データをファイルごと削除します",
                iAllTrashRemoveOperation)
        }
    }

    //  選択項目のゴミ箱または削除処理
    var iSelectRemoveOperatin = Consumer<String> { s ->
        if (s.compareTo(mSelectRemoveMenu[0]) == 0) {
            //  選択ゴミ箱
            if (mSelectList) {
                mGpsTraceList.setTrashData(getSelectDataPos())
                setSpinnerData()
                mSelectList = false
                setDataList()
            }
        } else if (s.compareTo(mSelectRemoveMenu[1]) == 0) {
            //  選択ゴミ箱解除
            if (mSelectList) {
                mGpsTraceList.setUnTrashData(getSelectDataPos())
                mSelectList = false
                setDataList()
            }
        } else if (s.compareTo(mSelectRemoveMenu[2]) == 0) {
            //  選択削除
            if (mSelectList) {
                val firstTimeList = getSelectFirstTimeStr()
                if (0 < firstTimeList.size) {
                    val n = mGpsTraceList.findListTitleFirstTime(firstTimeList[0])
                    val path = mGpsTraceList.mDataList[n].mFilePath
                    klib.messageDialog(
                        this, "確認", path + "など\n選択した項目をデータファイルごと削除します",
                        iRemoveOperation)
                }
            }
        }
    }

    //  選択表示項目を削除
    var iRemoveOperation = Consumer<String> { s ->
        if (s.compareTo("OK") == 0) {
            mGpsTraceList.removeDataFile(getSelectFirstTimeStr())
            mSelectList = false
            setDataList()
        }
    }

    //  表示されているすべてのゴミ箱の項目を削除
    var iAllTrashRemoveOperation = Consumer<String> { s ->
        if (s.compareTo("OK") == 0) {
            mGpsTraceList.removeTrashDataFile(getDispFirstTimeStr())
            setDataList()
        }
    }

    //  表示されているすべての項目を削除
    var iAllRemoveOperation = Consumer<String> { s ->
        if (s.compareTo("OK") == 0) {
            mGpsTraceList.removeDataFile(getDispFirstTimeStr())
            setDataList()
        }
    }

    //  GPXトレースの経路表示処理
    var iRouteDispOperatin = Consumer<String> { s ->
        if (s.compareTo(mDispMenu[0])== 0) {
            //  全経路表示
            mGpsTraceList.setVisible(getDispDataPos())
        } else if (s.compareTo(mDispMenu[1])== 0) {
            //  全経路非表示
            mGpsTraceList.clearVisible()
        } else if (s.compareTo(mDispMenu[2])== 0) {
            //  反転
            mGpsTraceList.reverseVisible()
        } else if (s.compareTo(mDispMenu[3])== 0) {
            //  選択モードに変更
            if (mSelectList) {
                mGpsTraceList.setVisible(getSelectDataPos())    //  表示のチェック設定
                mSelectList = false
            }
        } else
            return@Consumer
        setDataList()
    }

    //  ソートの設定
    var iSortOperatin = Consumer<String> { s ->
        if (s.compareTo(mSortMenu[0])== 0) {
            //  日付順
            mGpsTraceList.setDataListSortType(GpsTraceList.DATALISTSORTTYPE.DATE)
        } else if (s.compareTo(mSortMenu[1])== 0) {
            //  タイトル順
            mGpsTraceList.setDataListSortType(GpsTraceList.DATALISTSORTTYPE.TITLE)
        } else if (s.compareTo(mSortMenu[2])== 0) {
            //  距離順
            mGpsTraceList.setDataListSortType(GpsTraceList.DATALISTSORTTYPE.DISTANCE)
        } else if (s.compareTo(mSortMenu[3])== 0) {
            //  標高順
            mGpsTraceList.setDataListSortType(GpsTraceList.DATALISTSORTTYPE.ELEVATOR)
        }
        setDataList()
    }

    /**
     * 項目クリック処理メニュー表示
     */
    fun itemClickMenu()  {
        klib.setMenuDialog(this, "操作メニュー", mItemClickMenu, iItemClickOperation)
    }

    //  項目クリック処理
    var iItemClickOperation = Consumer<String> { s ->
        val position = mSelectListPosition
        if (0 <= position) {
            if (s.compareTo(mItemClickMenu[0]) == 0) {
                val n = getItemPos2DataListPos(position)
                if (0 <= n)
                    goGpsCsvEdit(mGpsTraceList.mDataList[n].mFilePath)
            } else if (s.compareTo(mItemClickMenu[1]) == 0) {
                //  経路表示設定
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    mGpsTraceList.clearVisible()
                    mGpsTraceList.mDataList[n].mVisible = true
                    val coordinate = mGpsTraceList.mDataList[n].mLocArea.center().toString()
                    mapMove(coordinate)
                }
            } else if (s.compareTo(mItemClickMenu[2]) == 0) {
                //  位置移動
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    val coordinate = mGpsTraceList.mDataList[n].mLocArea.center().toString()
                    mapMove(coordinate)
                }
            } else if (s.compareTo(mItemClickMenu[3]) == 0) {
                //  グラフ表示
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    val gpxPath = mGpsTraceList.mDataList[n].mFilePath
                    val title = mGpsTraceList.mDataList[n].mTitle
                    goGpsGraph(gpxPath, title)
                }
            } else if (s.compareTo(mItemClickMenu[4]) == 0) {
                //  ゴミ箱
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    mGpsTraceList.setTrashData(n)
                    setSpinnerData()
                    setDataList()
                }
            } else if (s.compareTo(mItemClickMenu[5]) == 0) {
                //  GPXエクスポート
                gpxExport(position)
            }
        }
    }

    /**
     * 指定のデータをGPXファイルに変換してエクスポートする
     * pos          データの表示位置
     */
    fun gpxExport(pos: Int) {
        mSelectListPosition = getItemPos2DataListPos(pos)
        Log.d(TAG,"gpxExport: "+mSelectListPosition+" "+lvDataList.selectedItemPosition)
        klib.folderSelectDialog(this, mGpsTraceFileFolder, iGpsExportOperation)
    }

    //  指定のフォルダーにGPXファイルを出力
    var iGpsExportOperation = Consumer<String> { s ->
        if (0 <= mSelectListPosition) {
            var gpsTraceList = mutableListOf<GpsTraceList.GpsTraceData>()
            gpsTraceList.add(mGpsTraceList.mDataList[mSelectListPosition])
            GpsTraceList.gpxExport(gpsTraceList, s)
        }
    }

    /**
     * GPXリストの項目編集画面に移行する
     * gxpFilePath      編集する項目のファイルパス
     */
    fun goGpsCsvEdit(gpsTraceFilePath: String) {
        Log.d(TAG,"goGpsCsvEdit: "+gpsTraceFilePath)
        val intent = Intent(this, GpxEditActivity::class.java)
        intent.putExtra("GPSTRACELISTPATH", mGpsTraceListPath)
        intent.putExtra("GPSTRACEFILEPATH", gpsTraceFilePath)
        startActivityForResult(intent, REQUESTCODE_CSVEDIT)
    }

    /**
     * GPXファイルのグラフ表示画面に移行する
     * gpxPath          GPXファイルのパス
     * title            グラフのタイトル
     */
    fun goGpsGraph(gpxPath: String, title: String) {
        if (klib.existsFile(gpxPath)) {
            val intent = Intent(this, GpsGraph::class.java)
            intent.putExtra("FILE", gpxPath)
            intent.putExtra("TITLE", title)
            startActivity(intent)
        }
    }

    /**
     * 地図の中心を設定して地図表示に戻る
     * coordinate       位置座標文字列
     */
    fun mapMove(coordinate: String) {
        if (0 < coordinate.length) {
            Log.d(TAG,"mapMove: " + coordinate )
            if (0 < coordinate.length) {
                val intent = Intent()
                intent.putExtra("座標", coordinate)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    /**
     * listViewに表示している項目をDataListの位置に変換する
     * return       データ位置リスト(List<Int>)
     */
    fun getDispDataPos(): List<Int> {
        var dataList = mutableListOf<Int>()
        for (i in 0..lvDataList.size - 1) {
            dataList.add(getItemPos2DataListPos(i))
        }
        return dataList
    }

    /**
     * listViewのチェック項目をDataListの位置に変換する
     * return       データ位置リスト(List<Int>)
     */
    fun getSelectDataPos(): List<Int> {
        var dataList = mutableListOf<Int>()
        var visibleChecked = lvDataList.checkedItemPositions
        mGpsTraceList.clearVisible()
        for (i in 0..visibleChecked.size()-1){
            dataList.add(getItemPos2DataListPos(visibleChecked.keyAt(i)))
        }
        return dataList
    }

    /**
     * 表示タイトルから日時を抽出しリスト化
     * return       日時リスト
     */
    fun getDispFirstTimeStr(): List<String> {
        var dataList = mutableListOf<String>()
        for (i in 0..lvDataList.size - 1){
            val title = lvDataList.getItemAtPosition(i).toString()
            dataList.add(title.substring(1, 20))
        }
        return dataList
    }

    /**
     * 選択されている表示タイトルから日時を抽出しリスト化
     * return       日時リスト
     */
    fun getSelectFirstTimeStr(): List<String> {
        var dataList = mutableListOf<String>()
        var visibleChecked = lvDataList.checkedItemPositions
        for (i in 0..visibleChecked.size()-1){
            val title = lvDataList.getItemAtPosition(visibleChecked.keyAt(i)).toString()
            dataList.add(title.substring(1, 20))
        }
        return dataList
    }

    /**
     * ListViewのItemの位置をDataListの位置に変換(開始時間firstTimeを使う)
     * pos          ListViewのItemの位置
     * return       DataListの位置
     */
    fun getItemPos2DataListPos(pos: Int): Int {
        val title = lvDataList.getItemAtPosition(pos).toString()
        if (0 < title.length) {
            val firstTimeStr = title.substring(1, 20)
            return mGpsTraceList.findListTitleFirstTime(firstTimeStr)
        }
        return -1
    }

    /**
     * データ年、分類、グループデータをspinnerに登録
     */
    fun setSpinnerData(){
        //  データの年をspinnerに登録
        var yearAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpsTraceList.getYearList("すべて"))
        spYear.adapter = yearAdapter
        //  分類をspinnerに登録
        var categoryAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpsTraceList.getCategoryList("すべて"))
        spCategory.adapter = categoryAdapter
        //  グループをspinnerに登録
        var groupAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpsTraceList.getGroupList("すべて"))
        spGroup.adapter = groupAdapter

    }

    /**
     * 一覧リストを設定する
     */
    fun setDataList() {
        val year = spYear.selectedItem.toString()
        val category = spCategory.selectedItem.toString()
        val group = spGroup.selectedItem.toString()
        if (mSelectList) {
            //  選択リスト
            var listTitleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_checked,
                mGpsTraceList.getListTitleData(year, category, group))
            lvDataList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            lvDataList.adapter = listTitleAdapter
            //  visibleをcheckに設定
            lvDataList.clearChoices()
        } else {
            //  通常リスト
            var listTitleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                mGpsTraceList.getListTitleData(year, category, group))
            lvDataList.adapter = listTitleAdapter
        }
        title = "GPSトレースデータ(${lvDataList.adapter.count})"
    }
}