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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.databaseexamproject.ManagePostFragment;
import com.example.databaseexamproject.R;
import com.example.databaseexamproject.ViewPostFragment;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Comment;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentForPostRecyclerViewAdapter extends RecyclerView.Adapter<CommentForPostRecyclerViewAdapter.ViewHolder>{


    private List<CommentWithUserName> commentsForPost;
    private String loggedUserID;
    // Our data for this post
    private PostWithReactions postData;

    // Integer counter for remote calls in progress
    AtomicInteger remoteCallsInProgress = new AtomicInteger(0);

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
            // It is the post

            // Handle a null user name
            if(postData.name == null){
                holder.textViewPostUserName.setText(postData.post.user_id);
            } else {
                holder.textViewPostUserName.setText(postData.name);
            }

            // Handle images
            String content = postData.post.content;
            if(content == null){
                content = "";
            }
            int[] imageURLLocation = textContainsImageURL(content);
            if(imageURLLocation[1] != 0){
                holder.imageViewContentImage.setVisibility(View.VISIBLE);
                // Now, get the image url
                String imageURL = content.substring(imageURLLocation[0], imageURLLocation[1]);
                // We setup the image download and showing process immediately
                new ImageDownload(holder.imageViewContentImage).execute(imageURL);
                Log.d(TAG, "onBindViewHolder: " + imageURL);
                Log.d(TAG, "onBindViewHolder: On post ID: " + postData.post.id);
                // Remove the Url from the String, and continue as we were
                content = content.substring(0, imageURLLocation[0]) + content.substring(imageURLLocation[1]);
            }

            // While loop to remove spaces in front of text
            while(content.length() > 0 && content.charAt(0) == ' '){
                content = content.substring(1);
            }
            Log.d(TAG, "onBindViewHolder: " + content);
            // We limit content which is just too long
            if(content.length() > 200){
                holder.textViewPostText.setText(content.substring(0, 200));
            } else {
                holder.textViewPostText.setText(content);
            }

            // Setup reaction buttons
            Button[] buttons = {
                    holder.buttonLikeReact,
                    holder.buttonDislikeReact,
                    holder.buttonAmbivalenceReact
            };
            int[] counts = {
                    postData.type1Reactions,
                    postData.type2Reactions,
                    postData.type3Reactions
            };
            String[] names = {
                    fragment.getString(R.string.likeReact),
                    fragment.getString(R.string.dislikeReact),
                    fragment.getString(R.string.ambivalenceReact)
            };
            boolean[] isReacted = {
                    postData.userReaction == 1,
                    postData.userReaction == 2,
                    postData.userReaction == 3
            };
            stylePostButtons(buttons, counts, names, isReacted);

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
                        // TODO reload fragment
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

    private void stylePostButtons(Button[] buttons, int[] counts, String[] names, boolean[] isReacted){

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
                final public int buttonNumber = thisButtonType + 1; // The actual integer representation in the database: 0 = deleted, 1 = like, 2 = displike, 3 = meh

                @Override
                public void onClick(View v) {
                    Button clickedButton = (Button) v;
                    int savedUserReaction = postData.userReaction;
                    if(savedUserReaction == buttonNumber){
                        // This button should have had been active
                        // This is the easy case. We had reacted this reaction, and now we want to remove it.
                        // First we launch a database request. (we do not wait for a response)
                        remoteCallsInProgress.incrementAndGet();
                        updateRemoteReactionTable( 0, (response, responseBody, requestName) -> {
                            // Error handling?
                            if(remoteCallsInProgress.decrementAndGet() == 0){
                                SynchronizeLocalDB.syncDB(fragment.getActivity(), (success) ->{});
                            }
                        });
                        // We then update the visual amount and status.
                        postData.userReaction = 0;
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
                            // NO DATABASE HERE
                            buttons[savedUserReaction - 1].setText(counts[savedUserReaction - 1] + " " + names[savedUserReaction - 1]);
                            setButtonInactive(buttons[savedUserReaction - 1]);
                        }
                        // As we change the saved user reaction here, we do it after checking/handling the already pressed button (if there is one)
                        postData.userReaction = buttonNumber;
                        clickedButton.setText((buttonCount + 1) + " " + buttonName);
                        setButtonActive(buttons[thisButtonType]);
                        // Now we update remote
                        remoteCallsInProgress.incrementAndGet();
                        updateRemoteReactionTable(buttonNumber, (response, responseBody, requestName) -> {
                            // Error handling?
                            if(remoteCallsInProgress.decrementAndGet() == 0){
                                SynchronizeLocalDB.syncDB(fragment.getActivity(), (success) ->{});
                            }
                        });
                    }
                }
            });
        }
    }

    /*
     * Sets a button as active
     * It is purely cosmetic
     * */
    public void setButtonActive(Button button){

    }

    /*
     * Sets a button as inactive
     * It is purely cosmetic
     * */
    public void setButtonInactive(Button button){

    }

    private void updateRemoteReactionTable(int newReactionType, HttpRequest.HttpRequestResponse requestResponse){
        Date userReactionTimestamp = postData.stamp;
        Reaction reaction = new Reaction(loggedUserID, postData.post.id, newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + loggedUserID);
        Log.d(TAG, "updateRemoteReactionTable: " + postData.post.id);
        Log.d(TAG, "updateRemoteReactionTable: " + newReactionType);
        Log.d(TAG, "updateRemoteReactionTable: " + userReactionTimestamp);
        if(userReactionTimestamp != null){
            // Update action
            reaction.stamp = userReactionTimestamp;
        } else {
            long stamp = System.currentTimeMillis();
            reaction.stamp = new Date(stamp);
            postData.post.stamp = reaction.stamp;
        }
        RemoteDBRequest.reaction(fragment.getActivity(), ( userReactionTimestamp != null ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT),
                reaction, requestResponse);
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

    @Override
    public int getItemCount() {
        return commentsForPost.size() + 2;
    }



}
