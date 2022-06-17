package com.example.databaseexamproject.data;

import android.content.Context;
import android.nfc.Tag;
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

            List<User> users = db.userDao().getAll();
            for (User u : users) {
                Log.d("", u.id);
            }
            db.close();
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