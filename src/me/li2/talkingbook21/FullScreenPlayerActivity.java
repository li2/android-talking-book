package me.li2.talkingbook21;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.text.style.TtsSpan;
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
    private LrcFragment mLrcFragment;
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
        
        // Load Timing-Json file
        String localJsonFileName = "chapter1.json.out.json";
        String stringFromJsonFile = FileOperateUtil.loadJSONFromAsset(getApplicationContext(), localJsonFileName);
        JSONObject jsonObj = null;
        JSONArray jsonArray = null;
        try {
			jsonObj = new JSONObject(stringFromJsonFile);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (jsonObj != null) {
        	Log.d(TAG, "parse json");
        	try {
				jsonArray = jsonObj.getJSONArray("words");
				for (int index = 0; index < jsonArray.length(); index++) {
					JSONArray obj = jsonArray.getJSONArray(index);
					Log.d(TAG, String.format("%-5.3f  %s", (double)obj.get(1), (String)obj.get(0)));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        
        // Play if isn't playing or paused.
        // 通过Uri，而不是Resource Id来构建MediaPlayer
        mAudioFileResId = R.raw.m2;
        Uri fileUri = Uri.parse("android.resource://" + getPackageName() + "/" + mAudioFileResId);
        mPlayer.play(this, fileUri);
//        String httpPath = "http://pan.baidu.com/s/1gd8enab";
//        mPlayer.play(this, httpPath);
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

        //*
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment oldLrcFragment = fm.findFragmentById(R.id.catcher_lrcFragmentContainer);
        Fragment newLrcFragment = LrcFragment.newInstance("test2.lrc");
        mLrcFragment = (LrcFragment)newLrcFragment;
        if (oldLrcFragment != null) {
            ft.remove(oldLrcFragment);
        }
        ft.add(R.id.catcher_lrcFragmentContainer, newLrcFragment);
        ft.commit();
        //*/
        
        /*
        mLrcView = (LrcView) findViewById(R.id.catcher_lrcView);
        String lrc = getLrcFromAssets("test.lrc");
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
        
        beginLrcPlay();
        
        mLrcView.setListener(new LrcViewListener() {
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (mPlayer2 != null) {
                    Log.d(TAG, "onLrcSeeked new position:" + newPosition);
                    mPlayer2.seekTo((int)row.time);
                }
            }
        });
        */
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
    public void onLrcItemSelected(int lrcRow) {
        int milliseconds = mPlayer.getCurrentPosition();
        milliseconds -= 567; // since human click the item has a little delay, maybe 567ms.
        milliseconds = (milliseconds > 0) ? milliseconds : 0;
        int minutesPart = (milliseconds/1000)/60;
        int secondsPart = ((int)(milliseconds/1000))%60;
        int msPart = milliseconds - (minutesPart * 60 + secondsPart) * 1000;
        msPart = msPart > 99 ? 99 : msPart;
        String timestampStr = String.format("[%02d:%02d.%02d]", minutesPart, secondsPart, msPart);
        Log.d(TAG, "onLrcItemSelected: Lrc Row " + lrcRow + ":" + timestampStr);
        
        // 
        mLrcFragment.setTimestampStr(timestampStr);
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
