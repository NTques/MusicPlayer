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
import android.view.MotionEvent;
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

        //외부저장소 접근 퍼미션 요청(거절시 음악 리스티가 읽히지 않음)
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        ProgressThread thread = new ProgressThread();
        thread.start();

        //Adapter가 참조하게 될 Context
        context = getBaseContext();
        //외부 Activity에서 MainActivity 접근시 참초하게 될 Context
        mContext = this;

        sIntent = new Intent(MainActivity.this, MusicPlayService.class);
        bindService(sIntent, conn, Context.BIND_AUTO_CREATE);
        //프래그먼트 설정
        setFrag(0);
        setClickEvent();

        registerReceiver(receiver, new IntentFilter("Main"));
    }

    //앱 종료시 binding초기화 및 Service Binding 해제
    @Override
    protected void onDestroy() {
        binding = null;
        unbindService(conn);
        super.onDestroy();
    }

    //MusicPlayService(서비스)에서 Intent받는 BroadcastReceiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // sendBroadcast에서 musicData값이 들어오지 않는다면 이전 값을 그대로 유지하기 위해 if 문으로 처리
            if (intent.getSerializableExtra("musicData") != null) {
                musicData = (MusicDTO) intent.getSerializableExtra("musicData");
                setContent(musicData);
            }

            isPlaying = intent.getBooleanExtra("isPlaying", false);

            //음악이 끝나고 다시 시작되는 사이 SeekBar 프로그레스 세팅에 지연이 있어 선언
            //curTime = ms.getMusicCurrentTime();
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

        //play버튼 클릭이벤트
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

        /*SeekBar 클릭 이벤트
         SeekBar클릭 중 Thread때문에 SeekBar의 프로세스가 현재 MediaPlayer 포지션으로 적용되는 오류방지를 위해
         IsTracking을 통해 클릭되는동안 동작하지 않게 제어*/
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

        /*하단 미니 Player 클릭 이벤트
        클릭시 MusicPlayActivity로 이동 및 음악 데이터를 전송하여 View 세팅
        PlayButton의 이미지 변경을 위해 isPlaying(현재 음악이 실행되고 있는지 여부)도 같이 Intent */
        binding.playerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent playerIntent = new Intent(MainActivity.this, MusicPlayerActivity.class);
//                playerIntent.putExtra("musicData", musicData);
//                playerIntent.putExtra("isPlaying", isPlaying);
//                startActivity(playerIntent);
            }
        });

        binding.playerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float startY = 0;
                float curY = event.getY();

                if (action == MotionEvent.ACTION_DOWN) {
                    startY = curY;
                    Log.d("시작점", String.valueOf(startY));
                } else if (action == MotionEvent.ACTION_UP) {
                    Log.d("종료점", String.valueOf(curY));
                    if (startY < curY) {
                        Log.d("Visibillity", "Gone");
                        binding.playerLayout.setVisibility(View.GONE);
                        serviceIntent.putExtra("release", true);
                        sendBroadcast(serviceIntent);
                        unbindService(conn);
                    }
                }
                return false;
            }
        });

        //이전곡과 다음곡으로 Skip하기 위한 버튼 이벤트
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

    //Fragment 변경을 위한 메소드 setCilckEvent의 TabLayout클릭 이벤트에서 fragNum을 받아 Fragment설정
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

    //SeekBar의 Progress를 MusicPlayService의 MediaPlayer의 CurrentPosition을 받아와 세팅
    class ProgressThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isPlaying) {
                    curTime = ms.getMusicCurrentTime();
                }
                if (!isTracking) {
                    binding.seekBar.setProgress(curTime);
                }
            }
        }
    }

    //MusicPlayerActivity에서 화면전환 후 즉각적인 Progress세팅을 위해 선언
    public int getCurrentTime() {
        return binding.seekBar.getProgress();
    }

    /*하단 미니 Player의 View 내용을 현재 재생중인 음악의 정보로 세팅하기 위한 메소드
    BroadcastReceiver에서 사용됨*/
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

    //btnPlaySmall 이미지를 설정하기 위한 메소드 isPlaying의 값에 따라 이미지 변경.
    private void setBtnPlayImage() {
        if (isPlaying) {
            binding.btnPlayMain.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            binding.btnPlayMain.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

}