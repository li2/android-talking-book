package me.li2.audioplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class AudioPlayerController implements MediaPlayer.OnCompletionListener  {

    public enum PlaybackState {
        PLAYBACK_STATE_NONE,
        PLAYBACK_STATE_STOPPED,  // State indicating this item is currently stopped.
        PLAYBACK_STATE_PAUSED,   // State indicating this item is currently paused.
        PLAYBACK_STATE_PLAYING,  // State indicating this item is currently playing.
    }
    
    public static interface Callbacks {
        /**
         * Override to handle changes in playback state.
         *
         * @param state The new playback state of the session
         */
        void onPlaybackStateChanged(PlaybackState state);
        
        /**
         * Override to handle changes to the current audio data.
         */
        void onAudioDataChanged(int duration);
    }
    
    public void registerCallback(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void unregisterCallback() {
        mCallbacks = null;
    }
    
    @SuppressWarnings("unused")
    private final static String TAG = "AudioPlayerController";
    private MediaPlayer mPlayer;
    private Callbacks mCallbacks;
    private PlaybackState mPlaybackState;
    private Uri mFileUri;
    private Context mContext;
    
    public void play(Context context, Uri fileUri) {
        mContext = context;
        mFileUri = fileUri;
        
        stop();

        // Create a MediaPlayer instance.
        mPlayer = MediaPlayer.create(context, fileUri);
        mPlayer.start();
        mPlayer.setOnCompletionListener(this);

        setPlaybackState(PlaybackState.PLAYBACK_STATE_PLAYING);
        mCallbacks.onAudioDataChanged(getDuration());
    }
    
    public void play() {
        if (mPlayer != null) {
            mPlayer.start();
        } else {
            play(mContext, mFileUri);
        }
        setPlaybackState(PlaybackState.PLAYBACK_STATE_PLAYING);
    }
    
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
            setPlaybackState(PlaybackState.PLAYBACK_STATE_PAUSED);
        }
    }
    
    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            setPlaybackState(PlaybackState.PLAYBACK_STATE_STOPPED);
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }
    
    // Return duration in milliseconds. 
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
    
    private void setPlaybackState(PlaybackState playbackState) {
        mPlaybackState = playbackState;
        mCallbacks.onPlaybackStateChanged(playbackState);
    }

    public PlaybackState getPlaybackState() {
        return mPlaybackState;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null && !mPlayer.isLooping()) {
            stop();
        }
    }    
}
