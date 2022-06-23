package jp.co.yoshida.katsushige.calc2;


/**
 * 三次元座標クラス
 * javafx.geometry のPoint3D互換の予定
 */
public class Point3D {
    public double X;
    public double Y;
    public double Z;

    /**
     * コンストラクタ Point3Dの新しいインスタンスを作成
     */
    public Point3D() {

    }

    /**
     * コンストラクタ Point3Dの新しいインスタンスを作成
     * @param x
     * @param y
     * @param z
     */
    public Point3D(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }

    /**
     * コンストラクタ Point3Dの新しいインスタンスを作成
     * @param point
     */
    public Point3D(Point3D point) {
        X = point.X;
        Y = point.Y;
        Z = point.Z;
    }

    /**
     * この点の座標に指定された座標が加算された点を返します。
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Point3D add(double x, double y, double z) {
        X += x;
        Y += y;
        Z += z;
        return new Point3D(X, Y, Z);
    }

    /**
     * この点の座標に指定された座標が加算された点を返します。
     * @param p
     * @return
     */
    public Point3D add(Point3D p) {
        X += p.X;
        Y += p.Y;
        Z += p.Z;
        return new Point3D(X, Y, Z);
    }

    /**
     * この点で表されるベクトルと指定されたベクトルの間の角度(rad)を計算します。
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double angle(double x, double y, double z) {
        double c = (X*x+Y*y+Z*z) /
                (Math.sqrt(X*X+Y*Y+Z*Z)*Math.sqrt(x*x+y*y+z*z));
        return Math.acos(c);
    }

    /**
     * この点で表されるベクトルと指定されたベクトルの間の角度(rad)を計算します。
     * @param point
     * @return
     */
    public double angle(Point3D point) {
        double c = (X*point.X+Y*point.Y+Z*point.Z) /
                (Math.sqrt(X*X+Y*Y+Z*Z)*Math.sqrt(point.X*point.X+point.Y*point.Y+point.Z*point.Z));
        return Math.acos(c);
    }

    /**
     * このインスタンスで表されるベクトルと指定されたベクトルのクロス積(外積)を計算します。
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Point3D crossProduct(double x, double y, double z) {
        Point3D vec = new Point3D();
        vec.X = Y*z - Z*y;
        vec.Y = Z*x - X*z;
        vec.Z = X*y - Y*x;
        return  vec;
    }

    /**
     * このインスタンスで表されるベクトルと指定されたベクトルのクロス積(外積)を計算します。
     * @param point
     * @return
     */
    public Point3D crossProduct(Point3D point) {
        Point3D vec = new Point3D();
        vec.X = Y*point.Z - Z*point.Y;
        vec.Y = Z*point.X - X*point.Z;
        vec.Z = X*point.Y - Y*point.X;
        return  vec;
    }

    /**
     * この点と指定されたpointの間の距離を計算します。
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double distance(double x, double y, double z) {
        return  Math.sqrt((X-x)*(X-x)+(Y-y)*(Y-y)+(Z-z)*(Z-z));
    }

    /**
     * この点と指定されたpointの間の距離を計算します。
     * @param point
     * @return
     */
    public double distance(Point3D point) {
        return  Math.sqrt((X-point.X)*(X-point.X)+(Y-point.Y)*(Y-point.Y)+(Z-point.Z)*(Z-point.Z));
    }
    public double getX() {
        return X;
    }
    public double getY() {
        return Y;
    }
    public double getZ() {
        return Z;
    }
    public Point3D max(Point3D point) {
        Point3D max = new Point3D();
        max.X = Math.max(X,point.X);
        max.Y = Math.max(Y,point.X);
        max.Z = Math.max(Z,point.Z);
        return max;
    }
    public Point3D min(Point3D point) {
        Point3D min = new Point3D();
        min.X = Math.min(X,point.X);
        min.Y = Math.min(Y,point.X);
        min.Z = Math.min(Z,point.Z);
        return min;
    }

    public String toString() {
        String buf = X+","+Y+","+Z;
        return buf;
    }
}
