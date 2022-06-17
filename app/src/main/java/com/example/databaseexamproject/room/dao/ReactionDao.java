package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Reaction... reaction);

    @Query("DELETE FROM reactions")
    void deleteEverything();
}