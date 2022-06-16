package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.databaseexamproject.adapter.CommentForPostRecyclerViewAdapter;
import com.example.databaseexamproject.adapter.PostsListRecyclerViewAdapter;
import com.example.databaseexamproject.databinding.FragmentViewPostBinding;
import com.example.databaseexamproject.room.AppDatabase;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPostFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POST_ID = "sentData_post_id";
    private static final String USER_ID = "sentData_user_id";

    // Parent activity and the userID
    private PostsListActivity parentActivity;
    private String loggedUserID;

    // Our data for this post
    private PostWithReactions postData;

    // Integer counter for remote calls in progress
    AtomicInteger remoteCallsInProgress = new AtomicInteger(0);

    // Fragment data values
    private int post_id;
    private String user_id;

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
    public static ViewPostFragment newInstance(int post_id, String user_id) {
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putInt(POST_ID, post_id);
        args.putString(USER_ID, user_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            post_id = getArguments().getInt(POST_ID);
            user_id = getArguments().getString(USER_ID);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: " + loggedUserID);
        if(loggedUserID.equals(user_id)){
            inflater.inflate(R.menu.view_post_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.item1){
            RemoteDBRequest.deletePost(getActivity(),post_id, () -> {
                Log.d(TAG, "onOptionsItemSelected: We are now done with the deletion");
                SynchronizeLocalDB.syncDB(getActivity(), (success -> {}));
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = (PostsListActivity) getActivity();
        loggedUserID = parentActivity.getUserID();
        // Inflate the layout for this fragment
        binding = FragmentViewPostBinding.inflate(inflater, container, false);
        binding.buttonSubmitComment.setOnClickListener((v)->{
            // Create content
            String content = "forPost:" + post_id + " " + binding.editTextCommentCreation.getText();
            long stamp = System.currentTimeMillis();
            Long post_generated_id = loggedUserID.hashCode() + stamp;
            int idToUse = post_generated_id.intValue();
            Post post = new Post(idToUse, loggedUserID, content);
            RemoteDBRequest.post(getActivity(), RemoteDBRequest.QUERY_TYPE_INSERT, post, (response, responseBody, requestName) ->{
                SynchronizeLocalDB.syncDB(getActivity(), (success)->{
                    Log.d(TAG, "onCreateView: Comment Created!");
                    parentActivity.recreate();
                });
            });
        });
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
        databaseRequest.runRequest(() -> db.postDao().getSpecificPostWithReactionByUserAndAllReactionsCounter(loggedUserID, post_id));

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
        if(postData.name == null){
            binding.include.textViewUserName.setText(postData.post.user_id);
        } else {
            binding.include.textViewUserName.setText(postData.name);
        }
        String content = postData.post.content;
        int[] imageURLLocation = textContainsImageURL(content);
        if(imageURLLocation[1] != 0){
            binding.include.imageViewContentImage.setVisibility(View.VISIBLE);
            // Now, get the image url
            String imageURL = content.substring(imageURLLocation[0], imageURLLocation[1]);
            // We setup the image download and showing process immediately
            new ImageDownload(binding.include.imageViewContentImage).execute(imageURL);
            Log.d(TAG, "onBindViewHolder: " + imageURL);
            Log.d(TAG, "onBindViewHolder: On post ID: " + postData.post.id);
            // Remove the Url from the String, and continue as we were
            content = content.substring(0, imageURLLocation[0]) + content.substring(imageURLLocation[1]);
        } else {
            binding.include.imageViewContentImage.setVisibility(View.GONE);
        }
        // While loop to remove spaces in front of text
        while(content.charAt(0) == ' ' && content.length() > 1){
            content = content.substring(1);
        }
        Log.d(TAG, "onBindViewHolder: " + content);
        if(content != null && content.length() > 200){
            binding.include.textViewPostText.setText(content.substring(0, 200));
        } else {
            binding.include.textViewPostText.setText(content);
        }

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

        // Now we fill in the comments
        getAndInsertComments();
    }

    private void getAndInsertComments() {
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<List<CommentWithUserName>> databaseRequest = new DatabaseRequest<>(getActivity(), (result) -> {
            RecyclerView recyclerView = binding.recyclerViewCommentsForPost;
            // Create a layout manager for the recyclerView
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);

            // Create and attach our adapter
            recyclerView.setAdapter(new CommentForPostRecyclerViewAdapter(this, result, loggedUserID));

        });
        databaseRequest.runRequest(() -> db.commentDao().getByPostSortedDateDesc(post_id));
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
                                SynchronizeLocalDB.syncDB(getActivity(), (success) ->{});
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
                                SynchronizeLocalDB.syncDB(getActivity(), (success) ->{});
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