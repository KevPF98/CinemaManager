package com.cinemamanager.model.people;
import com.cinemamanager.enums.Role;

public final class Account {

    private String nickname;
    private String password;
    private boolean mustChangePassword;
    private boolean enabled;
    private Role role;

    public Account (String nickname, String password, Role role) {
        this.nickname = nickname;
        this.password = password;
        this.enabled = true;
        this.role = role;
        this.mustChangePassword = true;
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

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
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
    public String toString() {
        String separator =  "--------------------------\n";

        String string1 = enabled ? "The user has an active account.\n" : "The user does not have an active account.\n";
        String string2 = role == Role.ADMIN ? "Role: admin.\n" : "Role: employee.\n";
        return separator + string1 + string2;
    }
}
