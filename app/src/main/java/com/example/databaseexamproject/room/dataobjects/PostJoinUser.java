package com.example.databaseexamproject.room.dataobjects;


import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;

import com.example.databaseexamproject.room.dataobjects.Post;

@Entity
public class PostJoinUser {

    @Embedded
    public Post post;

    @ColumnInfo(name = "name")
    public String name;

}
