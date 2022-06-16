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
    private List<CommentWithUserName> commentsForPost;

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
        getDataForViews();
        return binding.getRoot();
    }

    private void getDataForViews(){
        // We cannot send the data we need
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        new DatabaseRequest<PostWithReactions>(getActivity(), (postResult) -> {
            postData = postResult;
            // We also need all comments for this post
            new DatabaseRequest<List<CommentWithUserName>>(getActivity(), (commentsResult) -> {
                commentsForPost = commentsResult;
                // Now we can setup our views
                setupViewsWithData();
            }).runRequest(() -> db.commentDao().getByPostSortedDateDesc(post_id));
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

    }
}