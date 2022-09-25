package com.gmail.marcosav2010.myfitnesspal.api;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SeleniumLoginHandler implements LoginHandler {

    private static final int LOGIN_WAIT_SECONDS = 10;

    private static final String EMAIL_FIELD_NAME = "email";
    private static final String PASSWORD_FIELD_NAME = "password";
    private static final String SUBMIT_BUTTON_XPATH = "//button[@type='submit']";

    @Override
    public Map<String, String> login(String url, String username, String password) throws LoginException {
        Map<String, String> cookies = new HashMap<>();
        RemoteWebDriver driver = null;

        try {
            driver = new FirefoxDriver();

            driver.get(url);

            // Avoid rejecting cookies manually
            Date expire = Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Cookie cookieGdpr = new Cookie("notice_gdpr_prefs", "0:", ".myfitnesspal.com", "/", expire, true);
            Cookie cookiePrefs = new Cookie("notice_preferences", "0:", ".myfitnesspal.com", "/", expire, true);
            driver.manage().addCookie(cookieGdpr);
            driver.manage().addCookie(cookiePrefs);

            driver.navigate().refresh();

            WebElement emailField = driver.findElement(By.name(EMAIL_FIELD_NAME));
            WebElement passwordField = driver.findElement(By.name(PASSWORD_FIELD_NAME));

            emailField.sendKeys(username.trim());
            passwordField.sendKeys(password);

            String lastUrl = driver.getCurrentUrl();

            WebElement submit = driver.findElement(By.xpath(SUBMIT_BUTTON_XPATH));
            submit.click();

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
}
