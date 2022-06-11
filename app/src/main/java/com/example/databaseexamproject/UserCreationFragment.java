package com.example.databaseexamproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.databaseexamproject.databinding.FragmentUserCreationBinding;
import com.example.databaseexamproject.databinding.FragmentUserLoginBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserCreationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserCreationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String WANTED_USERNAME = "wantedUserName";

    private FragmentUserCreationBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;

    public UserCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment UserCreationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserCreationFragment newInstance(String param1) {
        UserCreationFragment fragment = new UserCreationFragment();
        Bundle args = new Bundle();
        args.putString(WANTED_USERNAME, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(WANTED_USERNAME);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toUserLoginButton.setOnClickListener( (v) -> {
            NavHostFragment.findNavController(UserCreationFragment.this)
                    .navigateUp();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}