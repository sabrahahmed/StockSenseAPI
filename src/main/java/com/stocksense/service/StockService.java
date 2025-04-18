package com.stocksense.service;

import com.stocksense.dto.FavoriteStockDTO;
import com.stocksense.model.Favorite;
import com.stocksense.model.NewsArticle;
import com.stocksense.model.StockAbout;
import com.stocksense.model.StockDetails;
import com.stocksense.repository.FavoriteRepository;
import com.stocksense.repository.StockRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final NewsService newsService;       // Still here for hotNews, if you want
    private final AIService aiService;           // Still here for regenerateSummary, etc.
    private final YFinanceService yFinanceService;
    private final PriceService priceService;

    private final StockRepository stockRepository;
    private final FavoriteRepository favoriteRepository;

    @Autowired
    public StockService(NewsService newsService,
                        AIService aiService,
                        YFinanceService yFinanceService,
                        PriceService priceService,
                        StockRepository stockRepository,
                        FavoriteRepository favoriteRepository) {
        this.newsService = newsService;
        this.aiService = aiService;
        this.priceService = priceService;
        this.stockRepository = stockRepository;
        this.favoriteRepository = favoriteRepository;
        this.yFinanceService = yFinanceService;
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String getOverallStockSentiment(String stockSymbol) {
        try {
            // Retrieve cached articles from Redis
            String cacheKey = "news:" + stockSymbol;
            @SuppressWarnings("unchecked")
            List<NewsArticle> articles = (List<NewsArticle>) redisTemplate.opsForValue().get(cacheKey);
            System.out.println("ARTICLES::: " + articles);
            if (articles == null) {
                return "No articles cached";
            }

            // Extract sentiment scores from the 15 articles
            List<Integer> scores = articles.stream()
                    .limit(15)
                    .map(article -> {
                        try {
                            return Integer.parseInt(article.getScore());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

//            if (scores.size() < 15) {
//                return "Invalid or missing scores in cached articles";
//            }

            System.out.println("Number of scores being passed: " + scores.size());

            // Build a comma-separated string of scores
            String scoresArg = scores.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            System.out.println("SCORES::: " + scoresArg);

            // Call the Python script with scores as CLI arg
            List<String> command = List.of(
                    "/Users/ahmedsabrah/Desktop/Capstone/stocksense/venv/bin/python",
                    "/Users/ahmedsabrah/Desktop/Capstone/stocksense/src/main/resources/scripts/overall_sentiment.py",
                    scoresArg
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Capture Python output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String prediction = reader.readLine();
            process.waitFor();

            if (prediction == null || prediction.isBlank()) {
                return "No sentiment predicted";
            }

            return prediction.trim(); // ✅ now returns string like "short-term bearish"

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating sentiment";
        }
    }


    public CompletableFuture<List<NewsArticle>> getStockNews(String stockId, int page) {
        return CompletableFuture.supplyAsync(() -> {
            List<NewsArticle> articles = yFinanceService.fetchYFinanceNews(stockId, page);
            return articles;
        });
    }

    public CompletableFuture<List<NewsArticle>> getHotNews(int limit, int page) {
        return newsService.fetchHotNews(limit, page)
                .thenApplyAsync(articles -> {
                    // Create a list of CompletableFutures for AI processing
                    List<CompletableFuture<Void>> aiFutures = new ArrayList<>();

                    for (NewsArticle article : articles) {
                        CompletableFuture<Void> aiFuture = aiService.analyzeArticleAsync(article.getLink())
                                .thenAccept(results -> {
                                    if (results != null) {
                                        JSONArray summaryArray = results.optJSONArray("summary");
                                        List<String> summaryList = new ArrayList<>();
                                        if (summaryArray != null) {
                                            for (int i = 0; i < summaryArray.length(); i++) {
                                                summaryList.add(summaryArray.optString(i));
                                            }
                                        } else {
                                            summaryList.add("No summary available");
                                        }                                        String score = results.optString("sentimentScore", "0");
                                        String timeSaved = results.optString("timeSaved", "0");

                                        article.setSummary(summaryList);
                                        article.setScore(score);
                                        article.setTimeSaved(timeSaved);
                                    } else {
                                        System.out.println("AI service returned no results for article: " + article.getTitle());
                                    }
                                });

                        aiFutures.add(aiFuture);
                    }

                    // Wait for all AI analysis tasks to complete asynchronously
                    CompletableFuture.allOf(aiFutures.toArray(new CompletableFuture[0])).join();

                    return articles;
                });
    }

    // Regenerate the summary and sentiment for a specific article link
    public CompletableFuture<Map<String, String>> regenerateSummary(String articleLink) {
        return aiService.analyzeArticleAsync(articleLink)
                .thenApplyAsync(results -> {
                    Map<String, String> resultMap = new HashMap<>();

                    if (results != null) {
                        // Extract summary, sentiment score, and time saved
                        String summary = results.optString("summary", "No summary available");
                        String sentimentScore = results.optString("sentimentScore", "0");
                        String timeSaved = results.optString("timeSaved", "0");

                        // Populate the map with the necessary key-value pairs
                        resultMap.put("summary", summary);
                        resultMap.put("sentimentScore", sentimentScore);
                        resultMap.put("timeSaved", timeSaved);
                    } else {
                        // If AI fails to return results, set defaults
                        resultMap.put("summary", "Failed to generate summary.");
                        resultMap.put("sentimentScore", "0");
                        resultMap.put("timeSaved", "0");
                    }

                    return resultMap;
                });
    }

    // Get stock info from the DB
    public StockAbout getStockInfoFromDatabase(String stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock info not found for " + stockId));
    }

    // Get stock price information
    public StockDetails getStockPrice(String stockSymbol) {
        return priceService.getStockPrice(stockSymbol);
    }

    // Retrieve favorites
    public List<FavoriteStockDTO> getFavoriteStocks(Long userId) {
        // Get the list of favorite stock symbols for the given user
        List<String> favoriteSymbols = favoriteRepository.findByUserId(userId)
                .stream()
                .map(Favorite::getStockSymbol)
                .toList();

        // Fetch stock details and map them to FavoriteStockDTO
        return favoriteSymbols.stream()
                .map(symbol -> {
                    // Get the stock details for each symbol
                    StockDetails stockDetails = priceService.getStockPrice(symbol);

                    // Create DTO with only the required fields
                    return new FavoriteStockDTO(
                            stockDetails.getSymbol(),
                            stockDetails.getClosePrice(),
                            stockDetails.getDailyChange()
                    );
                })
                .collect(Collectors.toList());
    }
}
