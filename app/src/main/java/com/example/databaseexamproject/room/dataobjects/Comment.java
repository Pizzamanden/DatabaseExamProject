package com.example.databaseexamproject.room.dataobjects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "comments")
public class Comment {

    @PrimaryKey
    public int id;

    @NonNull
    public String user_id;

    public int post_id;

    @NonNull
    public String text;

    @NonNull
    public Date stamp;

}
