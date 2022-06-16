package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.databinding.FragmentEditCommentBinding;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCommentFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POST_ID = "sentData_post_id";
    private static final String COMMENT_TEXT = "sentData_comment_text";
    private static final String COMMENT_ID = "sentData_comment_id";

    // Parent activity and the userID
    private PostsListActivity parentActivity;
    private String loggedUserID;

    private int post_id;
    private String comment_text;
    private int comment_id;

    // Binding
    FragmentEditCommentBinding binding;

    public EditCommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditCommentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCommentFragment newInstance(int post_id, String commentText, int comment_id) {
        EditCommentFragment fragment = new EditCommentFragment();
        Bundle args = new Bundle();
        args.putInt(POST_ID, post_id);
        args.putString(COMMENT_TEXT, commentText);
        args.putInt(COMMENT_ID, comment_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post_id = getArguments().getInt(POST_ID);
            comment_text = getArguments().getString(COMMENT_TEXT);
            comment_id = getArguments().getInt(COMMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditCommentBinding.inflate(inflater, container, false);
        parentActivity = (PostsListActivity) getActivity();
        loggedUserID = parentActivity.getUserID();
        // Set comment text into edit text box
        binding.editTextCommentEditText.setText(comment_text);
        // Attach listener to delete the comment
        binding.buttonSubmitDeleteComment.setOnClickListener((v -> {
            Log.d(TAG, "onCreateView: Deleting comment with ID: " + comment_id + " for post with ID: " + post_id);
            // Now we sync, then we go back.
            RemoteDBRequest.deletePost(getActivity(), comment_id, this::syncAndNavigateUp);
        }));
        // Attach listener to edit the comment into what it says now
        binding.buttonSubmitEditComment.setOnClickListener((v) -> {
            Log.d(TAG, "onCreateView: Editing comment with ID: " + comment_id + " for post with ID: " + post_id);
            String content = "forPost:" + post_id + " " + binding.editTextCommentEditText.getText();
            Log.d(TAG, "onCreateView: New content string: " + content);
            RemoteDBRequest.post(getActivity(), RemoteDBRequest.QUERY_TYPE_UPDATE, new Post(comment_id, loggedUserID, content), (response, responseBody, requestName) -> {
                // Now we sync, then we go back.
                syncAndNavigateUp();
            });
        });
        return binding.getRoot();
    }
    private void syncAndNavigateUp(){
        Log.d(TAG, "syncAndNavigateUp: Called");
        SynchronizeLocalDB.syncDB(getActivity(), (success) -> {});
        NavHostFragment.findNavController(EditCommentFragment.this)
                .navigateUp();
    }
}