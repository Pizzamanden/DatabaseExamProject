package com.example.databaseexamproject.webrequests;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.databaseexamproject.room.AppDatabase;
import com.example.databaseexamproject.room.DatabaseRequest;
import com.example.databaseexamproject.room.SynchronizeLocalDB;
import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.room.dataobjects.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteDBRequest {

    public static final String REMOTE_URL = "https://caracal.imada.sdu.dk/app2022";
    public static final String AUTH_KEY = "Authorization";
    public static final String AUTH_VALUE = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/json";

    public static final String QUERY_TYPE_INSERT = "queryTypeInsert";
    public static final String QUERY_TYPE_UPDATE = "queryTypeUpdate";


    // We found no way to prepare our SQL statements we send to remote against any kind of injection attempts.
    // We assume the API can handle all queries by first taking the elements we sent, sanitizing them, and then handling them.

    public static void post(Context context, String type, Post post, HttpRequest.HttpRequestResponse requestResponse){
        HttpRequest httpRequest = new HttpRequest(context, requestResponse, "Post" + type);
        String baselineURL = REMOTE_URL + "/posts";

        Gson gson = new Gson();
        String object = gson.toJson(post);

        byte[] toByteStream = object.getBytes(StandardCharsets.UTF_8);

        switch (type) {
            case QUERY_TYPE_INSERT: {
                Log.d(TAG, "post: Insert call");
                httpRequest.makeHttpRequest(new Request.Builder()
                        .post(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_UPDATE: {
                Log.d(TAG, "post: Update call on ID: " + post.id);
                baselineURL = baselineURL + "?id=eq." + post.id;
                httpRequest.makeHttpRequest(new Request.Builder()
                        .patch(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            default:
                Log.d(TAG, "post: Call was made without a type specification. No action will be taken");
                break;
        }
    }

    public static void user(Context context, String type, User user, HttpRequest.HttpRequestResponse requestResponse){
        HttpRequest httpRequest = new HttpRequest(context, requestResponse, "User" + type);
        String baselineURL = REMOTE_URL + "/users";

        Gson gson = new Gson();
        String object = gson.toJson(user);

        byte[] toByteStream = object.getBytes(StandardCharsets.UTF_8);

        switch (type) {
            case QUERY_TYPE_INSERT: {
                Log.d(TAG, "post: Insert call");
                httpRequest.makeHttpRequest(new Request.Builder()
                        .post(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_UPDATE: {
                Log.d(TAG, "post: Update call on ID: " + user.id);
                baselineURL = baselineURL + "?id=eq." + user.id;

                httpRequest.makeHttpRequest(new Request.Builder()
                        .patch(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            default:
                Log.d(TAG, "post: Call was made without a type specification. No action will be taken");
                break;
        }
    }

    public static void reaction(Context context, String type, Reaction reaction, HttpRequest.HttpRequestResponse requestResponse){
        HttpRequest httpRequest = new HttpRequest(context, requestResponse, "Reaction" + type);
        String baselineURL = REMOTE_URL + "/reactions";

        Gson gson = new Gson();
        String object = gson.toJson(reaction);

        byte[] toByteStream = object.getBytes(StandardCharsets.UTF_8);

        switch (type) {
            case QUERY_TYPE_INSERT: {
                Log.d(TAG, "reaction: Insert call");
                httpRequest.makeHttpRequest(new Request.Builder()
                        .post(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_UPDATE: {
                Log.d(TAG, "reaction: ");
                baselineURL = baselineURL + "?post_id=eq." + reaction.post_id + "&user_id=eq." + reaction.user_id;
                httpRequest.makeHttpRequest(new Request.Builder()
                        .patch(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            default:
                Log.d(TAG, "post: Call was made without a correct type specification. No action will be taken");
                break;
        }
    }

    public static void comment(Context context, String type, Comment comment, HttpRequest.HttpRequestResponse requestResponse){
        HttpRequest httpRequest = new HttpRequest(context, requestResponse, "Post" + type);
        String baselineURL = REMOTE_URL + "/posts";

        Gson gson = new Gson();
        String object = gson.toJson(comment);

        byte[] toByteStream = object.getBytes(StandardCharsets.UTF_8);

        switch (type) {
            case QUERY_TYPE_INSERT: {
                Log.d(TAG, "post: Insert call");
                httpRequest.makeHttpRequest(new Request.Builder()
                        .post(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_UPDATE: {
                Log.d(TAG, "post: Update call on ID: " + comment.id);
                baselineURL = baselineURL + "?id=eq." + comment.id;
                httpRequest.makeHttpRequest(new Request.Builder()
                        .patch(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            default:
                Log.d(TAG, "post: Call was made without a type specification. No action will be taken");
                break;
        }
    }

    public static void deletePost(Context context, int post_id, Runnable runAfterCompletion){
        new Thread(() -> {
            // Why do we cascade comments of comments, if we don't allow them in our app?
            // Because, maybe another group does!

            // At the very start, we synchronize the local database.
            SynchronizeLocalDB.syncDB(context, (success) ->{});

            AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "database-name").build();
            // Then we check the post we want to delete is still here.

            List<Integer> affectedComments = cascadeDeletion(db, post_id);
            affectedComments.add(post_id);
            // Now we can use these ID's to delete all reactions, comments and the post.
            String reactionsToDeleteURL = REMOTE_URL + "/reactions?post_id=eq.";
            for(int i = 0; i < affectedComments.size(); i++){
                reactionsToDeleteURL = reactionsToDeleteURL + affectedComments.get(i);
                if(i + 1 < affectedComments.size()){
                    reactionsToDeleteURL = reactionsToDeleteURL + ";eq.";
                }
            }
            Log.d(TAG, "deletePost: Reaction URL query: " + reactionsToDeleteURL);

            Request requestDeleteReactions = new Request.Builder()
                    .delete()
                    .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                    .addHeader(AUTH_KEY, AUTH_VALUE)
                    .url(reactionsToDeleteURL)
                    .build();
            OkHttpClient client = new OkHttpClient();
            // Make call on client with request
            try {
                Response response = client.newCall(requestDeleteReactions).execute();
                Log.d(TAG, "deletePost: " + response.code());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String postsToDeleteURL = REMOTE_URL + "/posts?id=eq.";
            for(int i = 0; i < affectedComments.size(); i++){
                postsToDeleteURL = postsToDeleteURL + affectedComments.get(i);
                if(i + 1 < affectedComments.size()){
                    postsToDeleteURL = postsToDeleteURL + ";eq.";
                }
            }
            Log.d(TAG, "deletePost: Posts URL query: " + postsToDeleteURL);

            Request requestDeletePosts = new Request.Builder()
                    .delete()
                    .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                    .addHeader(AUTH_KEY, AUTH_VALUE)
                    .url(postsToDeleteURL)
                    .build();
            // Make call on client with request
            try {
                Response response = client.newCall(requestDeletePosts).execute();
                Log.d(TAG, "deletePost: " + response.code());
            } catch (IOException e) {
                e.printStackTrace();
            }
            db.close();
            Activity activity = (Activity) context;
            activity.runOnUiThread(runAfterCompletion);
        }).start();
    }

    private static List<Integer> cascadeDeletion(AppDatabase db, int commentID){
        // The comment this method is run on is already in the list
        // Get all comments, which depends on this comment
        List<Integer> commentsOfThisComment = db.commentDao().getAllCommentIDByPostID(commentID);
        // Then run this method on all of them
        for(Integer dependingCommentID : commentsOfThisComment){
            commentsOfThisComment.addAll(cascadeDeletion(db, dependingCommentID));
        }
        // At the end, we should have one single list of all impacted comments (from this comment)
        return commentsOfThisComment;
    }
}
