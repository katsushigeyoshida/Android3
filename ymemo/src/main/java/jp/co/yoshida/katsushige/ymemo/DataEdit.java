package jp.co.yoshida.katsushige.ymemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import jp.co.yoshida.katsushige.mylib.FileSelectActivity;
import jp.co.yoshida.katsushige.mylib.ListData;
import jp.co.yoshida.katsushige.mylib.YLib;

public class DataEdit extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "DataEdit";

    private Spinner mSpDataFile;
    private EditText mEdCategory;
    private EditText mEdTitle;
    private EditText mEdFilePath;
    private Spinner mSpFileType;
    private Button mBtCategoryRef;
    private Button mBtFileRef;
    private Button mBtSave;
    private Button mBtCancel;
    private ArrayAdapter<String> mDataFileAdapter;
    private ArrayAdapter<String> mFileTypeAdapter;

    private String mSaveDirectory;      //  データファイル保存ディレクトリ
    private String mIndexFileName;      //  データインデックスファイル名
    private String mDataTitle;          //  データのタイトル
    private String mDataFilePath;       //  データファイルのパス
    private String mCategoryName;       //  データファイルの分類
    private String mDataFileType;       //  データの種別
    private String[] mDataFormat;

    enum FILESELECTMODE {load,save}
    private ListData mList;

    ClipboardManager cm = null;
    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_edit);

        ylib = new YLib(this);
        this.cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);    //システムのクリップボードを取得
        init();

        mSaveDirectory = ylib.setSaveDirectory(getPackageName().substring(getPackageName().lastIndexOf('.')+1));
        mIndexFileName = "Ymemo";
        mDataTitle = "";
        mDataFilePath = "";
        mCategoryName = "";

        loadDataFileList();             //  インデックスデータファイル(大分類)の取り込みと設定

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            //  外部から起動された時
            Bundle extras = intent.getExtras();
            if (extras != null) {
                CharSequence ext = extras.getCharSequence(Intent.EXTRA_TEXT);
                if (ext != null) {
                    String[] text = ext.toString().split("\n");
                    mIndexFileName = "Ymemo_WEB";
                    mDataFormat = DataListActivity.mDataFormat;
                    if (0 < text.length)
                        mEdFilePath.setText(text[0]);
                    if (1 < text.length)
                        mEdTitle.setText(text[1]);
                    Log.d(TAG,"onCreate: Intent: "+text.length+" "+ext+" "+ mIndexFileName);
                }
            }
        } else {
            mSaveDirectory = intent.getStringExtra("SAVEDIR");  //  データ保存ディレクトリ
            mIndexFileName = intent.getStringExtra("FILENAME"); //  リストデータ登録ファイル名
            mDataTitle = intent.getStringExtra("DATATITLE");    //  データタイトル
//            mDataFilePath = intent.getStringExtra("DATANAME");  //  データファイル名(mEdFilePath)
//            mCategoryName = intent.getStringExtra("CATEGORY");  //  カテゴリ名(分類)
            String buf = intent.getStringExtra("DATAFORMAT");   //  データファイルの項目
            mDataFormat = ylib.splitCsvString(buf);
            setDataFileNam(mIndexFileName);
            Log.d(TAG, "onCreate:4 "+ mIndexFileName +" "+ mIndexFileName.substring(mIndexFileName.indexOf("_")+1)+" "+ mDataFilePath +" "+mCategoryName);
        }

        mList = new ListData(this, mDataFormat);
        mList.setSaveDirectory(mSaveDirectory, mIndexFileName);
        mList.LoadFile();
        if (0 < mDataTitle.length()) {
            mBtSave.setText("更新");
            getData(mDataTitle);
            setDispData(mDataTitle);
        } else {
            mBtSave.setText("登録");
            mEdCategory.setText(mCategoryName);
        }

    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        if (button.getId() == mBtFileRef.getId()) {
            FileSelectOpen(FILESELECTMODE.load);
        } else if (button.getId() == mBtCategoryRef.getId()) {
            ylib.setMenuDialog(this, "分類選択", mList.getListDataArray("分類"), iCategorySet);
        } else if (button.getId() == mBtSave.getId()) {
            if (0 < mEdTitle.getText().length() && 0 <mEdFilePath.getText().length()){
                if (button.getText().toString().compareTo("登録")==0) {
                    saveData();
                    mBtSave.setText("更新");
                    finish();
                } else if (button.getText().toString().compareTo("更新")==0) {
                    saveData();
                    finish();
                }
            } else {
                Toast.makeText(this, "タイトルまたはファイルが設定され問いません", Toast.LENGTH_LONG).show();
            }
        } else if (button.getId() == mBtCancel.getId()) {
            finish();
        }
//        } else if (button.getText().toString().compareTo("貼付")==0) {
//            ClipData cd = cm.getPrimaryClip();              //クリップボードからClipDataを取得
//            if(cd != null) {
//                ClipData.Item item = cd.getItemAt(0);   //クリップデータからItemを取得
//                mEdFilePath.setText(item.getText());
//            }
//        } else if (button.getText().toString().compareTo("アプリ")==0) {
//            goAppSelect();
//        } else if (button.getText().toString().compareTo("クリア")==0) {
//            clearData();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // requestCodeがサブ画面か確認する
        if (requestCode == MainActivity.FILESELECT_ACTIVITY) {
            //	ファイル選択
            if (resultCode == RESULT_OK) {
                mIndexFileName = (String)data.getCharSequenceExtra("FileName");
                Log.d(TAG, "onActivityResult: FILESELECT" + mIndexFileName);
                mEdFilePath.setText(mIndexFileName);
                mEdTitle.setText(ylib.getNameWithoutExt(mIndexFileName));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 画面データをリストファイルに保存
     */
    private void saveData() {
        //  spinner で選択されたインデックスファイルデータの取得
        String indexFileName = MainActivity.LISTHEAD + "_" +
                mDataFileAdapter.getItem(mSpDataFile.getSelectedItemPosition());
        if (mIndexFileName.compareTo(indexFileName)!=0) {
            Log.d(TAG, "saveData: "+indexFileName+" "+mIndexFileName);
            mIndexFileName = indexFileName;
            mList.setSaveDirectory(mSaveDirectory, mIndexFileName);
            mList.LoadFile();
        }
        //  ファイルデータの登録
        if (mBtSave.getText().toString().compareTo("登録") == 0) {
            mDataTitle = mEdTitle.getText().toString();
        } else {
            if (mDataTitle.compareTo(mEdTitle.getText().toString())==0) {
            } else {
                mList.removeData(mDataTitle);
                mDataTitle = mEdTitle.getText().toString();
            }
        }
        getDispData();
        Log.d(TAG, "saveData: [" + mDataTitle + "][" + mCategoryName + "]["
                + mDataFilePath + "][" + mDataFileType +"]");
        setData(mDataTitle);
        //  ファイルデータの保存
        mList.saveDataFile();
    }

    /**
     * 分類データを画面にセットするための関数インターフェース
     */
    Consumer<String> iCategorySet = new Consumer<String>() {
        @Override
        public void accept(String s) {
            mEdCategory.setText(s);
        }
    };

    /**
     * ファイル選択画面の表示
     * @param mode  =FILESELECTMODE.load/FILESELECTMODE.save
     */
    private void FileSelectOpen(FILESELECTMODE mode) {
        if (mode==FILESELECTMODE.load) {
            goFileSelect("\\.*$", "FileSelectPrefs","NORMAL",FileSelectActivity.FILESELECT);
        } else if (mode==FILESELECTMODE.save) {
            goFileSelect("\\.*$", "SaveAsFilePrefs","NORMAL",FileSelectActivity.SAVEFILESELECT);
        }
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
     */
    private void goFileSelect(String filter,String preference,String sort,int activitytype) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Intent intent = new Intent(this, FileSelectActivity.class);
            intent.putExtra("FILTER", filter);
            intent.putExtra("PREFERENCE", preference);
            intent.putExtra("SORTTYPE",sort);
            startActivityForResult(intent,activitytype);
        } else {
            Toast.makeText(this, "FileSelectActivity エラー", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 指定項目のデータを取得して画面に表示
     * @param title
     */
    private void setDispData(String title) {
        getData(title);
        mEdTitle.setText(title);
        mEdCategory.setText(mCategoryName);
        mEdFilePath.setText(mDataFilePath);
        mSpFileType.setSelection(mFileTypeAdapter.getPosition(cnvFileType2Title(mDataFileType)));
    }

    /**
     * 画面データを取り込む
     */
    private void getDispData() {
        mDataTitle = mEdTitle.getText().toString();
        mCategoryName = mEdCategory.getText().toString();
        mDataFilePath = mEdFilePath.getText().toString();
        mDataFileType = cnvTitle2FileType(mSpFileType.getSelectedItem().toString());
    }

    /**
     * 指定の項目8タイトル)にデータを保存する
     * @param dataTitle     タイトルデータ(キーデータ)
     */
    private void setData(String dataTitle ) {
        mList.setData(dataTitle, "タイトル", mDataTitle, true);
        mList.setData(dataTitle, "ファイル", mDataFilePath, true);
        mList.setData(dataTitle, "分類", mCategoryName, true);
        mList.setData(dataTitle, "種別", mDataFileType, true);
    }

    /**
     * 指定項目のデータを取り込む
     * @param dataTitle     タイトルデータ(キーデータ)
     */
    private void getData(String dataTitle ) {
        mDataFilePath = mList.getData(dataTitle, "ファイル");
        mCategoryName = mList.getData(dataTitle, "分類");
        mDataFileType = mList.getData(dataTitle, "種別");
    }

    /**
     * 大分類の項目(分類ファイル名)をコンボボックスに設定
     * @param indexFileName     大分類の項目のファイル名
     */
    private void setDataFileNam(String indexFileName) {
        int n = mDataFileAdapter.getPosition(mIndexFileName.substring(indexFileName.indexOf("_")+1));
        mSpDataFile.setSelection(n);
    }

    /**
     * ファイルの種別を表示用表記に変換する
     * @param fileType      ファイル種別
     * @return              表示用表記
     */
    private String cnvFileType2Title(String fileType) {
        for (Map.Entry<String, String> keyValue : DataListActivity.mFileTypeMap.entrySet()) {
            if (keyValue.getValue().compareTo(fileType) == 0) {
                return keyValue.getKey();
            }
        }
        return "";
    }

    /**
     * 表示用表記をファイル種別コードに変換する
     * @param title     表示用表記
     * @return          ファイル種別コード
     */
    private String cnvTitle2FileType(String title) {
        return DataListActivity.mFileTypeMap.get(title);
    }

    /**
     * インデックスファイルからデータファイルのリストを取得しspinnerに登録
     */
    private void loadDataFileList() {
        ListData dataFileList = new ListData(this, MainActivity.mDataFormat);
        dataFileList.setSaveDirectory(mSaveDirectory, MainActivity.mListName);
        Log.d(TAG,"loadDataFileList: "+mSaveDirectory+" "+MainActivity.mListName);
        mDataFileAdapter.clear();
        if (dataFileList.LoadFile()) {
            List<String> title = dataFileList.getListData("分類");
            for (int i = 0; i < dataFileList.getSize(); i++) {
                Log.d(TAG,"loadDataFileList: "+title.get(i));
                mDataFileAdapter.add(title.get(i));
            }
        }
    }

    /**
     * 初期化
     */
    private void init() {
        mSpDataFile = (Spinner)findViewById(R.id.spinner2);
        mEdCategory = (EditText)findViewById(R.id.editText4);
        mEdTitle = (EditText)findViewById(R.id.editText5);
        mEdFilePath = (EditText)findViewById(R.id.editText6);
        mSpFileType = (Spinner)findViewById(R.id.spinner3);
        mBtCategoryRef = (Button)findViewById(R.id.button15);
        mBtFileRef = (Button)findViewById(R.id.button16);
        mBtSave = (Button)findViewById(R.id.button18);
        mBtCancel = (Button)findViewById(R.id.button17);

        mEdCategory.setText("");
        mEdTitle.setText("");
        mEdFilePath.setText("");

        mDataFileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mSpDataFile.setAdapter(mDataFileAdapter);

        String[] fileType = new String[DataListActivity.mFileTypeMap.size()];
        int i = 0;
        for (String type : DataListActivity.mFileTypeMap.keySet())
            fileType[i++] = type;
        mFileTypeAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, fileType);
        mSpFileType.setAdapter(mFileTypeAdapter);

        mBtCategoryRef.setOnClickListener(this);
        mBtFileRef.setOnClickListener(this);
        mBtSave.setOnClickListener(this);
        mBtCancel.setOnClickListener(this);

        //  フォーカスをボタンに移して起動時にキーボードが出るのを防ぐ
        mBtCategoryRef.setFocusable(true);
        mBtCategoryRef.setFocusableInTouchMode(true);
        mBtCategoryRef.requestFocus();
    }

}
