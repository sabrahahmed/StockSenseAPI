package com.stocksense.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocksense.dto.FavoriteStockDTO;
import com.stocksense.model.*;
import com.stocksense.repository.FavoriteRepository;
import com.stocksense.repository.UserRepository;
import com.stocksense.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    @Autowired
    private final StockService stockService;

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public StockController(StockService stockService, FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.stockService = stockService;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @Autowired
    private ObjectMapper objectMapper;
    @GetMapping(value = "/{stockSymbol}/news", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getStockNews(@PathVariable String stockSymbol, @RequestParam(defaultValue = "1") int page) {
        SseEmitter emitter = new SseEmitter(0L);
        String cacheKey = "news:" + stockSymbol;

        // Try to fetch cached articles
        List<NewsArticle> cachedArticles = (List<NewsArticle>) redisTemplate.opsForValue().get(cacheKey);

        // ✅ Use cache only if it has 15 or more articles
        if (cachedArticles != null && cachedArticles.size() >= 15) {
            try {
                for (NewsArticle article : cachedArticles) {
                    String json = objectMapper.writeValueAsString(List.of(article)); // Send as array of one
                    emitter.send(SseEmitter.event().name("news-batch").data(json));
                }
                emitter.complete(); // All cached articles sent
            } catch (Exception e) {
                e.printStackTrace();
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // ❌ Fallback: run Python script to fetch news
        CompletableFuture.runAsync(() -> {
            List<NewsArticle> accumulatedArticles = new ArrayList<>();
            try {
                List<String> command = Arrays.asList(
                        "/Users/ahmedsabrah/Desktop/Capstone/stocksense/venv/bin/python",
                        "/Users/ahmedsabrah/Desktop/Capstone/stocksense/src/main/resources/scripts/NewsSummarizer.py",
                        stockSymbol
                );
                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    List<NewsArticle> batch = parseNewsBatch(line);
                    accumulatedArticles.addAll(batch);

                    emitter.send(SseEmitter.event().name("news-batch").data(line));
                    emitter.send(SseEmitter.event().comment("keep-alive"));

                    // Cache only once we reach 15 articles
                    if (accumulatedArticles.size() == 15) {
                        redisTemplate.opsForValue().set(cacheKey, accumulatedArticles, Duration.ofMinutes(60));
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


    private List<NewsArticle> parseNewsBatch(String jsonLine) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Assuming jsonLine is a JSON array (e.g. "[{...}]")
        return mapper.readValue(jsonLine, new TypeReference<List<NewsArticle>>() {});
    }

    @GetMapping("/hot-news")
    public CompletableFuture<ResponseEntity<List<NewsArticle>>> getHotNews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int limit) {

        return stockService.getHotNews(limit, page)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{stockSymbol}/details")
    public ResponseEntity<StockDetails> getStockPrice(@PathVariable String stockSymbol) {
        StockDetails stockDetailsResponse = stockService.getStockPrice(stockSymbol);
        if (stockDetailsResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stockDetailsResponse);
    }

    // Get Static Stock Info (from Database)
    @GetMapping("/{stockSymbol}/info")
    public ResponseEntity<StockAbout> getStockInfo(@PathVariable String stockSymbol) {
        return ResponseEntity.ok(stockService.getStockInfoFromDatabase(stockSymbol));
    }

    @PostMapping("/regenerate-summary")
    public CompletableFuture<Map<String, String>> regenerateSummary(@RequestParam String articleLink) {
        return stockService.regenerateSummary(articleLink);
    }

    // Get Overall Stock Sentiment Score
    @GetMapping("/{stockSymbol}/sentiment")
    public ResponseEntity<String> getOverallStockSentiment(@PathVariable String stockSymbol) {
        return ResponseEntity.ok(stockService.getOverallStockSentiment(stockSymbol));
    }

    // FAVORITES

    // Get user's favorite stocks
    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteStockDTO>> getFavorites(@RequestParam Long userId) {
        List<FavoriteStockDTO> favoriteStocks = stockService.getFavoriteStocks(userId);
        return ResponseEntity.ok(favoriteStocks);
    }

    @PostMapping("/favorites")
    public void addFavoriteStock(@RequestParam Long userId, @RequestParam String stockSymbol) {
        // Fetch the User from the UserRepository
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));  // Handle user not found

        // Save the Favorite entry with the User and stockSymbol
        favoriteRepository.save(new Favorite(user, stockSymbol));
    }

    // Remove a stock from the user's favorites
    @DeleteMapping("/favorites")
    public ResponseEntity<String> removeFavoriteStock(@RequestParam Long userId, @RequestParam String stockSymbol) {
        // Check if the favorite exists for the user
        Favorite favorite = favoriteRepository.findByUserIdAndStockSymbol(userId, stockSymbol)
                .orElseThrow(() -> new RuntimeException("Favorite stock not found for this user")); // Handle favorite not found

        // Delete the favorite entry
        favoriteRepository.delete(favorite);

        return ResponseEntity.ok("Favorite stock removed successfully");
    }
}
