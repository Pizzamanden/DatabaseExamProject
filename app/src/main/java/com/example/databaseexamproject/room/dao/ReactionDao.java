package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.Reaction;

@Dao
public interface ReactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Reaction... reaction);

    @Query("DELETE FROM reactions")
    void deleteEverything();
}