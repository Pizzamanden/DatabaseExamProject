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
import android.widget.Button;

import com.example.databaseexamproject.databinding.ActivityPostsListBinding;

public class PostsListActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityPostsListBinding binding;
    private String loggedUserID;

    // RecyclerView position remember majiggy
    private int reyclerViewPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        loggedUserID = intent.getStringExtra("loggedUserID");
        Log.d(TAG, "onCreate: UserID from login is: " + loggedUserID);

        binding = ActivityPostsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Log.d(TAG, "onCreate: Creating nav controller");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_posts_list);
        Log.d(TAG, "onCreate: Created nav controller");
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        Log.d(TAG, "onCreate: Set action bar configuration");
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        Log.d(TAG, "onCreate: Set action bar");

        // TODO find a way to save the position of the PostsListFragment here?
        // Such that it can be used again


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
    public int getReyclerViewPosition(){
        return reyclerViewPosition;
    }
    public void setReyclerViewPosition(int reyclerViewPosition){
        this.reyclerViewPosition = reyclerViewPosition;
    }
}