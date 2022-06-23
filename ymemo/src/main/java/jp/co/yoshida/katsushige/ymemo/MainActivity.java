package jp.co.yoshida.katsushige.ymemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import jp.co.yoshida.katsushige.mylib.FileSelectActivity;
import jp.co.yoshida.katsushige.mylib.ListData;
import jp.co.yoshida.katsushige.mylib.YLib;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    //  共通定数
    public static final int DATAINPUT_ACTIVITY = 1001;
    public static final int FILESELECT_ACTIVITY = 1002;
    public static final int SHARESELECT_ACTIVITY = 1003;
    public static final int SAVEFILESELECT_ACTIVITY = 1004;
    public static final int DIRSELECT_ACTIVITY = 1005;
    public static final int FILEOPERATION_ACTIVITY = 1006;
    public static final int DATALIST_ACTIVITY = 1007;
    public static final int APPSELECT_ACTIVITY = 1010;

    private final int MENU01 = 0;
    private final int MENU02 = 1;

    private Button mBtAddCategory;
    private Button mBtFileOperation;
    private ListView mListView;
    private ArrayAdapter<String> mListDataAdapter;

    public static final String LISTHEAD = "Ymemo";  //  リストファルのヘッダ
    public static String mListName = "YmemoIndex";
    public static String[] mDataFormat = {"分類","表示属性"};
    private String[] mMenuTitle = {"分類変更","非表示化","表示化","削除"};   //  選択長押しメニュー
    private String[] mOptionItemTitle = {"ファイル選択DB初期化","非表示化を切換え"};
    private String[] mOpeMenuTitle = {"開く","テキストとして開く","削除"};
    private String mSelectTitle;                    //  分類名(編集用)
    enum FILESELECTMODE {load,save}
    enum CATEGORYEDITMODE {CREATE, EDIT};
    private boolean mDbInit = false;                //  ファイル選択DB初期化フラグ
    private boolean mAllListDisp = false;           //  非表示無効化フラグ
    private CATEGORYEDITMODE mCategoryEditMode = CATEGORYEDITMODE.CREATE;
    private String mSelectItem;
    private String mSelectFileName;

    private String mSaveDirectory;                  //  データ保存ディレクトリ
    private ListData mList;                         //  分類データリスト

    //  カテゴリ(分類)編集ダイヤログ用
    private EditText mEditText;
    private LinearLayout mLinearLayout;

    YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ドキュメント管理");
        ylib = new YLib(this);
        ylib.checkStragePermission(this);
        init();

        mSaveDirectory = ylib.setSaveDirectory(getPackageName().substring(getPackageName().lastIndexOf('.')+1));
        mList = new ListData(this, mDataFormat);
        mList.setSaveDirectory(mSaveDirectory, mListName);
        loadData();

        /**
         * リストの選択実行 ファイルリストに移行
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView listView = (ListView)adapterView;
                mSelectItem = (String)listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), mSelectItem, Toast.LENGTH_LONG).show();
                goDataList(mSaveDirectory, getListTitle(mSelectItem));
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView listView = (ListView)adapterView;
                mSelectItem = (String)listView.getItemAtPosition(position);
                mSelectTitle = getListTitle(mSelectItem);
                SetMenuDialog();
                return true;    // 戻り値をtrueにするとOnClickイベントは発生しない
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtAddCategory.getId()) {
            //  [分類]ボタン  分類作成
            mCategoryEditMode = CATEGORYEDITMODE.CREATE;
            CategoryEditDialog();
        } else if (view.getId() == mBtFileOperation.getId()) {
//            ylib.fileSelectDialog(mSaveDirectory, "*.*", true, iLoadFile);
            //  [操作]ボタン  ファイル操作
            FileSelectOpen(FILESELECTMODE.load);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DATAINPUT_ACTIVITY) {					 //	データ登録画面
        } else 	if (requestCode == FILESELECT_ACTIVITY) {            //	ファイルの選択結果
            if (resultCode == RESULT_OK) {
                mSelectFileName= (String) data.getCharSequenceExtra("FileName");
                SetOpeMenuDialog();
            }
        }
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
            case MENU01:            //  ファイル選択DB初期化
                mDbInit = true;
                break;
            case MENU02:            //  非表示化を反転にする
                mAllListDisp = !mAllListDisp;
                loadData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 選択行の属性を変更する
     */
    public void SetMenuDialog() {
        if (mMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "SetMenuDialog: " + mMenuTitle[which]);
                    switch (which) {
                         case 0:         //  編集
                             mCategoryEditMode = CATEGORYEDITMODE.EDIT;
                             CategoryEditDialog();
                             break;
                        case 1:         //  非表示化
                             setCategoryDispAttribute(mSelectTitle, false);
                             break;
                        case 2:         //  表示化
                             setCategoryDispAttribute(mSelectTitle, true);
                             break;
                        case 3:         //  削除
                             removeCategoryData(mSelectTitle);
                             DelDataFileDialog();    //  データファイルの削除確認
                             break;
                    }
                }
            })
            .create()
            .show();
    }

    /**
     * カテゴリ(分類)編集ダイヤログ
     */
    public void CategoryEditDialog() {
        mEditText = new EditText(this);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.setWidth(300);
        if (mCategoryEditMode == CATEGORYEDITMODE.EDIT)
            mEditText.setText(mSelectTitle);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(mEditText);
        // Show Dialog
        new AlertDialog.Builder(this)
            //	.setIcon(R.drawable.icon)
            .setTitle("分類の入力")
            .setView(mLinearLayout)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                /* OKボタンをクリックした時の処理 */
                if (mCategoryEditMode == CATEGORYEDITMODE.CREATE)
                    setCategoryData(mEditText.getText().toString());
                else if (mCategoryEditMode == CATEGORYEDITMODE.EDIT)
                     chgCategoryData(mSelectTitle, mEditText.getText().toString());
                loadData();
                }
                })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* Cancel ボタンをクリックした時の処理 */
                }
             })
             .show();
    }

    /***
     * 分類データリストファイル削除の確認ダイヤログ
     */
    public void DelDataFileDialog() {
        new AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage(getCategoryPath()+"のデータファイルも削除しますか")
            .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String path = getCategoryPath();
                    if (ylib.existsFile(path)) {
                        if (!ylib.deleteFile(path)) {
                            Toast.makeText(MainActivity.this, "データファイルが削除できませんでした", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(GpsInfoActivity.this, "Cancelしました。", Toast.LENGTH_SHORT).show();
                 }
            })
            .create()
            .show();
    }


    /***
     * 選択ファイルに対する操作メニュー
     * "開く","テキストとして開く","削除"
     */
    public void SetOpeMenuDialog() {
        if (mOpeMenuTitle.length==0)
            return;
        new AlertDialog.Builder(this)
            .setTitle("メニュー")
            .setItems(mOpeMenuTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "SetOpeMenuDialog: " + mOpeMenuTitle[which]);
                    switch (which) {
                        case 0:         //  開く
//                            executeFile(mSelectFileName);
                            ylib.executeFile(MainActivity.this, mSelectFileName);
                            break;
                        case 1:         //  テキストとして開く
                            goTextEdit(mSelectFileName);
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
     * カテゴリ(分類)の追加
     * @param category
     */
    private void setCategoryData(String category) {
        mList.addKeyData(category);
        mList.saveDataFile();
    }

    /**
     * カテゴリ(分類)名の変更
     * @param oldCategory
     * @param newCategory
     */
    private void chgCategoryData(String oldCategory, String newCategory) {
        if (mList.replaceKey(oldCategory, newCategory)) {
            mList.saveDataFile();
            ylib.rename(mSaveDirectory + "/" + LISTHEAD +"_"+ oldCategory + ".csv",
                    mSaveDirectory + "/" + LISTHEAD +"_"+ newCategory + ".csv");
            loadData();
        }
    }

    /**
     * カテゴリの表示属性の設定
     * @param Category
     * @param disp
     */
    private void setCategoryDispAttribute(String Category, boolean disp) {
        if (mList.setData(Category, "表示属性", disp?"表示":"非表示", true)) {
            mList.saveDataFile();
            loadData();
        }
    }

    /**
     * カテゴリ(分類)名の削除
     * @param category
     */
    private void removeCategoryData(String category) {
        if (mList.removeData(category)) {
            mList.saveDataFile();
            loadData();
        }
    }

    /**
     * データファイルのリストを取得する
     */
    private void loadData() {
        Log.d(TAG, "loadData:");
        mListDataAdapter.clear();
        if (mList.LoadFile()) {
            List<String[]> listData = mList.getListData();
            for (int i = 0; i < listData.size(); i++) {
                String title = listData.get(i)[mList.getTitlePos("分類")];
                String disp = listData.get(i)[mList.getTitlePos("表示属性")];
                if (mAllListDisp || disp.compareTo("非表示")!=0)
                    mListDataAdapter.add(String.format("[%04d] ", i) + title);
            }
        }
    }


    /**
     * リストのタイトルからファイルのタイトルを取り出す
     * @param item      リストのタイトル
     * @return          ファイルのタイトル
     */
    private String getListTitle(String item) {
        return item.substring(item.indexOf(']')+2);
    }


    /**
     * ファイル一覧選択画面を開く
     * @param mode      開き方(FILESELECTMODE.load/save)
     */
    private void FileSelectOpen(FILESELECTMODE mode) {
        if (mode==FILESELECTMODE.load) {
            goFileSelect("\\.*$", FileSelectActivity.FILESELECTPREFS, "NORMAL",
                    FileSelectActivity.FILESELECT, mDbInit);
        } else if (mode==FILESELECTMODE.save) {
            goFileSelect("\\.*$", FileSelectActivity.SAVEASFILEPREFS,"NORMAL",
                    FileSelectActivity.SAVEFILESELECT, mDbInit);
        }
        mDbInit = false;
    }

    /**
     * ファイル選択画面に移行
     * @param filter        ファイル一覧のフィルタ("\\.*"|"\\.gpx$"|...)
     * @param preference    ファイル一覧モードとフォルダ記憶キーワード (SaveAsFilePrefs/FileSelectPrefs/GpxSelectPrefs)
     * @param sort          ソートタイプ
     * @param activitytype  Activityの種類
     * @param init          初期化の有無
     */
    private void goFileSelect(String filter, String preference, String sort, int activitytype, boolean init) {
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

    Consumer<String> iLoadFile = new Consumer<String>() {
        @Override
        public void accept(String path) {
            ylib.messageDialog("選択ファイル", path);
//            loadStatisticsData(path);
//            mStatisticsFileName = ylib.getNameWithoutExt(path);
        }
    };

    /***
     * ファイル操作画面に移行
     * @param saveDirectory     ファイル保存ディレクトリ
     * @param category          分類名
     */
    private void goDataList(String saveDirectory,  String category) {
        Log.d(TAG, "goDataList: " + saveDirectory + " , " + category);
        Intent intent = new Intent(this, DataListActivity.class);
        intent.putExtra("SAVEDIR", saveDirectory);      //  ディレクトリ名名
        intent.putExtra("CATEGORY", category);          //	 分類名
        startActivity(intent);
    }


    /***
     * テキストファイルの編集画面
     * @param filepath      ファイルパス
     */
    private void goTextEdit(String filepath) {
        Log.d(TAG, "goTextEdit: " + filepath);
        Intent intent = new Intent(this, TextEdit.class);
        intent.putExtra("FILEPATH", filepath);	//	データファイル名
        startActivity(intent);
    }


    /**
     * 選択された項目のファイルパスを取得
     * @return      ファイルパス
     */
    private String getCategoryPath() {
        if (mSelectItem.length() <= 0)
            return "";
        String category = getListTitle(mSelectItem);
        return mSaveDirectory + "/" + LISTHEAD +"_"+ category + ".csv";
    }

    /**
     * 初期化
     */
    private void init() {
        mBtAddCategory = (Button)this.findViewById(R.id.button);
        mBtAddCategory.setOnClickListener(this);
        mBtFileOperation = (Button)this.findViewById(R.id.button2);
        mBtFileOperation.setOnClickListener(this);
        mListView = (ListView)this.findViewById(R.id.listView);
        mListDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mListView.setAdapter(mListDataAdapter);
        mListDataAdapter.add("TEST Comment");
        for (int i=0; i<60; i++)
            mListDataAdapter.add("Test List"+String.format("%03d",i));
    }
}
