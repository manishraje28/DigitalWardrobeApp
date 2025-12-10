package com.example.digitalwardrobe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private int editId = -1;
    private ClothingItem editingItem = null;

    EditText editType, editColor, editCategory, editOccasion;

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_cloth, container, false);

        // Check for edit mode
        if (getArguments() != null) {
            editId = getArguments().getInt("edit_id", -1);
        }

        // UI references
        imagePreview = view.findViewById(R.id.image_preview);
        editType = view.findViewById(R.id.edit_type);
        editColor = view.findViewById(R.id.edit_color);
        editCategory = view.findViewById(R.id.edit_category);
        editOccasion = view.findViewById(R.id.edit_occasion);
        Button pickImage = view.findViewById(R.id.button_pick_image);
        Button saveButton = view.findViewById(R.id.button_save);

        // Load old data if editing
        if (editId != -1) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                editingItem = db.clothingItemDao().getItemById(editId);

                requireActivity().runOnUiThread(() -> {
                    imagePreview.setImageURI(Uri.parse(editingItem.imageUri));
                    editType.setText(editingItem.type);
                    editColor.setText(editingItem.color);
                    editCategory.setText(editingItem.category);
                    editOccasion.setText(editingItem.occasion);

                    selectedImageUri = Uri.parse(editingItem.imageUri);
                });

            }).start();
        }

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);

                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}
                    }
                });

        pickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Save button logic
        saveButton.setOnClickListener(v -> {

            if (selectedImageUri == null ||
                    editType.getText().toString().isEmpty() ||
                    editColor.getText().toString().isEmpty() ||
                    editCategory.getText().toString().isEmpty() ||
                    editOccasion.getText().toString().isEmpty()) {

                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ClothingItem item = new ClothingItem();
            item.imageUri = selectedImageUri.toString();
            item.type = editType.getText().toString();
            item.color = editColor.getText().toString();
            item.category = editCategory.getText().toString();
            item.occasion = editOccasion.getText().toString();
            item.createdAt = System.currentTimeMillis();

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());

                if (editId == -1) {
                    db.clothingItemDao().insertClothingItem(item);
                } else {
                    item.id = editId;
                    db.clothingItemDao().updateItem(item);
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            (editId == -1) ? "Cloth Saved!" : "Cloth Updated!",
                            Toast.LENGTH_SHORT).show();

                    requireActivity().getSupportFragmentManager().popBackStack();
                });

            }).start();
        });

        return view;
    }
}
