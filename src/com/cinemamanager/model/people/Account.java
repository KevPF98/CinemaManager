package com.cinemamanager.model.people;

import com.cinemamanager.enums.Role;

import java.util.Objects;

public final class Account {

    private int accountId;
    private String nickname;
    private String password;
    private boolean enabled;
    private Role role;

    public Account (int accountId, String nickname, String password, Role role) {
        this.accountId = accountId;
        this.nickname = nickname;
        this.password = password;
        this.enabled = true;
        this.role = role;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return enabled;
    }

    public void activate() {
        if (!enabled) this.enabled = true;
        else System.out.println("This account is already enabled.");
    }

    public void deactivate(){
        if (enabled) this.enabled = false;
        else System.out.println("This account is already disabled.");
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountId);
    }

    @Override
    public String toString() {
        String separator =  "--------------------------\n";
        String string1 = "Nickname: " + nickname + ".\n";
        String string2 = enabled ? "The user has an active account.\n" : "The user does not have an active account.\n";
        String string3 = role == Role.ADMIN ? "Role: admin.\n" : "Role: employee.\n";
        return separator + string1 + string2 + string3 + separator;
    }
}
