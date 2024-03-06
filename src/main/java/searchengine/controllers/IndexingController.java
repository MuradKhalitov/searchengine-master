package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.ConfigSitesList;
import searchengine.config.ConfigSite;
import searchengine.services.IndexingService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IndexingController {
    @Autowired
    private IndexingService indexingService;
    @Autowired
    private ConfigSitesList configSitesList;

    @GetMapping("/startIndexing")
    public String startIndexing() {
        List<ConfigSite> sites = configSitesList.getSites();
        boolean success = indexingService.startIndexing(sites);
        if (success) {
            return "{\"result\": true}";
        } else {
            return "{\"result\": false, \"error\": \"Индексация уже запущена\"}";
        }
    }

    @GetMapping("/stopIndexing")
    public String stopIndexing() {
        indexingService.stopIndexing();
        return "{\"result\": true}";
    }
}



