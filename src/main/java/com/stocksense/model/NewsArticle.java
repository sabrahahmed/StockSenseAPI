package com.stocksense.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "news_articles")
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Removed `unique = true` to allow duplicate titles if necessary.
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String publishedAt;

    @Column(columnDefinition = "TEXT")
    private List<String> summary;
    private String score;
    private String sentiment;
    private String explanation;
    private String timeSaved;

    public NewsArticle() {}

    // Optional convenience constructor
    public NewsArticle(String title, String link, String publishedAt,
                       List<String> summary, String score, String sentiment, String explanation, String timeSaved) {
        this.title = title;
        this.link = link;
        this.publishedAt = publishedAt;
        this.summary = summary;
        this.score = score;
        this.sentiment = sentiment;
        this.explanation = explanation;
        this.timeSaved = timeSaved;
    }

    // Getters and Setters...

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<String> getSummary() {
        return summary;
    }
    public void setSummary(List<String> summary) {
        this.summary = summary;
    }

    public String getScore() {
        return score;
    }
    public void setScore(String score) {
        this.score = score;
    }

    public String getSentiment() {
        return sentiment;
    }
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getTimeSaved() {
        return timeSaved;
    }
    public void setTimeSaved(String timeSaved) {
        this.timeSaved = timeSaved;
    }
}
