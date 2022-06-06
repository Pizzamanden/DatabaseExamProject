package com.example.databaseexamproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReactionDao {

    @Query("SELECT * FROM reactions")
    List<Reaction> getAll();

    @Query("SELECT * FROM reactions WHERE reactions.post_id = (:postID)")
    List<Reaction> getByPost(int postID);

    @Query("SELECT * FROM reactions WHERE reactions.user_id = (:userID)")
    List<Reaction> getByUser(String userID);

    @Insert
    void insertReaction(Reaction reaction);
}