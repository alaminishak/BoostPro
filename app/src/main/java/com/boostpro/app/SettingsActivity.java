package com.boostpro.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.graphics.Color;

public class SettingsActivity extends AppCompatActivity implements AIServiceTask.AITaskCallback {
    private EditText apiKeyInput, promptInput;
    private Button saveButton, testButton;
    private TextView statusText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("BoostProPrefs", MODE_PRIVATE);
        initializeViews();
        loadSavedSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        apiKeyInput = findViewById(R.id.apiKeyInput);
        promptInput = findViewById(R.id.promptInput);
        saveButton = findViewById(R.id.saveButton);
        testButton = findViewById(R.id.testButton);
        statusText = findViewById(R.id.statusText);
    }

    private void loadSavedSettings() {
        apiKeyInput.setText(prefs.getString("apiKey", ""));
        String defaultPrompt = "Create engaging social media responses matching platform style. Keep responses concise and appropriate for the context.";
        promptInput.setText(prefs.getString("customPrompt", defaultPrompt));
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveSettings());
        testButton.setOnClickListener(v -> testAPI());
    }

    private void saveSettings() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String prompt = promptInput.getText().toString().trim();

        if (apiKey.isEmpty()) {
            showStatus("Please enter API key", false);
            return;
        }

        if (prompt.isEmpty()) {
            prompt = "Create engaging social media responses matching platform style.";
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("apiKey", apiKey);
        editor.putString("customPrompt", prompt);
        editor.apply();

        showStatus("Settings saved successfully!", true);
    }

    private void testAPI() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String prompt = promptInput.getText().toString().trim();
        
        if (apiKey.isEmpty()) {
            showStatus("Please enter API key first", false);
            return;
        }

        if (prompt.isEmpty()) {
            prompt = "Respond with exactly: TEST_SUCCESS";
        }

        showStatus("Testing API connection...", true);
        
        new AIServiceTask(this).execute("Test message for API connection", prompt, apiKey);
    }

    private void showStatus(String message, boolean isSuccess) {
        statusText.setText(message);
        statusText.setBackgroundColor(isSuccess ? 
            getResources().getColor(R.color.status_success) : 
            getResources().getColor(R.color.status_error));
        statusText.setTextColor(Color.WHITE);
        statusText.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void onTaskCompleted(String result) {
        runOnUiThread(() -> {
            if (result != null && !result.startsWith("Error")) {
                showStatus("API connection successful! Response: " + result, true);
            } else {
                showStatus("API connection failed: " + result, false);
            }
        });
    }
}