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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.databaseexamproject.adapter.CommentForPostRecyclerViewAdapter;
import com.example.databaseexamproject.databinding.FragmentViewPostBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.CommentWithUserName;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.User;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPostFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POST_ID = "sentData_post_id";
    private static final String USER_ID = "sentData_user_id";
    private static final String USER_NAME = "sentData_user_name";


    // Parent activity and the userID
    private PostsListActivity parentActivity;
    private String loggedUserID;

    // Our data for this post
    private PostWithReactions postData;
    private List<CommentWithUserName> commentsForPost;

    // Fragment data values
    private int post_id;
    private String user_id;
    private String user_name;

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
    public static ViewPostFragment newInstance(int post_id, String user_id, String user_name) {
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putInt(POST_ID, post_id);
        args.putString(USER_ID, user_id);
        args.putString(USER_NAME, user_name);
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
            user_name = getArguments().getString(USER_NAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(parentActivity != null){
            if(user_name != null){
                parentActivity.getSupportActionBar().setTitle("Post by " + user_name);
            } else {
                parentActivity.getSupportActionBar().setTitle("Viewing post");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: ViewPostFragment: " + loggedUserID);
        if(loggedUserID.equals(user_id)){
            Log.d(TAG, "onCreateOptionsMenu: ViewPostFragment: Is the owner");
            inflater.inflate(R.menu.view_post_owner_menu, menu);
        } else {
            Log.d(TAG, "onCreateOptionsMenu: ViewPostFragment: Not the owner");
            inflater.inflate(R.menu.view_post_notowner_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.item1){
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.detach(this);
            transaction.commit();
            FragmentTransaction transaction2 = getParentFragmentManager().beginTransaction();
            transaction2.attach(this);
            transaction2.commit();
            return true;
        } else if(id == R.id.item2){
            RemoteDBRequest.deletePost(getActivity(),post_id, () -> {
                Log.d(TAG, "onOptionsItemSelected: We are now done with the deletion");
                SynchronizeLocalDB.syncDB(getActivity(), (success -> {}));
                // Then we go back
                Toast.makeText(getActivity(), "Deletion successful!", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(ViewPostFragment.this)
                        .navigateUp();
            });
            return true;
        } else if(id == R.id.logutMenuButton){
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
        binding = FragmentViewPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDataForViews();
    }

    public void getDataForViews(){
        updateLoadingStatus(true);
        syncAndAuth(false);

        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();

        new DatabaseRequest<PostWithReactions>(getActivity(), (postResult) -> {
            postData = postResult;
            // If the post got deleted while we were viewing it, we need to know
            if(postData != null) {
                // We also need all comments for this post
                new DatabaseRequest<List<CommentWithUserName>>(getActivity(), (commentsResult) -> {
                    commentsForPost = commentsResult;
                    // Now we can setup our views
                    db.close();
                    setupViewsWithData();
                }).runRequest(() -> db.commentDao().getCommentsByPostSortedDateDesc(post_id));
            } else {
                // Our post got deleted while viewing it, or something?
                // Kick our user back to postsList
                Toast.makeText(getActivity(), "The post could not be found", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(ViewPostFragment.this)
                        .navigateUp();
            }
        }).runRequest(() -> db.postDao().getSpecificPostWithReactionByUserAndAllReactionsCounter(loggedUserID, post_id));
    }

    private void setupViewsWithData() {

        // Make the user able to edit this post, if they own it
        if (postData.post.user_id.equals(loggedUserID)) { // The user logged in is the same as the owner of this post!
            binding.fabEditPost.setOnClickListener(v -> {
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
        // Setup the recycler view
        RecyclerView recyclerView = binding.recyclerviewSinglePostAndComments;
        // Create a layout manager for the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create and attach our adapter
        recyclerView.setAdapter(new CommentForPostRecyclerViewAdapter(ViewPostFragment.this, commentsForPost, postData, loggedUserID));
        updateLoadingStatus(false);
    }

    public void updateLoadingStatus(boolean isLoading){
        // We hide relevant views, and show other relevant views, based on loading status
        if(isLoading){
            binding.loadingIndicatorViewPost.setVisibility(View.VISIBLE);
            binding.fabEditPost.setVisibility(View.GONE);
            binding.recyclerviewSinglePostAndComments.setVisibility(View.GONE);
        } else {
            binding.fabEditPost.setVisibility(View.VISIBLE);
            binding.recyclerviewSinglePostAndComments.setVisibility(View.VISIBLE);
            binding.loadingIndicatorViewPost.setVisibility(View.GONE);
        }
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