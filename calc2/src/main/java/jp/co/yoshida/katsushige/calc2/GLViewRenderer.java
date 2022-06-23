package jp.co.yoshida.katsushige.calc2;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLViewRenderer implements GLSurfaceView.Renderer {

    static final String TAG = "GLViewRederer";

    private int mWidth;
    private int mHeight;

    private GL10 mGL;

    private static final int SIZEOF_BYTE  = Byte.SIZE / 8;    // Byte.SIZEで、byte型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_SHORT = Short.SIZE / 8;    // Short.SIZEで、short型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_FLOAT = Float.SIZE / 8;    // Float.SIZEで、float型のビット数が得られるので、8で割って、バイト数を得る
    private float[][] mColorData = {
            {1.0f, 0.0f, 0.0f, 0.0f},   //  赤
            {0.0f, 1.0f, 0.0f, 0.0f},   //  緑
            {0.0f, 0.0f, 1.0f, 0.0f},   //  青
            {1.0f, 1.0f, 0.0f, 0.0f},   //  黄色
            {0.0f, 1.0f, 1.0f, 0.0f},   //  水色
            {1.0f, 0.0f, 1.0f, 0.0f},   //  ピンク
            {1.0f, 1.0f, 1.0f, 0.0f},   //  白
    };

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

    private int mPrimitive;                         //  座標データの種類
    private float[] mVertex;                        //  座標データ
    private FloatBuffer mVertexBuff;                //  頂点座標
    private ShortBuffer mTriangleVertexIndex;       //  三角形の頂点番号配列（unsigned shortの上限は65535）
    private ShortBuffer mEdgeVertexIndex;           //  稜線の番号しい列（unsigned shortの上限は65535）
    private float[][] mColor;
    private int[] mColorIndex;
    private int[] mVertexSize;

    public GL10 getGL() {
        return mGL;
    }


    public GLViewRenderer() {
        mRenderingRate = 300.0f;
        Matrix.setIdentityM(mObjectForm, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        mGL = gl;

        // クリア処理
        gl.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f ); // クリアカラー
        gl.glClearDepthf( 1.0f ); // クリア深度

        // デプス処理
        gl.glEnable( GL10.GL_DEPTH_TEST );  // デプステスト
        gl.glDepthFunc( GL10.GL_LEQUAL );   // デプスファンクの設定（同じか、手前にあるもので上描いていく）
        gl.glDepthMask( true );         // デプスバッファーへの書き込み許可

        // ポリゴン処理
        gl.glDisable( GL10.GL_CULL_FACE );  // 裏を向いている面のカリング
        gl.glEnable( GL10.GL_NORMALIZE );   // 法線処理
        gl.glDisable( GL10.GL_POLYGON_OFFSET_FILL );    // ポリゴンオフセットフィル
        gl.glPolygonOffset( 1.0f, 1.0f );       // ポリゴンオフセット量

        // ライティング処理
        gl.glDisable( GL10.GL_LIGHTING );           // 光源
        gl.glDisable( GL10.GL_COLOR_MATERIAL );     // カラー設定値をマテリアルとして使用

        // ブレンド処理
        gl.glDisable( GL10.GL_BLEND );              // 半透明およびアンチエイリアシング
        gl.glDisable( GL10.GL_POINT_SMOOTH );       // 点のスムース処理
        gl.glDisable( GL10.GL_LINE_SMOOTH );        // 線のスムース処理
        gl.glShadeModel( GL10.GL_SMOOTH );          // シェーディングモード
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        gl.glViewport(0, 0, width, height); //  ビューポートの設定
        setViewingFrustumValid(false);              // 視野角錐台設定の無効化
        setViewingTransformValid(false);            // 視点座標変換設定の無効化
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG,"onDrawFrame: ");
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        if (!isViewingFrustumValid())
            setupViewingFrustum();
        if (!isViewingTransformValid())
            setupViewingTransform();

//        draw3DCubu(gl);
        drawObject(gl);
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
        //  直投影ビューの設定(ビューのボリューム 左、右、下、上、前、後を設定)
        mGL.glOrthof(-mWidth*0.5f/mRenderingRate, mWidth*0.5f/mRenderingRate,
                -mHeight*0.5f/mRenderingRate, mHeight*0.5f/mRenderingRate,
                0.1f, 1000.0f);
        mGL.glMatrixMode(GL10.GL_MODELVIEW);
        setViewingFrustumValid(true);
    }

    /***
     * 視点座標変換設定(回転/移動)
     */
    public void setupViewingTransform() {
        mGL.glMatrixMode(GL10.GL_MODELVIEW);
        mGL.glLoadIdentity();
        //  視点設定(視点座標、見る方向の座標、上方向の座標)
        GLU.gluLookAt(mGL,
                mRenderingCenterX, mRenderingCenterY, 500,
                mRenderingCenterX, mRenderingCenterY, 0.0f,
                0.0f, 1.0f, 0.0f);
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
     * 領域幅の取得
     * @return  幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 領域高さの取得
     * @return  高さ
     */
    public int getHeight() {
        return mHeight;
    }


    private  void drawObject(GL10 gl) {
        if (mVertex==null)
            return;
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT,0, mVertexBuff);
        if (mPrimitive==GL10.GL_TRIANGLES) {
            if (mTriangleVertexIndex!=null) {
                gl.glColor4f( 0.5f, 0.5f, 0.0f, 1.0f );
                gl.glDrawElements(GL10.GL_TRIANGLES,
                        mTriangleVertexIndex.capacity(),
                        GL10.GL_UNSIGNED_SHORT,
                        mTriangleVertexIndex.position(0));
            }
            if (mEdgeVertexIndex!=null) {
                gl.glLineWidth(2.0f);
                gl.glColor4f(0.0f, 0.5f, 0.5f, 1.0f);
                gl.glDrawElements(GL10.GL_LINES,
                        mEdgeVertexIndex.capacity(),
                        GL10.GL_UNSIGNED_SHORT,
                        mEdgeVertexIndex.position(0));
            }
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        } else if (mPrimitive==GL10.GL_TRIANGLE_STRIP) {
            int pos = 0;
//            Log.d(TAG,"drawObject: GL_TRIANGLE_STRIP: "+mVertex.length+" "+mColor.length+" "+mVertexSize.length);
            for (int index = 0; index < mVertexSize.length; index++) {
//                Log.d(TAG,"drawObject: "+index+" "+mColor[index][0]+" "+pos+" "+mVertexSize[index]);
                gl.glColor4f(mColor[index][0], mColor[index][1], mColor[index][2],mColor[index][3]);
//                gl.glNormal3f(0, 0, 1.0f);
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, pos, mVertexSize[index]);
                pos += mVertexSize[index];
            }
        }
    }

    public void setVertexData(float[] vertex, int primitive, int[] sizeIndex, float[][] color) {
        mPrimitive = primitive;
        mVertex = vertex;
        mVertexBuff = makeFloatBuffer(vertex);
        mVertexSize = sizeIndex;
        mColor = color;

        if (mPrimitive==GL10.GL_TRIANGLES) {
            int countPoint = vertex.length / 3;
            int countTriangle = countPoint / 3;
            short[] triangleVertexIndex = new short[countTriangle * 3];
            for (int indexTriangle = 0; indexTriangle < countTriangle; indexTriangle++) {
                triangleVertexIndex[indexTriangle * 3 + 0] = (short) (indexTriangle * 3 + 0);
                triangleVertexIndex[indexTriangle * 3 + 1] = (short) (indexTriangle * 3 + 1);
                triangleVertexIndex[indexTriangle * 3 + 2] = (short) (indexTriangle * 3 + 2);
            }
            mTriangleVertexIndex = makeShortBuffer(triangleVertexIndex);

            short[] edgeVertexIndex = new short[countTriangle * 3 * 2];
            for (int indexTriangle = 0; indexTriangle < countTriangle; indexTriangle++) {
                edgeVertexIndex[indexTriangle * 6 + 0] = triangleVertexIndex[indexTriangle * 3 + 0];
                edgeVertexIndex[indexTriangle * 6 + 1] = triangleVertexIndex[indexTriangle * 3 + 1];
                edgeVertexIndex[indexTriangle * 6 + 2] = triangleVertexIndex[indexTriangle * 3 + 1];
                edgeVertexIndex[indexTriangle * 6 + 3] = triangleVertexIndex[indexTriangle * 3 + 2];
                edgeVertexIndex[indexTriangle * 6 + 4] = triangleVertexIndex[indexTriangle * 3 + 2];
                edgeVertexIndex[indexTriangle * 6 + 5] = triangleVertexIndex[indexTriangle * 3 + 0];
            }
            mEdgeVertexIndex = makeShortBuffer(edgeVertexIndex);
        }

        float min = vertex[0];
        float max = vertex[0];
        for (int i = 1; i < vertex.length; i++) {
            min = Math.min(min,vertex[i]);
            max = Math.max(max,vertex[i]);
        }
        mRenderingRate = 300 / (max - min);
        Log.d(TAG,"setVertexData: "+mRenderingRate+" "+min+" "+max);
    }

    // byteバッファーの作成
    public static ByteBuffer makeByteBuffer(byte[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_BYTE ); // byte:1byte
        bb.order( ByteOrder.nativeOrder() );
        bb.put( arr );
        bb.position( 0 );
        return bb;
    }

    // shortバッファーの作成
    public static ShortBuffer makeShortBuffer( short[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_SHORT ); // short:2byte
        bb.order( ByteOrder.nativeOrder() );
        ShortBuffer sb = bb.asShortBuffer();
        sb.put( arr );
        sb.position( 0 );
        return sb;
    }

    // floatバッファーの作成
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
