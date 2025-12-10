package com.example.digitalwardrobe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddClothFragment extends Fragment {

    private ImageView imagePreview;
    private Uri selectedImageUri = null;

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_cloth, container, false);

        imagePreview = view.findViewById(R.id.image_preview);
        Button pickImage = view.findViewById(R.id.button_pick_image);

        EditText editType = view.findViewById(R.id.edit_type);
        EditText editColor = view.findViewById(R.id.edit_color);
        EditText editCategory = view.findViewById(R.id.edit_category);
        EditText editOccasion = view.findViewById(R.id.edit_occasion);
        Button saveButton = view.findViewById(R.id.button_save);

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);
                    }
                });

        pickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> {
            String type = editType.getText().toString();
            String color = editColor.getText().toString();
            String category = editCategory.getText().toString();
            String occasion = editOccasion.getText().toString();

            if (selectedImageUri == null || type.isEmpty() || color.isEmpty() || category.isEmpty() || occasion.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Room DB
            ClothingItem item = new ClothingItem();
            item.imageUri = selectedImageUri.toString();
            item.type = type;
            item.color = color;
            item.category = category;
            item.occasion = occasion;
            item.createdAt = System.currentTimeMillis();

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getContext());
                db.clothingItemDao().insertClothingItem(item);

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Cloth Saved!", Toast.LENGTH_SHORT).show()
                );
            }).start();
        });

        return view;
    }
}
