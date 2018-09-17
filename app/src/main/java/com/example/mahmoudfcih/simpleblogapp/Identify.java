package com.example.mahmoudfcih.simpleblogapp;

/**
 * Created by mahmoud on 6/13/2017.
 */

public class Identify {
    String childName,userfc,userId,confidence,date,image,personId,location;

    public Identify() {
    }

    public Identify(String childName, String userfc, String userId, String confidence, String date, String image, String personId,String location) {
        this.childName = childName;
        this.userfc = userfc;
        this.userId = userId;
        this.confidence = confidence;
        this.date = date;
        this.image = image;
        this.personId = personId;
        this.location=location;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getUserfc() {
        return userfc;
    }

    public void setUserfc(String userfc) {
        this.userfc = userfc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
