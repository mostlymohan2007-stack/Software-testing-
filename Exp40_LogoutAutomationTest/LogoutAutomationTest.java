package selenium;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
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

public class LogoutAutomationTest {

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
    public void logout() throws Exception {
        // first valid account in practice-server/users.csv
        List<String> lines = Files.readAllLines(Path.of(System.getProperty("users", "practice-server/users.csv")));
        String[] account = lines.get(1).split(",");

        driver.get(System.getProperty("baseUrl", "http://127.0.0.1:8000") + "/portal/login");
        driver.findElement(By.id("txtusername")).sendKeys(account[0]);
        driver.findElement(By.id("txtpassword")).sendKeys(account[1]);
        driver.findElement(By.id("btnlogin")).click();
        System.out.println("After login : " + driver.findElement(By.id("welcome")).getText());
        saveScreenshot("Exp40_1_logged_in");

        driver.findElement(By.id("logout")).click();
        assertTrue(driver.findElement(By.id("btnlogin")).isDisplayed());
        System.out.println("After logout: " + driver.getTitle());
        saveScreenshot("Exp40_2_logged_out");
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
