package com.example.nemus.touchtest;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

/**
 * Created by nemus on 2016-07-19.
 */
public class ImageAdator extends PagerAdapter {

    LayoutInflater inflater;
    MainActivity main;
    private boolean zoomToggle = false;
    private boolean toggle = true;

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
                            Log.d("dd",dx+"/"+dy);
                        } else if (mode == ZOOM) {

                            float[] values = new float[9];
                            matrix.getValues(values);
                            float mdx = midStart.x - mid.x;
                            float mdy = midStart.y - mid.y;
                            float sx = values[0];
                            /*values[Matrix.MTRANS_X] += mdx*sx*10;
                            values[Matrix.MTRANS_Y] += mdy*sx*10;*/
                            Log.e("Value1", "value X : "+values[0]+"/"+values[1]+"/"+values[2]);
                            Log.e("Value1", "value Y : "+values[3]+"/"+values[4]+"/"+values[5]);
                            Log.e("Value1", "value per : "+values[6]+"/"+values[7]+"/"+values[8]);
                            Log.d("md",mid.x+"/"+mid.y);
                            //matrix.postTranslate(mdx,mdy);

                            Log.d("switch","5");

                            //
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                Log.d("switch","6");
                                matrix.set(savedMatrix);
                                float scale = (newDist / oldDist);

                                Log.d("switch","7");
                                newRot = rotation(event);
                                float r = newRot - d;
                                matrix.getValues(values);
                                float tx = values[2];
                                float ty = values[5];
                                //float sx = values[0];
                                float xc = (view.getWidth() / 2) * sx;
                                float yc = (view.getHeight() / 2) * sx;
                                int[] is = getRealImageSize(imageView);

                                matrix.postRotate(r, mid.x, mid.y);
                                matrix.postScale(scale, scale,mid.x, mid.y);
                                matrix.postTranslate(mdx,mdy);
                                Log.d("mid",mid.x+"/"+mid.y);
                                Log.d("center", tx+"/"+ty);
                                midPoint(midStart,event, view);
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
        switch (pos){
            case 0:
                return "sample color";
            case 1:
                return "6";
            case 2:
                return "42";
            case 3:
                return "12";
            case 4:
                return "오리";
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

    public Matrix matrixAni(float[] from,float[] to, float timeFlow){
        float[] value1 = to;
        float[] value2 = from;

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
            final long duration = 500;

            final float[] value1 = new float[9];
            final float[] value2 = new float[9];
            matrix.getValues(value2);
            if(zoomToggle) {
                originMatrix.getValues(value1);
                zoomToggle =false;
            }else{
                float rx = motionEvent.getRawX();
                float ry = motionEvent.getRawY();
                float mx = imageView.getWidth()/2;
                float my = imageView.getHeight()/2;
                matrix.setScale(1.0f,1.0f,mx,my);
                float tx = mx>rx ? mx + (value2[Matrix.MTRANS_X]-(mx-rx)):(value2[Matrix.MTRANS_X]-(rx-mx))-mx;
                float ty = my>ry ? my - (value2[Matrix.MTRANS_Y]-(my-ry)):(value2[Matrix.MTRANS_Y]-(ry-my))-my;
                matrix.postTranslate(tx,ty);
                matrix.getValues(value1);
                zoomToggle = true;

                //value1[Matrix.MTRANS_X] = ;
                //value1[Matrix.MTRANS_Y] = ry-(value2[Matrix.MTRANS_Y]-ry)-((value2[Matrix.MTRANS_Y]-ry)*(1.0f-value2[Matrix.MSCALE_Y]));
                /*value1[0] = 1.0f;
                value1[5] = 1.0f;
                float fx = ((value2[Matrix.MTRANS_X]-motionEvent.getRawX())*(1.0f-value2[Matrix.MSCALE_X]));
                float fy = (value2[Matrix.MTRANS_Y]-motionEvent.getRawY())*(1.0f-value2[Matrix.MSCALE_Y]);
                value1[Matrix.MTRANS_X] = fx;
                value1[Matrix.MTRANS_Y] = fy;*/
            }


            final float[] value = new float[9];

            for(int i=0;i<9;i++){
                value[i] = value1[i]-value2[i];
            }

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

                        Log.e("Value", "value X : " + ft[0] + "/" + ft[1] + "/" + ft[2]);
                        Log.e("Value", "value Y : " + ft[3] + "/" + ft[4] + "/" + ft[5]);
                        Log.e("Value", "value per : " + ft[6] + "/" + ft[7] + "/" + ft[8]);

                        matrix.setValues(ft);

                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                        if (t < 1f) {
                            imageView.post(this);
                            //zoomToggle = !zoomToggle;
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
