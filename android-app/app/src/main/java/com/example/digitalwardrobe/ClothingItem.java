package com.example.digitalwardrobe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clothing_items")
public class ClothingItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String imageUri;     // for storing image path
    public String category;     // "Top", "Bottom", "Ethnic"...
    public String color;        // "Red", "Blue"...
    public String type;         // "T-shirt", "Jeans"
    public String occasion;     // "Casual", "Formal"
    public long createdAt;      // timestamp
}
