package com.salesianos.housify.ui.auth.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.salesianos.housify.R;
import com.salesianos.housify.data.response.LoginResponse;
import com.salesianos.housify.retrofit.Service.AuthService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.auth.register.SignUpFragment;
import com.salesianos.housify.ui.dashboard.MainActivity;
import com.salesianos.housify.util.UtilToken;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private Context ctx;
    // View Components
    private View view;
    private Button btn_login;
    private TextInputEditText input_email, input_password;

    public LoginFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        setViewComponents();
        return view;
    }

    private void setViewComponents() {
        btn_login = view.findViewById(R.id.btn_login);
        input_email = view.findViewById(R.id.input_email);
        input_password = view.findViewById(R.id.input_password);
        TextView link_signup = view.findViewById(R.id.link_signup);
        btn_login.setOnClickListener(v -> login());
        link_signup.setOnClickListener(v -> getFragmentManager().beginTransaction().replace(R.id.auth_container, new SignUpFragment()).commit());
    }

    private void login() {
        if (!validateFields()) {
            onLoginFailed();
            return;
        }

        btn_login.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ctx,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username_txt = input_email.getText().toString();
        String password_txt = input_password.getText().toString();
        String credentials = Credentials.basic(username_txt, password_txt);
        System.out.println(username_txt);
        System.out.println(password_txt);
        System.out.println(credentials);
        AuthService service = ServiceGenerator.createService(AuthService.class, credentials, AuthType.BASIC);
        Call<LoginResponse> call = service.doLogin(credentials);
        call.enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.code() != 201) {
                    progressDialog.dismiss();
                    btn_login.setEnabled(true);
                    Log.e("RequestError", response.message());
                    Toast.makeText(ctx, "Error while trying to login", Toast.LENGTH_SHORT).show();
                } else {
                    UtilToken.setToken(ctx, Objects.requireNonNull(response.body()).getToken());
                    UtilToken.setId(ctx, response.body().getUser().getId());
                    System.out.println(UtilToken.getId(ctx));
                    progressDialog.dismiss();
                    btn_login.setEnabled(true);
                    getActivity().finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                btn_login.setEnabled(true);
                Log.e("NetworkFailure", t.getMessage());
                Toast.makeText(ctx, "Error. Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLoginFailed() {
        Toast.makeText(ctx, "Login failed", Toast.LENGTH_LONG).show();
        btn_login.setEnabled(true);
    }

    private boolean validateFields() {
        boolean valid = true;

        String email = input_email.getText().toString();
        String password = input_password.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError("enter a valid email address");
            valid = false;
        } else {
            input_email.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            input_password.setError("Password must containt at least 6 characters");
            valid = false;
        } else {
            input_password.setError(null);
        }

        return valid;
    }

}
