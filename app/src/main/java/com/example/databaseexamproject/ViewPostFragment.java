package com.example.databaseexamproject;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.databinding.FragmentViewPostBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_ID = "sentData_user_id";
    private static final String POST_ID = "sentData_post_id";
    private static final String POST_CONTENT = "sentData_content";
    private static final String USER_NAME = "sentData_user_name";
    private static final String SAVED_RECYCLERVIEW_POSITION = "recyclerViewElementPosition";

    // TODO: Rename and change types of parameters
    private String user_id;
    private int post_id;
    private String post_content;
    private String user_name;
    private int saved_recyclerview_position;

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
    // TODO: Rename and change types and number of parameters
    public static ViewPostFragment newInstance(String user_id, int post_id, String post_content, String user_name, int saved_recyclerview_position) {
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, user_id);
        args.putInt(POST_ID, post_id);
        args.putString(POST_CONTENT, post_content);
        args.putString(USER_NAME, user_name);
        args.putInt(SAVED_RECYCLERVIEW_POSITION, saved_recyclerview_position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user_id = getArguments().getString(USER_ID);
            post_id = getArguments().getInt(POST_ID);
            post_content = getArguments().getString(POST_CONTENT);
            user_name = getArguments().getString(USER_NAME);
            saved_recyclerview_position = getArguments().getInt(SAVED_RECYCLERVIEW_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewPostBinding.inflate(inflater, container, false);
        setDataToViews();
        return binding.getRoot();
    }

    private void setDataToViews(){
        binding.toPostsListButton.setOnClickListener(v->{
            // TODO the back button in toolbar and on the phone itself does not trigger this!
            Bundle args = new Bundle();
            args.putInt("recyclerViewElementPosition", saved_recyclerview_position);
            NavHostFragment.findNavController(ViewPostFragment.this)
                    .navigate(R.id.action_viewPostFragment_to_postsListFragment, args);

            // NavHostFragment.findNavController(ViewPostFragment.this).navigateUp();
        });
        binding.toManagePost.setOnClickListener(v->{
            // TODO manage a post :3
        });
        binding.textView.setText(post_content);
    }
}