package com.salesianos.housify.ui.property;

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
import com.salesianos.housify.ui.property.details.PropertyDetailsActivity;
import com.salesianos.housify.util.UtilToken;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class MyPropertyRecyclerViewAdapter extends RecyclerView.Adapter<MyPropertyRecyclerViewAdapter.ViewHolder> {


    private final PropertyListListener mListener;
    private List<PropertyResponse> data;
    private Context context;
    private View view;

    public MyPropertyRecyclerViewAdapter(Context ctx, List<PropertyResponse> data, PropertyListListener mListener) {
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
        if (holder.mItem.getCategoryId() != null)
            holder.tvCategory.setText(holder.mItem.getCategoryId().getName());
        else
            holder.tvCategory.setText(context.getString(R.string.no_category));

        // Imagen
        if (holder.mItem != null && holder.mItem.getPhotos() != null) {
            Glide.with(context).load(holder.mItem.getPhotos().get(0)).into(holder.coverImage);
        }

        // Obtener favoritos
        if (holder.mItem.getIsFav() == null)
            holder.mItem.setIsFav("true");

        // Cambiar icono fav
        if (holder.mItem.getIsFav().equals("false"))
            holder.ibtnFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);

        if (UtilToken.getToken(context) == null)
            holder.ibtnFav.setVisibility(View.GONE);
        else
            holder.ibtnFav.setVisibility(View.VISIBLE);

        // Cambiar favorito
        holder.ibtnFav.setOnClickListener(v -> {
            if (holder.mItem.getIsFav().equals("true")) {
                mListener.deleteFav(v, holder.mItem.getId());
                holder.mItem.setIsFav("false");
                ((ImageView) holder.mView.findViewById(R.id.ibtn_fav)).setImageResource(R.drawable.ic_favorite_border_black_24dp);
            } else {
                mListener.addToFav(v, holder.mItem.getId());
                holder.mItem.setIsFav("true");
                ((ImageView) holder.mView.findViewById(R.id.ibtn_fav)).setImageResource(R.drawable.ic_favorite_red_24dp);
            }
        });

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
        final TextView tvCategory;
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
            tvCategory = view.findViewById(R.id.tv_category);
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

    public void setList(List<PropertyResponse> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    public void addAll(List<PropertyResponse> newList) {
        int lastIndex = data.size() - 1;
        data.addAll(newList);
        notifyItemRangeInserted(lastIndex, newList.size());
    }
}
