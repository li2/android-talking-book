package me.li2.talkingbook21.data;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;

public class ChapterInfoLab {

    private static final String FILENAME = "TalkingBook21_chapter_list.json";
    private ChapterInfoJSONSerializer mSerializer;
    
    // Singletons and Centralized data storage.
    private static ChapterInfoLab sChapterInfoLab;
    private ArrayList<ChapterInfo> mChapterInfos;
    private Context mAppContext;
    
    private ChapterInfoLab(Context appContext) {
        mAppContext = appContext;
        assert mAppContext != null;
        mSerializer = new ChapterInfoJSONSerializer(mAppContext, FILENAME);
        
        try {
            mChapterInfos = mSerializer.loadChapterInfos();
        } catch (Exception e) {
            mChapterInfos = new ArrayList<ChapterInfo>();
            e.printStackTrace();
        }
    }
    
    // Setting up the singleton
    public static ChapterInfoLab get(Context c) {
        if (sChapterInfoLab == null) {
            sChapterInfoLab = new ChapterInfoLab(c.getApplicationContext());
        }
        return sChapterInfoLab;
    }
    
    public ArrayList<ChapterInfo> getChapterInfos() {
        return mChapterInfos;
    }
    
    // so make sure the chapter name is unique.
    public ChapterInfo getChapterInfo(String chapterName) {
        for (ChapterInfo info : mChapterInfos) {
            if (info.getName().equals(chapterName)) {
                return info;
            }
        }
        return null;
    }
    
    public void addChapterInfo(ChapterInfo info) {
        mChapterInfos.add(info);
    }
    
    public void saveChapterInfos() {
        try {
            mSerializer.saveInfos(mChapterInfos);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
