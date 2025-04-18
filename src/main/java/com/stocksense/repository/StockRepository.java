package com.stocksense.repository;

import com.stocksense.model.StockAbout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<StockAbout, String> {
}
