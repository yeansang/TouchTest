package com.example.nemus.touchtest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import Jama.SingularValueDecomposition;

/**
 * Created by nemus on 2016-07-19.
 */
public class ImageAdator extends PagerAdapter {

    LayoutInflater inflater;
    MainActivity main;
    private boolean zoomToggle = false;
    private boolean toggle = true;
    public boolean right = false;
    boolean touch = false;
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
    private float[] lastEvent = null;
    private Bitmap inview;
    private boolean inside = false;
    boolean pager = false;
    float startDrag=0;
    float scrollDiffStart = 0;
    RectF rf = new RectF();
    float rightEdge=0;
    float leftEdge=0;



    public ImageAdator(LayoutInflater layoutInflater, MainActivity act, @Nullable Bitmap imageView){
        this.inflater = layoutInflater;
        this.main = act;
        if(imageView != null) {
            this.inview = imageView;
            inside = true;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position){
        Log.d("incount",position+"");
        View view = inflater.inflate(R.layout.pager_imageview,null);

        final ImageView imageView = (ImageView)view.findViewById(R.id.imageview);
        Log.d("tag",position+""+view.toString());
        view.setTag(position);
        if(inside){
            imageView.setImageBitmap(inview);
        }else {
            imageView.setImageResource(R.drawable.image1 + position);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(main.getApplicationContext(), new GestureDetector.OnGestureListener() {
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
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ImageView view = (ImageView) v;
                matrix = view.getImageMatrix();
                Log.d("pager on",pager+"");

                float[] values = new float[9];
                matrix.getValues(values);

                float[] edge = matrixEdges(view, null);

                Log.d("action", event.getAction()+"");
                main.mViewPager.beginFakeDrag();

                float xmas = madMax(edge[0],edge[2],edge[4],edge[6]);
                float xmin = madMin(edge[0],edge[2],edge[4],edge[6]);
                float xsize = view.getWidth();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if(toggle){
                            float[] value = new float[9];
                            matrix.getValues(value);
                            originMatrix.setValues(value);
                            toggle = false;
                        }

                        rightEdge = event.getRawX() - (xmas - xsize);
                        leftEdge = event.getRawX() + (-xmin);

                        Log.d("Scrollpoint",rightEdge+"/"+leftEdge);

                        Log.d("switch","1");
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        lastEvent = null;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.d("switch","2");
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid,event,view);
                            midPoint(midStart,event, view);
                            mode = ZOOM;
                        }
                        lastEvent = new float[4];
                        lastEvent[0] = event.getX(0);
                        lastEvent[1] = event.getX(1);
                        lastEvent[2] = event.getY(0);
                        lastEvent[3] = event.getY(1);
                        d = rotation(event);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mode = NONE;
                        lastEvent = null;
                        main.mViewPager.endFakeDrag();
                        touch = false;
                        right = false;
                        dxCal = true;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("switch","3");
                        mode = NONE;
                        main.mViewPager.endFakeDrag();
                        lastEvent = null;
                        touch = false;
                        right = false;
                        dxCal = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //zoomToggle = true;
                        float[] value = new float[9];
                        matrix.getValues(value);
                        if (mode == DRAG) {

                            Log.d("switch","4");
                            //matrix.set(savedMatrix);

                            float dx = event.getRawX() - start.x;
                            start.x = event.getRawX();
                            float dy = event.getY() - start.y;
                            start.y = event.getY();

                            int scrollMode = 0;

                            if (dxCal) {
                                startDrag = event.getX();
                                scrollDiffStart = event.getRawX();
                                dxCal = false;
                            }
                            Log.d("Scrollpo",event.getRawX()+"/"+rightEdge);

                            if((rightEdge<event.getRawX())&&(event.getRawX()<leftEdge)){
                                scrollMode = 0;
                            }else{
                                if((rightEdge>event.getRawX())){
                                    float ddx = xsize - xmas;
                                    matrix.postTranslate(ddx,0);
                                }
                                if((leftEdge<event.getRawX())){
                                    float ddx = 0 - xmin;
                                    matrix.postTranslate(ddx,0);
                                }
                                scrollMode = 1;
                            }

                            switch(scrollMode){
                                case 1: {
                                    Log.d("Scrollpoint", "1");

                                    //matrix.postTranslate(-dx, dy);
                                    float ft[] = new float[9];
                                    matrix.getValues(ft);

                                    float drx = event.getX() - startDrag;
                                    //startDrag = event.getRawX();

                                    //Log.d("scrollmax", ddx + "ddx");
                                    Log.d("scrollmax", drx + "drx");
                                    //dx = dx + ddx;
                                    main.mViewPager.fakeDragBy(dx);
                                }
                                    break;

                                case 2: {
                                    Log.d("Scrollpoint", "3");

                                    //matrix.postTranslate(-dx, dy);
                                    float ft[] = new float[9];
                                    matrix.getValues(ft);

                                    float drx = event.getX() - startDrag;
                                    //startDrag = event.getRawX();

                                    //Log.d("scrollmax", ddx + "ddx");
                                    Log.d("scrollmax", drx + "drx");
                                    //dx = dx + ddx;
                                    main.mViewPager.fakeDragBy(dx);
                                }
                                    break;

                                case 3:
                                case 4:
                                default:
                                    main.mViewPager.endFakeDrag();
                                    matrix.postTranslate(dx, dy);
                                    dxCal = true;
                                    break;
                            }


                        } else if (mode == ZOOM) {
                            zoomToggle = true;

                            float mdx = midStart.x - mid.x;
                            float mdy = midStart.y - mid.y;
                            Log.d("md",mid.x+"/"+mid.y);
                            Log.d("switch","5");

                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                Log.d("switch","6");
                                matrix.set(savedMatrix);
                                float scale = (newDist / oldDist);

                                Log.d("switch","7");
                                newRot = rotation(event);
                                float r = newRot - d;

                                float ft[] = new float[9];
                                originMatrix.getValues(ft);

                                matrix.postScale(scale, scale,mid.x, mid.y);
                                matrix.postRotate(r, mid.x, mid.y);
                                matrix.postTranslate(mdx,mdy);
                                Log.d("mid",mid.x+"/"+mid.y);
                                midPoint(midStart,event, view);
                            }

                        }
                        break;
                }

                view.setImageMatrix(matrix);
                view.invalidate();

                Log.e("Value1", "value X : "+values[0]+"/"+values[1]+"/"+values[2]);
                Log.e("Value1", "value Y : "+values[3]+"/"+values[4]+"/"+values[5]);
                Log.e("Value1", "value per : "+values[6]+"/"+values[7]+"/"+values[8]);

                RectF point = new RectF();
                matrix.mapRect(point);

                Log.d("point", point.top+"/"+point.left);
                Log.d("point", point.bottom+"/"+point.right);
                /*Log.d("point", point[2]+"/"+point[3]);
                Log.d("point", point[4]+"/"+point[5]);
                Log.d("point", point[6]+"/"+point[7]);*/

                CustomDoubletap cd = new CustomDoubletap(view);
                gestureDetector.setOnDoubleTapListener(cd);
                gestureDetector.onTouchEvent(event);

                Log.d("touch",event.getX()+"/"+event.getY());

                return true;
            }

            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float)Math.sqrt(x * x + y * y);
            }

            /**
             * Calculate the mid point of the first two fingers
             */
            private void midPoint(PointF point, MotionEvent event, View view) {
                int[] p = getRowPoint(view, event, 1);
                float x = event.getRawX() + p[0];
                float y = event.getRawY() + p[1];
                point.set(x / 2, y / 2);
            }

            /**
             * Calculate the degree to be rotated by.
             *
             * @param event
             * @return Degrees
             */
            private float rotation(MotionEvent event) {
                double delta_x = (event.getX(0) - event.getX(1));
                double delta_y = (event.getY(0) - event.getY(1));
                double radians = Math.atan2(delta_y, delta_x);
                return (float) Math.toDegrees(radians);
            }
        });


        container.addView(view);
        return view;
    }


    @Override
    public CharSequence getPageTitle(int pos){
        toggle = true;
        if(inside) return "image";
        switch (pos){
            case 0:
                return "sample color";
            case 1:
                return "6";
            case 2:
                return "42";
            case 3:
                return "고추밭";
            case 4:
                return "고추밭";
            default:
                return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int pos, Object obj){
        container.removeView((View)obj);
    }

    @Override
    public int getCount() {
        if(inside) return 1;
        return 5;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    public int[] getRowPoint(View v,MotionEvent ev, int index){
        final int location[] = { 0, 0 };
        v.getLocationOnScreen(location);

        float x=ev.getX(index);
        float y=ev.getY(index);

        double angle=Math.toDegrees(Math.atan2(y, x));
        angle+=v.getRotation();

        final float length=PointF.length(x,y);

        x=(float)(length*Math.cos(Math.toRadians(angle)))+location[0];
        y=(float)(length*Math.sin(Math.toRadians(angle)))+location[1];

        return new int[]{(int)x,(int)y};
    }

    public float[] matrixEdges(ImageView iv, @Nullable Matrix matrix){
        RectF r = new RectF(0,0,iv.getDrawable().getIntrinsicWidth(),iv.getDrawable().getIntrinsicHeight());
        Matrix m;
        if(matrix == null){
            m = iv.getImageMatrix();
        }else {
            m = matrix;
        }
        float out[] = new float[8];
        float mat[] = new float[9];

        m.mapRect(r);
        Log.d("edge", r.right+"/"+r.bottom);

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

    public float madMax(float a, float b, float c, float d){
        return Math.max(Math.max(a,b),Math.max(c,d));
    }

    public float madMin(float a, float b, float c, float d){
        return Math.min(Math.min(a,b),Math.min(c,d));
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
            main.toggle();
            return true;
        }

        public Matrix castMatrix(Jama.Matrix m){
            double[][] arr = m.getArray();
            float[] out = new float[]{
                    (float)arr[0][0], (float)arr[0][1], (float)arr[0][2],
                    (float)arr[1][0], (float)arr[1][1], (float)arr[1][2],
                    (float)arr[2][0], (float)arr[2][1], (float)arr[2][2]
            };
            Matrix outm = new Matrix();
            outm.setValues(out);
            return outm;
        }

        public Jama.Matrix castMatrix(Matrix m){
            float[] arr = new float[9];
            m.getValues(arr);
            double[][] out = new double[][]{
                    {(double)arr[0], (double)arr[1], (double)arr[2]},
                    {(double)arr[3], (double)arr[4], (double)arr[5]},
                    {(double)arr[6], (double)arr[7], (double)arr[8]}
            };
            Jama.Matrix outm = new Jama.Matrix(out,3,3);
            return outm;
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
            final float diffScale = 1f-(origScale/mScale);
            final float diffAngle = origAngle - mAngle;

            Log.d("doubletap", diffX + "/" + diffY);
            Log.d("doubletap", diffScale + "/" + diffAngle);
            Log.d("doubletap", "===================");
            Jama.Matrix jmat = new Jama.Matrix(new double[][]{
                    {value1[0], value1[1], value1[2]},
                    {value1[3], value1[4], value1[5]},
                    {value1[6], value1[7], value1[8]}
            });

            SingularValueDecomposition svd = jmat.svd();

            Jama.Matrix v = svd.getV();
            double sd[][] = v.getArray();
            for (int i = 0; i < 3; i++) {
                Log.d("matS", sd[i][0] + "/" + sd[i][1] + "/" + sd[i][2]);
            }
            Log.d("matS", "==================");

            //matrix.postScale(diffScale, diffScale,500,500);
            //matrix.postTranslate(diffX,diffY);


            imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        float t = (float) (System.currentTimeMillis() - startTime) / duration;
                        t = t > 1.0f ? 1.0f : t;
                        Log.d("double", t + "");
                        matrix.setValues(value2);

                        matrix.postScale(1f+(-diffScale*t), 1f+(-diffScale*t),imageView.getWidth()/2,imageView.getHeight()/2);
                        matrix.postRotate(-diffAngle*t,imageView.getWidth()/2,imageView.getHeight()/2);
                        float[] ft = new float[9];
                        matrix.getValues(ft);
                        ft[Matrix.MTRANS_X] = value2[Matrix.MTRANS_X] + (diffX*t);
                        ft[Matrix.MTRANS_Y] = value2[Matrix.MTRANS_Y] + (diffY*t);
                        matrix.setValues(ft);
                        //matrix.postTranslate(diffX*t, diffY*t);

                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                        if (t < 1f) {
                            imageView.post(this);
                        }else{
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
