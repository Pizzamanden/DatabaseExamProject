package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.BigFuckPost;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.PostJoinUser;
import com.example.databaseexamproject.room.dataobjects.PostReactions;

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

    @Query("SELECT posts.id AS 'post_id'," +
            " (SELECT type FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'userReaction', " +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 1) AS 'type0Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 2) AS 'type1Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 3) AS 'type2Reactions' " +
            "FROM posts ORDER BY posts.stamp DESC")
    List<PostReactions> getAllPostsReactionsWithUserReactedSortedDesc(String userID);

    @Query("SELECT *, " +
            " (SELECT type FROM reactions WHERE (:userID) = reactions.user_id AND posts.id = reactions.post_id) AS 'userReaction', " +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 1) AS 'type0Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 2) AS 'type1Reactions'," +
            " (SELECT COUNT(*) FROM reactions WHERE post_id = posts.id AND type = 3) AS 'type2Reactions' " +
            "FROM posts JOIN users ON posts.user_id = users.id " +
            "ORDER BY posts.stamp DESC")
    List<BigFuckPost> bigFuck(String userID);



    @Insert
    void insertAll(Post... post);

    @Query("DELETE FROM posts")
    void deleteEverything();
}