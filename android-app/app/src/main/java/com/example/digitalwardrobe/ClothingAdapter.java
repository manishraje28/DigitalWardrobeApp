package com.example.digitalwardrobe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ViewHolder> {

    private Context context;
    private List<ClothingItem> itemList;
    List<ClothingItem> clothingList;

    public ClothingAdapter(Context context, List<ClothingItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clothing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClothingItem item = itemList.get(position);

        holder.type.setText(item.type != null ? item.type : "Unknown");
        holder.color.setText(item.color != null ? "Color: " + item.color : "Color: -");
        holder.occasion.setText(item.occasion != null ? "Occasion: " + item.occasion : "Occasion: -");
        holder.itemView.setOnClickListener(v -> {
            ClothingDetailsFragment fragment = new ClothingDetailsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("id", item.id);            // Pass ID only (best practice)
            fragment.setArguments(bundle);

            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        if (item.imageUri != null) {
            try {
                holder.image.setImageURI(Uri.parse(item.imageUri));
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setClothingList(List<ClothingItem> newList) {
        this.clothingList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView type, color, occasion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.item_image);
            type = itemView.findViewById(R.id.item_type);
            color = itemView.findViewById(R.id.item_color);
            occasion = itemView.findViewById(R.id.item_occasion);
        }
    }
}
