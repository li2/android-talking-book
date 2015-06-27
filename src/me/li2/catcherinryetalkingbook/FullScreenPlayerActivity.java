package me.li2.catcherinryetalkingbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.douzi.android.lrc.DefaultLrcBuilder;
import com.douzi.android.lrc.ILrcBuilder;
import com.douzi.android.lrc.ILrcView.LrcViewListener;
import com.douzi.android.lrc.LrcRow;
import com.douzi.android.lrc.LrcView;

public class FullScreenPlayerActivity extends ActionBarActivity
    implements LrcFragment.Callbacks {
    
    private final static String TAG = "FullScreenPlayerActivity";
    private final static int PROGRESS_UPDATE_INTERVAL = 200;
    
    private Handler mHandler = new Handler();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Button mPlayButton;
    private Button mStopButton;
    private SeekBar mSeekBar;
    private int mAudioFileResId;

    LrcView mLrcView;
    private int mPalyTimerDuration = 1000;
    private Timer mTimer;
    private TimerTask mTask;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        
        // Play if isn't playing or paused.
        // 通过Uri，而不是Resource Id来构建MediaPlayer
        mAudioFileResId = R.raw.yaoyuedui_haiou;
        Uri fileUri = Uri.parse("android.resource://" + getPackageName() + "/" + mAudioFileResId);
//        mPlayer.play(this, fileUri);
//        String httpPath = "http://pan.baidu.com/s/1gd8enab";
//        mPlayer.play(getActivity(), httpPath);
//        mPlayer.setLooping(true);
        
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

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment oldLrcFragment = fm.findFragmentById(R.id.catcher_lrcFragmentContainer);
        Fragment newLrcFragment = new LrcFragment();
        if (oldLrcFragment != null) {
            ft.remove(oldLrcFragment);
        }
//        ft.add(R.id.catcher_lrcFragmentContainer, newLrcFragment);
//        ft.commit();
        
        mLrcView = (LrcView) findViewById(R.id.catcher_lrcView);
        String lrc = getLrcFromAssets("test.lrc");
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
        
        beginLrcPlay();
        
        mLrcView.setListener(new LrcViewListener() {

            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (mPlayer2 != null) {
                    Log.d(TAG, "onLrcSeeked:" + row.time);
//                    mPlayer2.seekTo((int)row.time);
                }
            }
        });
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

    @Override
    public void onLrcItemSelected(int seconds) {
        Log.d(TAG, "onLrcItemSelected current position " + seconds + ":" + mPlayer.getCurrentPosition());
//        mPlayer.seekToPosition(seconds*1000);
    }
    
    public String getLrcFromAssets(String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    MediaPlayer mPlayer2;
    public void beginLrcPlay(){

        mPlayer2 = new MediaPlayer();
        try {
            mPlayer2.setDataSource(getAssets().openFd("m.mp3").getFileDescriptor());
            mPlayer2.setOnPreparedListener(new OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "onPrepared");
                    mp.start();
                    if(mTimer == null){
                        mTimer = new Timer();
                        mTask = new LrcTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                    }
                }
            });
            mPlayer2.setOnCompletionListener(new OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    stopLrcPlay();
                }
            });
            mPlayer2.prepare();
            mPlayer2.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    class LrcTask extends TimerTask{

        long beginTime = -1;

        @Override
        public void run() {
            if(beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }

            final long timePassed = mPlayer2.getCurrentPosition();
            runOnUiThread(new Runnable() {

                public void run() {
                    mLrcView.seekLrcToTime(timePassed);
                }
            });

        }
    };
}
