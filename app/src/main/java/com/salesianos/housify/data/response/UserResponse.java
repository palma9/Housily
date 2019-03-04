package com.salesianos.housify.data.response;

import java.io.Serializable;

public class UserResponse implements Serializable {
    private String _id;
    private String email;
    private String name;
    private String picture;
    private String role;

    public String getId() {
        return _id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public String getRole() {
        return role;
    }
}