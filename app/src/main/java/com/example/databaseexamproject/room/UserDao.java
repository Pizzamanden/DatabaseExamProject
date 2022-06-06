package com.example.databaseexamproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE name = (:name)")
    List<User> getByName(String name);

    @Insert
    void insertUser(User user);
}

