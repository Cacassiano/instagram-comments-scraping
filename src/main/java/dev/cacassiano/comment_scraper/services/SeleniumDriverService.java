package dev.cacassiano.comment_scraper.services;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.logging.Logger;

@Service
public class SeleniumDriverService {

    private static final Logger logger = Logger.getLogger(SeleniumDriverService.class.getName());

    @Value("${instagram.username}")
    private String instagramUsername;

    @Value("${instagram.password}")
    private String instagramPassword;

    @Value("${instagram.video_link}")
    private String videoLink;

    private WebDriver driver;

    public void initializeDriver() {
        logger.info("Initializing Selenium WebDriver...");

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");
        options.addArguments("--memory-pressure-off");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-renderer-backgrounding");
        //options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        this.driver = new ChromeDriver(options);
        logger.info("WebDriver initialized successfully");
    }

    public void loginToInstagram() throws InterruptedException {
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized. Call initializeDriver() first.");
        }

        logger.info("Starting Instagram login process...");

        // Navigate to Instagram
        driver.get("https://www.instagram.com/");
        Thread.sleep(3000); // Wait for page to load

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // Click on the username input field
            WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[name='username']")));

            usernameInput.sendKeys(instagramUsername);
            logger.info("Username entered");

            Thread.sleep(500);

            // Click on the password input field
            WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));
            passwordInput.sendKeys(instagramPassword);
            logger.info("Password entered");

            Thread.sleep(500);

            // Click the login button
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();
            logger.info("Login button clicked");

            Thread.sleep(10000); // Wait for login to complete

            // Wait for the page to redirect after login
            wait.until(ExpectedConditions.urlContains("instagram.com"));
            logger.info("Login successful");

        } catch (Exception e) {
            logger.severe("Login failed: " + e.getMessage());
            throw new RuntimeException("Instagram login failed", e);
        }
    }

    public void navigateToVideoLink() throws InterruptedException {
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized. Call initializeDriver() first.");
        }

        logger.info("Navigating to video link: " + videoLink);
        driver.get(videoLink);
        Thread.sleep(3000); // Wait for page to load
        logger.info("Video page loaded");
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public void quit() {
        if (driver != null) {
            logger.info("Closing WebDriver...");
            driver.quit();
            logger.info("WebDriver closed");
        }
    }

    public void scrollDown(int pixels) throws InterruptedException {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, " + pixels + ");");
        Thread.sleep(500);
    }

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

}
