package jp.co.yoshida.katsushige.mylib;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


//========================= グラフィックボタン処理 =================================

/**
 *
 * === グラフィックボタン処理 ==
 * clearButtons()                       グラフィックボタンのリストをクリア
 * addGButton(int id, BUTTONTYPE type, float cx, float cy, float r, float width, float height)     IDを指定してグラフィックボタンを登録
 * addGroupGButton(int id, BUTTONTYPE type, RectF rect, int rowCount, int colCount, String[] titles)   IDを指定してグラフィックボタングループを登録
 * addGButton(int id, BUTTONTYPE type, RectF rect)     グラフぃくボタンの登録
 * removeGButton(int id)                IDを指定して要素を削除
 * getGButtpnProp(int n)                List NoでGButtonクラスを返す
 * setGButton(int id, BUTTONTYPE type, float cx, float cy, float r, float width, float height)  ボタンのパラメータを修正
 * adjustGButton(int id, float dCx, float dCy, float dR, float dWidth, float dHeight)   ボタンのパラメータを相対値で修正
 * setGButtonSize(int id, RectF rect)   ボタンの位置とサイズを設定
 * getGButtonSize(int id)               ボタンの位置とサイズを取得
 * getGButtonSize(int id, boolean ext)  ボタンの位置とサイズを取得
 * getGButtonSize(int id, boolean ext)  ボタンの位置とサイズを取得
 * setGButtonBackColor(int id, int color)   ボタンの背景色を設定
 * setTransparent(int id, boolean transparent)  ボタンを透過にする
 * setGButtonTitle(int id, String title)    ボタンのタイトルを設定
 * setGButtonTitle(int id, String title, TEXTALIGNMENT ta)  ボタンのタイトルとアライメントを設定
 * getGButtonTitle(int id)              設定されているボタンのタイトルを取得
 * getGbuttonTitleId(String title)      ボタンに設定されている文字列の検索して見つかったボタンのIDを返す
 * setGButtonTitleSize(int id, float size)  IDで指定したボタンの文字列の大きさを設定
 * setGButtonTitleColor(int id, int color)  ボタンのタイトルの色を設定
 * setGButtonTitleXMargine(int id, float x) ボタンのタイトル文字が左寄せの場合の左マージンを設定
 * setGButtonExtension(int id, boolean ml)  タイトルの複数行を許可、行数に合わせてボタンの大きさを変更
 * setGButtonTitleMaxSize(int id, float size)   setGButtonTitleMaxSize(int id, float size)
 * getGButtonMaxTextCount(int id)       現在の文字サイズで書き込める最大文字数を求める
 * setGButtonBorderColor(int id, int color) ボタンの枠線の色設定
 * setGButtonBorderWidth(int id, float width)   ボタンの枠線の太さの設定
 * setGButtonEnabled(int id, boolean enable)    ボタンの有効/無効の設定
 * setGButtonVisible(int id, boolean visible)   ボタンの表示/非表示の設定
 *  findGButtonId(int id)               指定のIDを検索する
 *  GButtonDownRevers(int id)           GButtonDownRevers(int id)
 *  GButtonDown(int id, boolean down)   指定のボタンを押す
 *  getGButtonDownState(int id)         ボタンの状態(押し下げるを取得
 *  drawGButtons()                      登録されたボタンの表示(初期状態にクリアする)
 *  drawGButtonsDown(int downId)        登録されたボタンと押されたボタンの表示の更新
 *  drawGButton(int id)                 指定のボタンを表示する
 *  getButtonDownId(float x, float y)   座標から押されたボタンのIDを取得する
 *  getButtonDownCount()                押されているボタンの数をカウントする
 *
 */
public class YGButton extends YDraw {

    private static final String TAG = "GButton";

    public enum BUTTONTYPE {CIRCLE, RECT, GROUPCIRCLE, GROUPRECT };

    private Map<Integer, GButton> mGButtonMap = null;           //  ボタンリスト
    private Map<Integer, GroupGButton> mGButtonGroup = null;    //  グループボタンのIDと属性

    public YGButton(Canvas c) {
        canvas = c;
        canvas2 = c;
    }

    public YGButton(SurfaceHolder holder) {
        mHolder = holder;
    }

    /**
     * グラフィックボタンのリストをクリアする
     */
    public void clearButtons() {
        if (mGButtonMap != null)
            mGButtonMap.clear();
    }

    /**
     * IDを指定してグラフィックボタンを登録する
     * @param id        ID
     * @param type      ボタンのタイプ(BUTTONTYPE.CIRCLE/RECT)
     * @param cx        ボタン位置 中心座標
     * @param cy        ボタン位置 中心座標
     * @param r         CIRCLEボタンの半径
     * @param width     ボタンの幅
     * @param height    ボタンの高さ
     */
    public void addGButton(int id, BUTTONTYPE type, float cx, float cy, float r, float width, float height) {
        if (mGButtonMap == null)
            mGButtonMap = new LinkedHashMap<Integer, GButton>();
        GButton button = new GButton(id, type, cx, cy, r, width, height);
        mGButtonMap.put(id,button);
    }

    /**
     * IDを指定してグラフィックボタングループを登録する
     * @param id            ID
     * @param type          グループボタンのタイプ
     * @param rect          全体の領域
     * @param rowCount      行数
     * @param colCount      列数(0の時は円形に配列する)
     * @param titles        表示文字列
     */
    public void addGroupGButton(int id, BUTTONTYPE type, RectF rect, int rowCount, int colCount, String[] titles) {
        if (type != BUTTONTYPE.GROUPCIRCLE && type != BUTTONTYPE.GROUPRECT)
            return;
        if (mGButtonMap == null)
            mGButtonMap = new LinkedHashMap<Integer, GButton>();
        if (mGButtonGroup == null)
            mGButtonGroup = new HashMap<Integer, GroupGButton>();

        GroupGButton groupGButton = new GroupGButton(id, type, rect, rowCount, colCount);
        mGButtonGroup.put(id, groupGButton);
        GButton[] gbuttons = groupGButton.getGButtons();
        for (int i = 0; i < gbuttons.length; i++) {
            Log.d(TAG,"addGroupGButton: "+id+" "+i+" "+gbuttons[i].mTitle+" "+gbuttons[i].mRect);
            mGButtonMap.put(id + i, gbuttons[i]);
            if (i < titles.length)
                setGButtonTitle(id + i, titles[i]);
        }
    }

    /**
     * グラフぃくボタンの登録
     * @param id        ボタンのID
     * @param type      ボタンの種類(円/四角)
     * @param rect      位置とサイズ、円の場合は幅の1/2を半径とする
     */
    public void addGButton(int id, BUTTONTYPE type, RectF rect) {
        if (mGButtonMap == null)
            mGButtonMap = new LinkedHashMap<Integer, GButton>();
        GButton button = new GButton(id, type, rect);
        mGButtonMap.put(id,button);
    }

    /**
     * IDを指定して要素を削除する
     * IDがグループデータの場合はまとめて削除する
     * @param id        ID
     */
    public void removeGButton(int id) {
        if (mGButtonMap == null)
            return;
        if (mGButtonGroup!= null) {
            if (mGButtonGroup.containsKey(id)) {
                for (int i = 0; i < mGButtonGroup.get(id).getSize(); i++) {
                    if (mGButtonMap.containsKey(id + i))
                        mGButtonMap.remove(id + i);
                }
            }
        }
        if (mGButtonMap.containsKey(id))
            mGButtonMap.remove(id);
    }

    /**
     * List NoでGButtonクラスを返す
     * @param n
     * @return
     */
    public GButton getGButtpnProp(int n) {
        if (mGButtonMap == null)
            return null;
        return mGButtonMap.get(n);
    }

    /**
     * ボタンのパラメータを修正する
     * @param id
     * @param type
     * @param cx
     * @param cy
     * @param r
     * @param width
     * @param height
     */
    public void setGButton(int id, BUTTONTYPE type, float cx, float cy, float r, float width, float height) {
        if (mGButtonMap == null)
            return ;
        mGButtonMap.get(id).setButton(type, cx, cy, r, width, height);
    }

    /**
     * ボタンのパラメータを相対値で修正する
     * @param id
     * @param dCx        中心位置の移動距離
     * @param dCy        中心位置の移動距離
     * @param dR
     * @param dWidth
     * @param dHeight
     */
    public void adjustGButton(int id, float dCx, float dCy, float dR, float dWidth, float dHeight) {
        if (mGButtonMap == null)
            return ;
        mGButtonMap.get(id).adjustButton(dCx, dCy, dR, dWidth, dHeight);
    }

    /**
     * ボタンの位置とサイズを設定する
     * @param id        ボタンのID
     * @param rect      位置とサイズ
     */
    public void setGButtonSize(int id, RectF rect) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setButtonSize(rect);
    }

    /**
     * ボタンの位置とサイズを取得する
     * @param id        ボタンのID
     * @return          位置とサイズ
     */
    public RectF getGButtonSize(int id) {
        if (mGButtonMap == null)
            return null;
        return mGButtonMap.get(id).getButtonSize();
    }

    /**
     * ボタンの位置とサイズを取得する
     * @param id    ボタンのID
     * @param ext   拡張サイズか元のサイズを指定
     * @return      位置とサイズ
     */
    public RectF getGButtonSize(int id, boolean ext) {
        if (mGButtonMap == null)
            return null;
        return mGButtonMap.get(id).getButtonSize(ext);
    }

    /**
     * ボタンの背景色を設定する
     * @param id        ボタンのID
     * @param color     ｆｅ：ｅｄ）ｈ　
     */
    public void setGButtonBackColor(int id, int color) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setBackColor(color);
    }

    /**
     * ボタンを透過にする
     * @param id            ボタンのID
     * @param transparent   透過
     */
    public void setTransparent(int id, boolean transparent) {
        mGButtonMap.get(id).setTransparent(transparent);
    }


    /**
     * ボタンのタイトルを設定する
     * @param id
     * @param title
     */
    public void setGButtonTitle(int id, String title) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setButtonTitle(title);
    }

    /**
     * ボタンのタイトルとアライメントを設定する
     * @param id
     * @param title
     * @param ta
     */
    public void setGButtonTitle(int id, String title, TEXTALIGNMENT ta) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setButtonTitle(title, ta);
    }

    /**
     * 設定されているボタンのタイトルを取得する
     * @param id
     * @return
     */
    public String getGButtonTitle(int id) {
        if (mGButtonMap == null || mGButtonMap.get(id)==null)
            return "";
        return mGButtonMap.get(id).getButtonTitle();
    }

    /**
     * ボタンに設定されている文字列の検索して見つかったボタンのIDを返す
     * @param title     検索する文字列
     * @return          ボタンのID
     */
    public int getGbuttonTitleId(String title) {
        if (mGButtonMap == null)
            return -1;
        for (Integer i : mGButtonMap.keySet() ) {
            if (mGButtonMap.get(i).getButtonTitle().compareTo(title)==0)
                return i;
        }
        return -1;
    }

    /**
     * IDで指定したボタンの文字列の大きさを設定する
     * @param id        ボタンのID
     * @param size      文字列の大きさ
     */
    public void setGButtonTitleSize(int id, float size) {
        if (mGButtonMap == null)
            return ;
        mGButtonMap.get(id).setTitleSize(size);
    }
    /**
     * ボタンのタイトルの色を設定する
     * @param id
     * @param color
     */
    public void setGButtonTitleColor(int id, int color) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setTitleColor(color);
    }

    /**
     * ボタンのタイトル文字が左寄せの場合の左マージンを設定する
     * @param id
     * @param x
     */
    public void setGButtonTitleXMargine(int id, float x) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setTitleXMargine(x);
    }

    /**
     * タイトルの複数行を許可、行数に合わせてボタンの大きさを変更
     * @param id
     * @param ml
     */
    public void setGButtonExtension(int id, boolean ml) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setExtensionButton(ml);
    }

    /**
     * 文字の最大サイズを設定する
     * @param id
     * @param size
     */
    public void setGButtonTitleMaxSize(int id, float size) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setTitleMaxSize(size);
    }

    /**
     * 現在の文字サイズで書き込める最大文字数を求める
     * @param id
     * @return
     */
    public int getGButtonMaxTextCount(int id) {
        if (mGButtonMap == null)
            return 0;
        return mGButtonMap.get(id).getMaxTextCount();
    }

    /**
     * ボタンの枠線の色設定
     * @param id
     * @param color
     */
    public void setGButtonBorderColor(int id, int color) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setBorderColor(color);
    }

    /**
     * ボタンの枠線の太さの設定
     * @param id
     * @param width
     */
    public void setGButtonBorderWidth(int id, float width) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setBorderWidth(width);
    }

    /**
     * ボタンの有効/無効の設定
     * @param id            ボタンID
     * @param enable        有効/無効
     */
    public void setGButtonEnabled(int id, boolean enable) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setEnabled(enable);
    }

    /**
     * ボタンの表示/非表示の設定
     * @param id            ボタンのＩＤ
     * @param visible       表示/非表示
     */
    public void setGButtonVisible(int id, boolean visible) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).setVisible(visible);
    }

    /**
     * 指定のIDを検索する
     * @param id
     * @return
     */
    private int findGButtonId(int id) {
        if (mGButtonMap ==null)
            return -1;
        else
            return id;
    }

    /**
     * 押したボタンを反転する
     * @param id
     */
    public void GButtonDownRevers(int id) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).drawButtonRevers();
    }

    /**
     * 指定のボタンを押す
     * @param id        ボタンＩＤ
     * @param down      押した状態/通常の状態
     */
    public void GButtonDown(int id, boolean down) {
        if (mGButtonMap == null)
            return;
        if (down)
            mGButtonMap.get(id).drawButtonDown();
        else
            mGButtonMap.get(id).drawButton();
    }

    /**
     * ボタンの状態(押し下げるを取得
     * @param id            ボタンのID
     * @return              押されている/いない
     */
    public boolean getGButtonDownState(int id) {
        if (mGButtonMap == null)
            return false;
        return mGButtonMap.get(id).getDownState();
    }

    /**
     * 登録されたボタンの表示(初期状態にクリアする)
     */
    public void drawGButtons() {
        drawGButtonsDown(-1);
    }

    /**
     * 登録されたボタンと押されたボタンの表示の更新
     * 指定されたボタン以外は初期状態に戻す
     * @param downId        押されたボタンのID
     */
    public void drawGButtonsDown(int downId) {
        if (mGButtonMap == null)
            return;
        for (Map.Entry<Integer,GButton> button : mGButtonMap.entrySet()){
            if (button.getValue().getId() == downId)
                button.getValue().drawButtonDown();
            else
                button.getValue().drawButton();
        }
    }

    /**
     * 指定のボタンを表示する
     * @param id        ボタンのID
     */
    public void drawGButton(int id) {
        if (mGButtonMap == null)
            return;
        mGButtonMap.get(id).drawButton();
    }

    /**
     * 座標から押されたボタンのIDを取得する
     * @param x     X座標
     * @param y     Y座標
     * @return      ボタンID
     */
    public int getButtonDownId(float x, float y) {
        if (mGButtonMap == null)
            return -1;
        if (mGButtonGroup != null) {
            for (Map.Entry<Integer, GroupGButton> groupGButton : mGButtonGroup.entrySet()) {
                for (int i = 0; i < groupGButton.getValue().getSize(); i++) {
                    if (mGButtonMap.containsKey(groupGButton.getKey() + i))
                        if (mGButtonMap.get(groupGButton.getKey() + i).getButonIn(x, y))
                            return groupGButton.getKey() + i;
                }
            }
        }
        for (Map.Entry<Integer,GButton> button : mGButtonMap.entrySet()) {
            if (button.getValue().getButonIn(x, y))
                return button.getKey();
        }
        return -1;
    }

    /**
     * 押されているボタンの数をカウントする
     * @return      押されたボタンの数
     */
    public int getButtonDownCount() {
        int n = 0;
        if (mGButtonMap == null)
            return -1;
        for (Map.Entry<Integer,GButton> button : mGButtonMap.entrySet()) {
            if (button.getValue().getDownState())
                n++;
        }
        return n;
    }

    private class GroupGButton {
        private int mId;
        private BUTTONTYPE mType;       //  グループボタンの種類
        private RectF mRect;            //  全体の領域
        private int mRowCount;          //  行数
        private int mColCount;          //  列数(0の時は円形配置)

        public GroupGButton(int id, BUTTONTYPE type, RectF rect, int row, int col) {
            mId = id;
            mType = type;
            mRect = rect;
            mRowCount = row;
            mColCount = col;
        }

        public int getSize() {
            if (mColCount == 0)
                return mRowCount;
            else
                return mRowCount * mColCount;
        }

        public GButton[] getGButtons() {
            if (mType == BUTTONTYPE.GROUPCIRCLE) {
                return getGButtons(BUTTONTYPE.CIRCLE);
            } else if (mType ==BUTTONTYPE.GROUPRECT) {
                return getGButtons(BUTTONTYPE.RECT);
            }
            return null;
        }

        private GButton[] getGButtons(BUTTONTYPE buttonType) {
            GButton[] gbuttons;
            if (0 < mRowCount && mColCount == 0) {
                //  円形配置
                gbuttons= new GButton[mRowCount];
                float or = mRect.width() / 2;                   //  外円の半径
                float ratio = mRect.height() / mRect.width();   //  縦横比
                PointF oc = new PointF(mRect.centerX(), mRect.centerY());   //  外円の中心
                float ir = or * (float)(Math.sin(Math.PI / mRowCount) / (Math.sin(Math.PI / mRowCount) + 1));   //  描画ボタンの円の半径
                float cr = or - ir;
                int i = 0;
                double dth = 2 * Math.PI / mRowCount;
                for (double th = 0; th < 2 * Math.PI; th += dth) {
                    float cx = oc.x + cr * (float)Math.cos(th);
                    float cy = oc.y + cr * (float)Math.sin(th);
                    RectF rect = new RectF(cx - ir, cy - ir, cx + ir, cy + ir);
                    gbuttons[i++] = new GButton(mId + i, buttonType, rect);
                }
            } else if (0 < mRowCount && 0 < mColCount) {
                //  矩形配置
                gbuttons= new GButton[mRowCount *mColCount];
                float width = mRect.width() / mColCount;        //  一個のセルの幅
                float height = mRect.height() / mRowCount;      //  一個のセルの高さ
                float sx = mRect.left;
                float sy = mRect.top + height * 2f;
                int i = 0;
                for (int row = 0; row < mRowCount; row++) {
                    for (int col = 0; col < mColCount; col++) {
                        float x = sx + width * col;
                        float y = sy - height * row;
                        RectF rect = new RectF(x, y, x + width, y + height);
                        gbuttons[i++] = new GButton(mId + i, buttonType, rect);
                    }
                }
            } else {
                return null;
            }
            return gbuttons;
        }
    }

    /**
     * グラフィックボタン
     */
    private class GButton {
        private int mId = 0;                            //  ボタンのID
        private BUTTONTYPE mType = BUTTONTYPE.CIRCLE;   //  ボタンの種類
        private RectF mRect = null;
        private RectF mExRect = null;
        private int mBackColor = Color.WHITE;           //  ボタンの背景色
        private boolean mBackTransparent = false;       //  透過
        private int mButtonDownColor = Color.CYAN;      //  ボタン押したときの背景色
        private int mBorderColor = Color.BLACK;         //  境界線の色
        private float mBorderWidth = 1;                 //  境界線の幅
        private String mTitle = "";                     //  ボタンのタイトル
        private int mTitleColor = Color.BLACK;          //  ボタンタイトルの色
        private float mTitleSize = 0f;                  //  ボタン文字の大きさ
        private boolean mTitleAutoSize = true;          //  枠に合わせて文字の大きさを変える
        private int mTitleLength;                       //  一行の文字数
        private float mTitleXMargine = 5f;              //  文字の開始位置のマージン(左寄席の場合)
        private float mTitleYMargine = 0f;              //  未使用
        private float mMaxTitleSize = 0;                //  最大文字の大きさ(0は制限なし)
        private float mTitleAutoRatio = 0.7f;           //  自動の文字大きさ(枠に対する比率)
        private float mTitleRectAutoRatio = 0.9f;       //  四角ボタンで文字が長い場合の枠に対する比率
        private float mTitleStrokWidth = 1f;            //  文字の幅
        private TEXTALIGNMENT mTitleAlignment = TEXTALIGNMENT.CC;   //  タイトルの位置
        private boolean mResize = false;                //  複数行でボタンサイズ変更可
        private boolean mEnabled = true;                //  ボタンの使用可/不可
        private boolean mVisible = true;                //  ボタンの表示/彦表示
        private boolean mButtonDown = false;            //  ボタンの押下状態

        GButton() {
            mTitleSize = getTextSize();
        }

        /**
         * ボタンの作成
         * @param id        ボタンのID
         * @param type      ボタンの種類(BUTTONTYPE.CIRCLE/RECT)
         * @param cx        ボタン位置の設定(中心X座標)
         * @param cy        ボタン位置の設定(中心Y座標)
         * @param r         円ボタンの半径
         * @param width     四角ボタンの幅
         * @param height    四角ボタンの高さ
         */
        GButton(int id,BUTTONTYPE type,float cx, float cy, float r, float width, float height) {
            mId = id;
            mType = type;
            if (mType == BUTTONTYPE.CIRCLE) {
                mRect = new RectF(cx - r, cy - r, cx + r, cy + r);
                mTitleSize = r * (float) Math.sqrt(2d) * mTitleAutoRatio;
            } else if (mType == BUTTONTYPE.RECT) {
                mRect = new RectF(cx - width/2f, cy - height/2f, cx + width/2f, cy + height/2f);
                mTitleSize = height * mTitleAutoRatio;
            }
            mExRect = new RectF(mRect);
        }

        /**
         * ボタンの作成
         * @param id        ボタンのID
         * @param type      ボタンの種類(円/四角)
         * @param rect      位置とサイズ(円の場合は幅の半分が半径となる)
         */
        GButton(int id, BUTTONTYPE type, RectF rect) {
            mId = id;
            mType = type;
            mRect = rect;
            if (mType == BUTTONTYPE.CIRCLE) {
                mTitleSize = rect.height() / 2f * (float) Math.sqrt(2d) * mTitleAutoRatio;
            } else if (mType == BUTTONTYPE.RECT) {
                mTitleSize = rect.height() * mTitleAutoRatio;
            }
            mExRect = new RectF(mRect);
        }

        /**
         * ボタンの位置とサイズを取得する
         * @return      RectF
         */
        public RectF getButtonSize(boolean ext) {
            if (ext)
                return mExRect;
            else
                return mRect;
        }

        public RectF getButtonSize() {
            if (mResize)
                return mExRect;
            else
                return mRect;
        }
        /**
         * ボタンの位置とサイズを設定する
         * @param rect      位置とサイズRectF
         */
        public void setButtonSize(RectF rect) {
            mRect = new RectF(rect);
            mExRect = new RectF(rect);
        }

        /**
         * ボタンのIDを設定または変更する
         * @param id    ボタンのID
         */
        public void setId(int id) {
            mId = id;
        }

        /**
         * GButtonのプロパティを設定する
         * @param type      GButoonの種類(円/四角)
         * @param cx        ボタンの位置(中心座標)
         * @param cy
         * @param r         円タイプの時の円の半径
         * @param width     四角タイプのボタンの幅
         * @param height    四角タイプのボタンの高さ
         */
        public void setButton(BUTTONTYPE type,float cx, float cy, float r, float width, float height) {
            mType = type;
            if (mType == BUTTONTYPE.CIRCLE) {
                mRect = new RectF(cx - r, cy - r, cx + r, cy + r);
            } else if (mType == BUTTONTYPE.RECT) {
                mRect = new RectF(cx - width/2f, cy - height/2f, cx + width/2f, cy + height/2f);
            }
            mExRect = new RectF(mRect);
        }

        /**
         * ボタンのパラメータを相対値で修正する(0の場合は変更なし)
         * @param dCx        中心位置の移動距離
         * @param dCy        中心位置の移動距離
         * @param dR         円の場合は半径の増分
         * @param dWidth    四角の場合の幅の増分
         * @param dHeight   四角の場合の高さの増分
         */
        public void adjustButton(float dCx, float dCy, float dR, float dWidth, float dHeight) {
            mRect.left += dCx / 2f;
            mRect.right += dCx /2f;
            mRect.top += dCy / 2f;
            mRect.bottom += dCy / 2f;
            if (mType == BUTTONTYPE.CIRCLE) {
                mRect.left -= dR;
                mRect.top -= dR;
                mRect.right += dR;
                mRect.bottom += dR;
            } else if (mType == BUTTONTYPE.RECT) {
                mRect.left -= dWidth /2f;
                mRect.top -= dHeight / 2f;
                mRect.right += dWidth / 2f;
                mRect.bottom += dHeight / 2f;
            }
            mExRect = new RectF(mRect);
        }

        /**
         * 円ボタンの座標設定
         * @param cx    ボタンの位置(中心座標)
         * @param cy
         * @param r     円ボタンの半径
         */
        public void setCircleButton(float cx, float cy, float r) {
            mType = BUTTONTYPE.CIRCLE;
            mRect = new RectF(cx - r, cy - r, cx + r, cy + r);
            mExRect = new RectF(mRect);
        }

        /**
         * 四角ボタンの座標設定または変更
         * @param cx        ボタンの位置(中心座標)
         * @param cy
         * @param width     四角タイプのボタンの幅
         * @param height    四角タイプのボタンの高さ
         */
        public void setRectButton(float cx, float cy, float width, float height) {
            mType = BUTTONTYPE.RECT;
            mRect = new RectF(cx - width/2f, cy - height/2f, cx + width/2f, cy + height/2f);
            mExRect = new RectF(mRect);
        }

        /**
         * 四角ボタンのx座標設定または変更
         * @param rect      四角タイプのボタンの位置と大きさ
         */
        public void setRectButton(RectF rect) {
            mType = BUTTONTYPE.RECT;
            mRect = rect;
            mExRect = new RectF(mRect);
        }

        /**
         * ボタン内の文字列の複数行の可不可
         * @param ml        複数行フラグ
         */
        public void setExtensionButton(boolean ml) {
            mResize = ml;
        }

        /**
         * ボタンの背景色の設定
         * @param color
         */
        public void setBackColor(int color) {
            mBackColor = color;
        }

        /**
         * ボタンを押したときの背景色の設定
         * @param color
         */
        public void setButtonDownColor(int color) {
            mButtonDownColor = color;
        }

        /**
         * 透過ボタンの設定
         * @param tranparent
         */
        public void setTransparent(boolean tranparent) {
            mBackTransparent = tranparent;
        }

        /**
         * 境界線の色設定
         * @param color
         */
        public void setBorderColor(int color) {
            mBorderColor = color;
        }

        /**
         * 境界線の幅を設定
         * @param width
         */
        public void setBorderWidth(float width) {
            mBorderWidth = width;
        }

        /**
         * ボタンのタイトル文字とアライメントを設定
         * @param title
         * @param ta
         */
        public void setButtonTitle(String title, TEXTALIGNMENT ta) {
            setButtonTitle(title);
            setTitleAliment(ta);
        }

        /**
         * ボタンのタイトル文字とサイズ設定
         * 文字の大きさは枠の大きさに合わせて変える
         * 四角ボタンの場合は文字高さが枠の半分以下になった場合、２行で表示することを前提に高さを固定する
         * @param text
         */
        public void setButtonTitle(String text) {
            mTitle = text;
            if (text.length() <= 0)
                return;
            float th = getTextSize();           //  文字高さ
            float tw = measureText(text);       //  文字列の長さ
            if (mType == BUTTONTYPE.CIRCLE) {
                //  円ボタンの場合の文字サイズ
                float r = mRect.height() / 2f;
                if (tw < th) {
                    //  文字高さが文字列幅より大きい場合は直径をsqrt(2)で割ったもので固定(2R/sqrt(2))
                    mTitleSize = r * (float) Math.sqrt(2d) * mTitleAutoRatio;
                } else {
                    //  文字列幅が大きい場合、幅に合わせて文字高さを設定する
                    mTitleSize = th * r / (float) (Math.sqrt((double) (th * th + tw * tw)) / 2d) * mTitleAutoRatio;
                }
            } else if (mType == BUTTONTYPE.RECT) {
                //  四角ボタン時の文字サイズの設定
                if (mResize) {
                    //  文字サイズ固定で枠を広げる場合
                    mTitleSize = mRect.height() * mTitleAutoRatio;
                    setTextSize(mTitleSize);
                    tw = measureText(text);
                    mTitleLength = (int)(mTitle.length() * (mRect.width() - mTitleXMargine) / tw);
                    int n = mTitle.length() / mTitleLength;
                    mExRect.bottom = mRect.bottom + mTitleSize * n;         //  枠の下限座標
                } else {
                    //  枠を固定で文字サイズを変える、文字サイズが半分以下になったら２行にする
                    if (mTitleAutoSize) {
                        mTitleSize = mRect.height() * mTitleAutoRatio;
                        setTextSize(mTitleSize);
                        th = getTextSize();           //  文字高さ
                        tw = measureText(text);       //  文字列の長さ
                        if ((mRect.width() - mTitleXMargine * 2f) < tw)
                            mTitleSize *= (mRect.width() - mTitleXMargine * 2f) / tw * mTitleAutoRatio;
                        int n = 2;
                        Log.d(TAG,"setButtonTitle:"+" "+n+" "+mTitleSize+" "+mRect.height()+" "+tw+" "+mRect.width());
                        //  四角ボタンの場合の文字列の幅がボタンのサイズを超える場合、文字サイズを縮小する
                        if (mTitleSize < mRect.height() / n) {
                            //  文字列の高さと幅の比が枠の高さと幅の比より小さい場合
                            do {
                                mTitleSize = mRect.height() / n;
                                setTextSize(mTitleSize);
                                th = getTextSize();           //  文字高さ
                                tw = measureText(text) / n;       //  文字列の長さ
                                n++;
                            } while ((mRect.width() - mTitleXMargine * 2f) < tw);
                        }
                    }
                }
//                Log.d(TAG,"setButtonTitle: "+ mResize +" "+mTitle+" "+mTitleSize+" "+th+" "+tw+" "+mRect.height()+" "+mRect.width()+" ");
            }
            //  文字の最大高さが設定されている場合文字サイズを最大高さにする
            if (0 < mMaxTitleSize && mMaxTitleSize < mTitleSize)
                mTitleSize = mMaxTitleSize;
        }

        /**
         * 現在のボタンサイズと文字サイズで書き込める最大文字数を求める
         * @return      文字数
         */
        public int getMaxTextCount() {
            String dummyText = "0123456789";
            mTitleSize = mRect.height() * mTitleAutoRatio;
            setTextSize(mTitleSize);
            float tw = measureText(dummyText);       //  文字の長さ
            return (int)(mRect.width() / tw * 10f);
        }

        /**
         * ボタンのタイトル文字を取得する
         * @return      タイトル文字列
         */
        public String getButtonTitle() {
            return mTitle;
        }

        /**
         * 文字サイズの設定
         * @param textsize
         */
        public void setTitleSize(float textsize) {
            mTitleSize = textsize;
            mTitleAutoSize = false;
        }

        /**
         * 文字高さの最大値設定する
         * @param maxsize   文字高さ
         */
        public void setTitleMaxSize(float maxsize) {
            mMaxTitleSize = maxsize;
        }

        /**
         * タイトル文字が左寄せの場合の左マージンを設定する
         * @param x
         */
        public void setTitleXMargine(float x) {
            mTitleXMargine = x;
        }

        /**
         * 文字列の色
         * @param color
         */
        public void setTitleColor(int color) {
            mTitleColor = color;
        }

        /**
         * 文字の太さの設定
         * @param width
         */
        public void setTitleStrokeWidth(float width) {
            mTitleStrokWidth = width;
        }

        public void setTitleAliment(TEXTALIGNMENT ta) {
            mTitleAlignment = ta;
        }

        /**
         * ボタンの使用可否設定
         * @param enable
         */
        public void setEnabled(boolean enable) {
            mEnabled = enable;
        }

        /**
         * ボタンの表示非表示の設定
         * @param visible
         */
        public void setVisible(boolean visible) {
            mVisible = visible;
        }

        /**
         * ボタンが押された状態を取得
         * @return
         */
        public boolean getDownState() {
            return mButtonDown;
        }

        /**
         * グラフィックボタンの描画(BackColor表示)
         */
        public void drawButton() {
            if (!mVisible)
                return;
            if (mType == BUTTONTYPE.CIRCLE) {
                drawGCircle(mBackColor);
            } else if (mType == BUTTONTYPE.RECT) {
                drawGRect(mBackColor);
            }
            mButtonDown = false;
        }

        /**
         * グラフィックボタンの表示(押したとき)
         */
        public void drawButtonDown() {
            if (!mEnabled || !mVisible)
                return;
            if (mType == BUTTONTYPE.CIRCLE) {
                drawGCircle(mButtonDownColor);
            } else if (mType == BUTTONTYPE.RECT) {
                drawGRect(mButtonDownColor);
            }
            mButtonDown = true;
        }

        /**
         * グラフィックボタンの背景を反転する
         */
        public void drawButtonRevers() {
            if (mButtonDown)
                drawButton();
            else
                drawButtonDown();
        }

        /**
         * 円ボタンの描画
         * @param backColor
         */
        private void drawGCircle(int backColor) {
            if (!mBackTransparent) {    //  透過の場合は塗潰さない
                setColor(backColor);
                fillCircle(mRect.centerX(), mRect.centerY(), mRect.height()/2f);
            }
            setColor(mBorderColor);
            setStrokeWidth(mBorderWidth);
            drawText();
            drawCircle(mRect.centerX(), mRect.centerY(), mRect.height()/2f);
        }

        /**
         * 四角ボタンの描画
         * @param backColor
         */
        private void drawGRect(int backColor) {
            float x = mRect.left;
            float y = mRect.top;
            if (!mBackTransparent) {    //  透過の場合は塗潰さない
                setColor(backColor);
                if (mResize)
                    fillRect(x, y ,mExRect.width(), mExRect.height());
                else
                    fillRect(x, y,mRect.width(), mRect.height());
            }
            setColor(mBorderColor);
            setStrokeWidth(mBorderWidth);
            drawText();
            if (mResize)
                drawRect(mExRect);
            else
                drawRect(mRect);
        }

        /**
         * 文字列を描画する
         */
        private void drawText() {
            if (mTitle.length() <= 0)
                return;
            setTextSize(mTitleSize);
            setStrokeWidth(mTitleStrokWidth);
            setColor(mTitleColor);
            if (mType == BUTTONTYPE.CIRCLE)
                drawStringCenter(mTitle, mRect.centerX(), mRect.centerY());
            else
                drawTextAlignment();
        }

        /**
         * 四角ボタンの時にアライメントを調整して表示
         * 文字高さが枠の高さの半分以下の場合、２行に分割して表示
         */
        private void drawTextAlignment() {
            if (mTitle.length() <= 0)
                return;
            float alignmentX = this.mRect.centerX();
            float alignmentY = this.mRect.centerY();
            TEXTALIGNMENT ta = this.mTitleAlignment;
            //  アライメントによる文字位置の設定
            if (ta == TEXTALIGNMENT.LT || ta == TEXTALIGNMENT.LC || ta == TEXTALIGNMENT.LB) alignmentX -= this.mRect.width()/2f;
            if (ta == TEXTALIGNMENT.CT || ta == TEXTALIGNMENT.CC || ta == TEXTALIGNMENT.CB) alignmentX += 0f;
            if (ta == TEXTALIGNMENT.RT || ta == TEXTALIGNMENT.RC || ta == TEXTALIGNMENT.RB) alignmentX += this.mRect.width()/2f;
            if (ta == TEXTALIGNMENT.LT || ta == TEXTALIGNMENT.CT || ta == TEXTALIGNMENT.RT) alignmentY -= this.mRect.height()/2f;
            if (ta == TEXTALIGNMENT.LC || ta == TEXTALIGNMENT.CC || ta == TEXTALIGNMENT.RC) alignmentY += 0f;
            if (ta == TEXTALIGNMENT.LB || ta == TEXTALIGNMENT.CB || ta == TEXTALIGNMENT.RB) alignmentY += this.mRect.height()/2f;
            //  文字列が左寄せの時左マージンを設定
            if (mTitleAlignment == TEXTALIGNMENT.LT || mTitleAlignment == TEXTALIGNMENT.LC || mTitleAlignment == TEXTALIGNMENT.LB)
                alignmentX += mTitleXMargine;

            setTextSize(mTitleSize);
            String title = mTitle;
            if (mResize) {
                //  サイズ固定で複数行表示(文字列がボタン幅より大きい場合複数行で表示し枠も広げる)
                int n = 0;
                if (mTitleLength < mTitle.length()) {
                    //  改行しながら表示する
                    while (mTitleLength <= title.length()) {
                        String title1 = title.substring(0, mTitleLength);
                        title = title.substring(mTitleLength);
                        drawString(title1, alignmentX, alignmentY, ta);
                        alignmentY += mTitleSize;
                        n++;
                    }
                }
                if (0 < title.length()) {
                    drawString(title, alignmentX, alignmentY, ta);
                    n++;
                }
//                mExRect.bottom = mExRect.top + n * mTitleSize;
            } else {
                float tw = measureText(mTitle);
                int titlelen = (int) (mTitle.length() * (mRect.width() - mTitleXMargine) / tw);    //  1行に入る文字数
                int n = mTitle.length() / titlelen + 1;
                if (1 < n && titlelen < mTitle.length())
                    alignmentY -= mRect.height() / 2f * (1f - 1f / (float)n);
                for (int i = 0; i < n; i++) {
                    if (title.length() > titlelen) {
                        String title1 = title.substring(0, titlelen - 1);
                        title = title.substring(titlelen - 1);
                        drawString(title1, alignmentX, alignmentY, ta);
                        alignmentY += mRect.height() / (float) n;
                    } else {
                        drawString(title, alignmentX, alignmentY, ta);
                    }
                }
            }
//            Log.d(TAG,"drawTextAlignment:"+ mResize +" "+mTitle+" "+mTitleSize+" "+mExRect.bottom);
        }

        /**
         * ボタンのＩＤを返す
         * @return
         */
        public int getId() {
            return mId;
        }

        /**
         * 指定座標のボタン領域の内外を判定する
         * @param x     指定座標
         * @param y     指定座標
         * @return      内側かの判定
         */
        public boolean getButonIn(float x, float y) {
            if (!mEnabled || !mVisible)
                return false;
            if (mType == BUTTONTYPE.CIRCLE)
                return InCircle(mRect.centerX(), mRect.centerY(), mRect.height()/ 2f, x, y);
            else if (mType == BUTTONTYPE.RECT)
                return InRect(mRect.centerX(), mRect.centerY(), mRect.width(), mRect.height(), x, y);
            return false;
        }

        /**
         * 指定座標がボタン領域内であればIDを返す
         * @param x         内外判定対象座標
         * @param y         内外判定対象座標
         * @return          ID / 外の時は=1
         */
        public int getButtonInId(float x, float y) {
            if (mType == BUTTONTYPE.CIRCLE) {
                if (InCircle(mRect.centerX(), mRect.centerY(), mRect.height()/ 2f, x, y))
                    return mId;
            } else if (mType == BUTTONTYPE.RECT) {
                if (InRect(mRect.centerX(), mRect.centerY(), mRect.width(), mRect.height(), x, y))
                    return mId;
            }
            return -1;
        }

        /**
         * 円領域の内外判定
         * @param cx        円の中心座標
         * @param cy        円の中心座標
         * @param r         円の半径
         * @param x         内外判定対象座標
         * @param y         内外判定対象座標
         * @return
         */
        private boolean InCircle(float cx, float cy, float r, float x, float y) {
            float dx = cx - x;
            float dy = cy - y;
            if (r*r < (dx*dx + dy*dy))
                return false;
            else
                return true;
        }

        /**
         * 四角領域の内外判定
         * @param cx        四角の中心座標
         * @param cy        四角の中心座標
         * @param width     四角の幅
         * @param height    四角の高さ
         * @param x         内外判定対象座標
         * @param y         内外判定対象座標
         * @return
         */
        private boolean InRect(float cx, float cy, float width, float height, float x, float y ) {
            float left = cx - width / 2f;
            float right  =cx + width /2f;
            float top = cy - height / 2f;
            float bottom = cy + height / 2f;
            if (left <= x && x <= right && top <= y && y <= bottom)
                return true;
            else
                return false;
        }
    }
}
