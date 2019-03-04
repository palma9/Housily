package com.salesianos.housify.ui.auth;

import android.os.Bundle;

import com.salesianos.housify.R;
import com.salesianos.housify.ui.auth.login.LoginFragment;
import com.salesianos.housify.ui.auth.register.SignUpFragment;

import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (getIntent().getBooleanExtra("isLogin", true))
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_container, new LoginFragment())
                    .commit();
        else
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_container, new SignUpFragment())
                    .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
