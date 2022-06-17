package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.PostWithReactions;
import com.example.databaseexamproject.room.dataobjects.Post;

import java.util.Date;
import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts WHERE id = (:postID)")
    Post getPostByID(int postID);

    @Query("SELECT * FROM posts ORDER BY stamp DESC")
    List<Post> getAllSortedDateDesc();

    @Query("SELECT * FROM posts WHERE posts.user_id = (:userID) ORDER BY stamp DESC")
    List<Post> getByUserSortedDateDesc(String userID);

    @Query("SELECT * FROM posts WHERE stamp < (:specificDate) ORDER BY stamp DESC")
    List<Post> getBeforeDateSortedDateDesc(Date specificDate);

    @Query("SELECT * FROM posts WHERE stamp >= (:specificDate) ORDER BY stamp DESC")
    List<Post> getAfterDateSortedDateDesc(Date specificDate);

    @Query("SELECT posts.*, users.name, " +
            " (SELECT type FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'userReaction', " +
            " (SELECT stamp FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'reactionStamp', " +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 1) AS 'type1Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 2) AS 'type2Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 3) AS 'type3Reactions'" +
            "FROM posts JOIN users ON posts.user_id = users.id " +
            "ORDER BY posts.stamp DESC")
    List<PostWithReactions> getAllPostsWithReactionByUserAndAllReactionsCounter(String userID);

    @Query("SELECT posts.*, users.name, " +
            " (SELECT type FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'userReaction', " +
            " (SELECT stamp FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'reactionStamp', " +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 1) AS 'type1Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 2) AS 'type2Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE posts.id = reactions.post_id AND type = 3) AS 'type3Reactions'" +
            "FROM posts JOIN users ON posts.user_id = users.id " +
            "WHERE posts.id = (:postID) ORDER BY posts.stamp DESC")
    PostWithReactions getSpecificPostWithReactionByUserAndAllReactionsCounter(String userID, int postID);


    @Insert
    void insertAll(Post... post);

    @Query("DELETE FROM posts")
    void deleteEverything();
}