package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.databaseexamproject.adapter.PostsListRecyclerViewAdapter;
import com.example.databaseexamproject.databinding.FragmentPostsListBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsListFragment extends Fragment {

    FragmentPostsListBinding binding;


    private PostsListActivity parentActivity;
    private String loggedUserID;

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
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate Fragment: called");
        SynchronizeLocalDB.syncDB(getActivity(), (success) -> {});
    }

    @Override
    public void onResume() {
        super.onResume();

        if(parentActivity != null){
            parentActivity.getSupportActionBar().setTitle("Viewing all posts");
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: " + loggedUserID);

        inflater.inflate(R.menu.post_list_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.item1){
            Log.d(TAG, "onOptionsItemSelected: called");
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.detach(this);
            transaction.commit();
            FragmentTransaction transaction2 = getParentFragmentManager().beginTransaction();
            transaction2.attach(this);
            transaction2.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentActivity = (PostsListActivity) getActivity();
        loggedUserID = parentActivity.getUserID();
        Log.d(TAG, "onCreateView: User ID is: " + loggedUserID);
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        updateLoadingStatus(true);

        getDataForRecyclerView();

        return binding.getRoot();
    }

    public void getDataForRecyclerView(){
        // We must first, async, get the data for the recyclerview
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<List<PostWithReactions>> databaseRequest = new DatabaseRequest<>(getActivity(), (result) -> {
            db.close();
            onDatabaseRequestResponse(result);
        });
        databaseRequest.runRequest(() -> db.postDao().getAllPostsWithReactionByUserAndAllReactionsCounter(loggedUserID));
    }

    public void setRememberRecyclerViewPosition(int recyclerViewPosition){
        parentActivity.setRecyclerViewPosition(recyclerViewPosition);
    }

    public void onDatabaseRequestResponse(List<PostWithReactions> posts){
        // Now that we have the content for the RecyclerView, we can fill our adapter

        // Find our RecyclerView our recyclerView
        RecyclerView recyclerView = binding.recyclerviewPostsList;
        // Create a layout manager for the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create and attach our adapter
        recyclerView.setAdapter(new PostsListRecyclerViewAdapter(PostsListFragment.this, posts, loggedUserID));
        linearLayoutManager.scrollToPosition((parentActivity.getRecyclerViewPosition() > posts.size() ? 0 : parentActivity.getRecyclerViewPosition()));

        binding.fabNewPost.setOnClickListener((v) -> {
            parentActivity.setRecyclerViewPosition(linearLayoutManager.findFirstVisibleItemPosition());
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
            binding.fabNewPost.setVisibility(View.GONE);
        } else {
            binding.recyclerviewPostsList.setVisibility(View.VISIBLE);
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.fabNewPost.setVisibility(View.VISIBLE);
        }
    }


}