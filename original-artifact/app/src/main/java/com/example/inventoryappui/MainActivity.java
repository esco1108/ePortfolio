package com.example.inventoryappui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Login and account creation screen

public class MainActivity extends AppCompatActivity {

    // UI
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button createAccountButton;
    private TextView statusMessage;
    private TextView forgotCredentialsLink;

    // User database access
    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init repository
        userRepo = new UserRepository(this);

        // Bind UI
        usernameInput = findViewById(R.id.edtUsername);
        passwordInput = findViewById(R.id.edtPassword);
        loginButton = findViewById(R.id.btnLogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);
        statusMessage = findViewById(R.id.txtStatusMessage);
        forgotCredentialsLink = findViewById(R.id.txtForgotCredentials);

        // Hide status until needed
        statusMessage.setVisibility(View.GONE);

        // Underline the forgot user/password text
        forgotCredentialsLink.setPaintFlags(
                forgotCredentialsLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        // Button actions
        loginButton.setOnClickListener(v -> handleLogin());
        createAccountButton.setOnClickListener(v -> handleCreateAccount());

        // Placeholder behavior for recovery link
        forgotCredentialsLink.setOnClickListener(v -> Toast.makeText(
                MainActivity.this,
                "Please contact support to recover your credentials.",
                Toast.LENGTH_LONG
        ).show());
    }

    private void handleLogin() {
        // Read input
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password.", true);
            return;
        }

        // Check credentials in DB
        boolean ok = userRepo.validateUser(username, password);
        if (!ok) {
            // Show a clearer error message
            if (!userRepo.userExists(username)) {
                showStatus("Account not found. Please create an account first.", true);
            } else {
                showStatus("Incorrect password. Please try again.", true);
            }
            return;
        }

        // Save session and go to inventory screen
        SessionManager.setCurrentUsername(this, username);
        statusMessage.setVisibility(View.GONE);

        Intent intent = new Intent(this, DataDisplayActivity.class);
        startActivity(intent);
    }

    private void handleCreateAccount() {
        // Read input
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Username and password cannot be empty.", true);
            return;
        }

        // Insert new user
        boolean created = userRepo.createUser(username, password);
        if (!created) {
            showStatus("Username already exists. Please choose a different username.", true);
            return;
        }

        // Success message
        showStatus("New user account created. Please login.", false);

        // Clear password field
        passwordInput.getText().clear();
    }

    private void showStatus(String message, boolean isError) {
        // Display message with error/success color
        statusMessage.setText(message);
        statusMessage.setTextColor(isError ? 0xFFFF0000 : 0xFF00AA00);
        statusMessage.setVisibility(View.VISIBLE);
    }
}