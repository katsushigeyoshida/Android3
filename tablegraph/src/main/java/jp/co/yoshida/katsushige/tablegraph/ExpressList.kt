package jp.co.yoshida.katsushige.tablegraph

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Consumer
import jp.co.yoshida.katsushige.mylib.KLib

/**
 * 行単位の数式処理を行うための数式入力を行うダイヤログ
 * 一度入力した数式はファイルにリスト保存し、再使用可能とした
 * 入力  タイトル  TextView
 *      数式      EditTex
 *      数式選択   Spinner
 *      カラム選択  Spinner
 *      Delete/Cancel/OK
 */
class ExpressList(val mFilePath: String, var mSheetData: SheetData) {

    var mExpressList = mutableListOf<List<String>>()    //  数式リスト
    val mFileTitle = listOf("title", "express")         //  数式データ(タイトル,数式)
    val klib = KLib()


    fun inputDialog(c: Context, title: String, operation: Consumer<String>) {
        val linearLayout = LinearLayout(c)
        val expressLabel = TextView(c)
        val etExpress = EditText(c)
        val titleLabel = TextView(c)
        val etTitle = EditText(c)
        var spExpress = Spinner(c)
        var expressFirst = true
        val colTitleLabel = TextView(c)
        val spColTitle = Spinner(c)
        var colTitleFirst = true

        linearLayout.orientation = LinearLayout.VERTICAL
        titleLabel.setText("タイトル")
        expressLabel.setText("数式")
        colTitleLabel.setText("列タイトル")

        //  数式選択
        var expressAdapter = ArrayAdapter(c, R.layout.support_simple_spinner_dropdown_item, getExpressList())
        spExpress.adapter = expressAdapter
        spExpress.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!expressFirst || 0 == etExpress.text.length) {
                    etExpress.setText(mExpressList[position][1])
                    etTitle.setText(mExpressList[position][0])
                }
                expressFirst = false
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }
        //  カラム選択　([ColNo:ColTitle])
        var colTitleList = getColTitleList()
        var colTitleAdapter = ArrayAdapter(c, R.layout.support_simple_spinner_dropdown_item, colTitleList)
        spColTitle.adapter = colTitleAdapter
        spColTitle.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!colTitleFirst) {
                    var buf = etExpress.text.toString() + colTitleList[position]
                    etExpress.setText(buf)
                }
                colTitleFirst = false
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        linearLayout.addView(expressLabel)
        linearLayout.addView(etExpress)
        linearLayout.addView(titleLabel)
        linearLayout.addView(etTitle)
        linearLayout.addView(spExpress)
        linearLayout.addView(colTitleLabel)
        linearLayout.addView(spColTitle)

        val dialog = AlertDialog.Builder(c)
        dialog.setTitle(title)
        dialog.setView(linearLayout)
        //  OKボタン処理
        dialog.setPositiveButton("OK", DialogInterface.OnClickListener {
                dialog, which ->
            operation.accept("\"" + etTitle.text.toString() + "\",\"" +
                    etExpress.text.toString() + "\",\"")
            for (i in mExpressList.indices) {
                if (mExpressList[i][1].compareTo(etExpress.text.toString())==0) {
                    mExpressList.removeAt(i)
                    break
                }
            }
            var express = listOf<String>(etTitle.text.toString(), etExpress.text.toString())
            mExpressList.add(express)
            saveData()
        })
        //  Deleteボタン処理
        dialog.setNeutralButton("Delete", DialogInterface.OnClickListener {
                dialog, which ->
            for (i in mExpressList.indices) {
                if (mExpressList[i][1].compareTo(etExpress.text.toString())==0) {
                    mExpressList.removeAt(i)
                    break
                }
            }
            saveData()
        })
        //  Cancelボタン処理
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    var iExpressSetOperation = Consumer<String> { s ->
        var data = klib.splitCsvString(s)
    }

    /**
     * 数式リストから数式のみのリストを取得
     */
    fun getExpressList(): List<String> {
        var titleList = mutableListOf<String>()
        for (data in mExpressList)
            titleList.add(data[1])
        return titleList
    }

    /**
     * テーブルデータから列番号と列タイトルのリストを取得
     */
    fun getColTitleList(): List<String> {
        var titleList = mutableListOf<String>()
        var col = 0
        for (data in mSheetData.mDataList[0]) {
            titleList.add("[" + col + ":" + data + "]")
            col++
        }
        return titleList
    }

    /**
     * 数式リストを2ファイルから取得
     */
    fun loadData() {
        if (klib.existsFile(mFilePath)) {
            var expressList = klib.loadCsvData(mFilePath, mFileTitle)
            mExpressList.addAll(expressList)
        }
    }

    /**
     * 数式リストをファイルに保存
     */
    fun saveData() {
        klib.saveCsvData(mFilePath, mFileTitle, mExpressList)
    }
}