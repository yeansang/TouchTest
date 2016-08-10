package com.example.nemus.touchtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

/**
 * Created by nemus on 2016-08-08.
 */
public class PicView extends ImageView implements View.OnTouchListener {

    private boolean zoomToggle = false;
    private boolean toggle = true;
    boolean dxCal = true;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Matrix originMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private PointF midStart = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private ViewPager mViewPager;
    float startDrag = 0;
    float rightEdge = 0;
    float leftEdge = 0;

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    });

    public PicView(Context context) {
        super(context);
        this.setOnTouchListener(this);
        ViewParent vp = getParent();
        if (vp instanceof ViewPager) {
            mViewPager = (ViewPager) vp;
        } else {
            mViewPager = new ViewPager(null);
        }
    }

    public PicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;
        matrix = view.getImageMatrix();

        float[] values = new float[9];
        matrix.getValues(values);

        float[] edge = matrixEdges(view, null);

        Log.d("action", event.getAction() + "");
        mViewPager.beginFakeDrag();

        float xmas = madMax(edge[0], edge[2], edge[4], edge[6]);
        float xmin = madMin(edge[0], edge[2], edge[4], edge[6]);
        float xsize = view.getWidth();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (toggle) {
                    float[] value = new float[9];
                    matrix.getValues(value);
                    originMatrix.setValues(value);
                    toggle = false;
                }

                rightEdge = event.getRawX() - (xmas - xsize);
                leftEdge = event.getRawX() - xmin;

                Log.d("Scrollpoint", rightEdge + "/" + leftEdge);

                Log.d("switch", "1");
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("switch", "2");
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event, view);
                    midPoint(midStart, event, view);
                    mode = ZOOM;
                }
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mode = NONE;
                mViewPager.endFakeDrag();
                dxCal = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("switch", "3");
                //mode = NONE;
                Log.d("point", event.getPointerCount() + "");

                if (event.getPointerCount() == 2) {
                    //event.
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                }
                mViewPager.endFakeDrag();
                dxCal = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float[] value = new float[9];
                matrix.getValues(value);
                if (mode == DRAG) {

                    float dx = event.getRawX() - start.x;
                    start.x = event.getRawX();
                    float dy = event.getY() - start.y;
                    start.y = event.getY();

                    int scrollMode = 0;

                    if (dxCal) {
                        startDrag = event.getX();
                        dxCal = false;
                    }


                    if ((rightEdge > event.getRawX())) {
                        if ((0 < xmin) && (xmas < xsize)) {
                            scrollMode = 3;
                        } else {
                            scrollMode = 1;
                        }
                    }
                    if ((leftEdge < event.getRawX())) {
                        if ((0 < xmin) && (xmas < xsize)) {
                            scrollMode = 3;
                        } else {
                            scrollMode = 2;
                        }
                    }
                    if ((0 > xmin) && (xmas < xsize)) {
                        Log.d("Scrollpoint", "right");
                        scrollMode = 4;
                    }
                    if ((0 < xmin) && (xmas > xsize)) {
                        Log.d("Scrollpoint", "left");
                        scrollMode = 4;
                    }


                    Log.d("Scrollpoint", scrollMode + "");

                    switch (scrollMode) {
                        case 1: {
                            float ddx = xsize - xmas;
                            matrix.postTranslate(ddx, 0);
                            mViewPager.fakeDragBy(dx);
                        }
                        break;
                        case 2: {
                            float ddx = 0 - xmin;
                            matrix.postTranslate(ddx, 0);
                            mViewPager.fakeDragBy(dx);
                        }
                        break;
                        case 3: {
                            mViewPager.fakeDragBy(dx);
                        }
                        break;
                        case 4:
                        default:
                            mViewPager.endFakeDrag();
                            matrix.postTranslate(dx, dy);
                            dxCal = true;
                            break;
                    }
                } else if (mode == ZOOM) {
                    zoomToggle = true;

                    float mdx = midStart.x - mid.x;
                    float mdy = midStart.y - mid.y;
                    Log.d("md", mid.x + "/" + mid.y);
                    Log.d("switch", "5");

                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        Log.d("switch", "6");
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);

                        Log.d("switch", "7");
                        newRot = rotation(event);
                        float r = newRot - d;

                        float ft[] = new float[9];
                        originMatrix.getValues(ft);

                        matrix.postScale(scale, scale, mid.x, mid.y);
                        matrix.postRotate(r, mid.x, mid.y);
                        matrix.postTranslate(mdx, mdy);
                        Log.d("mid", mid.x + "/" + mid.y);
                        midPoint(midStart, event, view);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        view.invalidate();

        RectF point = new RectF();
        matrix.mapRect(point);

        CustomDoubletap cd = new CustomDoubletap(view);
        gestureDetector.setOnDoubleTapListener(cd);
        gestureDetector.onTouchEvent(event);

        return true;
    }

    public int[] getRowPoint(View v, MotionEvent ev, int index) {
        final int location[] = {0, 0};
        v.getLocationOnScreen(location);

        float x = ev.getX(index);
        float y = ev.getY(index);

        double angle = Math.toDegrees(Math.atan2(y, x));
        angle += v.getRotation();

        final float length = PointF.length(x, y);

        x = (float) (length * Math.cos(Math.toRadians(angle))) + location[0];
        y = (float) (length * Math.sin(Math.toRadians(angle))) + location[1];

        return new int[]{(int) x, (int) y};
    }

    public float[] matrixEdges(ImageView iv, @Nullable Matrix matrix) {
        RectF r = new RectF(0, 0, iv.getDrawable().getIntrinsicWidth(), iv.getDrawable().getIntrinsicHeight());
        Matrix m;
        if (matrix == null) {
            m = iv.getImageMatrix();
        } else {
            m = matrix;
        }
        float out[] = new float[8];

        m.mapRect(r);
        Log.d("edge", r.right + "/" + r.bottom);

        out[0] = r.left;
        out[1] = r.top;

        out[2] = r.right;
        out[3] = r.top;

        out[4] = r.right;
        out[5] = r.bottom;

        out[6] = r.left;
        out[7] = r.bottom;

        return out;
    }

    public float madMax(float a, float b, float c, float d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    public float madMin(float a, float b, float c, float d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0f;
        }
    }

    private void midPoint(PointF point, MotionEvent event, View view) {
        int[] p = getRowPoint(view, event, 1);
        float x = event.getRawX() + p[0];
        float y = event.getRawY() + p[1];
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    class CustomDoubletap implements GestureDetector.OnDoubleTapListener {
        ImageView imageView;
        int[] loc = new int[]{0, 0};

        CustomDoubletap(View view) {
            this.imageView = (ImageView) view;
            view.getLocationOnScreen(loc);
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return true;
        }

        public boolean onDoubleTap(final MotionEvent motionEvent) {
            final Matrix matrix = imageView.getImageMatrix();
            final long startTime = System.currentTimeMillis();
            final long duration = 400;

            final float[] value1 = new float[9];
            final float[] value2 = new float[9];
            matrix.getValues(value2);
            if (zoomToggle) {
                originMatrix.getValues(value1);
                zoomToggle = false;
            } else {
                matrix.getValues(value1);
                matrix.postScale(1.0f / value1[Matrix.MSCALE_X], 1.0f / value1[Matrix.MSCALE_Y], motionEvent.getRawX(), motionEvent.getRawY());
                matrix.getValues(value1);
                matrix.setValues(value2);
                zoomToggle = true;
            }

            // calculate real scale
            float scalex = value2[Matrix.MSCALE_X];
            float skewy = value2[Matrix.MSKEW_Y];
            float mScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);

            // calculate the degree of rotation
            float mAngle = Math.round(Math.atan2(value2[Matrix.MSKEW_X], value2[Matrix.MSCALE_X]) * (180 / Math.PI));

            Log.d("rotation", mScale + "/" + mAngle);

            float origScale = (float) Math.sqrt(value1[Matrix.MSCALE_X] * value1[Matrix.MSCALE_X] + value1[Matrix.MSKEW_Y] * value1[Matrix.MSKEW_Y]);
            float origAngle = 0;

            final float diffX = value1[Matrix.MTRANS_X] - value2[Matrix.MTRANS_X];
            final float diffY = value1[Matrix.MTRANS_Y] - value2[Matrix.MTRANS_Y];
            final float diffScale = 1f - (origScale / mScale);
            final float diffAngle = origAngle - mAngle;

            Log.d("doubletap", diffX + "/" + diffY);
            Log.d("doubletap", diffScale + "/" + diffAngle);
            Log.d("doubletap", "===================");

            //matrix.postScale(diffScale, diffScale,500,500);
            //matrix.postTranslate(diffX,diffY);

            imageView.post(new Runnable() {
                @Override
                public void run() {
                    float t = (float) (System.currentTimeMillis() - startTime) / duration;
                    t = t > 1.0f ? 1.0f : t;
                    Log.d("double", t + "");
                    matrix.setValues(value2);

                    matrix.postScale(1f + (-diffScale * t), 1f + (-diffScale * t), imageView.getWidth() / 2, imageView.getHeight() / 2);
                    matrix.postRotate(-diffAngle * t, imageView.getWidth() / 2, imageView.getHeight() / 2);
                    float[] ft = new float[9];
                    matrix.getValues(ft);
                    ft[Matrix.MTRANS_X] = value2[Matrix.MTRANS_X] + (diffX * t);
                    ft[Matrix.MTRANS_Y] = value2[Matrix.MTRANS_Y] + (diffY * t);
                    matrix.setValues(ft);
                    //matrix.postTranslate(diffX*t, diffY*t);

                    imageView.setImageMatrix(matrix);
                    imageView.invalidate();
                    if (t < 1f) {
                        imageView.post(this);
                    } else {
                        matrix.setValues(value1);
                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                    }
                }
            });

            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

}