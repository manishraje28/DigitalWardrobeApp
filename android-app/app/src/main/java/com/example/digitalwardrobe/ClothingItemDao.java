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

    @Update
    void updateItem(ClothingItem item);

    @Delete
    void deleteItem(ClothingItem item);

    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    List<ClothingItem> getAllItems();

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    ClothingItem getItemById(int id);

    // --------------------------------------------------
    // TOP WEAR
    // --------------------------------------------------
    @Query("SELECT * FROM clothing_items WHERE LOWER(type) LIKE '%shirt%' OR LOWER(type) LIKE '%tshirt%' OR LOWER(type) LIKE '%kurta%' OR LOWER(type) LIKE '%hoodie%'")
    List<ClothingItem> getAllTops();

    // --------------------------------------------------
    // BOTTOM WEAR
    // --------------------------------------------------
    @Query("SELECT * FROM clothing_items WHERE LOWER(type) LIKE '%pant%' OR LOWER(type) LIKE '%jeans%'")
    List<ClothingItem> getPants();

    @Query("SELECT * FROM clothing_items WHERE LOWER(type) LIKE '%short%' OR LOWER(type) LIKE '%jogger%'")
    List<ClothingItem> getShortsAndJoggers();

    @Query("SELECT * FROM clothing_items WHERE LOWER(category) = 'top'")
    List<ClothingItem> getByCategoryTop();

    @Query("SELECT * FROM clothing_items WHERE LOWER(category) = 'bottom'")
    List<ClothingItem> getByCategoryBottom();

    // --------------------------------------------------
    // OCCASION FILTER
    // --------------------------------------------------
    @Query("SELECT * FROM clothing_items WHERE LOWER(occasion) LIKE '%' || LOWER(:occ) || '%'")
    List<ClothingItem> getByOccasion(String occ);

    // --------------------------------------------------
    // TYPE FILTER
    // --------------------------------------------------
    @Query("SELECT * FROM clothing_items WHERE LOWER(type) LIKE '%' || LOWER(:type) || '%'")
    List<ClothingItem> getItemsByType(String type);

    // --------------------------------------------------
    // UNIVERSAL SEARCH (type/color/category)
    // --------------------------------------------------
    @Query("SELECT * FROM clothing_items WHERE LOWER(type) LIKE '%' || LOWER(:keyword) || '%' " +
            "OR LOWER(color) LIKE '%' || LOWER(:keyword) || '%' " +
            "OR LOWER(category) LIKE '%' || LOWER(:keyword) || '%'")
    List<ClothingItem> getByKeyword(String keyword);

}
