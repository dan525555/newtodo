package com.example.newtodo;


 import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Objects;



public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState!=null)
            savedInstanceState.clear();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        if (findViewById(R.id.idFrame) != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.idFrame, new SettingsFragment())
                    .commit();
        }

    }
}
