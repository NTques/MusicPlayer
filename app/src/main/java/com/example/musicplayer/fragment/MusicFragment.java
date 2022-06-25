package com.example.musicplayer.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.MusicListAdapter;
import com.example.musicplayer.databinding.FragmentMusicBinding;
import com.example.musicplayer.dto.MusicDTO;
import com.example.musicplayer.service.MusicPlayService;

import java.util.ArrayList;

public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding = null;

    //음악 정보를 담을 ArrayList선언
    private ArrayList<MusicDTO> musicList = new ArrayList<>();
    //ContentProvider를 통해 정보를 담을 MusicDTO 선언
    private MusicDTO musicData = new MusicDTO();
    private MusicListAdapter adapter;
    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);

        //어댑터 및 리사이클러뷰 세팅
        adapter = new MusicListAdapter();
        binding.rvMusicList.setHasFixedSize(true);
        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvMusicList.setAdapter(adapter);

        Intent sIntent = new Intent(getActivity(), MusicPlayService.class);

        //리사이클러뷰 아이템 클릭시 서비스로 musicList와 position Intent
        adapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                musicList = adapter.getMusicList();
                musicData = musicList.get(position);
                Toast.makeText(getActivity(), musicData.getTitle(), Toast.LENGTH_SHORT).show();

                sIntent.putExtra("playList", musicList);
                sIntent.putExtra("position", position);
                getActivity().startService(sIntent);
            }
        });
        readAudio();
        /** 프래그먼트 이동시 초기화되고 다시 세팅됨. onCreate에서 다시 코딩. **/
        return binding.getRoot();
    }

    //Content Provider를 통해 Device의 음악 파일 정보 읽은 후 MusicListAdapter의 addItem 메소드
    @SuppressLint("Range")
    private void readAudio() {
        ContentResolver contentResolver = getContext().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"; // 파일이 음악일 경우만
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC"; // 파일명을 기준으로 오름차순 정렬
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        //커서 초기화
        cursor.moveToFirst();

        /*다음 커서가 없을 때까지 MusicListAdapter의 addItem 메소드를 통해 MusicListAdapter의
        ArrayList(PlayList)에 아이템 추가*/
        if (cursor != null && cursor.getCount() > 0) {
            do {
                long trackId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String datapath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                adapter.addItem(trackId, albumId, title, artist, album, mDuration, datapath);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    //프래그먼트 종료시 View Binding초기화
    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
