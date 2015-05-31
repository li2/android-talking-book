package me.li2.android.hellomoon;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class HelloMoonFragment extends Fragment {
    private final static String TAG = "HelloMoonFragment";
    private final static int PROGRESS_UPDATE_INTERVAL = 200;
    
    private Handler mHandler = new Handler();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Button mPlayButton;
    private Button mStopButton;
    private SeekBar mSeekBar;
    private int mAudioFileResId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAudioFileResId = R.raw.yaoyuedui_haiyou;
        
        // Play if isn't playing or paused.
        // 通过Uri，而不是Resource Id来构建MediaPlayer
        Uri fileUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + mAudioFileResId);
        mPlayer.play(getActivity(), fileUri);
        mPlayer.setLooping(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hello_moon, parent);
        mPlayButton = (Button) v.findViewById(R.id.hellomoon_playButton);
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
        
        mStopButton = (Button) v.findViewById(R.id.hellomoon_stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.stop();
            }
        });
        
        mSeekBar = (SeekBar) v.findViewById(R.id.seekbar);
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
        return v;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
    }
    
    private void updateProgress() {
        getActivity().runOnUiThread(new Runnable() {
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
