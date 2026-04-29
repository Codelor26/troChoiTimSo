package com.timso.common.model;

import java.sql.Date;
import lombok.Data;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String fullname;
    private String gender;
    private Date dob;
    private int gold;
    private String avatar;
    private String player_name;

    public User() {
    }

    public int getID() {
        return id;
    }

    public String getUserName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullname;
    }

    public String getGender() {
        return gender;
    }

    public Date getDob() {
        return dob;
    }

    public int getGold() {
        return gold;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPlayerName() {
        return player_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPlayerName(String player_name) {
        this.player_name = player_name;
    }
}
