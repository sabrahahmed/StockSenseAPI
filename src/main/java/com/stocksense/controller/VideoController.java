package com.stocksense.controller;

import com.stocksense.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RequestMapping("/api/learn")

@RestController
public class VideoController {

    @Autowired
    private VideoService videoService;

    // Endpoint to fetch YouTube videos based on user favorites
    @GetMapping("/{userId}/videos")
    public List<String> getUserVideos(@PathVariable Long userId) {
        return videoService.getUserVideos(userId);
    }
}
