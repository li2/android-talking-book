package me.li2.talkingbook21.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import me.li2.sdcard.FileOperateUtil;
import me.li2.talkingbook21.R;

public class TalkingBookChapter {
    private static final String TAG = "TalkingBookChapter";
    
    // Singletons and centralized data storage
    private static TalkingBookChapter sChapter;
    private ArrayList<String> mWordArray;
    private ArrayList<Integer> mTimingArray;

    private TalkingBookChapter(Context context, Uri timingJsonUri) {
        String timingJsonString = FileOperateUtil.loadExtFileToString(timingJsonUri);
        if (timingJsonString == null) {
            timingJsonString = FileOperateUtil.loadRawFileToString(context, R.raw.demo_timing); // just for demo
        }

        JSONObject jsonObj = null;
        JSONArray jsonArray = null;
        mWordArray = new ArrayList<String>();
        mTimingArray = new ArrayList<Integer>();
        
        try {
            jsonObj = new JSONObject(timingJsonString);
            if (jsonObj != null) {
                Log.d(TAG, "parse timing json file");
                jsonArray = jsonObj.getJSONArray("words");
                for (int index = 0; index < jsonArray.length(); index++) {
                    JSONArray obj = jsonArray.getJSONArray(index);
                    // Log.d(TAG, String.format("%-5.3f  %s", (double)obj.get(1), (String)obj.get(0)));
                    mWordArray.add((String)obj.get(0));
                    double seconds = (double)obj.get(1);
                    mTimingArray.add((int)(seconds*1000));
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
    
    // Setting up the singleton
    public static TalkingBookChapter get(Context context, Uri timingJsonUri) {
        if (sChapter == null) {
            sChapter = new TalkingBookChapter(context, timingJsonUri);
        }
        return sChapter;
    }
    
    public static void destroy() {
        sChapter = null;
    }
    
    public List<String> getWordList(int fromIndex, int count) {
        return mWordArray.subList(fromIndex, fromIndex+count);
    }
    
    public List<Integer> getTimingList(int fromIndex, int count) {
        return mTimingArray.subList(fromIndex, fromIndex+count);
    }
    
    public int size() {
        return mWordArray.size();
    }
}
