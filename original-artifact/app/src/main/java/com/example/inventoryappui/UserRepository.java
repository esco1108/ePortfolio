package com.example.inventoryappui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Handles user account creation and login validation
public class UserRepository {

    // DB helper
    private final DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    //Creates a new user
    //@return true if created, false if invalid or already exists
    public boolean createUser(String username, String password) {
        // Basic null/empty validation
        if (username == null || password == null) return false;
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Prevent duplicate usernames
        if (userExists(username)) {
            return false;
        }

        // Insert user row
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.U_COL_USERNAME, username);
        values.put(DatabaseHelper.U_COL_PASSWORD, password);
        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);

        // Create default SMS settings row for this user
        ensureSmsSettingsRow(username);

        return id != -1;
    }

    // Validates username + password against the DB
    public boolean validateUser(String username, String password) {
        // Null/empty validation
        if (username == null || password == null) return false;
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) return false;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query stored password for this username
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.U_COL_PASSWORD},
                DatabaseHelper.U_COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        try {
            // Username not found
            if (!cursor.moveToFirst()) {
                return false;
            }

            // Compare plaintext passwords
            String stored = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.U_COL_PASSWORD)
            );
            return password.equals(stored);
        } finally {
            // Always close cursor
            cursor.close();
        }
    }

    // Checks if a username already exists
    public boolean userExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.U_COL_ID},
                DatabaseHelper.U_COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // Inserts a default SMS settings row if missing
    private void ensureSmsSettingsRow(String ownerUsername) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if settings row already exists
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SMS_SETTINGS,
                new String[]{DatabaseHelper.S_COL_ID},
                DatabaseHelper.S_COL_OWNER + "=?",
                new String[]{ownerUsername},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return;
            }
        } finally {
            cursor.close();
        }

        // Insert default settings values
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.S_COL_OWNER, ownerUsername);
        values.put(DatabaseHelper.S_COL_PHONE, "");
        values.put(DatabaseHelper.S_COL_LOW_STOCK_ENABLED, 0);
        db.insert(DatabaseHelper.TABLE_SMS_SETTINGS, null, values);
    }
}