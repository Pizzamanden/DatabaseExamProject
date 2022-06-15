package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.CommentWithUserName;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("SELECT comments.*, users.name FROM comments JOIN users ON users.id = comments.user_id" +
            " ORDER BY stamp DESC")
    List<CommentWithUserName> getAllSortedDateDesc();

    @Query("SELECT comments.*, users.name FROM comments JOIN users ON users.id = comments.user_id" +
            " WHERE comments.post_id = (:postID)" +
            " ORDER BY stamp DESC")
    List<CommentWithUserName> getByPostSortedDateDesc(int postID);

    @Query("SELECT comments.*, users.name FROM comments JOIN users ON users.id = comments.user_id" +
            " WHERE comments.user_id = (:userID)" +
            " ORDER BY stamp DESC")
    List<CommentWithUserName> getByUserSortedDateDesc(String userID);

    @Query("SELECT comments.*, users.name FROM comments JOIN users ON users.id = comments.user_id" +
            " WHERE comments.post_id = (:postID) AND comments.user_id = (:userID)" +
            " ORDER BY stamp DESC")
    List<CommentWithUserName> getByPostAndUserSortedDateDesc(int postID, String userID);

    @Insert
    void insertAll(Comment... comment);

    @Query("DELETE FROM comments")
    void deleteEverything();
}
