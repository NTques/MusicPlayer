package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.dto.PliDTO;

import java.util.ArrayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.CustomViewHolder> {
    private ArrayList<PliDTO> pli = new ArrayList<>();
    private PliDTO plidata;

    public interface OnItemClickListener { void onItemClick(View v, int position);}
    private OnItemClickListener mListener = null;
    public void setOnItemClickListener(OnItemClickListener listener) {this.mListener = listener;}

    public void addItem(String title) {
        plidata = new PliDTO();

        plidata.setPliTitle(title);
        pli.add(plidata);
    }
    @NonNull
    @Override
    public PlayListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.play_list_item, parent, false);
        CustomViewHolder customViewHolder = new CustomViewHolder(view);

        Log.d("야", "시발");
        return customViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        plidata = pli.get(position);

        holder.tvPliTitle.setText(plidata.getPliTitle());
    }

    @Override
    public int getItemCount() {
        return pli.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPliProfile;
        public TextView tvPliTitle;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPliProfile = itemView.findViewById(R.id.ivPliProfile);
            tvPliTitle = itemView.findViewById(R.id.tvPliTitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if(mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }
    }
}
