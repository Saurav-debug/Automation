package com.myProject.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.interactions.Actions;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.myProject.utilities.Driver.driver;

public class OpinionPage  {

    // Use @FindBy annotation for better maintainability and ease of locating elements
    @FindBy(css = "h3.c-post__title")
    private List<WebElement> articleTitles;

    // Initialize elements with PageFactory
    public OpinionPage() {
        PageFactory.initElements(driver, this);
    }
    public void handleCookiePopup() {
        try {
            // Wait for the cookie accept button to appear
            WebElement cookieAcceptButton = driver.findElement(By.id("didomi-notice-agree-button")); // Use the correct CSS selector for the cookie accept button
            cookieAcceptButton.click();
        } catch (Exception e) {
            // If cookie pop-up doesn't appear, continue execution
        }
    }
    public void saveImage(String imageUrl, String fileName) {
        try {
            // Get the user's Downloads folder path
            String downloadsFolder = System.getProperty("user.home") + File.separator + "Downloads";

            // Create the full file path
            File file = new File(downloadsFolder, fileName);

            // Download and save the image
            URL url = new URL(imageUrl);
            ImageIO.write(ImageIO.read(url), "jpg", file);

            System.out.println("Image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Getter method for article titles
    public List<WebElement> getArticleTitles() {
        return articleTitles;
    }

    // Example method demonstrating interaction with elements using Actions class
    public void hoverOverFirstArticleTitle() {
        Actions actions = new Actions(driver);
        actions.moveToElement(articleTitles.get(0)).perform();  // Hover over the first article title
    }
}
