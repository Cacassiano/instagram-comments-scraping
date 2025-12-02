package dev.cacassiano.comment_scraper.repositories;

import dev.cacassiano.comment_scraper.models.InstagramComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstagramCommentRepository extends JpaRepository<InstagramComment, Long> {

    List<InstagramComment> findByPostId(String postId);

    List<InstagramComment> findByUsername(String username);

    List<InstagramComment> findByPostIdAndIsReplyFalse(String postId);

    List<InstagramComment> findByPostIdAndIsReplyTrue(String postId);

    List<InstagramComment> findByScrapedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT ic FROM InstagramComment ic WHERE ic.postId = :postId AND ic.isReply = false ORDER BY ic.likesCount DESC")
    List<InstagramComment> findTopCommentsByPost(@Param("postId") String postId);

    @Query("SELECT COUNT(ic) FROM InstagramComment ic WHERE ic.postId = :postId")
    long countCommentsByPost(@Param("postId") String postId);

    Optional<InstagramComment> findByUsernameAndCommentAndPostId(String username, String comment, String postId);

    List<InstagramComment> findByUsernameAndPostId(String username, String postId);

}
