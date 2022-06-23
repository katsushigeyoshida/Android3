package jp.co.yoshida.katsushige.gameapp;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.yoshida.katsushige.mylib.Vector3;

public class GLRenderer implements GLSurfaceView.Renderer {

    static final String TAG = "GLRenderer";

    private int mWidth;
    private int mHeight;

    private static final int SIZEOF_BYTE  = Byte.SIZE / 8;  // Byte.SIZEで、byte型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_SHORT = Short.SIZE / 8; // Short.SIZEで、short型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_FLOAT = Float.SIZE / 8; // Float.SIZEで、float型のビット数が得られるので、8で割って、バイト数を得る
    private float[][] mColorData = {
            {0.0f, 0.0f, 0.0f, 0.0f},   //  黒
            {1.0f, 0.0f, 0.0f, 0.0f},   //  赤
            {0.0f, 1.0f, 0.0f, 0.0f},   //  緑
            {0.0f, 0.0f, 1.0f, 0.0f},   //  青
            {1.0f, 1.0f, 0.0f, 0.0f},   //  黄色
            {0.0f, 1.0f, 1.0f, 0.0f},   //  水色
            {1.0f, 0.0f, 1.0f, 0.0f},   //  ピンク
            {1.0f, 1.0f, 1.0f, 0.0f},   //  白
            {0.82f, 0.82f, 0.82f, 0.0f},// lightgray
    };
    enum COLORDATA {Black, Red, Green, Blue, Yellow, Cyane, Pink, White, LightGray};
    private boolean mViewingFrustrumValid;  // 視野角錐台設定の有効性
    private boolean mViewingTransformValid; // 視点座標変換設定の有効性

    enum TRAKING_MODE { NONE, ROTATE, PAN, ZOOM}    //  トラッキングモード
    private TRAKING_MODE mTrakingMode = TRAKING_MODE.NONE;
    private float mLastX;                           //  直前の座標
    private float mLastY;                           //  直前の座標
    private float mRenderingRate;                   //  描画倍率
    private float[] mObjectForm = new float[16];    //  オブジェクトフォーム
    private float mRenderingCenterX;                //  描画中心座標
    private float mRenderingCenterY;                //  描画中心座標
    private float[] mMatrixTemp1 = new float[16];   //  テンポラリ行列
    private float[] mMatrixTemp2 = new float[16];   //  テンポラリ行列
    public float mCubeSize = 1.0f;                 //  個々の立方体サイズ
    public int mCubes = 3;                          //  一辺の数
    public CubeUnit[][][] mCube;                   //  個々の立方体データ

    private GL10 mGL;

    public GL10 getGL() {
        return mGL;
    }

    public GLRenderer() {
        mRenderingRate = 150.0f;                    //  描画倍率
        Matrix.setIdentityM(mObjectForm, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mGL = gl10;
        // クリア処理
        mGL.glClearColor( 0.8f, 0.8f, 0.8f, 0f ); // 背景色(ライトグレイ)
        mGL.glClearDepthf( 1.0f );           // クリア深度
        // デプス処理
        mGL.glEnable( GL10.GL_DEPTH_TEST);     // デプステスト
        mGL.glDepthFunc( GL10.GL_LEQUAL);      // デプスファンクの設定（同じか、手前にあるもので上描いていく）
        mGL.glDepthMask( true );             // デプスバッファーへの書き込み許可
        // ポリゴン処理
        mGL.glDisable( GL10.GL_CULL_FACE );             // 裏を向いている面のカリング
        mGL.glEnable( GL10.GL_NORMALIZE );              // 法線処理
        mGL.glDisable( GL10.GL_POLYGON_OFFSET_FILL );   // ポリゴンオフセットフィル
        mGL.glPolygonOffset( 1.0f, 1.0f );       // ポリゴンオフセット量
        // ライティング処理
        mGL.glDisable( GL10.GL_LIGHTING );           // 光源
        mGL.glDisable( GL10.GL_COLOR_MATERIAL );     // カラー設定値をマテリアルとして使用
        // ブレンド処理
        mGL.glDisable( GL10.GL_BLEND );              // 半透明およびアンチエイリアシング
        mGL.glDisable( GL10.GL_POINT_SMOOTH );       // 点のスムース処理
        mGL.glDisable( GL10.GL_LINE_SMOOTH );        // 線のスムース処理
        mGL.glShadeModel( GL10.GL_SMOOTH );          // シェーディングモード

        initCube();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mWidth = width;
        mHeight = height;

        gl10.glViewport(0, 0, width, height); //  ビューポートの設定
        setViewingFrustumValid(false);              // 視野角錐台設定の無効化
        setViewingTransformValid(false);            // 視点座標変換設定の無効化
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        if (!isViewingTransformValid())
            setupViewingTransform();
        if (!isViewingFrustumValid())
            setupViewingFrustum();

        drawAxis(gl10, 3f);
        draw3D(gl10);
    }

    /**
     * トラッキングモードの状態取得
     * @return
     */
    public TRAKING_MODE getTrakingMode() {
        return mTrakingMode;
    }

    /**
     * トラッキングの開始
     * @param fx            開始座標
     * @param fy
     * @param trakingMode   トラッキングモード(ROTATE/PAN/ZOOM/NONE)
     */
    public void beginTraking(float fx, float fy, TRAKING_MODE trakingMode) {
        mTrakingMode = trakingMode;
        mLastX = fx;
        mLastY = fy;
    }

    /**
     * トラッキングの終了
     */
    public void endTraking() {
        mTrakingMode = TRAKING_MODE.NONE;
    }

    /**
     * トラッキング処理
     * @param x     座標値
     * @param y     座標値
     */
    public void doTraking(float x, float y) {
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        mLastX = x;
        mLastY = y;
        if (deltaX==0 && deltaY==0)
            return;
        switch (mTrakingMode) {
            case NONE:
                break;
            case ROTATE:
                float angleDeg = (float)(Math.sqrt(deltaX*deltaX+deltaY*deltaY)*180.0/(mWidth<mHeight?mWidth:mHeight));
                Matrix.setRotateM(mMatrixTemp1, 0, angleDeg, deltaY, deltaX, 0.0f);
                Matrix.multiplyMM(mMatrixTemp2, 0, mMatrixTemp1, 0, mObjectForm, 0);
                System.arraycopy(mMatrixTemp2, 0, mObjectForm, 0, 16);
                setViewingTransformValid(false);
                break;
            case PAN:
                mRenderingCenterX -= deltaX / mRenderingRate;
                mRenderingCenterY += deltaY / mRenderingRate;
                setViewingTransformValid(false);
                break;
            case ZOOM:
                mRenderingRate *= (float)Math.pow(2.0, -deltaY*0.002);
                setViewingFrustumValid(false);
                break;
        }
    }

    /***
     * 視野角錐台設定(拡大/縮小)
     */
    public void setupViewingFrustum() {
        mGL.glMatrixMode(GL10.GL_PROJECTION);
        mGL.glLoadIdentity();
        //  直投影ビューの設定(ビューのボリューム 左、右、下、上、前、後を設定)この場合視点の位置はeyeZ=500)
        mGL.glOrthof(-mWidth*0.5f/mRenderingRate, mWidth*0.5f/mRenderingRate,
                -mHeight*0.5f/mRenderingRate, mHeight*0.5f/mRenderingRate,
                0.1f, 1000.0f);
        //  透視投影(この場合視点の位置はeyeZ=10)
//        GLU.gluPerspective(mGL, 45f, mWidth/ mHeight, 1f, 1000.0f);   //  実機で使えない
//        gluPerspective(mGL, 45f, mWidth/ mHeight, 1f, 1000.0f);
        mGL.glMatrixMode(GL10.GL_MODELVIEW);
        setViewingFrustumValid(true);
    }

    //透視変換の指定
    public static void gluPerspective(GL10 gl, float angle, float aspect, float near, float far) {
        float top,bottom,left,right;
        if(aspect<1f) {
            top = near * (float) Math.tan(angle * (Math.PI / 360.0));
            bottom = -top;
            left = bottom * aspect;
            right = -left;
        } else {
            right = 1.1f*near * (float) Math.tan(angle * (Math.PI / 360.0));
            left = -right;
            bottom = left / aspect;
            top = -bottom;
        }
        gl.glFrustumf( left, right, bottom, top, near, far);
    }

    /***
     * 視点座標変換設定(回転/移動)
     */
    public void setupViewingTransform() {
        mGL.glMatrixMode(GL10.GL_MODELVIEW);
        mGL.glLoadIdentity();
        //  視点設定(視点座標、見る方向の座標、上方向の座標)
        GLU.gluLookAt(mGL,
                mRenderingCenterX, mRenderingCenterY, 500f,      //  視点の位置
                mRenderingCenterX, mRenderingCenterY, 0.0f,     //  見ている先
                0.0f, 1.0f, 0.0f);                      //  視界の上方向
        mGL.glMultMatrixf(mObjectForm, 0);  //  トラッキングデータと掛け合わせる
        setViewingTransformValid(true);
    }

    /**
     * 視野角推台設定有無の取得
     * @return
     */
    public boolean isViewingFrustumValid() {
        return mViewingFrustrumValid;
    }

    /**
     * 視点座標設定有無の取得
     * @return
     */
    public boolean isViewingTransformValid() {
        return mViewingTransformValid;
    }

    //

    /**
     * 視野角錐台設定有無の設定
     * @param arg
     */
    public void setViewingFrustumValid(boolean arg) {
        mViewingFrustrumValid = arg;
    }

    /**
     * 視点座標変換設定有無の設定
     * @param arg
     */
    public void setViewingTransformValid(boolean arg) {
        mViewingTransformValid = arg;
    }

    /**
     * ルービックキューブの大きさの設定
     * @param cubes     １辺の個数(2or3)
     * @param size      個々の立方体の大きさ
     */
    public void setCubeSize(int cubes, float size ) {
        mCubes = cubes;
        mCubeSize = size;
        initCube();
    }

    /**
     * 立方体全体の表示
     * @param gl
     */
    public void draw3D(GL10 gl) {
        for (int x = 0; x < mCube.length; x++)
            for (int y = 0; y < mCube[x].length; y++)
                for (int z = 0; z < mCube[x][y].length; z++)
                    drawCube(gl, mCube[x][y][z]);
    }

    /**
     * 個々の立方体の初期化
     * 立体的に配置する個々の立方体データを設定
     */
    public void initCube() {
        mCube = new CubeUnit[mCubes][mCubes][mCubes];
        for (int x = 0; x < mCube.length; x++)
            for (int y = 0; y < mCube[x].length; y++)
                for (int z = 0; z < mCube[x][y].length; z++)
                    mCube[x][y][z] = new CubeUnit(
                        new Vector3(
                                mCubeSize*(x - (mCubes - 1f) / 2f),
                                mCubeSize*(y - (mCubes - 1f) / 2f),
                                mCubeSize*(z - (mCubes - 1f) / 2f)),
                        x*100+y*10+z);
    }

    /**
     * 個別のデータに合わせて個々の立方体を配置する
     * @param gl
     * @param cube      立方体データ(移動、回転データ)
     */
    private void drawCube(GL10 gl, CubeUnit cube) {
        gl.glPushMatrix();
//        gl.glMatrixMode(GL10.GL_MODELVIEW);         //  行列演算ターゲットの設定
//        gl.glLoadIdentity();                        //  単位行列をロード
        float scale = mCubes==2 ?0.75f : 1f;
        gl.glScalef(scale, scale,scale);        //  スケーリング(dlScale(TYPE sx, TYPE sy, TYPE sz)
        //  移動位置の表示は初期状態からの回転操作をトレースすることにより再現(なぜか逆順でないと再現できない)
        for (int i = cube.mAngList.size() - 1; 0 <= i; i--) {
            if (0 != cube.mAngList.get(i).X)
                gl.glRotatef(cube.mAngList.get(i).X, 1f, 0f,0f);     // X軸回転
            if (0 != cube.mAngList.get(i).Y)
                gl.glRotatef(cube.mAngList.get(i).Y, 0f, 1f,0f);     // Y軸回転
            if (0 != cube.mAngList.get(i).Z)
                gl.glRotatef(cube.mAngList.get(i).Z, 0f, 0f,1f);     // Z軸回転
        }
        gl.glTranslatef(cube.mPos.X, cube.mPos.Y, cube.mPos.Z);                //  平行移動

        draw3DCube(gl);

        gl.glPopMatrix();
    }

    /**
     * 3次元軸の描画
     * @param gl
     */
    private void drawAxis(GL10 gl, float size) {
        gl.glPushMatrix();
        gl.glScalef(size, size, size);    //  スケーリング(dlScale(TYPE sx, TYPE sy, TYPE sz)
        draw3DAxis(gl);
        gl.glPopMatrix();
    }

    /**
     * 立方体の描画
     * @param gl    OpenGL
     */
    private void draw3DCube(GL10 gl) {
        float edge = mCubeSize * 0.5f;
        float edge1 = mCubeSize * 0.47f;
        float edge2 = mCubeSize * 0.499f;
        float vertex[] = {
                //  立方体の外側の座標
                // 前 (0)
                -edge1, -edge1, edge,
                 edge1, -edge1, edge,
                -edge1,  edge1, edge,
                 edge1,  edge1, edge,
                // 後 (4)
                -edge1, -edge1, -edge,
                 edge1, -edge1, -edge,
                -edge1,  edge1, -edge,
                 edge1,  edge1, -edge,
                // 左 (8)
                -edge, -edge1,  edge1,
                -edge, -edge1, -edge1,
                -edge,  edge1,  edge1,
                -edge,  edge1, -edge1,
                // 右 (12)
                 edge, -edge1,  edge1,
                 edge, -edge1, -edge1,
                 edge,  edge1,  edge1,
                 edge,  edge1, -edge1,
                // 上 (16)
                -edge1, edge,  edge1,
                 edge1, edge,  edge1,
                -edge1, edge, -edge1,
                 edge1, edge, -edge1,
                // 底 (20)
                -edge1, -edge,  edge1,
                 edge1, -edge,  edge1,
                -edge1, -edge, -edge1,
                 edge1, -edge, -edge1,
                //  立方体の内側の座標
                // 前 (24)
                -edge2, -edge2, edge2,
                 edge2, -edge2, edge2,
                -edge2,  edge2, edge2,
                 edge2,  edge2, edge2,
                // 後 (28)
                -edge2, -edge2, -edge2,
                 edge2, -edge2, -edge2,
                -edge2,  edge2, -edge2,
                 edge2,  edge2, -edge2,
                // 左 (32)
                -edge2, -edge2,  edge2,
                -edge2, -edge2, -edge2,
                -edge2,  edge2,  edge2,
                -edge2,  edge2, -edge2,
                // 右 (36)
                edge2, -edge2,  edge2,
                edge2, -edge2, -edge2,
                edge2,  edge2,  edge2,
                edge2,  edge2, -edge2,
                // 上 (40)
                -edge2, edge2,  edge2,
                 edge2, edge2,  edge2,
                -edge2, edge2, -edge2,
                 edge2, edge2, -edge2,
                // 底 (44)
                -edge2, -edge2,  edge2,
                 edge2, -edge2,  edge2,
                -edge2, -edge2, -edge2,
                 edge2, -edge2, -edge2
        };
        FloatBuffer v_buf = makeFloatBuffer(vertex);


        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);           //  頂点配列を有効にする
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, v_buf);   //  頂点配列を定義
        //  立方体の表面
        drawPlane(gl, COLORDATA.Yellow, 0, 0, 1.0f, 0, 4);  // Front
        drawPlane(gl, COLORDATA.White, 0, 0, -1.0f, 4, 4);  // Back
        drawPlane(gl, COLORDATA.Green, -1.0f, 0, 0, 8, 4);  // Left
        drawPlane(gl, COLORDATA.Blue, 1.0f, 0, 0, 12, 4);   // Right
        drawPlane(gl, COLORDATA.Red, 0, 1.0f, 0, 16, 4);    // Top
        drawPlane(gl, COLORDATA.Pink, 0, -1.0f, 0, 20, 4);  // Down
        //  律母体の内側を黒くする
        drawPlane(gl, COLORDATA.Black, 0, 0, 1.0f, 24, 4);  // Front
        drawPlane(gl, COLORDATA.Black, 0, 0, -1.0f, 28, 4); // Back
        drawPlane(gl, COLORDATA.Black, -1.0f, 0, 0, 32, 4); // Left
        drawPlane(gl, COLORDATA.Black, 1.0f, 0, 0, 36, 4);  // Right
        drawPlane(gl, COLORDATA.Black, 0, 1.0f, 0, 40, 4);  // Top
        drawPlane(gl, COLORDATA.Black, 0, -1.0f, 0, 44, 4); // Down
    }

    /**
     * 軸データの表示
     * @param gl
     */
    private void draw3DAxis(GL10 gl) {
        float[] vertexs = {
                //  x, y, z
                 0f,   0f,   0f,  //x axis start     P0
                 1f,   0f,   0f,                   //P1
                 0.9f, 0.05f,0f,                   //P2
                 0.9f, 0f,   0f,                   //P3
                 0f,   0f,   0f,  //x axis end     //P4
                 0f,   0f,   0f,  //y axis start   //P5
                 0f,   1f,   0f,                   //P6
                -0.05f,0.9f, 0f,                   //P7
                 0f,   0.9f, 0f,                   //P8
                 0f,   0f,   0f,  //y axis end      P9
                 0f,   0f,   0f,  //z axis start    P10
                 0f,   0f,   1f,                  //P11
                -0.05f,0f,   0.9f,                //P12
                 0f,   0f,   0.9f,//z axis end      P13

                 1.05f,0f,   0f,  //char X          P14
                 1.15f,0.12f,0f,                  //P15
                 1.1f, 0.06f,0f,                  //P16
                 1.05f,0.12f,0f,                  //P17
                 1.15f,0f,   0f,                  //P18

                 0.05f,1.05f,0f, //char Y         //P19
                 0.05f,1.12f,0f,                  //P20
                 0f,   1.17f,0f,                  //P21
                 0.05f,1.12f,0f,                  //P22
                 0.1f, 1.17f,0f,                  //P23

                 0.05f,0.12f,1.05f,   //char Z      P24
                 0.1f, 0.12f,1.05f,               //P25
                 0.05f,0f,   1.05f,               //P26
                 0.1f, 0f,   1.05f                //P27
        };

        FloatBuffer v_buf = makeFloatBuffer(vertexs);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);           //  頂点配列を有効にする
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, v_buf);   //  頂点配列を定義
        drawPolyLine(gl, COLORDATA.Blue, 2f, 0, 5);
        drawPolyLine(gl, COLORDATA.Red, 2f, 5, 5);
        drawPolyLine(gl, COLORDATA.Yellow, 2f, 10, 4);
        drawPolyLine(gl, COLORDATA.Black, 2f, 14, 5);
        drawPolyLine(gl, COLORDATA.Black, 2f, 19, 5);
        drawPolyLine(gl, COLORDATA.Black, 2f, 24, 4);
    }

    /**
     * 四角平面の描画
     * @param gl        OpenGL
     * @param color     カラー
     * @param x         法線ベクトル X座標
     * @param y         法線ベクトル Y座標
     * @param z         法線ベクトル Z座標
     * @param start     四角平面データの開始位置
     * @param size      四角平面データのサイズ
     */
    private void drawPlane(GL10 gl, COLORDATA color, float x, float y, float z, int start, int size) {
        glColorSet(gl, color);
        gl.glNormal3f(x, y, z);         //  法線ベクトル(なくてもよい)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, start, size);
    }

    /**
     * 軸用連続線分の表示
     * @param gl        OpenGL
     * @param color     カラー
     * @param width     線幅
     * @param start     軸データ開始位置
     * @param size      軸データサイズ
     */
    private void drawPolyLine(GL10 gl, COLORDATA color, float width, int start, int size) {
        glColorSet(gl, color);
        gl.glLineWidth(width);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, start, size);
    }

    /**
     * カラーの設定
     * @param gl        OpenGL
     * @param color     カラー
     */
    private void glColorSet(GL10 gl, COLORDATA color) {
        gl.glColor4f(mColorData[color.ordinal()][0], mColorData[color.ordinal()][1],
                mColorData[color.ordinal()][2],mColorData[color.ordinal()][3]);
    }

    /**
     * floatバッファーへのデータ変換
     * @param arr   データ配列
     * @return      FloatBufferデータ
     */
    public static FloatBuffer makeFloatBuffer( float[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_FLOAT );
        bb.order( ByteOrder.nativeOrder() );
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put( arr );
        fb.position( 0 );
        return fb;
    }
}
