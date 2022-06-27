package com.example.musicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.example.musicplayer.dto.MusicDTO;

import java.util.ArrayList;

public class MusicPlayService extends Service {
    private ArrayList<MusicDTO> playList = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private MusicDTO musicData = new MusicDTO();
    private int curPos;

    IBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d("서비스", "생성");

        mediaPlayer = new MediaPlayer();
        registerReceiver(receiver, new IntentFilter("Service"));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("서비스", "시작");

        //포지션만 Intent하게 될 경우를 대비하여 if 문으로 playList값이 null일 경우 NullPointError 방지
        if (intent.getSerializableExtra("playList") != null) {
            playList = (ArrayList<MusicDTO>) intent.getSerializableExtra("playList");
        }

        curPos = intent.getIntExtra("position", 0);

        setMediaPlayer(curPos);
        return super.onStartCommand(intent, flags, startId);
    }

    private void setMediaPlayer(int position) {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        musicData = playList.get(position);

        //음악 재생이 완료되었을 때 curPos++ 후 setMusic으로 다음곡 자동재생
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                curPos++;
                if (curPos > playList.size() - 1) {
                    curPos = 0;
                } else if (curPos < 0) {
                    curPos = playList.size() - 1;
                }
                musicData = playList.get(curPos);
                setMusic();
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                Log.d("재생", musicData.getTitle());

                /* 음악이 변경될 때마다 MainActivity와 MusicPlayActivity의 BoradcastReceiver로
                재생중인 음악의 데이터 전송 */
                Intent mainIntent = new Intent("Main");
                mainIntent.putExtra("musicData", musicData);
                mainIntent.putExtra("isPlaying", isPlaying());
                sendBroadcast(mainIntent);
            }
        });

        setMusic();
    }

    private void setMusic() {
        Uri uri = Uri.parse(musicData.getDataPath());
        try {
            mediaPlayer.reset();
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext() , uri);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent mainIntent = new Intent("Main");

            String mode = intent.getStringExtra("mode");
            if (mode != null && mode.equals("start")) {
                mediaPlayer.start();
            } else if (mode != null && mode.equals("stop")) {
                mediaPlayer.pause();
            }

            mainIntent.putExtra("isPlaying", mediaPlayer.isPlaying());
            sendBroadcast(mainIntent);

            int curTime = intent.getIntExtra("curTime", -333);
            if (curTime != -333) {
                mediaPlayer.seekTo(curTime);
            }

            String skip = intent.getStringExtra("skip");
            if (skip != null && skip.equals("next")) {
                curPos++;
                recyclePosition(curPos);
                musicData = playList.get(curPos);
                setMusic();
            } else if (skip != null && skip.equals("pre")) {
                curPos--;
                recyclePosition(curPos);
                musicData = playList.get(curPos);
                setMusic();
            }

//            boolean doRelease = intent.getBooleanExtra("release", false);
//            if (doRelease) {
//                mainIntent.putExtra("isPlaying", false);
//                sendBroadcast(mainIntent);
//                mediaPlayer.release();
//            }
        }
    };

    public int getMusicCurrentTime() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void recyclePosition(int position) {
        if (curPos > playList.size() - 1) {
            curPos = 0;
        } else if (curPos < 0) {
            curPos = playList.size() - 1;
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}