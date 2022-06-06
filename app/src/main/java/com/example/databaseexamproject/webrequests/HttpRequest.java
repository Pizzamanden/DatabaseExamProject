package com.example.databaseexamproject.webrequests;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// The main goal here is to create a class for http request.
// The way it should work is:
// Errors and failures should be recognizable by the class which used them
public class HttpRequest {

    // The header we were told to use.
    // Used as a normal final class variable.
    private static final String REQUEST_HEADER = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYXBwMjAyMiJ9.iEPYaqBPWoAxc7iyi507U3sexbkLHRKABQgYNDG4Awk";

    // Other static final Strings
    public static final String GET = "get";
    public static final String POST = "post";

    // These three are used as comparisons, to make the comparison simple.
    // Check result-string with these before parsing anything
    public static final String REQUEST_SUCCESS = "Request was successful!";
    public static final String REQUEST_ERROR = "Request resulted in an error";
    public static final String REQUEST_FAILURE = "Request failed";

    private Context mContext;
    private HttpRequestResponse mCallback;

    private Request currentRequest;
    private String requestName;


    // "https://caracal.imada.sdu.dk/app2022/"
    private String url = "https://caracal.imada.sdu.dk/app2022/";

    public HttpRequest(Context context, HttpRequestResponse callback){
        this.mCallback = callback;
        mContext = context;
    }


    // Build the request
    public HttpRequest builder(String type, ArrayList<String> keys, ArrayList<String> values, String requestName){
        // Set request Name:
        this.requestName = requestName;
        String requestURL = url;
        Request request;

        if(type.equals(POST)){
            MultipartBody.Builder mBody = new MultipartBody.Builder();
            mBody.setType(MultipartBody.FORM);
            for(int i = 0; i < keys.size(); i++){
                mBody.addFormDataPart(keys.get(i), values.get(i));
            }
            // Build Body
            RequestBody requestBody = mBody.build();
            // Build request with body and url
            request = new Request.Builder()
                    .url(requestURL)
                    .post(requestBody)
                    .build();
            this.currentRequest = request;
        } else if(type.equals(GET)){
            // Here we build the URL into the request

            for(int i = 0; i < keys.size(); i++){
                // Check if its the first part of the arraylist
                // If not, add a "&" to add more parameters
                requestURL += keys.get(i) + "=" + values.get(i);
                if(i < keys.size() - 1){ // If we are not at the last element of the list, add an "&"
                    requestURL += "&";
                }
            }
            // Build request with url string
            request = new Request.Builder()
                    .url(requestURL)
                    .build();
            this.currentRequest = request;
        } else {
            // Error
        }
        return this;
    }


    public void makeHttpRequest(){
        Log.d(TAG, "makeHttpRequest: Fired request " + requestName);
        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Make call on client with request
        client.newCall(currentRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "makeHttpRequest: onFailure call fired");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "makeHttpRequest: onResponse method fired");
                Log.d(TAG, "makeHttpRequest: Response code: " + response.code());
                String myResponse = null;
                if (response.code() == 200) {
                    // Successful response!
                    myResponse = response.body().string();
                } // add more blocks to read more HTTP codes
                runInterfaceOnUi(response.code(), myResponse, requestName);
                response.body().close();
            }
        });
    }

    private void runInterfaceOnUi(final int responseCode, final String responseString, final String requestName){
        Activity activity = (Activity) mContext;
        activity.runOnUiThread( () -> mCallback.onHttpRequestResponse(responseCode, responseString, requestName));
    }

    public interface HttpRequestResponse {
        void onHttpRequestResponse(final int responseCode, final String responseJson, final String requestName);
    }
}
