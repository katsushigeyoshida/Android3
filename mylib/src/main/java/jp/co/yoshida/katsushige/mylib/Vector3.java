package jp.co.yoshida.katsushige.mylib;

public class Vector3 {
    public float X;
    public float Y;
    public float Z;
    public Vector3() {

    }
    public Vector3(Vector3 v) {
        X = v.X;
        Y = v.Y;
        Z = v.Z;
    }

    public Vector3(float x, float y, float z) {
        X = x;
        Y = y;
        Z = z;
    }

    public float length() {
        return (float)Math.sqrt(X * X + Y * Y + Z * Z);
    }

    /**
     * XY平面上での2っのベクトルの角度
     * 時計回りに0<= θ <= 2PI
     * @param vf
     * @param vs
     * @return      角度(rad)
     */
    public static float getAngleXY(Vector3 vf, Vector3 vs) {
        Vector3 vf2 = new Vector3(vf);
        vf2.Z = 0f;
        Vector3 vs2 = new Vector3(vs);
        vs2.Z = 0f;
        float ang = getAngle(vf2, vs2);
        Vector3 outProduct = outerProduct(vf2, vs2);
        if (0f > outProduct.Z)
            ang = (float)Math.PI * 2f - ang;
        return ang;
    }

    /**
     * YZ平面上での2っのベクトルの角度
     * 時計回りに0<= θ <= 2PI
     * @param vf
     * @param vs
     * @return      角度(rad)
     */
    public static float getAngleYZ(Vector3 vf, Vector3 vs) {
        Vector3 vf2 = new Vector3(vf);
        vf2.X = 0f;
        Vector3 vs2 = new Vector3(vs);
        vs2.X = 0f;
        float ang = getAngle(vf2, vs2);
        Vector3 outProduct = outerProduct(vf2, vs2);
        if (0f > outProduct.X)
            ang = (float)Math.PI * 2f - ang;
        return ang;
    }

    /**
     * ZX平面上での2っのベクトルの角度
     * 時計回りに0<= θ <= 2PI
     * @param vf
     * @param vs
     * @return      角度(rad)
     */
    public static float getAngleZX(Vector3 vf, Vector3 vs) {
        Vector3 vf2 = new Vector3(vf);
        vf2.Y = 0f;
        Vector3 vs2 = new Vector3(vs);
        vs2.Y = 0f;
        float ang = getAngle(vf2, vs2);
        Vector3 outProduct = outerProduct(vf2, vs2);
        if (0f > outProduct.Y)
            ang = (float)Math.PI * 2f - ang;
        return ang;
    }

    /**
     * 2つのベクトルの角度(0 <= θ <= PI
     * 内積の公式　OA・OB = |OA||OB|cos(θ)より求める
     * @param vf
     * @param vs
     * @return      角度(rad)
     */
    public static float getAngle(Vector3 vf,Vector3 vs) {
        if (length(vf) * length(vs) == 0f)
            return -1f;
        float cosang = innerProduct(vf, vs) / (length(vf) * length(vs));
        if (1f < cosang)
            cosang = 1f;
        else if (cosang < -1f)
            cosang = -1f;
        return (float)Math.acos(cosang);
    }

    /**
     * ベクトルの内積
     * @param vf
     * @param vs
     * @return
     */
    private static float innerProduct(Vector3 vf, Vector3 vs) {
        return vf.X * vs.X + vf.Y * vs.Y + vf.Z *vs.Z;
    }

    /**
     * ベクトルの外積
     * @param vf
     * @param vs
     * @return
     */
    private static Vector3 outerProduct(Vector3 vf, Vector3 vs) {
        return new Vector3(vf.Y * vs.Z - vf.Z * vs.Y, vf.Z * vs.X - vf.X * vs.Z, vf.X * vs.Y - vf.Y * vs.X);
    }

    /**
     * ベクトルの長さ
     * @param vec
     * @return
     */
    private static float length(Vector3 vec) {
        return (float)Math.sqrt(vec.X * vec.X + vec.Y * vec.Y + vec.Z * vec.Z);
    }
}
