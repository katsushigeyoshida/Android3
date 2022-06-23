package jp.co.yoshida.katsushige.mylib;

public class Vector3I {
        public int X = 0;
        public int Y = 0;
        public int Z = 0;

    public Vector3I() {
    }
    public Vector3I(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
    }
    public Vector3I(float x, float y, float z) {
        X = (int)Math.round(x);
        Y = (int)Math.round(y);
        Z = (int)Math.round(z);
    }
    public Vector3I(Vector3I v) {
        X = v.X;
        Y = v.Y;
        Z = v.Z;
    }
    public Vector3I(Vector3 vec) {
        X = (int)Math.round(vec.X);
        Y = (int)Math.round(vec.Y);
        Z = (int)Math.round(vec.Z);
    }
    public Vector3 get() {
        return new Vector3(X, Y, Z);
    }

    public void add(Vector3I v) {
        X += v.X;
        Y += v.Y;
        Z += v.Z;
    }
}
