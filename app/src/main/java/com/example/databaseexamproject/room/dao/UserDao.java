package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.User;

import java.util.List;

@Dao
public interface UserDao {
    //Only used for log
    @Query("SELECT * FROM users")
    List<User> getAll();

    //not in use
    @Query("SELECT * FROM users WHERE name = (:name)")
    List<User> getByName(String name);

    //rename to findById!?!?!
    @Query("SELECT * FROM users WHERE id LIKE :id LIMIT 1")
    User findByName(String id);

    @Insert
    void insertAll(User... user);

    @Query("DELETE FROM users")
    void deleteEverything();
}

