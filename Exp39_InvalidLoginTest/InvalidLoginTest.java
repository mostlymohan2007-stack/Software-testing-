package selenium;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.Assert.*;

public class InvalidLoginTest {

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
    public void invalidLogin() throws Exception {
        driver.get(System.getProperty("baseUrl", "http://127.0.0.1:8000") + "/portal/login");
        driver.findElement(By.id("txtusername")).sendKeys("invaliduser");
        driver.findElement(By.id("txtpassword")).sendKeys("wrongpass");
        driver.findElement(By.id("btnlogin")).click();
        String error = driver.findElement(By.id("error")).getText();
        System.out.println("Error message: " + error);
        saveScreenshot("Exp39");
        assertTrue("Error message not found", error.toLowerCase().contains("invalid"));
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
