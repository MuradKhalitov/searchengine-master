package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class Statistics {
    private TotalStatistics total;
    private List<DetailedStatistics> detailed;
}
