package com.example.databaseexamproject.room.dataobjects;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import java.util.Date;

public class PostWithReactions {

    // The post itself
    // We also have the creators ID here
    @Embedded
    public Post post;

    // The name of the creator of the post
    @ColumnInfo(name = "name")
    public String name;

    // The loggedUserID's reaction (if any)
    public int userReaction;

    // The timestamp of the userReaction (if any, otherwise it will be null)
    public Date reactionStamp;

    // These 3 are the counts of their type of reaction
    public int type1Reactions;
    public int type2Reactions;
    public int type3Reactions;

}
