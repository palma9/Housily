package com.salesianos.housify.ui.property.map;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.salesianos.housify.R;
import com.salesianos.housify.data.dto.PropertyFilterDto;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.util.UtilLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng mapLocation = new LatLng(37.3890924, -5.9844589);
    private GoogleMap mMap;

    private Map<String, String> queryData;
    PropertyFilterDto propertyFilterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setFirstCoords();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, 15));
    }

    private void setFirstCoords() {
        String extraCoords = getIntent().getStringExtra("coords");
        String property_name = getIntent().getStringExtra("property_title");
        double property_price = getIntent().getDoubleExtra("property_price", 0);
        if (extraCoords != null && property_name != null) {
            String[] latlng = extraCoords.split(",");
            mapLocation = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
            mMap.addMarker(new MarkerOptions().position(mapLocation).snippet(property_name).title(String.valueOf(property_price + " €"))).showInfoWindow();
        } else if (extraCoords != null) {
            String[] latlng = extraCoords.split(",");
            mapLocation = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
            setQueryData();
        } else if (UtilLocation.getLocation(this) != null) {
            List<Double> utilLatLng = UtilLocation.getLocation(this);
            mapLocation = new LatLng(Objects.requireNonNull(utilLatLng).get(0), utilLatLng.get(1));
            setQueryData();
        } else {
            setQueryData();
        }
    }

    private void showNearbyLocations() {
        PropertyService service = ServiceGenerator.createService(PropertyService.class);
        Call<ResponseContainer<PropertyResponse>> call = service.listAll(queryData);
        call.enqueue(new Callback<ResponseContainer<PropertyResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseContainer<PropertyResponse>> call, @NonNull Response<ResponseContainer<PropertyResponse>> response) {
                if (response.code() != 200) {
                    Toast.makeText(PropertyMapsActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.clear();
                    for (PropertyResponse i : Objects.requireNonNull(response.body()).getRows()) {
                        String[] loc = i.getLoc().split(",");
                        String category;
                        if (i.getCategoryId() != null)
                            category = i.getCategoryId().getName();
                        else
                            category = "No category";

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])))
                                .title(String.valueOf(i.getPrice()))
                                .snippet(i.getTitle() + " | (" + category + ")")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_house_map_icon))
                        ).setTag(i.getId());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseContainer<PropertyResponse>> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(PropertyMapsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setQueryData() {
        queryData = new HashMap<>();
        queryData.put("near", mapLocation.longitude + ", " + mapLocation.latitude);
        queryData.put("limit", "100");
        queryData.put("max_distance", "5000");
        queryData.put("min_distance", "10");
        if (propertyFilterItem != null) {
            if (!propertyFilterItem.getRooms().equals(""))
                queryData.put("rooms", propertyFilterItem.getRooms());
            if (!propertyFilterItem.getCity().equals(""))
                queryData.put("city", propertyFilterItem.getCity());
            if (!propertyFilterItem.getProvince().equals(""))
                queryData.put("province", propertyFilterItem.getProvince());
            if (!propertyFilterItem.getZipcode().equals(""))
                queryData.put("zipcode", propertyFilterItem.getZipcode());
            if (!propertyFilterItem.getMinSize().equals(""))
                queryData.put("min_size", propertyFilterItem.getMinSize());
            if (!propertyFilterItem.getMaxSize().equals(""))
                queryData.put("max_size", propertyFilterItem.getMaxSize());
            if (!propertyFilterItem.getMinPrice().equals(""))
                queryData.put("min_price", propertyFilterItem.getMinPrice());
            if (!propertyFilterItem.getMaxPrice().equals(""))
                queryData.put("max_price", propertyFilterItem.getMaxPrice());
            if (!propertyFilterItem.getCategoryId().equals(""))
                queryData.put("category", propertyFilterItem.getCategoryId());

        }
        showNearbyLocations();
    }
}
