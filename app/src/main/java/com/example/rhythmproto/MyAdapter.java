package com.example.rhythmproto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jackandphantom.carouselrecyclerview.view.ReflectionImageView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<ImageItem> items;
    private OnItemClickListener listener;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    } // 뷰홀더 생성자

    public MyAdapter(List<ImageItem> items,OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    } // 어댑터 생성자

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       ImageItem item = items.get(position);
       holder.bind(item);
       holder.imageView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                listener.onItemSelected();
           }
       });
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ReflectionImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            // 뷰 홀더 초기화
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(ImageItem item){
            imageView.setImageResource(item.getImageResource());
        }
    }
}

