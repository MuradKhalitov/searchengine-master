package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.services.WebCrawler;

import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        try {
            URL url = new URL("https://www.playback.ru");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000); // Устанавливаем таймаут соединения в 10 секунд
            connection.setReadTimeout(10000); // Устанавливаем таймаут чтения в 10 секунд

            //String baseUrl = "https://www.lenta.ru";
            WebCrawler webCrawler = new WebCrawler(url.toString());
            webCrawler.crawl();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
