package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.databaseexamproject.databinding.FragmentEditCommentBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.User;
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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            post_id = getArguments().getInt(POST_ID);
            comment_text = getArguments().getString(COMMENT_TEXT);
            comment_id = getArguments().getInt(COMMENT_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(parentActivity != null){
            parentActivity.getSupportActionBar().setTitle("Edit you comment");
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
        // Inflate the layout for this fragment
        binding = FragmentEditCommentBinding.inflate(inflater, container, false);
        parentActivity = (PostsListActivity) getActivity();
        loggedUserID = parentActivity.getUserID();
        syncAndAuth(false);
        // Set comment text into edit text box
        binding.editTextCommentEditText.setText(comment_text);
        // Attach listener to delete the comment
        binding.buttonSubmitDeleteComment.setOnClickListener((v -> {
            Log.d(TAG, "onCreateView: Deleting comment with ID: " + comment_id + " for post with ID: " + post_id);
            // Now we sync, then we go back.

            RemoteDBRequest.deletePost(getActivity(), comment_id, () -> {
                Toast.makeText(getActivity(), R.string.comment_deleted, Toast.LENGTH_LONG).show();
                syncAndNavigateUp();
            });
        }));
        // Attach listener to edit the comment into what it says now
        binding.buttonSubmitEditComment.setOnClickListener((v) -> {
            Log.d(TAG, "onCreateView: Editing comment with ID: " + comment_id + " for post with ID: " + post_id);
            String content = "forPost:" + post_id + " " + binding.editTextCommentEditText.getText();
            Log.d(TAG, "onCreateView: New content string: " + content);
            RemoteDBRequest.post(getActivity(), RemoteDBRequest.QUERY_TYPE_UPDATE, new Post(comment_id, loggedUserID, content), (response, responseBody, requestName) -> {
                // Now we sync, then we go back.
                Toast.makeText(getActivity(), R.string.comment_changed, Toast.LENGTH_LONG).show();
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