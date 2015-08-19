package me.li2.talkingbook21;

import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
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
    
    private Uri mJsonUri;
    private int mFromIndex;
    private int mCount;
    
    private TalkingBookChapter mChapter;
    private List<String> mWordList;
    private List<Integer> mTimingList;
    
    private TextView lastReadingWord;
    private OnWordClickListener mOnWordClickListener;
    private ChapterPageUtil mChapterPageUtil;
    
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
        mChapter = TalkingBookChapter.get(getActivity(), mJsonUri);
        mWordList = mChapter.getWordList(mFromIndex, mCount);
        mTimingList = mChapter.getTimingList(mFromIndex, mCount);
        mChapterPageUtil = new ChapterPageUtil(getActivity());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_page, container, false);
        
        ChapterPageUtil pageUtil = new ChapterPageUtil(getActivity());
        LinearLayout pageLayout = (LinearLayout) view.findViewById(R.id.rootLayout);
        @SuppressWarnings("unused")
        int count = 1;
        int pageWidth = pageUtil.getChapterPageWidth();
        int pageHeight = pageUtil.getChapterPageHeight();
        int lineHeight = pageUtil.getChapterLineHeight();
        int remainingWidth = pageWidth;
        int remainingHeight = pageHeight - lineHeight;
        
        LinearLayout lineLayout = new LinearLayout(getActivity());
        lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, lineHeight));
        pageLayout.addView(lineLayout);

        for (int i = 0; i < mCount; i++) {
            // create a new TextView
            String word = mWordList.get(i);
            int timing = mTimingList.get(i);
            TextView wordTextView = createTextView(word, timing);
            int wordWidth;
            if (word.equals("\n")) {
                // occupy the whole line if is a new line break.
                wordWidth = pageWidth;
            } else {
                wordWidth = pageUtil.getStringWidth(word);
            }
        
            if (wordWidth > remainingWidth) {
                lineLayout = new LinearLayout(getActivity());
                count++;
                lineLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, lineHeight)); // in pixels
                
                if (remainingHeight <= lineHeight) {
                    break;
                }
                pageLayout.addView(lineLayout);
                remainingWidth = pageWidth;
                remainingHeight -= lineHeight;
            }
            // add the TextView to the LinearLayout
            lineLayout.addView(wordTextView);
            remainingWidth -= wordWidth;
            // Log.d(TAG, String.format("line%d word%d %s wordwidth %d, remaining width %d, remaining height %d", count, i, word, wordWidth, remainingWidth, remainingHeight));
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
    
    // This method called to update view in order to highlight the reading word.
    public void seekChapterToTime(int msec) {
        View page = getView();
        if (mTimingList == null || mTimingList.size() <= 0 || page == null) {
            Log.d(TAG, "not ready to seek chapter to time");
            return;
        }
        if (lastReadingWord != null) {
            lastReadingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.chapter_page_background));
            lastReadingWord = null;
        }
        int tag = findReadingWord(msec);
        TextView readingWord = (TextView) page.findViewWithTag(tag);
        if (readingWord != null) {
            readingWord.setBackgroundColor(getActivity().getResources().getColor(R.color.chapter_selected_highlight));
            lastReadingWord = readingWord;
        }
    }
    
    // Find the word base on given time, since the tag of word TextView is set to time.
    private int findReadingWord(int msec) {
        if (msec < mTimingList.get(0) || msec > mTimingList.get(mTimingList.size()-1)) {
            // Log.d(TAG, "cannot find reading word " + msec + " from " + mTimingList.get(0) + " to " + mTimingList.get(mTimingList.size()-1));
            return -1;
        }
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
    
    /*
    Fix mixed units problem of method setTextSize(float size), the given size interpreted as "scaled pixel".
    XML Attributes android:textSize available units are: px (pixels), dp (density-independent pixels), sp (scaled pixels based on preferred font size), in (inches), mm (millimeters).
    My Honor3C return 18 when call getDimension(R.dimen.9sp), LG-G3 return 36. The unit is px!
    So we should call setTextSize(TypedValue.COMPLEX_UNIT_PX, size).
    http://stackoverflow.com/a/5032433/2722270
    http://developer.android.com/reference/android/widget/TextView.html#setTextSize(int, float)
    */
    private TextView createTextView(String word, int timing) {
        TextView aword = new TextView(getActivity());
        aword.setText(word);
        aword.setTextSize(TypedValue.COMPLEX_UNIT_PX, mChapterPageUtil.getChapterFontSize());
        int width;
        if (word.equals("\n")) {
            width = LayoutParams.MATCH_PARENT;
        } else {
            width = mChapterPageUtil.getStringWidth(word);
        }
        aword.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));
        aword.setTag(timing);
        aword.setOnClickListener(this);
        return aword;
    }    
}
