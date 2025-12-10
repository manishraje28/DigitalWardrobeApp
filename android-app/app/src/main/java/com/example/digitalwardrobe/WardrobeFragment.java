package com.example.digitalwardrobe;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class WardrobeFragment extends Fragment {

    RecyclerView recyclerView;
    ClothingAdapter adapter;

    public WardrobeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wardrobe, container, false);

        recyclerView = view.findViewById(R.id.recycler_wardrobe);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadClothes();

        return view;
    }

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
