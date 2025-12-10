package com.example.digitalwardrobe;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

public class SuggestionsFragment extends Fragment {

    TextView suggestionText;
    ImageView suggestionImage;

    public SuggestionsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestions, container, false);

        suggestionText = view.findViewById(R.id.text_suggestion);
        suggestionImage = view.findViewById(R.id.image_suggestion);

        loadSuggestions();

        return view;
    }

    private void loadSuggestions() {

        new Thread(() -> {

            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClothingItem> items = db.clothingItemDao().getAllItems();

            if (items == null || items.size() < 2) {
                requireActivity().runOnUiThread(() ->
                        suggestionText.setText("Add more clothes to get recommendations.")
                );
                return;
            }

            ClothingItem top = null;
            ClothingItem bottom = null;

            for (ClothingItem item : items) {
                if (item == null) continue;

                if (item.category != null && item.category.equalsIgnoreCase("Top")) {
                    top = item;
                }

                if (item.category != null && item.category.equalsIgnoreCase("Bottom")) {
                    bottom = item;
                }
            }

            if (top == null || bottom == null) {
                requireActivity().runOnUiThread(() ->
                        suggestionText.setText("Add at least one Top and one Bottom.")
                );
                return;
            }

            ClothingItem finalTop = top;
            ClothingItem finalBottom = bottom;

            requireActivity().runOnUiThread(() -> {
                suggestionText.setText(
                        "Today's Outfit:\n" +
                                finalTop.color + " " + finalTop.type + " + " +
                                finalBottom.color + " " + finalBottom.type
                );

                try {
                    suggestionImage.setImageURI(Uri.parse(finalTop.imageUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }).start();
    }

}
