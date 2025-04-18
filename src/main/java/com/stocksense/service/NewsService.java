package com.stocksense.service;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.stocksense.model.NewsArticle;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class NewsService {

    private static final Map<String, String> tickerMap = new HashMap<>() {{
        put("AAPL", "Apple");
        put("AMZN", "Amazon");
        put("GOOGL", "Google");
        put("NVDA", "Nvidia");
        put("MSFT", "Microsoft");
        put("META", "Meta");
        put("TSLA", "Tesla");
        put("AMD", "AMD");
        put("INTC", "Intel");
        put("NFLX", "Netflix");
        put("IBM", "IBM");
        put("UBER", "Uber");
        put("LYFT", "Lyft");
        put("CRM", "Salesforce");
        put("ORCL", "Oracle");
        put("CSCO", "Cisco");
        put("PYPL", "PayPal");
        put("ADBE", "Adobe");
        put("SPOT", "Spotify");
        put("SHOP", "Shopify");
        put("SQ", "Square");
        put("BA", "Boeing");
        put("F", "Ford");
        put("GM", "General Motors");
        put("SBUX", "Starbucks");
        put("KO", "Coca-Cola");
        put("PEP", "Pepsi");
        put("WMT", "Walmart");
        put("TGT", "Target");
        put("DIS", "Disney");
    }};

    private static final String NEWS_API_KEY = "79be2619668f4f349138d7fdb5215418";
    private final NewsApiClient newsApiClient;

    public NewsService() {
        this.newsApiClient = new NewsApiClient(NEWS_API_KEY);
    }

    @Async
    public CompletableFuture<List<NewsArticle>> fetchArticles(String query, int limit, int page, String sortBy) {
        List<NewsArticle> articles = new ArrayList<>();

        CompletableFuture<ArticleResponse> futureResponse = new CompletableFuture<>();

        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .q(query)
                        .pageSize(limit)
                        .page(page)
                        .sortBy(sortBy)
                        .language("en")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        futureResponse.complete(response);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        futureResponse.completeExceptionally(throwable);
                    }
                }
        );

        return futureResponse.thenApply(response -> {
            if (response != null && response.getArticles() != null) {
                for (Article article : response.getArticles()) {
                    articles.add(new NewsArticle(
                            article.getTitle(),
                            article.getUrl(),
                            article.getPublishedAt(),
                            null,  // AI-generated summary
                            null,  // AI-generated score
                            null, // sentiment
                            null, // explanation
                            null   // AI-generated time saved
                    ));

                }
            }
            return articles;
        });
    }

    // Fetch news based on stock symbol
    public CompletableFuture<List<NewsArticle>> fetchStockNews(String stockSymbol, int limit, int page) {
        String query = generateStockQuery(stockSymbol);
        return fetchArticles(query, limit, page, "publishedAt");
    }

    // Fetch hot news
    public CompletableFuture<List<NewsArticle>> fetchHotNews(int limit, int page) {
        String query = generateHotNewsQuery();
        return fetchArticles(query, limit, page, "relevancy");
    }

    // Generate query for stock news
    private String generateStockQuery(String stockSymbol) {
        String companyName = tickerMap.get(stockSymbol);
        return String.format("'%s stock' '%s' '%s' '%s news'", stockSymbol, companyName, companyName, stockSymbol);
    }

    // Generate query for hot news
    private String generateHotNewsQuery() {
        return "'stock' 'stocks'";
    }
}
