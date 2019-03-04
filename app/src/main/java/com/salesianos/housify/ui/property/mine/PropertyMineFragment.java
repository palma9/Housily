package com.salesianos.housify.ui.property.mine;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.salesianos.housify.R;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.data.response.ResponseContainer;
import com.salesianos.housify.retrofit.Service.PropertyService;
import com.salesianos.housify.retrofit.generator.AuthType;
import com.salesianos.housify.retrofit.generator.ServiceGenerator;
import com.salesianos.housify.ui.property.PropertyListListener;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PropertyMineFragment extends Fragment {


    private View view;
    private PropertyListListener mListener;
    private MyPropertyMineRecyclerViewAdapter adapter;
    private LinearLayoutManager mAdapter;
    private RecyclerView recycler;
    private Context ctx;
    private SwipeRefreshLayout swipeContainer;
    // Retrofit Params
    private List<PropertyResponse> items;
    private Map<String, String> queryData;

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
        view.findViewById(R.id.fab).setVisibility(View.GONE);
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

    private void onScroll() {
        // initialise loading state
        mIsLoading = false;
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            setQueryData();
        }
        mIsLoading = true;
    }

    private void setQueryData() {
        queryData = new HashMap<>();
        queryData.put("limit", String.valueOf(pageSize));
        queryData.put("page", String.valueOf(mCurrentPage));
        getAllProperties();
    }

    private void getAllProperties() {
        view.findViewById(R.id.connection_fails).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        items = new ArrayList<>();
        PropertyService service = ServiceGenerator.createService(PropertyService.class, UtilToken.getToken(ctx), AuthType.JWT);
        Call<ResponseContainer<PropertyResponse>> call = service.getMine(queryData);
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
                        adapter = new MyPropertyMineRecyclerViewAdapter(ctx, items, mListener);
                        recycler.setAdapter(adapter);
                    } else {
                        adapter.addAll(items);
                        mIsLoading = false;
                    }
                    if (adapter.getItemCount() == 0) {
                        view.findViewById(R.id.content_not_created).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.property_list).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.content_not_created).setVisibility(View.GONE);
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
