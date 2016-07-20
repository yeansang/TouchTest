package com.example.nemus.touchtest;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    private boolean toggle = true;
    private boolean toggle2 = true;

    public ImageAdator(LayoutInflater layoutInflater, MainActivity act){
        this.inflater = layoutInflater;
        this.main = act;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position){

        View view = inflater.inflate(R.layout.pager_imageview,null);

        ImageView imageView = (ImageView)view.findViewById(R.id.imageview);
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
                //
                Log.d("point",motionEvent.getPointerCount()+"");

                if(motionEvent.getPointerCount() == 2) {
                    int raw[] = getRowPoint(view,motionEvent,1);
                    int dx = (int) motionEvent.getRawX() - raw[0];
                    int dy = (int) motionEvent.getRawY() - raw[1];
                    Log.d("y", (int) motionEvent.getY(0) +"/"+(int) motionEvent.getY(1));

                    double rad = Math.atan2(dy, dx);
                    double degree = Math.toDegrees(rad);
                    int base = (int)degree;

                    //int deg = base - (int)degree;
                    view.setRotation(base);

                    Log.d("pointdeg", base+"");
                }
                CustomDoubletap cd = new CustomDoubletap(view);
                gestureDetector.setOnDoubleTapListener(cd);
                gestureDetector.onTouchEvent(motionEvent);
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

    class ZoomAnim extends Animation {
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(500);         // 지속시간
            setInterpolator(new LinearInterpolator());    // 일정하게
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            Matrix matrix = t.getMatrix();
            //matrix.postScale(0.4f*interpolatedTime,0.4f*interpolatedTime,545.0f,912.0f);
            matrix.setScale(2.0f*interpolatedTime,2.0f*interpolatedTime,545.0f,912.0f);
            //matrix.setSkew(2.0f * interpolatedTime, 0);    // 기울기 * 시간
        }
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
        public boolean onDoubleTap(MotionEvent motionEvent) {
            Log.d("doubletab","tab");
            float x = motionEvent.getX();
            float y = motionEvent.getY();

            if(toggle) {
                Log.d("doubletab",toggle+"");
                Log.d("x",x-loc[0]+"");
                Log.d("y",y-loc[1]+"");
                imageView.animate().scaleXBy(2.0f).scaleYBy(2.0f).setDuration(1000);
                /*Matrix im = imageView.getImageMatrix();
                im.postScale(5.0f,5.0f,motionEvent.getRawX(),motionEvent.getRawY());
                imageView.setImageMatrix(im);
                imageView.invalidate();*/
                //imageView.startAnimation(new ZoomAnim());
                toggle = false;
            }else{
                Log.d("doubletab",toggle+"");
                //imageView.startAnimation(new ZoomAnim());
                toggle = true;
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }
}
