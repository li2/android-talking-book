package me.li2.talkingbook21;

import java.util.List;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class ChapterPageFragment extends Fragment implements OnClickListener {
    private static final String TAG = "ChapterPageFragment";
    private static final String EXTRA_TIMING_JSON_URI = "me.li2.talkingbook21.ChapterPageFragment.timing_json_uri";
    private static final String EXTRA_FROM_INDEX = "me.li2.talkingbook21.ChapterPageFragment.from_index";
    private static final String EXTRA_COUNT = "me.li2.talkingbook21.ChapterPageFragment.count";

    private static int DEFAULT_FONT_SIZE = 16;
    private static int DEFAULT_WORD_SPACE = 8;
    private static int DEFAULT_LINE_SPACE = 4;
    private static int DEFAULT_PAGE_PADDING = 16;
    
    private Uri mJsonUri;
    private int mFromIndex;
    private int mCount;
    
    private TalkingBookChapter mChapter;
    private List<String> mWordList;
    private List<Integer> mTimingList;
    
    private TextView lastReadingWord;
    private OnWordClickListener mOnWordClickListener;
    
    public void setOnWordClickListener(OnWordClickListener l) {
        mOnWordClickListener = l;
    }
    
    public interface OnWordClickListener {
        public void onWordClick(int msec);
    }
    
    // Create fragment instance
    public static ChapterPageFragment newInstance(Uri jsonUri, int fromIndex, int count) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TIMING_JSON_URI, jsonUri.toString());
        args.putInt(EXTRA_FROM_INDEX, fromIndex);
        args.putInt(EXTRA_COUNT, count);
        
        ChapterPageFragment fragment = new ChapterPageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJsonUri = Uri.parse(getArguments().getString(EXTRA_TIMING_JSON_URI));
        mFromIndex = getArguments().getInt(EXTRA_FROM_INDEX);
        mCount = getArguments().getInt(EXTRA_COUNT);
        mChapter = TalkingBookChapter.get(mJsonUri);
        mWordList = mChapter.getWordList(mFromIndex, mCount);
        mTimingList = mChapter.getTimingList(mFromIndex, mCount);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_page, container, false);
        
        LinearLayout pageLayout = (LinearLayout) view.findViewById(R.id.rootLayout);
        LinearLayout lineLayout = new LinearLayout(getActivity());
        lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 64));
        pageLayout.addView(lineLayout);
        int count = 1;
        
        int pageWidth = getPageWidth();
        int pageHeight = getPageHeight();
        int lineHeight = getLineHeight();
        int remainingWidth = pageWidth;
        int remainingHeight = pageHeight - lineHeight;
        
        int N = mCount;
        
        for (int i = 0; i < N; i++) {
            // create a new TextView
            String word = mWordList.get(i);
            int timing = mTimingList.get(i);
            TextView wordTextView = createTextView(word, timing);
            int wordWidth = getStringWidth(word);
            if (wordWidth > remainingWidth) {
                lineLayout = new LinearLayout(getActivity());
                count++;
                lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 64));
                
                if (remainingHeight <= getLineHeight()) {
                    break;
                }
                pageLayout.addView(lineLayout);
                remainingWidth = pageWidth;
                remainingHeight -= lineHeight;
            }
            // add the TextView to the LinearLayout
            lineLayout.addView(wordTextView);
            remainingWidth -= wordWidth;
            Log.d(TAG, String.format("line%d word%d %s wordwidth %d, remaining width %d, remaining height %d", count, i, word, wordWidth, remainingWidth, remainingHeight));
        }
        
        return view;
    }
    
    
    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            int msec = (int)v.getTag();
            if (mOnWordClickListener != null) {
                mOnWordClickListener.onWordClick(msec);
            }
        }
    }
    
    public void seekLrcToTime(int msec) {
        View page = getView();
        if (mTimingList == null || mTimingList.size() <= 0 || page == null) {
            return;
        }
        if (lastReadingWord != null) {
            lastReadingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            lastReadingWord = null;
        }
        int tag = findReadingWord(msec);
        TextView readingWord = (TextView) page.findViewWithTag(tag);
        if (readingWord != null) {
            readingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
            lastReadingWord = readingWord;
        }
    }
    
    private int findReadingWord(int msec) {
        int nearestDiff = mTimingList.get(0);
        int nearestTag = mTimingList.get(0);
        int count = mTimingList.size();
        for (int index = 0; index < count; index++) {
            int nextTiming = mTimingList.get(index);
            int diff = Math.abs(nextTiming - msec);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestTag = nextTiming;
            }
        }
        return nearestTag;
    }
    
    private TextView createTextView(String word, int timing) {
        TextView aword = new TextView(getActivity());
        aword.setText(word);
        aword.setPadding(0, 0, DEFAULT_WORD_SPACE, 0);
        aword.setTextSize(DEFAULT_FONT_SIZE);
        aword.setTag(timing);
        aword.setOnClickListener(this);
        return aword;
    }
    
    private int getStringWidth(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(getActivity());
        textView.setTextSize(DEFAULT_FONT_SIZE);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.width() + DEFAULT_WORD_SPACE;
    }
    
    private int getStringHeight(String content) {
        Rect bounds = new Rect();
        TextView textView = new TextView(getActivity());
        textView.setTextSize(DEFAULT_FONT_SIZE);
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(content, 0, content.length(), bounds);
        return bounds.height();
    }
    
    
    private int getPageWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
    
    private int getPageHeight() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
    
    private int getLineHeight() {
        return 64;
    }
}
