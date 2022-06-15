package com.example.databaseexamproject.room;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.databaseexamproject.R;
import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.room.dataobjects.User;
import com.example.databaseexamproject.webrequests.HttpRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();

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

                            List<Post> postParsePosts = new ArrayList<>();
                            List<Comment> postParseComment = new ArrayList<>();
                            String regexForPostID = "(-?)(\\d+)";
                            Pattern patternForPostID = Pattern.compile(regexForPostID);

                            String regexCommentFormat = "(?:(forPost:)|(re:\"))(-?)(\\d+)";
                            Pattern patternCommentFormat = Pattern.compile(regexCommentFormat);

                            for(Post post : posts){
                                if(post.content != null){
                                    Matcher matcherCommentFormat = patternCommentFormat.matcher(post.content);
                                    if(matcherCommentFormat.find()){
                                        // A comment format we can recognize
                                        String matchedString = matcherCommentFormat.group();
                                        // We save the rest of the post content, without this bit
                                        String commentContent = post.content.replace(matchedString, "");
                                        // Now we extract the number from the string (We know it is in there)
                                        Matcher matcherForPostID = patternForPostID.matcher(matchedString);
                                        if(matcherForPostID.find()){
                                            // Now we add our found comment
                                            postParseComment.add(new Comment(post.id, post.user_id, Integer.parseInt(matcherForPostID.group()), commentContent, post.stamp));
                                        }
                                    } else {
                                        // Not a comment we can recognize
                                        postParsePosts.add(post);
                                    }
                                } else {
                                    // As the content is null, it cannot be a comment
                                    postParsePosts.add(post);
                                }
                            }
                            // First we handle posts
                            Post[] dummy = new Post[0];
                            Post[] postsOnly = postParsePosts.toArray(dummy);
                            Log.d(TAG, "syncDB: Posts: Parsed JSON");
                            db.postDao().deleteEverything();
                            Log.d(TAG, "syncDB: Posts: Deleted table");
                            db.postDao().insertAll(postsOnly);
                            Log.d(TAG, "syncDB: Posts: Filled table");

                            // Then we handle the comments
                            Comment[] dummy2 = new Comment[0];
                            Comment[] commentsOnly = postParseComment.toArray(dummy2);
                            Log.d(TAG, "syncDB: Comments: Parsed JSON");
                            db.commentDao().deleteEverything();
                            Log.d(TAG, "syncDB: Comments: Deleted table");
                            db.commentDao().insertAll(commentsOnly);
                            Log.d(TAG, "syncDB: Comments: Filled table");
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

        afterSync.onCompletedSync(success);
    }


    public interface CompletedSync {
        void onCompletedSync(boolean success);
    }

}
