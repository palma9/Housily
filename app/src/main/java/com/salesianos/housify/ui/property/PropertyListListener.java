package com.salesianos.housify.ui.property;

import android.view.View;

import com.salesianos.housify.data.response.PropertyResponse;

public interface PropertyListListener {

    void addToFav(View v, String id);
    void deleteFav(View v, String id);
    void goLoc(View v, PropertyResponse property);
}
