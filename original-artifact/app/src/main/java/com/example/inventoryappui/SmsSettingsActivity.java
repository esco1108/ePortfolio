package com.example.inventoryappui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Screen for managing SMS permission status and per-user alert settings.
 */
public class SmsSettingsActivity extends AppCompatActivity {

    // Request code for the Send SMS permission prompt
    private static final int PERMISSION_REQUEST_CODE = 200;

    // UI elements
    private TextView txtSmsPermissionStatus;
    private Button btnRequestPermission;
    private EditText edtPhoneNumber;
    private Switch switchLowStock;
    private Button btnSaveSettings;
    private Button btnBackToData;

    // Repository for storing settings in SQLite
    private SmsSettingsRepository smsRepo;

    // Currently logged-in user
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        // Get current user session
        currentUser = SessionManager.getCurrentUsername(this);
        if (currentUser == null || currentUser.trim().isEmpty()) {
            // Safety: no session, close screen
            finish();
            return;
        }

        // Initialize repository
        smsRepo = new SmsSettingsRepository(this);

        // Bind UI components
        txtSmsPermissionStatus = findViewById(R.id.txtSmsPermissionStatus);
        btnRequestPermission = findViewById(R.id.btnRequestPermission);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        switchLowStock = findViewById(R.id.switchLowStock);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnBackToData = findViewById(R.id.btnBackToData);

        // Load saved settings into the UI
        SmsSettings settings = smsRepo.getSettings(currentUser);
        edtPhoneNumber.setText(settings.phoneNumber);
        switchLowStock.setChecked(settings.lowStockEnabled);

        // Show current permission status text
        refreshPermissionStatus();

        // Button listeners
        btnRequestPermission.setOnClickListener(v -> showPermissionDialog());
        btnBackToData.setOnClickListener(v -> finish());
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh status when returning to this screen
        refreshPermissionStatus();
    }

    /**
     * Saves the phone number and low-stock toggle for the current user.
     */
    private void saveSettings() {
        String phone = edtPhoneNumber.getText().toString().trim();
        boolean lowStockEnabled = switchLowStock.isChecked();

        // Require phone number if alerts are enabled
        if (lowStockEnabled && phone.isEmpty()) {
            Toast.makeText(this, "Enter a phone number to enable low-stock alerts.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Persist settings to SQLite
        smsRepo.saveSettings(currentUser, phone, lowStockEnabled);
        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
    }

    //Updates permission status based on per user choice

    private void refreshPermissionStatus() {
        SmsSettings settings = smsRepo.getSettings(currentUser);

        // Check platform-level permission state
        boolean osGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        // Default: Unknown until the user makes an in-app choice
        if (settings.permissionChoice == SmsSettings.CHOICE_UNKNOWN) {
            setStatusText("Unknown", 0xFFFF0000);
            btnRequestPermission.setEnabled(true);
            btnRequestPermission.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_500)
            );
            return;
        }

        // If user accepted in-app, reflect actual OS permission result
        if (settings.permissionChoice == SmsSettings.CHOICE_ACCEPTED) {
            if (osGranted) {
                setStatusText("Granted", 0xFF00AA00);
                btnRequestPermission.setEnabled(false);
            } else {
                setStatusText("Declined", 0xFFFF0000);
                btnRequestPermission.setEnabled(true);
            }
            btnRequestPermission.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_500)
            );
            return;
        }

        // If user declined in-app, show Declined
        setStatusText("Declined", 0xFFFF0000);
        btnRequestPermission.setEnabled(true);
        btnRequestPermission.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.purple_500)
        );
    }

    /**
     * Colors only the final status word in the label.
     */
    private void setStatusText(String statusWord, int statusColor) {
        String fullText = "SMS Permission Status: " + statusWord;
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(statusWord);
        if (start >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(statusColor),
                    start,
                    start + statusWord.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        txtSmsPermissionStatus.setText(spannable);
    }

    // Dialog asking user if they want to enable SMS permissions

    private void showPermissionDialog() {
        // Inflate custom permission dialog layout
        android.view.View popupView = getLayoutInflater().inflate(R.layout.dialog_sms_permission, null);

        Button btnYes = popupView.findViewById(R.id.btnYes);
        Button btnNo = popupView.findViewById(R.id.btnNo);
        ImageView imgClose = popupView.findViewById(R.id.imgClose);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(popupView)
                .setCancelable(false)
                .create();

        // Close dialog
        imgClose.setOnClickListener(v -> dialog.dismiss());

        // User chooses to proceed with OS permission request
        btnYes.setOnClickListener(v -> {
            // Store per-user "Accepted" choice
            smsRepo.setPermissionChoice(currentUser, SmsSettings.CHOICE_ACCEPTED);

            // Trigger the Android permission prompt
            ActivityCompat.requestPermissions(
                    SmsSettingsActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_CODE
            );
            dialog.dismiss();
        });

        // User declines in-app
        btnNo.setOnClickListener(v -> {
            // Store per-user "Declined" choice
            smsRepo.setPermissionChoice(currentUser, SmsSettings.CHOICE_DECLINED);
            refreshPermissionStatus();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle Android permission result
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Persist accepted state for this user
                smsRepo.setPermissionChoice(currentUser, SmsSettings.CHOICE_ACCEPTED);
            } else {
                // Persist declined state for this user
                smsRepo.setPermissionChoice(currentUser, SmsSettings.CHOICE_DECLINED);
            }
            refreshPermissionStatus();
        }
    }
}