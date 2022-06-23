package jp.co.yoshida.katsushige.gameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    class SubProgram {
        Class mClass;
        String mName;
        public SubProgram(Class classs,String name) {
            mClass = classs;
            mName = name;
        }
    }

    ListView mListView;
    Spinner mSimulationNameSelect;
    ArrayAdapter<String> mSimulationNameAdapter;
    ArrayList<SubProgram> mSubProgram = new ArrayList<SubProgram>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //	画面をスリープさせない
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);	//	画面を縦に固定する

        init();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Toast.makeText(this, "Selected: " + String.valueOf(position), Toast.LENGTH_SHORT).show();
        if (position < mSubProgram.size()) {
            if (mSubProgram.get(position) != null) {
                Intent intent = new Intent(this, mSubProgram.get(position).mClass);
                startActivity(intent);
            }
        }
    }

    private void init() {
        mSubProgram.add(new SubProgram(BlockGame.class,"ブロック崩し"));
        mSubProgram.add(new SubProgram(LifeGame.class,"ライフゲーム"));
        mSubProgram.add(new SubProgram(AllWhite.class,"白にしろ"));
        mSubProgram.add(new SubProgram(SlideGame.class,"スライドゲーム"));
        mSubProgram.add(new SubProgram(Sudoku.class,"数独"));
        mSubProgram.add(new SubProgram(RubicCube.class,"ルービックキューブ"));
        mSubProgram.add(new SubProgram(Tetris.class,"テトリス"));
        mSubProgram.add(new SubProgram(Bomb.class,"マインスィーパ"));
//        mSubProgram.add(new SubProgram(MineSweeper.class,"マインスィーパ"));

        mListView = (ListView) findViewById(R.id.rootListView);
        mSimulationNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mListView.setAdapter(mSimulationNameAdapter);
        for (int i=0; i<mSubProgram.size(); i++) {
            mSimulationNameAdapter.add(mSubProgram.get(i).mName);
        }

        mListView.setOnItemClickListener(this);
    }
}
