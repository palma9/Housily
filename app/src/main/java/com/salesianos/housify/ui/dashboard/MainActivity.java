package com.salesianos.housify.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.salesianos.housify.R;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.UserResponse;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.Service.UserService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.auth.AuthActivity;
import com.salesianos.housify.ui.property.PropertyFragment;
import com.salesianos.housify.ui.property.PropertyListListener;
import com.salesianos.housify.ui.property.create.PropertyCreateFragment;
import com.salesianos.housify.ui.property.fav.PropertyFavFragment;
import com.salesianos.housify.ui.property.map.PropertyMapsActivity;
import com.salesianos.housify.ui.property.map.SelectLocationFragment;
import com.salesianos.housify.ui.property.mine.PropertyMineFragment;
import com.salesianos.housify.util.UtilToken;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PropertyListListener {

    Fragment f, propertyFragment, propertyMineFragment, propertyFavFragment, propertyCreateFragment;
    UserResponse user;
    // ViewsIDs
    TextView username, email;
    ImageView profilePicture;
    Button btn_logIn, btn_signUp;
    View headerView;
    DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAllViewIds();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (f == null) {
            onNavigationItemSelected(navigationView.getMenu().getItem(0).setChecked(true));
        }
        checkUserLogged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationMenus(item);
        if (f == null) {

            return false;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard_fragment_container, f)
                .commit();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setAllViewIds() {
        createFragments();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        username = headerView.findViewById(R.id.tv_user_name);
        email = headerView.findViewById(R.id.tv_user_email);
        profilePicture = headerView.findViewById(R.id.iv_user_avatar);
        btn_logIn = headerView.findViewById(R.id.btn_goLogin);
        btn_signUp = headerView.findViewById(R.id.btn_goSignUp);
        btn_logIn.setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AuthActivity.class).putExtra("isLogin", true));
        });
        btn_signUp.setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AuthActivity.class).putExtra("isLogin", false));
        });
    }

    private void createFragments() {
        propertyFragment = new PropertyFragment();
        propertyMineFragment = new PropertyMineFragment();
        propertyFavFragment = new PropertyFavFragment();
        propertyCreateFragment = new PropertyCreateFragment();
    }

    private void checkUserLogged() {
        MenuItem nav_logout = navigationView.getMenu().findItem(R.id.nav_logout);
        if (UtilToken.getToken(MainActivity.this) != null) {
            username.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            btn_logIn.setVisibility(View.GONE);
            btn_signUp.setVisibility(View.GONE);
            nav_logout.setVisible(true);
            getUserData();
        } else {
            username.setVisibility(View.GONE);
            email.setVisibility(View.GONE);
            btn_logIn.setVisibility(View.VISIBLE);
            btn_signUp.setVisibility(View.VISIBLE);
            profilePicture.setImageResource(R.drawable.ic_user_avatar);
            nav_logout.setVisible(false);
        }
    }

    private void navigationMenus(MenuItem item) {
        if (UtilToken.getToken(this) != null) {
            switch (item.getItemId()) {
                case R.id.nav_search:
                    f = propertyFragment;
                    break;
                case R.id.nav_favorites:
                    f = propertyFavFragment;
                    break;
                case R.id.nav_mine:
                    f = propertyMineFragment;
                    break;
                case R.id.nav_new_house:
                    f = propertyCreateFragment;
                    break;
                case R.id.nav_logout:
                    UtilToken.setToken(this, null);
                    UtilToken.setId(this, null);
                    onNavigationItemSelected(navigationView.getMenu().getItem(0).setChecked(true));
                    checkUserLogged();
                    break;
            }
        } else {
            switch (item.getItemId()) {
                case R.id.nav_search:
                    f = propertyFragment;
                    break;
                default:
                    startActivity(new Intent(this, AuthActivity.class));
                    break;
            }
        }
    }

    private void getUserData() {
        String jwt = UtilToken.getToken(this);
        UserService service = ServiceGenerator.createService(UserService.class, jwt, AuthType.JWT);
        Call<UserResponse> call = service.getMe();
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.code() != 200) {
                    Toast.makeText(MainActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
                } else {
                    user = response.body();
                    username.setText(Objects.requireNonNull(user).getName());
                    email.setText(user.getEmail());
                    Glide.with(getBaseContext()).load(user.getPicture()).into(profilePicture);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void addToFav(View v, String id) {
        String jwt = UtilToken.getToken(this);
        PropertyService service = ServiceGenerator.createService(PropertyService.class, jwt, AuthType.JWT);
        Call<PropertyResponse> call = service.addFav(id);
        call.enqueue(new Callback<PropertyResponse>() {
            @Override
            public void onResponse(@NonNull Call<PropertyResponse> call, @NonNull Response<PropertyResponse> response) {
                if (response.code() != 200)
                    Toast.makeText(MainActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<PropertyResponse> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void deleteFav(View v, String id) {
        String jwt = UtilToken.getToken(this);
        PropertyService service = ServiceGenerator.createService(PropertyService.class, jwt, AuthType.JWT);
        Call<PropertyResponse> call = service.removeFav(id);
        call.enqueue(new Callback<PropertyResponse>() {
            @Override
            public void onResponse(@NonNull Call<PropertyResponse> call, @NonNull Response<PropertyResponse> response) {
                if (response.code() != 200)
                    Toast.makeText(MainActivity.this, "Request Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<PropertyResponse> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void goLoc(View v, PropertyResponse property) {
        System.out.println(property.getTitle());
        startActivity(new Intent(this, PropertyMapsActivity.class)
                .putExtra("coords", property.getLoc())
                .putExtra("property_title", property.getTitle())
                .putExtra("property_price", property.getPrice()));
    }
}
