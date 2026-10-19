package com.example.inventoryappui;

import android.content.Context;

//Manages the logged in user session using SharedPreferences

public final class SessionManager {

    // SharedPreferences file name
    private static final String PREFS = "inventory_session";

    // Key for stored username
    private static final String KEY_USERNAME = "current_username";

    // Prevent instantiation
    private SessionManager() { }

    // Save the current logged-in username
    public static void setCurrentUsername(Context context, String username) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USERNAME, username)
                .apply();
    }

    // Retrieve the current logged-in username
    public static String getCurrentUsername(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, null);
    }

    // Clear the saved session on logout
    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_USERNAME)
                .apply();
    }
}