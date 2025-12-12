package com.example.digitalwardrobe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ClothingItemDao {

    @Insert
    long insertClothingItem(ClothingItem item);

    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    List<ClothingItem> getAllItems();

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    ClothingItem getItemById(int id);

    @Delete
    void deleteItem(ClothingItem item);
    @Update
    void updateItem(ClothingItem item);

}
