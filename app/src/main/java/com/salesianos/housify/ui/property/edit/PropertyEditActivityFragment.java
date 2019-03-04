package com.salesianos.housify.ui.property.edit;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.salesianos.housify.R;
import com.salesianos.housify.data.model.Property;
import com.salesianos.housify.data.response.CategoryResponse;
import com.salesianos.housify.data.response.OnePropertyResponse;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.retrofit.Service.CategoryService;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.util.PlaceSearchAdapter;
import com.salesianos.housify.util.TextInputAutoCompleteTextView;
import com.salesianos.housify.util.UtilToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PropertyEditActivityFragment extends Fragment implements OnMapReadyCallback {

    private Context ctx;
    private View view;
    private TextInputEditText input_title, input_price, input_size, input_rooms, input_description;
    private TextInputAutoCompleteTextView input_address;
    private String selectedCategoryId;

    private Spinner spinner_categories;
    private List<CategoryResponse> listCategories = new ArrayList<>();
    private List<String> categoriesIds;

    private MapView mapView;
    private GoogleMap mMap;

    private OnePropertyResponse propertyResponse;
    private Address address;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_property_create, container, false);
        propertyResponse = (OnePropertyResponse) Objects.requireNonNull(Objects.requireNonNull(getActivity()).getIntent().getExtras()).getSerializable("property");
        setViewIds();
        setMap();
        setViewContent();
        return view;
    }

    private void setViewIds() {
        input_title = view.findViewById(R.id.input_title);
        input_price = view.findViewById(R.id.input_price);
        input_size = view.findViewById(R.id.input_size);
        input_rooms = view.findViewById(R.id.input_rooms);
        input_address = view.findViewById(R.id.input_address);
        input_description = view.findViewById(R.id.input_description);
        Button btn_create = view.findViewById(R.id.btn_create);
        btn_create.setText(getString(R.string.edit_property));
        btn_create.setOnClickListener(v -> propertyEdit());
        input_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    new getLocationFromAddress().execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        input_address.setOnItemClickListener((parent, v, position, id) -> setMapPosition(new LatLng(address.getLatitude(), address.getLongitude())));
        spinner_categories = view.findViewById(R.id.spinner_categories);
        spinner_categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categoriesIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadAllCategories();
    }

    private void loadAllCategories() {
        CategoryService service = ServiceGenerator.createService(CategoryService.class);
        Call<ResponseContainer<CategoryResponse>> call = service.listAll();

        call.enqueue(new Callback<ResponseContainer<CategoryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseContainer<CategoryResponse>> call, @NonNull Response<ResponseContainer<CategoryResponse>> response) {
                if (response.isSuccessful()) {
                    listCategories = Objects.requireNonNull(response.body()).getRows();
                    categoriesIds = new ArrayList<>();

                    ArrayAdapter<CategoryResponse> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, listCategories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_categories.setAdapter(adapter);

                    for (int i = 0; i < listCategories.size(); i++) {
                        categoriesIds.add(listCategories.get(i).getId());
                        if (propertyResponse.categoryId != null)
                            if (listCategories.get(i).getId().equals(propertyResponse.categoryId.getId()))
                                spinner_categories.setSelection(i);
                    }
                } else {
                    Toast.makeText(ctx, "Error loading categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseContainer<CategoryResponse>> call, @NonNull Throwable t) {
                Toast.makeText(ctx, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setViewContent() {
        input_title.setText(propertyResponse.title);
        input_price.setText(String.valueOf(propertyResponse.price));
        input_size.setText(String.valueOf(propertyResponse.size));
        input_rooms.setText(String.valueOf(propertyResponse.rooms));
        input_description.setText(propertyResponse.description);
        input_address.setText(propertyResponse.address);
    }

    private void setMap() {
        mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);  //Don't forget to call onCreate after get the mapView!
            mapView.getMapAsync(this);
        }
    }

    private void setMapPosition(LatLng coords) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 18));
        mMap.addMarker(new MarkerOptions().position(coords));
    }

    private void propertyEdit() {
        if (validationFields()) {
            Property property = new Property(Objects.requireNonNull(input_title.getText()).toString(),
                    Objects.requireNonNull(input_description.getText()).toString(),
                    Float.parseFloat(Objects.requireNonNull(input_price.getText()).toString()),
                    Integer.parseInt(Objects.requireNonNull(input_rooms.getText()).toString()),
                    Integer.parseInt(Objects.requireNonNull(input_size.getText()).toString()),
                    selectedCategoryId,
                    address.getThoroughfare().concat(", " + address.getFeatureName()),
                    address.getPostalCode(),
                    address.getLocality(),
                    address.getSubAdminArea(),
                    address.getLatitude() + ", " + address.getLongitude());

            String jwt = UtilToken.getToken(ctx);
            final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                    R.style.AppTheme_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Property...");
            progressDialog.show();

            PropertyService service = ServiceGenerator.createService(PropertyService.class, jwt, AuthType.JWT);
            Call<Property> call = service.edit(propertyResponse.id, property);
            call.enqueue(new retrofit2.Callback<Property>() {
                @Override
                public void onResponse(@NonNull Call<Property> call, @NonNull Response<Property> response) {
                    if (response.code() != 200) {
                        Log.e("RequestError", response.message());
                        Toast.makeText(getContext(), "Error while trying to create", Toast.LENGTH_LONG).show();
                    } else {
                        progressDialog.dismiss();
                        makeResult(RESULT_OK);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Property> call, @NonNull Throwable t) {
                    Log.e("NetworkFailure", t.getMessage());
                    Toast.makeText(getContext(), "Error. Can't connect to server", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean validationFields() {
        boolean valid = true;
        String title = Objects.requireNonNull(input_title.getText()).toString();
        String description = Objects.requireNonNull(input_description.getText()).toString();
        String price = Objects.requireNonNull(input_price.getText()).toString();
        String rooms = Objects.requireNonNull(input_rooms.getText()).toString();
        String size = Objects.requireNonNull(input_size.getText()).toString();
        String loc = input_address.getText().toString();
        Address fullAddress = address;

        if (title.isEmpty() || title.length() < 3) {
            input_title.setError("Title must have at least 3 characters..");
            valid = false;
        } else {
            input_title.setError(null);
        }
        if (selectedCategoryId.isEmpty()) {
            valid = false;
        }
        if (description.isEmpty()) {
            input_description.setError("Description can't be empty.");
            valid = false;
        } else {
            input_description.setError(null);
        }

        if (price.isEmpty()) {
            input_price.setError("Price need to have a value.");
            valid = false;
        } else {
            input_price.setError(null);
        }

        if (rooms.isEmpty()) {
            input_rooms.setError("Rooms need to have a value.");
            valid = false;
        } else {
            input_rooms.setError(null);
        }
        if (size.isEmpty()) {
            input_size.setError("Size need to have a value.");
            valid = false;
        } else {
            input_size.setError(null);
        }
        if (loc.isEmpty()) {
            input_address.setError("Address can't be empty.");
            valid = false;
        } else {
            input_address.setError(null);
        }
        if (fullAddress == null || fullAddress.getAddressLine(0).isEmpty()) {
            input_address.setError("We can't find your address. You need to choose an option.");
            valid = false;
        } else {
            input_address.setError(null);
        }

        return valid;

    }

    private void makeResult(int result) {
        Objects.requireNonNull(getActivity()).setResult(result);
        getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(Objects.requireNonNull(getActivity())) != null) {
                    makeResult(RESULT_CANCELED);
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (propertyResponse.loc != null) {
            String[] locArr = propertyResponse.loc.split(",");
            setMapPosition(new LatLng(Double.parseDouble(locArr[0]), Double.parseDouble(locArr[1])));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getLocationFromAddress extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... strings) {
            Geocoder coder = new Geocoder(ctx);
            List<Address> address;

            try {
                address = coder.getFromLocationName(strings[0], 5);
                if (address == null || address.isEmpty()) {
                    return null;
                }
                return address;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses != null) {
                if (!addresses.isEmpty()) {
                    List<String> addressName = new ArrayList<>();
                    for (Address a : addresses) {
                        addressName.add(a.getAddressLine(0));
                    }
                    PlaceSearchAdapter adapter = new PlaceSearchAdapter(ctx, R.layout.support_simple_spinner_dropdown_item, addressName);
                    input_address.setThreshold(1);
                    input_address.setAdapter(adapter);
                    address = addresses.get(0);
                }
            }
        }
    }
}
