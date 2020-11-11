package com.saiga.find.messagefinder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

// feeds the pages to the ViewPager aka Onboarding activity for first time users
public class SliderAdapter extends PagerAdapter {


    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private float screenDensity;
    private final static String TAG = "FTISetup";

    public SliderAdapter(Context context){
        mContext = context;
        // fetch the user configured device density -- determine the dpi
        screenDensity = mContext.getResources().getDisplayMetrics().density;
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
        // this fixes the one of the major crashes in redmi based / low memory based phones
        // initially the images were inflated in memory based on the original image size,
        // this caused to consume more app memory at app startup for first time users
        // causing crashes making the app totally unusable
        // This new change addresses the issue by inflating the image to the size of the image view
        // based on users device screen density and image view to be displayed (dp values),
        // so that the ram memory occupied will be very much less. This is one of the recommended
        // workflow for this issue
        Log.d(TAG,"Reported Screen Density " + String.valueOf(screenDensity));
        // width of the image in pixels to be inflated in memory
        // some adjustment calculations are to be done to take in account of the error in screen
        // density calculation, this will help in sub sample size
        int requested_width = (int) (180 * (screenDensity-0.5) );
        // height and width of our circular image view is same
        int requested_height = requested_width;
        // very efficient way of loading images in memory
        mPageIcon.setImageBitmap(decodeSampledBitmapFromResource(mContext.getResources(),mDrawableIcons[position],requested_width,requested_height));
        //@deprecated
     //   mPageIcon.setImageResource(mDrawableIcons[position]);
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            // we use gpu memory instead of main memory, so this further reduce the chance of crash
            // in the memory profiler test, this method reduced the initial memory consumption by
            // ~60% (220MB reduced to 80MB)
            options.inPreferredConfig = Bitmap.Config.HARDWARE;
        }
        else {
            // this method reduced memory usage by 50% (220MB reduced to 112MB)
            // we use different bit format / representation for individual pixel
            // instead of 32 bits,  we make it to 8 bits to save memory
            // but it degrades the quality, so dither flag is set to compensate
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d(TAG,"insample size " + options.inSampleSize);
        // Decode bitmap with inSampleSize set, after configuring all options retrieve the bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of original image, got from injustdecodebounds == false
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG,"Size of the image height " + height + " width " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize(which decides the downsampling factor of image)
            // value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
