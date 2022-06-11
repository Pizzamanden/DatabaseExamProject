package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.nfc.Tag;
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
import com.example.databaseexamproject.databinding.FragmentUserLoginBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.Post;
import com.example.databaseexamproject.room.User;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsListFragment extends Fragment {

    FragmentPostsListBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PostsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostsListFragment newInstance(String param1, String param2) {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        updateLoadingStatus(true);
        initRecyclerView();
        return binding.getRoot();
    }

    public void initRecyclerView(){
        // We must first, async, get the data for the recyclerview
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        DatabaseRequest<List<Post>> request = new DatabaseRequest<List<Post>>(this.getActivity(), this::onDatabaseRequestResponse);
        // Ingen thread, bare kør
        request.runRequest(() -> db.postDao().getAllSortedDateDesc());

        Log.d(TAG, "benis");
        db.close();
    }

    public void onDatabaseRequestResponse(List<Post> posts){
        // Find our RecyclerView our recyclerView
        RecyclerView recyclerView = binding.recyclerviewPostsList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        // Create and attach our adapter
        recyclerView.setAdapter(new PostsListRecyclerViewAdapter(posts));
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