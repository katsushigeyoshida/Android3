package jp.co.yoshida.katsushige.gameapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jp.co.yoshida.katsushige.mylib.YLib;

public class Sudoku extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "Sudoku";

    private Spinner mSpPattern;
    private Button mBtPatternRegist;
    private Button mBtDataDel;
    private Button mBtDataSave;
    private Button mBtDataRestor;
    private Button mBtClear;
    private Button mBtSolver;
    private Button mBtCreatProblem;
    private CheckBox mCbSupport;
    private SudokuView mSudokuView;
    private LinearLayout mLinearLayout;
    private ArrayAdapter<String> mPatternAdapter;

    private Map<String, String> mDataList = new LinkedHashMap<String, String>();    //  問題パターンリスト
    private String mSaveDirectory;                  //  ファイル保存ディレクトリ
    private String mSaveFileName = "SudokuData";    //  問題パターンファイル名
    private String mSvaeBoardName = "SudokuBoard";
    private String mSaveFilePath;                   //  保存ファイルのフルパス
    private String mSaveBoardPath;
    private String mExportDirectory;
    private String mImportDirectory;

    private final int MENU00 = 0;
    private final int MENU01 = 1;

    private String mHelpText = "使い方\n"+
            "1) 基本的な使い方\n"+
            "　盤のマス目にタッチすると1づつ数値が増える\n" +
            "　クリアボタンを押すと最後にタッチしたマス目がクリアされる\n" +
            "　薄いグレイは数値の変更できない場所を示す(問題パターン)\n" +
            "　クリアボタンを長押しすると入力した内容がすべて消える\n" +
            "　再度長押しをすると問題パターンも含めてすべて消える\n" +
            "2) 問題の登録方法\n" +
            "　盤をすべてクリアにして問題の数値を各マス目に設定する\n" +
            "　問題の設定が終わったら登録ボタンをおして名称を入力します\n" +
            "　登録された問題はドロップダウンリストにでてくるのでその中から選択する\n" +
            "3) 解答方法\n" +
            "　空白のマス目をタッチして数値を入力する\n" +
            "　タッチすると盤の上部にマス目に入力できる数値が表示される\n" +
            "　行、列、ブロックで一つでも1-9までそろうとマス目の背景がシアンにかわる\n" +
            "　盤が完成すると上部に赤で「完成」が表示される\n" +
            "　一時中だする時は保存ボタンを押して盤面を記憶させる\n" +
            "　再開するときは前回行っていた問題を選択して復元ボタンを押す\n" +
            "　右上のオプションメニューから「解法補助機能表示」を選択すると\n" +
            "　下部に1-9までの数字が表示され、その数値を選択すると選択した\n" +
            "　数値が使えないマス目に青線が引かれるので解法の参考にできる\n" +
            "　解答ボタンを押すと問題を解いて表示する"
            ;

    private YLib ylib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        this.setTitle("数独");
        ylib = new YLib(this);
        ylib.checkStragePermission(this);

        //  問題パターンファイル名の設定
        String packageName = getPackageName().substring(getPackageName().lastIndexOf('.')+1);
        mSaveDirectory = ylib.setSaveDirectory(packageName);
        mSaveFilePath = mSaveDirectory +"/" + mSaveFileName + ".csv";
        mSaveBoardPath = mSaveDirectory +"/" + mSvaeBoardName + ".csv";
        loadPatternData(mDataList, mSaveFilePath, false);   //  問題パターンの取り込み
        mImportDirectory = ylib.getStrPreferences("IMPORTFOLDER", this);
        if (!ylib.isDirectory(mImportDirectory))
            mImportDirectory = mSaveDirectory;
        mExportDirectory = ylib.getStrPreferences("EXPORTFOLDER", this);
        if (!ylib.isDirectory(mExportDirectory))
            mExportDirectory = mSaveDirectory;

        init();                                     //  初期化
        patternSetSpinner();                        //  問題パターンをスピナーに瀬つて

        //  問題パターンの切り替え
        mSpPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                mSudokuView.setPatternBoard(mDataList.get(mPatternAdapter.getItem(spinner.getSelectedItemPosition())));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onCreate:mSpPattern Nothing: " + mSpPattern.getSelectedItemPosition());
            }
        });    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtPatternRegist.getId()) {        //  問題パターンの登録
            dataNameInputDialog();
        } else if (view.getId() == mBtDataDel.getId()) {       //  問題パターンの削除
            deletePattern();
        } else if (view.getId() == mBtDataSave.getId()) {      //  データ一時保存
            saveData();
        } else if (view.getId() == mBtDataRestor.getId()) {    //  データ復元
            loadData();
        } else if (view.getId() == mBtClear.getId()) {         //  ボードのクリア
            mSudokuView.curCellClear();
        } else if (view.getId() == mBtSolver.getId()) {        //  解法
            solve();
        } else if (view.getId() == mBtCreatProblem.getId()) {  //  問題作成
            createProblem();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == mBtClear.getId()) {         //  ボードのクリア
            mSudokuView.boardClear();
        }
        return true;
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Toast.makeText(this, "解法補助機能切り替え", Toast.LENGTH_SHORT).show();
        mSudokuView.setSupportModeSW(b);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add(Menu.NONE, MENU00, Menu.NONE, "データインポート");
        item1.setIcon(android.R.drawable.ic_menu_set_as);
        MenuItem item2 = menu.add(Menu.NONE, MENU01, Menu.NONE, "データエクスポート");
        item2.setIcon(android.R.drawable.ic_menu_set_as);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case MENU00:                        //
                Toast.makeText(this, "データインポートが選択されました", Toast.LENGTH_SHORT).show();
                importData();
                break;
            case MENU01:                        //
                Toast.makeText(this, "データエクスポートが選択されました", Toast.LENGTH_SHORT).show();
                exportData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 問題を解いて盤に反映する
     */
    private void solve() {
        int[] board = new int[81];
        //  問題のパターンをSolverに合わせて変換する
        String pattern = mDataList.get(mPatternAdapter.getItem(mSpPattern.getSelectedItemPosition()));
        for (int i = 0; i<81; i++)
            board[i] = Integer.valueOf(String.valueOf(pattern.charAt(i)));
        //  Solverにデータをセットして開放する
        SudokuSolver solver = new SudokuSolver(board);
        solver.preCheck();                 //  解法前に候補データを求める
        if (solver.solver(0)) {         //  解法の実施
            board = solver.getResult();
            mSudokuView.setSolveData(board, solver.getCount());
        } else {
            mSudokuView.completeMessage("解答なし 操作回数:"+solver.getCount());
        }
    }

    private void createProblem(){
        CreatSudokuProblem creatProblem = new CreatSudokuProblem();
        int[] creatBoard = creatProblem.getCreatProblem(50, 1000);
        String buf = "";
        for (int i = 0; i < creatBoard.length; i++)
            buf += creatBoard[i];

        mSudokuView.setPatternBoard(buf);
    }

    /**
     * 問題パターンをフォルダを選択してファイル名を指定してエキスポートする
     */
    private void exportData() {
        ylib.folderSelectDialog(this, mExportDirectory, iInputFileName);
    }

    /**
     * ファイル名を入力する関数インターフェース
     */
    Consumer<String> iInputFileName = new Consumer<String>() {
        @Override public void accept(String s) {
            mExportDirectory = s;
            ylib.setStrPreferences(mExportDirectory, "EXPORTFOLDER", Sudoku.this);
            ylib.inputDialog(Sudoku.this, s, mSaveFileName+".csv", iSaveAs);
        }
    };

    /**
     * 選択したフォルダと指定したファイル名で問題パターンをエキスポートする
     */
    Consumer<String> iSaveAs = new Consumer<String>() {
        @Override public void accept(String s) {
            savePatternData(mDataList, mExportDirectory+"/"+s);
            Toast.makeText(Sudoku.this, mExportDirectory+"/"+s+" にエキスポートしました",
                    Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 問題パターンのファイルをファイル選択で選択し追加でインポートする
     */
    private void importData() {
        ylib.fileSelectDialog(this, mImportDirectory,"*.csv", true, iFileOperation);
    }

    /**
     * 問題パターンをファイル選択で選択したファイルでインポートする
     */
    Consumer<String> iFileOperation = new Consumer<String>() {
        @Override public void accept(String s) {
            Toast.makeText(Sudoku.this, s+" をインポートしました",
                    Toast.LENGTH_SHORT).show();
            loadPatternData(mDataList, s, true);   //  問題パターンの取り込み
            patternSetSpinner();                        //  問題パターンをスピナーに瀬つて
            mImportDirectory = ylib.getDir(s);
            ylib.setStrPreferences(mImportDirectory, "IMPORTFOLDER", Sudoku.this);
        }
    };


    /**
     * 盤の状態を復元する
     */
    private void loadData() {
        Map<String, String> dataList = new LinkedHashMap<String, String>();
        loadPatternData(dataList, mSaveBoardPath, false);
        String patterName = mPatternAdapter.getItem(mSpPattern.getSelectedItemPosition());
        if (dataList.containsKey(patterName)) {
            String boardData = dataList.get(patterName);
            mSudokuView.setCurBoard(boardData);
        } else
            Toast.makeText(this, "データが存在しません", Toast.LENGTH_LONG).show();
    }

    /**
     * 盤面のデータをパターン名で保存する
     */
    private void saveData() {
        String boardData = mSudokuView.getCurBoard();
        String patterName = mPatternAdapter.getItem(mSpPattern.getSelectedItemPosition());
        Map<String, String> dataList = new LinkedHashMap<String, String>();
        loadPatternData(dataList, mSaveBoardPath, false);
        dataList.put(patterName, boardData);
        savePatternData(dataList, mSaveBoardPath);
    }

    /**
     * 選択されているパターンの削除
     */
    private void deletePattern() {
        String pattern = mPatternAdapter.getItem(mSpPattern.getSelectedItemPosition());
        mDataList.remove(pattern);
        patternSetSpinner();
        savePatternData(mDataList, mSaveFilePath);
    }

    /**
     * 問題パターン名の入力と登録
     */
    private void dataNameInputDialog() {
        final EditText editText = new EditText(this);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        editText.setText(dateFormat.format(date));
        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(editText);
        new AlertDialog.Builder(this)
                //	.setIcon(R.drawable.icon)
                .setTitle("データ名")
                .setView(mLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "EditTextDialog:　OKボタン " + editText.getText().toString());
                        /* OKボタンをクリックした時の処理 */
                        mDataList.put(editText.getText().toString(), mSudokuView.getCurBoard());
                        savePatternData(mDataList, mSaveFilePath);
                        mSudokuView.setFixPos();
                        patternSetSpinner();
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
     * ファイルから問題パターン(CSV)を読み込む
     * @param dataList      問題パターンリスト(Map)
     * @param path          パターンファイルパス
     * @param add           追加読込フラグ
     * @return              取込みの成否
     */
    private boolean loadPatternData(Map<String, String> dataList, String path, boolean add) {
        //	ファイルの存在確認
        if (!ylib.existsFile(path)) {
            Toast.makeText(this, "データが登録されていません\n"+path, Toast.LENGTH_LONG).show();
            return false;
        }

        //	ファイルデータの取り込み
        ArrayList<String> fileData = new ArrayList<String>();
        if (!add)
            fileData.clear();
        ylib.readTextFile(path, fileData);
        if (fileData.size()<1)
            return false;

        //	データを複写する
        for (int i=0; i<fileData.size(); i++) {
            String[] text = ylib.splitCsvString(fileData.get(i));
            if (1 < text.length)
                dataList.put(text[0], text[1]);
        }
        return true;
    }

    /**
     * 問題パターンをファイルに書き込む
     * @param dataList      問題パターンリスト(Map)
     * @param path          パターンファイルパス
     */
    private void savePatternData(Map<String, String> dataList, String path) {
        String buffer = "";
        //  計算式リストをStringにバッファリングする
        for (Map.Entry<String, String> entry : dataList.entrySet()) {
            Log.d(TAG,"savePatternData: "+entry.getKey() + "," + entry.getValue());
            if (entry.getValue() != null) {                  //  タイトルのみ
                buffer += entry.getKey() + "," + entry.getValue() + "\n";
                Log.d(TAG,"savePatternData: "+entry.getKey() + "," + entry.getValue());
            }
        }
        //  ファイルに保存
        if (0 < buffer.length()) {
            ylib.writeFileData(path, buffer);
            Toast.makeText(this, path + "\n保存しました", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "データがありません", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 問題パターン選択スピナーを更新する
     */
    private void patternSetSpinner() {
        String patternName[] = new String[mDataList.size()];
        int n = mDataList.size();
        for (String key : mDataList.keySet())
            patternName[--n] = key;
        mPatternAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, patternName);
        mSpPattern.setAdapter(mPatternAdapter);
    }

    /**
     * 初期化
     */
    private void init() {
        mBtPatternRegist = (Button)findViewById(R.id.button);
        mBtDataDel = (Button)findViewById(R.id.button2);
        mBtDataSave = (Button)findViewById(R.id.button3);
        mBtDataRestor = (Button)findViewById(R.id.button4);
        mBtClear = (Button)findViewById(R.id.button5);
        mBtSolver = (Button)findViewById(R.id.button6);
        mBtCreatProblem = (Button)findViewById(R.id.button30);
        mCbSupport= (CheckBox)findViewById(R.id.checkBox);
        mSpPattern = (Spinner)findViewById(R.id.spinner);
        mLinearLayout = (LinearLayout)findViewById(R.id.linearLayout);

        mBtPatternRegist.setOnClickListener(this);
        mBtDataDel.setOnClickListener(this);
        mBtDataSave.setOnClickListener(this);
        mBtDataRestor.setOnClickListener(this);
        mBtClear.setOnClickListener(this);
        mBtSolver.setOnClickListener(this);
        mBtCreatProblem.setOnClickListener(this);
        mBtClear.setOnLongClickListener(this);
        mCbSupport.setOnCheckedChangeListener(this);

        mSudokuView = new SudokuView(this);
        mLinearLayout.addView(mSudokuView);
    }
}

/**
 * 数独の解法
 *
 * 解法手順(バックトラックに
 * 1.各セルに対してその行と列、3x3のブロックに対して使用していない数値を候補として記録する
 * 2.候補が1つしかない場合は確定値として盤データに記録し再度を候補値を検索する
 * 3.1つだけの候補値がなくなるまで1と2を繰り返す。
 * 4.各セルに対して順番に候補値をいれて盤の完成を確認する
 * 5.候補値を入れるとき1と同じように重複がないことを確認しあれば次の候補値を使う
 * 6.最終セルまで行って完成していなければ一つ戻って次の候補値を使う
 * 7.候補値がなければさらに戻って同じことを行う
 *
 *
 *
 */
class SudokuSolver {
    private int[][][] mBoard = new int[9][9][11];
    private int count;

    /**
     * コンストラクタ
     * @param board     問題パターン
     */
    public SudokuSolver(int[] board) {
        initBoard();
        for (int y=0; y<mBoard.length; y++) {
            for (int x=0; x<mBoard[y].length; x++) {
                mBoard[y][x][0] = board[y*mBoard.length+x];
            }
        }
        count = 0;
    }

    /**
     * 実行結果の取得
     * @return      盤の状態
     */
    public int[] getResult() {
        int[] result = new int[81];
        int n = 0;
        for (int y=0; y < 9; y++)
            for (int x=0; x < 9; x++)
                result[n++] = mBoard[y][x][0];
        return result;
    }

    /**
     * 解法手順の操作回数
     * @return      操作回数
     */
    public int getCount() {
        return count;
    }

    /**
     * 数独の解法処理(preCheck()を実行した後におこなう)
     * @param n     セルの番号(位置)
     * @return      完成の成否
     */
    public boolean solver(int n) {
        int x = n % 9;
        int y = n / 9;
        // System.out.println("No."+n+" y="+y+" x="+x);
        if (8<y) {
            if (completeCheck())
                return true;
            else
                return false;
        }
        if (mBoard[y][x][0]==0) {
            int i=0;
            do {
                i++;
                // System.out.println("No."+n+" "+i+" "+mBoard[y][x][i]);
                if (rowChexk(x, mBoard[y][x][i]) && columnChexk(y, mBoard[y][x][i]) && blockChexk(x, y, mBoard[y][x][i])) {
                    mBoard[y][x][0] = mBoard[y][x][i];
                    // dispBoard();
                    count++;
                    if (x==8 && y==8) {
                        if (completeCheck())
                            return true;
                        else {
                            mBoard[y][x][0] = 0;
                            return false;
                        }
                    }
                    if (solver(n+1))
                        return true;
                }
            } while (mBoard[y][x][i+1]!=0);
        } else {
            return solver(n+1);
        }
        mBoard[y][x][0] = 0;
        return false;
    }

    /**
     * 盤が完成したかをチェックする
     * @return      true: 完成
     */
    private boolean completeCheck() {
        for (int x=0; x<9; x++)
            if (!rowChexk(x))
                return false;
        for (int y=0; y<9; y++)
            if (!columnChexk(y))
                return false;
        for(int x=0; x<9; x+=3) {
            for (int y=0; y<9; y+=3) {
                if (!blockChexk(x, y))
                    return false;
            }
        }
        return true;
    }

    /**
     * 列単位で重複チェックを行う
     * @param x     列番
     * @return      true:重複なし
     */
    private boolean rowChexk(int x) {
        int[] check = new int[10];
        for (int y=0; y<9; y++) {
            if (mBoard[y][x][0] == 0)
                return false;
            if (check[mBoard[y][x][0]]==1)
                return false;
            else
                check[mBoard[y][x][0]] = 1;
        }
        return true;
    }

    /**
     * 行単位で重複のチェックを行う
     * @param y     行番
     * @return      true:重複なし
     */
    private boolean columnChexk(int y) {
        int[] check = new int[10];
        for (int x=0; x<9; x++) {
            if (mBoard[y][x][0] == 0)
                return false;
            if (check[mBoard[y][x][0]]==1)
                return false;
            else
                check[mBoard[y][x][0]] = 1;
        }
        return true;
    }

    /**
     * ブロック単位で重複のチェックを行う
     * @param x     列番
     * @param y     行番
     * @return      true:重複なし
     */
    private boolean blockChexk(int x, int y) {
        int[] check = new int[10];
        int ox = x/3*3;
        int oy = y/3*3;
        for (x=ox; x<ox+3; x++) {
            for (y=oy; y<oy+3; y++) {
                if (mBoard[y][x][0] == 0)
                    return false;
                if (check[mBoard[y][x][0]]==1)
                    return false;
                else
                    check[mBoard[y][x][0]] = 1;
            }
        }
        return true;
    }


    /**
     * 事前チェックで確定する候補がなくなるまで候補地を求める
     */
    public void preCheck() {
        do {
            preSubCheck();
        } while (0 < fixCheck());
    }

    /**
     * 各セルに対して候補となる値を求める
     */
    private void preSubCheck() {
        for (int y=0; y<mBoard.length; y++) {
            for (int x=0; x<mBoard[y].length; x++) {
                if (mBoard[y][x][0]==0) {
                    setPass(x, y);
                }
            }
        }
    }

    /**
     * 候補が一つしかない場合は確定値とする
     * @return      確定値に変更した数
     */
    private int fixCheck() {
        int n=0;
        for (int y=0; y<mBoard.length; y++) {
            for (int x=0; x<mBoard[y].length; x++) {
                if (mBoard[y][x][0]==0) {
                    if (0<mBoard[y][x][1] && mBoard[y][x][2]==0) {
                        mBoard[y][x][0] = mBoard[y][x][1];
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /**
     * 候補となる値を登録
     * @param x     列番
     * @param y     行番
     */
    private void setPass(int x, int y) {
        int n=1;
        for (int k=1; k<=9; k++)
            mBoard[y][x][k] = 0;
        for (int val=1; val<=9; val++) {
            if (rowChexk(x, val) && columnChexk(y, val) && blockChexk(x, y, val))
                mBoard[y][x][n++] = val;
        }
    }

    /**
     * 一列の中に同じ値があるチェック
     * @param x     列番
     * @param val   値
     * @return      true:重複なし
     */
    private boolean rowChexk(int x, int val) {
        for (int y=0; y<9; y++) {
            if (mBoard[y][x][0] == val)
                return false;
        }
        return true;
    }

    /**
     * 一行の中に同じ値があるかチェック
     * @param y     行番
     * @param val   値
     * @return      true:重複なし
     */
    private boolean columnChexk(int y, int val) {
        for (int x=0; x<9; x++) {
            if (mBoard[y][x][0] == val)
                return false;
        }
        return true;
    }

    /**
     * ブロックの中に同じ値があるかチェック
     * @param x     列番
     * @param y     行番
     * @param val   値
     * @return      true:重複なし
     */
    private boolean blockChexk(int x, int y, int val) {
        int ox = x/3*3;
        int oy = y/3*3;
        for (x=ox; x<ox+3; x++) {
            for (y=oy; y<oy+3; y++) {
                if (mBoard[y][x][0] == val)
                    return false;
            }
        }
        return true;
    }

    /**
     * ボードの初期化
     */
    private void initBoard() {
        for (int i=0; i<mBoard.length; i++) {
            for (int j=0; j<mBoard[i].length; j++) {
                for (int k=0; k<mBoard[i][j].length; k++)
                    mBoard[i][j][k] = 0;
            }
        }
    }
}
