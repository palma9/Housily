package com.salesianos.housify.data.response;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class CategoryResponse implements Serializable {
    private String id;
    private String name;

    public CategoryResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
