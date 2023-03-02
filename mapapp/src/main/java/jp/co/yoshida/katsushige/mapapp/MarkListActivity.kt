package jp.co.yoshida.katsushige.mapapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import jp.co.yoshida.katsushige.mapapp.databinding.ActivityMarkListBinding
import jp.co.yoshida.katsushige.mylib.PointD

class MarkListActivity : AppCompatActivity() {
    val TAG = "MarkListActivity"

    lateinit var binding: ActivityMarkListBinding
    lateinit var spGroup: Spinner
    lateinit var spMarkType : Spinner
    lateinit var lvMarkList: ListView

    val REQUESTCODE_MARKEDIT = 7

    var mMarkListPath = ""
    val mMarkList = MarkList()
    var mSelectList = false
    var mSelectListPosition = 0
    var mMapCemter = PointD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_list)

        title = "マークリスト"
        mMarkListPath = intent.getStringExtra("MARKLISTPATH").toString()
        mMapCemter.x = intent.getStringExtra("MAPCENTER_X").toString().toDouble()
        mMapCemter.y = intent.getStringExtra("MAPCENTER_Y").toString().toDouble()
        Log.d(TAG,"onCreate: "+mMapCemter.toString()+" "+mMarkListPath)
        mMarkList.mSaveFilePath = mMarkListPath
        mMarkList.mCenter = mMapCemter
        mMarkList.loadMarkFile(this)

        initControl()
        setSpinnerData()
        setDataList()
    }

    override fun onPause() {
        super.onPause()
        mMarkList.saveMarkFile(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTCODE_MARKEDIT -> {
                if (resultCode == RESULT_OK) {
                }
            }
        }
    }

    fun initControl() {
        binding = ActivityMarkListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spGroup = binding.spinner12
        spMarkType = binding.spinner13
        lvMarkList =binding.markListView

        //  [グループ]選択でのフィルタ処理
        spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setDataList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        lvMarkList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectListPosition = position
                if (mSelectList)
                    return
            }
        }

    }

    fun setSpinnerData(){
        //  選択値の取得
        val groupItem = if (spGroup.adapter == null) mMarkList.mAllListName
                        else spGroup.selectedItem.toString()

        //  グループをspinnerに登録
        spGroup.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            mMarkList.getGroupList(mMarkList.mAllListName))

        //  選択値を元に戻す
        val groupPos = mMarkList.getGroupList(mMarkList.mAllListName).indexOf(groupItem)
        if (0 <= groupPos)
            spGroup.setSelection(groupPos)
    }

    /**
     * 一覧リストを設定する
     */
    fun setDataList() {
        val group = ""//spGroup.selectedItem.toString()
//        val markType = spMarkType.selectedItem.toString()

        if (mSelectList) {
            //  選択リスト
            var listTitleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_checked,
                mMarkList.getTitleList(group))
            lvMarkList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            lvMarkList.adapter = listTitleAdapter
            //  visibleをcheckに設定
            lvMarkList.clearChoices()
        } else {
            //  通常リスト
            lvMarkList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                mMarkList.getTitleList(group))
        }
        title = "マークリスト(${lvMarkList.adapter.count})"
    }
}