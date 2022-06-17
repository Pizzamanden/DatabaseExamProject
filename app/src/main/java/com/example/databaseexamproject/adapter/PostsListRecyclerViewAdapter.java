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

    // Integer counter for remote calls in progress
    AtomicInteger remoteCallsInProgress = new AtomicInteger(0);

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
        // Set the users name and the content
        if (localData.get(position).name == null) {
            holder.textViewUserName.setText(localData.get(position).post.user_id);
        } else {
            holder.textViewUserName.setText(localData.get(position).name);
        }

        // We must scan the content for any images, and fetch them in case they are there
        String content = localData.get(position).post.content;
        int[] imageURLLocation = textContainsImageURL(content);
        if (imageURLLocation[1] != 0) {
            holder.imageViewContentImage.setVisibility(View.VISIBLE);
            // Now, get the image url
            String imageURL = content.substring(imageURLLocation[0], imageURLLocation[1]);
            // We setup the image download and showing process immediately
            new ImageDownload(holder.imageViewContentImage).execute(imageURL);
            Log.d(TAG, "onBindViewHolder: " + imageURL);
            Log.d(TAG, "onBindViewHolder: On post ID: " + localData.get(position).post.id);
            // Remove the Url from the String, and continue as we were
            content = content.substring(0, imageURLLocation[0]) + content.substring(imageURLLocation[1]);
        } else {
            holder.imageViewContentImage.setVisibility(View.GONE);
        }
        // While loop to remove spaces in front of text
        while (content.charAt(0) == ' ' && content.length() > 1) {
            content = content.substring(1);
        }
        Log.d(TAG, "onBindViewHolder: " + content);
        if (content != null && content.length() > 200) {
            holder.textViewPostText.setText(content.substring(0, 200));
        } else {
            holder.textViewPostText.setText(content);
        }

        Button[] buttons = {
                holder.buttonLikeReact,
                holder.buttonDislikeReact,
                holder.buttonAmbivalenceReact
        };
        int[] counts = {
                localData.get(position).type1Reactions,
                localData.get(position).type2Reactions,
                localData.get(position).type3Reactions
        };
        String[] names = {
                fragment.getString(R.string.likeReact),
                fragment.getString(R.string.dislikeReact),
                fragment.getString(R.string.ambivalenceReact)
        };
        boolean[] isReacted = {
                localData.get(position).userReaction == 1,
                localData.get(position).userReaction == 2,
                localData.get(position).userReaction == 3
        };

        // Set our buttons
        stylePostButtons(buttons, counts, names, isReacted, position);


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

    private void stylePostButtons(Button[] buttons, int[] counts, String[] names, boolean[] isReacted, int postPosition){

        for(int i = 0; i < buttons.length; i++){
            final int thisButtonType = i;
            // First we setup the state of our buttons
            buttons[i].setText(counts[i] + " " + names[i]);
            if(isReacted[i]){
                // Set the new styling, to show it is pressed down and synch
                setButtonActive(buttons[i]);
                counts[i] = counts[i] - 1;
            }
            // Then we attach the listener, which handles changes when the user clicks any button
            buttons[i].setOnClickListener(new View.OnClickListener() {
                final public String buttonName = names[thisButtonType]; // What the applicable textString is for this type
                final public int buttonCount = counts[thisButtonType];
                final public int buttonPosition = postPosition; // The position in the dataset
                final public int buttonNumber = thisButtonType + 1; // The actual integer representation in the database: 0 = deleted, 1 = like, 2 = displike, 3 = meh

                @Override
                public void onClick(View v) {
                    Button clickedButton = (Button) v;
                    Log.d(TAG, "styleButton: The number is " + buttonNumber);
                    Log.d(TAG, "styleButton: Post id is: " + localData.get(buttonPosition).post.id + " at position: " + buttonPosition);
                    Log.d(TAG, "styleButton: Has the user already reacted?: " + localData.get(buttonPosition).userReaction);
                    int savedUserReaction = localData.get(buttonPosition).userReaction;
                    if(savedUserReaction == buttonNumber){
                        // This button should have had been active
                        Log.d(TAG, "onClick: Unpress button " + (buttonNumber - 1) + " for post " + buttonPosition);
                        // This is the easy case. We had reacted this reaction, and now we want to remove it.
                        // First we launch a database request. (we do not wait for a response)
                        remoteCallsInProgress.incrementAndGet();
                        updateRemoteReactionTable(buttonPosition, 0, (response, responseBody, requestName) -> {
                            if(response.code() >= 300){
                                Toast.makeText(fragment.getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                            if(remoteCallsInProgress.decrementAndGet() == 0){
                                SynchronizeLocalDB.syncDB(fragment.getContext(), (success) ->{});
                            }
                        });
                        // We then update the visual amount and status.
                        localData.get(buttonPosition).userReaction = 0;
                        clickedButton.setText(buttonCount + " " + buttonName);
                        setButtonInactive(buttons[thisButtonType]);

                    } else {
                        // This button should have had been inactive
                        // This means we have a new value, for this user, for this reaction.
                        // Now we need to see if the value was set to something other than 0
                        if(savedUserReaction != 0){
                            // Now it gets less simple
                            // Another button was pressed, and we must now de-press that one and update the remote DB.
                            // We know which one it was, based on the saved user reaction
                            buttons[savedUserReaction - 1].setText((counts[savedUserReaction - 1]) + " " + names[savedUserReaction - 1]);
                            setButtonInactive(buttons[savedUserReaction - 1]);
                        }
                        Log.d(TAG, "onClick: Press button " + (buttonNumber - 1) + " for post " + buttonPosition);
                        // As we change the saved user reaction here, we do it after checking/handling the already pressed button (if there is one)
                        localData.get(buttonPosition).userReaction = buttonNumber;
                        clickedButton.setText((buttonCount + 1) + " " + buttonName);
                        setButtonActive(buttons[thisButtonType]);
                        // Now we update remote
                        remoteCallsInProgress.incrementAndGet();
                        updateRemoteReactionTable(buttonPosition, buttonNumber, (response, responseBody, requestName) -> {
                            if(response.code() >= 300){
                                Toast.makeText(fragment.getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                            if(remoteCallsInProgress.decrementAndGet() == 0){
                                SynchronizeLocalDB.syncDB(fragment.getContext(), (success) ->{});
                            }
                        });
                    }
                }
            });
        }
    }

    private void updateRemoteReactionTable(int dataPosition, int newReactionType, HttpRequest.HttpRequestResponse requestResponse){
        int post_id = localData.get(dataPosition).post.id;
        Log.d(TAG, "updateRemoteReactionTable: " + localData.get(dataPosition).post.content);
        Date userReactionTimestamp = localData.get(dataPosition).stamp;
        Reaction reaction = new Reaction(loggedUserID, post_id, newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + loggedUserID);
        Log.d(TAG, "updateRemoteReactionTable: " + post_id);
        Log.d(TAG, "updateRemoteReactionTable: " + newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + userReactionTimestamp);
        if(userReactionTimestamp != null){
            // Update action
            reaction.stamp = userReactionTimestamp;
        } else {
            long stamp = System.currentTimeMillis();
            reaction.stamp = new Date(stamp);
            localData.get(dataPosition).stamp = reaction.stamp;
        }
        RemoteDBRequest.reaction(fragment.getContext(), ( userReactionTimestamp != null ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT),
                reaction, requestResponse);
    }

    private void setButtonActive(Button button){
        // Here we style buttons for when they are active/highlighted
        // TODO button styling when clicked
    }

    private void setButtonInactive(Button button){
        // Here we style buttons for when they are inactive/just normal
        // TODO button styling when clicked
    }

    private int[] textContainsImageURL(String text){
        if(text == null){
            return new int[2];
        } else {
            int[] substringLocation = new int[2];
            String regex = "(http(s?):/)(/[^/]+)+\\.(?:jpg|gif|png)"; // Regex for mathing image urls
            // Regex explanation:
            // It was not made by hand, but with a tool, but still:
            // Group 1: matches (http)(s)(:/), where the (s) is optional
            // Group 3: It must end with (/)(*)(.)(format) where * is any NOT forward slash character, and format is an allowed image format.
            // It also breaks the regex if at any point (ignoring the first forward slash in group 1) there are two consequent forward slashes.
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if(matcher.find()){
                substringLocation[0] = matcher.start();
                substringLocation[1] = matcher.end();
            }
            return substringLocation;
        }
    }

}
