package com.salesianos.housify.ui.property.mine;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.salesianos.housify.R;
import com.salesianos.housify.data.response.PropertyResponse;
import com.salesianos.housify.ui.property.PropertyListListener;
import com.salesianos.housify.ui.property.details.PropertyDetailsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class MyPropertyMineRecyclerViewAdapter extends RecyclerView.Adapter<MyPropertyMineRecyclerViewAdapter.ViewHolder> {


    private final PropertyListListener mListener;
    private List<PropertyResponse> data;
    private Context context;
    private View view;

    public MyPropertyMineRecyclerViewAdapter(Context ctx, List<PropertyResponse> data, PropertyListListener mListener) {
        this.data = data;
        this.context = ctx;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = data.get(position);
        holder.tvTitle.setText(holder.mItem.getTitle());
        holder.tvPrice.setText(String.format("%s%s", String.valueOf(holder.mItem.getPrice()), context.getString(R.string.euro)));
        holder.tvRoom.setText(String.valueOf(holder.mItem.getRooms()));
        holder.tvMet.setText(String.valueOf(holder.mItem.getSize()));
        holder.tvCity.setText(holder.mItem.getCity());

        // Imagen
        if (holder.mItem != null && holder.mItem.getPhotos() != null) {
            Glide.with(context).load(holder.mItem.getPhotos().get(0)).into(holder.coverImage);
        }

        // Ocultar icono fav
        holder.ibtnFav.setVisibility(View.GONE);

        // Conseguir localizacion
        holder.ibtnLoc.setOnClickListener(v ->
                mListener.goLoc(v, holder.mItem));

        // Ir a details
        holder.mView.setOnClickListener(v ->
                context.startActivity(new Intent(context, PropertyDetailsActivity.class)
                        .putExtra("id", holder.mItem.getId())));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView tvTitle;
        final TextView tvPrice;
        final TextView tvRoom;
        final TextView tvMet;
        final TextView tvCity;
        final ImageButton ibtnFav;
        final ImageButton ibtnLoc;
        final ImageView coverImage;
        PropertyResponse mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            tvTitle = view.findViewById(R.id.tv_title);
            tvPrice = view.findViewById(R.id.tv_price);
            tvRoom = view.findViewById(R.id.tv_room);
            tvMet = view.findViewById(R.id.tv_met);
            tvCity = view.findViewById(R.id.tv_city);
            ibtnFav = view.findViewById(R.id.ibtn_fav);
            ibtnLoc = view.findViewById(R.id.ibtn_loc);
            coverImage = view.findViewById(R.id.coverImage);
        }
    }

    void setList(List<PropertyResponse> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    void addAll(List<PropertyResponse> newList) {
        int lastIndex = data.size() - 1;
        data.addAll(newList);
        notifyItemRangeInserted(lastIndex, newList.size());
    }
}
