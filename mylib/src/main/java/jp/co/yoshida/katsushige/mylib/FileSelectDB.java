package jp.co.yoshida.katsushige.mylib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * ファイル選択のデータベースアクセスクラス
 */
public class FileSelectDB {

    static final String DATABASE_NAME = "fileSelect.db";
    static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "favFolder";
    public static final String COL_ID = "_id";
    public static final String COL_PATH = "path";

    protected final Context mContext;
    protected FileSelectDBHelper mDbHelper;
    protected SQLiteDatabase mDb = null;	// = new SQLiteDatabase();

    public FileSelectDB(Context context) {
        this.mContext = context;
        mDbHelper = new FileSelectDBHelper(this.mContext);
    }

    private static class FileSelectDBHelper extends SQLiteOpenHelper {

        public FileSelectDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_PATH + " TEXT NOT NULL"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * データベースのテーブルを一度削除して開く
     */
    public void upgrade() {
        mDb = mDbHelper.getWritableDatabase();
        try {
            mDbHelper.onUpgrade(mDb, 0, 0);
        } catch (Exception e) {
            Log.d("--AS--", "DBAdapter onupgrade:" + e.toString());
        }
        mDbHelper.close();
    }

    /**
     * データベースを開く
     *
     * @return
     */
    public FileSelectDB open() {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * データベースを閉じる
     *
     * @return
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * データベースの全データ削除
     *
     * @return
     */
    public boolean deleteAllData() {
        return mDb.delete(TABLE_NAME, null, null) > 0;
    }

    /**
     * データベースからデータの削除
     *
     * @param id
     * @return
     */
    public boolean deleteData(int id) {
        return mDb.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
    }

    public boolean deleteTitle(String title) {
        return mDb.delete(TABLE_NAME, COL_PATH + " = " + title, null) > 0;
    }

    /**
     * データベースから全データ取得
     *
     * @return
     */
    public Cursor getAllData() {
        return mDb.query(TABLE_NAME, null, null, null, null, null, null);
    }

    /**
     * データベースにデータを書き込む
     *
     * @param path
     */
    public void saveData(String path) {
        ContentValues values = new ContentValues();
        values.put(COL_PATH, path);
        mDb.insertOrThrow(TABLE_NAME, null, values);
    }
}
