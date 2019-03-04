package com.salesianos.housify.data.response;

public class LoginResponse {
    private String token;
    private UserAuthResponse user;

    public String getToken() {
        return token;
    }

    public UserAuthResponse getUser() {
        return user;
    }
}
