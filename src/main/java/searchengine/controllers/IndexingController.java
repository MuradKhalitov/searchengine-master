package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.IndexingService;

@RestController
@RequestMapping("/api")
public class IndexingController {
    @Autowired
    private IndexingService indexingService;

    @GetMapping("/startIndexing")
    public String startIndexing() {
        boolean success = indexingService.startIndexing();
        if (success) {
            return "{\"result\": true}";
        } else {
            return "{\"result\": false, \"error\": \"Индексация уже запущена\"}";
        }
    }
}


