package com.salesianos.housify.ui.property.details;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.salesianos.housify.R;
import com.salesianos.housify.data.response.OnePropertyResponse;
import com.salesianos.housify.data.response.PhotoResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.data.response.ResponseOneContainer;
import com.salesianos.housify.retrofit.Service.PhotoService;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.property.edit.PropertyEditActivity;
import com.salesianos.housify.util.UtilToken;
import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyDetailsActivity extends AppCompatActivity {

    // View
    private String propertyId;
    private TextView tv_details_title, tv_details_price, tv_details_room,
            tv_details_met, tv_details_city, tv_details_province,
            tv_details_address, tv_details_zipcode, tv_details_description;
    private FloatingActionMenu fab_my_property_options;
    private FloatingActionButton fab_details_edit_property, fab_details_delete_property, fab_details_add_image;
    private ProgressBar details_progress_bar;
    private LinearLayout details_content;
    private MapView mapView;
    private GoogleMap map;

    // Photos
    private Uri uriSelected;
    public static final int READ_REQUEST_CODE = 42;
    SliderLayout sliderLayout;

    // Property
    private OnePropertyResponse p;
    public static final int EDIT_REQUEST_CODE = 203;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getStringExtra("id") == null)
            finish();

        setViewIds();
        getViewDetails();
    }

    private void setViewIds() {
        setContentView(R.layout.activity_property_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setMap();

        tv_details_title = findViewById(R.id.tv_details_title);
        tv_details_price = findViewById(R.id.tv_details_price);
        tv_details_room = findViewById(R.id.tv_details_room);
        tv_details_met = findViewById(R.id.tv_details_met);
        tv_details_city = findViewById(R.id.tv_details_city);
        tv_details_province = findViewById(R.id.tv_details_province);
        tv_details_address = findViewById(R.id.tv_details_address);
        tv_details_zipcode = findViewById(R.id.tv_details_zipcode);
        tv_details_description = findViewById(R.id.tv_details_description);
        fab_my_property_options = findViewById(R.id.fab_my_property_options);
        fab_details_edit_property = findViewById(R.id.fab_details_edit_property);
        fab_details_delete_property = findViewById(R.id.fab_details_delete_property);
        fab_details_add_image = findViewById(R.id.fab_details_add_image);
        details_progress_bar = findViewById(R.id.details_progress_bar);
        details_content = findViewById(R.id.details_content);
        propertyId = getIntent().getStringExtra("id");

        sliderLayout = findViewById(R.id.imageSlider);
        sliderLayout.setAutoScrolling(false);
    }

    private void setPropertyDetails() {
        tv_details_title.setText(p.title);
        tv_details_price.setText(String.valueOf(p.price));
        tv_details_room.setText(String.valueOf(p.rooms));
        tv_details_met.setText(String.valueOf(p.size));
        tv_details_city.setText(p.city);
        tv_details_province.setText(p.province);
        tv_details_address.setText(p.address);
        tv_details_zipcode.setText(String.valueOf(p.zipcode));
        tv_details_description.setText(p.description);
        if (UtilToken.getId(this) != null)
            if (p.ownerId.getId().equals(UtilToken.getId(this)))
                fab_my_property_options.setVisibility(View.VISIBLE);
        fab_details_add_image.setOnClickListener(v -> performFileSearch());
        fab_details_delete_property.setOnClickListener(v -> dialogDelete());
        fab_details_edit_property.setOnClickListener(v ->
                startActivityForResult(new Intent(this, PropertyEditActivity.class)
                        .putExtra("property", p), EDIT_REQUEST_CODE));
        String[] latlng = p.loc.split(",");
        setMapPosition(new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1])));
        setSliderViews();
    }

    private void dialogDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Deleting " + p.title)
                .setMessage("Are you sure you want to delete this property?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Call<ResponseContainer> call = ServiceGenerator.createService(PropertyService.class, UtilToken.getToken(PropertyDetailsActivity.this), AuthType.JWT).delete(propertyId);
                    call.enqueue(new Callback<ResponseContainer>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseContainer> call, @NonNull Response<ResponseContainer> response) {
                            if (response.code() != 204) {
                                Toast.makeText(PropertyDetailsActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
                            } else {
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseContainer> call, @NonNull Throwable t) {
                            Log.e("Network Failure", t.getMessage());
                            Toast.makeText(PropertyDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void getViewDetails() {
        details_content.setVisibility(View.GONE);
        details_progress_bar.setVisibility(View.VISIBLE);
        findViewById(R.id.connection_fails).setVisibility(View.GONE);
        PropertyService service = ServiceGenerator.createService(PropertyService.class);
        Call<ResponseOneContainer> call = service.getOne(propertyId);
        call.enqueue(new Callback<ResponseOneContainer>() {
            @Override
            public void onResponse(@NonNull Call<ResponseOneContainer> call, @NonNull Response<ResponseOneContainer> response) {
                if (response.code() != 200) {
                    Toast.makeText(PropertyDetailsActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.connection_fails).setVisibility(View.VISIBLE);
                    details_progress_bar.setVisibility(View.GONE);
                } else {
                    details_content.setVisibility(View.VISIBLE);
                    details_progress_bar.setVisibility(View.GONE);
                    p = Objects.requireNonNull(response.body()).rows;
                    setPropertyDetails();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseOneContainer> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(PropertyDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                findViewById(R.id.connection_fails).setVisibility(View.VISIBLE);
                details_progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void setMap() {
        mapView = findViewById(R.id.mv_details_map);
        if (mapView != null) {
            mapView.onCreate(null);  //Don't forget to call onCreate after get the mapView!
            mapView.getMapAsync(googleMap -> map = googleMap);
            MapsInitializer.initialize(this);
        }
    }

    private void setMapPosition(LatLng coords) {
        map.clear();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 18));
        map.addMarker(new MarkerOptions().position(coords));
    }

    public void uploadPhoto() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriSelected);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            int cantBytes;
            byte[] buffer = new byte[1024 * 4];

            while ((cantBytes = bufferedInputStream.read(buffer, 0, 1024 * 4)) != -1) {
                baos.write(buffer, 0, cantBytes);
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(Objects.requireNonNull(getContentResolver().getType(uriSelected))), baos.toByteArray());

            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "photo", requestFile);

            RequestBody propertyId = RequestBody.create(MultipartBody.FORM, p.id);

            PhotoService servicePhoto = ServiceGenerator.createService(PhotoService.class, UtilToken.getToken(this), AuthType.JWT);
            Call<PhotoResponse> callPhoto = servicePhoto.upload(body, propertyId);
            callPhoto.enqueue(new Callback<PhotoResponse>() {
                @Override
                public void onResponse(@NonNull Call<PhotoResponse> call, @NonNull Response<PhotoResponse> response) {
                    if (response.isSuccessful()) {
                        p.photos.add(Objects.requireNonNull(response.body()).getId());
                    } else {
                        Log.e("Upload error", Objects.requireNonNull(response.errorBody()).toString());
                    }

                }

                @Override
                public void onFailure(@NonNull Call<PhotoResponse> call, @NonNull Throwable t) {
                    Log.e("Upload error", t.getMessage());
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void setSliderViews() {
        SliderView sliderView = new DefaultSliderView(this);
        if (!p.photos.isEmpty())
            for (String image : p.photos) {
                sliderView.setImageUrl(image);
                sliderView.setImageScaleType(ImageView.ScaleType.FIT_XY);
                sliderLayout.addSliderView(sliderView);
            }
        else {
            sliderView.setImageDrawable(R.drawable.no_home);
            sliderView.setImageScaleType(ImageView.ScaleType.FIT_XY);
            sliderLayout.addSliderView(sliderView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("Filechooser URI", "Uri: " + Objects.requireNonNull(uri).toString());
            }
            uriSelected = uri;
            uploadPhoto();
        } else if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getViewDetails();
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
