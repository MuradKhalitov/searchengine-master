package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.ConfigSite;
import searchengine.config.ConfigSitesList;
import searchengine.dto.statistics.DetailedStatistics;
import searchengine.dto.statistics.Statistics;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final ConfigSitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setHowManySites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatistics> detailed = new ArrayList<>();
        List<ConfigSite> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            ConfigSite site = sitesList.get(i);
            DetailedStatistics item = new DetailedStatistics();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = random.nextInt(1_000);
            int lemmas = pages * random.nextInt(1_000);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(statuses[i % 3]);
            item.setError(errors[i % 3]);
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
            total.setHowManyPages(total.getHowManyPages() + pages);
            total.setHowManyLemmas(total.getHowManyLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        Statistics data = new Statistics();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
