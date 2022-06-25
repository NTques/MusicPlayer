package com.example.musicplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musicplayer.R;
import com.example.musicplayer.activity.SetPlayListDialog;
import com.example.musicplayer.adapter.PlayListAdapter;
import com.example.musicplayer.databinding.FragmentPliBinding;
import com.example.musicplayer.dto.PliDTO;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PliFragment extends Fragment {
    private FragmentPliBinding binding = null;
    private PlayListAdapter adapter;

    private ArrayList<PliDTO> pli = new ArrayList<>();
    private PliDTO data = new PliDTO();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPliBinding.inflate(inflater, container, false);

        adapter = new PlayListAdapter();
        binding.rvPlayList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPlayList.setAdapter(adapter);

        binding.layoutAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetPlayListDialog dialog = new SetPlayListDialog(getActivity());
                dialog.show();
            }
        });
        adapter.addItem("Test 1");
        adapter.addItem("Test 2");
        adapter.addItem("Test 3");

        return binding.getRoot();
    }
}
