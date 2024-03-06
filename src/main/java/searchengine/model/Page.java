package searchengine.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "page", indexes = @Index(name = "path_index", columnList = "path"))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "path", columnDefinition = "TEXT")
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
}

