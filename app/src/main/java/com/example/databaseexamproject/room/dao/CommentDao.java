package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("SELECT * FROM comments ORDER BY stamp DESC")
    List<Comment> getAllSortedDateDesc();

    @Query("SELECT * FROM comments WHERE comments.post_id = (:postID) ORDER BY stamp DESC")
    List<Comment> getByPostSortedDateDesc(int postID);

    @Query("SELECT * FROM comments WHERE comments.user_id = (:userID) ORDER BY stamp DESC")
    List<Comment> getByUserSortedDateDesc(String userID);

    @Query("SELECT * FROM comments WHERE comments.post_id = (:postID) AND comments.user_id = (:userID) ORDER BY stamp DESC")
    List<Comment> getByPostAndUserSortedDateDesc(int postID, String userID);

    @Insert
    void insertComment(Comment... comment);
}
