package me.li2.talkingbook21;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import me.li2.talkingbook21.ChapterListFragment.OnChapaterSelectedListener;
import me.li2.talkingbook21.data.ChapterInfo;

public class ChapterListActivity extends FragmentActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "ChapterListActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        
        FragmentManager fm = getSupportFragmentManager();
        ChapterListFragment fragment = (ChapterListFragment) fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new ChapterListFragment();
            fragment.setOnChapaterSelectedListener(mOnChapaterSelectedListener);
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
	}
	
    private OnChapaterSelectedListener mOnChapaterSelectedListener = new OnChapaterSelectedListener() {
        @Override
        public void onChapterSelected(ChapterInfo info) {
            startFullScreenPlayerActivity(info);
        }
    };

    private void startFullScreenPlayerActivity(ChapterInfo info) {
        Intent intent = new Intent(this, FullScreenPlayerActivity.class);
        intent.putExtra(FullScreenPlayerActivity.EXTRA_CHAPTER_NAME, info.getName());
        startActivity(intent);
    }
}
