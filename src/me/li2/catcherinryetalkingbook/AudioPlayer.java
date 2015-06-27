package me.li2.catcherinryetalkingbook;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class AudioPlayer {
    private final static String TAG = "AudioPlayer";
    private MediaPlayer mPlayer;
    
    public void play(Context c, String httpPath) {
        stop();
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(httpPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void play(Context c, Uri fileUri) {
        stop();
        
        // Create a MediaPlayer instance.
        mPlayer = MediaPlayer.create(c, fileUri);
        mPlayer.start();
    }
    
    public void start() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }
    
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }
    
    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }
    
    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return 0;
    }
    
    // Gets the current playback position. @return the current position in milliseconds.
    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    public void seekToPosition(int msec) {
        if (mPlayer != null){
            mPlayer.seekTo(msec);
        }
    }
    
    public void setLooping(boolean looping) {
        if (mPlayer != null) {
            mPlayer.setLooping(looping);
        }
    }
}
