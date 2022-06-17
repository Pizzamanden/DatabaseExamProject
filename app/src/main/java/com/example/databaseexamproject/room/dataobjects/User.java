package com.example.databaseexamproject.room.dataobjects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


// Mimics table from remote database: users(id text, name text, stamp timestamp)
@Entity(tableName = "users")
public class User {



    @NonNull
    @PrimaryKey
    public String id;


    public String name;

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    public Date stamp;


    public User(@NonNull String id, String name) {
        this.id = id;
        this.name = name;
    }

}
