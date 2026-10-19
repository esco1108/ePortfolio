package com.example.inventoryappui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Displays inventory items in SQLite grid

public class ItemsActivity extends AppCompatActivity {

    // Repository for CRUD access
    private ItemRepository repo;

    // RecyclerView adapter for grid UI
    private ItemsGridAdapter adapter;

    // Logged-in user (item owner)
    private String ownerUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        // Screen title
        setTitle("SQLite Grid Screen");

        // Get current session user
        ownerUsername = SessionManager.getCurrentUsername(this);

        // If not logged in, return to login screen
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Init repository
        repo = new ItemRepository(this);

        // Remove once confirm persistence works
        if (repo.getAllItems(ownerUsername).isEmpty()) {
            repo.addItem(ownerUsername, "Test Item A", 3, "Storage Room", "Seed item", nowDate());
            repo.addItem(ownerUsername, "Test Item B", 7, "Front Shelf", "Seed item", nowDate());
        }

        // Bind RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerItems);

        // 2 column grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Load initial data from SQLite
        List<ItemRepository.ItemRow> data = repo.getAllItems(ownerUsername);

        // Attach adapter
        adapter = new ItemsGridAdapter(data);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh grid data when returning to this screen
        if (adapter != null && repo != null) {
            adapter.setData(repo.getAllItems(ownerUsername));
        }
    }

    private String nowDate() {
        // Timestamp used for last-edited field
        return new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
    }
}