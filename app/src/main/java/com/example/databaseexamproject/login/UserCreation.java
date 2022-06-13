package com.example.databaseexamproject.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.MyApp;
import com.example.databaseexamproject.R;
import com.example.databaseexamproject.databinding.ActivityUserCreationBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.User;

public class UserCreation extends AppCompatActivity {

    private ActivityUserCreationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_creation);

        binding = ActivityUserCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final EditText usernameEditText = binding.userid;
        final EditText passwordEditText = binding.fullName;
        final Button createButton = binding.button;

        createButton.setOnClickListener(v -> {
            Log.d("UserCreation",usernameEditText.getText().toString());

            AppDatabase db = Room.databaseBuilder(MyApp.getAppContext(), AppDatabase.class, "users").allowMainThreadQueries().build();
            db.userDao().insertAll(new User(usernameEditText.getText().toString(), passwordEditText.getText().toString()));

            startActivity(new Intent(UserCreation.this, LoginActivity.class));
        });
    }
}