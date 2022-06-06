package com.example.databaseexamproject.room;


import android.app.Activity;
import android.content.Context;

// Class for making the async calls to the database
// What does it do?
// First, we create our request object
// In this specification, we want a method to run when we are done
// Ideally, the method should have as parameters, the return of the query
public class DatabaseRequest<R> {

    private final Context mContext;
    private final DatabaseRequestResponse<R> mCallback;


    public DatabaseRequest(Context context, DatabaseRequestResponse<R> callback){
        mContext = context;
        mCallback = callback;
    }

    // RUN ON SEPERATE THREAD
    public void runRequest(DatabaseQuery<R> query){
        // When the request is done, we make it know and visible to the UI thread
        runInterfaceOnUi(query.query());
    }


    private void runInterfaceOnUi(R queryResult){
        Activity activity = (Activity) mContext;
        activity.runOnUiThread( () -> mCallback.onDatabaseRequestResponse(queryResult));
    }

    public interface DatabaseRequestResponse<T> {
        void onDatabaseRequestResponse(T result);
    }

    public interface DatabaseQuery<T>{
        T query();
    }

}
