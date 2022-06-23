package jp.co.yoshida.katsushige.ymemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.yoshida.katsushige.mylib.FileSelectActivity;
import jp.co.yoshida.katsushige.mylib.ListData;
import jp.co.yoshida.katsushige.mylib.YLib;

public class DataListActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "DataListActivity";

    private Spinner mSpCategory;
    private ListView mListView;
    private Button mBtFileSelect;                       //  ファイル操作ボタン
    private Button mBtEdit;                             //  編集などのメニュー表示ボタン
    private Button mBtAdd;                              //  追加などのメニュー表示ボタン
    private Button mBtDel;                              //  削除(選択モード)/ソート(通常モード)ボタン
    private Button mBtSelect;                           //  選択モード切替ボタン
    private EditText mEdSearchWord;                     //  検索ワード入力
    private Button mBtSearch;                           //  検索ボタン
    private String mCategoryName;                       //  大分類
    private String mFileName;                           //  データファイル名
    private String mListName = "Ymemo";                 //  リストファイル名
    private ArrayAdapter<String> mCategoryAdapter;
    private ArrayAdapter<String> mListDataAdapter;

    private final int MENU01 = 0;
    private final int MENU02 = 1;

    enum FILESELECTMODE {load,save}

    public static String[] mDataFormat = {
            "タイトル","ファイル","分類","種別","表示属性"};
    private String[] mKeyData = new String[] {"ファイル"};
    private String[] mMenuTitle = {             //  項目長押し
            "開く","テキストとして開く","ファイル属性編集","タイトル変更","分類変更","削除","コピー","貼付け"};
    private String[] mOpeMenuTitle = {          //  [一覧]ボタンでファイル選択後のメニュー
            "開く","テキストとして開く","削除"};
    private String[] mEditMenuTitle = {         //      [編集]ボタン
            "分類編集","種別変更","非表示化","非表示化解除","ファイルの存在しない項目の削除"};
    private String[] mRegistMenuTitle = {       //  [追加]ボタン
            "ファイル個別登録","ファイル一括登録","分類登録"};
    private String[] mSortMenuTitle = {         //  [ソート]ボタン
            "ソートなし","タイトル","ファイルパス","分類","逆順"};
    private String[] mOptionItemTitle = {       //  オプションメニュー
            "ファイル選択DB初期化","非表示化を切換える"};
    public static Map<String, String> mFileTypeMap =  //  ファイルの種別
            new LinkedHashMap<String, String>() {
                {
                    put("ファイル", "FILE");
                    put("Webアドレス", "URL");
                    put("テキストファイル", "TEXT");
                }
            };

    private int mSelectNo;                              //  選択項目No
    private String mSelectTitle;                        //  選択項目のタイトル
    private String mSelectFileName;                     //  選択項目のファイル名
    private String mSelectCategory;                     //  選択項目の分類名
    private String mSelectFileType;                     //  選択項目のファイル種別

    private String mSearchWord = "";                    //  リストタイトル検索フィルター
    private String[] mSearchListMenu;
    private ArrayList<String> mSearchWords = new ArrayList<String>();
    private String mSearcWordFileName;                  //  検索ワードのファイル名

    private boolean mMultiSelectMode = false;           //   複数選択モード
    private boolean mDbInit = false;                    //  ファイル選択DBの初期化
    private boolean mAllListDisp = false;               //  非表示無効化フラグ
    enum CommandMode {non, addlist, addlistall};
    private CommandMode mCommandMode = CommandMode.non;
    private String mFindFileName = "";
    private final Handler mDataHandler = new Handler();
    private ProgressDialog mProgressDialog = null;

    //  EditText Dialogで使用するコントロール
    private EditText mEditText;
    private CheckBox mCheckBox;
    private LinearLayout mLinearLayout;
    private String mEditTextTitle;
    enum MultiFunctionMode {
        non, chgCategory, chgSelectCategory, chgCategoryAll, chgDispAttribute, chgDispAttributeAll,
        fileExtSelect, setCategory, addCategory, chgTitle};
    private MultiFunctionMode mMultiFunctionMode = MultiFunctionMode.non;
    private boolean mSubDirSearch = true;               //  再帰検索フラグ

    private String mSaveDirectory;                      //  データ保存ディレクトリ

    private YLib ylib;
    private ListData mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list);

        ylib = new YLib(this);
        InitScreen();

        //	データ保存ディレクトリ
        mSaveDirectory = ylib.setSaveDirectory(getPackageName().substring(getPackageName().lastIndexOf('.')+1));

        Intent intent = getIntent();
        String action = intent.getAction();
        mSaveDirectory = intent.getStringExtra("SAVEDIR");  //  データ保存ディレクトリ
        mCategoryName  = intent.getStringExtra("CATEGORY"); //  リストデータ登録ファイル名
        mListName = MainActivity.LISTHEAD + "_" + mCategoryName;
        this.setTitle(mCategoryName);

        mList = new ListData(this, mDataFormat);
        mList.setSaveDirectory(mSaveDirectory, mListName);
//        mList.setKeyData(mKeyData);
        mSearcWordFileName = mSaveDirectory + "/" + MainActivity.LISTHEAD + "SearchWord.csv";
        setCategory();
        setDefaultCategory();
        loadData(getCurCategory());
        loadSearchWordData();

        //  分類の選択
        mSpCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                loadData(getCurCategory());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //  リスト項目がクリックされた時はファイルの実行
        //  ファイルの選択
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                if (!mMultiSelectMode) {
                    Toast.makeText(getApplicationContext(), "clicked:" + position + " " + item, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCreate:mListView onItemClick[" + item + "] ");
                    setSelectData(item);
                    executeFile(mSelectFileName, mSelectFileType);
                }
            }
        });

        //  リスト項目をロングクリックした時はメニューの表示
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //ここに処理を書く
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Log.d(TAG, "onCreate:mListView onItemLongClick[" + item + "] ");
                if (!mMultiSelectMode) {
                    setSelectData(item);
                    SetMenuDialog();
                }
                return true;    // 戻り値をtrueにするとOnClickイベントは発生しない
            }
        });
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        Log.d(TAG, "onClick:" + button.getText().toString());
        if (button.getText().toString().compareTo("一覧")==0) {
            FileSelectOpen(FILESELECTMODE.load);
        } else if (button.getText().toString().compareTo("編集")==0) {
            SetEditMenuDialog();
        } else if (button.getText().toString().compareTo("追加")==0) {
            SetRegistMenuDialog();
        } else if (button.getText().toString().compareTo("削除")==0) {
            removeSelectData();
        } else if (button.getText().toString().compareTo("選択")==0) {
            setSelectMode(!mMultiSelectMode);
        } else if (button.getText().toString().compareTo("ソート")==0) {
            SetSortMenuDialog();
        } else if (button.getText().toString().compareTo("検索")==0) {
            mSearchWord = mEdSearchWord.getText().toString();
            setSearchWordList(mSearchWord);
            loadData(getCurCategory());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.DATAINPUT_ACTIVITY) {                    //	データ登録画面
            String category = getCurCategory();
            setCategory();
            loadData(category);
        } else 	if (requestCode == MainActivity.FILESELECT_ACTIVITY) {			//	ファイルの選択結果
            if (resultCode == RESULT_OK) {
                mFileName = (String)data.getCharSequenceExtra("FileName");
                SetOpeMenuDialog();
            }
        } else 	if (requestCode == MainActivity.DIRSELECT_ACTIVITY) {			//	ディレクトリの選択結果
            if (resultCode == RESULT_OK) {
                if (mCommandMode == CommandMode.addlistall) {   //  フォルダ内のファイルを一括登録
                    Toast.makeText(this, "一括 " + data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
                    AddAllData(data.getCharSequenceExtra("FileName").toString());
                }
            }
        } else 	if (requestCode == MainActivity.SHARESELECT_ACTIVITY) {		//	共有アプリ起動
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
           }
        } else 	if (requestCode == MainActivity.SAVEFILESELECT_ACTIVITY) {		//	ファイル保存
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
                mFileName = (String)data.getCharSequenceExtra("FileName");
            }
        } else 	if (requestCode == MainActivity.FILEOPERATION_ACTIVITY) {		//	ファイル操作
            if (resultCode == RESULT_OK) {

            }
        }
        mCommandMode = CommandMode.non;
    }

    @Override
    protected void onDestroy() {
        setDefaultCategory(getCurCategory());
        saveSearchWordData();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add(Menu.NONE, MENU01, Menu.NONE, mOptionItemTitle[0]);
        MenuItem item2 = menu.add(Menu.NONE, MENU02, Menu.NONE, mOptionItemTitle[1]);
        item1.setIcon(android.R.drawable.ic_menu_set_as);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case MENU01:
                mDbInit = true;     //  ファイル検索一覧のDB初期化
                break;
            case MENU02:            //  非表示化を反転にする
                mAllListDisp = !mAllListDisp;
                loadData(getCurCategory());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /***
     * [一覧]ボタン　ファイル選択後のの実行選択メニュー
     */
    private void SetOpeMenuDialog() {
        if (mOpeMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mOpeMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:         //  開く
                        executeFile(mFileName, ""); //  選択したファイルを開く
                        break;
                    case 1:
                        goTextEdit(mFileName);              //  テキストファイルとして開く
                        break;
                    case 2:         //  削除
                        break;
                }
                }
            })
            .create()
            .show();
    }

    /**
     *  [編集]ボタン メニュー
     */
    private void SetEditMenuDialog() {
        if (mEditMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mEditMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "SetEditMenuDialog: " + mEditMenuTitle[which]);
                if (mEditMenuTitle[which].compareTo("分類編集") == 0) {
                    ChgCategoryDialog();
                } else if (mEditMenuTitle[which].compareTo("種別変更") == 0) {
                    ChgFileTypeDialog();
                } else if (mEditMenuTitle[which].compareTo("非表示化") == 0) {
                    if (mMultiSelectMode)
                        chgSelectDispAttibute(false);
                    else
                        chgDispAllDispAttibute(false);
                } else if (mEditMenuTitle[which].compareTo("ファイルの存在しない項目の削除") == 0) {
                    DelNonExistsFile();
                }
                }
            })
            .create()
            .show();
    }

    /**
     *  [ファイル選択時] 長押しでの操作メニュー
     */
    private void SetMenuDialog() {
        if (mMenuTitle.length == 0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "SetMenuDialog: " + mMenuTitle[which] + " [" + mSelectFileName + "][" + mListName + "]");
                switch (which) {
                    case 0:     //  ファイルを開く
                        executeFile(mSelectFileName, mSelectFileType);
                        break;
                    case 1:     //  テキストファイルの編集
                        goTextEdit(mSelectFileName);
                        break;
                    case 2:     //  登録データ編集
                        goDataEdit(mSaveDirectory, mListName, mSelectTitle);
                        break;
                    case 3:     //  タイトル変更
                        ChgTitleDialog();
                        break;
                    case 4:     //  分類変更
                        ChgCategoryDialog();
                        break;
                    case 5:     //  削除
                        removeData(mSelectTitle);
                        break;
                    case 6:
                        copyData();
                        break;
                    case 7:
                        pasteData();
                        break;
                }
                }
            })
            .create()
            .show();
    }


    /***
     * [追加]ボタン ファイル登録の選択メニュー
     */
    private void SetRegistMenuDialog() {
        if (mRegistMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mRegistMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "SetRegistMenuDialog: " + mRegistMenuTitle[which]);
                switch (which) {
                    case 0:         //  開く
                        goDataEdit(mSaveDirectory, mListName, "");
                       break;
                    case 1:         //  一括登録
                        AddFileListAll();
                        break;
                    case 2:         //  分類登録
                        SetCategoryDialog();
                        break;
                    }
                }
            })
            .create()
            .show();
    }

    /***
     * [ソート]ボタン　ソートメニューの表示
     */
    private void SetSortMenuDialog() {
        if (mSortMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mSortMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "SetSortMenuDialog: " + mSortMenuTitle[which]);
                switch (which) {
                    case 0:
                        mList.setDataSortDirect(ListData.DataSortDirect.non);
                        break;
                    case 1:
                        mList.setDataSortCategory("タイトル");
                        break;
                    case 2:
                        mList.setDataSortCategory("ファイル");
                        break;
                    case 3:
                        mList.setDataSortCategory("分類");
                        break;
                    case 4:
                        mList.setDataSortReverse();
                        break;
                }
                loadData(getCurCategory());
                }
            })
            .create()
            .show();
    }

    /**
     * 分類名を追加する
     */
    private void SetCategoryDialog() {
        mMultiFunctionMode = MultiFunctionMode.addCategory;
        mEditTextTitle = "分類の入力";
        EditTextDialog("");
    }


    /**
     * 指定フォルダのファイルを一括で登録する
     * @param path
     */
    private void AddAllData(String path) {
        Log.d(TAG,"AddAllData: ");
        String category = getCurCategory().compareTo("すべて")==0?"":getCurCategory();
        List<String> fileList = ylib.getFileList(path, mFindFileName, mSubDirSearch);
        for (String fpath : fileList) {
            AddData(category, ylib.getNameWithoutExt(ylib.getName(fpath)), fpath);
        }
        mList.saveDataFile();
        loadData(getCurCategory());
        setCategory();
    }

    /**
     * ファイルをリストに登録する
     * 同じパスが存在すれば追加しない
     *
     * @param category      分類
     * @param title         登録タイトル
     * @param path          登録ファイルのフルパス
     */
    private void AddData(String category, String title, String path) {
        //  データの追加
        mList.setData(title, "分類", category, true);
        mList.setData(title, "ファイル", path, true);
        Log.d(TAG,"AddData: "+title+" "+category+" "+path);
    }

    /**
     * 再帰的にファイルを検索して登録する
     * ダイヤログを表示して検索ファイル名を指定する(ワイルドカード)
     */
    private void AddFileListAll() {
        mMultiFunctionMode = MultiFunctionMode.fileExtSelect;
        mEditTextTitle = "検索ファイル名(ワイルドカード)";
        EditTextDialog("*.pdf");
    }

    /**
     * 分類名を変更する
     * ダイヤログを表示して分類名を入力する
     */
    private void ChgCategoryDialog() {
        Log.d(TAG,"ChgCategoryDialog: "+mMultiSelectMode);
        mEditTextTitle = "分類の入力";
        if (mMultiSelectMode) {
            //  複数選択で選択されているファイルの分類変更
            mMultiFunctionMode = MultiFunctionMode.chgSelectCategory;
            EditTextDialog(checkedFirstItemData("分類"));
        } else {
            //  単独選択での分類名変更
            mMultiFunctionMode = MultiFunctionMode.chgCategory;
            EditTextDialog(mSelectCategory);
        }
    }

    /***
     * 選択されているファイルのタイトル名を変更する
     */
    private void ChgTitleDialog() {
        mMultiFunctionMode = MultiFunctionMode.chgTitle;
        mEditTextTitle = "タイトルの変更";
        EditTextDialog(mSelectTitle);
    }

    /**
     * 選択されている項目または表示されている項目のファイルタイプを変更する
     */
    private void ChgFileTypeDialog() {
        int size = mFileTypeMap.size();
        String[] fileTypeTitle = new String[size];
        int i = 0;
        for (String title : mFileTypeMap.keySet())
            fileTypeTitle[i++] = title;
        ylib.setMenuDialog(DataListActivity.this, "ファイル種別",fileTypeTitle, iChgFileType);
    }

    /**
     *  登録データを削除する
     * @param title
     */
    private void removeData(String title) {
        Log.d(TAG,"removeData: "+title);
        mList.LoadFile();
        if (mList.removeData(title)) {
            mList.saveDataFile();
            setCategory();
            loadData(getCurCategory());
        }
    }

    /**
     * 選択データ(チェック付き)をリストから削除する
     */
    private void removeSelectData() {
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        mList.LoadFile();
        int count = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                setSelectData(mListDataAdapter.getItem(checked.keyAt(i)));
                Log.d(TAG,"removeSelectData: "+mSelectTitle);
                if (mList.removeData(mSelectTitle))
                    count++;
            }
        }
        mListView.clearChoices();
        mList.saveDataFile();
        setCategory();
        loadData(getCurCategory());
        ylib.messageDialog(this, "削除",count + " ファイル削除しました");
    }

    /***
     * ファイルが存在しない項目を削除する
     */
    private void DelNonExistsFile() {
        String[] keys = mList.getKeyData();
        int count = 0;
        for (int i = 0; i < keys.length; i++) {
            String path = mList.getData(keys[i],"ファイル");
            if (!ylib.existsFile(path)) {
                mList.removeData(keys[i]);
                count++;
            }
        }
        if (0 < count) {
            mList.saveDataFile();
            setCategory();
            loadData(getCurCategory());
            ylib.messageDialog(this, "削除", count + " 項目を削除しました");
        } else {
            ylib.messageDialog(this, "削除", "削除した項目はありません");
        }
    }

    /**
     * 選択されているデータをプリファレンスに書き込む
     */
    private void copyData() {
        ylib.setStrPreferences(mSelectTitle, "CopyTitle", this);
        ylib.setStrPreferences(mSelectFileName, "CopyFileName", this);
        ylib.setStrPreferences(mSelectCategory, "CopyCategory", this);
        ylib.setStrPreferences(mSelectFileType, "CopyFileType", this);
    }


    /**
     * プリファレスにコピーしたデータを追加登録する
     */
    private void pasteData() {
        String title = ylib.getStrPreferences("CopyTitle", this);
        String fileName = ylib.getStrPreferences("CopyFileName", this);
        String category = ylib.getStrPreferences("CopyCategory", this);
        String fileType = ylib.getStrPreferences("CopyFileType", this);
        if (title.compareTo("###")==0 || title.length()<=0) {
            Toast.makeText(this, "データがありません", Toast.LENGTH_LONG);
            return;
        }
        mList.setData(title,"分類", category, true);
        mList.setData(title, "ファイル", fileName, true);
        mList.setData(title, "種別", fileType, true);
        mList.saveDataFile();

        loadData(getCurCategory());
        loadSearchWordData();
    }


    /**
     * 文字入力ダイヤログ
     * 入力した文字はmultiFunction()でモード(mMultiFunctionMode)ごとに処理が別れる
     */
    private void EditTextDialog(String editText) {
        Log.d(TAG,"EditTextDialog: "+mMultiSelectMode+" "+mMultiFunctionMode);
        mEditText = new EditText(this);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.setWidth(300);
        mEditText.setText(editText);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(mEditText);
        if (mMultiFunctionMode == MultiFunctionMode.fileExtSelect) {
            mCheckBox = new CheckBox(this);
            mCheckBox.setText("サブディレクトリも検索");
            mLinearLayout.addView(mCheckBox);
        }
        // Show Dialog
        new AlertDialog.Builder(DataListActivity.this)
            //	.setIcon(R.drawable.icon)
            .setTitle(mEditTextTitle)
            .setView(mLinearLayout)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "ChgCategoryDialog:　OKボタン " + mEditText.getText().toString());
                    /* OKボタンをクリックした時の処理 */
                    if (mMultiFunctionMode == MultiFunctionMode.fileExtSelect)
                        mSubDirSearch = mCheckBox.isChecked();      //  サブディレクトリ検索チェック
                    multiFunction(mEditText.getText().toString());
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* Cancel ボタンをクリックした時の処理 */
                }
            })
            .show();
    }

    /**
     * 指定の文字列をモードごとに処理を変える
     * @param text
     */
    private void multiFunction(String text) {
        Log.d(TAG, "multiFunction: " + mMultiFunctionMode + " " + text);
        if (mMultiFunctionMode==MultiFunctionMode.chgCategory) {                //  選択行の分類変更
            chgFileTitle(mSelectTitle, "分類", text);
        } else if (mMultiFunctionMode==MultiFunctionMode.chgSelectCategory) {   //  複数選択の分類変更
            chgSelectCatergory(text);
        } else if (mMultiFunctionMode==MultiFunctionMode.chgCategoryAll) {      //  全行の分類変更
            chgDispAllCatergory(text);
        } else if (mMultiFunctionMode==MultiFunctionMode.fileExtSelect) {
            mCommandMode = CommandMode.addlistall;
            mFindFileName = text;
            goFileSelect("\\.*$", "DirSelectFileDispPrefs", "NAME", FileSelectActivity.DIRSELECT, mDbInit);
            mDbInit = false;
        } else if (mMultiFunctionMode==MultiFunctionMode.setCategory) {
        } else if (mMultiFunctionMode==MultiFunctionMode.addCategory) {         //  分類項目をメニューに追加
            setAddCategory(text);
        } else if (mMultiFunctionMode==MultiFunctionMode.chgTitle) {            //  選択行のタイトル変更
            chgFileTitle(mSelectTitle, "タイトル", text);
        }
        mMultiFunctionMode = MultiFunctionMode.non;
    }

    /***
     * 分類の追加
     * @param category
     */
    private void setAddCategory(String category) {
        String curCategory = category;
        addCategory(category);
        setCategory(category);
        loadData(curCategory);
    }

    /**
     * ファイルのタイトル項目のデータを変更する
     * @param title     選択タイトル
     * @param category  分類
     * @param text      変更データ
     */
    private void chgFileTitle(String title, String category, String text) {
        mList.setData(title, category, text, true);
        mList.saveDataFile();

        setCategory();
        loadData(getCurCategory());
    }

    /**
     * ファイルタイプを変更する関数インターフェース
     */
    Consumer<String> iChgFileType = new Consumer<String>() {
        @Override
        public void accept(String s) {
            chgFileType(mFileTypeMap.get(s));
        }
    };

    /**
     * ファイル種別を変更する
     * @param fileType
     */
    private void chgFileType(String fileType) {
        if (mList.LoadFile()) {
            if (mMultiSelectMode) {
                chgSelectData("種別", fileType);
            } else {
                chgDispAllData("種別", fileType);
            }
        }
        mList.saveDataFile();
    }

    /**
     * 選択したデータの分類名を一括で変更する
     * @param category
     */
    private void chgSelectCatergory(String category) {
        chgSelectData("分類", category);
    }

    /**
     * 選択したデータの表示属性を変更する
     * @param disp
     */
    private void chgSelectDispAttibute(boolean disp) {
        chgSelectData("表示属性", disp?"":"非表示");
    }

    /**
     * 表示されているリストの分類を一括で変更する
     * @param category
     */
    private void chgDispAllCatergory(String category) {
        chgDispAllData("分類", category);
    }


    /**
     * 表示されているリストの表示属性を一括で変更する
     * @param disp
     */
    private void chgDispAllDispAttibute(boolean disp) {
        chgDispAllData("表示属性", disp?"":"非表示");
    }

    /**
     * 選択されたリストデータ(チェック付き)のデータを変更する
     * @param category      変更するデータ項目
     * @param data          変更データ
     */
    private void chgSelectData(String category, String data) {
        Log.d(TAG,"chgSelectData:複数選択 "+category+" "+data);
        //  getCheckedItemPositions()は一度チェックしたアイテムの結果も返すので注意
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        if (mList.LoadFile()) {
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    setSelectData(mListDataAdapter.getItem(checked.keyAt(i))); //   選択したデータを設定する
                    mList.setData(mSelectTitle, category, data, true);
                }
            }
            mListView.clearChoices();
        }
        mList.saveDataFile();
        String curCategory = getCurCategory();
        setCategory();
        loadData(curCategory);
    }

    /***
     * ListViewの複数選択の時、最初にチェックされた行のタイトル項目のデータを返す
     * @param category  分類のタイトル項目
     * @return          データ
     */
    private String checkedFirstItemData(String category) {
        //  getCheckedItemPositions()は一度チェックしたアイテムの結果も返すので注意
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        int n = 0;
        for (n=0; n<checked.size(); n++) {
            if (checked.valueAt(n))
                break;
        }
        if (checked.size() <= n || !checked.valueAt(n))
            return "";
        String key = getListTitle(mListDataAdapter.getItem(checked.keyAt(n)));
        return mList.getData(key, category); //   選択したデータを設定する
    }

    /**
     * リストに表示されているすべてのデータを変更する
     * @param category     変更する項目
     * @param data         変更データ
     */
    private void chgDispAllData(String category, String data) {
        Log.d(TAG, "chgDispAllData:全数 " + category + " , " + data);
        String curCategory = getCurCategory();
        if (mList.LoadFile()) {
            for (int i =0; i<mListDataAdapter.getCount(); i++) {
                setSelectData(mListDataAdapter.getItem(i));
                mList.setData(mSelectTitle, category, data, true);
            }
        }
        mList.saveDataFile();
        setCategory();
        loadData(curCategory);
    }

    /**
     * 内部テキストエディタで開く
     * @param filepath
     */
    private void goTextEdit(String filepath) {
        Intent intent = new Intent(this, TextEdit.class);
        intent.putExtra("FILEPATH", filepath);	//	データファイル名
        startActivity(intent);
    }

    /**
     *  登録ファイルデータ編集
     * @param saveDirectory
     * @param listName
     * @param dataTitle
     */
    private void goDataEdit(String saveDirectory, String listName, String dataTitle) {
        Log.d(TAG, "goDataEdit: "+saveDirectory+" , " + listName+" , " + dataTitle);
        Intent intent = new Intent(this, DataEdit.class);
        intent.putExtra("SAVEDIR", saveDirectory);              //  ディレクトリ名名
        intent.putExtra("FILENAME", listName);                  //  リストデータ保存ファイル名名
        intent.putExtra("DATATITLE", dataTitle);                //  ファイルデータ名
//        intent.putExtra("DATANAME", dataFile);                      //  ファイルデータ名
//        intent.putExtra("CATEGORY", getCurCategory().compareTo("すべて")==0?"":getCurCategory());  //	 分類名
        intent.putExtra("DATAFORMAT", mList.getCsvData(mDataFormat));  //  ファイルデータのタイトル
        startActivityForResult(intent, MainActivity.DATAINPUT_ACTIVITY);
    }

    /**
     * ファイル選択画面を開く
     * @param mode
     */
    private void FileSelectOpen(FILESELECTMODE mode) {
        if (mode==FILESELECTMODE.load) {
            goFileSelect("\\.*$", "FileSelectPrefs", "NORMAL", FileSelectActivity.FILESELECT, mDbInit);
        } else if (mode==FILESELECTMODE.save) {
            goFileSelect("\\.*$", "SaveAsFilePrefs","NORMAL",FileSelectActivity.SAVEFILESELECT, mDbInit);
        }
        mDbInit = false;
    }

    /**
     * 	ファイル選択画面を開く
     * @param filter		正規表現("\\.*"|"\\.gpx$"|...)
     * @param preference	フォルダ記憶キーワード (SaveAsFilePrefs/FileSelectPrefs/DirSelectPrefs)
     * @param sort			ソート(DATE:日付  other:名前
     * @param activitytype	DATAINPUT_ACTIVITY = 1001
     * 						FILESELECT_ACTIVITY = 1002
     * 						SHARESELECT_ACTIVITY = 1003
     * 						SAVEFILESELECT_ACTIVITY = 1004
     *                      DIRSELECT_ACTIVITY      = 1005;
     *                      FILEOPERATION_ACTIVITY  = 1006;
     */
    private void goFileSelect(String filter,String preference,String sort,int activitytype, boolean init) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Intent intent = new Intent(this, FileSelectActivity.class);
            intent.putExtra("FILTER", filter);
            intent.putExtra("PREFERENCE", preference);
            intent.putExtra("SORTTYPE",sort);
            intent.putExtra("DBINIT", init);
            startActivityForResult(intent, activitytype);
        } else {
            Toast.makeText(this,"FileSelectActivity エラー",Toast.LENGTH_LONG).show();
        }
    }


    /** リストの単独選択と複数選択を切り替える
     * @param multi
     */
    private void setSelectMode(boolean multi) {
        String category = getCurCategory();
        if (multi) {
            mListDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked);
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } else {
            mListDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        }
        mListView.setAdapter(mListDataAdapter);
        mMultiSelectMode = multi;
        setCategory(category);
        loadData(getCurCategory());
        setButtonMode();
    }

    /**
     * リストの選択アイテムからデータNo、ファイル名などをグローバルデータに設定する
     * @param item
     */
    private void setSelectData(String item) {
        Log.d(TAG, "setSelectData: [" + item+"]["+getListTitle(item)+"]");
        mSelectNo = getListNo(item);
        mSelectTitle = mList.getData(getListTitle(item), "タイトル");
        mSelectCategory = mList.getData(getListTitle(item), "分類");
        mSelectFileName = mList.getData(getListTitle(item), "ファイル");
        mSelectFileType = mList.getData(getListTitle(item), "種別");
        Log.d(TAG, "setSelectData: [" + mSelectNo+"]["+mSelectTitle+"]["+
                mSelectCategory+"]["+mSelectFileName+"]["+mSelectFileType+"]");
    }

    /***
     * 検索ワード(フィルタ)をファイルに保存する
     */
    private void saveSearchWordData() {
        String buffer = "";
        for (String key : mSearchWords) {
            buffer += key + "\n";
        }
        if (0 < buffer.length()) {
            ylib.writeFileData(mSearcWordFileName, buffer);
        }
    }

    /***
     * 検索ワード(フィルタ)をファイルから読み出す
     * @return
     */
    private boolean loadSearchWordData() {
        //	ファイルの存在確認
        if (!ylib.existsFile(mSearcWordFileName)) {
            Toast.makeText(this, "データが登録されていません\n"+mSearcWordFileName, Toast.LENGTH_LONG).show();
            return false;
        }

        //	ファイルデータの取り込み
        ArrayList<String> fileData = new ArrayList<String>();
        fileData.clear();
        ylib.readTextFile(mSearcWordFileName, fileData);
        if (fileData.size()<1)
            return false;
        mSearchWords = new ArrayList<String>();
        int n=0;
        for (String str : fileData) {
            if (0 < str.length() && n < 100) {
                mSearchWords.add(str);
                n++;
            }
        }
        return true;
    }

    /***
     * 検索キーワードを登録する
     * 新規に登録したキーワードの重複をなくし、最初に持ってくる
     * @param word      キーワード
     */
    private void setSearchWordList(String word) {
        ArrayList<String> wordList = new ArrayList<String>();
        wordList.addAll(mSearchWords);
        mSearchWords.clear();
        mSearchWords.add(word);
        for (int i = 0; i < wordList.size(); i++) {
            if (wordList.get(i).compareTo(word)!=0)
                mSearchWords.add(wordList.get(i));
        }
    }

    /**
     *  指定された分類で表示する
     * @param category
     */
    private void loadData(String category) {
        Log.d(TAG, "loadData:" + category);
        mListDataAdapter.clear();
        if (mList.LoadFile()) {
            //  正規表現のフィルター設定
            mList.resetFilter();
            if (0 < category.length() && category.compareTo("すべて")!=0)
                mList.setFilter("分類", category);    //  一致
            if (0 < mSearchWord.length())
                mList.setFilter("タイトル", ".*"+mSearchWord+".*"); //  含む
            if (!mAllListDisp)
                mList.setFilter("表示属性","^(?!.*非).*$");  //  非を含まない
            String[] dataList = mList.getStrDatas("タイトル");
            if (dataList != null) {
                for (int i = 0; i < dataList.length; i++)
                    mListDataAdapter.add(String.format("[%04d] ", i) + dataList[i]);
            }
        }
    }

    /**
     * 分類の追加
     * @param category
     */
    private void addCategory(String category) {
        int n = mCategoryAdapter.getPosition(category);
        if (n < 0)
            mCategoryAdapter.add(category);
    }
    /**
     * 分類メニューを指定のものにする
     * @param category
     */
    private void setCategory(String category) {
        int n = mCategoryAdapter.getPosition(category);
        if (0<=n)
            mSpCategory.setSelection(n);
    }

    /**
     * プリファレンスに設定されているカテゴリィをカテゴリィ選択に設定する
     */
    private void setDefaultCategory() {
        setCategory(getDefaultCategory());
    }

    /**
     *  分類をドロップダウンメニューに設定する
     */
    private void setCategory() {
        mList.LoadFile();
        mCategoryAdapter.clear();
        mCategoryAdapter.add("すべて");
        String[] categories = mList.getStrDatas("分類");
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                //  重複なしで登録
                if (mCategoryAdapter.getPosition(categories[i]) < 0) {
                    mCategoryAdapter.add(categories[i]);
                }
            }
        }
    }

    /**
     * プリファレンスに設定されているカテゴリを取り出す
     * @return
     */
    private String getDefaultCategory() {
        return ylib.getStrPreferences("DefaultCategory", this);
    }

    /**
     * プリファレンスにカテゴリィを設定する
     * @param category
     */
    private void setDefaultCategory(String category) {
        ylib.setStrPreferences(category, "DefaultCategory", this);
    }

    /**
     * 選択されている分類を取得する
     * @return
     */
    private String getCurCategory() {
        int l = mSpCategory.getSelectedItemPosition();
        return (0<=l?mCategoryAdapter.getItem(l):"");
    }

    /***
     * ローカルファイルを関連付けで開く
     * ファイルの種別に合わせて開く方法を変える
     * @param path      開くファイルのパス
     * @param fileType  ファイルの種類
     */
    private void executeFile(String path, String fileType) {
        Log.d(TAG,"executeFile: "+path+" "+fileType);
        if (fileType.compareTo("URL")==0) {
            ylib.webDisp(this, path);
        } else if (fileType.compareTo("TEXT")==0) {
            goTextEdit(path);    //  テキストファイル編集
        } else {
            ylib.executeFile(this, path);
        }
    }

    /**
     * リストタイトルからデータNoを取り出す
     * @param title
     * @return
     */
    private int getListNo(String title) {
        String no = title.substring(1, 5);
        return Integer.valueOf(no);
    }

    /**
     * リストタイトルからデータNoを覗いたタイトルを取り出す
     * @param title     リストタイトル
     * @return          タイトル
     */
    private String getListTitle(String title) {
        return title.substring(7);
    }

    /**
     * リストの選択モードでボタンの有効無効を切り替える
     */
    private void setButtonMode() {
        mBtFileSelect.setEnabled(!mMultiSelectMode);
        mBtAdd.setEnabled(!mMultiSelectMode);
        mBtDel.setText(mMultiSelectMode?"削除":"ソート");
    }

    /***
     * 操作画面の初期化
     */
    private void InitScreen() {
        //Log.d(TAG, "InitScreen: " + getWindowsHeight()+","+getWindowsWidth());
        mSpCategory = (Spinner)this.findViewById(R.id.spinner);
        mListView = (ListView)this.findViewById(R.id.listView1);
        mBtFileSelect = (Button)this.findViewById(R.id.button3);
        mBtEdit = (Button)this.findViewById(R.id.button4);
        mBtAdd = (Button)this.findViewById(R.id.button5);
        mBtDel = (Button)this.findViewById(R.id.button6);
        mBtSelect = (Button)this.findViewById(R.id.button7);
        mEdSearchWord = (EditText)findViewById(R.id.editText2);
        mBtSearch = (Button)findViewById(R.id.button8);

        mBtFileSelect.setText("一覧");
        mBtEdit.setText("編集");
        mBtAdd.setText("追加");
        mEdSearchWord.setText("");

        mBtFileSelect.setOnClickListener(this);
        mBtEdit.setOnClickListener(this);
        mBtAdd.setOnClickListener(this);
        mBtDel.setOnClickListener(this);
        mBtSelect.setOnClickListener(this);
        mBtSearch.setOnClickListener(this);
        mEdSearchWord.setOnLongClickListener(this);
        setButtonMode();

        mCategoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mSpCategory.setAdapter(mCategoryAdapter);
        mListDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mListView.setAdapter(mListDataAdapter);

        mCategoryAdapter.add("すべて");
        //  フォーカスをボタンに移して起動時にキーボードが出るのを防ぐ
        mBtSearch.setFocusable(true);
        mBtSearch.setFocusableInTouchMode(true);
        mBtSearch.requestFocus();
    }
}
