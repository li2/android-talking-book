package me.li2.talkingbook21.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;


public class ChapterInfo {

    private static final String JSON_NAME = "name";
    private static final String JSON_DURATION = "duration";
    private static final String JSON_LAST_POSITION = "last_position";
    private static final String JSON_ACCULUMATED_TIME = "acculumated_time";
    private static final String JSON_AUDIO_URI = "audio_uri";
    private static final String JSON_TIMING_JSON_URI = "timing_json_uri";

    private String mName;
    private int mDuration;
    private int mLastPosition;
    private int mAcculumatedTime;
    private Uri mAudioUri;
    private Uri mTimingJsonUri;
    
    public ChapterInfo(String name) {
        mName = name;
    }

    // Constructor that accepts a JSONObject.
    public ChapterInfo(JSONObject json) throws JSONException {
        if (json.has(JSON_NAME)) {
            mName = json.getString(JSON_NAME);
        }
        
        if (json.has(JSON_DURATION)) {
            mDuration = json.getInt(JSON_DURATION);
        }
        
        if (json.has(JSON_LAST_POSITION)) {
            mLastPosition = json.getInt(JSON_LAST_POSITION);
        }
        
        if (json.has(JSON_ACCULUMATED_TIME)) {
            mAcculumatedTime = json.getInt(JSON_ACCULUMATED_TIME);
        }
        
        if (json.has(JSON_AUDIO_URI)) {
            mAudioUri = Uri.parse(json.getString(JSON_AUDIO_URI));
        }
        
        if (json.has(JSON_TIMING_JSON_URI)) {
            mTimingJsonUri = Uri.parse(json.getString(JSON_TIMING_JSON_URI));
        }
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_NAME, mName);
        json.put(JSON_DURATION, mDuration);
        json.put(JSON_LAST_POSITION, mLastPosition);
        json.put(JSON_ACCULUMATED_TIME, mAcculumatedTime);
        json.put(JSON_AUDIO_URI, mAudioUri.toString());
        json.put(JSON_TIMING_JSON_URI, mTimingJsonUri.toString());
        return json;
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
