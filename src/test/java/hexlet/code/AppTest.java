package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {
    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;
    private static MockWebServer mockServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url("https://www.deepl.com");
        existingUrl.save();

        File file = new File("src/test/resources/expected.html");
        String expected = file.toString();

        mockServer = new MockWebServer();
        mockServer.enqueue(new MockResponse().setBody(expected));
        mockServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testGetUrls() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        System.out.println(body);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(existingUrl.getName());
    }

    @Test
    void testShow() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + existingUrl.getId())
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(existingUrl.getName());
    }

    @Test
    void testAddUrl() {
        String url = "https://www.youtube.com";
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", url)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(url);
        assertThat(body).contains("Страница успешно добавлена");

        Url listOfUrls = new QUrl()
                .name.equalTo(url)
                .findOne();

        assertThat(listOfUrls).isNotNull();
        assertThat(listOfUrls.getName()).isEqualTo(url);
    }

    @Test

    public void testCheckUrl() {

        String mockUrl = mockServer.url("/").toString();

        HttpResponse httpResponse = Unirest.post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asEmpty();

        assertThat(httpResponse.getStatus()).isEqualTo(302);

        Url actualUrl = new QUrl()
                .name.equalTo(mockUrl.substring(0, mockUrl.length() - 1))
                .findOne();

        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls/" + actualUrl.getId() + "/check")
                .asEmpty();

        assertThat(response.getStatus()).isEqualTo(302);

    }
}
