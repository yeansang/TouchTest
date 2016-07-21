package com.example.nemus.touchtest;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by nemus on 2016-07-19.
 */
public class ImageAdator extends PagerAdapter {

    LayoutInflater inflater;
    MainActivity main;
    private boolean zoomToggle = false;
    private boolean toggle = true;
    private float baseLength = 0;
    private int deg =0;
    private int[] center = new int[2];
    float[] originalScale = new float[]{0,0};
    float bx,by = 0;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Matrix originMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;


    public ImageAdator(LayoutInflater layoutInflater, MainActivity act){
        this.inflater = layoutInflater;
        this.main = act;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position){

        View view = inflater.inflate(R.layout.pager_imageview,null);

        final ImageView imageView = (ImageView)view.findViewById(R.id.imageview);
        imageView.setImageResource(R.drawable.image1+position);


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
            private int[] pointid = new int[2];
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ImageView view = (ImageView) v;
                matrix = view.getImageMatrix();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if(toggle){
                            float[] value = new float[9];
                            matrix.getValues(value);
                            originMatrix.setValues(value);
                            Log.e("Value1", "value X : "+value[0]+"/"+value[1]+"/"+value[2]);
                            Log.e("Value1", "value Y : "+value[3]+"/"+value[4]+"/"+value[5]);
                            Log.e("Value1", "value per : "+value[6]+"/"+value[7]+"/"+value[8]);
                            toggle = false;
                        }
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
                            midPoint(mid, event);
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
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("switch","3");
                        mode = NONE;
                        lastEvent = null;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        zoomToggle = true;
                        if (mode == DRAG) {
                            Log.d("switch","4");
                            matrix.set(savedMatrix);
                            float dx = event.getX() - start.x;
                            float dy = event.getY() - start.y;
                            matrix.postTranslate(dx, dy);
                        } else if (mode == ZOOM) {
                            Log.d("switch","5");
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                Log.d("switch","6");
                                matrix.set(savedMatrix);
                                float scale = (newDist / oldDist);
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                            if (lastEvent != null && event.getPointerCount() == 2) {
                                Log.d("switch","7");
                                newRot = rotation(event);
                                float r = newRot - d;
                                float[] values = new float[9];
                                matrix.getValues(values);
                                float tx = values[2];
                                float ty = values[5];
                                float sx = values[0];
                                float xc = (view.getWidth() / 2) * sx;
                                float yc = (view.getHeight() / 2) * sx;
                                matrix.postRotate(r, tx + xc, ty + yc);
                            }
                        }
                        break;
                }

                view.setImageMatrix(matrix);
                view.invalidate();

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
            private void midPoint(PointF point, MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
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
        switch (pos){
            case 0:
                return "아이유";
            case 1:
                return "야경";
            case 2:
                return "긴목청둥오리";
            case 3:
                return "long 1";
            case 4:
                return "long 2";
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

    public int[] getWindowSize(){
        Display dp = main.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        dp.getSize(p);
        return new int[]{p.x,p.y};
    }

    public int[] getRealImageSize(ImageView imageView){
        Matrix matrix = imageView.getImageMatrix();
        final Drawable d = imageView.getDrawable();
        final float[] ft = new float[9];
        matrix.getValues(ft);
        final int actW = Math.round(d.getIntrinsicWidth() * ft[Matrix.MSCALE_X]);
        final int actH = Math.round(d.getIntrinsicHeight() * ft[Matrix.MSCALE_Y]);
        return new int[]{actW,actH};
    }

    public Matrix matrixAni(Matrix from, Matrix to, float timeFlow){
        float[] value1 = new float[9];
        float[] value2 = new float[9];
        to.getValues(value1);
        from.getValues(value2);

        float[] value = new float[9];

        for(int i=0;i<9;i++){
            value[i] = value1[i]-value2[i];
        }
        float[] ft = new float[9];

        for (int i = 0; i < 9; i++) {
            ft[i] = value2[i] + value[i] * timeFlow;
        }
        Matrix out = new Matrix();
        out.setValues(ft);

        return out;
    }

    class CustomDoubletap implements GestureDetector.OnDoubleTapListener{
        ImageView imageView;
        int[] loc = new int[]{0,0};

        CustomDoubletap(View view){
            this.imageView = (ImageView)view;
            view.getLocationOnScreen(loc);
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            main.toggle();
            return true;
        }

        @Override
        public boolean onDoubleTap(final MotionEvent motionEvent) {
            final Matrix matrix = imageView.getImageMatrix();
            final long startTime = System.currentTimeMillis();
            final long duration = 1000;

            final float[] value1 = new float[9];
            final float[] value2 = new float[9];
            originMatrix.getValues(value1);
            matrix.getValues(value2);

            final float[] value = new float[9];

            for(int i=0;i<9;i++){
                value[i] = value1[i]-value2[i];
            }

            if(zoomToggle) {
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        float t = (float) (System.currentTimeMillis() - startTime) / duration;
                        t = t > 1.0f ? 1.0f : t;
                        Log.d("double", t + "");
                        float[] ft = new float[9];

                        for (int i = 0; i < 9; i++) {
                            ft[i] = value2[i] + value[i] * t;
                        }

                        Log.e("Value", "value X : " + value[0] + "/" + value[1] + "/" + value[2]);
                        Log.e("Value", "value Y : " + value[3] + "/" + value[4] + "/" + value[5]);
                        Log.e("Value", "value per : " + value[6] + "/" + value[7] + "/" + value[8]);

                        matrix.setValues(ft);

                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                        if (t < 1f) {
                            imageView.post(this);
                            zoomToggle = false;
                        }
                    }
                });
            }else{
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        float t = (float) (System.currentTimeMillis() - startTime) / duration;
                        t = t > 1.0f ? 1.0f : t;
                        Log.d("double", t + "");

                        Log.d("zoom", zoomToggle + "");
                        float ft = 1.0f - value2[0];
                        int[] size = getWindowSize();
                        matrix.setScale(t, t, motionEvent.getRawX(), motionEvent.getRawY());

                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                        if (t < 1f) {
                            imageView.post(this);
                            zoomToggle =true;
                        }
                    }
                });
            }

            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }
}
