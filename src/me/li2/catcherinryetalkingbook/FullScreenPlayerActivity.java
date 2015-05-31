package me.li2.catcherinryetalkingbook;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FullScreenPlayerActivity extends ActionBarActivity {
    private final static String TAG = "FullScreenPlayerActivity";
    private final static int PROGRESS_UPDATE_INTERVAL = 200;
    
    private Handler mHandler = new Handler();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Button mPlayButton;
    private Button mStopButton;
    private SeekBar mSeekBar;
    private int mAudioFileResId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        
        // Play if isn't playing or paused.
        // 通过Uri，而不是Resource Id来构建MediaPlayer
        mAudioFileResId = R.raw.yaoyuedui_haiou;
        Uri fileUri = Uri.parse("android.resource://" + getPackageName() + "/" + mAudioFileResId);
        mPlayer.play(this, fileUri);
//        String httpPath = "http://pan.baidu.com/s/1gd8enab";
//        mPlayer.play(getActivity(), httpPath);
        mPlayer.setLooping(true);
        
        mPlayButton = (Button) findViewById(R.id.catcher_playButton);
        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pause if audio is playing.
                if (mPlayer.isPlaying()) {
                    Log.d(TAG, "Pasue when audio is playing.");
                    mPlayer.pause();
                    mPlayButton.setText("Play");
                } else {
                    Log.d(TAG, "Resume when audio is paused.");
                    mPlayer.start();
                    mPlayButton.setText("Pause");
                }
            }
        });
        
        mStopButton = (Button) findViewById(R.id.catcher_stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.stop();
            }
        });
        
        mSeekBar = (SeekBar) findViewById(R.id.catcher_seekbar);
        mSeekBar.setMax(mPlayer.getDuration());
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.seekToPosition(seekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        });
        scheduleSeekbarUpdate();

        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
    }
    
    private void updateProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(mPlayer.getCurrentPosition());
            }
        });
    }
    
    private void scheduleSeekbarUpdate() {
        Log.d(TAG, "scheduleSeekbarUpdate()");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress();
                mHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        }, PROGRESS_UPDATE_INTERVAL);
    }
}
