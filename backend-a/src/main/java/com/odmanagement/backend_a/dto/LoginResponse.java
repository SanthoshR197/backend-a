package com.odmanagement.backend_a.dto;

public class LoginResponse {

    private String token;
    private String role;
    private Object userDetails;

    public LoginResponse(String token, String role, Object userDetails) {
        this.token = token;
        this.role = role;
        this.userDetails = userDetails;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public Object getUserDetails() {
        return userDetails;
    }
}
