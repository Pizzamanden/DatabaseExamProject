package com.example.databaseexamproject.adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.databaseexamproject.room.dataobjects.BigFuckPost;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;
import com.example.databaseexamproject.room.dataobjects.PostReactions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<PostsListRecyclerViewAdapter.ViewHolder> {

    // The post, with the users name attached
    private List<BigFuckPost> localData;

    // The 3 most recent comments for this post
    // TODO get the 3 most recent comments

    private Fragment fragment;


    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        public ConstraintLayout layout;
        public TextView textViewUserName;
        public TextView textViewPostText;
        public Button buttonLikeReact;
        public Button buttonDislikeReact;
        public Button buttonAmbivalenceReact;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            layout = itemView.findViewById(R.id.parent);
            textViewPostText = itemView.findViewById(R.id.textView_postText);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            buttonLikeReact = itemView.findViewById(R.id.button_likeReact);
            buttonDislikeReact = itemView.findViewById(R.id.button_dislikeReact);
            buttonAmbivalenceReact = itemView.findViewById(R.id.button_ambivalentReact);
        }
    }

    public PostsListRecyclerViewAdapter(Fragment fragment, List<BigFuckPost> data){
        localData = data;
        this.fragment = fragment;
        Log.d(TAG, "PostsListRecyclerViewAdapter: Posts length: " + localData.size());
    }


    @NonNull
    @Override
    public PostsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the users name and the content
        if(localData.get(position).name == null){
            holder.textViewUserName.setText(localData.get(position).post.user_id);
        } else {
            holder.textViewUserName.setText(localData.get(position).name);
        }

        // We must scan the content for any images, and fetch them in case they are there
        String content = localData.get(position).post.content;
        String imageURL = textContainsImageURL(content);
        if(imageURL != null){
            Log.d(TAG, "onBindViewHolder: Content contained image!");
            // Remove the Url from the String
            content = content.replace(imageURL, "");
            // TODO now do something with that URL
        }
        holder.textViewPostText.setText(content);

        // Set our buttons
        styleButton(holder.buttonLikeReact, localData.get(position).type0Reactions, fragment.getString(R.string.likeReact), localData.get(position).userReaction == 1);
        styleButton(holder.buttonDislikeReact, localData.get(position).type1Reactions, fragment.getString(R.string.dislikeReact), localData.get(position).userReaction == 2);
        styleButton(holder.buttonAmbivalenceReact, localData.get(position).type2Reactions, fragment.getString(R.string.ambivalenceReact), localData.get(position).userReaction == 3);


        // Set the listener for the posts, to view them
        holder.layout.setOnClickListener( (v) -> {
            // We now send all the data required to show a post!
            Bundle args = new Bundle();
            args.putString("sentData_user_id" , localData.get(holder.getAdapterPosition()).post.user_id);
            args.putInt("sentData_post_id", localData.get(holder.getAdapterPosition()).post.id);
            args.putString("sentData_content" , localData.get(holder.getAdapterPosition()).post.content);
            args.putString("sentData_user_name" , localData.get(holder.getAdapterPosition()).name);
            args.putInt("recyclerViewElementPosition", holder.getAdapterPosition());
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.action_postsListFragment_to_viewPostFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return localData.size();
    }

    private void styleButton(Button button, int count, String name, boolean isReacted){
        if(isReacted){
            // Set the new styling, to show it is pressed down and sych
        }
        button.setText(count + " " + name);
    }

    private String textContainsImageURL(String text){
        String regex = "(http(s?):/)(/[^/]+)+\\.(?:jpg|gif|png)"; // Regex for mathing image urls
        // Regex explanation:
        // It was not made by hand, but with a tool, but still:
        // Group 1: matches (http)(s)(:/), where the (s) is optional
        // Group 3: It must end with (/)(*)(.)(format) where * is any NOT forward slash character, and format is an allowed image format.
        // It also breaks the regex if at any point (ignoring the first forward slash in group 1) there are two consequent forward slashes.
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String result = null;
        while(matcher.find()){
            result = matcher.group();
            Log.d(TAG, "textContainsImageURL: URL location starts at: " + matcher.start());
            Log.d(TAG, "textContainsImageURL: URL location ends at: " + matcher.end());
            Log.d(TAG, "textContainsImageURL: The found URL is: " + matcher.group());
        }
        return result;
    }
}
