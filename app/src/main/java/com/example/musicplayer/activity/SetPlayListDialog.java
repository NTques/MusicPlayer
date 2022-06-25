package com.example.musicplayer.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.example.musicplayer.databinding.DialogSetPlaylistBinding;

public class SetPlayListDialog extends Dialog {

    public SetPlayListDialog(@NonNull Context context) {
        super(context);
        DialogSetPlaylistBinding binding = DialogSetPlaylistBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.etPliTitle.setText("제목");
        binding.etPliExplain.setText("설명");
    }
}
