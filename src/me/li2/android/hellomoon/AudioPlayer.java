package me.li2.android.hellomoon;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class AudioPlayer {

    private MediaPlayer mPlayer;
    
    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    
    public void play(Context c) {
        stop();
        
        mPlayer = MediaPlayer.create(c, R.raw.one_small_step);
        mPlayer.start();
        
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
    }
}
