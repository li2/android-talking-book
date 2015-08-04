package me.li2.talkingbook21;

import java.io.File;

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
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                mAudioUri = Uri.fromFile(audioFile);
            }
            File timingJsonFile = new File(timingJsonFilePath);
            if (timingJsonFile.exists()) {
                mTimingJsonUri = Uri.fromFile(timingJsonFile);
            }
            Log.d(TAG, "File Path: " + mAudioUri + ", " + mTimingJsonUri);
        }
        
        // for debug
        if (mAudioUri == null) {
            mAudioUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.c1_audio);
        }
        if (mTimingJsonUri == null) {
            mTimingJsonUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.c1_timing);
        }
        
        // start full screen activity when get audio and lrc.
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
