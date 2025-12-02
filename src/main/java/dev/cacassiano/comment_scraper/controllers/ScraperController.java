package dev.cacassiano.comment_scraper.controllers;

import dev.cacassiano.comment_scraper.services.InstagramScraperService;
import dev.cacassiano.comment_scraper.services.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ScraperController {

    private final InstagramScraperService instagramScraperService;
    private final ExcelExportService excelExportService;

    @GetMapping("/scrape")
    public ResponseEntity<Map<String, String>> scrapeVideo() {
        try {
            instagramScraperService.scrapeComments();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Instagram comments scraping started successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error during scraping: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comments/{postId}")
    public ResponseEntity<Map<String, Object>> getCommentsByPostId(@PathVariable String postId) {
        try {
            var comments = instagramScraperService.getCommentsByPostId(postId);
            var totalCount = instagramScraperService.getTotalCommentCount(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("postId", postId);
            response.put("totalComments", totalCount);
            response.put("comments", comments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error retrieving comments: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comments/user/{username}")
    public ResponseEntity<Map<String, Object>> getCommentsByUsername(@PathVariable String username) {
        try {
            var comments = instagramScraperService.getCommentsByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("username", username);
            response.put("totalComments", comments.size());
            response.put("comments", comments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error retrieving comments: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCommentsExcel() {
        try {
            byte[] data = excelExportService.generateCommentsExcel();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=instagram_comments.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(data);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
