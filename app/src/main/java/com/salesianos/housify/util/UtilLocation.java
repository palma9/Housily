package com.salesianos.housify.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.salesianos.housify.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilLocation {

    public static void setLocation(Context mContext, String location) {
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences(
                        mContext.getString(R.string.sharedpreferences_filename),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mContext.getString(R.string.default_location), location);
        editor.commit();
    }

    public static List<Double> getLocation(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                mContext.getString(R.string.sharedpreferences_filename),
                Context.MODE_PRIVATE
        );

        String location = sharedPreferences.getString(mContext.getString(R.string.default_location), null);
        if (location != null) {
            String[] newLoc = location.split(",");
            return Arrays.asList(Double.parseDouble(newLoc[0]), Double.parseDouble(newLoc[1]));
        }
        return null;
    }
}
