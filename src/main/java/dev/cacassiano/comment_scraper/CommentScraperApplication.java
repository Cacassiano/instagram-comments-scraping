package dev.cacassiano.comment_scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommentScraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommentScraperApplication.class, args);
	}

}
