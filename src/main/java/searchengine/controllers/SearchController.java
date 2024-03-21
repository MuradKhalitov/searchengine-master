package searchengine.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class SearchController {
    @GetMapping("/api/search")
    public String search(
            @RequestParam("query") String query,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        try {
            // List<SearchResult> results = searchService.search(query, site, offset, limit);
            // int totalCount = searchService.getTotalCount(query, site);
            return "{\n" +
                    "\t'result': true,\n" +
                    "\t'count': 574,\n" +
                    "\t'data': [\n" +
                    "\t\t{\n" +
                    "\t\t\t\"site\": \"http://www.site.com\",\n" +
                    "\t\t\t\"siteName\": \"Имя сайта\",\n" +
                    "\"uri\": \"/path/to/page/6784\",\n" +
                    "\t\t\t\"title\": \"Заголовок страницы,\n" +
                    "которую выводим\",\n" +
                    "\t\t\t\"snippet\": \"Фрагмент текста,\n" +
                    " в котором найдены \n" +
                    " совпадения, <b>выделенные \n" +
                    " жирным</b>, в формате HTML\",\n" +
                    "\t\t\t\"relevance\": 0.93362\n" +
                    "},\n" +
                    "...\n" +
                    "]\n" +
                    "}\n";
            //return new ApiResponse(true, totalCount, results);
        } catch (Exception e) {
            //return new ApiResponse(false, e.getMessage());
            return "{\n" +
                    "\t'result': false,\n" +
                    "\t'error': \"Задан пустой поисковый запрос\"\n" +
                    "}\n";
        }
    }
}
