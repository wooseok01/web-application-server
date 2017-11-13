package model;

import java.util.Map;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    public User(Map<String, String> queryMap) {
        this.userId = queryMap.get("userId");
        this.password = queryMap.get("password");
        this.name = queryMap.get("name");
        this.email = queryMap.get("email");
    }

    public User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }
}
