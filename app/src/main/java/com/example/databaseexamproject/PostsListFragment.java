package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.adapter.PostsListRecyclerViewAdapter;
import com.example.databaseexamproject.databinding.FragmentPostsListBinding;
import com.example.databaseexamproject.login.UserLoginFragment;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsListFragment extends Fragment {

    FragmentPostsListBinding binding;


    private PostsListActivity parentActivity;
    private String loggedInUserID;

    public PostsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostsListFragment.
     */
    public static PostsListFragment newInstance() {
        PostsListFragment fragment = new PostsListFragment();
        Log.d(TAG, "newInstance Fragment: called");
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Fragment: called");
        if (getArguments() != null) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(parentActivity != null){
            parentActivity.getSupportActionBar().setTitle("Viewing all posts");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentActivity = (PostsListActivity) getActivity();
        loggedInUserID = parentActivity.getUserID();
        Log.d(TAG, "onCreateView: User ID is: " + loggedInUserID);
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        updateLoadingStatus(true);

        getDataForRecyclerView();

        return binding.getRoot();
    }

    public void getDataForRecyclerView(){
        // We must first, async, get the data for the recyclerview
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<List<PostWithReactions>> databaseRequest = new DatabaseRequest<>(getActivity(), this::onDatabaseRequestResponse);
        databaseRequest.runRequest(() -> db.postDao().getAllPostsWithReactionByUserAndAllReactionsCounter(loggedInUserID));
    }

    public void setRememberRecyclerViewPosition(int recyclerViewPosition){
        parentActivity.setReyclerViewPosition(recyclerViewPosition);
    }

    public void onDatabaseRequestResponse(List<PostWithReactions> posts){
        // Now that we have the content for the RecyclerView, we can fill our adapter

        // Find our RecyclerView our recyclerView
        RecyclerView recyclerView = binding.recyclerviewPostsList;
        // Create a layout manager for the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create and attach our adapter
        recyclerView.setAdapter(new PostsListRecyclerViewAdapter(PostsListFragment.this, posts, loggedInUserID));
        linearLayoutManager.scrollToPosition((parentActivity.getReyclerViewPosition() > posts.size() ? 0 : parentActivity.getReyclerViewPosition()));

        binding.fabNewPost.setOnClickListener((v) -> {
            parentActivity.setReyclerViewPosition(linearLayoutManager.findFirstVisibleItemPosition());
            NavHostFragment.findNavController(PostsListFragment.this)
                    .navigate(R.id.action_postsListFragment_to_managePostFragment);
        });

        updateLoadingStatus(false);
    }

    public void updateLoadingStatus(boolean isLoading){
        // We hide relevant views, and show other relevant views, based on loading status
        if(isLoading){
            binding.loadingIndicator.setVisibility(View.VISIBLE);
            binding.recyclerviewPostsList.setVisibility(View.GONE);
        } else {
            binding.recyclerviewPostsList.setVisibility(View.VISIBLE);
            binding.loadingIndicator.setVisibility(View.GONE);
        }
    }


}