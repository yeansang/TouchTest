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
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Created by nemus on 2016-07-19.
 */
public class ImageAdator extends PagerAdapter {

    LayoutInflater inflater;
    MainActivity main;
    private boolean zoomToggle = true;
    private boolean toggle = true;
    private float baseLength = 0;
    private int deg =0;
    float[] originalScale = new float[]{0,0};

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
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    deg =0;
                    toggle=true;
                    baseLength=0;
                }

                Log.d("point",motionEvent.getPointerCount()+"");

                if(motionEvent.getPointerCount() == 2) {

                    int raw[] = getRowPoint(view,motionEvent,1);
                    int dx = (int) motionEvent.getRawX() - raw[0];
                    int dy = (int) motionEvent.getRawY() - raw[1];
                    Log.d("y", (int) motionEvent.getY(0) +"/"+(int) motionEvent.getY(1));

                    double rad = Math.atan2(dy, dx);
                    double degree = Math.toDegrees(rad);
                    int base = (int)degree-deg;
                    deg = (int)degree;
                    ImageView iv = (ImageView)view;

                    Matrix matrix = iv.getImageMatrix();
                    float[] value = new float[9];

                    if(toggle){
                        baseLength = (float)Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
                        toggle=false;
                    }
                    float vLength = (float)Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
                    float sc = vLength/baseLength;
                    Log.d("scale",sc+"");

                    matrix.postRotate(base, view.getWidth()/2,view.getHeight()/2);
                    matrix.postScale(sc,sc,view.getWidth()/2,view.getHeight()/2);

                    matrix.getValues(value);



                    iv.setImageMatrix(matrix);
                    iv.invalidate();
                    //int deg = base - (int)degree;
                    //view.setRotation(base);


                    Log.d("pointdeg", base+"");
                    Log.d("deg",imageView.getRotation()+"");
                    Log.e("Value", "value X : "+value[0]+"/"+value[1]+"/"+value[2]);
                    Log.e("Value", "value Y : "+value[3]+"/"+value[4]+"/"+value[5]);
                    Log.e("Value", "value per : "+value[6]+"/"+value[7]+"/"+value[8]);
                }

                CustomDoubletap cd = new CustomDoubletap(view);
                gestureDetector.setOnDoubleTapListener(cd);
                gestureDetector.onTouchEvent(motionEvent);

                Log.d("touch",motionEvent.getX()+"/"+motionEvent.getY());

                return true;
            }
        });

        container.addView(view);
        return view;
    }


    @Override
    public CharSequence getPageTitle(int pos){

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
            final float[] ft = new float[9];

            matrix.getValues(ft);

            final int[] act = getRealImageSize(imageView);

            final float targetX = 1 - ft[0];
            final float targetY = 1 - ft[4];

            final int actW = act[0];
            final int actH = act[1];


            imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        float t = (float) (System.currentTimeMillis() - startTime) / duration;
                        t = t > 1.0f ? 1.0f : t;
                        Log.d("double", t + "");

                        int[] size = getWindowSize();

                        Log.d("drawx", targetX * t + "");
                        Log.d("drawy", targetY * t + "");
                        /*if(zoomToggle){
                            (targetX * t)
                            (targetY * t)
                        }*/

                        matrix.postRotate(0);
                        matrix.postScale(0.4f,0.4f);
                        /*matrix.setScale(ft[0]+(targetX * t) , ft[4]+(targetY * t),motionEvent.getRawX()-(actW/2),motionEvent.getRawY()-(actH/2));
                        matrix.postTranslate(motionEvent.getRawX()-(actW/2),motionEvent.getRawY()-(actH/2));*/
                        //matrix.setTranslate(motionEvent.getRawX()-(actW/2),motionEvent.getRawY()-(actH/2));

                        Log.d("width,height",ft[2]+"/"+ft[5]);
                        imageView.setImageMatrix(matrix);
                        imageView.invalidate();
                        if (t < 1f) {
                            imageView.post(this);
                            zoomToggle = !zoomToggle;
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
