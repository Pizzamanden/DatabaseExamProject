package com.example.databaseexamproject.room.dataobjects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "posts")
public class Post {

    @NonNull
    @PrimaryKey
    public int id;

    public String user_id;

    public String content;


    public Date stamp;


    public boolean equals(Object other){
        if(other == null)
            return false;
        if(!(other instanceof Post))
            return false;
        Post otherPost = (Post) other;
        return (this.id == otherPost.id && this.user_id.equals(otherPost.user_id) && this.content.equals(otherPost.content) && this.stamp.equals(otherPost.stamp));
    }
}
