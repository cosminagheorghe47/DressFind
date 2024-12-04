package com.example.dressfind.models;

import java.util.Date;

public class User {

    private String UserId;
    private String FirstName;
    private String LastName;
    private Date RegistrationDate;
    private String Email;

    public User() {
    }

    public User(String userId, String firstName, String lastName, Date registrationDate, String email) {
        UserId = userId;
        FirstName = firstName;
        LastName = lastName;
        RegistrationDate = registrationDate;
        Email = email;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public Date getRegistrationDate() {
        return RegistrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        RegistrationDate = registrationDate;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
