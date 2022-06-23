package jp.co.yoshida.katsushige.mylib;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

//  Web上のファイルをダウンロードする
//  使用例
//  WebFileDownLoad webFileDownLoad = new WebFileDownLoad();
//  webFileDownLoad.mUrl = mUrl;            //  ダウンロード先のURL
//  webFileDownLoad.mOutPath = filePath;    //  ファイルの出力パス
//  webFileDownLoad.start();                //  スレッドの開始
//  while (webFileDownLoad.isAlive()) {     //  処理完了を待つ
//      Thread.sleep(100L);
//  }
public class WebFileDownLoad extends Thread {
    private final String TAG = "WebFileDownLoad";

    public String mUrl;
    public String mOutPath;
    public Drawable mDrawable;

    public void run() {
        try {
            URL url = new URL(mUrl);
            InputStream is = (InputStream)url.getContent();
            FileOutputStream outputStream = new FileOutputStream(mOutPath);
//            mDrawable = Drawable.createFromStream(is, "");
            fileCopy(is, outputStream);
            outputStream.close();
            is.close();
        } catch (Exception e) {
            Log.d(TAG, "WebFileDownLoad Exception: " + e.getMessage());
        }
    }

    //  Stream間ファイルコピー
    private void fileCopy(InputStream is, OutputStream os) {
        int size = 1024 * 4;
        byte[] buf = new byte[size];
        try {
            while (-1 != (size = is.read(buf))) {
                os.write(buf, 0, size);
            }
            os.flush();
            is.close();
            os.close();
        } catch (Exception e) {
            Log.d(TAG, "fileCopy Exception: " + e.getMessage());
        }
    }
}