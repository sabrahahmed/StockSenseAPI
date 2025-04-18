package com.stocksense.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Service
public class AIService {

    private final String scriptPath;
    @Autowired
    public AIService() {
        this.scriptPath = "/Users/ahmedsabrah/Desktop/Capstone/stocksense/src/main/resources/scripts/AI.py";
    }
    public JSONObject analyzeArticle(String articleLink) {
        try {
            // Build the command to call the Python script
            String[] command = {
                    "/Users/ahmedsabrah/Desktop/Capstone/stocksense/venv/bin/python",
                    scriptPath,
                    articleLink
            };

            // Execute the Python script
            Process process = new ProcessBuilder(command).start();

            // Capture the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Wait for the process to finish
            process.waitFor();

            // Output from Python script (JSON result)
            String result = output.toString();

            // Parse the result into a JSON object
            return new JSONObject(result);

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle error (or return a custom error object if necessary)
        }
    }

    public CompletableFuture<JSONObject> analyzeArticleAsync(String articleLink) {
        return CompletableFuture.supplyAsync(() -> analyzeArticle(articleLink));
    }
}
