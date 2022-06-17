package com.example.databaseexamproject.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.databaseexamproject.MainActivity;
import com.example.databaseexamproject.R;
import com.example.databaseexamproject.databinding.FragmentUserLoginBinding;
import com.example.databaseexamproject.room.SynchronizeLocalDB;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserLoginFragment extends Fragment {



    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USERNAME = "USERNAME";
    private static final String HAS_CREATED_USER = "HAS_CREATED_USER";

    private FragmentUserLoginBinding binding;
    private LoginViewModel loginViewModel;

    private String name;
    private boolean hasCreatedUser;

    public UserLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static UserLoginFragment newInstance(String user, Boolean createdUser) {
        UserLoginFragment fragment = new UserLoginFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, user);
        args.putBoolean(HAS_CREATED_USER, createdUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(USERNAME);
            hasCreatedUser = getArguments().getBoolean(HAS_CREATED_USER);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        //final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;



        loginViewModel.getLoginFormState().observe(getActivity(), loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
/*                if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }*/
        });

        loginViewModel.getLoginResult().observe(getActivity(), loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
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
                loginViewModel.loginDataChanged(usernameEditText.getText().toString());
            }

            /*@Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }*/
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
/*        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            getActivity());
                }
                return false;
            }
        });*/

        if (hasCreatedUser){
            usernameEditText.setText(name);
        }

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(usernameEditText.getText().toString(),
                    getActivity());
        });

        binding.toCreateUserButton.setOnClickListener((v -> {
            NavHostFragment.findNavController(UserLoginFragment.this)
                    .navigate(R.id.action_userLoginFragment_to_userCreationFragment);
        }));
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity parentActivity = (MainActivity) getActivity();
        if(parentActivity != null){
            parentActivity.getSupportActionBar().setTitle("Login");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getName();
        Intent intent = new Intent(getActivity(), com.example.databaseexamproject.PostsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("loggedUserID", model.getUserid());
        startActivity(intent);

        Toast.makeText(getActivity(), welcome, Toast.LENGTH_LONG).show();

        getActivity().setResult(Activity.RESULT_OK);

        //Complete and destroy login activity once successful
        getActivity().finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        SynchronizeLocalDB.syncDB(getActivity(), (success) ->{});
        Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
    }
}