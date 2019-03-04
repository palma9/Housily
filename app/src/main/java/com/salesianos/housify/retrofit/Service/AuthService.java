package com.salesianos.housify.retrofit.Service;

import com.salesianos.housify.data.dto.RegisterDto;
import com.salesianos.housify.data.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {

    @POST("/auth")
    Call<LoginResponse> doLogin(@Header("Authorization") String authorization);


    @POST("/users")
    Call<LoginResponse> doSignUp(@Body RegisterDto register);
}
