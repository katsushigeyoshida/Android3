package jp.co.yoshida.katsushige.gameapp;


import java.util.ArrayList;
import java.util.List;

import jp.co.yoshida.katsushige.mylib.Vector3;
import jp.co.yoshida.katsushige.mylib.Vector3I;

public class CubeUnit {

    public int mId = 0;                     //  識別子
    public Vector3 mPos;                    //  元の位置座標
    public Vector3 mTranPos;                //  移動後の位置座標
    public Vector3 mAng;                    //  軸に対する回転角(deg)
    public Vector3I mAngInt;                //  軸に対する回転角(deg)
    public Vector3I mPosInt;                //  元の立方体の位置({x,y,z})
    public Vector3I mTranPosInt;            //  移動後の立方体の位置({x,y,z})
    public List<Vector3I> mAngList;         //  操作リスト

    /**
     * コンストラクタ
     */
    public CubeUnit() {
        mPos        = new Vector3();
        mTranPos    = new Vector3();
        mAng        = new Vector3();
        mPosInt     = new Vector3I();
        mTranPosInt = new Vector3I();
        mAngInt     = new Vector3I();
        mAngList    = new ArrayList<Vector3I>();
    }

    /**
     * コンストラクタ
     * @param pos   三次元座標
     * @param id    ID
     */
    public CubeUnit(Vector3 pos, int id) {
        mPos        = new Vector3(pos);
        mTranPos    = new Vector3(mPos);
        mAng        = new Vector3();
        mPosInt     = new Vector3I(mPos);
        mTranPosInt = new Vector3I(mPos);
        mAngInt     = new Vector3I();
        mAngList    = new ArrayList<Vector3I>();
        mId         = id;
    }

    /**
     * 前回の軸の回転角から移動位置を求める
     * @param xang      X軸で回転(deg)
     * @param yang      Y軸で回転(deg)
     * @param zang      Z軸で回転(deg)
     */
    public void setAddAngle(int xang, int yang, int zang) {
        setAddAngle(new Vector3I(xang, yang, zang));
    }

    /**
     * 前回の軸の回転角から移動位置を求める
     * 軸の回転角に対して座標位置をもとめる
     * @param ang   XYZの回転角(deg)
     */
    public void setAddAngle(Vector3I ang) {
        mAngInt.add(ang);
        mAngList.add(ang);
        mTranPos = transPos(mPos, mAngList);   //  初期位置からの移動位置
        normalizeIntPos(mAngInt);              //  90°ごとの位置を求める
    }

    /**
     * 座標位置を回転移動させる
     * @param pos       3次元座標
     * @param angList   回転角リスト
     * @return          回転後の座標
     */
    private Vector3 transPos(Vector3 pos, List<Vector3I>angList) {
        Vector3 outPos;
        outPos = pos;
        for (int i = 0; i < angList.size(); i++) {
            if (0 != angList.get(i).X)
                outPos = RotateX(outPos, angList.get(i).X);
            else if (0 != angList.get(i).Y)
                outPos = RotateY(outPos, angList.get(i).Y);
            else if (0 != angList.get(i).Z)
                outPos = RotateZ(outPos, angList.get(i).Z);
        }
        //outPos = roundVector(outPos);     //  座標値の丸め
        return outPos;
    }

    /**
     * MtransPosInt を 90°おきに整数位置を設定する
     * @param ang
     */
    private void normalizeIntPos(Vector3I ang) {
        if (ang.X % 90 == 0 && ang.Y % 90 == 0 && ang.Z % 90 == 0) {
            mTranPosInt = new Vector3I(mTranPos);
            //mTranPos = new Vector3(mTranPosInt.X, mTranPosInt.Y, mTranPosInt.Z);
        }
    }

    /**
     * X軸で回転(時計回り)
     * @param vec   3次元座標
     * @param ang   回転角(deg)
     * @return      変換後の3次元座標
     */
    public Vector3 RotateX(Vector3 vec, float ang) {
        Vector3 outVec = new Vector3();
        float rang = ang * (float)Math.PI / 180f;
        outVec.X = vec.X;
        outVec.Y = vec.Y * (float)Math.cos(rang) - vec.Z * (float)Math.sin(rang);
        outVec.Z = vec.Z * (float)Math.cos(rang) + vec.Y * (float)Math.sin(rang);
        return outVec;
    }

    /**
     * Y軸で回転(時計回り)
     * @param vec   3次元座標
     * @param ang   回転角(deg)
     * @return      変換後の3次元座標
     */
    public Vector3 RotateY(Vector3 vec, float ang) {
        Vector3 outVec = new Vector3();
        float rang = ang * (float)Math.PI / 180f;
        outVec.X = vec.X * (float)Math.cos(rang) + vec.Z * (float)Math.sin(rang);
        outVec.Y = vec.Y;
        outVec.Z = vec.Z * (float)Math.cos(rang) - vec.X * (float)Math.sin(rang);
        return outVec;
    }

    /**
     * Z軸で回転(時計回り)
     * @param vec   3次元座標
     * @param ang   回転角(deg)
     * @return      変換後の3次元座標
     */
    public Vector3 RotateZ(Vector3 vec, float ang) {
        Vector3 outVec = new Vector3();
        float rang = ang * (float)Math.PI / 180f;
        outVec.X = vec.X * (float)Math.cos(rang) - vec.Y * (float)Math.sin(rang);
        outVec.Y = vec.Y * (float)Math.cos(rang) + vec.X * (float)Math.sin(rang);
        outVec.Z = vec.Z;
        return outVec;
    }

}

