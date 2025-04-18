package com.stocksense.service;

import com.stocksense.model.NewsArticle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A service that fetches a single "page" of 3 articles from NewsSummarizer.py,
 * by passing the 'page' argument.
 */
@Service
public class YFinanceService {

    private final String scriptPath = "/Users/ahmedsabrah/Desktop/Capstone/stocksense/src/main/resources/scripts/NewsSummarizer.py";

    public List<NewsArticle> fetchYFinanceNews(String tickerSymbol, int page) {
        try {
            String[] command = {
                    "/Users/ahmedsabrah/Desktop/Capstone/stocksense/venv/bin/python",
                    scriptPath,
                    tickerSymbol,
                    String.valueOf(page)
            };

            Process process = new ProcessBuilder(command).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            process.waitFor();
            System.out.println("OUTPUT::: " + output);

            // Output is a JSON array of up to 3 articles
            JSONArray jsonArray = new JSONArray(output.toString());
            List<NewsArticle> articles = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                NewsArticle article = new NewsArticle();
                article.setTitle(obj.optString("title", "Untitled"));
                article.setLink(obj.optString("link", ""));
                article.setPublishedAt(obj.optString("publication_time", ""));
                JSONArray summaryArray = obj.optJSONArray("summary");
                List<String> summaryList = new ArrayList<>();
                if (summaryArray != null) {
                    for (int x = 0; x < summaryArray.length(); x++) {
                        summaryList.add(summaryArray.optString(i));
                    }
                } else {
                    summaryList.add("No summary available");
                }                article.setScore(obj.optString("score", "N/A"));
                article.setSentiment(obj.optString("sentiment", "N/A"));
                article.setExplanation(obj.optString("explanation", "N/A"));
                article.setTimeSaved(obj.optString("timeSaved", "N/A"));
                articles.add(article);
            }

            return articles;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
