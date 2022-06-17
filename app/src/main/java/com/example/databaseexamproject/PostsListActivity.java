package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.databaseexamproject.databinding.ActivityPostsListBinding;

public class PostsListActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityPostsListBinding binding;
    private String loggedUserID;

    // RecyclerView position remember majiggy
    private int recyclerViewPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        loggedUserID = intent.getStringExtra("loggedUserID");
        Log.d(TAG, "onCreate: UserID from login is: " + loggedUserID);

        binding = ActivityPostsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_posts_list);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }



    public String getUserID(){
        return loggedUserID;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_posts_list);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    // Set and get the recyclerview position
    // Fragments can access these by calling their parent activity!
    public int getRecyclerViewPosition(){
        return recyclerViewPosition;
    }
    public void setRecyclerViewPosition(int recyclerViewPosition){
        this.recyclerViewPosition = recyclerViewPosition;
    }
}