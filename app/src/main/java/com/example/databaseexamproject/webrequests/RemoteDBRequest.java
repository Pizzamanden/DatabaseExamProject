package com.example.databaseexamproject.webrequests;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.User;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    public static final String QUERY_TYPE_DELETE = "queryTypeDelete";


    // TODO complete the class for insertions, updates, and deletions.
    // Remember Injections!

    public static void post(Context context, String type, Post post, Runnable runnableCallback){
        HttpRequest httpRequest = new HttpRequest(context, (response, responseBody, requestName) -> {
            processResponse(response, responseBody, requestName, runnableCallback);
        }, "Post" + type);
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

                httpRequest.makeHttpRequest(new Request.Builder()
                        .put(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_DELETE:
                Log.d(TAG, "post: Delete call on ID: " + post.id);
                httpRequest.makeHttpRequest(new Request.Builder()
                        .delete(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            default:
                Log.d(TAG, "post: Call was made without a type specification. No action will be taken");
                break;
        }
    }

    public static void user(Context context, String type, User user, Runnable runnableCallback){
        HttpRequest httpRequest = new HttpRequest(context, (response, responseBody, requestName) -> {
            processResponse(response, responseBody, requestName, runnableCallback);
        }, "User" + type);
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

                httpRequest.makeHttpRequest(new Request.Builder()
                        .put(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            }
            case QUERY_TYPE_DELETE:
                Log.d(TAG, "post: Delete call on ID: " + user.id);
                httpRequest.makeHttpRequest(new Request.Builder()
                        .delete(RequestBody.create(toByteStream))
                        .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                        .addHeader(AUTH_KEY, AUTH_VALUE)
                        .url(baselineURL)
                        .build()
                );
                break;
            default:
                Log.d(TAG, "post: Call was made without a type specification. No action will be taken");
                break;
        }
    }

    private static void processResponse(Response response, String body, String requestName, Runnable runnableCallback){
        // This is on the Main Thread
        // We now check the result (mainly with logs, i want to know what is going on :3)
        runnableCallback.run();
    }
}
