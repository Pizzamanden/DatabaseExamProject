package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.PostLayoutSetup;
import com.example.databaseexamproject.R;
import com.example.databaseexamproject.ViewPostFragment;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.CommentWithUserName;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.example.databaseexamproject.webrequests.ImageDownload;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentForPostRecyclerViewAdapter extends RecyclerView.Adapter<CommentForPostRecyclerViewAdapter.ViewHolder>{


    private List<CommentWithUserName> commentsForPost;
    private String loggedUserID;
    // Our data for this post
    private PostWithReactions postData;

    private List<Integer> layouts = new ArrayList<>();
    private Fragment fragment;


    public CommentForPostRecyclerViewAdapter(Fragment fragment, List<CommentWithUserName> commentsForPost, PostWithReactions postData, String loggedUserID){
        this.commentsForPost = commentsForPost;
        this.loggedUserID = loggedUserID;
        this.fragment = fragment;
        this.postData = postData;
        // Adding the types of layout we can use
        layouts.add(R.layout.recyclerview_post_layout);
        layouts.add(R.layout.recyclerview_add_comment_layout);
        layouts.add(R.layout.recyclerview_comment_owned_layout);
        layouts.add(R.layout.recyclerview_comment_layout);
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        // Views for comments
        public TextView textViewCommentUserName;
        public TextView textViewCommentText;
        public Button commentEditButton;

        // Views for the post
        public TextView textViewPostUserName;
        public TextView textViewPostText;
        public Button buttonLikeReact;
        public Button buttonDislikeReact;
        public Button buttonAmbivalenceReact;
        public ImageView imageViewContentImage;

        // Views for "add comment" box
        public EditText editTextCommentCreation;
        public Button buttonSubmitComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            // Comment views
            textViewCommentUserName = itemView.findViewById(R.id.textView_commentUserName);
            textViewCommentText = itemView.findViewById(R.id.textView_commentContent);
            commentEditButton = itemView.findViewById(R.id.button_editComment);

            // Post views
            textViewPostText = itemView.findViewById(R.id.textView_postText);
            textViewPostUserName = itemView.findViewById(R.id.textView_userName);
            buttonLikeReact = itemView.findViewById(R.id.button_likeReact);
            buttonDislikeReact = itemView.findViewById(R.id.button_dislikeReact);
            buttonAmbivalenceReact = itemView.findViewById(R.id.button_ambivalentReact);
            imageViewContentImage = itemView.findViewById(R.id.imageView_contentImage);

            // "add comment" box
            editTextCommentCreation = itemView.findViewById(R.id.editText_commentCreation);
            buttonSubmitComment = itemView.findViewById(R.id.button_submitComment);
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
        if(position == 0) {
            return 0;
        } else if(position == 1) {
            return 1;
        } else {
            if(loggedUserID.equals(commentsForPost.get(position-2).comment.user_id)){
                // The comment is owned
                return 2;
            } else {
                // The comment is not owned
                return 3;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CommentForPostRecyclerViewAdapter.ViewHolder holder, int position) {
        if(position == 0){
            List<PostWithReactions> postAsList = new ArrayList<>();
            postAsList.add(postData);
            PostLayoutSetup thisPost = new PostLayoutSetup(postAsList, position, fragment.getActivity(), loggedUserID,
                    holder.textViewPostUserName, holder.textViewPostText, holder.buttonLikeReact, holder.buttonDislikeReact, holder.buttonAmbivalenceReact, holder.imageViewContentImage);

        } else if(position == 1){
            // It is the "add comment" box
            holder.buttonSubmitComment.setOnClickListener((v)->{
                // Create content
                String content = "forPost:" + postData.post.id + " " + holder.editTextCommentCreation.getText();
                long stamp = System.currentTimeMillis();
                Long post_generated_id = loggedUserID.hashCode() + stamp;
                int idToUse = post_generated_id.intValue();
                Post post = new Post(idToUse, loggedUserID, content);
                RemoteDBRequest.post(fragment.getActivity(), RemoteDBRequest.QUERY_TYPE_INSERT, post, (response, responseBody, requestName) ->{
                    SynchronizeLocalDB.syncDB(fragment.getActivity(), (success)->{
                        Log.d(TAG, "onCreateView: Comment Created!");
                        Toast.makeText(fragment.getActivity(), R.string.comment_created, Toast.LENGTH_LONG).show();
                        ViewPostFragment viewPostFragment = (ViewPostFragment) fragment;
                        viewPostFragment.refreshFragment();
                    });
                });
            });
        } else {
            int commentPosition = position - 2;
            // We load the comments
            Log.d(TAG, "onBindViewHolder: Comment ID: " + commentsForPost.get(commentPosition).comment.id);
            Log.d(TAG, "onBindViewHolder: Post ID: " + commentsForPost.get(commentPosition).comment.post_id);
            if(commentsForPost.get(commentPosition).name == null){
                holder.textViewCommentUserName.setText(commentsForPost.get(commentPosition).comment.user_id);
            } else {
                holder.textViewCommentUserName.setText(commentsForPost.get(commentPosition).name);
            }
            String commentContent = commentsForPost.get(commentPosition).comment.text;
            // While loop to remove spaces in front of text
            while(commentContent.charAt(0) == ' ' && commentContent.length() > 1){
                commentContent = commentContent.substring(1);
            }
            final String actualCommentContent = commentContent;
            holder.textViewCommentText.setText(actualCommentContent);
            if(loggedUserID.equals(commentsForPost.get(commentPosition).comment.user_id)){
                // The comment is owned, and the layout has the button
                holder.commentEditButton.setOnClickListener((v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("sentData_post_id", commentsForPost.get(commentPosition).comment.post_id);
                    bundle.putString("sentData_comment_text", actualCommentContent);
                    bundle.putInt("sentData_comment_id", commentsForPost.get(commentPosition).comment.id);
                    NavHostFragment.findNavController(fragment)
                            .navigate(R.id.action_viewPostFragment_to_editCommentFragment, bundle);
                }));
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentsForPost.size() + 2;
    }



}