package com.example.databaseexamproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.databaseexamproject.databinding.FragmentUserCreationBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.User;
import com.example.databaseexamproject.webrequests.RemoteDBRequest;

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

    private MutableLiveData<CreationFormState> creationFormState = new MutableLiveData<>();

    LiveData<CreationFormState> getCreationFormState() {
        return creationFormState;
    }

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
        final Button toLoginButton = binding.toUserLoginButton;

        getCreationFormState().observe(getActivity(), CreationFormState -> {
            if (CreationFormState == null) {
                return;
            }
            toLoginButton.setEnabled(CreationFormState.isDataValid());
            if (CreationFormState.getUsernameError() != null) {
                useridEditText.setError(getString(CreationFormState.getUsernameError()));
            }
            if (CreationFormState.getNameError() != null) {
                fullNameEditText.setError(getString(CreationFormState.getNameError()));
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                creationDataChanged(useridEditText.getText().toString(),
                        fullNameEditText.getText().toString());
            }
        };

        useridEditText.addTextChangedListener(afterTextChangedListener);
        fullNameEditText.addTextChangedListener(afterTextChangedListener);


        toLoginButton.setOnClickListener( (v) -> {
            SynchronizeLocalDB.syncDB(getActivity(), (success) -> {});
            String username = useridEditText.getText().toString();
            AppDatabase db = Room.databaseBuilder(getActivity(), AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();
            User userExists = db.userDao().findByName(username);

            if (userExists == null) {
                User user = new User(username.trim(), fullNameEditText.getText().toString());
                db.userDao().insertAll(user);
                Log.d("UserCreationFragment", "User created");
                synchronizeWithRemoteDB(user);
            }
            else {
                Toast.makeText(getActivity(), R.string.user_already_exists, Toast.LENGTH_LONG).show();
            }
            db.close();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity parentActivity = (MainActivity) getActivity();
        if(parentActivity != null){
            parentActivity.getSupportActionBar().setTitle("Create a new user");
        }
    }

    private void synchronizeWithRemoteDB(User user) {
        RemoteDBRequest.user(getActivity(), RemoteDBRequest.QUERY_TYPE_INSERT, user, (response, responseBody, requestName) -> {
            Bundle bundle = new Bundle();
            bundle.putString("USERNAME", user.id);
            bundle.putBoolean("HAS_CREATED_USER", true);
            NavHostFragment.findNavController(UserCreationFragment.this)
                    .navigate(R.id.action_userCreationFragment_to_userLoginFragment, bundle);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserCreationBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

    public void creationDataChanged(String username, String name) {
        if (isFieldEmpty(username)) {
            creationFormState.setValue(new CreationFormState(R.string.invalid_name, null));
        } else if (!isUserNameValid(username)) {
            creationFormState.setValue(new CreationFormState(R.string.invalid_username_taken, null));
        }  else if (isFieldEmpty(name)) {
            creationFormState.setValue(new CreationFormState(null, R.string.invalid_name));
        } else {
            creationFormState.setValue(new CreationFormState(true));
        }
    }

    private boolean isFieldEmpty(String name) {
        if (name == null) {
            return true;
        } else {
            return name.trim().isEmpty();
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            Log.d("here", "username is null");
            return false;
        }

        while (username.charAt(0) == ' ' && username.length() > 1) {
            username = username.substring(1);
        }

        AppDatabase db = Room.databaseBuilder(getActivity(), AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        User user = db.userDao().findByName(username);

        if (!(user == null)) {
            return false;
        } else {
            return !username.trim().isEmpty();
        }

    }
}