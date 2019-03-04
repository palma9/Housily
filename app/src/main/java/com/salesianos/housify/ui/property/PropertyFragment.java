package com.salesianos.housify.ui.property;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.salesianos.housify.R;
import com.salesianos.housify.data.dto.PropertyFilterDto;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.property.filter.PropertyFilterActivity;
import com.salesianos.housify.ui.property.map.SelectLocationFragment;
import com.salesianos.housify.util.UtilToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;


public class PropertyFragment extends Fragment {

    public static final String PROPERTY_FILTER = "";

    private String sort;
    private View view;
    private PropertyListListener mListener;
    private MyPropertyRecyclerViewAdapter adapter;
    private LinearLayoutManager mAdapter;
    private RecyclerView recycler;
    private Context ctx;
    private SwipeRefreshLayout swipeContainer;
    // Retrofit Params
    private List<PropertyResponse> items;
    private Map<String, String> queryData;
    private PropertyFilterDto propertyFilterItem;

    // LoadMore
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int pageSize = 5;
    private int mCurrentPage = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_property_list, container, false);
        setViewIds();
        return view;
    }

    private void setViewIds() {
        swipeContainer = view.findViewById(R.id.swipeContainer);
        recycler = view.findViewById(R.id.property_list);
        mAdapter = new LinearLayoutManager(ctx);
        setHasOptionsMenu(true);
        // Set the adapter
        if (recycler != null) {
            recycler.setLayoutManager(mAdapter);
        }
        swipeContainer.setOnRefreshListener(() -> {
            mCurrentPage = 1;
            items.clear();
            setQueryData();
            if (swipeContainer.isRefreshing()) {
                swipeContainer.setRefreshing(false);
            }
        });
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Objects.requireNonNull(getFragmentManager())
                    .beginTransaction()
                    .replace(R.id.dashboard_fragment_container, new SelectLocationFragment())
                    .commit();
            // startActivity(new Intent(this, PropertyMapsActivity.class))
        });
        onScroll();
        mCurrentPage = 1;
        setQueryData();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
        if (context instanceof PropertyListListener) {
            mListener = (PropertyListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PropertyListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.activity_main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                startActivityForResult(new Intent(ctx, PropertyFilterActivity.class), 0);
                break;
            case R.id.action_sort_newest:
                sort = "-createdAt";
                setQueryData();
                break;
            case R.id.action_sort_name:
                sort = "title";
                setQueryData();
                break;
            case R.id.action_sort_price:
                sort = "price";
                setQueryData();
                break;
            case R.id.action_sort_rooms:
                sort = "rooms";
                setQueryData();
                break;
            case R.id.action_sort_size:
                sort = "size";
                setQueryData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onScroll() {
        // initialise loading state
        mIsLoading = false;
        recycler.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // number of visible items
                int visibleItemCount = mAdapter.getChildCount();
                // number of items in layout
                int totalItemCount = mAdapter.getItemCount();
                // the position of first visible item
                int firstVisibleItemPosition = mAdapter.findFirstVisibleItemPosition();

                boolean isNotLoadingOrIsLastPage = !mIsLoading && !mIsLastPage;
                // flag if number of visible items is at the last
                boolean isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount;
                // validate non negative values
                boolean isValidFirstItem = firstVisibleItemPosition >= 0;
                // validate total items are more than possible visible items
                boolean totalIsMoreThanVisible = totalItemCount >= pageSize;
                // flag to know whether to load more
                boolean shouldLoadMore = isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingOrIsLastPage;
                if (shouldLoadMore) loadMoreItems();
            }
        });
    }

    private void loadMoreItems() {
        // change loading state
        if (!mIsLoading) {
            mCurrentPage = mCurrentPage + 1;
            propertyFilterItem = null;
            setQueryData();
        }
        mIsLoading = true;
    }

    private void setQueryData() {
        queryData = new HashMap<>();
        queryData.put("limit", String.valueOf(pageSize));
        queryData.put("page", String.valueOf(mCurrentPage));
        if (sort != null)
            queryData.put("sort", sort);
        if (propertyFilterItem != null) {
            if (!propertyFilterItem.getRooms().equals(""))
                queryData.put("rooms", propertyFilterItem.getRooms());
            if (!propertyFilterItem.getCity().equals(""))
                queryData.put("city", propertyFilterItem.getCity());
            if (!propertyFilterItem.getProvince().equals(""))
                queryData.put("province", propertyFilterItem.getProvince());
            if (!propertyFilterItem.getZipcode().equals(""))
                queryData.put("zipcode", propertyFilterItem.getZipcode());
            if (!propertyFilterItem.getMinSize().equals(""))
                queryData.put("min_size", propertyFilterItem.getMinSize());
            if (!propertyFilterItem.getMaxSize().equals(""))
                queryData.put("max_size", propertyFilterItem.getMaxSize());
            if (!propertyFilterItem.getMinPrice().equals(""))
                queryData.put("min_price", propertyFilterItem.getMinPrice());
            if (!propertyFilterItem.getMaxPrice().equals(""))
                queryData.put("max_price", propertyFilterItem.getMaxPrice());
            if (!propertyFilterItem.getCategoryId().equals(""))
                queryData.put("category", propertyFilterItem.getCategoryId());
        }
        getAllProperties();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            propertyFilterItem = (PropertyFilterDto) data.getSerializableExtra(PROPERTY_FILTER);
            mCurrentPage = 1;
            mIsLastPage = false;
            setQueryData();
        }
    }

    private void getAllProperties() {
        view.findViewById(R.id.connection_fails).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        items = new ArrayList<>();
        PropertyService service = ServiceGenerator.createService(PropertyService.class);
        Call<ResponseContainer<PropertyResponse>> call = service.listAll(queryData);
        if (UtilToken.getToken(ctx) != null) {
            service = ServiceGenerator.createService(PropertyService.class, UtilToken.getToken(ctx), AuthType.JWT);
            call = service.getAuth(queryData);
        }
        call.enqueue(new Callback<ResponseContainer<PropertyResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ResponseContainer<PropertyResponse>> call, @NonNull Response<ResponseContainer<PropertyResponse>> response) {
                if (response.code() != 200) {
                    Toast.makeText(ctx, "Request Error", Toast.LENGTH_SHORT).show();
                    if (items.isEmpty()) {
                        view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        view.findViewById(R.id.property_list).setVisibility(View.GONE);
                        view.findViewById(R.id.connection_fails).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.property_list).setVisibility(View.VISIBLE);
                    }
                } else {
                    items = Objects.requireNonNull(response.body()).getRows();
                    if (response.body().getCount() != pageSize)
                        mIsLastPage = true;

                    if (mCurrentPage == 1) {
                        adapter = new MyPropertyRecyclerViewAdapter(ctx, items, mListener);
                        recycler.setAdapter(adapter);
                    } else {
                        adapter.addAll(items);
                        mIsLoading = false;
                    }
                    if (adapter.getItemCount() == 0) {
                        view.findViewById(R.id.content_no_houses).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.property_list).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.content_no_houses).setVisibility(View.GONE);
                        view.findViewById(R.id.property_list).setVisibility(View.VISIBLE);
                    }
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseContainer<PropertyResponse>> call, @NonNull Throwable t) {
                Log.e("Network Failure", t.getMessage());
                Toast.makeText(ctx, "Network Error", Toast.LENGTH_SHORT).show();
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                view.findViewById(R.id.property_list).setVisibility(View.GONE);
                view.findViewById(R.id.connection_fails).setVisibility(View.VISIBLE);
            }
        });
    }
}
