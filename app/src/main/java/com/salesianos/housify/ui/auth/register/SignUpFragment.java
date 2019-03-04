package com.salesianos.housify.ui.auth.register;

import android.app.ProgressDialog;
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

import com.salesianos.housify.R;
import com.salesianos.housify.data.dto.RegisterDto;
import com.salesianos.housify.data.response.LoginResponse;
import com.salesianos.housify.retrofit.Service.AuthService;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.auth.login.LoginFragment;
import com.salesianos.housify.ui.dashboard.MainActivity;
import com.salesianos.housify.util.UtilToken;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    // View Components
    private View view;
    private Button btn_signup;
    private EditText input_name, input_email, input_password, input_reEnterPassword;
    private TextView link_login;

    public SignUpFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        setViewComponents();
        return view;
    }

    private void setViewComponents() {
        btn_signup = view.findViewById(R.id.btn_signup);
        input_name = view.findViewById(R.id.input_name);
        input_email = view.findViewById(R.id.input_email);
        input_password = view.findViewById(R.id.input_password);
        input_reEnterPassword = view.findViewById(R.id.input_reEnterPassword);
        link_login = view.findViewById(R.id.link_login);
        btn_signup.setOnClickListener(v -> signup());
        link_login.setOnClickListener(v -> Objects.requireNonNull(getFragmentManager()).beginTransaction().replace(R.id.auth_container, new LoginFragment()).commit());
    }

    private void signup() {

        if (!validateFields()) {
            onSignupFailed();
            return;
        }

        btn_signup.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String username_txt = input_email.getText().toString();
        String password_txt = input_password.getText().toString();
        String email_txt = input_email.getText().toString();
        RegisterDto register = new RegisterDto(username_txt, email_txt, password_txt);
        AuthService service = ServiceGenerator.createService(AuthService.class);
        Call<LoginResponse> call = service.doSignUp(register);
        call.enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.code() != 201) {
                    Log.e("RequestError", response.message());
                    Toast.makeText(getContext(), "Error while trying to SignUp", Toast.LENGTH_SHORT).show();
                } else {
                    UtilToken.setToken(Objects.requireNonNull(getContext()), Objects.requireNonNull(response.body()).getToken());
                    UtilToken.setId(getContext(), response.body().getUser().getId());
                    progressDialog.dismiss();
                    btn_signup.setEnabled(true);
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("NetworkFailure", t.getMessage());
                Toast.makeText(getContext(), "Error. Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSignupFailed() {
        Toast.makeText(getActivity(), "SignUp failed", Toast.LENGTH_LONG).show();
        btn_signup.setEnabled(true);
    }

    private boolean validateFields() {
        boolean valid = true;

        String email = input_email.getText().toString();
        String password = input_password.getText().toString();
        String password_confirm = input_reEnterPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError("enter a valid email address");
            valid = false;
        } else {
            input_email.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            input_password.setError("Password must contains at least 6 characters");
            valid = false;
        } else {
            input_password.setError(null);
        }

        if (password_confirm.isEmpty() || !password_confirm.equals(password)) {
            input_reEnterPassword.setError("Passwords do not match");
            valid = false;
        } else {
            input_reEnterPassword.setError(null);
        }

        return valid;
    }

}
