package com.example.databaseexamproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverter;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts ORDER BY stamp DESC")
    List<Post> getAllSortedDateDesc();

    @Query("SELECT * FROM posts WHERE posts.user_id = (:userID) ORDER BY stamp DESC")
    List<Post> getByUserSortedDateDesc(String userID);

    @Query("SELECT * FROM posts WHERE stamp < (:specificDate) ORDER BY stamp DESC")
    List<Post> getBeforeDateSortedDateDesc(Date specificDate);

    @Query("SELECT * FROM posts WHERE stamp >= (:specificDate) ORDER BY stamp DESC")
    List<Post> getAfterDateSortedDateDesc(Date specificDate);




    @Insert
    void insertPost(Post post);
}