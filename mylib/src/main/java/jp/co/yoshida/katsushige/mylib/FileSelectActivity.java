package jp.co.yoshida.katsushige.mylib;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ファイル一覧選択機能
 * 呼び出し方
 * 　　　　Intent intent = new Intent(this, FileSelectActivity.class);
 *        intent.putExtra("FILTER", filter);            フィルタ("\\.csv$")
 *        intent.putExtra("PREFERENCE", preference);    保存キーワード(モードとディレクトリ)("FileSelectPrefs"/"SaveAsFilePrefs")
 *        intent.putExtra("SORTTYPE",sort);             ソートの種別(現状は無効で前回値を使用)
 *        intent.putExtra("DBINIT", init);              データベースの初期化(「お気に入りフォルダ」)
 *        startActivityForResult(intent, activitytype);
 */
public class FileSelectActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "FileSelectActivity";

    ListView mListView = null;
    EditText mFileEditText;
    Button mBtOk;
    Button mBtCancel;

    public static final int FILESELECT = 1002;
    public static final int SHARESELECT = 1003;
    public static final int SAVEFILESELECT = 1004;
    public static final int DIRSELECT = 1005;
    public static final int FILEOPERATION = 1006;

    public static final String SAVEASFILEPREFS = "SaveAsFilePrefs";    //	フォルダ選択とファイル名入力モード
    public static final String FILESELECTPREFS = "FileSelectPrefs";    //	ファイル選択、ファイル名変更不
    public static final String DIRSELECTPREFS = "DirSelectPrefs";      //	フォルダ選択
    public static final String DIRSELECTFILEDISPPREFS = "DirSelectFileDispPrefs";    //	フォルダ選択とファイル名表示
    public static final String MULTISELECTPREFS = "MultiSelctPrefs";   //	複数ファイル選択

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//, new Locale("ja", "JP", "JP"));

    private ArrayList<fileListData> mList = null;
    private FileListAdapter mAdapter = null;

    private String mFilterPattern = "\\.*$";            //	ファイル一覧の正規表現フィルタ("\\.*"|"\\.gpx$"|...)
    private String mPreferenceKey = "FileSelectPrefs";  //	ファイル一覧モードとフォルダ記憶キーワード (SaveAsFilePrefs/FileSelectPrefs/GpxSelectPrefs)
    private boolean mFileSelectMode = true;             //  ファイル選択モード
    private boolean mFileDispMode = true;               //  ファイル表示モード
    private boolean mMultiSelectMode = false;           //  複数選択
    private String[] FileOperationTitle ={
            "コピー","移動","削除","フォルダ作成","ファイル名変更","ソート","お気に入り","お気に入り登録"};
    private String[] mSortNameList ={
            "名前順","日付順","サイズ順","名前逆順","日付逆順","サイズ逆順"};
    enum SortType {name, date, size, revname, revdate, revsize};
    private SortType mSortType = SortType.name;
    private SortType[] mSortIntList = {
            SortType.name,SortType.date,SortType.size,SortType.revname,SortType.revdate,SortType.revsize};
    private String mDir;
    private String mExternalStorageDir;         //  外部ストレージのディレクトリ
    private String mFileName;
    private FileFilter mFileFilter;             //  ファイル検索のフィルタ(mFilterPatternを設定)
    private String mSourceFile = "";            //  移動元/コピー元ファイル名
    private String mDestDir = "";               //  移動先/コピー先ディレクトリ名
    enum FileOperation { non, copy, move, delete, rename, mkdir ; }
    FileOperation mFileOpe = FileOperation.non;

    private static FileSelectDB mDbAdapter;     //  ファイル選択データベース
    private boolean mDbInit = false;            //  DB初期化有無
    private String[] mFavFolderList;            //  「お気に入りフォルダ」リスト

    YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        ylib = new YLib(this);
        init();

        makeFileFilter();               //  ファイル選択フィルタの設定
        getFileList(mFileDispMode);     //  ファイルリストの取得

        //	デーベースの設定
        mDbAdapter = new FileSelectDB(this);
        if (mDbInit) {
            mDbAdapter.upgrade();
        }
        if (0 < dbDataCount())
            loadData();

        //  ファイルリストをクリックした時の処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView listView = (ListView) adapterView;
                fileListData item = (fileListData) listView.getItemAtPosition(position);
                Log.d(TAG, String.format("onItemClick: %s %s", mDir, item.toString()));
                Toast.makeText(getApplicationContext(), item + " clicked", Toast.LENGTH_SHORT).show();
                if (item.isDirectory()) {
                    //  ディレクトリを選択した場合
                    if (item.getFileName().compareTo("..") == 0) {
                        File file = new File(mDir);
                        mDir = file.getParent();
                    } else
                        mDir += "/" + item.getFileName();
                    getFileList(mFileDispMode);
                } else {
                    //  ファイルを選択した場合
                    if (mFileSelectMode) {
                        mFileName = item.getFileName();
                        if (mPreferenceKey.compareTo("SaveAsFilePrefs") == 0) {
                            mFileEditText.setText(mFileName);
                        } else {
                            Intent data = new Intent();
                            data.putExtra("FileName", mDir + "/" + mFileName);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
                }
                return;
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView listView = (ListView) adapterView;
                fileListData item = (fileListData) listView.getItemAtPosition(position);
                mSourceFile = mDir + "/" + item.fileName;
                //Toast.makeText(getApplicationContext(), "long clicked: " + SourceFile, Toast.LENGTH_SHORT).show();
                FileOpeDialog();            //	ファイル操作リストの表示

                return true;    // 戻り値をtrueにするとOnClickイベントは発生しない
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SetPreferencePreFolder(mDir);       //  ディレクトリの保存
        SetPreferenceSortType(mSortType);   //  ソートタイプの保存
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtOk.getId()) {
            //	ファイル名をフルパスで受け渡して終了
            mFileName = mFileEditText.getText().toString();
            Intent data = new Intent();
            data.putExtra("FileName", mDir + "/" + mFileName);
            setResult(RESULT_OK, data);
            finish();
        } else if (view.getId() == mBtCancel.getId()) {
            //  Canccelで終了
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILESELECT) {
            //	目的地リストファイルの選択結果
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: FILESELECT " + data.getCharSequenceExtra("FileName"));
            }
        } else if (requestCode == SHARESELECT) {
            //	共有アプリ起動
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onActivityResult: SHARESELECT " + data.getCharSequenceExtra("FileName"));
            }
        } else if (requestCode == SAVEFILESELECT) {
            //	ファイル保存
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onActivityResult: SAVEFILESELECT " + data.getCharSequenceExtra("FileName"));
            }
        } else if (requestCode == DIRSELECT) {
            //ディレクトリ選択
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, data.getCharSequenceExtra("FileName") + "が選択されました", Toast.LENGTH_SHORT).show();
                mDestDir = (String) data.getCharSequenceExtra("FileName");
                Log.d(TAG, "onActivityResult: DIRSELECT "+ mFileOpe + ":" + mSourceFile + " " + mDestDir);
                if (mFileOpe == FileOperation.copy) {
                    ylib.copyFile(mSourceFile, mDestDir);
                } else if (mFileOpe == FileOperation.move) {
                    ylib.moveFile(mSourceFile, mDestDir);
                }
                mFileOpe = FileOperation.non;
                getFileList(mFileDispMode);
            }
        } else if (requestCode == FILEOPERATION) {
            //ファイル操作
            if (resultCode == RESULT_OK) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初期化
     */
    private void init() {
        //  コントロールの設定
        mListView = (ListView) this.findViewById(R.id.listView);
        mFileEditText = (EditText) this.findViewById(R.id.editText);
        mBtOk = (Button) this.findViewById(R.id.button2);
        mBtCancel = (Button) this.findViewById(R.id.button);
        mBtOk.setOnClickListener(this);
        mBtCancel.setOnClickListener(this);
        mFileEditText.setText("");

        //  Intentで起動された時のパラメータの取得
        Intent intent = getIntent();
        mFilterPattern = intent.getStringExtra("FILTER");     //	ファイル一覧のフィルタ("\\.*"|"\\.gpx$"|...)
        mPreferenceKey = intent.getStringExtra("PREFERENCE"); //	ファイル一覧モードとフォルダ記憶キーワード (SaveAsFilePrefs/FileSelectPrefs/GpxSelectPrefs)
        String sort = intent.getStringExtra("SORTTYPE");    //  ソートの種類
        mDbInit = intent.getBooleanExtra("DBINIT", false);  //  データベース蜀かフラグ

        //  モードごとの設定
        if (mPreferenceKey.compareTo(SAVEASFILEPREFS)==0) {
            //	フォルダ選択とファイル名入力モード
            mFileSelectMode = true;
            mFileDispMode = true;
            mFileEditText.setVisibility(View.VISIBLE);
            mBtOk.setVisibility(View.VISIBLE);
            mBtCancel.setVisibility(View.VISIBLE);
        } else if (mPreferenceKey.compareTo(FILESELECTPREFS)==0) {
            //	ファイル選択、ファイル名変更不可
            mFileSelectMode = true;
            mFileDispMode = true;
            mFileEditText.setVisibility(View.INVISIBLE);
            mBtOk.setVisibility(View.INVISIBLE);
            mBtCancel.setEnabled(true);
        } else if (mPreferenceKey.compareTo(DIRSELECTPREFS)==0) {
            //	フォルダ選択
            mFileSelectMode = false;        //  ファイル選択不可
            mFileDispMode = false;           //  ファイル表示なし
            mFileEditText.setVisibility(View.INVISIBLE);
            mBtOk.setVisibility(View.VISIBLE);
            mBtCancel.setVisibility(View.VISIBLE);
        } else if (mPreferenceKey.compareTo(DIRSELECTFILEDISPPREFS)==0) {
            //	フォルダ選択、ファイル名表示
            mFileSelectMode = false;        //  ファイル選択不可
            mFileDispMode = true;           //  ファイル表示あり
            mFileEditText.setVisibility(View.INVISIBLE);
            mBtOk.setVisibility(View.VISIBLE);
            mBtCancel.setVisibility(View.VISIBLE);
        } else {
            //	ファイル選択モード
            mFileSelectMode = true;
            mFileDispMode = true;
            mFileEditText.setEnabled(false);
            mBtOk.setEnabled(false);
            mBtCancel.setEnabled(false);
        }

        //	ファイル一覧のソートタイプ
        mSortType = GetPrefernceSortType();
        //  初期ディレクトリの設定
        mDir = mExternalStorageDir = Environment.getExternalStorageDirectory().toString();
        //	前回のディレクトリを取り出す
        mDir = GetPreferncePreFolder();

    }



    /**
     * ファイル操作ダイヤログ
     */
    public void FileOpeDialog() {
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("ファイル操作")
                .setItems(FileOperationTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "SetModeDialog: "+FileOperationTitle[which]);
                        Toast.makeText(FileSelectActivity.this, FileOperationTitle[which], Toast.LENGTH_SHORT).show();
                        mFileOpe = FileOperation.non;
                        if (FileOperationTitle[which].compareTo("コピー")==0) {
                            mFileOpe = FileOperation.copy;
                            nextFileSelect();
                        } else if (FileOperationTitle[which].compareTo("移動")==0) {
                            mFileOpe = FileOperation.move;
                            nextFileSelect();
                        } else if (FileOperationTitle[which].compareTo("削除")==0) {
                            mFileOpe = FileOperation.delete;
                            FileDeleteDialog();
                        } else if (FileOperationTitle[which].compareTo("フォルダ作成")==0) {
                            mFileOpe = FileOperation.mkdir;
                            DirCreateDialog();
                        } else if (FileOperationTitle[which].compareTo("ファイル名変更")==0) {
                            mFileOpe = FileOperation.rename;
                            RenameDialog();
                        } else if (FileOperationTitle[which].compareTo("ソート")==0) {
                            SortListDialog();
                        } else if (FileOperationTitle[which].compareTo("お気に入り")==0) {
                            loadData();
                            dbFavFolderDialog();
                        } else if (FileOperationTitle[which].compareTo("お気に入り登録")==0) {
                            saveData(mDir);
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * 移動先/コピー先のディレクトリ選択
     */
    private void nextFileSelect() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Intent intent = new Intent(FileSelectActivity.this, FileSelectActivity.class);
            intent.putExtra("FILTER", "\\.*");
            intent.putExtra("PREFERENCE", "DirSelectPrefs");
            intent.putExtra("SORTTYPE","NORMAL");
            startActivityForResult(intent,DIRSELECT);
        } else {
            Log.d(TAG, "nextFileSelect: Error"+ mSourceFile);
            //Toast.makeText(this,"エラー",1000);
        }
    }

    /**
     * ファイル名変更ダイヤログ
     */
    public void RenameDialog() {
        editDialogText = new EditText(this);
        editDialogText.setText(ylib.getName(mSourceFile));
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("ファイル名変更")
                .setView(editDialogText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(FileSelectActivity.this, editDialogText.getText(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "RenameDialog: " + mDir + "/" + editDialogText.getText().toString());
                        ylib.rename(mSourceFile, mDir + "/" + editDialogText.getText().toString());
                        getFileList(mFileDispMode);
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


    /**
     * フォルダ作成ダイヤログ
     */
    EditText editDialogText;
    public void DirCreateDialog() {
        editDialogText = new EditText(this);
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("フォルダ作成")
                .setView(editDialogText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(FileSelectActivity.this, editDialogText.getText(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "DirCreateDialog: " + mDir + "/" + editDialogText.getText().toString());
                        ylib.mkdir(mDir + "/" + editDialogText.getText().toString());
                        getFileList(mFileDispMode);
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

    /**
     * ソート種別選択ダイヤログ
     */
    public void SortListDialog() {
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("ファイルソート")
                .setItems(mSortNameList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mSortType = Int2SortType(which);
                        Log.d(TAG, "SortListDialog: "+mSortNameList[which]+","+mSortType);
                        getFileList(mFileDispMode);
                    }
                })
                .create()
                .show();
    }

    /**
     * 	お気に入りフォルダ表示
     */
    public void dbFavFolderDialog() {
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("お気に入りフォルダ")
                .setItems(mFavFolderList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "SetModeDialog: "+mFavFolderList[which]);
                        mDir = mFavFolderList[which];
                        getFileList(true);
                    }
                })
                .create()
                .show();
    }

    /**
     * 「お気に入りフォルダ」で登録したフォルダを取り出す
     */
    private void loadData() {
        Log.d(TAG, "loadData:0");
        mDbAdapter.open();
        Cursor c = mDbAdapter.getAllData();
        startManagingCursor(c);
        if (c.moveToFirst()) {
            mFavFolderList = new String[c.getCount()];
            int i=0;
            do {
                mFavFolderList[i] = c.getString(c.getColumnIndex(FileSelectDB.COL_PATH));
                Log.d(TAG, "loadData:" + c.getCount() + ":" + mFavFolderList[i]);
                i++;
            } while (c.moveToNext() && i<c.getCount());
            if (i!=c.getCount())
                Toast.makeText(FileSelectActivity.this, "データかあっていません "+i+" "+c.getCount(), Toast.LENGTH_SHORT).show();
        } else {
            mFavFolderList = null;
        }
        mDbAdapter.close();
    }

    /**
     * ファイル削除ダイヤログ
     */
    public void FileDeleteDialog() {
        new AlertDialog.Builder(FileSelectActivity.this)
                .setTitle("確認")
                .setMessage(mSourceFile + "  を削除します")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "FileDeleteDialog: " + mSourceFile + "  を削除します");
                        if (ylib.deleteFile(mSourceFile))
                            Toast.makeText(FileSelectActivity.this, "データを削除しました。", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(FileSelectActivity.this, "削除に失敗しました。", Toast.LENGTH_SHORT).show();
                        getFileList(mFileDispMode);
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

    /**
     * 「お気に入りフォルダ」をDBに保存
     * @param path      フォルダパス
     */
    private void saveData(String path) {
        Log.d(TAG, "saveData:0");
        mDbAdapter.open();
        mDbAdapter.saveData(path);
        mDbAdapter.close();
    }

    /**
     * データベースの登録数を取得
     * @return      データ数
     */
    private int dbDataCount() {
        mDbAdapter.open();
        Cursor c = mDbAdapter.getAllData();
        int n = c.getCount();
        mDbAdapter.close();
        return n;
    }

    /**
     * ファイル検索時のフィルタ作成(関数インターフェース)
     * FileクラスのlistFiles(FileFilter)メソッドに渡す
     * 拡張子による選択の場合　"\\.ext$"
     */
    void makeFileFilter(){
        mFileFilter = new FileFilter() {
            //  指定された抽象パス名がパス名リストに含まれる必要があるかどうかを判定
            //  file - テスト対象の抽象パス名
            //  戻り値: file が含まれる必要がある場合は true
            public boolean accept(File file) {
                //  正規表現によるパターンマッチ
                Pattern p = Pattern.compile(mFilterPattern ,Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(file.getName());
                //  パターンマッチしたファイルとディレクトリを含み隠しファイルは除く
                boolean shown = (m.find()||file.isDirectory())&&!file.isHidden();
                return shown;
            }
        };
    }

    /**
     * ファイルリストの取得
     * @param fileselect	falseならフォルダのみ
     */
    void getFileList(boolean fileselect ) {
        try {
            //  外部ストレージの確認
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                finish();
            //  ファイルとフォルダの取得
            File file = new File(mDir);
            if (!file.exists()) {
                //  指定ディレクトリが存在しない場合外部ストレージを参照
                mDir = mExternalStorageDir;
                file = new File(mDir);
                if (!file.exists()) {
                    mDir = "/";
                    file = new File(mDir);
                }
            }

            this.setTitle(mDir);
            File[] fc = file.listFiles(mFileFilter);    //  ファイルのフィルタ設定
            qsort(fc, 0, fc.length-1);      //  ファイルのソート

            //  ファイルリストの登録
            mList = new ArrayList<fileListData>();
            if (mDir.compareTo("/") != 0) {
                //  ルートディレクトリ以外は[..]をリストに追加
                mList.add(new fileListData("..", "", 0, true));
            }
            for (int i = 0; i < fc.length; i++) {
                if (fc[i].isFile() && !fileselect)  //  ファイル選択以外はファイルの登録を除外
                    continue;
                mList.add(new fileListData(fc[i].getName(),
                        dateFormat.format(fc[i].lastModified()), fc[i].length(), fc[i].isDirectory()));
            }

            //  リストビューにファイルを登録(ファイル選択モードの処理)
            if (mMultiSelectMode) {
                //  複数選択
                mAdapter = new FileListAdapter(this, android.R.layout.simple_list_item_checked, mList);
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            } else {
                //  単一選択
                mAdapter = new FileListAdapter(this, R.layout.list_item, mList);
            }
            mListView.setAdapter(mAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "get File List エラー"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ファイルリストのソート
     * @param a         ファイルリスト
     * @param left
     * @param right
     */
    private void qsort(File a[], int left, int right) {
        int i,j;
        File s,t;
        if (left < right) {
            s = a[(left+right)/2];
            i = left - 1;
            j = right + 1;
            while (true) {
                while (0 > fileCompare(a[++i],s));
                while (0 < fileCompare(a[--j],s));
                if (i >= j)
                    break;
                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
            qsort(a, left, i-1);
            qsort(a, j+1, right);
        }
    }

    /**
     * ソートの比較 (ディレクトリ優先、ファイル名順)
     * mSortType: 0:名前順 1:日付順 3:サイズ順
     * @param fc1       対象ファイル1
     * @param fc2       対象ファイル2
     * @return          比較結果
     */
    private long fileCompare(File fc1, File fc2) {
        if (fc1.isDirectory() && fc2.isFile()) {
            return -65536L;
        } else if (fc1.isFile() && fc2.isDirectory()) {
            return 65536L;
        } else {
            if (mSortType==SortType.date) {
                return fc1.lastModified() - fc2.lastModified();
            } else if (mSortType==SortType.size) {
                return fc1.length() - fc2.length();
            } else if (mSortType==SortType.revname) {
                return (long)fc2.getName().compareToIgnoreCase(fc1.getName());
            } else if (mSortType==SortType.revdate) {
                return fc2.lastModified() - fc1.lastModified();
            } else if (mSortType==SortType.revsize) {
                return fc2.length() - fc1.length();
            } else {
                return (long)fc1.getName().compareToIgnoreCase(fc2.getName());
            }
        }
    }

    /**
     * 前回のフォルダ位置をプリファレンスから取得
     * @return
     */
    private String GetPreferncePreFolder() {
        String externalStorageDir = Environment.getExternalStorageDirectory().toString();
        SharedPreferences pref = this.getSharedPreferences(mPreferenceKey, MODE_PRIVATE);
        return pref.getString("Folder",externalStorageDir);
    }

    /**
     * フォルダ位置をプリファレンスに登録
     * @param dir
     */
    private void SetPreferencePreFolder(String dir) {
        SharedPreferences pref = this.getSharedPreferences(mPreferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Folder", dir);
        editor.commit();
    }

    /**
     * ソートタイプの前回値をプリファレンスから取得
     * @return
     */
    private SortType GetPrefernceSortType() {
        SharedPreferences pref = this.getSharedPreferences(mPreferenceKey, MODE_PRIVATE);
        return String2SortType(pref.getString("SorType",mSortNameList[0]));
    }

    /**
     * ソートタイプをプリファレスに登録
     * @param sorttype
     */
    private void SetPreferenceSortType(SortType sorttype) {
        SharedPreferences pref = this.getSharedPreferences(mPreferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("SorType", SortType2String(sorttype));
        editor.commit();
    }

    /**
     * ソートタイプをソートNoに変換
     * @param type      ソートタイプ
     * @return          ソートNo
     */
    private int SortType2Int(SortType type) {
        for (int i=0; i<mSortIntList.length; i++) {
            if (mSortIntList[i]==type)
                return i;
        }
        return 0;
    }

    /**
     * ソートNoをソートタイプに変換
     * @param type      ソートNo
     * @return          ソートタイプ
     */
    private SortType Int2SortType(int type) {
        return mSortIntList[type % mSortIntList.length];
    }

    /**
     * ソートタイプをソート名に変換
     * @param type      ソートタイプ
     * @return          ソート名
     */
    private String SortType2String(SortType type) {
        return mSortNameList[SortType2Int(type)];
    }

    /**
     * ソート名をソートタイプに変換
     * @param type      ソート名
     * @return          ソートタイプ
     */
    private SortType String2SortType(String type) {
        for (int i=0; i<mSortNameList.length; i++) {
            if (mSortNameList[i].compareTo(type)==0)
                return mSortIntList[i];
        }
        return mSortIntList[0];
    }

    /**
     * リストビューへの登録アダプタ
     * @author katsushige
     *
     */
    private class FileListAdapter extends ArrayAdapter {
        private ArrayList<fileListData> items;
        private LayoutInflater inflater;

        public FileListAdapter(Context context, int textViewResourceId, ArrayList<fileListData> items  ) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = inflater.inflate(R.layout.list_item, null);
                //view = inflater.inflate(android.R.layout.simple_list_item_checked, null);
                // 背景画像をセットする
                //view.setBackgroundResource(R.drawable.back);
            }
            // 表示すべきデータの取得
            fileListData item = (fileListData)items.get(position);
            if (item != null) {
                TextView fileName = (TextView)view.findViewById(R.id.textView1);
                fileName.setTypeface(Typeface.DEFAULT_BOLD);

                TextView fileDate = (TextView)view.findViewById(R.id.textView2);
                TextView fileSize = (TextView)view.findViewById(R.id.textView3);
                if (fileName != null) {
                    fileName.setText(item.getFileName());
                }
                if (fileDate != null) {
                    fileDate.setText(item.getDate());
                }
                if (fileSize != null) {
                    NumberFormat form = NumberFormat.getNumberInstance();
                    fileSize.setText(form.format(item.getSize()));
                }

                ImageView fIcon = (ImageView)view.findViewById(R.id.listIcon);
                if(item.isDirectory()){
                    fIcon.setImageResource(R.drawable.folder);
                }else{
                    fIcon.setImageResource(R.drawable.text);
                }
            }
            return view;
        }

    }

    /**
     * ファイルリストのデータ構造
     * @author katsushige
     *
     */
    private class fileListData {
        String fileName;		//	ファイル名
        String date;			//	ファイル日付
        long size;				//	ファィルサイズ
        boolean dir;			//	ディレクトリかどうか

        public fileListData(String fileName, String date, long size, boolean dir) {
            this.fileName = fileName;
            this.date = date;
            this.size = size;
            this.dir = dir;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDate() {
            return date;
        }

        public long getSize() {
            return size;
        }
        public boolean isDirectory() {
            return dir;
        }

        public String toString() {
            return fileName + " - " + date + " " + (dir?"<Directory>":"<File>");
        }
    }
}
