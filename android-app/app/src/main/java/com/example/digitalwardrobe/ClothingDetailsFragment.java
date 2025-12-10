package com.example.digitalwardrobe;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ClothingDetailsFragment extends Fragment {

    ImageView detailImage;
    TextView detailType, detailColor, detailCategory, detailOccasion, detailCreated;

    // ⭐ FIX: Make itemId a class variable so all methods can use it
    int itemId = -1;

    public ClothingDetailsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clothing_details, container, false);

        // ⭐ Now itemId is available BEFORE buttons
        itemId = getArguments().getInt("id", -1);

        detailImage = view.findViewById(R.id.detail_image);
        detailType = view.findViewById(R.id.detail_type);
        detailColor = view.findViewById(R.id.detail_color);
        detailCategory = view.findViewById(R.id.detail_category);
        detailOccasion = view.findViewById(R.id.detail_occasion);
        detailCreated = view.findViewById(R.id.detail_created);

        Button deleteBtn = view.findViewById(R.id.button_delete);
        Button editBtn = view.findViewById(R.id.button_edit);

        // ---------------- DELETE -----------------
        deleteBtn.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this clothing item?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        new Thread(() -> {
                            AppDatabase db = AppDatabase.getInstance(requireContext());
                            ClothingItem item = db.clothingItemDao().getItemById(itemId);

                            db.clothingItemDao().deleteItem(item);

                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            });

                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ---------------- EDIT -----------------
        editBtn.setOnClickListener(v -> {
            AddClothFragment fragment = new AddClothFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("edit_id", itemId);
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ---------------- LOAD DETAILS -----------------
        if (itemId != -1) {
            loadClothingItem(itemId);
        }

        return view;
    }

    private void loadClothingItem(int id) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            ClothingItem item = db.clothingItemDao().getItemById(id);

            requireActivity().runOnUiThread(() -> {
                detailImage.setImageURI(Uri.parse(item.imageUri));
                detailType.setText("Type: " + item.type);
                detailColor.setText("Color: " + item.color);
                detailCategory.setText("Category: " + item.category);
                detailOccasion.setText("Occasion: " + item.occasion);

                detailCreated.setText("Added On: " +
                        android.text.format.DateFormat.format("dd MMM yyyy", item.createdAt));
            });

        }).start();
    }
}
