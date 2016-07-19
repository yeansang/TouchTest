package com.example.nemus.touchtest;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
    private boolean toggle = true;

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

                    pointid[0] = motionEvent.getPointerId(0);
                    pointid[1] = motionEvent.getPointerId(1);


                    int dx = (int) motionEvent.getX(pointid[1]) - (int) motionEvent.getX(pointid[0]);
                    int dy = (int) motionEvent.getY(pointid[1]) - (int) motionEvent.getY(pointid[0]);
                    double rad = Math.atan2(dx, dy);
                    double degree = (rad * 180) / Math.PI;
                    int base = (int)degree*-1;

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

    class CustomDoubletap implements GestureDetector.OnDoubleTapListener{
        View view;
        int[] loc = new int[]{0,0};

        CustomDoubletap(View view){
            this.view = view;
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
                view.animate().scaleX(2.0f).scaleY(2.0f).translationX(520-(x-loc[0])).translationY(900-(y-loc[1])).setDuration(500);
                toggle = false;
            }else{
                Log.d("doubletab",toggle+"");
                view.animate().scaleX(1.0f).scaleY(1.0f).translationX(0).translationY(0).setDuration(500);
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
