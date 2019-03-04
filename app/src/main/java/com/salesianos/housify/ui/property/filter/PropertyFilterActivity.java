package com.salesianos.housify.ui.property.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.salesianos.housify.R;
import com.salesianos.housify.data.dto.PropertyFilterDto;
import com.salesianos.housify.data.response.CategoryResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.retrofit.Service.CategoryService;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.property.PropertyFragment;
import com.salesianos.housify.util.TextInputAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyFilterActivity extends AppCompatActivity {

    private TextInputEditText input_min_price, input_max_price, input_min_met, input_max_met, input_zipcode;
    private TextInputAutoCompleteTextView input_province, input_city;
    private List<CheckBox> checkBoxRooms = new ArrayList<>();
    private String selectedCategoryId;

    private Spinner spinner_categories;
    private List<CategoryResponse> listCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_filter);
        setViewIds();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    makeResult(RESULT_CANCELED);
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setViewIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btn_set_filter = findViewById(R.id.btn_set_filter);
        btn_set_filter.setOnClickListener(view -> makeResult(RESULT_OK));

        input_min_price = findViewById(R.id.input_min_price);
        input_max_price = findViewById(R.id.input_max_price);
        input_min_met = findViewById(R.id.input_min_met);
        input_max_met = findViewById(R.id.input_max_met);
        input_zipcode = findViewById(R.id.input_zipcode);

        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_one));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_two));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_three));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_four));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_five));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_six));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_seven));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_eight));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_nine));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_ten));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_eleven));
        checkBoxRooms.add(findViewById(R.id.checkbox_rooms_twelve));

        input_province = findViewById(R.id.input_province);
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.Provinces));
        input_province.setThreshold(1);
        input_province.setAdapter(provinceAdapter);
        input_city = findViewById(R.id.input_city);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.Cities));
        input_city.setThreshold(1);
        input_city.setAdapter(cityAdapter);

        spinner_categories = findViewById(R.id.spinner_categories);
        spinner_categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = listCategories.get(position).getId();
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

                    listCategories.add(new CategoryResponse("", "Choose an option"));
                    Collections.reverse(listCategories);

                    ArrayAdapter<CategoryResponse> adapter = new ArrayAdapter<>(PropertyFilterActivity.this, android.R.layout.simple_spinner_dropdown_item, listCategories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_categories.setAdapter(adapter);
                } else {
                    Toast.makeText(PropertyFilterActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseContainer<CategoryResponse>> call, @NonNull Throwable t) {
                Toast.makeText(PropertyFilterActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeResult(int result) {
        Intent i = new Intent();
        PropertyFilterDto propertyFilter = new PropertyFilterDto();
        propertyFilter.setCategoryId(selectedCategoryId);
        propertyFilter.setMaxPrice(input_max_price.getText().toString());
        propertyFilter.setMinPrice(input_min_price.getText().toString());
        propertyFilter.setRooms(getCheckedRooms());
        propertyFilter.setMinSize(input_min_met.getText().toString());
        propertyFilter.setMaxSize(input_max_met.getText().toString());
        propertyFilter.setProvince(input_province.getText().toString());
        propertyFilter.setCity(input_city.getText().toString());
        propertyFilter.setZipcode(input_zipcode.getText().toString());

        i.putExtra(PropertyFragment.PROPERTY_FILTER, propertyFilter);
        setResult(result, i);
        finish();
    }

    private String getCheckedRooms() {
        List<String> rooms = new ArrayList<>();
        for (CheckBox cb : checkBoxRooms) {
            if (cb.isChecked()) {
                rooms.add(cb.getText().toString());
            }
        }
        return Arrays.toString(rooms.toArray()).replace("[", "").replace("]", "");
    }

}
