package searchengine.dto.statistics;

import lombok.Data;

@Data
public class TotalStatistics {
    private int howManySites;
    private int howManyPages;
    private int howManyLemmas;
    private boolean indexing;
}
