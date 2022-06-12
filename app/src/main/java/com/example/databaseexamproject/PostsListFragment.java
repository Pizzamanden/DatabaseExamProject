package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.adapter.PostsListRecyclerViewAdapter;
import com.example.databaseexamproject.databinding.FragmentPostsListBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.dataobjects.BigFuckPost;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;
import com.example.databaseexamproject.room.dataobjects.PostReactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsListFragment extends Fragment {

    FragmentPostsListBinding binding;


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SAVED_RECYCLERVIEW_POSITION = "recyclerViewElementPosition";
    private static final String USER_ID = "userID";


    private int saved_recyclerview_position;
    private String user_id;

    public PostsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param saved_recyclerview_position Integer for recyclerview postion. Has a default of 0.
     * @return A new instance of fragment PostsListFragment.
     */
    public static PostsListFragment newInstance(int saved_recyclerview_position, String user_id) {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        args.putInt(SAVED_RECYCLERVIEW_POSITION, saved_recyclerview_position);
        args.putString(USER_ID, user_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            saved_recyclerview_position = getArguments().getInt(SAVED_RECYCLERVIEW_POSITION);
            user_id = getArguments().getString(USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        updateLoadingStatus(true);
        getDataForRecyclerView();
        return binding.getRoot();
    }

    public void getDataForRecyclerView(){
        // We must first, async, get the data for the recyclerview
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<List<BigFuckPost>> databaseRequest = new DatabaseRequest<>(getActivity(), this::onDatabaseRequestResponse);
        databaseRequest.runRequest(() -> db.postDao().bigFuck(user_id));
    }

    public void onDatabaseRequestResponse(List<BigFuckPost> posts){
        // Now that we have the content for the RecyclerView, we can fill our adapter

        // Find our RecyclerView our recyclerView
        RecyclerView recyclerView = binding.recyclerviewPostsList;
        // Create a layout manager for the recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create and attach our adapter
        recyclerView.setAdapter(new PostsListRecyclerViewAdapter(PostsListFragment.this, posts));
        linearLayoutManager.scrollToPosition(saved_recyclerview_position);

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