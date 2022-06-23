package jp.co.yoshida.katsushige.ymemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.util.Consumer;

import jp.co.yoshida.katsushige.mylib.YLib;

public class TextEdit extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "TextEdit";

    private EditText mEditText;
    private Button mBtOpen;
    private Button mBtSaveAs;
    private Button mBtSave;
    private Button mBtNew;
    private Button mBtFileOpe;
    private Button mBtUp;
    private Button mBtDown;
    private Button mBtLeft;
    private Button mBtRight;

    private String mFileName;
    enum FILESELECTMODE {load,save}
    private String[] mOpeMenu = {"全選択","貼付け","コピー","フォント大","フォント小"};

    private YLib ylib;
    ClipboardManager cm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);

        init();
        ylib = new YLib(this);
        cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);    //システムのクリップボードを取得

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                CharSequence ext = extras.getCharSequence(Intent.EXTRA_TEXT);
                if (ext != null) {
                }
            }
        } else {
            Log.d(TAG, "onCreate:4");
            mFileName = intent.getStringExtra("FILEPATH");
            this.setTitle(ylib.getName(mFileName));
            LoadFile(mFileName);
        }

    }

    @Override
    public void onClick(View view) {
        Button button = (Button)view;
        int istart = mEditText.getSelectionStart();
        int iend = mEditText.getSelectionEnd();

        if (button.getText().toString().compareTo("読込")==0) {
//            FileSelectOpen(FILESELECTMODE.load);
        } else if (button.getText().toString().compareTo("保存")==0) {
//            FileSelectOpen(FILESELECTMODE.save);
        } else if (button.getText().toString().compareTo("上書")==0) {
            SaveFile(mFileName);
        } else if (button.getText().toString().compareTo("新規")==0) {
//            FileNew();
        } else if (button.getText().toString().compareTo("操作")==0) {
            ylib.setMenuDialog(TextEdit.this,"操作メニュー", mOpeMenu, iOperation);
        } else if (button.getText().toString().compareTo("大")==0) {
            float textsize = mEditText.getTextSize();
            mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,textsize*1.2f);
        } else if (button.getText().toString().compareTo("小")==0) {
            float textsize = mEditText.getTextSize();
            mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,textsize/1.2f);
        } else if (button.getText().toString().compareTo("↑")==0) {
            mEditText.setSelection(prevRow(mEditText.getText().toString(), mEditText.getSelectionStart()));
        } else if (button.getText().toString().compareTo("↓")==0) {
            mEditText.setSelection(nextRow(mEditText.getText().toString(), mEditText.getSelectionStart()));
        } else if (button.getText().toString().compareTo("←")==0) {
            int n = mEditText.getSelectionStart();
            mEditText.setSelection(0<n?n-1:0);
        } else if (button.getText().toString().compareTo("→")==0) {
            int n = mEditText.getSelectionStart();
            mEditText.setSelection(n<(mEditText.getText().length()-1)?n+1:(mEditText.getText().length()-1));
        }
    }


    /**
     * クリップボードのデータを編集画面に張り付ける
     */
    private void clipbordPaste() {
        ClipData cd = cm.getPrimaryClip();              //クリップボードからClipDataを取得
        if(cd != null) {
            String buf = mEditText.getText().toString();
            ClipData.Item item = cd.getItemAt(0);   //クリップデータからItemを取得
            buf = buf.substring(0, mEditText.getSelectionStart())+item.getText()+
                    buf.substring(mEditText.getSelectionEnd());
            mEditText.setText(buf);
        }
    }

    /**
     * 編集画面で選択した文字列をクリップボードにコピーする
     */
    private void clipbordCopy() {
        String text = mEditText.getText().toString().substring(mEditText.getSelectionStart(), mEditText.getSelectionEnd());
        Log.d(TAG,"コピー: "+text);
        //クリップボードに格納するItemを作成
        ClipData.Item item = new ClipData.Item(text);
        //MIMETYPEの作成
        String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        //クリップボードに格納するClipDataオブジェクトの作成
        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        //クリップボードにデータを格納
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
    }

    /**
     * メニュー入選択ダイヤログ(setMenuDialog)で選択した項目を実行する関数
     */
    Consumer<String> iOperation = new Consumer<String>() {
        @Override
        public void accept(String s) {
            if (s.compareTo("全選択")==0) {
                mEditText.selectAll();
            } else if (s.compareTo("貼付け")==0) {
                clipbordPaste();
            } else if (s.compareTo("コピー")==0) {
                clipbordCopy();
            } else if (s.compareTo("フォント大")==0) {
                float textsize = mEditText.getTextSize();
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,textsize*1.2f);
            } else if (s.compareTo("フォント小")==0) {
                float textsize = mEditText.getTextSize();
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,textsize/1.2f);
            }
        }
    };

    /**
     * 次の行にカーソル位置を移動する
     * @param text      対象文字列
     * @param cpos      現在のカーソル位置
     * @return          移動後のカーソル位置
     */
    private int nextRow(String text, int cpos) {
        int curRow = text.lastIndexOf('\n', cpos);          //  カレント行の始まりの位置
        if (curRow<0)
            curRow = 0;
        else
            curRow++;
        int rowPos = cpos - curRow;                         //  その行での位置
        rowPos = rowPos<0?0:rowPos;
        int nextStart = text.indexOf('\n', cpos);           //  次の行の始まりの位置
        if (nextStart < 0)
            return text.length() - 1;
        else
            nextStart++;
        int nextEnd = text.indexOf('\n', nextStart);        //  次の行の終わりの位置
        if (nextEnd < 0)
            nextEnd = text.length() - 1;
        else
            nextEnd--;
        int nextPos = nextStart + rowPos;
        return nextEnd<nextPos?nextEnd:(nextPos<0?0:nextPos);
    }

    /**
     * 前の行にカーソルを移動する
     * @param text      対象文字列
     * @param cpos      現在のカーソル位置
     * @return          移動後のカーソル位置
     */
    private int prevRow(String text, int cpos) {
        int prevEnd = text.lastIndexOf('\n', cpos) - 1;     //  前の行の終わり位置
        if (prevEnd<=0)
            return 0;
        int rowPos = cpos - (prevEnd + 2);                    //  その行での位置
        int prevStart = text.lastIndexOf('\n', prevEnd);    //  前の行の始まりの位置
        if (prevStart < 0)
            prevStart = 0;
        else
            prevStart++;
        int nextPos = prevStart + rowPos;
        return prevEnd<nextPos?prevEnd:(nextPos<0?0:nextPos);
    }

    /**
     * ファイルを読み込む
     * @param filePath      ファイルパス
     */
    private void LoadFile(String filePath) {
        Log.d(TAG, "LoadFile: " + filePath);
        if (!ylib.existsFile(filePath))
            return ;
        String buf = ylib.readFileData(filePath);
//        buf = buf.replace("\r", "\n");
        mEditText.setText(buf);
    }

    /**
     * ファイルに保存する
     * @param filePath      保存ファイル名(パス)
     */
    private void SaveFile(String filePath) {
        String buf = mEditText.getText().toString();
        ylib.writeFileData(filePath, buf);
    }

    private void init() {
        mEditText = (EditText) this.findViewById(R.id.editText3);
        mEditText.setGravity(Gravity.TOP);
        mBtFileOpe = (Button) this.findViewById(R.id.button9);
        mBtSave = (Button) this.findViewById(R.id.button10);
        mBtUp = (Button) this.findViewById(R.id.button11);
        mBtDown = (Button) this.findViewById(R.id.button12);
        mBtLeft = (Button) this.findViewById(R.id.button13);
        mBtRight = (Button) this.findViewById(R.id.button14);

        mBtFileOpe.setOnClickListener(this);
        mBtSave.setOnClickListener(this);
        mBtUp.setOnClickListener(this);
        mBtDown.setOnClickListener(this);
        mBtLeft.setOnClickListener(this);
        mBtRight.setOnClickListener(this);
    }
}
