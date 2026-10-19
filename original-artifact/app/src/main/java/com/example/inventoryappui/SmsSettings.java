package com.example.inventoryappui;

// Simple data carrier for SMS settings

public class SmsSettings {

    // Default state: user has not made a choice yet
    public static final int CHOICE_UNKNOWN = 0;

    // User accepted SMS permission in-app
    public static final int CHOICE_ACCEPTED = 1;

    // User declined SMS permission in-app
    public static final int CHOICE_DECLINED = 2;

    // Phone number used for SMS alerts
    public final String phoneNumber;

    // Whether low stock alerts are enabled
    public final boolean lowStockEnabled;

    // Per user permission choice (Unknown/Accepted/Declined)
    public final int permissionChoice;

    public SmsSettings(String phoneNumber, boolean lowStockEnabled, int permissionChoice) {
        // Store empty string if phone number is null
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
        this.lowStockEnabled = lowStockEnabled;
        this.permissionChoice = permissionChoice;
    }
}