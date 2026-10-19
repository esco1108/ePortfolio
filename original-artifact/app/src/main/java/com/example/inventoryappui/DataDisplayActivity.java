package com.example.inventoryappui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataDisplayActivity extends AppCompatActivity {

    private static final int LOW_STOCK_THRESHOLD = 2;

    private LinearLayout itemContainer;
    private EditText edtNewItemName;
    private EditText edtNewItemQty;
    private EditText edtNewItemLocation;
    private Button btnAddItem;
    private Button btnSmsSettings;
    private Button btnLogout;

    private ItemRepository itemRepo;
    private SmsSettingsRepository smsRepo;

    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        currentUser = SessionManager.getCurrentUsername(this);
        if (currentUser == null || currentUser.trim().isEmpty()) {
            // Safety: session expired or user navigated here unexpectedly.
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        itemRepo = new ItemRepository(this);
        smsRepo = new SmsSettingsRepository(this);

        itemContainer = findViewById(R.id.itemContainer);
        edtNewItemName = findViewById(R.id.edtNewItemName);
        edtNewItemQty = findViewById(R.id.edtNewItemQty);
        edtNewItemLocation = findViewById(R.id.edtNewItemLocation);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSmsSettings = findViewById(R.id.btnSmsSettings);
        btnLogout = findViewById(R.id.btnLogout);

        // Seed demo data once per user if no items
        if (itemRepo.getAllItems(currentUser).isEmpty()) {
            itemRepo.addItem(currentUser,
                    "Item A",
                    5,
                    "Storage Room",
                    "Stock stored in the back storage room on shelves.",
                    nowDate());
            itemRepo.addItem(currentUser,
                    "Item B",
                    12,
                    "Front Shelf",
                    "Items currently displayed on the front shelf for customers.",
                    nowDate());
        }

        renderItems();

        btnAddItem.setOnClickListener(v -> addItem());
        btnSmsSettings.setOnClickListener(v -> startActivity(new Intent(this, SmsSettingsActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh after edits/returning from SMS settings
        renderItems();
    }

    private void addItem() {
        String name = edtNewItemName.getText().toString().trim();
        String qtyText = edtNewItemQty.getText().toString().trim();
        String location = edtNewItemLocation.getText().toString().trim();

        if (name.isEmpty() || qtyText.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please enter item name, quantity, and location.", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = "No description provided.";
        String lastEdited = nowDate();

        long id = itemRepo.addItem(currentUser, name, qty, location, description, lastEdited);
        if (id == -1) {
            Toast.makeText(this, "Failed to add item.", Toast.LENGTH_SHORT).show();
            return;
        }

        edtNewItemName.getText().clear();
        edtNewItemQty.getText().clear();
        edtNewItemLocation.getText().clear();

        renderItems();

        // Trigger SMS if configured
        maybeSendLowStockSms(name, qty);
    }

    private void logout() {
        SessionManager.clear(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void renderItems() {
        itemContainer.removeAllViews();

        List<ItemRepository.ItemRow> items = itemRepo.getAllItems(currentUser);

        for (ItemRepository.ItemRow item : items) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowLayout.setPadding(0, dp(8), 0, dp(8));
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView txtName = new TextView(this);
            txtName.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));
            txtName.setText(item.name);
            txtName.setTextSize(12f);

            TextView txtQty = new TextView(this);
            txtQty.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));
            txtQty.setText(String.valueOf(item.quantity));
            txtQty.setTextSize(12f);
            txtQty.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView txtLocation = new TextView(this);
            txtLocation.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2f
            ));
            txtLocation.setText(item.location);
            txtLocation.setTextSize(12f);
            txtLocation.setSingleLine(true);
            txtLocation.setGravity(Gravity.CENTER_HORIZONTAL);

            LinearLayout deleteContainer = new LinearLayout(this);
            deleteContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));
            deleteContainer.setGravity(Gravity.CENTER);

            Button btnDelete = new Button(this);
            btnDelete.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(32)
            ));
            btnDelete.setText("DELETE");
            btnDelete.setAllCaps(true);
            btnDelete.setTextSize(10f);
            btnDelete.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500, getTheme()));
            btnDelete.setPadding(dp(12), 0, dp(12), 0);
            deleteContainer.addView(btnDelete);

            LinearLayout infoContainer = new LinearLayout(this);
            infoContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));
            infoContainer.setGravity(Gravity.CENTER);

            Button btnInfo = new Button(this);
            btnInfo.setLayoutParams(new LinearLayout.LayoutParams(
                    dp(40),
                    dp(32)
            ));
            btnInfo.setText("i");
            btnInfo.setAllCaps(false);
            btnInfo.setTextSize(12f);
            btnInfo.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            btnInfo.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500, getTheme()));
            btnInfo.setPadding(0, 0, 0, 0);
            infoContainer.addView(btnInfo);

            rowLayout.addView(txtName);
            rowLayout.addView(txtQty);
            rowLayout.addView(txtLocation);
            rowLayout.addView(deleteContainer);
            rowLayout.addView(infoContainer);

            itemContainer.addView(rowLayout);

            btnDelete.setOnClickListener(v -> {
                itemRepo.deleteItem(item.id, currentUser);
                renderItems();
            });

            btnInfo.setOnClickListener(v -> showItemInfoDialog(item));
        }
    }

    private void showItemInfoDialog(ItemRepository.ItemRow item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_info, null);

        TextView txtDescription = dialogView.findViewById(R.id.txtDescription);
        TextView txtLastEditedValue = dialogView.findViewById(R.id.txtLastEditedValue);
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        Button btnEditItem = dialogView.findViewById(R.id.btnEditItem);

        txtDescription.setText(item.description);
        txtLastEditedValue.setText(item.lastEdited);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        imgClose.setOnClickListener(v -> dialog.dismiss());
        btnEditItem.setOnClickListener(v -> {
            dialog.dismiss();
            showItemEditDialog(item);
        });

        dialog.show();
    }

    private void showItemEditDialog(ItemRepository.ItemRow item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_edit, null);

        EditText edtName = dialogView.findViewById(R.id.edtEditName);
        EditText edtQty = dialogView.findViewById(R.id.edtEditQty);
        EditText edtLocation = dialogView.findViewById(R.id.edtEditLocation);
        EditText edtDescription = dialogView.findViewById(R.id.edtEditDescription);
        ImageView imgClose = dialogView.findViewById(R.id.imgCloseEdit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveItem);

        edtName.setText(item.name);
        edtQty.setText(String.valueOf(item.quantity));
        edtLocation.setText(item.location);
        edtDescription.setText(item.description);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        imgClose.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            String qtyText = edtQty.getText().toString().trim();
            String newLocation = edtLocation.getText().toString().trim();
            String newDescription = edtDescription.getText().toString().trim();

            if (newName.isEmpty() || qtyText.isEmpty() || newLocation.isEmpty()) {
                Toast.makeText(this, "Name, quantity, and location cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQty;
            try {
                newQty = Integer.parseInt(qtyText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantity must be a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }

            String lastEdited = nowDate();
            itemRepo.updateItem(item.id, currentUser, newName, newQty, newLocation,
                    newDescription.isEmpty() ? "No description provided." : newDescription,
                    lastEdited);

            dialog.dismiss();
            renderItems();

            // Trigger SMS if configured
            maybeSendLowStockSms(newName, newQty);
        });

        dialog.show();
    }

    private void maybeSendLowStockSms(String itemName, int qty) {
        try {
            SmsSettings settings = smsRepo.getSettings(currentUser);

            if (!settings.lowStockEnabled) return;

            // Only send if the user explicitly accepted permission in-app
            if (settings.permissionChoice != SmsSettings.CHOICE_ACCEPTED) return;

            if (qty > LOW_STOCK_THRESHOLD) return;
            if (settings.phoneNumber.trim().isEmpty()) return;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String msg = "Low stock alert: " + itemName + " (Qty: " + qty + ")";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(settings.phoneNumber, null, msg, null, null);

            Toast.makeText(this, "Low stock SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
        }
    }

    private String nowDate() {
        return new SimpleDateFormat("MM/dd/yy", Locale.US).format(new Date());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
