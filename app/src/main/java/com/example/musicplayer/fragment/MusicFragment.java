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

    private ArrayList<MusicDTO> musicList = new ArrayList<>();
    private MusicDTO musicData = new MusicDTO();
    private MusicListAdapter adapter;
    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);

        adapter = new MusicListAdapter();
        binding.rvMusicList.setHasFixedSize(true);
        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvMusicList.setAdapter(adapter);

        Intent sIntent = new Intent(getActivity(), MusicPlayService.class);
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

    @SuppressLint("Range")
    private void readAudio() {
        ContentResolver contentResolver = getContext().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        cursor.moveToFirst();
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
}
