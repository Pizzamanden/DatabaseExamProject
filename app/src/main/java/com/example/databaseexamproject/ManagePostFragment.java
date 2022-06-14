package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.databinding.FragmentManagePostBinding;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

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
        if (getArguments() != null) {
            user_id = getArguments().getString(USER_ID);
            isExistingPost = getArguments().getBoolean(EXISTING_POST);
            post_id = getArguments().getInt(POST_ID);
            post_content = getArguments().getString(POST_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = (PostsListActivity) getActivity();
        // Inflate the layout for this fragment
        binding = FragmentManagePostBinding.inflate(inflater, container, false);
        setupViews();
        return binding.getRoot();
    }

    private void setupViews(){
        if(isExistingPost) {
            // Set the header and button to reflect this is an edit
            binding.textViewManagePostStatus.setText(R.string.editPostHeader);
            binding.buttonSubmitAction.setText(R.string.editPostButton);
            // And fill the editText with the current content
            binding.editTextPostContent.setText(post_content);
        }
        // Set our listener on our submit button
        binding.buttonSubmitAction.setOnClickListener((v)->{
            String postContent = binding.editTextPostContent.getText().toString();
            //submitPost(postContent);
        });
    }

    private void submitPost(String content){
        // TODO TEST AND FINISH
        Log.d(TAG, "submitPost: Content submitted was: " + content);
        // Now we have the content from our textEdit, and we must do the SQL thing
        int idToUse;
        if(!isExistingPost){
            // Insert action
            long stamp = System.currentTimeMillis();
            long post_generated_id = user_id.hashCode() + stamp;
            idToUse = (int) post_generated_id;
        } else {
            // Update action
            idToUse = post_id;
        }
        Post post = new Post(idToUse, content, user_id);

        RemoteDBRequest.post(getContext(), (isExistingPost ? RemoteDBRequest.QUERY_TYPE_UPDATE : RemoteDBRequest.QUERY_TYPE_INSERT),
                post, (response, responseBody, requestName) -> {
            // TODO this is where we continue (on main thread)
            SynchronizeLocalDB.syncDB(getContext(),(success) -> {

            });
            NavHostFragment.findNavController(ManagePostFragment.this)
                    .navigateUp();
        });
    }
}