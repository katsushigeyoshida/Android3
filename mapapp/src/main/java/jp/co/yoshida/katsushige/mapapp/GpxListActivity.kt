package jp.co.yoshida.katsushige.mapapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.size
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityGpxListBinding
import jp.co.yoshida.katsushige.mylib.KLib

/**
 * 現在未使用 GpsTraceListActivityに統一
 */
class GpxListActivity : AppCompatActivity() {
    val TAG = "GpxListActivity"

    lateinit var binding: ActivityGpxListBinding
    lateinit var spYear: Spinner
    lateinit var spGroup: Spinner
    lateinit var spCategory: Spinner
    lateinit var btAdd: Button
    lateinit var btRemove: Button
    lateinit var btSort: Button
    lateinit var btDisp: Button
    lateinit var btSelect: Button
    lateinit var lvDataList: ListView

    val REQUESTCODE_GPXEDIT = 3
    val REQUESTCODE_GPXFILELIST = 4

    val mDispMenu = listOf<String>(
        "全表示", "全非表示", "反転表示"
    )
    val mRemoveMenu = listOf<String>(
        "全削除"
    )
    val mSortMenu = listOf<String>(
        "日付順", "タイトル順", "距離順", "標高順"
    )
    val mItemClickMenu = listOf<String>(
        "編集", "経路表示", "位置移動", "グラフ表示", "削除"
    )
    var mSelectListPosition = -1
    var mSelectList = false
    var mGpxDataList = GpsDataList()                    //  GPXファイルリスト
    var mGpxDataListPath = ""                           //  GPXファイルリストパス
    val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpx_list)
        title = "GPXファイルリスト"

        mGpxDataListPath = klib.getStrPreferences("GpxDataListPath", this).toString()
        mGpxDataList.mSaveFilePath = mGpxDataListPath
        mGpxDataList.loadDataFile(this)

        initControl()
        setDataList()
    }

    override fun onPause() {
        super.onPause()
        mGpxDataList.saveDataFile(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTCODE_GPXEDIT -> {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG,"onActivityResult: REQUESTCODE_GPXEDIT: ")
                    mGpxDataList.loadDataFile(this)
                    setSpinnerData()
                    setDataList()
                }
            }
        }
    }

    /**
     * コントロールの初期化
     */
    fun initControl() {
        binding = ActivityGpxListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spYear = binding.spinner4
        spGroup = binding.spinner5
        spCategory = binding.spinner6
        btAdd = binding.button11
        btRemove = binding.button12
        btSort = binding.button13
        btDisp = binding.button14
        btSelect = binding.button26
        lvDataList = binding.gpxFileListView

        setSpinnerData()

        //  [データ年]選択でのフィルタ処理
        spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        //  [分類]選択でのフィルタ処理
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        //  [グループ]選択でのフィルタ処理
        spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        //  [追加]ボタン
        btAdd.setOnClickListener {
            goGpxEdit("")
        }

        //  [削除]ボタン
        btRemove.setOnClickListener {
            if (mSelectList) {
                var Checked = lvDataList.checkedItemPositions
                for (i in 0..Checked.size()-1){
                    if (Checked.valueAt(i)) {
                        val n = getItemPos2DataListPos(Checked.keyAt(i))
                        if (0<= n)
                            mGpxDataList.mDataList.removeAt(n)
                    }
                }
                mSelectList = !mSelectList
                setDataList()
            } else {
                klib.setMenuDialog(this, "削除メニュー", mRemoveMenu, iRemoveOperatin)
            }
        }

        //  [ソート]ボタン
        btSort.setOnClickListener {
            klib.setMenuDialog(this, "ソートメニュー", mSortMenu, iSortOperatin)
        }

        //  [表示]ボタン
        btDisp.setOnClickListener {
            if (mSelectList) {
                var visibleChecked = lvDataList.checkedItemPositions
                Log.d(TAG, "setOnClickListener: "+visibleChecked)
                mGpxDataList.clearVisible()
                for (i in 0..visibleChecked.size()-1){
                    if (visibleChecked.valueAt(i)) {
                        val n = getItemPos2DataListPos(visibleChecked.keyAt(i))
                        Log.d(TAG, "setOnClickListener: "+i+" "+n)
                        if (0 <= n)
                            mGpxDataList.mDataList[n].mVisible = true
                    }
                }
                mSelectList = !mSelectList
                setDataList()
            } else {
                klib.setMenuDialog(this, "表示設定メニュー", mDispMenu, iDispOperatin)
            }
        }

        //  [選択]ボタン
        btSelect.setOnClickListener {
            mSelectList = !mSelectList
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

    /**
     * データ年、分類、グループデータをspinnerに登録
     */
    fun setSpinnerData(){
        //  データの年をspinnerに登録
        var yearAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpxDataList.getYearList("すべて"))
        spYear.adapter = yearAdapter
        //  分類をspinnerに登録
        var categoryAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpxDataList.getCategoryList("すべて"))
        spCategory.adapter = categoryAdapter
        //  グループをspinnerに登録
        var groupAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mGpxDataList.getGroupList("すべて"))
        spGroup.adapter = groupAdapter
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
                //  編集
                val n = getItemPos2DataListPos(position)
                if (0 <= n)
                    goGpxEdit(mGpxDataList.mDataList[n].mFilePath)
            } else if (s.compareTo(mItemClickMenu[1]) == 0) {
                //  経路表示
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    mGpxDataList.clearVisible()
                    mGpxDataList.mDataList[n].mVisible = true
                    val coordinate = mGpxDataList.mDataList[n].mLocArea.center().toString()
                    mapMove(coordinate)
                }
            } else if (s.compareTo(mItemClickMenu[2]) == 0) {
                //  位置移動
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    val coordinate = mGpxDataList.mDataList[n].mLocArea.center().toString()
                    mapMove(coordinate)
                }
            } else if (s.compareTo(mItemClickMenu[3]) == 0) {
                //  グラフ表示
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    val gpxPath = mGpxDataList.mDataList[n].mFilePath
                    val title = mGpxDataList.mDataList[n].mTitle
                    goGpsGraph(gpxPath, title)
                }
            } else if (s.compareTo(mItemClickMenu[4]) == 0) {
                //  削除
                val n = getItemPos2DataListPos(position)
                if (0 <= n) {
                    mGpxDataList.mDataList.removeAt(n)
                    setDataList()
                }
            }
        }
    }

    //  ソートの設定
    var iSortOperatin = Consumer<String> { s ->
        if (s.compareTo(mSortMenu[0])== 0) {
            //  日付順
            mGpxDataList.setDataListSortType(GpsDataList.DATALISTSORTTYPE.DATE)
        } else if (s.compareTo(mSortMenu[1])== 0) {
            //  タイトル順
            mGpxDataList.setDataListSortType(GpsDataList.DATALISTSORTTYPE.TITLE)
        } else if (s.compareTo(mSortMenu[2])== 0) {
            //  距離順
            mGpxDataList.setDataListSortType(GpsDataList.DATALISTSORTTYPE.DISTANCE)
        } else if (s.compareTo(mSortMenu[3])== 0) {
            //  標高順
            mGpxDataList.setDataListSortType(GpsDataList.DATALISTSORTTYPE.ELEVATOR)
        }
        setDataList()
    }

    //  削除処理
    var iRemoveOperatin = Consumer<String> { s ->
        if (s.compareTo(mRemoveMenu[0])== 0) {
            //  表示データを全削除
            for (i in 0..lvDataList.size-1) {
                val n = getItemPos2DataListPos(i)
                mGpxDataList.removeListData(n)
            }
        }
        setDataList()
    }

    //  GPXトレースの表示処理
    var iDispOperatin = Consumer<String> { s ->
        if (s.compareTo(mDispMenu[0])== 0) {
            //  全表示
            mGpxDataList.setAllVisible()
        } else if (s.compareTo(mDispMenu[1])== 0) {
            //  全非表示
            mGpxDataList.clearVisible()
        } else if (s.compareTo(mDispMenu[2])== 0) {
            //  反転
            mGpxDataList.reverseVisible()
        } else
            return@Consumer
        setDataList()
    }

    /**
     * GPXリストの項目編集画面に移行する
     * gxpFilePath      編集する項目のファイルパス
     */
    fun goGpxEdit(gpxFilePath: String) {
        Log.d(TAG,"goGpxEdit: "+gpxFilePath)
        val intent = Intent(this, GpxEditActivity::class.java)
        intent.putExtra("GPXFILEPATH", gpxFilePath)
        startActivityForResult(intent, REQUESTCODE_GPXEDIT)
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
     * ListViewのItemの位置をDataListの位置に変換
     * pos          ListViewのItemの位置
     * return       DataListの位置
     */
    fun getItemPos2DataListPos(pos: Int): Int {
        val title = lvDataList.getItemAtPosition(pos).toString()
        Log.d(TAG, "getItemPos2DataListPos: "+title)
        if (0 < title.length) {
            val firstTimeStr = title.substring(1, 20)
            Log.d(TAG, "getItemPos2DataListPos: "+firstTimeStr)
            return mGpxDataList.findListTitleFirstTime(firstTimeStr)
        }
        return -1
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
                mGpxDataList.getListTitleData(year, category, group))
            lvDataList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            lvDataList.adapter = listTitleAdapter
            //  visibleをcheckに設定
            lvDataList.clearChoices()
//            for (i in mGpxDataList.mDataList.indices) {
//                if (mGpxDataList.mDataList[i].mVisible) {
//                    lvDataList.setItemChecked(i, true)
//                }
//            }
        } else {
            //  通常リスト
            var listTitleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                mGpxDataList.getListTitleData(year, category, group))
            lvDataList.adapter = listTitleAdapter
        }
    }
}