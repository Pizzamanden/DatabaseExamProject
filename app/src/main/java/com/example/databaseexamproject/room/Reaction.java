package com.example.databaseexamproject.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "reactions", primaryKeys = {"user_id", "post_id", "stamp"}) // Refer to ColumnInfo names!
public class Reaction {

    @NonNull
    @ColumnInfo(name = "user_id")
    public String userID;

    @NonNull
    @ColumnInfo(name = "post_id")
    public int postID;

    public int type;

    @NonNull
    public Date stamp;
}
