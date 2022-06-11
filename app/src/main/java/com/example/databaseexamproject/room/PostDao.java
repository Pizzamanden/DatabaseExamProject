package com.example.databaseexamproject.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;

import java.util.Date;
import java.util.List;

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

    @Query("SELECT * FROM posts JOIN users ON posts.user_id = users.id ORDER BY posts.stamp DESC")
    List<PostJoinUser> getAllPostsWithUserNameSortedDateDesc();

    @Query("SELECT * FROM posts")
    List<PostJoinUser> fuckem();



    @Insert
    void insertAll(Post... post);

    @Query("DELETE FROM posts")
    void deleteEverything();
}