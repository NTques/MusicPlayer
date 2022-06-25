package com.example.musicplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.example.musicplayer.R;
import com.example.musicplayer.databinding.ActivityMainBinding;
import com.example.musicplayer.dto.MusicDTO;
import com.example.musicplayer.fragment.AlbumFragment;
import com.example.musicplayer.fragment.FolderFragment;
import com.example.musicplayer.fragment.MusicFragment;
import com.example.musicplayer.fragment.PliFragment;
import com.example.musicplayer.service.MusicPlayService;
import com.example.musicplayer.service.MusicPlayService.MyBinder;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding = null;

    public Context context;
    public static Context mContext;

    MusicDTO musicData;

    MusicPlayService ms;
    boolean isService = false;
    boolean isPlaying = false;
    boolean isTracking = false;
    int curTime;

    Intent sIntent;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinder mb = (MyBinder) service;
            ms = mb.getService();

            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        ProgressThread thread = new ProgressThread();
        thread.start();

        context = getBaseContext();
        mContext = this;

        sIntent = new Intent(MainActivity.this, MusicPlayService.class);
        bindService(sIntent, conn, Context.BIND_AUTO_CREATE);
        //프래그먼트 설정
        setFrag(0);
        setClickEvent();

        registerReceiver(receiver, new IntentFilter("Main"));
    }

    @Override
    protected void onDestroy() {
        binding = null;
        unbindService(conn);
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getSerializableExtra("musicData") != null) {
                musicData = (MusicDTO) intent.getSerializableExtra("musicData");
                setContent(musicData);
            }
            isPlaying = intent.getBooleanExtra("isPlaying", false);

            //음악이 끝나고 다시 시작되는 사이 SeekBar 프로그레스 세팅에 지연이 있어 선언
            curTime = ms.getMusicCurrentTime();
            binding.seekBar.setProgress(curTime);

            Log.d("메인", "리시버");
            setBtnPlayImage();
        }
    };

    private void setClickEvent() {
        Intent serviceIntent = new Intent("Service");
        //탭 레이아웃 클릭이벤트
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabPos = tab.getPosition();
                setFrag(tabPos);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.btnPlayMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = ms.isPlaying();
                if (isPlaying) {
                    serviceIntent.putExtra("mode", "stop");
                } else {
                    serviceIntent.putExtra("mode", "start");
                }
                serviceIntent.putExtra("curTime", binding.seekBar.getProgress());
                sendBroadcast(serviceIntent);
                //다른 버튼 클릭시 같이 sendBroastcast되니 초기화
                serviceIntent.putExtra("mode", "");
            }
        });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                sendBroadcast(serviceIntent);
                isTracking = false;
            }
        });

        binding.playerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerIntent = new Intent(MainActivity.this, MusicPlayerActivity.class);
                playerIntent.putExtra("musicData", musicData);
                playerIntent.putExtra("isPlaying", isPlaying);
                startActivity(playerIntent);
            }
        });

        binding.btnNextMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceIntent.putExtra("skip", "next");
                sendBroadcast(serviceIntent);
                serviceIntent.putExtra("skip", "");
            }
        });

        binding.btnPreMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceIntent.putExtra("skip", "pre");
                sendBroadcast(serviceIntent);
                serviceIntent.putExtra("skip", "");
            }
        });
    }

    private void setFrag(int fragNum) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (fragNum) {
            case 0:
                ft.replace(R.id.mainFrame, new MusicFragment()).commit();
                break;
            case 1:
                ft.replace(R.id.mainFrame, new FolderFragment()).commit();
                break;
            case 2:
                ft.replace(R.id.mainFrame, new PliFragment()).commit();
                break;
            case 3:
                ft.replace(R.id.mainFrame, new AlbumFragment()).commit();
                break;
        }
    }

    class ProgressThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                    curTime = ms.getMusicCurrentTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isTracking) {
                    binding.seekBar.setProgress(curTime);
                }
            }
        }
    }

    public int getCurrentTime() {
        return binding.seekBar.getProgress();
    }
    private void setContent(MusicDTO musicData) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());

        binding.playerLayout.setVisibility(View.VISIBLE);

        binding.tvTitleMain.setText(musicData.getTitle());
        binding.tvTitleMain.setSelected(true);
        binding.tvArtistMain.setText(musicData.getArtist());
        binding.tvArtistMain.setSelected(true);
        binding.ivAlbumMain.setImageURI(sAlbumArtUri);
        binding.seekBar.setMax((int)musicData.getDuration());
    }

    private void setBtnPlayImage() {
        if (isPlaying) {
            binding.btnPlayMain.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            binding.btnPlayMain.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

}