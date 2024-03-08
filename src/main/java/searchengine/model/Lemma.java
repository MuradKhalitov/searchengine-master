package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "lemma", nullable = false, length = 255)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;
}
