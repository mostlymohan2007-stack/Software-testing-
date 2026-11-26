package selenium;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.*;

public class JavaScriptAlertTest {

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
    public void handleAlert() throws Exception {
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
        driver.findElement(By.xpath("//button[text()='Click for JS Alert']")).click();
        Alert alert = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.alertIsPresent());
        System.out.println("Alert text: " + alert.getText());
        alert.accept();
        String result = driver.findElement(By.id("result")).getText();
        System.out.println("Result    : " + result);
        saveScreenshot("Exp43");
        assertTrue(result.contains("You successfully clicked an alert"));
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
