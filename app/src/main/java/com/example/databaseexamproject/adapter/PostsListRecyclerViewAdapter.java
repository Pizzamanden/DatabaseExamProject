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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.PostLayoutSetup;
import com.example.databaseexamproject.PostsListFragment;
import com.example.databaseexamproject.R;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.example.databaseexamproject.webrequests.ImageDownload;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    // The post, with the users name attached
    private List<PostWithReactions> localData; // We are changing the data within, so it could be final. But it is NOT unchanging!

    private final Fragment fragment;

    private final String loggedUserID;

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public ConstraintLayout layout;
        public TextView textViewUserName;
        public TextView textViewPostText;
        public Button buttonLikeReact;
        public Button buttonDislikeReact;
        public Button buttonAmbivalenceReact;
        public ImageView imageViewContentImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            layout = itemView.findViewById(R.id.parent);
            textViewPostText = itemView.findViewById(R.id.textView_postText);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            buttonLikeReact = itemView.findViewById(R.id.button_likeReact);
            buttonDislikeReact = itemView.findViewById(R.id.button_dislikeReact);
            buttonAmbivalenceReact = itemView.findViewById(R.id.button_ambivalentReact);
            imageViewContentImage = itemView.findViewById(R.id.imageView_contentImage);
        }
    }

    public PostsListRecyclerViewAdapter(Fragment fragment, List<PostWithReactions> data, String userID){
        localData = data;
        this.fragment = fragment;
        Log.d(TAG, "PostsListRecyclerViewAdapter: Posts length: " + localData.size());
        loggedUserID = userID;
    }

    @NonNull
    @Override
    public PostsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PostLayoutSetup thisPost = new PostLayoutSetup(localData, position, fragment.getActivity(), loggedUserID,
                holder.textViewUserName, holder.textViewPostText, holder.buttonLikeReact, holder.buttonDislikeReact, holder.buttonAmbivalenceReact, holder.imageViewContentImage);


        // Set the listener for the posts, to view them
        holder.layout.setOnClickListener((v) -> {
            // Set the variable in the parent activity to remember the recycler position
            PostsListFragment postsListFragment = (PostsListFragment) fragment;
            postsListFragment.setRememberRecyclerViewPosition(holder.getAdapterPosition());
            // Send the post id only, we make a SQL query on the other side (and we just need the id for that)
            Bundle args = new Bundle();
            args.putInt("sentData_post_id", localData.get(holder.getAdapterPosition()).post.id);
            args.putString("sentData_user_id", localData.get(holder.getAdapterPosition()).post.user_id);
            args.putString("sentData_user_name", localData.get(holder.getAdapterPosition()).name);
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.action_postsListFragment_to_viewPostFragment, args);
        });

    }

    @Override
    public int getItemCount() {
        return localData.size();
    }
}