package com.example.digitalwardrobe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClothingItemDao {

    @Insert
    long insertClothingItem(ClothingItem item);

    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    List<ClothingItem> getAllItems();
}
