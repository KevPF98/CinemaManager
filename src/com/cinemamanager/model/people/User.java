package com.cinemamanager.model.people;

import com.cinemamanager.iface.Identifiable;

import java.util.Objects;

public final class User implements Identifiable <Integer> {

    private final int userId;
    private Account account;
    private PersonalData personalData;

    public User(int userId, Account account, PersonalData personalData) {
        this.userId = userId;
        this.account = account;
        this.personalData = personalData;
    }

    public Integer getId() {
        return userId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public String toString() {
        return "ID: " + userId + ".\n" + account.toString() + personalData.toString();
    }
}
