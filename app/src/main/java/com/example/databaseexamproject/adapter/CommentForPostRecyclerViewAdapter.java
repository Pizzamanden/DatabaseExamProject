package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.R;
import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.CommentWithUserName;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;

import java.util.List;

public class CommentForPostRecyclerViewAdapter extends RecyclerView.Adapter<CommentForPostRecyclerViewAdapter.ViewHolder>{

    private List<CommentWithUserName> commentsForPost;
    private String loggedUserID;


    public CommentForPostRecyclerViewAdapter(List<CommentWithUserName> commentsForPost, String loggedUserID){
        this.commentsForPost = commentsForPost;
        this.loggedUserID = loggedUserID;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public TextView textViewUserName;
        public TextView textViewCommentText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            textViewUserName = itemView.findViewById(R.id.textView_commentUserName);
            textViewCommentText = itemView.findViewById(R.id.textView_commentContent);
        }
    }

    @NonNull
    @Override
    public CommentForPostRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // The layout to inflate
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_comment_layout, parent, false);
        return new CommentForPostRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentForPostRecyclerViewAdapter.ViewHolder holder, int position) {
        // What happens when we want to inflate a layout
        if(commentsForPost.get(position).name == null){
            holder.textViewUserName.setText(commentsForPost.get(position).comment.user_id);
        } else {
            holder.textViewUserName.setText(commentsForPost.get(position).name);
        }
        holder.textViewCommentText.setText(commentsForPost.get(position).comment.text);
        if(loggedUserID.equals(commentsForPost.get(position).comment.user_id)){
            // We own the comment
            // TODO delete/edit comment action here
        } else {
            // We do not own the comment
        }
    }

    @Override
    public int getItemCount() {
        return commentsForPost.size();
    }



}
