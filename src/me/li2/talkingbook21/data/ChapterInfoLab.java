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
        
        // 由于音频文件占据近 300M 的空间，所以只在 res/raw 中包含了一个音频文件，用于 demo。
        // App 启动后会检查 sdcard 指定路径下是否有音频文件，如果有，则遍历该路径下的所有文件，并把路径写入 JSON 文件中，
        // 这是 ChapterListActivity.onCreate() 做的事情:
        // if (ChapterInfoLab.get(this).getChapterInfos().size() == 0) {
        //    ChapterInfoLab.get(this).addChapterInfo(demoInfo);
        
        // 而 ChapterInfoJSONSerializer 是一个工具类，正是用于JSON文件的读写。
        // 所以 App 的逻辑是，先检查 Sdcard 指定路径中是否包含音频文件，
        // 如果不包含，显示 res/raw 中的 demo
        // 如果包含，则遍历所有文件，并保存在 FILENAME 这个 json 文件中。
        // 此后 app 就会从该 json 文件中读取文件信息，并构建章节列表的 list 界面。
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
