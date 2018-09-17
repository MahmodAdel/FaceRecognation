package com.example.mahmoudfcih.simpleblogapp;

/**
 * Created by mahmoud on 3/4/2017.
 */

public class Comment {
    String username,userprofile,time,comment;

    public Comment(String userprofile, String username, String time ,String comment) {
        this.userprofile = userprofile;
        this.username = username;
        this.time = time;
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Comment() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserprofile() {
        return userprofile;
    }

    public void setUserprofile(String userprofile) {
        this.userprofile = userprofile;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
