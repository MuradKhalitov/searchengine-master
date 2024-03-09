package searchengine.services;

import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.concurrent.*;

@Service
public class IndexingService {
    private static final int THREAD_POOL_SIZE = 10; // Размер пула потоков
    //ExecutorService executorService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private Set<String> visitedUrls;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    //@Transactional
    public boolean startIndexing(List<ConfigSite> configSites) throws InterruptedException {
        if (isIndexingInProgress()) {
            return false;
        }
        // Удаляем все данные из таблицы
        pageRepository.deleteAll();
        siteRepository.deleteAll();


        // Создаем записи в таблице site со статусом INDEXING
        for (ConfigSite configSite : configSites) {
            Site site = new Site();
            site.setUrl(configSite.getUrl());
            site.setName(configSite.getName());
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            //siteRepository.save(site);
            visitedUrls = new HashSet<>();
            //executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            indexSite(site);
        }
        //executorService.shutdown();
        //executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return true;
    }

    private void indexSite(Site site) {
        try {
            String baseUrl = site.getUrl();
            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
            pageCrawler(baseUrl, site);
        } catch (IOException | InterruptedException e) {
            // Если произошла ошибка, обновляем статус сайта и сохраняем информацию об ошибке
            site.setStatus(Status.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError(e.getMessage());
            siteRepository.save(site);
        }

    }

//    private void pageCrawler(String url, Site site) throws IOException, InterruptedException {
//// Проверяем, была ли уже посещена данная страница
//        if (!visitedUrls.contains(url)) {
//            // Добавляем URL в список посещенных
//            visitedUrls.add(url);
//            savePage(site, url);
//            try {
//                Document doc = Jsoup.connect(url)
//                        .userAgent("HeliontSearchBot")
//                        .referrer("http://www.google.com")
//                        .get();
//                System.out.println("Обход: " + url);
//                Elements links = doc.select("a[href]");
//                // Обходим все ссылки на страницы сайта
//                ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//                for (Element link : links) {
//                    String absUrl = link.absUrl("href");
//                    if (!absUrl.isEmpty()) {
//                        if (absUrl.startsWith("http://") || absUrl.startsWith("https://")) {
//                            if (absUrl.contains(site.getUrl())) {
//                                executorService.execute(() -> {
//                                    try {
//                                        pageCrawler(absUrl, site);
//                                    } catch (IOException | InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                });
//                            }
//                        }
//                    }
//                }
//                executorService.shutdown();
//                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
private void pageCrawler(String url, Site site) throws IOException, InterruptedException {
    if (!visitedUrls.contains(url)) {
        visitedUrls.add(url);
        savePage(site, url);
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();
            System.out.println("Обход: " + url);
            Elements links = doc.select("a[href]");

            ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_POOL_SIZE);
            forkJoinPool.invoke(new PageCrawlerTask(links, site));
            forkJoinPool.shutdown();
            forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
        // Остановка выполнения всех потоков в ForkJoinPool
        ForkJoinPool.commonPool().shutdownNow();
        // Обновляем статусы всех сайтов, на которых обход ещё не завершён
        List<Site> sites = siteRepository.findByStatus(Status.INDEXING);
        for (Site site : sites) {
            site.setStatus(Status.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    }
    private class PageCrawlerTask extends RecursiveAction {
        private final Elements links;
        private final Site site;

        public PageCrawlerTask(Elements links, Site site) {
            this.links = links;
            this.site = site;
        }

        @Override
        protected void compute() {
            for (Element link : links) {
                String absUrl = link.absUrl("href");
                if (!absUrl.isEmpty() && absUrl.startsWith("http://") || absUrl.startsWith("https://")) {
                    if (absUrl.contains(site.getUrl())) {
                        try {
                            pageCrawler(absUrl, site);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}

