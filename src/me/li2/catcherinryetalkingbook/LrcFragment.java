package me.li2.catcherinryetalkingbook;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LrcFragment extends ListFragment {
    private final static String TAG = "LrcFragment";
    private final static int LRC_FONT_SIZE = 16;
    
    private static int sSelectedLine;
    
    private Callbacks mCallbacks;
    
    public interface Callbacks {
        void onLrcItemSelected(int seconds);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> lrcList = new ArrayList<String>();
        lrcList.add("天空是什么颜色 在云的那端 或许是生命的港湾");
        lrcList.add("风中是谁在唱着 动人的歌谣 展翅翱翔");
        lrcList.add("让我们飞 飞过高山 飞过海洋直到那梦的彼岸");
        lrcList.add("让我们飞 穿越丛林 越过荆棘密布的地方");

        lrcList.add("天空是什么颜色 在云的那端 或许是生命的港湾");
        lrcList.add("风中是谁在唱着 动人的歌谣 展翅翱翔");
        lrcList.add("让我们飞 飞过高山 飞过海洋直到那梦的彼岸");
        lrcList.add("让我们飞 穿越丛林 越过荆棘密布的地方");
        
        lrcList.add("年轻的心 不会停止 就像海鸥一样在风中歌唱");
        lrcList.add("年轻的心 充满力量 想着光明的地方");

        setListAdapter(new LrcAdapter(lrcList));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setDivider(null);
        listView.setVerticalScrollBarEnabled(false);
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
        sSelectedLine = position;
        ((LrcAdapter)l.getAdapter()).notifyDataSetChanged();
        mCallbacks.onLrcItemSelected(position);
    }

    private class LrcAdapter extends ArrayAdapter<String> {
        public LrcAdapter(List<String> objects) {
            super(getActivity(), android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;
            textView.setText(getItem(position));
            textView.setTextSize(LRC_FONT_SIZE);
            if (sSelectedLine == position) {
                textView.setBackgroundColor(getResources().getColor(R.color.lightgray));
            } else {
                textView.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
            return view;
        }
    }
}
