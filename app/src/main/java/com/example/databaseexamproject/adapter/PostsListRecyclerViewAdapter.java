package com.example.databaseexamproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.R;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    private String[] localdata;


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public TextView textViewUserName;
        public TextView textViewPostText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            textViewPostText = itemView.findViewById(R.id.textView_postText);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
        }
    }

    public PostsListRecyclerViewAdapter(String[] data){
        localdata = data;
    }


    @NonNull
    @Override
    public PostsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewUserName.setText(localdata[position]);
    }

    @Override
    public int getItemCount() {
        return localdata.length;
    }
}
