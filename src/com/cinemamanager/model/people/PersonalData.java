package com.cinemamanager.model.people;

import java.util.Objects;

public final class PersonalData {

    private String nationalId;
    private String name;
    private String lastName;
    private String email;
    private String phoneNumber;

    public PersonalData(String nationalId, String name, String lastName, String email, String phoneNumber) {
        this.nationalId = nationalId;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonalData personalData = (PersonalData) o;
        return Objects.equals(nationalId, personalData.nationalId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nationalId);
    }

    @Override
    public String toString() {
        return  "--------------------------\n" +
                "National ID: " + nationalId + ".\n" +
                "Name: " + name + ".\n" +
                "Last name: " + lastName + ".\n" +
                "Email: " + email + ".\n" +
                "Phone number: " + phoneNumber + ".\n" +
                "--------------------------\n" ;
    }
}
