package me.li2.talkingbook21.data;

import java.util.ArrayList;

import android.content.Context;
import me.li2.sdcard.FileOperateUtil;
import me.li2.talkingbook21.R;

public class ChapterInfoLab {

    // Singletons and Centralized data storage.
    private static ChapterInfoLab sChapterInfoLab;
    private ArrayList<ChapterInfo> mChapterInfos;
    private Context mAppContext;
    
    private ChapterInfoLab(Context appContext) {
        mAppContext = appContext;
        assert mAppContext != null;
        mChapterInfos = new ArrayList<ChapterInfo>();
        
        ChapterInfo demoInfo = new ChapterInfo("Demo");
        demoInfo.setAudioUri(FileOperateUtil.getRawFileUri(appContext, R.raw.c1_audio));
        demoInfo.setTimingJsonUri(FileOperateUtil.getRawFileUri(appContext, R.raw.c1_timing));
        mChapterInfos.add(demoInfo);
        
//        String sdcardPath = SdcardUtil.getExtSDCardPath();
//        Uri audioUri;
//        Uri timingJsonUri;
//        if (sdcardPath != null) {
//            String timingJsonFileName = "chapter1.json.out.json";
//            String audioFileName = "chapter1.mp3";
//            String folderPath = sdcardPath + "/" + "TalkingBook21/";
//            String timingJsonFilePath = folderPath + timingJsonFileName;
//            String audioFilePath = folderPath + audioFileName;
//            File audioFile = new File(audioFilePath);
//            if (audioFile.exists()) {
//                audioUri = Uri.fromFile(audioFile);
//            }
//            File timingJsonFile = new File(timingJsonFilePath);
//            if (timingJsonFile.exists()) {
//                timingJsonUri = Uri.fromFile(timingJsonFile);
//            }
//        }
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
}
