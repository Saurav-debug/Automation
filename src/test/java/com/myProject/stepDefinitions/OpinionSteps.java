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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static com.myProject.utilities.Driver.driver;

public class OpinionSteps {
    private final List<Map<String, String>> articleDataList = new ArrayList<>();
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

        for (int i = 0; i < 5 && i < articles.size(); i++) {
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
    public void theTitlesAreTranslatedToEnglish() throws IOException, InterruptedException {
        if (articleDataList.isEmpty()) {
            System.out.println("No articles available for translation.");
            return;
        }

        for (Map<String, String> articleData : articleDataList) {
            String title = articleData.get("title");

            // Translate title
            String translatedTitle = APIUtils.translateText(title, "en");

            // Remove special characters from the beginning and end
            translatedTitle = translatedTitle.replaceAll("^\\p{P}+", "").replaceAll("\\p{P}+$", "");

           // Add translated title to the article data
            articleData.put("translatedTitle", translatedTitle);


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
            if (articleData == null) {
                System.out.println("Skipping null article data entry.");
                continue;
            }

            String translatedTitle = articleData.get("translatedTitle");
            if (translatedTitle == null || translatedTitle.isEmpty()) {
                System.out.println("Skipping article due to null or empty translated title.");
                continue;
            }

            // Split into words and add to the list
            String[] words = translatedTitle.split("\\s+");
            allWords.addAll(Arrays.asList(words));
        }

        if (allWords.isEmpty()) {
            System.out.println("No valid words found to analyze for repeated words.");
            return;
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

        System.out.println("Repeated words analysis completed successfully.");
    }

    @Given("Convert language")
    public static String translateText() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://rapid-translate-multi-traduction.p.rapidapi.com/t"))
                .header("x-rapidapi-key", "ea0803d75fmsha8ca955d52b3e20p159cdcjsnd3fcae71a28d")
                .header("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com")
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\"from\":\"en\",\"to\":\"es\",\"q\":\"Hello ! Rapid Translate Multi Traduction\"}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        return response.body();
    }
}
