package com.example.musicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.dto.MusicDTO;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.CustomViewHolder> {
    private final ArrayList<MusicDTO> musicList = new ArrayList<>();
    private MusicDTO musicData;

    public interface OnItemClickListener { void onItemClick(View v, int position);}
    private OnItemClickListener mListener = null;
    public void setOnItemClickListener(OnItemClickListener listener) {this.mListener = listener;}

    @NonNull
    @Override
    public MusicListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.music_list_item, parent, false);
        CustomViewHolder customViewHolder = new CustomViewHolder(view);

        Log.d("야", "호");
        return customViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        musicData = musicList.get(position);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());

        holder.tvTitle.setText(musicData.getTitle());
        holder.tvArtist.setText(musicData.getArtist());
        holder.ivAlbum.setImageURI(sAlbumArtUri);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void addItem(long id, long albumId, String title, String artist, String album, long duration, String datapath) {
        MusicDTO data = new MusicDTO();
        data.setId(id);
        data.setAlbumId(albumId);
        data.setTitle(title);
        data.setArtist(artist);
        data.setAlbum(album);
        data.setDuration(duration);
        data.setDataPath(datapath);
        musicList.add(data);
    }

    public ArrayList<MusicDTO> getMusicList() {
        return musicList;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvArtist;
        public ImageView ivAlbum;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if(mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                        tvTitle.setSelected(true);
                        tvArtist.setSelected(true);
                    }
                }
            });
        }

    }
}
