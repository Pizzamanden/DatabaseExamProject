package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.R;
import com.example.databaseexamproject.room.Converters;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;

import java.util.List;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    private List<PostJoinUser> localdata;


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public ConstraintLayout layout;
        public TextView textViewUserName;
        public TextView textViewPostText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            layout = itemView.findViewById(R.id.parent);
            textViewPostText = itemView.findViewById(R.id.textView_postText);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
        }
    }

    public PostsListRecyclerViewAdapter(List<PostJoinUser> data){
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

        holder.textViewUserName.setText(localdata.get(position).post.user_id);
        holder.textViewPostText.setText(localdata.get(position).post.content);

        holder.layout.setOnClickListener( (v) -> {
            Log.d(TAG, "onBindViewHolder: " + Converters.dateToTimestamp(localdata.get(holder.getAdapterPosition()).post.stamp));
            Log.d(TAG, "onBindViewHolder:  This post ID is: " + localdata.get(holder.getAdapterPosition()).post.id);
        });
    }

    @Override
    public int getItemCount() {
        return localdata.size();
    }
}
