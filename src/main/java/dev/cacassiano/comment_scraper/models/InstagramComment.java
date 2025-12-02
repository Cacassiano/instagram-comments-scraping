package dev.cacassiano.comment_scraper.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "instagram_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstagramComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String postUrl;

    @Column(nullable = false)
    private String postId;

    @Column(name = "likes_count")
    private Long likesCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

    @Column(name = "author_profile_url")
    private String authorProfileUrl;

    @Column(name = "is_reply")
    private Boolean isReply;

    @Column(name = "parent_comment_id")
    private String parentCommentId;

}
