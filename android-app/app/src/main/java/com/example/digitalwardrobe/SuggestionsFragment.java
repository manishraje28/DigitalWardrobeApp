package com.example.digitalwardrobe;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestionsFragment extends Fragment {

    TextView suggestionText;
    ImageView topImage, bottomImage;

    public SuggestionsFragment() {}

    // COLOR MATCHING LOGIC
    private static final Map<String, List<String>> COLOR_MATCHES = new HashMap<String, List<String>>() {{
        put("red", Arrays.asList("black", "white", "blue"));
        put("blue", Arrays.asList("black", "white", "grey"));
        put("black", Arrays.asList("red", "blue", "white", "grey"));
        put("white", Arrays.asList("blue", "black", "red", "green"));
        put("green", Arrays.asList("black", "white", "beige"));
        put("yellow", Arrays.asList("blue", "black", "white"));
    }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestions, container, false);

        suggestionText = view.findViewById(R.id.text_suggestion);
        topImage = view.findViewById(R.id.image_top);
        bottomImage = view.findViewById(R.id.image_bottom);

        generateSmartOutfit();

        return view;
    }

    private void generateSmartOutfit() {

        new Thread(() -> {

            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Detecting tops
            List<ClothingItem> tops = db.clothingItemDao().getItemsByType("shirt");
            tops.addAll(db.clothingItemDao().getItemsByType("tshirt"));
            tops.addAll(db.clothingItemDao().getItemsByType("hoodie"));
            tops.addAll(db.clothingItemDao().getItemsByType("kurta"));

            // Detecting bottoms
            List<ClothingItem> bottoms = db.clothingItemDao().getItemsByType("pant");
            bottoms.addAll(db.clothingItemDao().getItemsByType("jeans"));
            bottoms.addAll(db.clothingItemDao().getItemsByType("shorts"));
            bottoms.addAll(db.clothingItemDao().getItemsByType("joggers"));

            if (tops.isEmpty() || bottoms.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        suggestionText.setText("Add at least 1 Top & Bottom for suggestions.")
                );
                return;
            }

            ClothingItem bestTop = null;
            ClothingItem bestBottom = null;
            int bestScore = -1;

            // SCORE MATCHING LOGIC
            for (ClothingItem t : tops) {
                for (ClothingItem b : bottoms) {
                    int score = 0;

                    // Color match
                    List<String> matches = COLOR_MATCHES.get(t.color.toLowerCase());
                    if (matches != null && matches.contains(b.color.toLowerCase())) {
                        score += 5;
                    }

                    // Occasions match (gym, casual, wedding)
                    if (t.occasion.equalsIgnoreCase(b.occasion)) {
                        score += 3;
                    }

                    // Category match
                    if (t.category.equalsIgnoreCase(b.category)) {
                        score += 1;
                    }

                    // Pick best-scoring outfit
                    if (score > bestScore) {
                        bestScore = score;
                        bestTop = t;
                        bestBottom = b;
                    }
                }
            }

            ClothingItem finalTop = bestTop;
            ClothingItem finalBottom = bestBottom;

            requireActivity().runOnUiThread(() -> {
                if (finalTop == null || finalBottom == null) {
                    suggestionText.setText("Not enough data for smart recommendation.");
                    return;
                }

                suggestionText.setText(
                        "Today's Outfit:\n" +
                                finalTop.color + " " + finalTop.type + " + " +
                                finalBottom.color + " " + finalBottom.type
                );

                try {
                    topImage.setImageURI(Uri.parse(finalTop.imageUri));
                    bottomImage.setImageURI(Uri.parse(finalBottom.imageUri));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error loading outfit images.", Toast.LENGTH_SHORT).show();
                }
            });

        }).start();
    }
}
