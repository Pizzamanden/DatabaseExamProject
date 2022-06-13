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
        // TODO is this shitty with long text?
        if(imageURL != null){
            Log.d(TAG, "onBindViewHolder: Content contained image!");
            // Remove the Url from the String
            content = content.replace(imageURL, "");
            // TODO now do something with that URL
        }
        holder.textViewPostText.setText(content);

        Button[] buttons = {
                holder.buttonLikeReact,
        };
        int[] counts = {
                localData.get(position).type1Reactions
        };
        String[] names = {
                fragment.getString(R.string.likeReact)
        };
        boolean[] isReacted = {
                localData.get(position).userReaction == 1
        };

        // Set our buttons
        stylePostButtons(buttons, counts, names, isReacted, position);


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

    private void stylePostButtons(Button[] buttons, int[] counts, String[] names, boolean[] isReacted, int postPosition){

        for(int i = 0; i < buttons.length; i++){
            final int thisButtonType = i;
            // TODO work out listener states
            // First we setup the state of our buttons
            if(isReacted[i]){
                // Set the new styling, to show it is pressed down and synch
                setButtonActive(buttons[i]);
            }
            buttons[i].setText(counts[i] + " " + names[i]);
            // Then we attach the listener, which handles changes when the user clicks any button
            buttons[i].setOnClickListener(new View.OnClickListener() {
                final public String buttonName = names[thisButtonType];
                final public int buttonCount = counts[thisButtonType];
                final public int buttonPosition = postPosition;
                final public int buttonNumber = thisButtonType + 1;

                @Override
                public void onClick(View v) {
                    Button clickedButton = (Button) v;
                    Log.d(TAG, "styleButton: The number is " + buttonNumber);
                    int savedUserReaction = localData.get(buttonPosition).userReaction;
                    if(savedUserReaction == buttonNumber){
                        // This button should have had been active
                        // This is the easy case. We had reacted this reaction, and now we want to remove it.
                        // First we launch a database request. (we do not wait for a response) TODO should we?
                        // TODO do remote shizz
                        // We then update the visual amount and status.
                        localData.get(buttonPosition).userReaction = 0;
                        clickedButton.setText((buttonCount - 1) + " " + buttonName);
                        setButtonInactive(buttons[thisButtonType]);

                    } else {
                        // This button should have had been inactive
                        // We know we must add 1 to the count of this button, and update the remote DB
                        // TODO update remote
                        // Now we need to see if the value was set to something other than 0
                        if(savedUserReaction != 0){
                            // Now it gets less simple
                            // Another button was pressed, and we must now de-press that one and update the remote DB.
                            // We know which one it was, based on the saved user reaction
                            // TODO do remote shizz
                            buttons[savedUserReaction - 1].setText((counts[savedUserReaction - 1] - 1) + " " + names[savedUserReaction - 1]);
                            setButtonInactive(clickedButton);
                        }
                        // As we change the saved user reaction here, we do it after checking/handling the already pressed button (if there is one)
                        localData.get(buttonPosition).userReaction = buttonNumber;
                        clickedButton.setText((buttonCount + 1) + " " + buttonName);
                        setButtonActive(buttons[thisButtonType]);
                    }
                }
            });
        }
    }

    private void setButtonActive(Button button){
        // Here we style buttons for when they are active/highlighted
        // TODO
    }

    private void setButtonInactive(Button button){
        // Here we style buttons for when they are inactive/just normal
        // TODO
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
