package searchengine.model;


import jakarta.persistence.*;
import lombok.Data;

@Data

@Entity
@Table(name = "index")
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(name = "lemma_id", nullable = false)
    private Long lemmaId;

    @Column(name = "rank", nullable = false)
    private float rank;
}
