package me.li2.talkingbook21.data;

import android.net.Uri;

public class ChapterInfo {

    private String mName;
    private int mDuration;
    private int mLastPosition;
    private int mAcculumatedTime;
    private Uri mAudioUri;
    private Uri mTimingJsonUri;
    
    public ChapterInfo(String name) {
        mName = name;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public int getDuration() {
        return mDuration;
    }
    
    public void setDuration(int duration) {
        mDuration = duration;
    }
    
    public int getLastPosition() {
        return mLastPosition;
    }
    
    public void setLastPosition(int lastPosition) {
        mLastPosition = lastPosition;
    }
    
    public int getAcculumatedTime() {
        return mAcculumatedTime;
    }
    
    public void setAcculumatedTime(int acculumatedTime) {
        mAcculumatedTime = acculumatedTime;
    }
    
    public Uri getAudioUri() {
        return mAudioUri;
    }
    
    public void setAudioUri(Uri audioUri) {
        mAudioUri = audioUri;
    }
    
    public Uri getTimingJsonUri() {
        return mTimingJsonUri;
    }
    
    public void setTimingJsonUri(Uri timingJsonUri) {
        mTimingJsonUri = timingJsonUri;
    }
    
}
