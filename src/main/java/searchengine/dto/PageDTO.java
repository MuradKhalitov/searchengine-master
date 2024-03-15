package searchengine.dto;

import jakarta.persistence.*;
import searchengine.model.Indexing;
import searchengine.model.Site;

import java.util.List;

public class PageDTO {
    private SiteDTO siteDTO;
    private String path;
    private int code;
    private String content;
}
