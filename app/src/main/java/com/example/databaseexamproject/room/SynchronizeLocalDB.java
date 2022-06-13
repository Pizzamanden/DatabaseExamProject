package com.example.databaseexamproject.room;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.databaseexamproject.R;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.room.dataobjects.User;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

import androidx.room.Room;

public class SynchronizeLocalDB {

    public static final String remote_url = "https://caracal.imada.sdu.dk/app2022";


    public static void syncDB(Context context, CompletedSync afterSync){
        // First, get remote content
        // Then, update local to reflect remote (i guess, just overwrite?)
        // Then, tell Main Thread we are done and ready

        // Create the request
        String[] table_names = {
          "users",
          "posts",
          "reactions"
        };

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>( executor );

        AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "database-name").build();

        int successfulSyncs = 0;

        try{
            int pendingTasks = table_names.length;
            for (String table_name : table_names) {
                completionService.submit(() -> {
                    String thisURL = remote_url + "/" + table_name;
                    Request request = new Request.Builder()
                            .url(thisURL)
                            .build();


                    OkHttpClient client = new OkHttpClient();
                    // Make call on client with request
                    Response response = client.newCall(request).execute();
                    Gson gson = new Gson();
                    switch (table_name){
                        case "users":
                            User[] users = gson.fromJson(response.body().string(), User[].class);
                            Log.d(TAG, "syncDB: Users: Parsed JSON");
                            db.userDao().deleteEverything();
                            Log.d(TAG, "syncDB: Users: Deleted table");
                            db.userDao().insertAll(users);
                            Log.d(TAG, "syncDB: Users: Filled table");
                            break;
                        case "posts":
                            Post[] posts = gson.fromJson(response.body().string(), Post[].class);
                            Log.d(TAG, "syncDB: Posts: Parsed JSON");
                            db.postDao().deleteEverything();
                            Log.d(TAG, "syncDB: Posts: Deleted table");
                            db.postDao().insertAll(posts);
                            Log.d(TAG, "syncDB: Posts: Filled table");
                            break;
                        case "reactions":
                            Reaction[] reactions = gson.fromJson(response.body().string(), Reaction[].class);
                            Log.d(TAG, "syncDB: Reactions: Parsed JSON");
                            db.reactionDao().deleteEverything();
                            Log.d(TAG, "syncDB: Reactions: Deleted table");
                            db.reactionDao().insertAll(reactions);
                            Log.d(TAG, "syncDB: Reactions: Filled table");
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + table_name);
                    }
                    return true;
                });
            }
            while( pendingTasks > 0 ) {
                Boolean result = completionService.take().get();
                if(result)
                    successfulSyncs++;
                pendingTasks--;
            }
        } catch( InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            executor.shutdown();
            executor.awaitTermination( 1, TimeUnit.DAYS );
        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
        boolean success = successfulSyncs == table_names.length;
        Log.d(TAG, "syncDB: Completed sync, success: " + success);

        runCallbackOnUIThread(context, afterSync, success);
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
