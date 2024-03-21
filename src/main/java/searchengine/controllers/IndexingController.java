package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import searchengine.config.ConfigSite;
import searchengine.config.ConfigSitesList;
import searchengine.dto.ErrorResponse;
import searchengine.dto.Response;
import searchengine.model.Site;
import searchengine.services.IndexingService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IndexingController {
    private IndexingService indexingService;
    private ConfigSitesList configSitesList;
    public IndexingController(IndexingService indexingService, ConfigSitesList configSitesList) {
        this.indexingService = indexingService;
        this.configSitesList = configSitesList;
    }

    @GetMapping("/startIndexing")
    public Response startIndexing() throws InterruptedException {
        List<ConfigSite> sites = configSitesList.getSites();
        boolean success = indexingService.startIndexing(sites);
        if (success){
            new Response();
        }
     return new ErrorResponse("Индексация не была запущена");
    }
    @PostMapping("/indexPage")
    public Response indexPage(@RequestParam(required = false) String url) {
        Site site = new Site();
        site.setUrl(url);
        site.setName(url);
        boolean success = indexingService.indexSite(site);
        if (success) {
            return new Response();
        }
        return new ErrorResponse("Индексация не была запущена");
    }

    @GetMapping("/stopIndexing")
    public Response stopIndexing() {
        boolean success = indexingService.stopIndexing();
        if (success){
            return new Response();
        }
        return new ErrorResponse("Индексация не была запущена");
    }

}
