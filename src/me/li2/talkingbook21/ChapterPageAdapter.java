package me.li2.talkingbook21;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import me.li2.talkingbook21.ChapterPageFragment.OnWordClickListener;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class ChapterPageAdapter extends FragmentPagerAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ChapterPageAdapter";
    private Context mAppContext;
    private Uri mJsonUri;
    private ArrayList<Integer> mPageBeginningWordIndexList;
    private ArrayList<Integer> mPageBeginningWordTimmingList;
    private int mSeekingTime;

    private OnChapterPageWordClickListener mOnChapterPageWordClickListener;
    
    public void setOnChapterPageWordClickListener(OnChapterPageWordClickListener l) {
        mOnChapterPageWordClickListener = l;
    }
    
    public interface OnChapterPageWordClickListener {
        // This method will be called when user click a word on fragment.
        public void onChapterPageWordClick(int msec);
    }
    
    public ChapterPageAdapter(Context context, FragmentManager fm, Uri jsonUri) {
        super(fm);
        mAppContext = context;
        assert context != null;
        mJsonUri = jsonUri;
        mPageBeginningWordIndexList = new ArrayList<Integer>();
        mPageBeginningWordTimmingList = new ArrayList<Integer>();
        splitChapterToPages(mJsonUri);
    }
    
    @Override
    public int getCount() {
        return mPageBeginningWordIndexList.size()-1;
    }
    
    @Override
    public Fragment getItem(int poisition) {
        int fromIndex = mPageBeginningWordIndexList.get(poisition);
        int count = mPageBeginningWordIndexList.get(poisition+1) - fromIndex;
        ChapterPageFragment fragment = ChapterPageFragment.newInstance(mJsonUri, fromIndex, count);
        fragment.setOnWordClickListener(mOnWordClickListener);
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof ChapterPageFragment) {
            ((ChapterPageFragment) object).seekChapterToTime(mSeekingTime);
        }
        return super.getItemPosition(object);
    }
    
    // This method will be called to notify fragment to update view in order to highlight the reading word.
    public void seekChapterToTime(int msec) {
        mSeekingTime = msec;
        notifyDataSetChanged();
    }

    // Get time of beginning word on given page.
    public int getPageTiming(int position) {
        return mPageBeginningWordTimmingList.get(position);
    }
    
    // Calculate the current page base on given time. 
    public int getReadingPage(int msec) {
        ArrayList<Integer> timeList = mPageBeginningWordTimmingList;
        for (int i=0; i<timeList.size()-1; i++) {
            int pageStartTime = timeList.get(i);
            int pageEndTime = timeList.get(i+1);
            if (msec >= pageStartTime && msec < pageEndTime) {
                // Log.d(TAG, "li21 get reading page " + i + " [" + pageStartTime + ", " + pageEndTime + "]");
                return i;
            }
        }
        
        return -1;
    }
    
    // This callback will be called when user click a word on fragment.
    private OnWordClickListener mOnWordClickListener = new OnWordClickListener() {
        @Override
        public void onWordClick(int msec) {
            if (mOnChapterPageWordClickListener != null) {
                mOnChapterPageWordClickListener.onChapterPageWordClick(msec);
            }
        }
    };
    
    // INIT data model.
    private void splitChapterToPages(Uri jsonUri) {
        TalkingBookChapter chapter = TalkingBookChapter.get(mAppContext, jsonUri);
        int size = chapter.size();
        List<String> wordList =  chapter.getWordList(0, size);
        List<Integer> timingList = chapter.getTimingList(0, size);
        int beginningWordIndex = 0;
        int pageMaxWords = 0;
        for (int index=0; index<size; index++) {
            mPageBeginningWordIndexList.add(beginningWordIndex);
            mPageBeginningWordTimmingList.add(timingList.get(beginningWordIndex));
            pageMaxWords = totalWordsCanDisplayOnOnePage(wordList, beginningWordIndex);
            if (pageMaxWords == -1) {
                // end of file, these two list size = ViewPager.count + 1.
                mPageBeginningWordIndexList.add(size);
                mPageBeginningWordTimmingList.add(timingList.get(size-1));
                break;
            }
            beginningWordIndex += pageMaxWords;
        }
    }
    
    // The total number of words can display on a given page (with known width and height).
    private int totalWordsCanDisplayOnOnePage(List<String> words, int startIndex) {
        ChapterPageUtil pageUtil = new ChapterPageUtil(mAppContext);
        int pageWidth = pageUtil.getChapterPageWidth();
        int pageHeight = pageUtil.getChapterPageHeight();
        int remainingWidth = pageWidth;
        int lineHeight = pageUtil.getChapterLineHeight();
        int remainingHeight = pageHeight - lineHeight;
        
        int i = startIndex;
        for (; i<words.size(); i++) {
            String word = words.get(i);
            int wordWidth = pageUtil.getStringWidth(word);
            if (wordWidth > remainingWidth) {
                if (lineHeight >= remainingHeight) {
                    break;
                }
                // next line
                remainingWidth = pageWidth;
                remainingHeight -= lineHeight;
            }
            // next word
            remainingWidth -= wordWidth;
        }
        if (i == words.size()) {
            // end of words list.
            return -1;
        }
        
        return (i - startIndex);
    }
}
