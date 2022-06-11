package com.example.databaseexamproject.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;

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
    void insertAll(Reaction... reaction);

    @Query("DELETE FROM reactions")
    void deleteEverything();
}