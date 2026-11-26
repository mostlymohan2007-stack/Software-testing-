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
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.*;

public class DropdownSelectionTest {

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
    public void selectFromDropdown() throws Exception {
        driver.get("https://www.w3schools.com/tags/tryit.asp?filename=tryhtml_select");
        driver.switchTo().frame("iframeResult");
        Select select = new Select(driver.findElement(By.name("cars")));
        select.selectByVisibleText("Saab");
        String selected = select.getFirstSelectedOption().getText();
        System.out.println("Selected value: " + selected);
        saveScreenshot("Exp41");
        assertEquals("Saab", selected);
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
