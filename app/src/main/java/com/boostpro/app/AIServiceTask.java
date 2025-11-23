package com.boostpro.app;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AIServiceTask extends AsyncTask<String, Void, String> {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private AITaskCallback callback;
    
    public interface AITaskCallback {
        void onTaskCompleted(String result);
    }
    
    public AIServiceTask(AITaskCallback callback) {
        this.callback = callback;
    }
    
    @Override
    protected String doInBackground(String... params) {
        String content = params[0];
        String customPrompt = params[1];
        String apiKey = params[2];
        
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("HTTP-Referer", "https://boostpro-app.com");
            conn.setRequestProperty("X-Title", "BoostPro Android App");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            String requestBody = String.format(
                "{"model": "deepseek/deepseek-chat", " +
                ""messages": [{"role": "user", "content": "%s\n\nContent: %s"}], " +
                ""max_tokens": 500, "temperature": 0.7}",
                customPrompt, content.replace(""", "\"")
            );

            Log.d("AIServiceTask", "Sending request to API");
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d("AIServiceTask", "Response code: " + responseCode);
            
            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String aiResponse = message.getString("content");
                    return aiResponse.trim();
                }
                return "Error: No content in response";
            } else {
                return "Error: HTTP " + responseCode + " - " + response.toString();
            }
        } catch (Exception e) {
            Log.e("AIServiceTask", "Error: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onTaskCompleted(result);
        }
    }
}