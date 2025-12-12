package com.example.digitalwardrobe;

public class Outfit {
    public ClothingItem top;
    public ClothingItem bottom;

    public Outfit(ClothingItem top, ClothingItem bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    public boolean isValid() {
        return top != null && bottom != null;
    }
}
