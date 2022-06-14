package com.example.databaseexamproject.room.dataobjects;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import java.util.Date;

public class BigFuckPost {

    @Embedded
    public Post post;

    @ColumnInfo(name = "name")
    public String name;

    public int contentType;

    public int userReaction;

    public int type1Reactions;

    public int type2Reactions;

    public int type3Reactions;

    @ColumnInfo(name = "reactionStamp")
    public Date stamp;
}
