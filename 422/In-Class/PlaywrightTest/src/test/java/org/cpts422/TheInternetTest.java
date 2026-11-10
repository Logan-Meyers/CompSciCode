package org.cpts422;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class TheInternetTest {
    static final String BASE_URL = "https://the-internet.herokuapp.com";

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false).setSlowMo(1000));
    }

    @BeforeEach
    void createContextPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @Test
    @DisplayName("Login page shows heading")
    void loginPageShowsHeading() {
        page.navigate(BASE_URL + "/login");
        //assertThat(page.locator("h2")).hasText("Login Page");
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Login Page"))).isVisible();
        //page.pause();
    }

    void login(String username, String password) {
        page.navigate(BASE_URL + "/login");
        page.getByLabel("Username").fill(username);
        page.getByLabel("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
    }

    @Test
    @DisplayName("Login with correct credentials")
    void loginWithCorrectCredentials() {
        login("tomsmith", "SuperSecretPassword!");
        assertThat(page).hasURL(BASE_URL + "/secure");
        assertThat(page.locator("#flash")).containsText("You logged into a secure area!");
    }

    @Test
    @DisplayName("Login with incorrect username")
    void loginWithIncorrectUsername() {
        login("tomsmith1", "SuperSecretPassword!");
        assertThat(page).hasURL(BASE_URL + "/login");
        assertThat(page.locator("#flash")).containsText("Your username is invalid!");
    }

    @Test
    @DisplayName("Login with incorrect password")
    void loginWithIncorrectPassword() {
        login("tomsmith", "SuperSecretPassword!123");
        assertThat(page).hasURL(BASE_URL + "/login");
        assertThat(page.locator("#flash")).containsText("Your password is invalid!");
    }

    @Test
    @DisplayName("Select option from dropdown")
    void dropdownSelectOption() {
        page.navigate(BASE_URL + "/dropdown");
        Locator dropdown = page.locator("#dropdown");
        dropdown.selectOption("Option 2");
        assertThat(dropdown).hasValue("2");
    }

    @Test
    @DisplayName("Toggle checkboxes")
    void checkboxesToggle() {
        page.navigate(BASE_URL + "/checkboxes");
        Locator boxes = page.getByRole(AriaRole.CHECKBOX);
        boxes.first().check();
        boxes.nth(1).check();
        assertThat(boxes.first()).isChecked();
        assertThat(boxes.nth(1)).isChecked();
    }

    @Test
    @DisplayName("Dynamic loading")
    void dynamicLoadingTest() {
        page.navigate(BASE_URL + "/dynamic_loading/1");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Start")).click();
        assertThat(page.getByText("Hello World!")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));
    }
}