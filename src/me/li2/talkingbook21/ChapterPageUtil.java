package me.li2.talkingbook21;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

public class ChapterPageUtil {
    
    // in pixels
    private Context mAppContext;
    private int mChapterFontSize;
    private int mChapterWordSapce;
    private int mChapterLineHeight;
    private int mChapterPagePadding;
    private int mChapterPageIndicatorHeight;

    public ChapterPageUtil(Context context) {
        mAppContext = context;
        assert context!=null;
        mChapterFontSize = (int)mAppContext.getResources().getDimension(R.dimen.chapter_word_font);
        mChapterWordSapce = (int)mAppContext.getResources().getDimension(R.dimen.chapter_word_space);
        mChapterLineHeight = (int)mAppContext.getResources().getDimension(R.dimen.chapter_line_height);
        mChapterPagePadding = (int)mAppContext.getResources().getDimension(R.dimen.chapter_page_padding);
        mChapterPageIndicatorHeight = (int)mAppContext.getResources().getDimension(R.dimen.chapter_page_indicator_layout_height);
    }

    public int getStringWidth(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(mAppContext);
        textView.setTextSize(mChapterFontSize);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.width() + mChapterWordSapce;
    }
    
    public int getStringHeight(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(mAppContext);
        textView.setTextSize(mChapterFontSize);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.height();
    }
    
    public int getChapterPageWidth() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.widthPixels - mChapterPagePadding * 2;
    }
    
    public int getChapterPageHeight() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int actionBarHeight = 96;
        TypedValue tv = new TypedValue();
        if (mAppContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mAppContext.getResources().getDisplayMetrics());
        }
        return metrics.heightPixels - mChapterPageIndicatorHeight - actionBarHeight;
        
//        Display display = ((WindowManager)mAppContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size.y;
    }
    
    public int getChapterFontSize() {
        return mChapterFontSize;
    }
    
    public int getChapterWordSapce() {
        return mChapterWordSapce;
    }
    
    public int getChapterLineHeight() {
        return mChapterLineHeight;
    }

    public int getChapterPagePadding() {
        return mChapterPagePadding;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density. 
     * 
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    @SuppressWarnings("unused")
    private float convertDpToPixel(float dp){
        Resources resources = mAppContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     * 
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    @SuppressWarnings("unused")
    private int convertPixelsToDp(float px){
        Resources resources = mAppContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int)dp;
    }
}
