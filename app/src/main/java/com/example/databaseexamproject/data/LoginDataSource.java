package com.example.databaseexamproject.data;

import android.content.Context;

import androidx.room.Room;

import com.example.databaseexamproject.data.model.LoggedInUser;
import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.dataobjects.User;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {


    public Result<LoggedInUser> login(String userid, Context context) {

        try {
            AppDatabase db = Room.databaseBuilder(context,
                    AppDatabase.class, "users").allowMainThreadQueries().build();

            User user = db.userDao().findByName(userid);


            LoggedInUser fakeUser =
                    new LoggedInUser(
                            String.valueOf(user.id),
                            user.name);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e), userid);
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}