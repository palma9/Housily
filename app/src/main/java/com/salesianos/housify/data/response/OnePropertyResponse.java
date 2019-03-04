package com.salesianos.housify.data.response;

import java.io.Serializable;
import java.util.List;

public class OnePropertyResponse implements Serializable {
    public String id;
    public String title;
    public String description;
    public double price;
    public int rooms;
    public int size;
    public CategoryResponse categoryId;
    public String address;
    public int zipcode;
    public String city;
    public String province;
    public String loc;
    public List<String> photos;
    public String isFav;
    public UserResponse ownerId;
}