package com.example.databaseexamproject.room.dataobjects;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class BigFuckPost {

    @Embedded
    public Post post;

    @ColumnInfo(name = "name")
    public String name;

    public int userReaction;

    public int type0Reactions;

    public int type1Reactions;

    public int type2Reactions;

}
