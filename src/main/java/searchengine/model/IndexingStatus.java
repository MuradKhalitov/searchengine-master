package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
public class IndexingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean indexingInProgress;

    public IndexingStatus(boolean indexingInProgress) {
        this.indexingInProgress = indexingInProgress;
    }
}
