package com.example.databaseexamproject.room.dataobjects;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import java.util.Date;

public class PostWithReactions {

    @Embedded
    public Post post;

    @ColumnInfo(name = "name")
    public String name;

    public int userReaction;

    @ColumnInfo(name = "reactionStamp")
    public Date stamp;

    public int type1Reactions;

    public int type2Reactions;

    public int type3Reactions;


}
