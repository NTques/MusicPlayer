package com.example.musicplayer.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;

import com.example.musicplayer.R;
import com.example.musicplayer.databinding.ActivityMusicPlayerBinding;
import com.example.musicplayer.dto.MusicDTO;
import com.example.musicplayer.service.MusicPlayService;

import java.util.Locale;

public class MusicPlayerActivity extends AppCompatActivity {
    private ActivityMusicPlayerBinding binding = null;
    private MusicDTO musicData;

    public int curTime;

    MusicPlayService ms;
    boolean isService = false;
    boolean isPlaying = false;
    boolean isTracking = false;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.MyBinder mb = (MusicPlayService.MyBinder) service;
            ms = mb.getService();

            isService = true;

            ProgressThread thread = new ProgressThread();
            thread.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //서비스 바인딩
        Intent sIntent = new Intent(MusicPlayerActivity.this, MusicPlayService.class);
        bindService(sIntent, conn, Context.BIND_AUTO_CREATE);

        //리시버 등록
        registerReceiver(receiver, new IntentFilter("Main"));

        //View 내용 변경
        musicData = (MusicDTO) getIntent().getSerializableExtra("musicData");
        setContent(musicData);

        //play버튼 이미지 적용
        isPlaying = ((MainActivity)MainActivity.mContext).isPlaying;
        setBtnPlayIamge();

        //클릭 이벤트 모은 함수
        setClickEvent();

        //SeekBar 프로그레스 변경
        setSeekBarProgress(((MainActivity) MainActivity.mContext).getCurrentTime());
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getSerializableExtra("musicData") != null) {
                musicData = (MusicDTO) intent.getSerializableExtra("musicData");
            }
            isPlaying = intent.getBooleanExtra("isPlaying", false);

            setContent(musicData);
            setBtnPlayIamge();

            //음악이 끝나고 다시 시작되는 사이 SeekBar 프로그레스 세팅에 지연이 있어 선언
            curTime = ms.getMusicCurrentTime();
            setSeekBarProgress(curTime);
        }
    };

    private void setClickEvent() {
        Intent serviceIntent = new Intent("Service");

        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = ms.isPlaying();
                if (isPlaying) {
                    serviceIntent.putExtra("mode", "stop");
                } else {
                    serviceIntent.putExtra("mode", "start");
                }
                serviceIntent.putExtra("curTime", binding.seekBar2.getProgress());
                sendBroadcast(serviceIntent);
                serviceIntent.putExtra("mode", "");
            }
        });

        binding.seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                serviceIntent.putExtra("curTime", seekBar.getProgress());
                serviceIntent.putExtra("mode", "start");
                sendBroadcast(serviceIntent);
                isTracking = false;
                serviceIntent.putExtra("mode", "");
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceIntent.putExtra("skip", "next");
                sendBroadcast(serviceIntent);
                serviceIntent.putExtra("skip", "");
            }
        });

        binding.btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceIntent.putExtra("skip", "pre");
                sendBroadcast(serviceIntent);
                serviceIntent.putExtra("skip", "");
            }
        });
    }

    private void setContent(MusicDTO musicData) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());


        binding.tvTitlePlay.setText(musicData.getTitle());
        binding.tvTitlePlay.setSelected(true);
        binding.tvArtistPlay.setText(musicData.getArtist());
        binding.ivAlbumPlay.setImageURI(sAlbumArtUri);
        binding.tvDuration.setText(timeFormat((int) musicData.getDuration()));
        binding.seekBar2.setMax((int) musicData.getDuration());
    }

    private void setBtnPlayIamge() {
        if (isPlaying) {
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private String timeFormat(int duration) {
        int dur = duration;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;
        String songTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mns, scs);
        if (hrs == 0) {
            songTime = String.format(Locale.getDefault(), "%02d:%02d", mns, scs);
        }
        return songTime;
    }

    private void setSeekBarProgress(int cur) {
        binding.seekBar2.setProgress(cur);
        binding.tvCurTime.setText(timeFormat(cur));
    }

    class ProgressThread extends Thread {
        public void run() {
            while (isService) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isPlaying && !isTracking) {
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            curTime = ms.getMusicCurrentTime();
            setSeekBarProgress(curTime);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
