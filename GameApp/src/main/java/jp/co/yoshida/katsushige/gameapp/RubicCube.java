package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class RubicCube extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "RubicCube";

    private LinearLayout mLinearLauout;
    private Button mBtFront;
    private Button mBtBack;
    private Button mBtUp;
    private Button mBtDown;
    private Button mBtLeft;
    private Button mBtRight;
    private Button mBtFront2;
    private Button mBtBack2;
    private Button mBtUp2;
    private Button mBtDown2;
    private Button mBtLeft2;
    private Button mBtRight2;
    private Button mBtReset;
    private Button mBtRandom;
    private Spinner mSpLevel;
    private Spinner mSpCubeSize;
    private GLView mGLView;
    private ArrayAdapter<String> mLevelAdapter;
    private ArrayAdapter<String> mCubeSizeAdapter;

    private int mOperationCount = 5;        //  ランダム化の操作回数
    private int mTiltAngle = 15;            //  一回の操作で回転する角度
    private long waitTime = 100;            //  問題作瀬パターン表示間隔(ms)
    private int mCubes = 3;
    private float mCubeSize = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rubic_cube);

        init();
        mGLView.requestRender();

        //  ランダム化の回数指定
        mSpLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mOperationCount = getProbremLevel();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        //  ルービックキューブのサイズ変更
        mSpCubeSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mSpCubeSize.getSelectedItemPosition() == 0) {
                    mCubes = 2;
                    mCubeSize = 2.0f;
                } else {
                    mCubes = 3;
                    mCubeSize = 1.0f;
                }
                mGLView.mGLRenderer.setCubeSize(mCubes, mCubeSize);
                mGLView.requestRender();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        int ang = 30;
        if (view.getId() == mBtFront.getId()) {
            translateCube(5, ang);
        } else if (view.getId() == mBtFront2.getId()) {
            translateCube(5, -ang);
        } else if (view.getId() == mBtBack.getId()) {
            translateCube(4, ang);
        } else if (view.getId() == mBtBack2.getId()) {
            translateCube(4, -ang);
        } else if (view.getId() == mBtUp.getId()) {
            translateCube(3, ang);
        } else if (view.getId() == mBtUp2.getId()) {
            translateCube(3, -ang);
        } else if (view.getId() == mBtDown.getId()) {
            translateCube(2, ang);
        } else if (view.getId() == mBtDown2.getId()) {
            translateCube(2, -ang);
        } else if (view.getId() == mBtRight.getId()) {
            translateCube(1, ang);
        } else if (view.getId() == mBtRight2.getId()) {
            translateCube(1, -ang);
        } else if (view.getId() == mBtLeft.getId()) {
            translateCube(0, ang);
        } else if (view.getId() == mBtLeft2.getId()) {
            translateCube(0, -ang);
        } else if (view.getId() == mBtReset.getId()) {
            mGLView.mGLRenderer.initCube();;
            mGLView.requestRender();
        } else if (view.getId() == mBtRandom.getId()) {
            createProblemCube();
        }
    }

    /**
     * ランダム化の操作回数の取得
     * @return
     */
    private int getProbremLevel() {
        int l = mSpLevel.getSelectedItemPosition();
        return l < 0 ? 3 : Integer.valueOf(mLevelAdapter.getItem(l));
    }

    /**
     * ランダム化の実施
     * アニメーション表示
     */
    private void createProblemCube()
    {
        mGLView.mGLRenderer.initCube();;
        mGLView.requestRender();

        int operationCount = 90 / mTiltAngle * mOperationCount;
        int face = 0;
        while (0 < operationCount) {
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
            }
            if (operationCount % (90 / mTiltAngle) == 0)
                face = (int)Math.floor(Math.random() * 6);
            translateCube(face,  mTiltAngle);
            mGLView.requestRender();
            operationCount--;
        }
    }

    /**
     * Cubeの回転
     * @param face      面の種類
     * @param ang       回転角(deg)
     */
    private void translateCube(int face, int ang) {
        //  回転面の位置
        float pos = (mCubes - 1f) / 2f * mGLView.mGLRenderer.mCubeSize;
        //  指定面の回転
        for (int x = 0; x < mCubes; x++) {
            for (int y = 0; y < mCubes; y++) {
                for (int z = 0; z < mCubes; z++) {
                    if (face == 0) {        //  L
                        if ( mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.X == -pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(ang, 0, 0);
                        }
                    } else if (face == 1) { //  R
                        if (mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.X == pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(ang, 0, 0);
                        }
                    } else if (face == 2) { //  D
                        if (mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.Y == -pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(0, ang, 0);
                        }
                    } else if (face == 3) { //  U
                        if (mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.Y == pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(0, ang, 0);
                        }
                    } else if (face == 4) { //  B
                        if (mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.Z == -pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(0, 0, ang);
                        }
                    } else if (face == 5) { //  F
                        if (mGLView.mGLRenderer.mCube[x][y][z].mTranPosInt.Z == pos) {
                            mGLView.mGLRenderer.mCube[x][y][z].setAddAngle(0, 0, ang);
                        }
                    }
                }
            }
        }
        mGLView.requestRender();
    }

    /**
     * 画面コントロールの初期化
     */
    private void init() {
        mBtFront = (Button)findViewById(R.id.button16);
        mBtBack = (Button)findViewById(R.id.button17);
        mBtUp = (Button)findViewById(R.id.button18);
        mBtDown = (Button)findViewById(R.id.button19);
        mBtLeft = (Button)findViewById(R.id.button20);
        mBtRight = (Button)findViewById(R.id.button21);
        mBtFront2 = (Button)findViewById(R.id.button22);
        mBtBack2 = (Button)findViewById(R.id.button23);
        mBtUp2 = (Button)findViewById(R.id.button24);
        mBtDown2 = (Button)findViewById(R.id.button25);
        mBtLeft2 = (Button)findViewById(R.id.button26);
        mBtRight2 = (Button)findViewById(R.id.button27);
        mBtReset = (Button)findViewById(R.id.button28);
        mBtRandom = (Button)findViewById(R.id.button29);
        mSpLevel = (Spinner)findViewById(R.id.spinner5);
        mSpCubeSize = (Spinner)findViewById(R.id.spinner6);
        mLinearLauout = (LinearLayout)findViewById(R.id.linearLayoutCube);

        mBtFront.setOnClickListener(this);
        mBtBack.setOnClickListener(this);
        mBtUp.setOnClickListener(this);
        mBtDown.setOnClickListener(this);
        mBtLeft.setOnClickListener(this);
        mBtRight.setOnClickListener(this);
        mBtFront2.setOnClickListener(this);
        mBtBack2.setOnClickListener(this);
        mBtUp2.setOnClickListener(this);
        mBtDown2.setOnClickListener(this);
        mBtLeft2.setOnClickListener(this);
        mBtRight2.setOnClickListener(this);
        mBtReset.setOnClickListener(this);
        mBtRandom.setOnClickListener(this);

        String[] levelItems = {"3","5","10","20","50"};
        mLevelAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, levelItems);
        mSpLevel.setAdapter(mLevelAdapter);
        mSpLevel.setSelection(mLevelAdapter.getPosition("5"));
        String[] cubeSizeItem = {"2x2x2","3x3x3"};
        mCubeSizeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, cubeSizeItem);
        mSpCubeSize.setAdapter(mCubeSizeAdapter);
        mSpCubeSize.setSelection(mCubeSizeAdapter.getPosition(("3x3x3")));

        mGLView = new GLView(this);
        mLinearLauout.addView(mGLView);
    }
}
