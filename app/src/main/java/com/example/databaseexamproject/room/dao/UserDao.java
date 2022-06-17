package com.example.databaseexamproject.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.databaseexamproject.room.dataobjects.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE id LIKE :id LIMIT 1")
    User findByName(String id);

    @Insert
    void insertAll(User... user);

    @Query("DELETE FROM users")
    void deleteEverything();
}

