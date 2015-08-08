package me.li2.talkingbook21;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class ChapterPageAdapter extends FragmentPagerAdapter {

    private Context mAppContext;
    private Uri mJsonUri;
    private ArrayList<Integer> mPageBeginningWordIndexList;
    
    public ChapterPageAdapter(Context context, FragmentManager fm, Uri jsonUri) {
        super(fm);
        mAppContext = context;
        assert context != null;
        mJsonUri = jsonUri;
        mPageBeginningWordIndexList = new ArrayList<Integer>();
        splitChapterToPages(mJsonUri);
    }

    @Override
    public Fragment getItem(int poisition) {
        int fromIndex = mPageBeginningWordIndexList.get(poisition);
        int count = mPageBeginningWordIndexList.get(poisition+1) - fromIndex;
        return ChapterPageFragment.newInstance(mJsonUri, fromIndex, count);
    }

    @Override
    public int getCount() {
        return mPageBeginningWordIndexList.size()-1;
    }
    
    private void splitChapterToPages(Uri jsonUri) {
        TalkingBookChapter chapter = TalkingBookChapter.get(jsonUri);
        int size = chapter.size();
        List<String> wordList =  chapter.getWordList(0, size);
        int beginningWordIndex = 0;
        int pageMaxWords = 0;
        for (int index=0; index<size; index++) {
            mPageBeginningWordIndexList.add(beginningWordIndex);
            pageMaxWords = totalWordsCanDisplayOnOnePage(wordList, beginningWordIndex);
            if (pageMaxWords == -1) {
                mPageBeginningWordIndexList.add(size);
                break;
            }
            beginningWordIndex += pageMaxWords;
        }
    }
    
    // The total number of words can display on a given page (with known width and height).
    private int totalWordsCanDisplayOnOnePage(List<String> words, int startIndex) {
        ChapterPageUtil pageUtil = new ChapterPageUtil(mAppContext);
        int pageWidth = pageUtil.getPageWidth();
        int pageHeight = pageUtil.getPageHeight();
        int remainingWidth = pageWidth;
        int lineHeight = pageUtil.getLineHeight();
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
