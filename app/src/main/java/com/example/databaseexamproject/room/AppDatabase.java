package com.example.databaseexamproject.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Designate how and what we can interact with here!
@Database(entities = {User.class, Post.class, Reaction.class, Comment.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PostDao postDao();
    public abstract ReactionDao reactionDao();
    public abstract CommentDao commentDao();
}
