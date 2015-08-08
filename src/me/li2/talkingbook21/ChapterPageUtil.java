package me.li2.talkingbook21;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class ChapterPageUtil {
    
    public static int DEFAULT_FONT_SIZE = 16;
    public static int DEFAULT_WORD_SPACE = 8;
    public static int DEFAULT_LINE_SPACE = 4;
    public static int DEFAULT_PAGE_PADDING = 16;
    
    private Context mAppContext;

    public ChapterPageUtil(Context context) {
        mAppContext = context;
        assert context!=null;
    }

    public int getStringWidth(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(mAppContext);
        textView.setTextSize(DEFAULT_FONT_SIZE);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.width() + DEFAULT_WORD_SPACE;
    }
    
    public int getStringHeight(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(mAppContext);
        textView.setTextSize(DEFAULT_FONT_SIZE);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.height();
    }
    
    public int getPageWidth() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.widthPixels;
    }
    
    public int getPageHeight() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.heightPixels;
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size.y;
    }
    
    public int getLineHeight() {
        return 64;
    }
}
