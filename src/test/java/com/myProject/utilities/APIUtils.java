package com.myProject.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class APIUtils {

    // Base URL for Rapid Translate Multi Traduction API
    private static final String API_URL = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";
    private static final String API_KEY = "ea0803d75fmsha8ca955d52b3e20p159cdcjsnd3fcae71a28d"; // Replace with your key

    public static String translateText(String text, String targetLanguage) {
        String translatedText = "";

        try {
            // Prepare the request body as JSON
            String requestBody = String.format(
                    "{\"from\":\"es\",\"to\":\"%s\",\"text\":\"%s\"}",
                    targetLanguage,
                    text
            );
           System.out.println(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-key", API_KEY)
                    .header("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com")
                    .header("x-rapidapi-ua", "RapidAPI-Playground")
                    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();
            // Print the response body for debugging
            System.out.println("Response Body: " + response.body());
            if (responseCode == 200) {
                // Parse the response body
                JsonElement jsonElement = JsonParser.parseString(response.body());
                if (jsonElement.isJsonArray() && !jsonElement.getAsJsonArray().isEmpty()) {
                    translatedText = jsonElement.getAsJsonArray().get(0).getAsString();
                } else {
                    translatedText = "Translation error";
                }
            } else {
                translatedText = "Translation failed with response code " + responseCode;
            }
        } catch (IOException | InterruptedException e) {
            translatedText = "Translation failed due to exception: " + e.getMessage();
        }

        return translatedText;
    }
}
