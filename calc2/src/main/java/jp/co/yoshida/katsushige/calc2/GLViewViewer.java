package jp.co.yoshida.katsushige.calc2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLViewViewer extends GLSurfaceView {

    private static final String TAG = "GLViewViewer";

    private GLViewRenderer mGLViewRenderer;

    public GLViewViewer(Context context) {
        super(context);

        mGLViewRenderer = new GLViewRenderer();
        setRenderer(mGLViewRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG,"onTouchEvent: "+event.getX()+" "+event.getY()+"  multi: "+event.getPointerCount());

        int action = event.getAction();
        int pointCount = event.getPointerCount();
        final float fx = event.getX();
        final float fy = event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointCount==1) {
                    mGLViewRenderer.beginTraking(fx, fy, GLViewRenderer.TRAKING_MODE.ROTATE);
                } else if (pointCount==2) {
                    float fx1 = event.getX(1);
                    float fy1 = event.getY(1);
                    final float distance = (float)Math.sqrt((fx1-fx)*(fx1-fx)+(fy1-fy)*(fy1-fy));
                    if (300 < distance) {
                        mGLViewRenderer.beginTraking(-distance, -distance, GLViewRenderer.TRAKING_MODE.ZOOM);
                    } else {
                        mGLViewRenderer.beginTraking(fx, fy, GLViewRenderer.TRAKING_MODE.PAN);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mGLViewRenderer.endTraking();
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointCount==2 && mGLViewRenderer.getTrakingMode()== GLViewRenderer.TRAKING_MODE.ZOOM) {
                    float fx1 = event.getX(1);
                    float fy1 = event.getY(1);
                    final float distance = (float) Math.sqrt((fx1 - fx) * (fx1 - fx) + (fy1 - fy) * (fy1 - fy));
                    mGLViewRenderer.doTraking(-distance, -distance);
                } else {
                    mGLViewRenderer.doTraking(fx, fy);
                }
                requestRender();
                break;
        }
        return true;
//        return super.onTouchEvent(event);
    }

    public void setVertexData(float[] vertex, int primitive) {
        mGLViewRenderer.setVertexData(vertex, primitive, null, null);
    }

    public void setVertexData(float[] vertex, int primitive, int[] sizeIndex, float[][] color) {
        mGLViewRenderer.setVertexData(vertex, primitive, sizeIndex, color);
    }
}
