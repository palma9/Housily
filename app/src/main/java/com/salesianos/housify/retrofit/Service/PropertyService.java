package com.salesianos.housify.retrofit.Service;

import com.salesianos.housify.data.model.Property;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.data.response.ResponseOneContainer;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface PropertyService {

    String BASE_URL = "/properties";

    @GET(BASE_URL)
    Call<ResponseContainer<PropertyResponse>> listAll(@QueryMap Map<String, String> options);

    @GET(BASE_URL + "/auth")
    Call<ResponseContainer<PropertyResponse>> getAuth(@QueryMap Map<String, String> options);

    @GET(BASE_URL + "/fav")
    Call<ResponseContainer<PropertyResponse>> getFavs(@QueryMap Map<String, String> options);

    @GET(BASE_URL + "/{id}")
    Call<ResponseOneContainer> getOne(@Path("id") String id);

    @GET(BASE_URL + "/mine")
    Call<ResponseContainer<PropertyResponse>> getMine(@QueryMap Map<String, String> options);

    @POST(BASE_URL)
    Call<Property> createProperty(@Body Property property);

    @PUT(BASE_URL + "/{id}")
    Call<Property> edit(@Path("id") String id, @Body Property property);

    @DELETE(BASE_URL + "/{id}")
    Call<ResponseContainer> delete(@Path("id") String id);

    @POST(BASE_URL + "/fav/{id}")
    Call<PropertyResponse> addFav(@Path("id") String id);

    @DELETE(BASE_URL + "/fav/{id}")
    Call<PropertyResponse> removeFav(@Path("id") String id);
}
