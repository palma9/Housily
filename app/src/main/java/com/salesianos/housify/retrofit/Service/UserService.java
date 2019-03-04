package com.salesianos.housify.retrofit.Service;

import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.data.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {

    String BASE_URL = "/users";

    @GET(BASE_URL)
    Call<ResponseContainer<UserResponse>> listAll();

    @GET(BASE_URL + "/{id}")
    Call<UserResponse> getOne(@Path("id") String id);

    @GET(BASE_URL + "/me")
    Call<UserResponse> getMe();

    @PUT(BASE_URL + "/{id}")
    Call<UserResponse> edit(@Path("id") String id);

    @DELETE(BASE_URL + "/{id}")
    Call delete(@Path("id") String id);
}
