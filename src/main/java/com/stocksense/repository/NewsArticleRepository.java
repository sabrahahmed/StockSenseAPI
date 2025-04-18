package com.stocksense.repository;

import com.stocksense.model.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    // Optionally add custom queries if needed
}
