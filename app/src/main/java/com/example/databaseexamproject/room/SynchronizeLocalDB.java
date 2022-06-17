package com.example.databaseexamproject.room;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.room.dataobjects.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
                    if(table_name.equals("reactions")){
                        thisURL = thisURL + "?order=stamp.desc";
                    }
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
                            db.userDao().deleteEverything();
                            db.userDao().insertAll(users);
                            break;
                        case "posts":
                            Post[] posts = gson.fromJson(response.body().string(), Post[].class);

                            List<Post> postParsePosts = new ArrayList<>();
                            List<Comment> postParseComment = new ArrayList<>();
                            // Identifies a number in a string (can be negative)
                            String regexForPostID = "(-?)(\\d+)";
                            Pattern patternForPostID = Pattern.compile(regexForPostID);
                            // This regex is important. It allows us to incorporate the other groups formats for designating which posts are comments.
                            // Right now we have our own format, and two others we spotted in the remote DB while working.
                            // We can include as many as we want, as long as they use the general format of:
                            // Having a number, which is then the foreign key
                            // AND the number has an identifier prepended to it.
                            String regexCommentFormat = "(?:(forPost:)|(re:\")|(\"__COMMENT_FOR\":))(-?)(\\d+)";
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
                                            int forPost = Integer.parseInt(matcherForPostID.group());
                                            postParseComment.add(new Comment(post.id, post.user_id, forPost, commentContent, post.stamp));
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
                            db.postDao().deleteEverything();
                            db.postDao().insertAll(postsOnly);

                            // Then we handle the comments
                            Comment[] dummy2 = new Comment[0];
                            Comment[] commentsOnly = postParseComment.toArray(dummy2);
                            db.commentDao().deleteEverything();
                            db.commentDao().insertAll(commentsOnly);
                            break;
                        case "reactions":
                            Reaction[] reactions = gson.fromJson(response.body().string(), Reaction[].class);
                            db.reactionDao().deleteEverything();
                            db.reactionDao().insertAll(reactions);
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
        db.close();
        boolean success = successfulSyncs == table_names.length;
        Log.d(TAG, "syncDB: Completed sync, success: " + success);

        afterSync.onCompletedSync(success);
    }


    public interface CompletedSync {
        void onCompletedSync(boolean success);
    }

}
