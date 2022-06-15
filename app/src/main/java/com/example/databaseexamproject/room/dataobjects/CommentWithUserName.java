package com.example.databaseexamproject.room.dataobjects;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class CommentWithUserName {

    @Embedded
    public Comment comment;

    @ColumnInfo(name = "name")
    public String name;
}
