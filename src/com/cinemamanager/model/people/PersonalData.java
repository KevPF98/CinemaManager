package com.cinemamanager.model.people;

import com.cinemamanager.iface.Identifiable;

import java.util.Objects;

public final class PersonalData implements Identifiable <String> {

    private String nationalId;
    private String name;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean mustCompleteProfile;

    public PersonalData(String nationalId, String name, String lastName, String email, String phoneNumber, boolean mustCompleteProfile) {
        this.nationalId = nationalId;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.mustCompleteProfile = mustCompleteProfile;
    }

    @Override
    public String getId() {
        return nationalId;
    }

    public void setId(String nationalId) {
        this.nationalId = nationalId;
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

    public boolean isMustCompleteProfile() {
        return mustCompleteProfile;
    }

    public void setMustCompleteProfile(boolean mustCompleteProfile) {
        this.mustCompleteProfile = mustCompleteProfile;
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
                "Phone number: " + phoneNumber + ".\n" ;
    }
}
