package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.databaseexamproject.databinding.FragmentViewPostBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPostFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POST_ID = "sentData_post_id";

    // Parent activity and the userID
    private PostsListActivity parentActivity;
    private String loggedUserID;

    // Our data for this post
    private PostWithReactions postData;

    // Fragment data values
    private int post_id;

    // Binding
    FragmentViewPostBinding binding;

    public ViewPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewPostFragment.
     */
    public static ViewPostFragment newInstance(int post_id) {
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putInt(POST_ID, post_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post_id = getArguments().getInt(POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = (PostsListActivity) getActivity();
        loggedUserID = parentActivity.getUserID();
        // Inflate the layout for this fragment
        binding = FragmentViewPostBinding.inflate(inflater, container, false);
        setDataToViews();
        return binding.getRoot();
    }

    private void setDataToViews(){
        // We cannot send the data we need
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<PostWithReactions> databaseRequest = new DatabaseRequest<>(getActivity(), (result) -> {
            postData = result;
            // Now we have the data to setup our other views
            setupViewsWithData();
        });
        databaseRequest.runRequest(() -> db.postDao().getSpecificPostWithReactionByUserAnAllReactionsCounter(loggedUserID, post_id));

    }

    private void setupViewsWithData(){
        if(postData.post.user_id.equals(loggedUserID)){ // The user logged in is the same as the owner of this post!
            binding.fabEditPost.setOnClickListener(v->{
                Log.d(TAG, "setupViewsWithData: click");
                Bundle args = new Bundle();
                args.putString("existingPost_userID", postData.post.user_id);
                args.putBoolean("isExistingPost", true);
                args.putInt("existingPost_id", post_id);
                args.putString("existingPost_content", postData.post.content);
                NavHostFragment.findNavController(ViewPostFragment.this)
                        .navigate(R.id.action_viewPostFragment_to_managePostFragment, args);
            });
        } else {
            binding.fabEditPost.setVisibility(View.GONE);
        }
        // TODO handle images here also
        binding.include.textViewUserName.setText(postData.name);
        binding.include.textViewPostText.setText(postData.post.content);

        // Setup reaction buttons
        Button[] buttons = {
                binding.include.buttonLikeReact,
                binding.include.buttonDislikeReact,
                binding.include.buttonAmbivalentReact
        };
        int[] counts = {
                postData.type1Reactions,
                postData.type2Reactions,
                postData.type3Reactions
        };
        String[] names = {
                getString(R.string.likeReact),
                getString(R.string.dislikeReact),
                getString(R.string.ambivalenceReact)
        };
        boolean[] isReacted = {
                postData.userReaction == 1,
                postData.userReaction == 2,
                postData.userReaction == 3
        };
        stylePostButtons(buttons, counts, names, isReacted);
    }

    private void stylePostButtons(Button[] buttons, int[] counts, String[] names, boolean[] isReacted){

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
                final public String buttonName = names[thisButtonType]; // What the applicable textString is for this type
                final public int buttonCount = counts[thisButtonType] - ( isReacted[thisButtonType] ? 1 : 0); // What the default value is
                final public int buttonNumber = thisButtonType + 1; // The actual integer representation in the database: 0 = deleted, 1 = like, 2 = displike, 3 = meh

                @Override
                public void onClick(View v) {
                    Button clickedButton = (Button) v;
                    int savedUserReaction = postData.userReaction;
                    if(savedUserReaction == buttonNumber){
                        // This button should have had been active
                        // This is the easy case. We had reacted this reaction, and now we want to remove it.
                        // First we launch a database request. (we do not wait for a response)
                        updateRemoteReactionTable( 0, (response, responseBody, requestName) -> {
                            // Error handling?
                            SynchronizeLocalDB.syncDB(getActivity(), (success) ->{});
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
                            buttons[savedUserReaction - 1].setText(buttonCount + " " + names[savedUserReaction - 1]);
                            setButtonInactive(buttons[savedUserReaction - 1]);
                        }
                        // As we change the saved user reaction here, we do it after checking/handling the already pressed button (if there is one)
                        postData.userReaction = buttonNumber;
                        clickedButton.setText((buttonCount + 1) + " " + buttonName);
                        setButtonActive(buttons[thisButtonType]);
                        // Now we update remote
                        updateRemoteReactionTable(buttonNumber, (response, responseBody, requestName) -> {
                            // Error handling?
                            SynchronizeLocalDB.syncDB(getActivity(), (success) ->{});
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
            postData.stamp = reaction.stamp;
        }
        RemoteDBRequest.reaction(getActivity(), ( userReactionTimestamp != null ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT),
                reaction, requestResponse);
    }
}