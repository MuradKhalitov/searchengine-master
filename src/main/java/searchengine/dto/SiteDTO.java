package searchengine.dto;

import jakarta.persistence.*;
import searchengine.model.Page;
import searchengine.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public class SiteDTO {
    private Status status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;
    private List<Page> page;
}
