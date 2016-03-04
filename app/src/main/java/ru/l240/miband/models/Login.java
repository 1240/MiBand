package ru.l240.miband.models;

/**
 * @author Alexander Popov created on 21.09.2015.
 */
public class Login {
    private String username;

    private String password;

    public Login(String usename, String password) {
        this.username = usename;
        this.password = password;
    }

    public Login() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
