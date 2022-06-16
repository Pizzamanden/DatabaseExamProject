package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.ManagePostFragment;
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
    private Fragment fragment;


    public CommentForPostRecyclerViewAdapter(Fragment fragment, List<CommentWithUserName> commentsForPost, String loggedUserID){
        this.commentsForPost = commentsForPost;
        this.loggedUserID = loggedUserID;
        this.fragment = fragment;
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
        Log.d(TAG, "onBindViewHolder: Comment ID: " + commentsForPost.get(position).comment.id);
        Log.d(TAG, "onBindViewHolder: Post ID: " + commentsForPost.get(position).comment.post_id);
        if(commentsForPost.get(position).name == null){
            holder.textViewUserName.setText(commentsForPost.get(position).comment.user_id);
        } else {
            holder.textViewUserName.setText(commentsForPost.get(position).name);
        }
        String commentContent = commentsForPost.get(position).comment.text;
        // While loop to remove spaces in front of text
        while(commentContent.charAt(0) == ' ' && commentContent.length() > 1){
            commentContent = commentContent.substring(1);
        }
        final String actualCommentContent = commentContent;
        holder.textViewCommentText.setText(actualCommentContent);
        if(loggedUserID.equals(commentsForPost.get(position).comment.user_id)){
            // The comment is owned, and the layout has the button
            holder.commentEditButton.setOnClickListener((v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("sentData_post_id", commentsForPost.get(position).comment.post_id);
                bundle.putString("sentData_comment_text", actualCommentContent);
                bundle.putInt("sentData_comment_id", commentsForPost.get(position).comment.id);
                NavHostFragment.findNavController(fragment)
                        .navigate(R.id.action_viewPostFragment_to_editCommentFragment, bundle);
            }));
        }
    }

    @Override
    public int getItemCount() {
        return commentsForPost.size();
    }



}
