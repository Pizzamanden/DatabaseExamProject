package com.example.databaseexamproject.room;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.databaseexamproject.webrequests.HttpRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

import androidx.room.Room;

public class SynchronizeLocalDB {

    public static final String remote_url = "https://caracal.imada.sdu.dk/app2022";


    public static void syncDB(Context context, CompletedSync afterSync){
        // First, get remote content
        // Then, update local to reflect remote (i guess, just overwrite?)
        // Then, tell Main Thread we are done and ready

        // Create the request
        // TODO finish request
        String[] table_names = {
          "users",
          "posts",
          "reactions"
        };

//        for (int i = 0; i < table_names.length; i++) {
//            String thisURL = remote_url + "/" + table_names[i];
//            Request request = new Request.Builder()
//                    .url(thisURL)
//                    .build();
//
//            // Send the request
//            new HttpRequest(context, (response, name) -> {
//                runCallbackOnUIThread(context, afterSync, updateLocalDatabase(response, name));
//            }, table_names[i]).makeHttpRequest(request);
//        }


            String thisURL = remote_url + "/users";
            Request request = new Request.Builder()
                    .url(thisURL)
                    .build();

            // Send the request
            new HttpRequest(context, (response, JSON, name) -> {
                updateLocalDatabase(context, afterSync, response, JSON, name);
            }, "users").makeHttpRequest(request);
            // TODO make this work for all tables? the String responseName should maybe be an array of the correct table???????????

    }

    private static void updateLocalDatabase(Context context, CompletedSync afterSync, Response response, String JSON, String responseName){
        Log.d(TAG, "updateLocalDatabase: Fired");
        // TODO result body as string (the variable "JSON") will always be an array of the table we are working on. ???????

        // We should have our result.
        // First we read the code, to make sure we got a readable answer.
        // If the code is NOT 200, we could have something un-parsable
        // Another check is a good idea
        if(response.code() == 200){
            // Now we must setup the correct response.

            AtomicInteger successCounter = new AtomicInteger(0);
            AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "database-name").build();
            // Now, we must decide how we actually sync our DB
            // TODO decide this

            switch (responseName){
                case "users":

                    break;
                case "posts":

                    break;
                case "reactions":

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + responseName);
            }

            // Code for saving and using the result
            Gson gson = new Gson();
            User[] users = gson.fromJson(JSON, User[].class);
            Log.d(TAG, users.length + " many " + responseName);




            runCallbackOnUIThread(context, afterSync, true);
        } else {
            // Error handling on failed result
            runCallbackOnUIThread(context, afterSync, false);
        }
    }


    private static void runCallbackOnUIThread(Context context, CompletedSync afterSync, boolean success){
        Log.d(TAG, "runCallbackOnUIThread: Fired");
        Activity activity = (Activity) context;
        activity.runOnUiThread( () -> afterSync.onCompletedSync(success));
    }


    public interface CompletedSync {
        void onCompletedSync(boolean success);
    }

}
