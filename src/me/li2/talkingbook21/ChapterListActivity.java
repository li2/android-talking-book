package me.li2.talkingbook21;

import java.io.File;
import java.util.HashSet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import me.li2.sdcard.SdcardUtil;

public class ChapterListActivity extends FragmentActivity {
    private static final String TAG = "ChapterListActivity";
    private Uri mAudioUri;
    private Uri mTimingJsonUri;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String sdcardPath = SdcardUtil.getExtSDCardPath();
        
        if (sdcardPath != null) {
            String timingJsonFileName = "chapter1.json.out.json";
            String audioFileName = "chapter1.mp3";
            String folderPath = sdcardPath + "/" + "TalkingBook21/";
            String timingJsonFilePath = folderPath + timingJsonFileName;
            String audioFilePath = folderPath + audioFileName;
            mTimingJsonUri = Uri.fromFile(new File(timingJsonFilePath));
            mAudioUri = Uri.fromFile(new File(audioFilePath));
            Log.d(TAG, "File Path: " + mTimingJsonUri.toString() + ", " + mAudioUri);
        }
        
        if (mTimingJsonUri != null && mAudioUri != null) {
            startFullScreenPlayerActivity();
        }
	}
	
    private void startFullScreenPlayerActivity() {
        Intent intent = new Intent(this, FullScreenPlayerActivity.class);
        intent.putExtra(FullScreenPlayerActivity.EXTRA_AUDIO_PATH, mAudioUri.toString());
        intent.putExtra(FullScreenPlayerActivity.EXTRA_LRC_PATH, mTimingJsonUri.toString());
        startActivity(intent);
    }
}
