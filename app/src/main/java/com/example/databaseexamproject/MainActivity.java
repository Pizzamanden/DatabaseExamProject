package com.example.databaseexamproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.example.databaseexamproject.databinding.ActivityMainBinding;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.Post;
import com.example.databaseexamproject.room.User;
import com.example.databaseexamproject.webrequests.HttpRequest;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        clickMe();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void clickMe(){
        ArrayList<String> keyList = new ArrayList<>();
        ArrayList<String> valList = new ArrayList<>();
        //keyList.add("key1");
        //valList.add("valueForKey1");

        HttpRequest httpRequest =
                new HttpRequest(this, (code, json, requestName) -> callMeMaybe(code))
                    .builder(HttpRequest.GET, keyList, valList, "Calling mom xD");
        httpRequest.makeHttpRequest();
    }

    private void callMeMaybe(int code){
        Log.d(TAG, "Called me, maybe, with code " + code);
    }

}