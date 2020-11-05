package com.jon.shclient.ble;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jon.shclient.R;

import java.util.List;

public class CDataAdapter extends RecyclerView.Adapter<CDataAdapter.ViewHolder> {
    private List<CData> mDataList;

    public CDataAdapter(List<CData> fruitList) {
        mDataList = fruitList;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataView;

        TextView tvTime;
        TextView tvAuthor;
        TextView tvTitle;
        ImageView imgCollect;

        public ViewHolder(View view) {
            super(view);
            dataView = view;
            tvTime = view.findViewById(R.id.tvTime);
            tvAuthor = view.findViewById(R.id.tvAuthor);
            tvTitle = view.findViewById(R.id.tvTitle);
            imgCollect = view.findViewById(R.id.ivCollect);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_ble_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.dataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                CData data = mDataList.get(position);
                Toast.makeText(view.getContext(), "你点击了View" + data.name, Toast.LENGTH_SHORT).show();
            }
        });

        holder.imgCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                CData data = mDataList.get(position);
                Toast.makeText(view.getContext(), "你点击了图片" + data.name, Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CData data = mDataList.get(position);
        //holder.imgCollect.setImageResource(fruit.getImageId());
        holder.tvAuthor.setText(data.author);
        holder.tvTime.setText(data.time);

        holder.tvTitle.setText(data.name);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}

