package selenium;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.*;

public class GoogleSearchAutomationTest {

    private WebDriver driver;

    @Before
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,800");
        if (Boolean.getBoolean("headless")) options.addArguments("--headless=new");
        driver = new ChromeDriver(options);   // Selenium locates ChromeDriver automatically
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    public void googleSearch() throws Exception {
        driver.get(System.getProperty("url", "https://www.google.com/"));
        driver.findElement(By.name("q")).sendKeys("Saveetha School of Engineering" + Keys.ENTER);
        Thread.sleep(4000);
        boolean captcha = driver.getCurrentUrl().contains("/sorry/");
        System.out.println("Page title: " + (captcha ? "(Google verification page)" : driver.getTitle()));
        // A CAPTCHA is meant to stop automated browsers, so the script does not try to get past it.
        Assume.assumeFalse("Google showed a CAPTCHA to the automated browser", captcha);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getTitle().contains("Saveetha"));
        saveScreenshot("Exp37");
        assertTrue(driver.getTitle().contains("Saveetha"));
    }

    private void saveScreenshot(String name) throws Exception {
        File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.createDirectories(Path.of("screenshots"));
        Files.copy(shot.toPath(), Path.of("screenshots", name + ".png"), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Screenshot saved: screenshots/" + name + ".png");
    }

    @After
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }
}
