package com.example.databaseexamproject.data;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.databaseexamproject.data.model.LoggedInUser;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.dataobjects.User;

import java.io.IOException;
import java.util.List;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {


    public Result<LoggedInUser> login(String userid, Context context) {

        try {
            AppDatabase db = Room.databaseBuilder(context,
                    AppDatabase.class, "database-name").allowMainThreadQueries().build();

            User user = db.userDao().findByName(userid);
            db.close();
            if(user == null){
                Log.d(TAG, "login: User ID not found");
            }
            
            LoggedInUser currentUser =
                    new LoggedInUser(
                            user.id,
                            user.name);
            return new Result.Success<>(currentUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}