package com.example.course;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Users {

    private String userName;
    private String UserLastName;
    private String UserIndentifier;
    private String userId;
    private String id;
    private String phoneNumber;
    private String mail;
    private String employee;
    private String photo;

    public Users() {
        this.userName = userName;
        this.UserLastName = UserLastName;
        this.UserIndentifier = UserIndentifier;
        this.userId = userId;
    }

    public Users(String userName, String UserLastName, String UserIndentifier, String userId) {
        this.userName = userName;
        this.UserLastName = UserLastName;
        this.UserIndentifier = UserIndentifier;
        this.userId = userId;
    }

    public Users(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public Users(String userName, String userId, String employee) {
        this.userName = userName;
        this.userId = userId;
        this.employee = employee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String UserName) {
        this.userName = UserName;
    }

    public String getUserLastName() {
        return UserLastName;
    }

    public void setUserLastName(String UserLastName) {
        this.UserLastName = UserLastName;
    }

    public String getUserIndentifier() {
        return UserIndentifier;
    }

    public void setUserIndentifier(String UserIndentifier) {
        this.UserIndentifier = UserIndentifier;
    }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPhoneNumber() { return phoneNumber; }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("photo", photo);
        result.put("mail", mail);
        result.put("phone", phoneNumber);
        result.put("employee", employee);

        return result;
    }
}

