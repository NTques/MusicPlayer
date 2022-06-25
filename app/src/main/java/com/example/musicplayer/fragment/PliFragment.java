package com.example.musicplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musicplayer.activity.SetPlayListDialog;
import com.example.musicplayer.adapter.PlayListAdapter;
import com.example.musicplayer.databinding.FragmentPliBinding;
import com.example.musicplayer.dto.PliDTO;

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

        SetPlayListDialog dialog = new SetPlayListDialog(getActivity(), new SetPlayListDialog.DialogListener() {
            @Override
            public void clickBtn(String title, String explain) {
                adapter.addItem(title);
            }
        });
        binding.layoutAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        return binding.getRoot();
    }
}
