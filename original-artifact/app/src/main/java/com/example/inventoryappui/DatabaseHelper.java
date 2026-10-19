package com.example.inventoryappui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database
    public static final String DB_NAME = "inventory_app.db";
    public static final int DB_VERSION = 3; // bumped for new column

    // users
    public static final String TABLE_USERS = "users";
    public static final String U_COL_ID = "_id";
    public static final String U_COL_USERNAME = "username";
    public static final String U_COL_PASSWORD = "password";

    // items
    public static final String TABLE_ITEMS = "items";
    public static final String I_COL_ID = "_id";
    public static final String I_COL_OWNER = "owner_username";
    public static final String I_COL_NAME = "name";
    public static final String I_COL_QTY = "quantity";
    public static final String I_COL_LOCATION = "location";
    public static final String I_COL_DESCRIPTION = "description";
    public static final String I_COL_LAST_EDITED = "last_edited";

    // sms_settings
    public static final String TABLE_SMS_SETTINGS = "sms_settings";
    public static final String S_COL_ID = "_id";
    public static final String S_COL_OWNER = "owner_username";
    public static final String S_COL_PHONE = "phone_number";
    public static final String S_COL_LOW_STOCK_ENABLED = "low_stock_enabled";
    public static final String S_COL_PERMISSION_CHOICE = "permission_choice"; // 0=Unknown,1=Accepted,2=Declined

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    U_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    U_COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    U_COL_PASSWORD + " TEXT NOT NULL" +
                    ");";

    private static final String SQL_CREATE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    I_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    I_COL_OWNER + " TEXT NOT NULL, " +
                    I_COL_NAME + " TEXT NOT NULL, " +
                    I_COL_QTY + " INTEGER NOT NULL DEFAULT 0, " +
                    I_COL_LOCATION + " TEXT NOT NULL, " +
                    I_COL_DESCRIPTION + " TEXT NOT NULL DEFAULT '', " +
                    I_COL_LAST_EDITED + " TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(" + I_COL_OWNER + ") REFERENCES " + TABLE_USERS + "(" + U_COL_USERNAME + ")" +
                    ");";

    private static final String SQL_CREATE_SMS_SETTINGS =
            "CREATE TABLE " + TABLE_SMS_SETTINGS + " (" +
                    S_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    S_COL_OWNER + " TEXT NOT NULL UNIQUE, " +
                    S_COL_PHONE + " TEXT NOT NULL DEFAULT '', " +
                    S_COL_LOW_STOCK_ENABLED + " INTEGER NOT NULL DEFAULT 0, " +
                    S_COL_PERMISSION_CHOICE + " INTEGER NOT NULL DEFAULT 0, " + // default Unknown
                    "FOREIGN KEY(" + S_COL_OWNER + ") REFERENCES " + TABLE_USERS + "(" + U_COL_USERNAME + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_ITEMS);
        db.execSQL(SQL_CREATE_SMS_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Course-project approach: drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}