package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.CommentWithUserName;

import java.util.List;

@Dao
public interface CommentDao {

    // Is used in cascade deletion
    @Query("SELECT id FROM comments WHERE post_id = (:postID)")
    List<Integer> getAllCommentsIDByPostID(int postID);

    @Query("SELECT comments.*, users.name FROM comments JOIN users ON users.id = comments.user_id" +
            " WHERE comments.post_id = (:postID)" +
            " ORDER BY stamp DESC")
    List<CommentWithUserName> getCommentsByPostSortedDateDesc(int postID);

    @Insert
    void insertAll(Comment... comment);

    @Query("DELETE FROM comments")
    void deleteEverything();
}
