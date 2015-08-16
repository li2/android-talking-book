package me.li2.talkingbook21;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import me.li2.talkingbook21.data.ChapterInfo;
import me.li2.talkingbook21.data.ChapterInfoLab;

public class ChapterListFragment extends ListFragment {

    private ArrayList<ChapterInfo> mChapterInfos;
    private OnChapaterSelectedListener mOnChapaterSelectedListener;
    
    // Required interface for hosting activities.
    public void setOnChapaterSelectedListener(OnChapaterSelectedListener l) {
        mOnChapaterSelectedListener = l;
    }
    
    public interface OnChapaterSelectedListener {
        void onChapterSelected(ChapterInfo info);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChapterInfos = ChapterInfoLab.get(getActivity()).getChapterInfos();
        setListAdapter(new ChapterInfoAdapter(mChapterInfos));
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ChapterInfo info = ((ChapterInfoAdapter)l.getAdapter()).getItem(position);
        mOnChapaterSelectedListener.onChapterSelected(info);
    }
    
    private class ChapterInfoAdapter extends ArrayAdapter<ChapterInfo> {
        public ChapterInfoAdapter(ArrayList<ChapterInfo> chapterInfoList) {
            super(getActivity(), 0, chapterInfoList);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_chapter_info, parent, false);
            }
            
            TextView nameLabel = (TextView) convertView.findViewById(R.id.chapter_list_item_name);
            ChapterInfo info = getItem(position);
            nameLabel.setText(info.getName());

            return convertView;
        }
    }
}
