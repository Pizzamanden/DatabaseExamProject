package com.example.databaseexamproject.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.databaseexamproject.room.dao.CommentDao;
import com.example.databaseexamproject.room.dao.PostDao;
import com.example.databaseexamproject.room.dao.ReactionDao;
import com.example.databaseexamproject.room.dao.UserDao;
import com.example.databaseexamproject.room.dataobjects.Comment;
import com.example.databaseexamproject.room.dataobjects.Post;
import com.example.databaseexamproject.room.dataobjects.Reaction;
import com.example.databaseexamproject.room.dataobjects.User;

// Designate how and what we can interact with here!
@Database(entities = {User.class, Post.class, Reaction.class, Comment.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PostDao postDao();
    public abstract ReactionDao reactionDao();
    public abstract CommentDao commentDao();
}
