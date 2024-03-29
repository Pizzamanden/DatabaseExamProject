package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.databaseexamproject.adapter.PostsListRecyclerViewAdapter;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.example.databaseexamproject.webrequests.ImageDownload;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostLayoutSetup {

    private List<PostWithReactions> localData;
    private int position;
    private Context context;
    private String loggedUserID;

    // Views for the post
    private TextView textViewPostUserName;
    private TextView textViewPostText;
    private Button buttonLikeReact;
    private Button buttonDislikeReact;
    private Button buttonAmbivalenceReact;
    private ImageView imageViewContentImage;

    public PostLayoutSetup(List<PostWithReactions> localData, int position, Context context, String loggedUserID, TextView textViewPostUserName, TextView textViewPostText, Button buttonLikeReact, Button buttonDislikeReact, Button buttonAmbivalenceReact, ImageView imageViewContentImage) {
        this.localData = localData;
        this.position = position;
        this.context = context;
        this.loggedUserID = loggedUserID;
        this.textViewPostUserName = textViewPostUserName;
        this.textViewPostText = textViewPostText;
        this.buttonLikeReact = buttonLikeReact;
        this.buttonDislikeReact = buttonDislikeReact;
        this.buttonAmbivalenceReact = buttonAmbivalenceReact;
        this.imageViewContentImage = imageViewContentImage;
        setupViews();
    }

    private void setupViews(){
        // Set the users name and the content
        if (localData.get(position).name == null) {
            textViewPostUserName.setText(localData.get(position).post.user_id);
        } else {
            textViewPostUserName.setText(localData.get(position).name);
        }

        // We must scan the content for any images, and fetch them in case they are there
        String content = localData.get(position).post.content;
        if(content == null){
            content = "";
        }
        int[] imageURLLocation = textContainsImageURL(content);
        if (imageURLLocation[1] != 0) {
            imageViewContentImage.setVisibility(View.VISIBLE);
            // Now, get the image url
            String imageURL = content.substring(imageURLLocation[0], imageURLLocation[1]);
            // We setup the image download and showing process immediately
            new ImageDownload(imageViewContentImage).execute(imageURL);
            Log.d(TAG, "onBindViewHolder: " + imageURL);
            Log.d(TAG, "onBindViewHolder: On post ID: " + localData.get(position).post.id);
            // Remove the Url from the String, and continue as we were
            content = content.substring(0, imageURLLocation[0]) + content.substring(imageURLLocation[1]);
        } else {
            imageViewContentImage.setVisibility(View.GONE);
        }
        // While loop to remove spaces in front of text
        while (content.length() > 0 && content.charAt(0) == ' ') {
            content = content.substring(1);
        }
        Log.d(TAG, "onBindViewHolder: " + content);
        if (content.length() > 200) {
            textViewPostText.setText(content.substring(0, 200));
        } else {
            textViewPostText.setText(content);
        }

        Button[] buttons = {
                buttonLikeReact,
                buttonDislikeReact,
                buttonAmbivalenceReact
        };
        int[] counts = {
                localData.get(position).type1Reactions,
                localData.get(position).type2Reactions,
                localData.get(position).type3Reactions
        };
        String[] names = {
                context.getString(R.string.likeReact),
                context.getString(R.string.dislikeReact),
                context.getString(R.string.ambivalenceReact)
        };
        boolean[] isReacted = {
                localData.get(position).userReaction == 1,
                localData.get(position).userReaction == 2,
                localData.get(position).userReaction == 3
        };

        // Set our buttons
        stylePostButtons(buttons, counts, names, isReacted, position);
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
                        updateRemoteReactionTable(buttonPosition, 0, (response, responseBody, requestName) -> {
                            if(response.code() >= 300){
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
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
                        updateRemoteReactionTable(buttonPosition, buttonNumber, (response, responseBody, requestName) -> {
                            if(response.code() >= 300){
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
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
        Date userReactionTimestamp = localData.get(dataPosition).reactionStamp;
        Reaction reaction = new Reaction(loggedUserID, post_id, newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + loggedUserID);
        Log.d(TAG, "updateRemoteReactionTable: " + post_id);
        Log.d(TAG, "updateRemoteReactionTable: " + newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + userReactionTimestamp);
        if(userReactionTimestamp != null){
            // Update action
            reaction.stamp = userReactionTimestamp;
        } else {
            // Insert action
            long stamp = System.currentTimeMillis();
            reaction.stamp = new Date(stamp);
            localData.get(dataPosition).reactionStamp = reaction.stamp;
        }
        RemoteDBRequest.reaction(context, ( userReactionTimestamp != null ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT),
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
