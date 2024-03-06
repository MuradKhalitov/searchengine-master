package searchengine.services;

import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.util.List;
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
            siteRepository.save(site);

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

            // Ваш код для обработки страницы и сохранения её в базу данных

            // Пример:
            Page page = new Page();
            page.setSite(site);
            page.setPath(site.getUrl());
            page.setStatus(Status.INDEXED);
            page.setContent(doc.body().text());
            pageRepository.save(page);

            // Обработка ссылок и дальнейший обход страницы
        } catch (IOException e) {
            e.printStackTrace();
            site.setStatus(Status.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
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

