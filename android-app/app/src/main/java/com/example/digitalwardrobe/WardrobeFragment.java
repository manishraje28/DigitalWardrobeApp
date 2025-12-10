package com.example.digitalwardrobe;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WardrobeFragment extends Fragment {

    RecyclerView recyclerView;
    ClothingAdapter adapter;

    public WardrobeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wardrobe, container, false);

        // FILTER BUTTON
        Button filterColor = view.findViewById(R.id.filter_color);
        filterColor.setOnClickListener(v -> showColorFilter());

        // RECYCLER VIEW
        recyclerView = view.findViewById(R.id.recycler_wardrobe);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(16));

        // SEARCH BAR
        EditText searchBar = view.findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchClothes(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        loadClothes();

        return view;
    }

    // ---------------- FILTERS ------------------

    private void showColorFilter() {
        String[] colors = {"Red", "Blue", "Green", "White", "Black", "Yellow"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Color")
                .setItems(colors, (dialog, which) -> applyColorFilter(colors[which]))
                .show();
    }

    private void applyColorFilter(String color) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClothingItem> filtered = db.clothingItemDao().filterByColor(color);

            requireActivity().runOnUiThread(() -> {
                adapter.setClothingList(filtered);
            });

        }).start();
    }

    // ---------------- SEARCH ------------------

    private void searchClothes(String query) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClothingItem> result = db.clothingItemDao().searchClothes(query);

            requireActivity().runOnUiThread(() -> {
                adapter.setClothingList(result);
            });
        }).start();
    }

    // ---------------- LOAD ALL CLOTHES ------------------

    private void loadClothes() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClothingItem> itemList = db.clothingItemDao().getAllItems();

            if (itemList == null) itemList = new ArrayList<>();

            List<ClothingItem> finalList = itemList;

            requireActivity().runOnUiThread(() -> {
                adapter = new ClothingAdapter(requireContext(), finalList);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
