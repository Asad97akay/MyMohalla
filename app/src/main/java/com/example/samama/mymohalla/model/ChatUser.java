package com.example.samama.mymohalla.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Class ChatUse is a Java Bean class that represents a single user.
 */
@IgnoreExtraProperties
public class ChatUser implements Serializable {


    public String id;
    public String username;
    public String email;
    public Boolean online, gender;
    public ArrayList<String> room;
    public String location;
    public String profilePicture;
    public String cnic;

    public ChatUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public ChatUser(String id, String username, String email, Boolean online, ArrayList<String> room) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.online = online;
        this.room = room;
    }

    public ChatUser(String id, String username, String email, Boolean online, ArrayList<String> room, String profilePicture, String location, String cnic, Boolean gender) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.online = online;
        this.room = room;
        this.profilePicture = profilePicture;
        this.location = location;
        this.cnic = cnic;
        this.gender = gender;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Boolean isOnline() {
        return online;
    }

    public ArrayList<String> getRoom() {
        return room;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public void setRoom(ArrayList<String> room) {
        this.room = room;
    }

}