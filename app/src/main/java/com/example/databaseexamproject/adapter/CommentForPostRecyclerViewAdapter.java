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

import java.util.ArrayList;
import java.util.List;

public class CommentForPostRecyclerViewAdapter extends RecyclerView.Adapter<CommentForPostRecyclerViewAdapter.ViewHolder>{

    private List<CommentWithUserName> commentsForPost;
    private String loggedUserID;

    private List<Integer> layouts = new ArrayList<>();


    public CommentForPostRecyclerViewAdapter(List<CommentWithUserName> commentsForPost, String loggedUserID){
        this.commentsForPost = commentsForPost;
        this.loggedUserID = loggedUserID;
        // Adding the types of layout we can use
        layouts.add(R.layout.recyclerview_comment_layout);
        layouts.add(R.layout.recyclerview_comment_owned_layout);
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public TextView textViewUserName;
        public TextView textViewCommentText;
        public Button commentEditButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            textViewUserName = itemView.findViewById(R.id.textView_commentUserName);
            textViewCommentText = itemView.findViewById(R.id.textView_commentContent);
            commentEditButton = itemView.findViewById(R.id.button_editComment);
        }
    }

    @NonNull
    @Override
    public CommentForPostRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // The layout to inflate
        View view = LayoutInflater.from(parent.getContext()).inflate(layouts.get(viewType), parent, false);
        return new CommentForPostRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(loggedUserID.equals(commentsForPost.get(position).comment.user_id)){
            // The comment is owned
            return 1;
        } else {
            // The comment is not owned
            return 0;
        }
        // Yes, this could just be "return (loggedUserID.equals(commentsForPost.get(position).comment.user_id) ? 1 : 0);"
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
            // The comment is owned, and the layout has the button
            holder.commentEditButton.setOnClickListener((v -> {
                int commentID = commentsForPost.get(position).comment.id;
                Log.d(TAG, "onBindViewHolder: Comment ID is: " + commentID);
            }));
        }
    }

    @Override
    public int getItemCount() {
        return commentsForPost.size();
    }



}
