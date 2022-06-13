package com.example.databaseexamproject.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String userid;
    private String name;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String userid, String name) {

        this.userid = userid;
        this.name = name;
    }

    public String getUserid() { return userid; }

    public String getName() {
        return name;
    }
}