package searchengine.services;

import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.ConfigSite;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class IndexingService {
    private static final int THREAD_POOL_SIZE = 10; // Размер пула потоков
    private ExecutorService executorService; // Пул потоков для индексации сайтов
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;
    private Set<String> visitedUrls;
    public IndexingService() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Transactional
    public boolean startIndexing(List<ConfigSite> configSites) {
        if (isIndexingInProgress()) {
            return false;
        }

        // Удаляем все данные из таблицы
        siteRepository.deleteAll();
        pageRepository.deleteAll();

        // Создаем записи в таблице site со статусом INDEXING
        for (ConfigSite configSite : configSites) {
            Site site = new Site();
            site.setUrl(configSite.getUrl());
            site.setName(configSite.getName());
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            //siteRepository.save(site);
            visitedUrls = new HashSet<>();

            executorService.submit(() -> indexSite(site));
        }

        return true;
    }

    private void indexSite(Site site) {
        try {
            Document doc = Jsoup.connect(site.getUrl())
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();
            String baseUrl = site.getUrl();
            Elements links = doc.select("a[href]");
            // Обходим все ссылки на страницы сайта
            for (Element link : links) {
                String href = link.attr("href");
                if (!href.isEmpty()) {
                    if (href.startsWith("http://") || href.startsWith("https://")) {
                        // Определяем абсолютный URL страницы
                        String absUrl = link.absUrl("href");
                        pageCrawler(absUrl, site);

                        // Сохраняем страницу в базу данных
                        //savePage(site, absUrl);
                    }
                }
            }

            // После завершения обхода всех страниц обновляем статус сайта
            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        } catch (IOException e) {
            // Если произошла ошибка, обновляем статус сайта и сохраняем информацию об ошибке
            site.setStatus(Status.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError(e.getMessage());
            siteRepository.save(site);
        }
    }

    private void pageCrawler(String url, Site site) throws IOException {
// Проверяем, была ли уже посещена данная страница
        if (!visitedUrls.contains(url)) {
            // Добавляем URL в список посещенных
            visitedUrls.add(url);
            Document doc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();
            String baseUrl = url;
            Elements links = doc.select("a[href]");
            // Обходим все ссылки на страницы сайта
            for (Element link : links) {
                String href = link.attr("href");
                if (!href.isEmpty()) {
                    if (href.startsWith("http://") || href.startsWith("https://")) {
                        // Определяем абсолютный URL страницы
                        String absUrl = link.absUrl("href");

                        // Сохраняем страницу в базу данных
                        savePage(site, href);
                        pageCrawler(absUrl, site);
                    }
                }

            }
        }
    }

    private void savePage(Site site, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String path = url.substring(site.getUrl().length()); // Относительный путь страницы
            String content = doc.toString(); // Код страницы

            Page page = new Page();
            page.setSite(site);
            page.setPath(path);
            page.setCode(200); // Прошли успешно
            page.setContent(content);
            pageRepository.save(page);
        } catch (IOException e) {
            // Если произошла ошибка при получении страницы, сохраняем информацию о ней с кодом ошибки
            Page page = new Page();
            page.setSite(site);
            page.setPath(url);
            page.setCode(-1); // Код ошибки при получении страницы
            page.setContent(e.getMessage());
            pageRepository.save(page);
        }
    }

    private boolean isIndexingInProgress() {
        List<Site> sites = siteRepository.findByStatus(Status.INDEXING);
        return !sites.isEmpty();
    }

    public void stopIndexing() {
        executorService.shutdownNow();
        // Обновляем статусы всех сайтов, на которых обход ещё не завершён
        List<Site> sites = siteRepository.findByStatus(Status.INDEXING);
        for (Site site : sites) {
            site.setStatus(Status.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    }
}

