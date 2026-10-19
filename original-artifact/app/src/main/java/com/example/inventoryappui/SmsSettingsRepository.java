package com.example.inventoryappui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

//Persists SMS settings per user in SQLite
public class SmsSettingsRepository {

    // Database helper for opening/creating the SQLite DB
    private final DatabaseHelper dbHelper;

    public SmsSettingsRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Loads the saved SMS settings for a given user
    public SmsSettings getSettings(String ownerUsername) {
        // Ensure a settings row exists for this user
        ensureRow(ownerUsername);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the user's SMS settings row
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SMS_SETTINGS,
                new String[]{
                        DatabaseHelper.S_COL_PHONE,
                        DatabaseHelper.S_COL_LOW_STOCK_ENABLED,
                        DatabaseHelper.S_COL_PERMISSION_CHOICE
                },
                DatabaseHelper.S_COL_OWNER + "=?",
                new String[]{ownerUsername},
                null,
                null,
                null
        );

        try {
            // Read the first row for user
            if (cursor.moveToFirst()) {
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.S_COL_PHONE));
                int enabledInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.S_COL_LOW_STOCK_ENABLED));
                int choice = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.S_COL_PERMISSION_CHOICE));

                // Build a settings object used by the UI
                return new SmsSettings(
                        phone == null ? "" : phone,
                        enabledInt == 1,
                        choice
                );
            }
        } finally {
            // Always close the cursor
            cursor.close();
        }

        // Fallback default if no row is found
        return new SmsSettings("", false, SmsSettings.CHOICE_UNKNOWN);
    }

    // Saves phone number + low stock toggle

    public void saveSettings(String ownerUsername, String phoneNumber, boolean lowStockEnabled) {
        // Ensure a settings row exists for this user
        ensureRow(ownerUsername);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Prepare values to update
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.S_COL_PHONE, phoneNumber == null ? "" : phoneNumber);
        values.put(DatabaseHelper.S_COL_LOW_STOCK_ENABLED, lowStockEnabled ? 1 : 0);

        // Update the user's row
        db.update(
                DatabaseHelper.TABLE_SMS_SETTINGS,
                values,
                DatabaseHelper.S_COL_OWNER + "=?",
                new String[]{ownerUsername}
        );
    }

    // Save per user choice (Unknown/Accepted/Declined)
    public void setPermissionChoice(String ownerUsername, int choice) {
        // Ensure a settings row exists for this user
        ensureRow(ownerUsername);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Update only the permission choice column
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.S_COL_PERMISSION_CHOICE, choice);

        db.update(
                DatabaseHelper.TABLE_SMS_SETTINGS,
                values,
                DatabaseHelper.S_COL_OWNER + "=?",
                new String[]{ownerUsername}
        );
    }

    // Creates a default row for the user if one doesn't already exist
    private void ensureRow(String ownerUsername) {
        // No user, nothing to create
        if (ownerUsername == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check whether a row already exists for this user
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
            // Row already exists
            if (cursor.moveToFirst()) {
                return;
            }
        } finally {
            // Always close the cursor
            cursor.close();
        }

        // Insert default settings row
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.S_COL_OWNER, ownerUsername);
        values.put(DatabaseHelper.S_COL_PHONE, "");
        values.put(DatabaseHelper.S_COL_LOW_STOCK_ENABLED, 0);
        values.put(DatabaseHelper.S_COL_PERMISSION_CHOICE, SmsSettings.CHOICE_UNKNOWN); // default Unknown
        db.insert(DatabaseHelper.TABLE_SMS_SETTINGS, null, values);
    }
}