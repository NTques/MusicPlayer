package com.example.musicplayer.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.musicplayer.databinding.DialogSetPlaylistBinding;

public class SetPlayListDialog extends Dialog {
    private DialogListener dialogListener;

    public SetPlayListDialog(@NonNull Context context, DialogListener dialogListener) {
        super(context);
        DialogSetPlaylistBinding binding = DialogSetPlaylistBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.etPliTitle.setText("제목");
        binding.etPliExplain.setText("설명");

        binding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.clickBtn(binding.etPliTitle.getText().toString(), binding.etPliExplain.getText().toString());
                dismiss();
            }
        });
    }

    public interface DialogListener {
        void clickBtn(String title, String explain);
    }
}
