package com.salesianos.housify.ui.property.map;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.salesianos.housify.R;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.util.TextInputAutoCompleteTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectLocationFragment extends Fragment {

    private View view;
    private Context ctx;
    private TextInputAutoCompleteTextView input_location;
    private Button btn_search;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_location, container, false);
        getViewIds();
        return view;
    }

    private void getViewIds() {
        input_location = view.findViewById(R.id.input_location);
        btn_search = view.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(v -> {
            if (validateField()) {
                searchLocation();
            }
        });
    }

    private void searchLocation() {
        String location = input_location.getText().toString();
        List<Address> loc = new ArrayList<>();
        try {
             loc = new Geocoder(ctx).getFromLocationName(location, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!loc.isEmpty()) {
            String coords = loc.get(0).getLatitude() +  "," +  loc.get(0).getLongitude();
            startActivity(new Intent(getActivity(), PropertyMapsActivity.class).putExtra("coords", coords));
        } else {
            input_location.setError("We can't find location, try with different one.");
        }
    }

    private boolean validateField() {
        boolean valid = true;
        String location = input_location.getText().toString();
        if (location.isEmpty() || location.length() < 5) {
            input_location.setError("Location must have at least 5 characters..");
            valid = false;
        } else {
            input_location.setError(null);
        }
        return valid;
    }

}
