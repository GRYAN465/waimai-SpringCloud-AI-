package com.cloudtakeout.user.dto;

public class UpdateUserProfileRequest {
    private String name;
    private String phone;
    private String tastePreference;
    private String avatar;
    private String bio;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTastePreference() {
        return tastePreference;
    }

    public void setTastePreference(String tastePreference) {
        this.tastePreference = tastePreference;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
