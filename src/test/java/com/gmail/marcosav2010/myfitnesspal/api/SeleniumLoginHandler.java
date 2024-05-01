package com.gmail.marcosav2010.myfitnesspal.api;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class SeleniumLoginHandler implements LoginHandler {

    private static final int LOGIN_WAIT_SECONDS = 10;

    private static final String EMAIL_FIELD_NAME = "//input[@name='email']";
    private static final String PASSWORD_FIELD_NAME = "password";
    private static final String SUBMIT_BUTTON_XPATH = "//button[@type='title']";

    private final boolean headless;

    public SeleniumLoginHandler() {
        this(true);
    }

    public SeleniumLoginHandler(boolean headless) {
        this.headless = headless;
    }

    @Override
    public Map<String, String> login(
            String url,
            String username,
            String password) throws LoginException {
        if (username == null || password == null || url == null)
            throw new IllegalArgumentException("URL, username and password must not be null");

        if (username.isBlank() || password.isBlank() || url.isBlank())
            throw new IllegalArgumentException("URL, username and password must not be blank");

        Map<String, String> cookies = new HashMap<>();
        RemoteWebDriver driver = null;

        try {
            FirefoxOptions options = new FirefoxOptions();
            options.setHeadless(headless);
            driver = new FirefoxDriver(options);

            driver.get(url);

            var sel = By.xpath("//iframe[@title='SP Consent Message']");

            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.findElement(sel));

            driver.switchTo().frame(driver.findElement(sel));

            WebElement b = driver.findElement(By.xpath("//button[@title='AGREE AND PROCEED']"));
            b.click();

            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(d -> false);

            WebElement emailField = driver.findElement(By.xpath(EMAIL_FIELD_NAME));
            WebElement passwordField = driver.findElement(By.name(PASSWORD_FIELD_NAME));

            emailField.sendKeys(username.trim());
            passwordField.sendKeys(password);

            String lastUrl = driver.getCurrentUrl();

            /*WebElement submit = driver.findElement(By.xpath(SUBMIT_BUTTON_XPATH));
            submit.click();*/

            new WebDriverWait(driver, Duration.ofSeconds(100000))
                    .until(d -> false);

            new WebDriverWait(driver, Duration.ofSeconds(LOGIN_WAIT_SECONDS))
                    .until(d -> !d.getCurrentUrl().equalsIgnoreCase(lastUrl));

            String postLoginUrl = driver.getCurrentUrl();

            if (postLoginUrl.contains("error")) {
                String[] cause = postLoginUrl.split("error=");
                if (cause.length == 2 && cause[1].equalsIgnoreCase("CredentialsSignin"))
                    throw new LoginException("Incorrect username or password");
                else
                    throw new LoginException("Unable to sign in, try again later or with a different account");
            }

            driver.manage().getCookies().forEach(c -> cookies.put(c.getName(), c.getValue()));

        } catch (LoginException ex) {
            throw ex;

        } catch (TimeoutException ex) {
            throw new LoginException("Timed out, log in took more than " + LOGIN_WAIT_SECONDS + " seconds");

        } catch (Exception ex) {
            throw new LoginException("Unexpected error while logging in", ex);

        } finally {
            if (driver != null)
                driver.quit();
        }

        return cookies;
    }

    static void setDriverProperties() {
        var driverPath = System.getProperty("user.dir") + "/src/test/resources/geckodriver";
        System.out.println("Setting geckodriver to " + driverPath);
        System.setProperty("webdriver.gecko.driver", driverPath);
    }
}
