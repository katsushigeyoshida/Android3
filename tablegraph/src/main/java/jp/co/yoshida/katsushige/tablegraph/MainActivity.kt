package jp.co.yoshida.katsushige.tablegraph

//import androidx.core.view.isVisible
//import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import jp.co.yoshida.katsushige.mylib.DownLoadWebFile
import jp.co.yoshida.katsushige.mylib.KLib
import jp.co.yoshida.katsushige.mylib.PointD
import jp.co.yoshida.katsushige.tablegraph.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var mTableView: TableSheetView         //  表をグラフィック表示するview
    private var mDataList = mutableListOf<SheetData>()      //  表データのリスト
    private val mAddressDataList = AddressDataList()        //  オープンデータのアドレスリスト
    private val mColSelect = mutableListOf(-1, -1, -1)      //  列タイトル選択の列番号(パラメータとして使う)
    private val mColSelectColor = listOf("Red", "Green", "Yellow")
    private var mAddressListName = "SheetDataAddress.csv"   //  データのアドレスリストファイル名
    private var mTempDataFilePath = "TempData.csv"          //  一時保存ファイル名・パス
    private var mExpressFilePath = "CalcExpress.csv"        //  計算式リストパス
    private var mOutputFolder = "Download"                  //  ダウンロードフォルダ名
    private var mDataTitle = ""                             //  表データ名
    private val mEncodeLis = listOf("UTF8", "SJIS", "EUC")
    private val mDataGetMenu = listOf(
        "ダウンロードファイルの済みファイル読込", "ファイル選択読込", "既存データ削除"
    )
    private val mMenuList = listOf(
        "集計処理","指定列でフィルタリング", "行単位での数式処理", "縦横変換",
        "日付変換", "全角英数半角変換",
        "列間削除", "列間移動", "列結合", "選択行結合", "選択行削除",
        "一つ戻る", "グラフ化", "CSV保存"
    )
    private val mDateMenu = listOf("年週表示", "年月表示", "年表示")
    private val mMoveDownMenu = listOf("最下行へ移動", "中間行に移動", "1行下へ")
    private val mMoveUpMenu = listOf("最上行へ移動", "中間行に移動", "1行上へ")
    private val mMoveRightMenu = listOf("右端へ移動", "中間列に移動", "1列右に移動")
    private val mMoveLeftMenu = listOf("左端へ移動", "中間列に移動", "1列左に移動")
    private var mTableViewTop = 616                           //  ViewのTOP位置(マウス位置のオフセット)

    private val klib = KLib()

    private lateinit var binding: ActivityMainBinding
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var llTableSheet: LinearLayout
    private lateinit var etDataTitle: EditText
    private lateinit var etDataAddress: EditText
    private lateinit var spDataEncode: Spinner
    private lateinit var etDataComment: EditText
    private lateinit var etDataReference: EditText
    private lateinit var btColSelect1: ImageButton
    private lateinit var btColSelect2: ImageButton
    private lateinit var btColSelect3: ImageButton
    private lateinit var btDataSelect: Button
    private lateinit var btDataGet: Button
    private lateinit var btDataReference: Button
    private lateinit var btZoomUp: ImageButton
    private lateinit var btZoomDown: ImageButton
    private lateinit var btMoveUp: ImageButton
    private lateinit var btMoveDown: ImageButton
    private lateinit var btMoveLeft: ImageButton
    private lateinit var btMoveRight: ImageButton
    private lateinit var btMenu: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = this.resources.getString(R.string.app_name)
        //  パーミッションチェック
//        klib.checkStragePermission(this)
        chkFileAccessPermission()   //  ファイルアクセスのパーミッション設定
        //  グラフィック表
        mTableView = TableSheetView(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constraintLayout
        llTableSheet = binding.llTableSheet
        etDataTitle = binding.etDataTitle
        etDataAddress = binding.etDataAddress
        spDataEncode = binding.spDataEncode
        etDataComment = binding.etDataComment
        etDataReference = binding.etDataReference
        btColSelect1 = binding.btColSelect1
        btColSelect2 = binding.btColSelect2
        btColSelect3 = binding.btColSelect3
        btDataSelect = binding.btDataSelect
        btDataGet = binding.btDataGet
        btDataReference = binding.btDataReference
        btZoomUp = binding.btZoomUp
        btZoomDown = binding.btZoomDown
        btMoveUp = binding.btMoveUp
        btMoveDown = binding.btMoveDown
        btMoveLeft = binding.btMoveLeft
        btMoveRight = binding.btMoveRight
        btMenu = binding.btMenu

        llTableSheet.addView(mTableView)
        //  初期化
        init()
    }


    /**
     *  ファイルアクセスのパーミッションチェック
     */
    fun chkFileAccessPermission(){
        if (30<= Build.VERSION.SDK_INT)
            chkManageAllFilesAccess()
        else
            klib.checkStragePermission(this)
    }

    /**
     *  MANAGE_ALL_FILES_ACCESS_PERMISSIONの確認(Android11 API30以上)
     */
    fun chkManageAllFilesAccess() {
        var file = File("/storage/emulated/0/chkManageAllFilesAccess.txt")
        Log.d(TAG,"chkManageAllFilesAccess:")
        try {
            if (file.createNewFile()) {
                Log.d(TAG,"chkManageAllFilesAccess: create " + "OK")
            }
        } catch (e: Exception) {
            Log.d(TAG,"chkManageAllFilesAccess: create " + "NG")
            val intent = Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION")
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //  マウス位置をMapViewに合わせるためのオフセット値
        Log.d(TAG,"onWindowFocusChanged: "+
                llTableSheet.top + " "+ llTableSheet.left + " "+
                llTableSheet.bottom + " "+ llTableSheet.right + " "+
                getWindowHeight()+" "+
                constraintLayout.height+" "+constraintLayout.width)

        if (constraintLayout.width < 1100) {
            btColSelect1.isVisible = false
            btColSelect2.isVisible = false
            btColSelect3.isVisible = false
        }
        //  TableViewのタッチ位置の縦座標の起点を求める
        mTableViewTop = getWindowHeight() - constraintLayout.height + llTableSheet.top
    }

    //  タッチ位置の前回値
    private var mPreTouchPosition = PointD(0.0, 0.0)
    private var mPreTouchDistance = 0.0
    private var mZoomOn = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pos = PointD(event.x.toDouble(), (event.y - mTableViewTop).toDouble())
        var pointCount = event.pointerCount
        Log.d(TAG, "onTouchEvent: "+event.action+" "+pos)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {    //  (0)
                mPreTouchPosition = pos
                mZoomOn = false
            }
            MotionEvent.ACTION_UP -> {
//                mTableView.setTitleColor(0, "Cyan")
                if (!mZoomOn) {
                    val dis = mTableView.offsetColRow(pos, mPreTouchPosition)
                    if (dis.x == 0 && dis.y == 0) {
                        //  タッチ位置から表の行列を求める
                        val row = mTableView.getRowPosition(pos)
                        val col = mTableView.getColPosition(pos)
                        Log.d(TAG, "onTouchEvent: "+row+" "+col)
                        //  選択されたセルの背景色を変更
                        if (col == 0 && 0 <= row)       //  行タイトル(1列目)の選択色を反転
                            mTableView.mSheetData.mRowSelectList[row]= !mTableView.mSheetData.mRowSelectList[row]
                        if (row == 0 && 0 <= col)       //  列タイトル(1行目)の葉生食変更
                            setSelectTitleColor(col)
                        if (0 < row && 0 < col)         //  タイトル以外はパス
                            return true
                    } else {
                        mTableView.setColOffset(dis.x)
                        mTableView.setRowOffset(dis.y)
                    }
                    mTableView.reDraw()
                }
            }
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_POINTER_DOWN -> {

            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
        }

        return true
//        return super.onTouchEvent(event)
    }

    /**
     * 初期化処理
     */
    private fun init() {
        mAddressListName = klib.getPackageNameDirectory(this) + "/" + mAddressListName
        mTempDataFilePath = klib.getPackageNameDirectory(this) + "/" + mTempDataFilePath
        mExpressFilePath = klib.getPackageNameDirectory(this) + "/" + mExpressFilePath
        mOutputFolder = klib.getPackageNameDirectory(this) + "/" + mOutputFolder
        if (!klib.mkdir(mOutputFolder))
            Toast.makeText(this, "データ保存フォルダが作成できません", Toast.LENGTH_LONG).show()
        mAddressDataList.loadData(mAddressListName)

        etDataTitle.setText("")
        etDataAddress.setText("")
        spDataEncode.setSelection(0)
        etDataComment.setText("")
        etDataReference.setText("")

        val encodeListAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mEncodeLis)
        spDataEncode.adapter = encodeListAdapter

        /**
         * 登録データ選択ボタン
         */
        btDataSelect.setOnClickListener {
            klib.setMenuDialog(this, "データ取得先選択", mAddressDataList.getTitleList(), iAddressSelectOperation)
        }

        /**
         * データの登録、ダウンロード、読込ボタン
         */
        btDataGet.setOnClickListener {
            if (etDataTitle.text.isNotEmpty()) {
                val data = DataAddress(
                    etDataTitle.text.toString(),
                    etDataAddress.text.toString().trim(),
                    ENCODE.fromOrdinal(spDataEncode.selectedItemPosition),
                    etDataComment.text.toString(),
                    etDataReference.text.toString().trim(),
                )
                mAddressDataList.add(data)
                Log.d(TAG, "btDataGet.setOnClickListener: "+data.mTitle+" "+mAddressListName)
                mAddressDataList.saveData(mAddressListName)
                Toast.makeText(this, "ダウンロード開始", Toast.LENGTH_LONG).show()
                val downLoadFile = dataDownLoad(data.mAddress)
                if (0 < downLoadFile.length) {
                    Toast.makeText(this, "データ読込中", Toast.LENGTH_LONG).show()
                    mDataTitle = data.mTitle
                    dataLoadDisp(downLoadFile, spDataEncode.selectedItemPosition)
                }
            }
            initSelectTitle()
        }

        /**
         * ダウンロードファイル読込ボタン(ロングクリック)
         */
        btDataGet.setOnLongClickListener {
            klib.setMenuDialog(this, "メニュー選択", mDataGetMenu, iDataGetMenuOperation)
            true
        }

        /**
         * 参照ファイル/Webを開く
         */
        btDataReference.setOnClickListener {
            val file = etDataReference.text.toString()
            if (file.indexOf("http") == 0) {
                klib.webDisp(this, file)
            } else {
                klib.executeFile(this, file)
            }
        }

        /**
         * 拡大ボタン
         */
        btZoomUp.setOnClickListener {
            mTableView.mTextSize *= 1.2
            mTableView.reDraw()
        }

        /**
         * 縮小ボタン
         */
        btZoomDown.setOnClickListener {
            mTableView.mTextSize /= 1.2
            mTableView.reDraw()
        }

        /**
         * 上移動ボタン
         */
        btMoveUp.setOnClickListener {
            mTableView.setRowOffset(-(mTableView.dispRowCount() - 1))
            mTableView.reDraw()
        }

        /**
         * 上移動メニュー表示ボタン(ロングクリック)
         */
        btMoveUp.setOnLongClickListener {
            klib.setMenuDialog(this, "上下移動", mMoveUpMenu, iMoveUpSelectOperation)
            true
        }

        /**
         * 下移動ボタン
         */
        btMoveDown.setOnClickListener {
            mTableView.setRowOffset(mTableView.dispRowCount() - 1)
            mTableView.reDraw()
        }

        /**
         * 下移動メニュー表示ボタン(ロングクリック)
         */
        btMoveDown.setOnLongClickListener {
            klib.setMenuDialog(this, "上下移動", mMoveDownMenu, iMoveDownSelectOperation)
            true
        }

        /**
         * 右移動ボタン
         */
        btMoveRight.setOnClickListener {
            mTableView.setColOffset(mTableView.dispColCount() - 1)
            mTableView.reDraw()
        }

        /**
         * 右移動メニュー表示ボタン(ロングクリック)
         */
        btMoveRight.setOnLongClickListener {
            klib.setMenuDialog(this, "右移動", mMoveRightMenu, iMoveRightSelectOperation)
            true
        }

        /**
         * 左移動ボタン
         */
        btMoveLeft.setOnClickListener {
            val colLeft = -mTableView.dispColCount() + 1
            mTableView.setColOffset(if (colLeft == 0) -1 else colLeft)
            mTableView.reDraw()
        }

        /**
         * 左移動メニュー表示ボタン(ロングクリック)
         */
        btMoveLeft.setOnLongClickListener {
            klib.setMenuDialog(this, "左移動", mMoveLeftMenu, iMoveLeftSelectOperation)
            true
        }

        /**
         * 列選択ボタン(第1パラメータ)
         */
        btColSelect1.setOnClickListener {
            klib.setMenuIntDialog(this, "列タイトル", mTableView.mSheetData.getTitleList(), iCol0Select)
        }

        /**
         * 列選択ボタン(第2パラメータ)
         */
        btColSelect2.setOnClickListener {
            klib.setMenuIntDialog(this, "列タイトル", mTableView.mSheetData.getTitleList(), iCol1Select)
        }

        /**
         * 列選択ボタン(第3パラメータ)
         */
        btColSelect3.setOnClickListener {
            klib.setMenuIntDialog(this, "列タイトル", mTableView.mSheetData.getTitleList(), iCol2Select)
        }

        /**
         * /.メニューボタン
         */
        btMenu.setOnClickListener {
            klib.setMenuDialog(this, "メニュー選択", mMenuList, iMenuSelectOperation)
        }

    }

    //  データ読込メニューの実行
    private var iDataGetMenuOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mDataGetMenu.indexOf(s)) {
            0 -> {      //  ダウンロード済みファイルの読込
                if (0 < etDataAddress.text.length) {
                    Toast.makeText(this, "ダウンロード済みファイルの読込", Toast.LENGTH_LONG).show()
                    val downLoadFile = getDownLoadPath(etDataAddress.text.toString())
                    if (klib.existsFile(downLoadFile)) {
                        mDataTitle = etDataTitle.text.toString()
                        dataLoadDisp(downLoadFile, spDataEncode.selectedItemPosition)
                    } else {
                        Toast.makeText(this, "ファイルがありません", Toast.LENGTH_LONG).show()
                    }
                }
            }
            1 -> {      //  ファイル選択読込
                klib.fileSelectDialog(this, klib.getPackageNameDirectory(this), "*.csv", true, iFilePath)
            }
            2 -> {      //  既存ファイル削除
                if (0 < etDataAddress.text.length) {
                    Toast.makeText(this, "既存ファイルの削除", Toast.LENGTH_LONG).show()
                    val downLoadFile = getDownLoadPath(etDataAddress.text.toString())
                    if (klib.existsFile(downLoadFile)) {
                        klib.removeFile(downLoadFile)
                    }
                }
            }
        }
        initSelectTitle()
    }

    //  ファイル選択でのデータ読出し
    private var iFilePath = Consumer<String> { s ->
        if (0 < s.length) {
            Toast.makeText(this, "選択ファイルの読込", Toast.LENGTH_LONG).show()
            etDataTitle.setText("")
            etDataAddress.setText(s)
            spDataEncode.setSelection(0)
            etDataComment.setText("")
            etDataReference.setText("")
            mDataTitle = klib.getFileNameWithoutExtension(s)
            dataLoadDisp(s, spDataEncode.selectedItemPosition)
        }
    }


    //  登録データを選択して画面に反映
    private var iAddressSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        Log.d(TAG, "iAddressSelectOperation: "+s)
        val n = mAddressDataList.searchTitle(s)
        if (0 <= n) {
            etDataTitle.setText(mAddressDataList.mDataList[n].mTitle)
            etDataAddress.setText(mAddressDataList.mDataList[n].mAddress)
            spDataEncode.setSelection(mAddressDataList.mDataList[n].mEncode.ordinal)
            etDataComment.setText(mAddressDataList.mDataList[n].mComment)
            etDataReference.setText(mAddressDataList.mDataList[n].mReference)
        }
    }

    //  表データ操作の処理
    private var iMenuSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        Log.d(TAG, "iMenuSelectOperation: "+s)
        val dataSize = mDataList.size
        when (mMenuList.indexOf(s)) {
            0 -> {      //  集計処理
                Log.d(TAG, "iMenuSelectOperation: "+mColSelect[0]+" "+mColSelect[1]+" "+mColSelect[2])
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.pivotTable(mColSelect[0], mColSelect[1], mColSelect[2])
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            1 -> {      //  指定列でフィルタリング
                colFiltering()
            }
            2 -> {      //  行単位での数式処理
                calcExpress()
            }
            3 -> {      //  縦横変換
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.transposeData()
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            4 ->{       //  日付変換
                dateConvert()
            }
            5 -> {      //  全角数値半角変換
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.zenNum2HanConvert()
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            6 -> {      //  列削除
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.colRemove(mColSelect[0], mColSelect[1])
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            7 -> {      //  列移動
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.colMove(mColSelect[0], mColSelect[1], mColSelect[2])
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            8 -> {      //  列結合
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
                sheetData.colCombine(mColSelect[0], mColSelect[1])
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            9 -> {      //  選択行結合
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList,
                    mDataList[mDataList.lastIndex].mRowSelectList)
                sheetData.rowCombaine()
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            10 -> {      //  選択行削除
                val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList,
                    mDataList[mDataList.lastIndex].mRowSelectList)
                sheetData.removeRowData()
                mDataList.add(sheetData)
                tableDisp(dataSize)
            }
            11 -> {      //  一つ戻る
                if (1 < mDataList.size) {
                    mDataList.removeLast()
                    mTableView.mSheetData.initRowSelectList()
                    tableDataDisp(dataSize)
                }
            }
            12 -> {      //  グラフ化
                mDataList[mDataList.lastIndex].saveData(mTempDataFilePath)
                val intent = Intent(this, TableGraphActivity::class.java)
                intent.putExtra("FILE", mTempDataFilePath)
                intent.putExtra("TITLE", mDataTitle)
                startActivity(intent)
            }
            13 -> {     //  CSV保存
                klib.saveFileSelectDialog(this, klib.getPackageNameDirectory(this), "*.csv", true, iSaveFilePath)
            }
        }
    }

    /**
     * 指定列でフィルタリング
     */
    private fun colFiltering() {
        val titleList = mTableView.mSheetData.getTitleList(mColSelect[0])
        if (titleList.isEmpty())
            return
        val chkList = mutableListOf<Boolean>()
        for (i in titleList.indices)
            chkList.add(false)
        klib.setChkMenuDialog(this, "表示データリスト", titleList.toTypedArray(), chkList.toBooleanArray(), iColFiltering)
    }

    private var iColFiltering = Consumer<BooleanArray> { s ->
        val dataSize = mDataList.size
        val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)  //  コピー作成
        sheetData.filterDataList(mColSelect[0], s.toList())
        mDataList.add(sheetData)
        tableDisp(dataSize)
    }

    /**
     * 行単位での演算処理
     */
    private fun calcExpress() {
        val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
        val expressList = ExpressList(mExpressFilePath,sheetData)
        expressList.loadData()
        expressList.inputDialog(this, "数式設定", iExpressSetOperation)
    }

    private var iExpressSetOperation = Consumer<String> { s ->
        val data = klib.splitCsvString(s)
        Log.d(TAG, "iExpressSetOperation: "+data[0]+" "+data[1])
        val dataSize = mDataList.size
        val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)  //  コピー作成
        sheetData.calcExpressData (data[1], data[0])
        mDataList.add(sheetData)
        tableDisp(dataSize)
    }

    /**
     * 日付変換
     * 変換タイプを選択して行う
     */
    private fun dateConvert() {
        klib.setMenuDialog(this, "日付の種類", mDateMenu, iDateConvert)
    }

    //  日付変換
    private var iDateConvert = Consumer<String> { s ->
        val type = when (mDateMenu.indexOf(s)) {
            0 -> 12         //  年週
            1 -> 6          //  年月
            2 -> 8          //  年
            else -> 0       //  yyyy/mm/dd
        }
        val dataSize = mDataList.size
        val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
        sheetData.dateConvert(mColSelect[0], type)
        mDataList.add(sheetData)
        tableDisp(dataSize)
    }

    //  指定ファイルをCSV形式で保存
    private var iSaveFilePath = Consumer<String> { s ->
        if (s.isNotEmpty()) {
            Toast.makeText(this, "保存 " + s, Toast.LENGTH_LONG).show()
            val sheetData = SheetData(mDataList[mDataList.lastIndex].mDataList)
            sheetData.saveData(s)
        }
    }

    /**
     * 表データの再表示と選択行列の初期化
     * dataSize     シートデータの保管数
     */
    private fun tableDisp(dataSize: Int) {
        mTableView.mSheetData.initRowSelectList()
        initSelectTitle()
        tableDataDisp(dataSize)
    }

    /**
     * 表データの再表示
     * 保管データ数に変化があれば表示する
     * dataSize     シートデータの保管数
     */
    private fun tableDataDisp(dataSize: Int) {
        if (dataSize != mDataList.size) {
            //  データの再表示
            mTableView.setSheetData(mDataList[mDataList.lastIndex])
            mTableView.setRowOffset(-mTableView.mRowOffset)
            mTableView.reDraw()
        }
    }

    //  上側への列移動のメニュー処理
    private var iMoveUpSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mMoveUpMenu.indexOf(s)) {
            0 -> {      //  最上行に移動
                mTableView.setRowOffset(-mTableView.mRowOffset)
            }
            1 -> {      //  中間行に移動
                mTableView.setRowOffset(-mTableView.mRowOffset / 2)
            }
            2 -> {      //  1行上に
                mTableView.setRowOffset(-1)
            }
        }
        mTableView.reDraw()
    }

    //  下側への列移動のメニュー処理
    private var iMoveDownSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mMoveDownMenu.indexOf(s)) {
            0 -> {      //  最下行に移動
                val offset = mTableView.mRowMaxOffset - mTableView.mRowOffset + 1
                mTableView.setRowOffset(offset)
            }
            1 -> {      //  中間行に移動
                val offset = mTableView.mRowMaxOffset - mTableView.mRowOffset
                mTableView.setRowOffset(offset / 2)
            }
            2 -> {      //  1行下に
                mTableView.setRowOffset(1)
            }
        }
        mTableView.reDraw()
    }

    //  右側への列移動のメニュー処理
    private var iMoveRightSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mMoveRightMenu.indexOf(s)) {
            0 -> {      //  右端へ移動
                val offset = mTableView.mColMaxOffset - mTableView.mColOffset
                mTableView.setColOffset(offset)
            }
            1 -> {      //  右中間列に移動
                val offset = mTableView.mColMaxOffset - mTableView.mColOffset
                mTableView.setColOffset(offset / 2)
            }
            2 -> {      //  1列右に移動
                mTableView.setColOffset(1)
            }
        }
        mTableView.reDraw()
    }

    //  左側への列移動のメニュー処理
    private var iMoveLeftSelectOperation = Consumer<String> { s ->
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
        when (mMoveLeftMenu.indexOf(s)) {
            0 -> {      //  左端へ移動
                mTableView.setColOffset(-mTableView.mColOffset)
            }
            1 -> {      //  左中間列に移動
                mTableView.setColOffset( -mTableView.mColOffset / 2)
            }
            2 -> {      //  1列左に移動
                mTableView.setColOffset(-1)
            }
        }
        mTableView.reDraw()
    }

    //  集計処理などの第1パラメータとして列選択
    private var iCol0Select = Consumer<Int> { s ->
        Toast.makeText(this, mTableView.mSheetData.mDataList[0][s], Toast.LENGTH_LONG).show()
        selectTitleColorSet(0, s)
        mTableView.reDraw()
    }

    //  集計処理などの第2パラメータとして列選択
    private var iCol1Select = Consumer<Int> { s ->
        Toast.makeText(this, mTableView.mSheetData.mDataList[0][s], Toast.LENGTH_LONG).show()
        selectTitleColorSet(1, s)
        mTableView.reDraw()
    }

    //  集計処理などの第3パラメータとして列選択
    private var iCol2Select = Consumer<Int> { s ->
        Toast.makeText(this, mTableView.mSheetData.mDataList[0][s], Toast.LENGTH_LONG).show()
        selectTitleColorSet(2, s)
        mTableView.reDraw()
    }

    /**
     * 列パラメータ(列タイトル)の選択処理
     * icol     パラメータの種別(first/second/third)
     * s        選択列
     */
    private fun selectTitleColorSet(icol: Int, s: Int) {
        selectTitleColorClear()
        if (mColSelect[icol] == s) {
            mColSelect[icol] = -1
        } else {
            for (col in 0..mColSelect.lastIndex) {
                if (mColSelect[col] == s)
                    mColSelect[col] = -1
            }
            mColSelect[icol] = s
        }
        for (col in 0..mColSelect.lastIndex) {
            if(0 <= mColSelect[col])
                mTableView.setTitleColor(mColSelect[col], mColSelectColor[col])
        }
    }

    /**
     * 列パラメータの選択リスト/カラーの初期化
     */
    private fun selectTitleColorClear() {
        for (col in mColSelect) {
            if(0 <= col)
                mTableView.setTitleColor(col, "White")
        }
    }

    /**
     * 列選択処理
     * 列選択は3つまででクリックした順にRed,Green,Yellowにタイトルの背景色を設定する
     * 設定済みの列を選択した場合には選択を解除する
     * col      列番号
     */
    private fun setSelectTitleColor(col: Int) {
        if (col < 0 || mTableView.mCellTitleColor.size <= col)
            return
        if (mTableView.mCellTitleColor[col].compareTo("White") == 0) {
            if (mColSelect[0] < 0) {
                mColSelect[0] = col
                mTableView.setTitleColor(col, "Red")
            } else if (mColSelect[1] < 0) {
                mColSelect[1] = col
                mTableView.setTitleColor(col, "Green")
            } else if (mColSelect[2] < 0) {
                mColSelect[2] = col
                mTableView.setTitleColor(col, "Yellow")
            }
        } else {
            mTableView.setTitleColor(col, "White")
            for (i in mColSelect.indices) {
                if (mColSelect[i] == col)
                    mColSelect[i] = -1
            }
        }
        Log.d(TAG,"setSelectTitleColor: "+mColSelect[0]+" "+mColSelect[1]+" "+mColSelect[2])
    }

    /**
     * 列選択リストを初期化
     */
    private fun initSelectTitle() {
        if (0 < mTableView.mSheetData.mDataList.size) {
            mTableView.initTitleColor()
            for (i in mColSelect.indices) {
                mColSelect[i] = -1
            }
        }
    }

    /**
     * ダウンロードしたファイルを表形式で表示
     * downLoadFile     ダウンロードファイルパス
     */
    private fun dataLoadDisp(downLoadFile: String, encode: Int) {
        if (klib.existsFile(downLoadFile)) {
            val sheetData = SheetData()
            sheetData.loadData(downLoadFile, encode)
            mDataList.add(sheetData)
            mTableView.setSheetData(mDataList[mDataList.lastIndex])
            mTableView.reDraw()
//            //  グラフ受け渡し用に一時保存
//            GlobalScope.launch {
//                mDataList[mDataList.size - 1].saveData(mTempDataFilePath)
//            }
        }
    }

    /**
     * Webファイルのダウロード
     * address      Web Address
     * return       保存ファイルパス
     */
    private fun dataDownLoad(address: String): String {
        if (0 == address.indexOf("http")) {
            val downLoadFile = File(getDownLoadPath(address))
            Log.d(TAG, "dataDownLoad: "+address+" "+downLoadFile.path)
            if (downLoadFile.exists())
                downLoadFile.delete()
            val downLoad = DownLoadWebFile(address, downLoadFile.path)
            downLoad.start()
            while (downLoad.isAlive()) {
                Thread.sleep(100L)
            }
            if (downLoadFile.exists()) {
                Log.d(TAG, "dataDownLoad: OK "+downLoadFile.path)
                return downLoadFile.path
            } else {
                Log.d(TAG, "dataDownLoad: ERROR "+downLoadFile.path)
                return ""
            }
        }
        return address
    }

    /**
     * ダウンロード先のファイルパスの取得
     * return   ファイルパス
     */
    private fun getDownLoadPath(address: String): String {
        if (0 == address.indexOf("http")) {
            return mOutputFolder + "/" + address.substring(address.lastIndexOf("/") + 1)
        } else {
            return address
        }
    }

    /**
     * 画面の高さ(下記ボタンを含まない)
     */
    private fun getWindowHeight(): Int {
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        return display.height
    }
}