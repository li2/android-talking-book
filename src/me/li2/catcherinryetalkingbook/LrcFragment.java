package me.li2.catcherinryetalkingbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LrcFragment extends ListFragment {
    
    public static final String EXTRA_LRC_FILE_NAME = "me.li2.catcherinryetalkingbook.LrcFragment.lrcFileName";
    
    private final static String TAG = "LrcFragment";
    private final static int WRITE_FILE_TIME_INTERVAL = 1000; // 1s
    private final static int LRC_FONT_SIZE = 16;
    private final static int LRC_TIMESTAMP_LENGTH = 10; // [00:00.00]
    // \\[ transfer [ in bracket expression as literal [
    // \\d to indicate a digit
    // X{n} X exactly n times
    private final static String LRC_TIMESTAMP_REG_EXP = "\\[\\d{2}:\\d{2}.\\d{2}\\].*";
    
    private static int sSelectedRow = -1;
    private LrcAdapter mLrcAdapter;
    private ArrayList<String> mLrcArray;
    private String mLrcPath;
    private String mTimestampStr;
    private Callbacks mCallbacks;
    private Handler mHandler;

    public interface Callbacks {
        void onLrcItemSelected(int lrcRow);
    }
    
    // Create a fragment instance.
    public static LrcFragment newInstance(String fileName) {
        Bundle args = new Bundle();
        args.putString(EXTRA_LRC_FILE_NAME, fileName);
        
        LrcFragment fragment = new LrcFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get lrc name from bundle.
        String lrcFileName = getArguments().getString(EXTRA_LRC_FILE_NAME);
        // Load lrc from external sdcard or app assets file.
        mLrcPath = buildExtFilePathWithName(lrcFileName);
        File lrcFile = new File(mLrcPath);
        if (lrcFile.exists()) {
            try {
                mLrcArray = loadLrcFromExtFile(mLrcPath);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "ERROR loading lrc from ext file: ", e);
            }
        } else {
            mLrcArray = loadLrcFromAssetsFile(lrcFileName);
        }
                
        // Set listfragment adapter datasource.
        mLrcAdapter = new LrcAdapter(mLrcArray);
        setListAdapter(mLrcAdapter);

        //
        mHandler = new Handler();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
//        listView.setDivider(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_light)));
        listView.setDividerHeight(1);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.smokeWhite));
        return view;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        sSelectedRow = position;
        mLrcAdapter.notifyDataSetChanged();
        mCallbacks.onLrcItemSelected(position);
    }
    
    // Setter method, when timestamp changed, update view and save to file.
    public void setTimestampStr(String timestampStr) {
        mTimestampStr = timestampStr;
        Log.d(TAG, "setTimestampStr " + mTimestampStr);

        // Attach timestamp to header of selected lrc.
        String selectedLrc = mLrcAdapter.getItem(sSelectedRow);
        if (selectedLrc.length() >= LRC_TIMESTAMP_LENGTH && selectedLrc.matches(LRC_TIMESTAMP_REG_EXP)) {
            // already has timestamp, modify.
            selectedLrc = timestampStr + selectedLrc.subSequence(LRC_TIMESTAMP_LENGTH, selectedLrc.length());
        } else {
            // no timestamp, add.
            selectedLrc = timestampStr + selectedLrc;
        }
        mLrcArray.set(sSelectedRow, selectedLrc);
        
        // Then notify lrc adapter that its data changed
        mLrcAdapter.notifyDataSetChanged();
        
        // Lastly, write to file
        mHandler.removeCallbacks(mWriteToFileRunnable);
        mHandler.postDelayed(mWriteToFileRunnable, WRITE_FILE_TIME_INTERVAL);
    }

    private Runnable mWriteToFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                saveLrcToExtFile(mLrcPath, mLrcArray);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    };
    
    private class LrcAdapter extends ArrayAdapter<String> {
        public LrcAdapter(List<String> objects) {
            super(getActivity(), android.R.layout.simple_list_item_1, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;
            textView.setTextSize(LRC_FONT_SIZE);
            
            String item = getItem(position);
            boolean hasTimestamp = item.matches(LRC_TIMESTAMP_REG_EXP);
            
            if (hasTimestamp) {
                String part1 = item.substring(0, LRC_TIMESTAMP_LENGTH);
                String part2 = item.substring(LRC_TIMESTAMP_LENGTH, item.length());
                textView.setText(buildAttributedString(part1, part2));
            } else {
                textView.setText(item);
            }
            
            if (sSelectedRow == position) {
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                if(hasTimestamp) {
                    textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    textView.setBackgroundColor(getResources().getColor(R.color.smokeWhite));
                }
            }
            
            return view;
        }
    }
    
    private CharSequence buildAttributedString(String str1, String str2) {
        SpannableString ss1 = new SpannableString(str1);
        ss1.setSpan(new RelativeSizeSpan(1.5f), 0, str1.length(), 0);
        ss1.setSpan(getResources().getColor(android.R.color.holo_blue_dark), 0, str1.length(), 0);

        SpannableString ss2 = new SpannableString(str2);
        ss2.setSpan(new RelativeSizeSpan(1f), 0, str2.length(), 0);

        return TextUtils.concat(ss1, ss2);
    }
    
    // Read line from file, and use these lines to build an array
    private ArrayList<String> loadLrcFromAssetsFile(String fileName){
        try {            
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            ArrayList<String>result = new ArrayList<String>();
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals("")) {
                    continue;
                }
                result.add(line);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String buildExtFilePathWithName(String fileName) {
        String extSDCardPath = getActivity().getExternalFilesDir(null).getAbsolutePath();
        String filePath = extSDCardPath + "/" + fileName + ".txt";
        return filePath;
    }
    
    private ArrayList<String> loadLrcFromExtFile(String filePath) throws IOException, JSONException {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            File file = new File(filePath);
            reader = new BufferedReader(new FileReader(file));
            
            String line = null;           
            while ((line = reader.readLine()) != null) {
                if(line.trim().equals("")) {
                    continue;
                }
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            // Ignore this one, it happens when starting fresh.
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return result;
    }
    
    private void saveLrcToExtFile(String filePath, ArrayList<String> lrcArray) throws JSONException, IOException {
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            writer = new BufferedWriter(new FileWriter(file));
            String result = new String();
            for (String line : lrcArray) {
                result += line + "\r\n";
            }
            writer.write(result);
        } catch (FileNotFoundException e) {
        } finally {
            if (writer != null) {
                Log.e(TAG, filePath + "not found!");
                writer.close();
            }
        }
    }    
}
