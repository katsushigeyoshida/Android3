package jp.co.yoshida.katsushige.gameapp;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLView extends GLSurfaceView {

    private static final String TAG = "GLView";

    public GLRenderer mGLRenderer;

    public GLView(Context context) {
        super(context);
        mGLRenderer = new GLRenderer();
        setRenderer(mGLRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int pointCount = event.getPointerCount();
        final float fx = event.getX();
        final float fy = event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointCount==1) {
                    mGLRenderer.beginTraking(fx, fy, GLRenderer.TRAKING_MODE.ROTATE);
                } else if (pointCount==2) {
                    float fx1 = event.getX(1);
                    float fy1 = event.getY(1);
                    final float distance = (float)Math.sqrt((fx1-fx)*(fx1-fx)+(fy1-fy)*(fy1-fy));
                    if (300 < distance) {
                        mGLRenderer.beginTraking(-distance, -distance, GLRenderer.TRAKING_MODE.ZOOM);
                    } else {
                        mGLRenderer.beginTraking(fx, fy, GLRenderer.TRAKING_MODE.PAN);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mGLRenderer.endTraking();
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointCount==2 && mGLRenderer.getTrakingMode()== GLRenderer.TRAKING_MODE.ZOOM) {
                    float fx1 = event.getX(1);
                    float fy1 = event.getY(1);
                    final float distance = (float) Math.sqrt((fx1 - fx) * (fx1 - fx) + (fy1 - fy) * (fy1 - fy));
                    mGLRenderer.doTraking(-distance, -distance);
                } else {
                    mGLRenderer.doTraking(fx, fy);
                }
                requestRender();
                break;
        }
        return true;
//        return super.onTouchEvent(event);
    }
}
