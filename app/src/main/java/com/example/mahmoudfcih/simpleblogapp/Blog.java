package com.example.mahmoudfcih.simpleblogapp;

/**
 * Created by mahmoud on 2/21/2017.
 */

public class Blog {
    private String title,desc,image,username,date,address,uid;

    public Blog()
    {

    }
    public Blog(String title, String image, String desc,String username,String date,String address,String uid) {
        this.title = title;
        this.image = image;
        this.desc = desc;
        this.username=username;
        this.date=date;
        this.address=address;
        this.uid=uid;

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
