package dev.cacassiano.comment_scraper.services;

import dev.cacassiano.comment_scraper.models.InstagramComment;
import dev.cacassiano.comment_scraper.repositories.InstagramCommentRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
// org.jsoup.select.Elements not used directly here
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class InstagramScraperService {

    private static final Logger logger = Logger.getLogger(InstagramScraperService.class.getName());

    private final SeleniumDriverService seleniumDriverService;
    private final InstagramCommentRepository commentRepository;

    @Value("${instagram.comments_container_xpath}")
    private String commentsContainerClass;

    @Value("${instagram.video_link}")
    private String videoLink;

    @Value("${instagram.max_iterations:10000}")
    private int maxIterations;

    @Value("${instagram.no_new_rounds:60}")
    private int maxNoNewRounds;

    @Value("${instagram.scroll_pause_ms:5000}")
    private int scrollPauseMs;

    public void scrapeComments() {
        try {
            // Initialize driver and login
            seleniumDriverService.initializeDriver();
            seleniumDriverService.loginToInstagram();

            // Navigate to the video
            seleniumDriverService.navigateToVideoLink();

            // Scrape comments
            extractComments();

        } catch (Exception e) {
            logger.severe("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            seleniumDriverService.quit();
        }
    }

    private void extractComments() throws InterruptedException {
        WebDriverWait wait = seleniumDriverService.getWait(15);

        WebDriver driver = seleniumDriverService.getDriver();

        try {
            // Initial wait for any comment elements to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(commentsContainerClass)));
            WebElement commentsContainer = driver.findElement(By.xpath(commentsContainerClass));
            // System.out.println(commentsContainer.getAttribute("outerHTML"));
            // Thread.sleep(10000);
            logger.info("Starting per-element scraping loop using Selenium + Jsoup");

            int processed = 0;
            int noNewRounds = 0;
            int iterations = 0;

            String postId = extractPostIdFromUrl(videoLink);

            // Loop until we reach limits or no new elements appear for several rounds
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Random rand = new Random();
            while (iterations < maxIterations && noNewRounds < maxNoNewRounds) {
                iterations++;
                
                List<WebElement> elements = commentsContainer.findElements(By.xpath("./*"));
                logger.info("Iteration " + iterations + ": found " + elements.size() + " processed: "+processed);
                
                WebElement comment1 = elements.get(0);
                String classComment = comment1.getAttribute("class");

                // Volta para o primeiro elemento a cada iteracao
                seleniumDriverService.scrollToElement(comment1);

                System.out.println(classComment);
                
                if (elements.size() > processed) {
                    // Process newly discovered elements (infinite scroll behavior)
                    for (int i = processed; i < elements.size(); i++) {
                        try {
                            
                            WebElement el = elements.get(i);

                            // Scroll to the element (Selenium handles this)
                            seleniumDriverService.scrollToElement(el);
                            // small pause to allow dynamic content to load
                            Thread.sleep(rand.nextInt(300, scrollPauseMs / 5));
                            
                            // Print outer HTML of the single comment div
                            String outerHtml = el.getAttribute("outerHTML");

                            // Parse the single element with Jsoup
                            Document frag = Jsoup.parseBodyFragment(outerHtml);

                            InstagramComment comment = extractCommentDataWithJsoup(frag, postId);
                            if (comment == null) {
                                continue;
                            }

                            // Deduplication: check repository before saving
                            boolean exists = commentRepository.findByUsernameAndCommentAndPostId(
                                    comment.getUsername(), comment.getComment(), comment.getPostId()
                            ).isPresent();

                            if (exists) {
                                logger.info("Duplicate comment skipped for user=" + comment.getUsername());
                            } else {
                                // Save and add to list
                                commentRepository.save(comment);
                                logger.info("Saved comment by " + comment.getUsername());
                            }

                        } catch (Exception e) {
                            logger.warning("Error processing element index " + i + ": "+e.getMessage());
                        }
                    }

                    // update processed count and reset noNewRounds
                    processed = elements.size();
                    noNewRounds = 0;
                    System.out.println(elements.size());
                    if(iterations % 10 == 0 && classComment != null && !classComment.isBlank() && !classComment.isEmpty()){
                        js.executeScript(
                            "document.querySelectorAll('div."+classComment.replace(" ", ".")+"').forEach( e => e.remove() );"
                        );
                        processed = 0;
                    }
                    // ((ChromiumDriver) driver).executeCdpCommand(
                    //     "HeapProfiler.collectGarbage", new HashMap<>()
                    // );
                    // small pause before next iteration to allow more comments to load
                    Thread.sleep(scrollPauseMs);

                } else {
                    // no new elements found this round
                    noNewRounds++;
                    logger.info("No new elements detected (round " + noNewRounds + ")");
                    Thread.sleep(scrollPauseMs);
                }
            }

            logger.info("Finished scraping loop: iterations=" + iterations + " processed=" + processed );

        } catch (Exception e) {
            logger.severe("Error during per-element scraping: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private InstagramComment extractCommentDataWithJsoup(Element commentElement, String postId) {
        try {
            InstagramComment comment = new InstagramComment();
            // Extract username from the first anchor tag
            Element usernameElement = commentElement.selectFirst("a");
            System.out.println(usernameElement.html());
            String username = null;
            
            if (usernameElement != null && usernameElement.hasAttr("href")) {
                username = usernameElement != null ? usernameElement.attr("href").replace("/", ""): "Unknown";
                comment.setAuthorProfileUrl("https://instagram.com"+usernameElement.attr("href"));

                logger.info("Getting the username from comment -- value: "+username);

                comment.setUsername(username);
            }
            
                        

            // Extract comment text - try multiple selectors
            String commentText = "";
            
            // Try to find span with data-content attribute
            Element textElement = commentElement.selectFirst("span[data-content]");
            if (textElement != null) {
                commentText = textElement.text();
            } else {
                // Fallback: get all text from comment element and remove username
                commentText = commentElement.text().replace(username, "").trim();
            }

            String[] commentSplit= commentText.split(" ");
            commentText = "";
            String likesCount = null;
            for(int i = 2; i<commentSplit.length; i++){
                if (commentSplit[i].equals("curtidas")) {
                    likesCount = commentSplit[i-1];
                    commentText = commentText.replace(likesCount, "");
                    break;
                }
                commentText += commentSplit[i]+" ";
            }
            comment.setComment(commentText);
            if (likesCount != null) {
                System.out.println(likesCount);
                comment.setLikesCount(Long.parseLong(likesCount.replace(".", "")));    
            } else {
                comment.setLikesCount(0l);
            }

            // Set other fields
            comment.setPostId(postId);
            comment.setPostUrl(videoLink);
            comment.setScrapedAt(LocalDateTime.now());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setIsReply(false);

            return comment;

        } catch (Exception e) {
            logger.warning("Error extracting comment data: " + e.getMessage());
            return null;
        }
    }

    // scrollToLoadComments removed - per-element scrolling is handled in extractComments()

    private String extractPostIdFromUrl(String url) {
        // Extract post ID from Instagram URL
        // Format: https://www.instagram.com/reel/POSTID/
        try {
            String[] parts = url.split("/reel/");
            if (parts.length > 1) {
                return parts[1].split("/")[0];
            }
        } catch (Exception e) {
            logger.warning("Error extracting post ID from URL: " + e.getMessage());
        }
        return "UNKNOWN";
    }

    public List<InstagramComment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<InstagramComment> getCommentsByUsername(String username) {
        return commentRepository.findByUsername(username);
    }

    public long getTotalCommentCount(String postId) {
        return commentRepository.countCommentsByPost(postId);
    }

}
