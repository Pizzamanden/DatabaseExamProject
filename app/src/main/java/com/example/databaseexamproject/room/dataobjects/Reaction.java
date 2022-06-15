package com.example.databaseexamproject.room.dataobjects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "reactions", primaryKeys = {"user_id", "post_id", "stamp"}) // Refer to ColumnInfo names!
public class Reaction {

    @NonNull
    public String user_id;

    @NonNull
    public int post_id;

    public int type;

    @NonNull
    public Date stamp;

    public Reaction(@NonNull String user_id, int post_id, int type) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.type = type;
    }
}
