package com.example.databaseexamproject;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.databaseexamproject.databinding.FragmentUserCreationBinding;
import com.example.databaseexamproject.databinding.FragmentUserLoginBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.dataobjects.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserCreationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserCreationFragment extends Fragment {


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String WANTED_USERNAME = "wantedUserName";

    private FragmentUserCreationBinding binding;

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

        final EditText useridEditText = binding.userid;
        final EditText fullNameEditText = binding.fullName;

        binding.toUserLoginButton.setOnClickListener( (v) -> {
            AppDatabase db = Room.databaseBuilder(getActivity(), AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();
            db.userDao().insertAll(new User(useridEditText.getText().toString(), fullNameEditText.getText().toString()));
            Log.d("UserCreationFragment", "User created");

            try {
                synchronizeWithRemoteDB();
            } catch (IOException e) {
                e.printStackTrace();
            }

            NavHostFragment.findNavController(UserCreationFragment.this)
                    .navigateUp();
        });
    }

    private void synchronizeWithRemoteDB() throws IOException {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserCreationBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }
}