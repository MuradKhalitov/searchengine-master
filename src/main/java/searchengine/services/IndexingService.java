package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.ConfigSite;
import searchengine.model.*;
import searchengine.repository.Repos;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class IndexingService {
    private static final int THREAD_POOL_SIZE = 16; // Размер пула потоков
    private static final int MAX_INDEXING_PAGE = 10000;
    private static int indexingPageCount = 0;
    //ExecutorService executorService;

    private Boolean stop = false;
    private Set<String> visitedUrls;

    public boolean startIndexing(List<ConfigSite> configSites) throws InterruptedException {
        if (isIndexingInProgress()) {
            return false;
        }
        stop = false;
        // Удаляем все данные из таблицы
        Repos.indexRepo.deleteAll();
        Repos.lemmaRepo.deleteAll();
        Repos.pageRepo.deleteAll();
        Repos.siteRepo.deleteAll();

        Long start = System.currentTimeMillis();
        // Создаем записи в таблице site со статусом INDEXING
        for (ConfigSite configSite : configSites) {
            Site site = new Site();
            site.setUrl(configSite.getUrl());
            site.setName(configSite.getName());
            site.setType(Site.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            visitedUrls = new HashSet<>();
            indexSite(site);
            System.out.println("Индексация " + site.getUrl() + " " + ((System.currentTimeMillis() - start) / 60000.00f) + " min");
        }
        return true;
    }

    public boolean indexSite(Site site) {
        try {
            indexingPageCount = 0;
            String baseUrl = site.getUrl();
            site.setType(Site.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            Repos.siteRepo.save(site);
            Document doc = Jsoup.connect(site.getUrl())
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();
            pageCrawler(baseUrl, site, doc);
        } catch (IOException | InterruptedException e) {
            // Если произошла ошибка, обновляем статус сайта и сохраняем информацию об ошибке
            site.setType(Site.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError(e.getMessage());
            Repos.siteRepo.save(site);
            return false;
        }
return true;
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
    private void pageCrawler(String url, Site site, Document doc) throws IOException, InterruptedException {
        if (!stop || indexingPageCount < MAX_INDEXING_PAGE) {
            if (!visitedUrls.contains(url) && !isFileLink(url)) {
                visitedUrls.add(url);
                savePage(site, url, doc);
                indexingPageCount++;
                try {
                    Document document = Jsoup.connect(url)
                            .userAgent("HeliontSearchBot")
                            .referrer("http://www.google.com")
                            .get();
                    System.out.println("Обход: " + url);
                    Elements links = doc.select("a[href]");

                    ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_POOL_SIZE);
                    forkJoinPool.invoke(new PageCrawlerTask(links, site, document));
                    forkJoinPool.shutdown();
                    forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else {
            ForkJoinPool.commonPool().shutdown();

            try {
                // Ждем завершения всех потоков
                ForkJoinPool.commonPool().awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                // Обработка возможного исключения
                e.printStackTrace();
            }
        }
    }

    private void savePage(Site site, String url, Document doc) {
        try {
            //Document doc = Jsoup.connect(url).get();
            String path = url.substring(site.getUrl().length() - 1); // Относительный путь страницы
            String content = doc.html(); // Код страницы

            Page page = new Page();
            page.setSite(site);
            page.setPath(path);
            page.setCode(200); // Прошли успешно
            page.setContent(content);
            Repos.pageRepo.save(page);
            lemmatizeText(page, site, doc);
        } catch (IOException e) {
            // Если произошла ошибка при получении страницы, сохраняем информацию о ней с кодом ошибки
            Page page = new Page();
            page.setSite(site);
            page.setPath(url);
            page.setCode(-1); // Код ошибки при получении страницы
            page.setContent(e.getMessage());
            Repos.pageRepo.save(page);
        }
    }
    private void lemmatizeText(Page page, Site site, Document doc) throws IOException {
        // Удаление всех HTML-тегов, кроме разрешенных
        Safelist Whitelist = null;
        String text = Jsoup.clean(doc.body().html(), Whitelist.simpleText());

        // Создаем экземпляр класса для морфологического анализа русского языка
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        // Удаляем все знаки препинания и символы, оставляя только слова
        text = text.replaceAll("[^а-яА-Я\\s]", "")
                // Замена последовательностей пробелов на одиночные пробелы
                .replaceAll("(^\\s+|\\s+$|\\s+\\s+)", " ")
                .replaceAll("(^\\s+)", "");

        // Разделяем текст на отдельные слова
        String[] words = text.split("\\s+");

        // Создаем HashMap для хранения лемм и их количества упоминаний
        Map<String, Integer> lemmaCountMap = new HashMap<>();

        // Проходимся по каждому слову в тексте
        for (String word : words) {
            // Получаем базовые формы слова
            List<String> baseForms = luceneMorph.getNormalForms(word.toLowerCase());

            // Проходимся по каждой базовой форме слова
            for (String baseForm : baseForms) {
                // Добавляем базовую форму слова в карту и увеличиваем счетчик упоминаний
                lemmaCountMap.put(baseForm, lemmaCountMap.getOrDefault(baseForm, 0) + 1);

            }

        }
        // Выводим результаты
        for (Map.Entry<String, Integer> entry : lemmaCountMap.entrySet()) {
            Lemma lemma = new Lemma();
            lemma.setLemma(entry.getKey());
            lemma.setSite(site);
            lemma.setFrequency(entry.getValue());
            Repos.lemmaRepo.save(lemma);
            Index index = new Index();
            index.setLemma(lemma);
            index.setPage(page);
            index.setRank(entry.getValue());
            Repos.indexRepo.save(index);

            //System.out.println(entry.getKey() + " — " + entry.getValue());
        }
    }

    private boolean isIndexingInProgress() {
        List<Site> sites = Repos.siteRepo.findAllByType(Site.INDEXING);
        return !sites.isEmpty();
    }

    public boolean stopIndexing() {
        // Остановка выполнения всех потоков в ForkJoinPool
        //ForkJoinPool.commonPool().shutdownNow();
        stop = true;
        // Обновляем статусы всех сайтов, на которых обход ещё не завершён
        List<Site> sites = Repos.siteRepo.findAllByType(Site.INDEXING);
        for (Site site : sites) {
            site.setType(Site.FAILED);
            site.setStatusTime(LocalDateTime.now());
            Repos.siteRepo.save(site);
        }
        return true;
    }

    private boolean isFileLink(String url) {
        String[] parts = url.split("\\.");
        if (parts.length > 1) {
            String extension = parts[parts.length - 1];
            // Список расширений файлов, которые нужно исключить (например, pdf, jpg, png и т.д.)
            String[] excludedExtensions = {"pdf", "jpg", "jpeg", "png", "gif", "doc", "docx", "xls", "xlsx"};
            for (String ext : excludedExtensions) {
                if (extension.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    private class PageCrawlerTask extends RecursiveAction {
        private final Elements links;
        private final Site site;
        private final Document doc;

        public PageCrawlerTask(Elements links, Site site, Document doc) {
            this.links = links;
            this.site = site;
            this.doc = doc;
        }

        @Override
        protected void compute() {
            for (Element link : links) {
                String absUrl = link.absUrl("href");
                if (absUrl.contains(site.getUrl())) {
                    try {
                        pageCrawler(absUrl, site, doc);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}

