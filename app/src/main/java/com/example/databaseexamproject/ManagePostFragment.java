package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import com.example.databaseexamproject.databinding.FragmentManagePostBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.User;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManagePostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManagePostFragment extends Fragment {

    // Fragment argument names
    private static final String USER_ID = "existingPost_userID";
    private static final String EXISTING_POST = "isExistingPost";
    private static final String POST_ID = "existingPost_id";
    private static final String POST_CONTENT = "existingPost_content";


    // Fragment argument parameters
    private String user_id;
    private boolean isExistingPost;
    private int post_id;
    private String post_content;


    // Binding
    private FragmentManagePostBinding binding;

    private PostsListActivity parentActivity;
    private String loggedUserID;

    public ManagePostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ManagePostFragment.
     */
    public static ManagePostFragment newInstance(String user_id, boolean isExistingPost, int post_id, String post_content) {
        ManagePostFragment fragment = new ManagePostFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, user_id);
        args.putBoolean(EXISTING_POST, isExistingPost);
        args.putInt(POST_ID, post_id);
        args.putString(POST_CONTENT, post_content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            user_id = getArguments().getString(USER_ID);
            isExistingPost = getArguments().getBoolean(EXISTING_POST);
            post_id = getArguments().getInt(POST_ID);
            post_content = getArguments().getString(POST_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        PostsListActivity parentActivity = (PostsListActivity) getActivity();
        if(parentActivity != null){
            if(isExistingPost){
                parentActivity.getSupportActionBar().setTitle("Edit your post");
            } else {
                parentActivity.getSupportActionBar().setTitle("Create a new post");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: ViewPostFragment: " + loggedUserID);
        inflater.inflate(R.menu.only_logout_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.item1){
            loggedUserID = null;
            syncAndAuth(true);
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
        binding = FragmentManagePostBinding.inflate(inflater, container, false);
        setupViews();
        return binding.getRoot();
    }

    private void setupViews(){
        syncAndAuth(false);

        if(isExistingPost) {
            // Set the header and button to reflect this is an edit
            binding.buttonSubmitAction.setText(R.string.editPostButton);
            // And fill the editText with the current content
            String content = post_content;
            if(content == null){
                content = "";
            }
            int[] imageURLLocation = textContainsImageURL(content);
            if(imageURLLocation[1] != 0){
                // Now, get the image url
                String imageURL = content.substring(imageURLLocation[0], imageURLLocation[1]);
                // And then set it into the textbox
                binding.editTextImageURL.setText(imageURL);
                // Remove the Url from the String, and continue as we were
                content = content.substring(0, imageURLLocation[0]) + content.substring(imageURLLocation[1]);
            }
            // While loop to remove spaces in front of text
            while(content.length() > 0 && content.charAt(0) == ' '){
                content = content.substring(1);
            }
            binding.editTextPostContent.setText(content);
        }
        binding.editTextImageURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // We check if the image is a valid URL
                String maybeURL = binding.editTextImageURL.getText().toString();
                int[] imageStringLocation = textContainsImageURL(maybeURL);
                if(!(maybeURL.equals("")) && imageStringLocation[0] == 0 && imageStringLocation[1] == 0){
                    binding.editTextImageURL.setError("Text does not contain an allowed image URL");
                    binding.buttonSubmitAction.setEnabled(false);
                } else {
                    binding.buttonSubmitAction.setEnabled(true);
                }
            }
        });
        // Set our listener on our submit button
        binding.buttonSubmitAction.setOnClickListener((v) -> {
            String imageURL = "";
            imageURL = binding.editTextImageURL.getText().toString();
            String postContent = binding.editTextPostContent.getText().toString();
            submitPost(imageURL + " " + postContent);
        });
    }



    private void submitPost(String content){
        // Now we have the content from our textEdit, and we must do the SQL thing
        int idToUse;
        if(!isExistingPost){
            // Insert action, create a new ID!
            long stamp = System.currentTimeMillis();
            long post_generated_id = loggedUserID.hashCode() + stamp;
            idToUse = (int) post_generated_id;
        } else {
            // Update action, we use the same ID as before
            idToUse = post_id;
        }
        Post post = new Post(idToUse, loggedUserID, content);
        Log.d(TAG, "submitPost: ID: " + post.id);
        Log.d(TAG, "submitPost: Content: " + content);
        Log.d(TAG, "submitPost: UserID: " + loggedUserID);

        RemoteDBRequest.post(getContext(), (isExistingPost ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT), post, (response, responseBody, requestName) -> {
            // We now sync the local database, to make sure it reflects the new changes!
            SynchronizeLocalDB.syncDB(getContext(),(success) -> {});
            // We can check the response of our action here, and handle errors
            if(response.code() == 201 || response.code() == 204){
                // The insert or update was successful!
                Toast.makeText(getActivity(), (isExistingPost ? R.string.post_changed : R.string.post_created), Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(ManagePostFragment.this)
                        .navigateUp();
            } else {
                // Something went wrong
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                refreshFragment();
            }
        });
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

    public void refreshFragment(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.detach(this);
        transaction.commit();
        FragmentTransaction transaction2 = getParentFragmentManager().beginTransaction();
        transaction2.attach(this);
        transaction2.commit();
    }

    public void syncAndAuth(boolean logoutOnPurpose){
        // First we sync
        SynchronizeLocalDB.syncDB(getActivity(), (success -> {}));

        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
        // Check our logged in user exists
        User thisUser = db.userDao().findByName(loggedUserID);
        db.close();
        if(thisUser == null){
            // Our user is not logged in / does not exists
            Intent intent = new Intent(getActivity(), com.example.databaseexamproject.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if(!logoutOnPurpose){
                Toast.makeText(getActivity(), "Authentication failed, please login again", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "You are now logged out", Toast.LENGTH_LONG).show();
            }
            getActivity().finish();
        }
    }
}