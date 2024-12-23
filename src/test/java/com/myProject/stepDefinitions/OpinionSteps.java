package com.myProject.stepDefinitions;

import com.myProject.pages.OpinionPage;
import com.myProject.utilities.APIUtils;
import com.myProject.utilities.ConfigurationReader;
import com.myProject.utilities.Driver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.*;

import static com.myProject.utilities.Driver.driver;

public class OpinionSteps {
    private final List<Map<String, String>> articleDataList = new ArrayList<>();
    private final Map<String, Integer> wordCount = new HashMap<>();
    OpinionPage opinionPage = new OpinionPage();


    @Given("User navigates to the Opinion section page")
    public void userNavigatesToTheOpinionSectionOn() {
        Driver.get().get(ConfigurationReader.get("url"));
        String expectedUrl = "https://elpais.com/";
        Assert.assertEquals(expectedUrl, Driver.get().getCurrentUrl());
        System.out.println("Launched the page");

    }

    @When("User retrieves the first five article titles")
    public void userRetrievesTheFirstFiveArticleTitlesAndContent() {
        opinionPage.handleCookiePopup();
        WebElement opinionSection = driver.findElement(By.linkText("Opinión"));
        opinionSection.click();
        System.out.println("Clicked on Opinions");

        List<WebElement> articles = driver.findElements(By.tagName("article"));

        for (int i = 0; i < 1 && i < articles.size(); i++) {
            WebElement article = articles.get(i);

            // Extract title
            WebElement titleElement = article.findElement(By.cssSelector("h2.c_t a"));
            String title = titleElement.getText();

            // Extract article URL using JavascriptExecutor
            String articleUrl = "";
            try {
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                articleUrl = (String) jsExecutor.executeScript("return arguments[0].href;", titleElement);
            } catch (Exception e) {
                System.out.println("No URL found for Article " + (i + 1));
            }

            // Extract content
            String content = "";
            try {
                WebElement contentElement = article.findElement(By.cssSelector(".c_d"));
                content = contentElement.getText();
            } catch (Exception e) {
                System.out.println("No content found for Article " + (i + 1));
            }

            // Extract image URL using JavascriptExecutor
            String imageUrl = "";
            try {
                WebElement imageElement = article.findElement(By.cssSelector("img.c-post__media"));
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                imageUrl = (String) jsExecutor.executeScript("return arguments[0].src;", imageElement);
            } catch (Exception e) {
                System.out.println("No image found for Article " + (i + 1));
            }

            // Save article data
            Map<String, String> articleData = new HashMap<>();
            articleData.put("title", title);
            articleData.put("content", content);
            articleData.put("url", articleUrl);
            articleData.put("imageUrl", imageUrl);
            articleDataList.add(articleData);

            // Save image if URL is valid
            assert imageUrl != null;
            if (!imageUrl.isEmpty()) {
                opinionPage.saveImage(imageUrl, "images/article_" + (i + 1) + ".jpg");
            } else {
                System.out.println("Article " + (i + 1) + " does not have an image.");
            }

            System.out.println("Article " + (i + 1) + ": " + title);
            System.out.println("URL: " + articleUrl);
            System.out.println("Content: " + content);
            System.out.println("Image URL: " + imageUrl);
            System.out.println("---------------");
        }
    }
    @Then("The titles are translated to English")
    public void theTitlesAreTranslatedToEnglish() {
        if (articleDataList.isEmpty()) {
            System.out.println("No articles available for translation.");
            return;
        }

        for (Map<String, String> articleData : articleDataList) {
            String title = articleData.get("title");

            // Translate title
            String translatedTitle = APIUtils.translateText(title, "en");

            System.out.println("Original Title: " + title);
            System.out.println("Translated Title: " + translatedTitle);
            System.out.println("---------------");
        }
    }

    @Then("Repeated words in titles are logged")
    public void identifyRepeatedWordsFromTranslatedTitles() {
        if (articleDataList.isEmpty()) {
            System.out.println("No articles available to check for repeated words.");
            return;
        }

        // Collect all words from translated titles
        List<String> allWords = new ArrayList<>();
        for (Map<String, String> articleData : articleDataList) {
            String translatedTitle = articleData.get("translatedTitle");
            if (translatedTitle == null) {
                continue; // Skip if translatedTitle is null
            }

            String[] words = translatedTitle.split("\\s+"); // Split into words
            allWords.addAll(Arrays.asList(words)); // Add all words to the list
        }

        // Count word occurrences
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : allWords) {
            word = word.toLowerCase().replaceAll("[^a-zA-Z]", ""); // Clean the word
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        // Print words that appear more than twice
        System.out.println("Repeated words with count:");
        wordCount.forEach((word, count) -> {
            if (count > 2) {
                System.out.println(word + ": " + count);
            }
        });
    }
}
