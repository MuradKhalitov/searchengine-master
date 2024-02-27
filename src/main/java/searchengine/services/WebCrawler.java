package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {

    private static final int MAX_DEPTH = 2; // Максимальная глубина поиска
    private static final int MAX_THREADS = 10; // Максимальное количество потоков

    private final Set<String> visitedUrls = new HashSet<>(); // Список посещенных URL
    private final String baseUrl; // Базовый URL сайта

    public WebCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void crawl() {
        crawlPage(baseUrl, 0);
    }

    private void crawlPage(String url, int depth) {
        if (depth >= MAX_DEPTH || visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);

        try {
            Document document = Jsoup.connect(url).get();
            System.out.println("Обход: " + url);

            Elements links = document.select("a[href]");

            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!nextUrl.isEmpty()) {
                    if (nextUrl.startsWith("http://") || nextUrl.startsWith("https://")) {
                        executor.execute(() -> crawlPage(nextUrl, depth + 1));
                    } else {
                        //System.out.println("Пропуск неподдерживаемого URL-адреса: " + nextUrl);
                    }
                }
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

