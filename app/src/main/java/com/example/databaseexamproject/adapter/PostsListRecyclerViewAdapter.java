package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.R;
import com.example.databaseexamproject.UserLoginFragment;
import com.example.databaseexamproject.room.Converters;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;

import java.util.List;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    private List<PostJoinUser> localdata;
    private Fragment fragment;


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

    public PostsListRecyclerViewAdapter(List<PostJoinUser> data, Fragment fragment){
        localdata = data;
        this.fragment = fragment;
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
            // We now send all the data required to show a post!
            Bundle args = new Bundle();
            args.putString("sentData_user_id" , localdata.get(holder.getAdapterPosition()).post.user_id);
            Log.d(TAG, "onBindViewHolder: " + localdata.get(holder.getAdapterPosition()).post.user_id);
            args.putInt("sentData_post_id", localdata.get(holder.getAdapterPosition()).post.id);
            Log.d(TAG, "onBindViewHolder: " + localdata.get(holder.getAdapterPosition()).post.id);
            args.putString("sentData_content" , localdata.get(holder.getAdapterPosition()).post.content);
            Log.d(TAG, "onBindViewHolder: " + localdata.get(holder.getAdapterPosition()).post.content);
            args.putString("sentData_user_name" , localdata.get(holder.getAdapterPosition()).name);
            Log.d(TAG, "onBindViewHolder: " + localdata.get(holder.getAdapterPosition()).name);
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.action_postsListFragment_to_viewPostFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return localdata.size();
    }
}
