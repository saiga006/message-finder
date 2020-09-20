package com.saiga.find.messagefinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

// main class which handles the onboarding screens of the app, this runs only once.
public class Onboarding extends AppCompatActivity {

    private ViewPager slideView;
    private LinearLayout dotLayout;
    private SliderAdapter pageAdapter;
    private Button prevButton;
    private Button nextButton;
    private TextView[] mDots;
    // stores the current page that user scrolled
    private int mCurrentPage;
    //colors of navigation dots
    private int[] mDotColor = {R.color.lightOrangeColor, R.color.lightBlueColor, R.color.lightPinkColor};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.OnboardingTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_onboarding);
        slideView = findViewById(R.id.slideView);
        dotLayout = findViewById(R.id.navigator_menu);
        prevButton = findViewById(R.id.PrevButton);
        nextButton = findViewById(R.id.NextButton);
        pageAdapter = new SliderAdapter(this);
        // set the slide adapter to pageviewer
        slideView.setAdapter(pageAdapter);
        // adds navigation dot to layout
        addDotsIndicator();
        // initialise screen position
        setPosition(0);
        // register page change listener
        slideView.addOnPageChangeListener(pageListener);
        // scroll forward to next page
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("button listener",String.valueOf(mCurrentPage));
                if (mCurrentPage != mDots.length-1)
                    slideView.setCurrentItem(mCurrentPage+1,true);
                else {
                    Intent launchMainActivity = new Intent().setComponent(new ComponentName(v.getContext(),MainActivity.class));
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Onboarding.this);
                    startActivity(launchMainActivity,options.toBundle());
                    getPackageManager().setComponentEnabledSetting(new ComponentName(v.getContext(),Onboarding.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                    Onboarding.this.finish();
                }
            }
        });
        // scroll back to previous page
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("prev button listener",String.valueOf(mCurrentPage));
                if(mCurrentPage != 0)
                    slideView.setCurrentItem(mCurrentPage-1);
            }
        });

    }


    // adds dots to the onboarding screen
    public void addDotsIndicator() {

        mDots = new TextView[pageAdapter.getCount()];
        for (int i = 0; i < mDots.length; i++) {

            mDots[i] = new TextView(this);
            mDots[i].setId(1000+i);
            mDots[i].setText(HtmlCompat.fromHtml("&#9679;", HtmlCompat.FROM_HTML_MODE_COMPACT));
            mDots[i].setTextColor(getColor(R.color.primaryTextColor));
            dotLayout.addView(mDots[i]);
        }
    }


    // callback invoked when current page is scrolled to new page, updates colors of dots and
     // buttons
    public void setPosition(int position) {
        Log.d("Page view position", String.valueOf(position));
        for (int i = 0; i < mDots.length; i++) {
            mDots[i].setTextColor(getColor(R.color.primaryTextColor));
        }
        mDots[position].setTextColor(getColor(mDotColor[position]));
        nextButton.setTextColor(getColor(mDotColor[position]));
    }

    ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setPosition(position);
            // save the current selected/ scrolled user position
            mCurrentPage = position;

            if (position == 0){
                // disable back button on first on boarding screen
                prevButton.setVisibility(View.INVISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            } else if (position == mDots.length-1) {
                // enable finish text on end of onboarding screen
                nextButton.setText(R.string.finish);
            } else {
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                prevButton.setText(R.string.prev);
                nextButton.setText(R.string.next);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
