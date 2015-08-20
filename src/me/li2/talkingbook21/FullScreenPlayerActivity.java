package me.li2.talkingbook21;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.viewpagerindicator.LinePageIndicator;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import me.li2.audioplayer.AudioPlayerController;
import me.li2.audioplayer.AudioPlayerController.PlaybackState;
import me.li2.talkingbook21.ChapterPageAdapter.OnChapterPageWordClickListener;
import me.li2.talkingbook21.data.ChapterInfo;
import me.li2.talkingbook21.data.ChapterInfoLab;
import me.li2.talkingbook21.data.TalkingBookChapter;

public class FullScreenPlayerActivity extends FragmentActivity {
    private final static String TAG = "FullScreenPlayerActivity";
    
    public final static String EXTRA_CHAPTER_NAME = "me.li2.talkingbook21.FullScreenPlayerActivity.chapter_name";
    private final static int PROGRESS_UPDATE_INTERVAL = 200;
    
    private TextView mCurrentTimeLabel;
    private TextView mDurationLabel;
    private MenuItem mPlayPauseMenuItem;
    private ViewPager mChapterViewPager;
    private ChapterPageAdapter mChapterPageAdapter;
    
    private Handler mHandler = new Handler();
    private AudioPlayerController mPlayerController;

    private Drawable mPlayDrawable;
    private Drawable mPauseDrawable;
    private ChapterInfo mChapterInfo;
    private Uri mAudioUri;
    private Uri mTimingJsonUri;
    private int mChapterPageSelectedIndex;
    // set this flag when audio position out of page, and clear this flag when page selected.    
    private boolean isAudioPositionOutOfPage;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        String chapterName = getIntent().getStringExtra(EXTRA_CHAPTER_NAME);
        mChapterInfo = ChapterInfoLab.get(this).getChapterInfo(chapterName);
        mAudioUri = mChapterInfo.getAudioUri();
        mTimingJsonUri = mChapterInfo.getTimingJsonUri();

        // Enables the "home" icon to be some kind of button and displays the "<".
        // should also add meta-data "android.support.PARENT_ACTIVITY" in manifest.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(this) != null) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(chapterName);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }
        
        mCurrentTimeLabel = (TextView) findViewById(R.id.catcher_currentTimeLabel);
        mDurationLabel = (TextView) findViewById(R.id.catcher_durationLabel);
        mChapterViewPager = (ViewPager) findViewById(R.id.catcher_chapterViewPager);
        LinePageIndicator indicator = (LinePageIndicator) findViewById(R.id.catcher_chapterPageIndicator);
        
        mPlayDrawable = getResources().getDrawable(R.drawable.ic_play_circle_outline_white_36dp);
        mPauseDrawable = getResources().getDrawable(R.drawable.ic_pause_circle_outline_white_36dp);

        mChapterPageAdapter = new ChapterPageAdapter(this, getSupportFragmentManager(), mTimingJsonUri);
        mChapterPageAdapter.setOnChapterPageWordClickListener(mOnPageAdapterWordClickListener);
        mChapterViewPager.setAdapter(mChapterPageAdapter);
        mChapterViewPager.setOnPageChangeListener(mOnPageChangeListener);

        indicator.setViewPager(mChapterViewPager);
        indicator.setOnPageChangeListener(mOnPageChangeListener);
        indicator.setLineWidth(calculateIndicatorWidth());
        indicator.setStrokeWidth(getResources().getDimension(R.dimen.chapter_page_indicator_stroke_width));
        indicator.setGapWidth(getResources().getDimension(R.dimen.chapter_page_indicator_gap_width));
        indicator.setSelectedColor(getResources().getColor(R.color.chapter_selected_highlight));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (mPlayerController == null) {
            mPlayerController = new AudioPlayerController();
            mPlayerController.registerCallback(mPlayerControllerCallbacks);
            mPlayerController.play(this, mAudioUri);
            mPlayerController.setLooping(true);
            int lastPosition = mChapterInfo.getLastPosition();
            Log.d(TAG, "last positioin " + lastPosition);
            if (lastPosition > 0) {
                seekToPosition(lastPosition);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        TalkingBookChapter.destroy();
        mChapterInfo.setLastPosition(mPlayerController.getCurrentPosition());
        stopSeekbarUpdate();
        mExecutorService.shutdown();
        mPlayerController.stop();
    }
    
    
    private AudioPlayerController.Callbacks mPlayerControllerCallbacks = new AudioPlayerController.Callbacks() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updatePlaybackState(state);
        }
        
        @Override
        public void onAudioDataChanged(int duration) {
            updateDuration(duration);
        }
    };
    
    private void updatePlaybackState(PlaybackState playbackState) {
        Log.d(TAG, "updatePlaybackState: " + playbackState);
        switch (playbackState) {
        case PLAYBACK_STATE_PLAYING:
            if (mPlayPauseMenuItem != null) {
                mPlayPauseMenuItem.setIcon(mPauseDrawable);
            }
            scheduleSeekbarUpdate();
            break;
        case PLAYBACK_STATE_PAUSED:
        case PLAYBACK_STATE_STOPPED:
        case PLAYBACK_STATE_NONE:
            if (mPlayPauseMenuItem != null) {
                mPlayPauseMenuItem.setIcon(mPlayDrawable);
            }
            stopSeekbarUpdate();
            break;            
        default:
            break;
        }
    }
    
    // Update Seekbar *********************************************************
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    
    private void scheduleSeekbarUpdate() {
        Log.d(TAG, "scheduleSeekbarUpdate()");
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(mUpdateProgressTask);
                    }
                }, PROGRESS_UPDATE_INTERVAL, PROGRESS_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }
    
    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    
    private void updateProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int msec = mPlayerController.getCurrentPosition();
                // set SeekBar progress & current time label
                mCurrentTimeLabel.setText(DateUtils.formatElapsedTime(msec/1000));
                mChapterPageAdapter.seekChapterToTime(msec);
                // check if seeking time is out of selected page, if true, then set ViewPager to current item.
                if (isAudioPositionOutOfPage(msec)) {
                    isAudioPositionOutOfPage = true;
                    // onPageSelected will be called after setCurrentItem,
                    // so we set a flag to notify onPageSelected don't seek audio again.
                    mChapterViewPager.setCurrentItem(mChapterPageAdapter.getReadingPage(msec));
                }
            }
        });
    }
    
    // Update duration label ************************************************** 
    private void updateDuration(int duration) {
        // Update Seekbar & TotalTimeLabel
        mDurationLabel.setText(DateUtils.formatElapsedTime(duration/1000));
    }

    private OnChapterPageWordClickListener mOnPageAdapterWordClickListener = new OnChapterPageWordClickListener() {
        @Override
        public void onChapterPageWordClick(int msec) {
            seekToPosition(msec);
        }
    };
    
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mChapterPageSelectedIndex = position;
            if (isAudioPositionOutOfPage) {
                Log.d(TAG, "onPageSelected(): out of page, no seek again!");
                // in case for reading the beginning word twice!
                isAudioPositionOutOfPage = false;
            } else {
                seekToPosition(mChapterPageAdapter.getPageTiming(position));
            }
        }
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        
        @Override
        public void onPageScrollStateChanged(int state) {}
    };
    
    private boolean isAudioPositionOutOfPage(int msec) {
        int readingPage = mChapterPageAdapter.getReadingPage(msec);
        // Log.d(TAG, "reading page " + readingPage + ", ViewPager count " + mChapterPageAdapter.getCount() + ", Selected " + mChapterPageSelectedIndex);
        if (readingPage >=0 && readingPage < mChapterPageAdapter.getCount() && readingPage != mChapterPageSelectedIndex) {
            Log.d(TAG, "audio already out of page!");
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (NavUtils.getParentActivityName(this) != null) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;

        case R.id.catcher_action_playPause:
            onActionPlayPauseClick();
            return true;

        case R.id.catcher_action_forward5:
            seekToPosition(mPlayerController.getCurrentPosition()+5*1000);
            return true;
            
        case R.id.catcher_action_replay5:
            seekToPosition(mPlayerController.getCurrentPosition()-5*1000);
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_full_player, menu);
        mPlayPauseMenuItem = menu.findItem(R.id.catcher_action_playPause);
        return super.onCreateOptionsMenu(menu);
    }
    
    private void onActionPlayPauseClick() {
        PlaybackState playbackState = mPlayerController.getPlaybackState();
        switch (playbackState) {
        case PLAYBACK_STATE_PLAYING:
            mPlayerController.pause();
            break;
        case PLAYBACK_STATE_STOPPED:
        case PLAYBACK_STATE_PAUSED:
            mPlayerController.play();
            break;
        default:
            Log.d(TAG, "onClick with state " + playbackState);
            break;
        }
    }
    
    
    private void seekToPosition(int position) {
        if (mPlayerController.getPlaybackState() != PlaybackState.PLAYBACK_STATE_PLAYING) {
            mPlayerController.play();
        }
        mPlayerController.seekToPosition(position);
    }

    private int calculateIndicatorWidth() {
        ChapterPageUtil util = new ChapterPageUtil(this);
        int pageWidth = util.getChapterPageWidth();
        float indicatorGapWidth = getResources().getDimension(R.dimen.chapter_page_indicator_gap_width);
        int count = mChapterPageAdapter.getCount();
        int width = (int)(pageWidth - indicatorGapWidth * (count - 1)) / count;
        return width;
    }
}
