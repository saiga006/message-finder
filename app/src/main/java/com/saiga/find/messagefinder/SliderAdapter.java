package com.saiga.find.messagefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {


    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public SliderAdapter(Context context){
        mContext = context;
    }

    public int[] mDrawableIcons = {
            R.drawable.message2,
            R.drawable.priority_notifications,
            R.drawable.privacy
    };

    public int[] mPageTitle = {
            R.string.OnboardingTitle1,
            R.string.OnboardingTitle2,
            R.string.OnboardingTitle3
    };

    public int[] mPageDescs = {
            R.string.OnboardingDesc1,
            R.string.OnboardingDesc2,
            R.string.OnboardingDesc3
    };

    public int[] mPageTitleColor = {
            R.color.lightOrangeColor,
            R.color.lightBlueColor,
            R.color.lightPinkColor
    };

    public int[] mPageDescColor = {
            R.color.lightOrangeColor,
            R.color.lightBlueColor,
            R.color.lightPinkColor
    };

    @Override
    public int getCount() {
        // to return the number of pages
        return mPageTitle.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        // to be checked
        return view == (RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // inflates our pages for the view pager, for each onboarding screen
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.page_layout,container, false);
        ImageView mPageIcon = view.findViewById(R.id.boarding_image);
        TextView mPageCaption = view.findViewById(R.id.title_textView);
        TextView mPageSummary = view.findViewById(R.id.summaryView);

        mPageIcon.setImageResource(mDrawableIcons[position]);
        mPageCaption.setTextColor(mContext.getColor(mPageTitleColor[position]));
        mPageSummary.setTextColor(mContext.getColor(mPageDescColor[position]));
        mPageCaption.setText(mPageTitle[position]);
        mPageSummary.setText(mPageDescs[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // marks the end of the onboarding screen
        container.removeView((RelativeLayout)object);
    }
}
