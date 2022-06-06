package com.example.databaseexamproject.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "posts")
public class Post {

    @NonNull
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "user_id")
    public String userID;

    public String content;

    // TODO
    public Date stamp;

}
