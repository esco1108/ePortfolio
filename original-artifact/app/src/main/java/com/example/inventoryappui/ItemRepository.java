package com.example.inventoryappui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// Data access layer for inventory items

public class ItemRepository {

    // DB helper instance
    private final DatabaseHelper dbHelper;

    public ItemRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Insert a new item for specific user
    public long addItem(String ownerUsername,
                        String name,
                        int quantity,
                        String location,
                        String description,
                        String lastEdited) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Map item fields to columns
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.I_COL_OWNER, ownerUsername);
        values.put(DatabaseHelper.I_COL_NAME, name);
        values.put(DatabaseHelper.I_COL_QTY, quantity);
        values.put(DatabaseHelper.I_COL_LOCATION, location);
        values.put(DatabaseHelper.I_COL_DESCRIPTION, description == null ? "" : description);
        values.put(DatabaseHelper.I_COL_LAST_EDITED, lastEdited == null ? "" : lastEdited);

        // Insert row and return new row ID
        return db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
    }

    // Load all items for a user (used by grid/list UI)
    public List<ItemRow> getAllItems(String ownerUsername) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<ItemRow> results = new ArrayList<>();

        // Query items owned by this user
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                new String[]{
                        DatabaseHelper.I_COL_ID,
                        DatabaseHelper.I_COL_NAME,
                        DatabaseHelper.I_COL_QTY,
                        DatabaseHelper.I_COL_LOCATION,
                        DatabaseHelper.I_COL_DESCRIPTION,
                        DatabaseHelper.I_COL_LAST_EDITED
                },
                DatabaseHelper.I_COL_OWNER + "=?",
                new String[]{ownerUsername},
                null,
                null,
                DatabaseHelper.I_COL_NAME + " ASC"
        );

        try {
            // Cache column indexes
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_NAME);
            int qtyIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_QTY);
            int locationIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_LOCATION);
            int descIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_DESCRIPTION);
            int lastEditedIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.I_COL_LAST_EDITED);

            // Convert each row into an ItemRow model
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                int qty = cursor.getInt(qtyIndex);
                String location = cursor.getString(locationIndex);
                String description = cursor.getString(descIndex);
                String lastEdited = cursor.getString(lastEditedIndex);

                results.add(new ItemRow(id, ownerUsername, name, qty, location, description, lastEdited));
            }
        } finally {
            // Always close cursor
            cursor.close();
        }

        return results;
    }

    // UPDATE: modify an existing item (only if it belongs to the user)
    public int updateItem(long id,
                          String ownerUsername,
                          String name,
                          int quantity,
                          String location,
                          String description,
                          String lastEdited) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Updated column values
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.I_COL_NAME, name);
        values.put(DatabaseHelper.I_COL_QTY, quantity);
        values.put(DatabaseHelper.I_COL_LOCATION, location);
        values.put(DatabaseHelper.I_COL_DESCRIPTION, description == null ? "" : description);
        values.put(DatabaseHelper.I_COL_LAST_EDITED, lastEdited == null ? "" : lastEdited);

        // Update by id + owner
        return db.update(
                DatabaseHelper.TABLE_ITEMS,
                values,
                DatabaseHelper.I_COL_ID + "=? AND " + DatabaseHelper.I_COL_OWNER + "=?",
                new String[]{String.valueOf(id), ownerUsername}
        );
    }

    // Remove an item (only if belongs to user)
    public int deleteItem(long id, String ownerUsername) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Delete by id + owner for safety
        return db.delete(
                DatabaseHelper.TABLE_ITEMS,
                DatabaseHelper.I_COL_ID + "=? AND " + DatabaseHelper.I_COL_OWNER + "=?",
                new String[]{String.valueOf(id), ownerUsername}
        );
    }

    // Simple row model for UI rendering.
    public static class ItemRow {
        public final long id;
        public final String ownerUsername;
        public final String name;
        public final int quantity;
        public final String location;
        public final String description;
        public final String lastEdited;

        public ItemRow(long id,
                       String ownerUsername,
                       String name,
                       int quantity,
                       String location,
                       String description,
                       String lastEdited) {
            this.id = id;
            this.ownerUsername = ownerUsername;
            this.name = name;
            this.quantity = quantity;
            this.location = location;
            this.description = description;
            this.lastEdited = lastEdited;
        }
    }
}