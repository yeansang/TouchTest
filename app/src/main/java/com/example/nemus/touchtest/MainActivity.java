package com.example.nemus.touchtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    /*Button nomalButton;
    Button rgButton;
    Button gButton;*/
    Button bButton;
    TextView mUpView;
    MyViewPager mViewPager;
    private ImageAdator pa;
    private ImageView imageView;


    float[] mat1 = new float[] //red-out
            {
                    0.2f, 0.5f, 0.3f, 0, 0,
                    0.2f, 0.5f, 0.3f, 0, 0,
                    0f, 0f, 1f, 0, 0,
                    0f, 0f, 0f, 1, 0};

    float[] mat2 = new float[] //green-out
            {
                    0.5f, 0.5f, 0, 0, 0,
                    0.5f, 0.5f, 0f, 0, 0,
                    0f, 0f, 1f, 0, 0,
                    0f, 0f, 0f, 1, 0};

    float[] mat3 = new float[] //fix
            {
                    0.6f, 0.2f, 0.2f, 0f, 0,
                    0f, 1f, 0, 0, 0,
                    0.2f, 0.2f, 0.6f, 0, 0,
                    0f, 0f, 0f, 1.2f, 0};

    float[] mat4 = new float[] //normal
            {
                    1f, 0f, 0f, 0, 0,
                    0f, 1f, 0f, 0f, 0,
                    0f, 0f, 1f, 0f, 0,
                    0f, 0f, 0f, 1, 0};

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;

        mUpView = (TextView) findViewById(R.id.titleText);
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        //rgButton = (Button)findViewById(R.id.button3);
        //nomalButton = (Button)findViewById(R.id.button4);
        bButton = (Button)findViewById(R.id.button);
        //gButton = (Button)findViewById(R.id.button2);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            pa = new ImageAdator(getLayoutInflater(), this, handleSendImage(intent));
        }else{
            pa = new ImageAdator(getLayoutInflater(), this, null);
        }

        mUpView.setText(pa.getPageTitle(0));

        mViewPager.setAdapter(pa);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int beforePos = 0;
            int currentPos = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPos = position;
                Log.d("roll", position+"/"+positionOffset+"/"+positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int pos) {
                beforePos = pos;
                mUpView.setText(pa.getPageTitle(pos));
                if((pos==0)) {
                    PicView pv = (PicView) mViewPager.findViewWithTag(pos+1);
                    if(pv!=null) pv.setOrigin();
                }else if((pos==(pa.getCount()-1))){
                    PicView pv = (PicView) mViewPager.findViewWithTag(pos-1);
                    if(pv!=null) pv.setOrigin();
                }else{
                    PicView pv = (PicView) mViewPager.findViewWithTag(pos-1);
                    if(pv!=null) pv.setOrigin();
                    pv = (PicView) mViewPager.findViewWithTag(pos+1);
                    if(pv!=null) pv.setOrigin();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });



        /*nomalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = (ImageView)mViewPager.findViewWithTag(mViewPager.getCurrentItem());
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(mat4);
                imageView.setColorFilter(cf);
            }
        });

        rgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = (ImageView)mViewPager.findViewWithTag(mViewPager.getCurrentItem());
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(mat1);
                imageView.setColorFilter(cf);
            }
        });

        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = (ImageView)mViewPager.findViewWithTag(mViewPager.getCurrentItem());
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(mat2);
                imageView.setColorFilter(cf);
            }
        });*/

        bButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = (ImageView)mViewPager.findViewWithTag(mViewPager.getCurrentItem());
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(mat3);
                imageView.setColorFilter(cf);
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);

        mUpView.animate().alpha(0.0f).setDuration(500);

    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mVisible = true;

        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

        mUpView.setVisibility(View.VISIBLE);
        mUpView.animate().alpha(0.5f).setDuration(500);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private Bitmap handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Bitmap bm;
        if (imageUri != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                return bm;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
