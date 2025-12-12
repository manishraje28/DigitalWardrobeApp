package com.example.digitalwardrobe;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.digitalwardrobe.WardrobeFragment;
import com.example.digitalwardrobe.AddClothFragment;
import com.example.digitalwardrobe.SuggestionsFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Load your layout

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Load default fragment
        loadFragment(new WardrobeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_wardrobe) {
                fragment = new WardrobeFragment();
            } else if (item.getItemId() == R.id.nav_add) {
                fragment = new AddClothFragment();
            } else if (item.getItemId() == R.id.nav_suggestions) {
                fragment = new SuggestionsFragment();
            }else if (item.getItemId() == R.id.nav_chat) {
                fragment = new ChatFragment();
            }


            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
            return true;
        }
        return false;

    }


}
