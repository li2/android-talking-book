package me.li2.talkingbook21;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class LrcFragment extends ListFragment {
    
    public static final String EXTRA_LRC_URI = "me.li2.talkingbook21.LrcFragment.lrcUri";

    private final static String TAG = "LrcFragment";
    private final static int LRC_FONT_SIZE = 16;
    
    private static int sSelectedRow = -1;
    private LrcAdapter mLrcAdapter;
    private List<String> mLrcArray;
    private List<Integer> mTimingArray;
    private Callbacks mCallbacks;
    private TalkingBookChapter mCharacter;

    public interface Callbacks {
        void onLrcItemSelected(int msec);
    }
    
    // Create a fragment instance.
    public static LrcFragment newInstance(Uri timingJsonUri) {
        Bundle args = new Bundle();
        args.putString(EXTRA_LRC_URI, timingJsonUri.toString());
        
        LrcFragment fragment = new LrcFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get timing Json file uri from bundle.
        Uri timingJsonUri = Uri.parse(getArguments().getString(EXTRA_LRC_URI));
        mCharacter = TalkingBookChapter.get(timingJsonUri);
        mLrcArray = mCharacter.getWordList(0, mCharacter.size());
        mTimingArray = mCharacter.getTimingList(0, mCharacter.size());
                
        // Set listfragment adapter datasource.
        mLrcAdapter = new LrcAdapter(mLrcArray);
        setListAdapter(mLrcAdapter);
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
        mCallbacks.onLrcItemSelected(mTimingArray.get(position));
    }

    // 
    public void seekLrcToTime(int msec) {
        if (mTimingArray == null || mTimingArray.size() <= 0) {
            return;
        }
        sSelectedRow = findNearestTiming(msec);
        mLrcAdapter.notifyDataSetChanged();
//        getListView().setSmoothScrollbarEnabled(true);
//        getListView().smoothScrollToPosition(sSelectedRow);
        
        if (getListView().getLastVisiblePosition() < sSelectedRow) {
          getListView().setSelection(sSelectedRow);
        }
    }
    
    private int findNearestTiming(int msec) {
        int nearestDiff = mTimingArray.get(0);
        int nearestIndex = 0;
        for (int index = 0; index < mTimingArray.size(); index++) {
            int diff = Math.abs(mTimingArray.get(index) - msec);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestIndex = index;
            }
        }
        return nearestIndex;
    }
    

    private class LrcAdapter extends ArrayAdapter<String> {
        public LrcAdapter(List<String> objects) {
            super(getActivity(), android.R.layout.simple_list_item_1, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;
            textView.setTextSize(LRC_FONT_SIZE);
            textView.setText(getItem(position));
            
            if (sSelectedRow == position) {
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                textView.setBackgroundColor(getResources().getColor(R.color.smokeWhite));
            }
            return view;
        }
    }
}
