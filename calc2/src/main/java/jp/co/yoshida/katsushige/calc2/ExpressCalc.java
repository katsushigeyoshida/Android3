package jp.co.yoshida.katsushige.calc2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import jp.co.yoshida.katsushige.mylib.YLib;

public class ExpressCalc extends AppCompatActivity {

    private ExpressCalcView mExpressCalcView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express_calc);

        YLib ylib = new YLib();
        if (ylib.checkStragePermission(this))
            init();
    }

    private void init() {
        this.setTitle("計算式処理");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//	画面を縦に固定する

        //requestWindowFeature(Window.FEATURE_NO_TITLE);        //  ウインドウタイトルバーを非表示にする　有効?
        mExpressCalcView = new ExpressCalcView(this);   //  ExpressCalcViewを生成
        setContentView(mExpressCalcView);                      //  ExpressCalcViewを画面に指定
    }
}
