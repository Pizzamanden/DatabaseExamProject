package com.example.databaseexamproject.room.dataobjects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "comments", primaryKeys = {"post_id", "user_id", "text", "stamp"})
public class Comment {

    @NonNull
    public String user_id;

    @NonNull
    public int post_id;

    @NonNull
    public String text;

    @NonNull
    public Date stamp;

}
