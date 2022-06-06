package com.example.databaseexamproject.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "comments", primaryKeys = {"post_id", "user_id", "text", "stamp"})
public class Comment {

    @NonNull
    @ColumnInfo(name = "user_id")
    public String userID;

    @NonNull
    @ColumnInfo(name = "post_id")
    public int postID;

    @NonNull
    public String text;

    @NonNull
    public Date stamp;

}
