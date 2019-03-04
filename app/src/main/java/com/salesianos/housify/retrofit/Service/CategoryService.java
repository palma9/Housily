package com.salesianos.housify.retrofit.Service;

import com.salesianos.housify.data.response.CategoryResponse;
import com.salesianos.housify.data.response.ResponseContainer;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryService {

    String BASE_URL = "/categories";

    @GET(BASE_URL)
    Call<ResponseContainer<CategoryResponse>> listAll();

    @GET(BASE_URL + "/{id}")
    Call<CategoryResponse> getOne(@Path("id") String id);

    @PUT(BASE_URL + "/{id}")
    Call<CategoryResponse> edit(@Path("id") String id);

    @DELETE(BASE_URL + "/{id}")
    Call<CategoryResponse> delete(@Path("id") String id);
}
