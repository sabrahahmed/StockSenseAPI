package com.stocksense.service;

import com.stocksense.dto.youtube.YouTubeItemDto;
import com.stocksense.dto.youtube.YouTubeSearchResponseDto;
import com.stocksense.model.Favorite;
import com.stocksense.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class VideoService {
    private static final Map<String, String> tickerMap = Map.ofEntries(
            Map.entry("AAPL", "Apple"), Map.entry("AMZN", "Amazon"), Map.entry("GOOGL", "Google"),
            Map.entry("NVDA", "Nvidia"), Map.entry("MSFT", "Microsoft"), Map.entry("META", "Meta"),
            Map.entry("TSLA", "Tesla"), Map.entry("AMD", "AMD"), Map.entry("INTC", "Intel"),
            Map.entry("NFLX", "Netflix"), Map.entry("IBM", "IBM"), Map.entry("UBER", "Uber"),
            Map.entry("LYFT", "Lyft"), Map.entry("CRM", "Salesforce"), Map.entry("ORCL", "Oracle"),
            Map.entry("CSCO", "Cisco"), Map.entry("PYPL", "PayPal"), Map.entry("ADBE", "Adobe"),
            Map.entry("SPOT", "Spotify"), Map.entry("SHOP", "Shopify"), Map.entry("SQ", "Square"),
            Map.entry("BA", "Boeing"), Map.entry("F", "Ford"), Map.entry("GM", "General Motors"),
            Map.entry("SBUX", "Starbucks"), Map.entry("KO", "Coca-Cola"), Map.entry("PEP", "Pepsi"),
            Map.entry("WMT", "Walmart"), Map.entry("TGT", "Target"), Map.entry("DIS", "Disney")
    );

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_KEY = "AIzaSyB8UT99yJfXq3TLQThPh3O0WS7Pzmpl0PU";
    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final int MAX_VIDEOS = 20;

    public List<String> getUserVideos(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        List<String> youtubeLinks = new ArrayList<>();
        Queue<Favorite> stockQueue = new LinkedList<>(favorites);

        // If userId is 0 OR user has no favorites, fetch 20 generic videos
        if (userId == 0 || favorites.isEmpty()) {
            return fetchYouTubeLinks("stock market news", MAX_VIDEOS);
        }

        // Otherwise, fetch 20 videos based on the user's favorites
        Random random = new Random();
        while (youtubeLinks.size() < MAX_VIDEOS && !stockQueue.isEmpty()) {
            Favorite favorite = stockQueue.poll();
            int videosToFetch = Math.min(random.nextInt(6) + 1, MAX_VIDEOS - youtubeLinks.size());
            String query = generateYoutubeQuery(favorite.getStockSymbol());
            youtubeLinks.addAll(fetchYouTubeLinks(query, videosToFetch));
        }

        Collections.shuffle(youtubeLinks);
        return youtubeLinks.subList(0, Math.min(MAX_VIDEOS, youtubeLinks.size()));
    }

    private List<String> fetchYouTubeLinks(String query, int maxResults) {
        String oneMonthAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60).toString();
//        String url = String.format("%s?part=snippet&type=video&q=%s&maxResults=%d&order=viewCount" +
//                        "&videoDuration=short&publishedAfter=%s&key=%s&relevanceLanguage=en",
//                YOUTUBE_API_URL, query, maxResults, oneMonthAgo, API_KEY);
        String url = String.format("%s?part=snippet&type=video&q=%s&maxResults=%d&order=viewCount" +
                        "&publishedAfter=%s&key=%s",
                YOUTUBE_API_URL, query, maxResults, oneMonthAgo, API_KEY);

        YouTubeSearchResponseDto response = restTemplate.getForObject(url, YouTubeSearchResponseDto.class);
        List<String> videoLinks = new ArrayList<>();

        if (response != null && response.getItems() != null) {
            for (YouTubeItemDto item : response.getItems()) {
                videoLinks.add("https://www.youtube.com/watch?v=" + item.getId().getVideoId());
            }
        }
        return videoLinks;
    }

    private String generateYoutubeQuery(String stockSymbol) {
        String companyName = tickerMap.get(stockSymbol);
        return String.format("%s stock news", companyName);
    }
}
