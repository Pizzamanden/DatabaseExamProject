package com.example.databaseexamproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.R;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    private String[] localdata;


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views


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

    }

    @Override
    public int getItemCount() {
        return localdata.length;
    }
}
