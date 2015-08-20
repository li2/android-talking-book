package me.li2.talkingbook21;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import me.li2.sdcard.FileOperateUtil;
import me.li2.sdcard.SdcardUtil;
import me.li2.talkingbook21.ChapterListFragment.OnChapaterSelectedListener;
import me.li2.talkingbook21.data.ChapterInfo;
import me.li2.talkingbook21.data.ChapterInfoLab;


public class ChapterListActivity extends FragmentActivity {
    private static final String TAG = "ChapterListActivity";
    private static final String PATH_CATCHER_IN_RYE = "TalkingBook21/TheCatcherInTheRye";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        
        FragmentManager fm = getSupportFragmentManager();
        ChapterListFragment fragment = (ChapterListFragment) fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new ChapterListFragment();
            fragment.setOnChapaterSelectedListener(mOnChapaterSelectedListener);
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
        
        if (ChapterInfoLab.get(this).getChapterInfos().size() == 0) {
            if (!loadFileFromExtSD()) {
                ChapterInfo demoInfo = new ChapterInfo("Demo");
                demoInfo.setAudioUri(FileOperateUtil.getRawFileUri(this, R.raw.demo_audio));
                demoInfo.setTimingJsonUri(FileOperateUtil.getRawFileUri(this, R.raw.demo_timing));
                ChapterInfoLab.get(this).addChapterInfo(demoInfo);
              }
        }
	}
	
    private OnChapaterSelectedListener mOnChapaterSelectedListener = new OnChapaterSelectedListener() {
        @Override
        public void onChapterSelected(ChapterInfo info) {
            startFullScreenPlayerActivity(info);
        }
    };

    private void startFullScreenPlayerActivity(ChapterInfo info) {
        Intent intent = new Intent(this, FullScreenPlayerActivity.class);
        intent.putExtra(FullScreenPlayerActivity.EXTRA_CHAPTER_NAME, info.getName());
        startActivity(intent);
    }
    
    private boolean loadFileFromExtSD() {
        String sdcardPath = SdcardUtil.getExtSDCardPath();
        if (sdcardPath != null) {

            ChapterInfoLab lab = ChapterInfoLab.get(this);
            
            String folderPath = sdcardPath + "/" + PATH_CATCHER_IN_RYE;
            File folderFile = new File(folderPath);
            ArrayList<File> audioFileList = new ArrayList<File>();
            ArrayList<File> timingJsonList = new ArrayList<File>();
            
            for (File f : folderFile.listFiles()) {
                String ext = FilenameUtils.getExtension(f.getName());
                if (ext.equals("mp3") || ext.equals("wav")) {
                    audioFileList.add(f);
                } else if (ext.equals("json")) {
                    timingJsonList.add(f);
                }
                String fileNameWithoutExt = FilenameUtils.removeExtension(f.getName());
                Log.d(TAG, "File: name " + fileNameWithoutExt + ", ext " + ext);
            }
            
            for (File audioFile : audioFileList) {
                String audioFileName = FilenameUtils.removeExtension(audioFile.getName());
                for (File timingJsonFile : timingJsonList) {
                    String timingJsonName = FilenameUtils.removeExtension(timingJsonFile.getName());
                    if (audioFileName.equals(timingJsonName)) {
                        ChapterInfo info = new ChapterInfo(audioFileName);
                        info.setAudioUri(Uri.fromFile(audioFile));
                        info.setTimingJsonUri(Uri.fromFile(timingJsonFile));
                        lab.addChapterInfo(info);
                    }
                }
            }
            
            if (lab.getChapterInfos().size() > 0) {
                lab.saveChapterInfos();
                return true;
            }
        }
        return false;
    }
}
